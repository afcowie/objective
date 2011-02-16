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
 * contacted through http://research.operationaldynamics.com/projects/objective/.
 */
package accounts.ui;

import generic.ui.AbstractWindow;
import generic.util.Debug;

import org.gnome.gtk.Button;
import org.gnome.gtk.Gtk;
import org.gnome.gtk.RadioButton;

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

        // this one won't actually be displayed, but forms the foundation of
        // the
        // radiobutton group; otherwise difficult to dynamically instantiate.
        // RadioButton rb = new RadioButton((RadioButton) null, "None
        // selected",
        // false);

        // _assetsVBox = (VBox) _glade.getWidget("assets_vbox");

        // Label
        // _assetsVBox.add()

        Button ok = (Button) gladeParser.getWidget("ok_button");
        ok.connect(new Button.Clicked() {
            public void onClicked(Button source) {
                Debug.print("listeners", "Ok Button click");
                deleteHook();
            }
        });

        Button cancel = (Button) gladeParser.getWidget("cancel_button");
        cancel.connect(new Button.Clicked() {
            public void onClicked(Button source) {
                Debug.print("listeners", "Cancel Button click");
                deleteHook();
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
            // rb.clicked();
            rb.activate();
        }
    }

    public boolean deleteHook() {
        // FIXME
        Gtk.mainQuit();
        return false;
    }
}
