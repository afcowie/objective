/*
 * Text.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 * 
 * Word wrap algorithm GPL code imported from xseq.ui.OverviewWindow
 * Copyright (c) 2004-2005 Operational Dynamics
 */
package generic.ui;

/**
 * This class also has numerous static methods with useful routines for doing
 * basic formatting on Strings.
 * 
 * @author Andrew Cowie
 */
public abstract class Text
{
	/**
	 * @param str
	 *            the String to pad.
	 * @param width
	 *            maximum length of the padded result.
	 * @param justify
	 *            if RIGHT, right justify. If LEFT, normal left justification.
	 * @return the padded String.
	 */
	public static String pad(String str, int width, Align justify) {
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
		if (justify == Align.LEFT) {
			buf.append(trimmed);
		}
		for (int i = 0; i < spaces; i++) {
			buf.append(" ");
		}
		if (justify == Align.RIGHT) {
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

	/**
	 * Carry out manual word wrap on a String. Normalizes all \n and \t
	 * characters to spaces, trims leading and training whitespace, and then
	 * inserts \n characters to "wrap" at the specified width boundary.
	 * <p>
	 * This code came about because unfortunately, Pango markup has no syntax
	 * for expressing auto word wrap, and worse, GtkCellRendererText has no
	 * ability to wrap text. So we (ick) do it by hand.
	 * 
	 * @return a single String with newline characters inserted at appropriate
	 *         points to cause the effect of wrapping at the specified width.
	 */
	public static String wrap(String str, int width) {
		StringBuffer buf = new StringBuffer(str);
		int index;
		/*
		 * normalize any existing IFS characters to spaces
		 */
		while ((index = buf.indexOf("\n")) != -1) {
			buf.setCharAt(index, ' ');
		}
		while ((index = buf.indexOf("\t")) != -1) {
			buf.setCharAt(index, ' ');
		}
		/*
		 * trim. Yes, I know about String.trim(), but we've already done half
		 * the work it does; no need to be inefficient.
		 */
		while (buf.charAt(0) == ' ') {
			buf.deleteCharAt(0);
		}
		while (buf.charAt(buf.length() - 1) == ' ') {
			buf.deleteCharAt(buf.length() - 1);
		}

		while ((index = buf.indexOf("  ")) != -1) {
			buf.deleteCharAt(index);
		}

		/*
		 * word wrap.
		 */
		int next_space = 0;
		int line_start = 0;

		while (next_space != -1) {
			if ((next_space - line_start) > width) {
				buf.setCharAt(next_space, '\n');
				line_start = next_space;
			}
			next_space = buf.indexOf(" ", next_space + 1); // bounds?
		}

		return buf.toString();
	}
}
