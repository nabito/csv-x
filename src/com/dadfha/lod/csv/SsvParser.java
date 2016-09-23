package com.dadfha.lod.csv;

import static com.univocity.parsers.csv.UnescapedQuoteHandling.RAISE_ERROR;
import static com.univocity.parsers.csv.UnescapedQuoteHandling.SKIP_VALUE;
import static com.univocity.parsers.csv.UnescapedQuoteHandling.STOP_AT_CLOSING_QUOTE;
import static com.univocity.parsers.csv.UnescapedQuoteHandling.STOP_AT_DELIMITER;

import com.univocity.parsers.common.AbstractParser;
import com.univocity.parsers.common.TextParsingException;
import com.univocity.parsers.common.input.DefaultCharAppender;
import com.univocity.parsers.common.input.ExpandingCharAppender;
import com.univocity.parsers.common.input.NoopCharAppender;
import com.univocity.parsers.csv.CsvFormat;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.UnescapedQuoteHandling;

/**
 * Based on CsvParser implementation. Use of this parser will set the delimiter to ' '.
 * However the parser behavior is different from normal delimited-parser in that it 
 * will regard a consecutive chunk of delimiters as one.
 * 
 * @author Wirawit
 *
 */
public class SsvParser extends AbstractParser<CsvParserSettings> {
	
	private final boolean ignoreTrailingWhitespace;
	private final boolean ignoreLeadingWhitespace;
	private boolean parseUnescapedQuotes;
	private boolean parseUnescapedQuotesUntilDelimiter;	
	private final boolean doNotEscapeUnquotedValues;
	private final boolean keepEscape;
	private final boolean keepQuotes;	
	
	private char prev;
	private char delimiter;
	private char quote;
	private char quoteEscape;
	private final char escapeEscape;
	private final char newLine;
	private final DefaultCharAppender whitespaceAppender;
	private final boolean normalizeLineEndingsInQuotes;
	private UnescapedQuoteHandling quoteHandling;
	private final String nullValue;
	private final int maxColumnLength;	

	public SsvParser(CsvParserSettings settings) {
		super(settings);
		ignoreTrailingWhitespace = settings.getIgnoreTrailingWhitespaces();
		ignoreLeadingWhitespace = settings.getIgnoreLeadingWhitespaces();
		parseUnescapedQuotes = settings.isParseUnescapedQuotes();
		parseUnescapedQuotesUntilDelimiter = settings.isParseUnescapedQuotesUntilDelimiter();
		doNotEscapeUnquotedValues = !settings.isEscapeUnquotedValues();
		keepEscape = settings.isKeepEscapeSequences();
		keepQuotes = settings.getKeepQuotes();		
		normalizeLineEndingsInQuotes = settings.isNormalizeLineEndingsWithinQuotes();
		nullValue = settings.getNullValue();
		maxColumnLength = settings.getMaxCharsPerColumn();
		
		CsvFormat format = settings.getFormat();
		delimiter = ' '; // delimiter of SSV is always a space ' '
		quote = format.getQuote();
		quoteEscape = format.getQuoteEscape();
		escapeEscape = format.getCharToEscapeQuoteEscaping();
		newLine = format.getNormalizedNewline();

		whitespaceAppender = new ExpandingCharAppender(10, "");		

		this.quoteHandling = settings.getUnescapedQuoteHandling();
		if (quoteHandling == null) {
			if (parseUnescapedQuotes) {
				if (parseUnescapedQuotesUntilDelimiter) {
					quoteHandling = STOP_AT_DELIMITER;
				} else {
					quoteHandling = STOP_AT_CLOSING_QUOTE;
				}
			} else {
				quoteHandling = RAISE_ERROR;
			}
		} else {
			parseUnescapedQuotesUntilDelimiter = quoteHandling == STOP_AT_DELIMITER || quoteHandling == SKIP_VALUE;
			parseUnescapedQuotes = quoteHandling != RAISE_ERROR;
		}		
		
	}
	
	@Override
	protected final void parseRecord() {
		if (ignoreLeadingWhitespace && ch <= ' ') {
			ch = input.skipWhitespace(ch, quote, quote); // skipping not stops on empty character ' '
		}

		while (ch != newLine) {
			if (ignoreLeadingWhitespace && ch <= ' ') {
				ch = input.skipWhitespace(ch, quote, quote);
			}

			if (ch == newLine) { // removed delimiter check here
				output.emptyParsed();
			} else {
				prev = '\0';
				if (ch == quote) {
					output.trim = false;
					if (normalizeLineEndingsInQuotes) {
						parseQuotedValue();
					} else {
						input.enableNormalizeLineEndings(false);
						parseQuotedValue();
						input.enableNormalizeLineEndings(true);
					}
					output.valueParsed();
				} else if (doNotEscapeUnquotedValues) { // this defaults to true
					//System.out.println("Entered doNotEscapeUnquotedValues");
					String value = null;
					if (output.appender.length() == 0) {
						value = input.getString(ch, delimiter, ignoreTrailingWhitespace, nullValue, maxColumnLength);
					}
					if (value != null) {
						output.valueParsed(value);
						ch = input.getChar();
					} else {
						output.trim = ignoreTrailingWhitespace;
						ch = output.appender.appendUntil(ch, input, delimiter, newLine);
						output.valueParsed();
					}
				} else {
					output.trim = ignoreTrailingWhitespace;
					parseValueProcessingEscape();
					output.valueParsed();
				}
			}
			if (ch != newLine) {
				ch = input.nextChar();
				if (ch == newLine) { // may be this condition is not necessary for Ssv
					output.emptyParsed();
				}
			}
		}
	}	
	
	private void parseQuotedValue() {
		if (prev != '\0' && parseUnescapedQuotesUntilDelimiter) {
			if (quoteHandling == SKIP_VALUE) {
				skipValue();
				return;
			}
			if (!keepQuotes) {
				output.appender.prepend(quote);
			}
			ch = input.nextChar();
			output.trim = ignoreTrailingWhitespace;
			ch = output.appender.appendUntil(ch, input, delimiter, newLine);
		} else {
			if (keepQuotes && prev == '\0') {
				output.appender.append(quote);
			}
			while (true) {
				ch = input.nextChar();

				if (prev == quote && (ch <= ' ' || ch == delimiter || ch == newLine)) {
					break;
				}

				if (ch != quote && ch != quoteEscape) {
					if (prev == quote) { //unescaped quote detected
						if (handleUnescapedQuote()) {
							break;
						} else {
							return;
						}
					}
					ch = output.appender.appendUntil(ch, input, quote, quoteEscape, escapeEscape);
				} else {
					processQuoteEscape();
				}
				prev = ch;
			}

			// handles whitespaces after quoted value: whitespaces are ignored. Content after whitespaces may be parsed if 'parseUnescapedQuotes' is enabled.
			if (ch != delimiter && ch != newLine && ch <= ' ') {
				whitespaceAppender.reset();
				do {
					//saves whitespaces after value
					whitespaceAppender.append(ch);
					ch = input.nextChar();
					//found a new line, go to next record.
					if (ch == newLine) {
						return;
					}
				} while (ch <= ' ');

				//there's more stuff after the quoted value, not only empty spaces.
				if (ch != delimiter && parseUnescapedQuotes) {
					if (output.appender instanceof DefaultCharAppender) {
						//puts the quote before whitespaces back, then restores the whitespaces
						output.appender.append(quote);
						((DefaultCharAppender) output.appender).append(whitespaceAppender);
					}
					//the next character is not the escape character, put it there
					if (parseUnescapedQuotesUntilDelimiter || ch != quote && ch != quoteEscape) {
						output.appender.append(ch);
					}

					//sets this character as the previous character (may be escaping)
					//calls recursively to keep parsing potentially quoted content
					prev = ch;
					parseQuotedValue();
				} else if (keepQuotes) {
					output.appender.append(quote);
				}
			} else if (keepQuotes) {
				output.appender.append(quote);
			}

			if (ch != delimiter && ch != newLine) {
				throw new TextParsingException(context, "Unexpected character '" + ch + "' following quoted value of CSV field. Expecting '" + delimiter + "'. Cannot parse CSV input.");
			}
		}
	}
	
	private void skipValue() {
		output.appender.reset();
		ch = NoopCharAppender.getInstance().appendUntil(ch, input, delimiter, newLine);
	}

	private void handleValueSkipping(boolean quoted) {
		switch (quoteHandling) {
			case SKIP_VALUE:
				skipValue();
				break;
			case RAISE_ERROR:
				throw new TextParsingException(context, "Unescaped quote character '" + quote
						+ "' inside " + (quoted ? "quoted" : "") + " value of CSV field. To allow unescaped quotes, set 'parseUnescapedQuotes' to 'true' in the CSV parser settings. Cannot parse CSV input.");
		}
	}	
	
	private void parseValueProcessingEscape() {
		while (ch != delimiter && ch != newLine) {
			if (ch != quote && ch != quoteEscape) {
				if (prev == quote) { //unescaped quote detected
					handleUnescapedQuoteInValue();
					return;
				}
				output.appender.append(ch);
			} else {
				processQuoteEscape();
			}
			prev = ch;
			ch = input.nextChar();
		}
	}	
	
	private void handleUnescapedQuoteInValue() {
		switch (quoteHandling) {
			case STOP_AT_CLOSING_QUOTE:
			case STOP_AT_DELIMITER:
				output.appender.append(quote);
				prev = ch;
				parseValueProcessingEscape();
				break;
			default:
				handleValueSkipping(false);
				break;
		}
	}

	private boolean handleUnescapedQuote() {
		switch (quoteHandling) {
			case STOP_AT_CLOSING_QUOTE:
			case STOP_AT_DELIMITER:
				output.appender.append(quote);
				output.appender.append(ch);
				prev = ch;
				parseQuotedValue();
				return true; //continue;
			default:
				handleValueSkipping(true);
				return false;
		}
	}

	private void processQuoteEscape() {
		if (ch == quoteEscape && prev == escapeEscape && escapeEscape != '\0') {
			if (keepEscape) {
				output.appender.append(escapeEscape);
			}
			output.appender.append(quoteEscape);
			ch = '\0';
		} else if (prev == quoteEscape) {
			if (ch == quote) {
				if (keepEscape) {
					output.appender.append(quoteEscape);
				}
				output.appender.append(quote);
				ch = '\0';
			} else {
				output.appender.append(prev);
			}
		} else if (ch == quote && prev == quote) {
			output.appender.append(quote);
		}
	}	
	
	
}