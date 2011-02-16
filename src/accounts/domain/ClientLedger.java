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
package accounts.domain;

import java.util.Set;

public class ClientLedger extends DebitPositiveLedger implements ItemsLedger
{
    private Client client;

    public ClientLedger() {
        super();
    }

    public ClientLedger(Client client) {
        super();
        String name = client.getName();
        if ((name == null) || name.equals("")) {
            throw new IllegalArgumentException("Client object must at least have its name filled in");
        }
        super.setName(name);
        this.client = client;
    }

    public Set getItems() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setItems(Set items) {
    // TODO Auto-generated method stub

    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public String getClassString() {
        return "Client Ledger";
    }
}
