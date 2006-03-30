/*
 * AccountTypeSelectorDialog.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.ui;

import generic.util.Debug;

import org.gnu.gtk.Button;
import org.gnu.gtk.Gtk;
import org.gnu.gtk.RadioButton;
import org.gnu.gtk.event.ButtonEvent;
import org.gnu.gtk.event.ButtonListener;

import accounts.domain.Account;
import accounts.domain.AssetAccount;
import accounts.domain.BankAccount;
import accounts.domain.CashAccount;

/**
 * A popup question dialog to find out what kind of new account is to be
 * created.
 * 
 * @author Andrew Cowie
 */
public class AccountTypeSelectorDialog extends AbstractWindow
{
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
			rb.click();
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
