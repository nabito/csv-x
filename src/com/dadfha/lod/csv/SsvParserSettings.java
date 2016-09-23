package com.dadfha.lod.csv;

import com.univocity.parsers.common.CommonParserSettings;

public class SsvParserSettings extends CommonParserSettings<SsvFormat> {

	@Override
	protected SsvFormat createDefaultFormat() {
		return null;
	}

}
