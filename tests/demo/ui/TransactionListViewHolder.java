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
package demo.ui;

import generic.client.Master;
import generic.persistence.DataClient;
import generic.persistence.Engine;
import generic.ui.PrimaryWindow;

import java.util.List;

import accounts.domain.Transaction;
import accounts.ui.TransactionListView;

public class TransactionListViewHolder extends PrimaryWindow
{
    public TransactionListViewHolder() {
        super();
        super.setTitle("Example display of TransactionListView widget");

        DataClient ro = Engine.primaryClient();

        List rL = ro.queryByExample(Transaction.class);

        TransactionListView view = new TransactionListView(ro, rL);

        top.add(view);

        super.present();
    }

    public boolean deleteHook() {
        // hide & destroy
        super.deleteHook();
        // quit
        System.out.println("Notice: deleteHook() overriden to call Master.shutdown()");
        Master.shutdown();
        return false;
    }
}
