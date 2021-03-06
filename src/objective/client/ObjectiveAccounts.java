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
 * contacted via http://research.operationaldynamics.com/projects/objective/.
 */
package objective.client;

/**
 * A reentry point for the whole framework. Holds a number of "global"
 * variables for use throughout the program. See the JavaDoc for the public
 * static members of this class.
 * 
 * @author Andrew Cowie
 */
public class ObjectiveAccounts
{
    /**
     * A String constant expressing the current version of the overall
     * released codebase. Used by equivalence (the top level
     * <code>./configure</code> program) to find out the version to use in the
     * names of release and snapshot tarballs. Small helper programs making
     * use of this class do not need to override this constant.
     */
    public static final String VERSION = "0.1.18";
}
