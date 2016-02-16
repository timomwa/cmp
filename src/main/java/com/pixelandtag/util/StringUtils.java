package com.pixelandtag.util;

import java.util.Vector;

public class StringUtils {

	/**
	 * Split String.
	 * USed for SMS
	 * @param input
	 * @return
	 */
	public static Vector<String> splitText(String input) {
		Vector<String> ret = new Vector<String>();
		if (input.trim().length() <= 250) {
			ret.add(input.trim());
			return ret;
		}
		while (true) {
			input = input.trim();
			if (input.length() <= 250) {
				ret.add(input);
				break;
			}
			int pos = 156;

			while ((input.charAt(pos) != ' ') && (input.charAt(pos) != '\n')
					&& (pos > 0))
				pos--;
			if (pos == 0)
				pos = 250;
			String tmp = input.substring(0, pos);
			ret.add(tmp);
			input = input.substring(pos);
		}

		return ret;
	}
}
