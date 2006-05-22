/*
 * Align.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package generic.ui;

/**
 * Alignment constants, for convenience. Use object indentity, ie
 * 
 * <pre>
 *          if (justify == Align.LEFT) { ... }
 * </pre>
 * 
 * or just grab a reference to use locally or statically in your class.
 * <p>
 * Originally created for {@link TextOutput}, now generally available for
 * anything that needs to specifiy alignment.
 * 
 * @author Andrew Cowie
 */
public final class Align
{
	private String	name;

	private Align(String name) {
		this.name = name;
	}

	/**
	 * Specify that you want to align whatever you are formatting or laying to
	 * or on the left; or, use the left slot in a layout.
	 */
	public static final Align	LEFT	= new Align("LEFT");

	/**
	 * Indicate that you want to align whatever you are formatting with a
	 * central tendancy, or, use the center slot in a layout (assuming, of
	 * course that there is one).
	 */
	public static final Align	CENTER	= new Align("CENTER");

	/**
	 * Specify that you want to align whatever you are formatting or laying to
	 * or on the right; or, use the right slot in a layout.
	 */
	public static final Align	RIGHT	= new Align("RIGHT");

	/*
	 * Just for debugging
	 */
	public String toString() {
		return "Align." + name;
	}
}
