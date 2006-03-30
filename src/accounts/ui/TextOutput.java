/*
 * TextOutput.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.ui;

import generic.util.Environment;

import java.io.PrintStream;
import java.io.PrintWriter;

class Alignment
{
}

public abstract class TextOutput
{
	/**
	 * Terminal width, in character cells. Pulled from environment variable
	 * "COLUMNS" if it set, otherwise a default value is used.
	 */
	public static final int			COLUMNS;
	private static final int		COLUMNS_DEFAULT	= 80;
	protected static final int		COLUMNS_MIN		= 50;

	/**
	 * Specify left alignment for whatever you are outputting or padding.
	 */
	public static final Alignment	LEFT;
	/**
	 * Specify right alignment for whatever you are outputting or padding.
	 */
	public static final Alignment	RIGHT			= new Alignment();

	static {
		LEFT = new Alignment();

		String env = Environment.getenv("COLUMNS");
		if (env == null) {
			COLUMNS = COLUMNS_DEFAULT;
		} else {
			int val = Integer.valueOf(env).intValue();
			if (val < COLUMNS_MIN) {
				throw new IllegalStateException("Terminal too narrow for TextOutput. Min width " + COLUMNS_MIN
					+ " characters.");
			}
			COLUMNS = val;
		}
	}

	/**
	 * Wrapper around toOutput(PrintWriter) to which you can easily pass an old
	 * school PrintStream
	 * 
	 * @param out
	 *            a PrintStream like System.out or System.err
	 */
	public final void toOutput(PrintStream out) {
		toOutput(new PrintWriter(out, true));

	}

	public abstract void toOutput(PrintWriter out);

	/**
	 * @param str
	 *            the String to pad.
	 * @param width
	 *            maximum length of the padded result.
	 * @param justify
	 *            if RIGHT, right justify. If LEFT, normal left justification.
	 * @return the padded String.
	 */
	public static String pad(String str, int width, Alignment justify) {
		String trimmed = null;
		/*
		 * crop
		 */
		int len;
		if (str == null) {
			len = 0;
			trimmed = "";
		} else {
			len = str.length();

			if (len > width) {
				trimmed = str.substring(0, width);
				len = width;
			} else {
				trimmed = str;
			}
		}
		int spaces = width - len;

		/*
		 * pad
		 */
		StringBuffer buf = new StringBuffer("");
		if (justify == LEFT) {
			buf.append(trimmed);
		}
		for (int i = 0; i < spaces; i++) {
			buf.append(" ");
		}
		if (justify == RIGHT) {
			buf.append(trimmed);
		}
		return buf.toString();
	}

	/**
	 * If argument is longer than width, then trim it back and add three dots.
	 */
	public static String chomp(String str, int width) {
		if (width < 4) {
			throw new IllegalArgumentException(
				"Can't chomp to less than a width of 4 because of adding three dots as an elipses");
		}
		if (str == null) {
			return "";
		}
		/*
		 * crop
		 */
		int len = str.length();
		if (len > width) {
			StringBuffer buf = new StringBuffer(str.substring(0, width - 3));
			final int end = buf.length() - 1;
			if (buf.charAt(end) == ' ') {
				buf.deleteCharAt(end);
			}
			buf.append("...");
			return buf.toString();
		} else {
			return str;
		}
	}

}