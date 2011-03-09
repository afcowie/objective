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
package objective.ui;

import objective.domain.Account;
import objective.domain.Ledger;

import org.gnome.glib.Glib;
import org.gnome.gtk.Alignment;
import org.gnome.gtk.HBox;
import org.gnome.gtk.Label;
import org.gnome.pango.EllipsizeMode;

/**
 * Display the title and name of an Account / Ledger pair. This widget wraps
 * and delegates to a Label, whose String is a complex series of Pango markup
 * used to achieve the correct colours and spacing.
 * 
 * <p>
 * AccountLedgerDisplay sets a minimum width that is very small. It is up to
 * the code using this widget to call
 * {@link org.gnome.gtk.Widget#setSizeRequest(int, int)} with a width for this
 * Display widget that will work out as appropriate to its Window.
 * 
 * @author Andrew Cowie
 */
public class AccountLedgerDisplay extends HBox
{
    private final HBox box;

    private Label label;

    /**
     * Shows an Account's title and Ledger's name. Delegating to a Label, this
     * widget applies appropriate Debit/Credit colour markup.
     */
    public AccountLedgerDisplay() {
        super(false, 0);

        box = this;

        label = new Label("Select Account");
        label.setUseMarkup(true);
        label.setAlignment(Alignment.LEFT, Alignment.CENTER);
        label.setEllipsize(EllipsizeMode.END);

        box.packStart(label, true, true, 0);
        box.setSizeRequest(60, -1);
    }

    /**
     * Set the Account and Ledger shown. The Account (implicitly from the
     * Ledger by following its parentAccount reference) and Ledger will be
     * have their title/name marked up and used as the text of the Label
     * underlying this Display widget.
     * 
     * @param ledger
     *            a Ledger whose parentAccount is set.
     */
    public void setLedger(final Ledger ledger) {
        final Account account;
        final StringBuilder buf;
        String str;

        if (ledger == null) {
            throw new AssertionError();
        }

        account = ledger.getParentAccount();
        if (account == null) {
            throw new AssertionError();
        }

        buf = new StringBuilder();

        buf.append("<span color='");
        buf.append(account.getColor(false));
        buf.append("'>");

        str = account.getTitle();
        buf.append(Glib.markupEscapeText(str));

        buf.append("</span>");

        buf.append("<span color='");
        buf.append(CHARCOAL);
        buf.append("'>");
        buf.append(" \u00bb ");
        buf.append("</span>");

        buf.append("<span color='");
        buf.append(ledger.getColor(false));
        buf.append("'>");

        str = ledger.getName();
        buf.append(Glib.markupEscapeText(str));

        buf.append("</span>");

        label.setLabel(buf.toString());
    }

    private static final String CHARCOAL = "#575757";
}
