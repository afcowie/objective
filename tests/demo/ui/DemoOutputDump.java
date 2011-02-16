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
package demo.ui;

import generic.persistence.DataClient;
import generic.persistence.Engine;
import generic.ui.TextOutput;

import java.io.FileNotFoundException;

import accounts.domain.Books;
import accounts.ui.AccountTextOutput;
import accounts.ui.TransactionTextOutput;
import demo.client.DemoBooksSetup;

/**
 * Use the toOuput() routine to dump the demo database.
 * 
 * @author Andrew Cowie
 */
public class DemoOutputDump
{
    public static void main(String[] args) {
        TextOutput outputter = null;

        try {
            Engine.openDatafile(DemoBooksSetup.DEMO_DATABASE, Books.class);
        } catch (FileNotFoundException fnfe) {
            System.err.println("\nDemo database not found! Did you run DemoBooksSetup?\n");
            System.exit(1);
        }

        DataClient ro = Engine.primaryClient();
        try {
            Books root = (Books) ro.getRoot();

            System.out.println();

            /*
             * First output all the Accounts
             */

            outputter = new AccountTextOutput(ro);
            outputter.toOutput(System.out);

            /*
             * And now output all the Transactions
             */
            System.out.println();

            outputter = new TransactionTextOutput(ro);
            outputter.toOutput(System.out);

            System.out.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }

        Engine.shutdown();
    }
}
