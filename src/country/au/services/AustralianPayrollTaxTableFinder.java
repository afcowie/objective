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
import generic.persistence.Selector;
import generic.util.DebugException;

import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import accounts.domain.Datestamp;
import accounts.services.Finder;
import accounts.services.NotFoundException;
import country.au.domain.AustralianPayrollTaxIdentifier;
import country.au.domain.AustralianPayrollTaxTable;

public class AustralianPayrollTaxTableFinder extends Finder
{
    protected transient AustralianPayrollTaxIdentifier scale;

    protected transient Datestamp date;

    private transient List result;

    /**
     * @param scale
     *            The deduction scale Identifier you want to look up
     * @param date
     *            The effective date you need data for.
     */
    public AustralianPayrollTaxTableFinder(AustralianPayrollTaxIdentifier scale, Datestamp date) {
        super();
        setScale(scale);
        setDate(date);
    }

    public void setScale(AustralianPayrollTaxIdentifier scale) {
        if (scale == null) {
            throw new IllegalArgumentException("Can't use null as tax scale");
        }
        this.scale = scale;
        reset();
    }

    public void setDate(Datestamp date) {
        if (date == null) {
            throw new IllegalArgumentException("Can't use null as the pay date you're looking up");
        }
        this.date = date;
        reset();
    }

    /**
     * Implements Finder, but is really just here to be used by the domain
     * specific methods. In this case, we <b>only</b>get [all] the tax tables
     * that match a given scale; the getTaxTables() method then attempts to
     * pick one that is valid for the date given.
     * 
     * @throws NotFoundException
     *             In the event you've asked for tax table data but there is
     *             none stored for the data/identifier parameters you've set.
     */
    /*
     * query() returns a list but also caches a reference internally.
     */
    public List query(DataClient store) throws NotFoundException {
        /*
         * Although a queryByExample() would work here, guard against the
         * possibility that an object from a different DataClient is being
         * used by using DomainObject.congruent()
         */
        List l = store.nativeQuery(new Selector<AustralianPayrollTaxTable>(scale) {
            public boolean match(AustralianPayrollTaxTable table) {
                if (table.getScale().congruent(target)) {
                    return true;
                } else {
                    return false;
                }
            }
        });
        if (l.size() == 0) {
            throw new NotFoundException();
        }

        this.result = l;
        return l;
    }

    /**
     * Either Having called query() manually, or, just letting this call it
     * for you, we then iterate through the found TaxTables looking for one
     * which works for the specified date.
     * 
     * @return Coefficient data: an array of 3-wide arrays of doubles.
     */
    public double[][] getCoefficients() throws NotFoundException {
        if (result == null) {
            throw new DebugException("Need to call query() first");
        }

        /*
         * No reason to assume they're sorted any particular way, so put them
         * into a TreeMap which will sort them for us since Datestamp
         * implements Comparable.
         */

        TreeMap taxTables = new TreeMap();

        Iterator iter = result.iterator();
        while (iter.hasNext()) {
            AustralianPayrollTaxTable table = (AustralianPayrollTaxTable) iter.next();
            taxTables.put(table.getEffectiveDate(), table);
        }

        /*
         * We then loop over the [ordered] keys until we find one that is
         * newer than the date we were passed, then return the table data.
         */
        Datestamp which = null;

        iter = taxTables.keySet().iterator();

        while (iter.hasNext()) {
            Datestamp d = (Datestamp) iter.next();
            if (d.compareTo(date) > 0) {
                break;
            }
            which = d;
        }

        if (which == null) {
            throw new NotFoundException("We don't have tax table data for the date you asked for");
        }

        AustralianPayrollTaxTable chosen = (AustralianPayrollTaxTable) taxTables.get(which);
        return chosen.getCoefficients();
    }

    /**
     * Clear the cached result; query() will be run again as necessary.
     */
    protected void reset() {
        this.result = null;
    }
}
