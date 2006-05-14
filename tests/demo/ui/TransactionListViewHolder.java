/*
 * TransactionListViewHolder.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package demo.ui;

import generic.client.Master;
import generic.persistence.DataClient;
import generic.persistence.Engine;
import generic.ui.AbstractWindow;

import java.util.List;

import accounts.domain.Transaction;
import accounts.ui.TransactionListView;

public class TransactionListViewHolder extends AbstractWindow
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
