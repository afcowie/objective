/*
 * AccountTypeSelectorDialog.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.ui;

import org.gnome.gtk.Button;
import org.gnome.gtk.Gtk;
import org.gnome.gtk.RadioButton;

import generic.ui.AbstractWindow;
import generic.util.Debug;


import accounts.domain.Account;
import accounts.domain.AssetAccount;
import accounts.domain.BankAccount;
import accounts.domain.CashAccount;

/**
 * A popup question dialog to find out what kind of new account is to be
 * created.
 * <p>
 * <b>THIS CLASS INCOMPLETE</b>. Marked package until ready.
 * 
 * @author Andrew Cowie
 */
class AccountTypeSelectorDialog extends AbstractWindow
{
	/**
	 * Instantiate a Dialog like Window to allow you to pick an Account type
	 * (ie, a concrete class in the Account hierarchy).
	 */
	public AccountTypeSelectorDialog() {
		super("typeselector", "share/AccountTypeSelectorDialog.glade");

		// this one won't actually be displayed, but forms the foundation of the
		// radiobutton group; otherwise difficult to dynamically instantiate.
		// RadioButton rb = new RadioButton((RadioButton) null, "None selected",
		// false);

		// _assetsVBox = (VBox) _glade.getWidget("assets_vbox");

		// Label
		// _assetsVBox.add()

		Button ok = (Button) gladeParser.getWidget("ok_button");
		ok.addListener(new ButtonListener() {
			public void buttonEvent(ButtonEvent event) {
				if (event.getType() == ButtonEvent.Type.CLICK) {
					Debug.print("listeners", "Ok Button click");
					deleteHook();
				}
			}
		});

		Button cancel = (Button) gladeParser.getWidget("cancel_button");
		cancel.addListener(new ButtonListener() {
			public void buttonEvent(ButtonEvent event) {
				if (event.getType() == ButtonEvent.Type.CLICK) {
					Debug.print("listeners", "Cancel Button click");
					deleteHook();
				}
			}
		});

		window.showAll();
		window.present();
	}

	/**
	 * FIXME change to setAccount();
	 */
	public void preSelectType(Account type) {
		String activate = null;
		if (type instanceof AssetAccount) {
			if (type instanceof BankAccount) {
				activate = "bankaccount_rb";
			} else if (type instanceof CashAccount) {
				activate = "cashaccount_rb";
			}
			// else if (type instanceof AccountsReceivable) {

			// }
			// } else if (type instanceof LiabilityAccount) {
			// if (type instanceof LoanPayableAccount) {
			// activate = "loanpayable_rb";
			// }

		}
		if (activate != null) {
			RadioButton rb = (RadioButton) gladeParser.getWidget(activate);
			rb.clicked();
		}
	}

	public boolean deleteHook() {
		// FIXME
		Gtk.mainQuit();
		return false;
	}
}

class AccountTypeListener implements ButtonListener
{

	public void buttonEvent(ButtonEvent event) {
		event.notify();
	}

}
