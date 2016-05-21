package com.dadfha.lod;

// Original concept of JSON Minifier: http://www.crockford.com/javascript/jsmin.html
// Father of JSON: https://en.wikipedia.org/wiki/Douglas_Crockford allows comments in JSON but not when transferring.

// JSONMinify - JSON Minifier (removes comments and whitespace from JSON/JSON+C) that JUST WORKS.
//
// Made by Stefan Reich because all the other minifiers just didn't work.
// http://jsonminify.tinybrain.de
// stefan.reich.maker.of.eye@gmail.com
// v1, March 2014
// License: Public domain
//
// Notes. -
//
// The performance of this class  is pretty much perfect. Except for the fairly generous creation of one String object
// per iteration ("cc"), there is NO overhead. I'd probably give it an award for "cleanest code" too. So yeah... I think
// I'm happy with it. Performance class is (of course!) O(n) with n being the number of input characters. ^^
//
// Please tell me how it works for you :)
//
// The syntax we accept is called "JSON+C" (regular JSON plus JavaScript-style comments),
// which means this minifier works on all regular JSON documents too.
// You could thusly also say that this minifier translates JSON+C to regular JSON.

public class JSONMinify {
  public static String minify(String jsonString) {
    boolean in_string = false;
    boolean in_multiline_comment = false;
    boolean in_singleline_comment = false;
    char string_opener = 'x'; // unused value, just something that makes compiler happy

    StringBuilder out = new StringBuilder();
    for (int i = 0; i < jsonString.length(); i++) {
      // get next (c) and next-next character (cc)

      char c = jsonString.charAt(i);
      String cc = jsonString.substring(i, Math.min(i+2, jsonString.length()));

      // big switch is by what mode we're in (in_string etc.)
      if (in_string) {
        if (c == string_opener) {
          in_string = false;
          out.append(c);
        } else if (c == '\\') { // no special treatment needed for \\u, it just works like this too
          out.append(cc);
          ++i;
        } else
          out.append(c);
      } else if (in_singleline_comment) {
        if (c == '\r' || c == '\n')
          in_singleline_comment = false;
      } else if (in_multiline_comment) {
        if (cc.equals("*/")) {
          in_multiline_comment = false;
          ++i;
        }
      } else {
        // we're outside of the special modes, so look for mode openers (comment start, string start)
        if (cc.equals("/*")) {
          in_multiline_comment = true;
          ++i;
        } else if (cc.equals("//")) {
          in_singleline_comment = true;
          ++i;
        } else if (c == '"' || c == '\'') {
          in_string = true;
          string_opener = c;
          out.append(c);
        } else if (!Character.isWhitespace(c))
          out.append(c);
      }
    }
    return out.toString();
  }
}