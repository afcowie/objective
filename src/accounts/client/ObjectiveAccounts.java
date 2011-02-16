/*
 * ObjectiveAccounts, accounting for small professional services firms.
 *
 * Copyright © 2005-2011 Operational Dynamics Consulting, Pty Ltd
 *
 * The code in this file, and the program it is a part of, is made available
 * to you by its authors as open source software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License version
 * 2 ("GPL") as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GPL for more details.
 *
 * You should have received a copy of the GPL along with this program. If not,
 * see http://www.gnu.org/licenses/. The authors of this program may be
 * contacted through http://research.operationaldynamics.com/projects/objective/.
 */
package accounts.client;

/**
 * A reentry point for the whole framework. Holds a number of "global"
 * variables for use throughout the program. See the JavaDoc for the public
 * static members of this class. See also {@link generic.client.Master} for
 * the general reentry point for program wide actions (ie shutdown) and access
 * to global UI actions.
 * 
 * @author Andrew Cowie
 */
public class ObjectiveAccounts
{
    /**
     * A String constant expressing the current version of the overall
     * released codebase. Used by equivalence (the top level
     * <code>./configure</code> program) to find out the version to use in
     * the names of release and snapshot tarballs. Small helper programs
     * making use of this class do not need to override this constant.
     */
    public static final String VERSION = "0.1.17";

    /**
     * The copyright statement for this code and a pointer to the top level
     * <code>LICENCE</code> file. Iterate over this array and print with
     * newlines or {@link org.gnu.gtk.Label}s if you will, but this is here
     * to embed it in distributed bytecode.
     */
    public static final String[] COPYRIGHT = new String[] {
            "Copyright © 2005-2011 Operational Dynamics Consulting, Pty Ltd and Others.",
            "The LICENCE file included with these sources lists the terms under which",
            "you may use and redistribute this code.",
    };

    /**
     * Get a statement about the copyright of the program code. [If you want
     * to do something other than print this to a terminal, then get the
     * individual lines by accessing the COPYRIGHT String array directly; it's
     * public.]
     * 
     * @return the embedded copyright statement joined together into a single
     *         String with its lines separated by newlines.
     */
    public static String getCopyrightText() {
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < COPYRIGHT.length; i++) {
            buf.append(COPYRIGHT[i]);
            buf.append("\n");
        }

        return buf.toString();
    }
}
