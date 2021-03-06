/*
 * ObjectiveAccounts, accounting for small professional services firms.
 *
 * Copyright © 2006-2011 Operational Dynamics Consulting, Pty Ltd
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
package country.au.services;

import generic.persistence.DataClient;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import objective.domain.Datestamp;
import objective.services.NotFoundException;

import accounts.services.Command;
import accounts.services.CommandNotReadyException;
import accounts.services.CommandNotUndoableException;
import country.au.domain.AustralianPayrollTaxIdentifier;
import country.au.domain.AustralianPayrollTaxTable;

/**
 * Store or update the Australian PAYG tax table data. This Command acts
 * somewhat like a big static { } block would - its mostly here as somewhere
 * to do the mundane job of specifiy the coefficient data that make up the
 * PAYG tables.
 * <p>
 * As subsequent year's data comes along, it can be added here; reexecuting an
 * instance of this Command will update the data sets in the DataClient.
 * 
 * @author Andrew Cowie
 */
public class StoreAustralianPayrollTaxTablesCommand extends Command
{
    /**
     * A simple collection of the data that we will subsequently iterate over
     * when we store the TaxTable objects themselves.
     */
    private transient List data = new ArrayList(2);

    public StoreAustralianPayrollTaxTablesCommand() {
        AustralianPayrollTaxTable table;

        /*
         * Each line of the table is a max salary (<=) and then the
         * coefficients a and b to be used at or below that point.
         */
        // Scale 1
        table = new AustralianPayrollTaxTable(AustralianPayrollTaxIdentifier.NO_TAXFREE_THRESHOLD,
                new Datestamp("1 Jul 05"), 4);
        table.addCoefficients(98.0, 0.1650, 0.1650);
        table.addCoefficients(894, 0.3150, 14.8765);
        table.addCoefficients(1509, 0.4340, 122.1842);
        table.addCoefficients(Double.MAX_VALUE, 0.4850, 197.6650);

        data.add(table);

        // Scale 2
        table = new AustralianPayrollTaxTable(
                AustralianPayrollTaxIdentifier.TAXFREE_THRESHOLD_WITH_LEAVE_LOADING, new Datestamp(
                        "1 Jul 05"), 7);
        table.addCoefficients(109, 0, 0);
        table.addCoefficients(301, 0.1522, 16.7186);
        table.addCoefficients(325, 0.3553, 77.8907);
        table.addCoefficients(409, 0.1675, 16.7438);
        table.addCoefficients(1205, 0.3150, 77.1053);
        table.addCoefficients(1820, 0.4350, 221.7515);
        table.addCoefficients(Double.MAX_VALUE, 0.485, 312.7899);

        data.add(table);

        // Scale 4
        table = new AustralianPayrollTaxTable(AustralianPayrollTaxIdentifier.NO_TFN_PROVIDED,
                new Datestamp("1 Jul 05"), 1);
        table.addCoefficients(Double.MAX_VALUE, 0.4850, 0.5);

        data.add(table);

        // Scale 7
        table = new AustralianPayrollTaxTable(
                AustralianPayrollTaxIdentifier.TAXFREE_THRESHOLD_NO_LEAVE_LOADING, new Datestamp(
                        "1 Jul 05"), 7);
        table.addCoefficients(111, 0, 0);
        table.addCoefficients(305, 0.1500, 16.7308);
        table.addCoefficients(330, 0.3500, 77.8923);
        table.addCoefficients(415, 0.1650, 16.7320);
        table.addCoefficients(1211, 0.3150, 79.0397);
        table.addCoefficients(1826, 0.4350, 224.4243);
        table.addCoefficients(Double.MAX_VALUE, 0.4850, 315.7705);

        data.add(table);
    }

    protected void action(DataClient store) throws CommandNotReadyException {
        /*
         * Pull up any existing instances of the existing table data, so we
         * can respectively replace them.
         */
        Iterator iter = data.iterator();
        while (iter.hasNext()) {
            AustralianPayrollTaxTable t = (AustralianPayrollTaxTable) iter.next();

            try {
                AustralianPayrollTaxTableFinder f = new AustralianPayrollTaxTableFinder(t.getScale(),
                        t.getEffectiveDate());
                List result = f.query(store);

                if (result.size() == 1) {
                    /*
                     * If we found a table by this scale,date combination,
                     * then replace that object with our "new" one; when we
                     * save() in a moment the reference will get updated.
                     * Presumably they'll be the same... but this is how we
                     * fix old bad data.
                     */
                    AustralianPayrollTaxTable current = (AustralianPayrollTaxTable) result.get(0);
                    current = t;
                } else if (result.size() > 1) {
                    throw new IllegalStateException(
                            "More than one tax table was returned when querying by a given scale and date. How did you pull that off?");
                }
            } catch (NotFoundException nfe) {
                // no problem.
            }

            store.save(t);
        }
    }

    protected void reverse(DataClient store) throws CommandNotUndoableException {
        throw new CommandNotUndoableException();
    }

    public String getClassString() {
        return "Store(Update) Australian Payroll Tax Tables";
    }

}
