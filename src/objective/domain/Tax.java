/*
 * ObjectiveAccounts, accounting for small professional services firms.
 *
 * Copyright © 2011 Operational Dynamics Consulting, Pty Ltd
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
package objective.domain;

/**
 * Australian GST tax codes
 */
public class Tax
{
    private String code;

    private String name;

    private double rate;

    public Tax(String code, String name, double rate) {
        super();
        setCode(code);
        setName(name);
        setRate(rate);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        if (code.length() != 3) {
            throw new IllegalArgumentException("Tax codes are, by our convention, three characters long");
        }
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("The currency name can't be null");
        }
        if (name.length() == 0) {
            throw new IllegalArgumentException("The currency name can't be empty");
        }
        this.name = name;
    }

    public void setRate(double rate) {
        if ((rate < 0.0) || (rate > 1.0)) {
            throw new IllegalArgumentException("The rate must be a % between 0.0 and 1.0");
        }
        this.rate = rate;
    }

    public double getRate() {
        return rate;
    }
}
