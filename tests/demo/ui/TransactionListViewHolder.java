/*
 * TransactionListViewHolder.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package demo.ui;

import java.util.List;

import org.gnu.gtk.Gtk;

import accounts.domain.Transaction;
import accounts.persistence.DataClient;
import accounts.persistence.Engine;
import accounts.ui.TransactionListView;
import generic.ui.AbstractWindow;

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
		System.out.println("Notice: deleteHook() overriden to call Gtk.mainQuit()");
		Gtk.mainQuit();
		return false;
	}
}
