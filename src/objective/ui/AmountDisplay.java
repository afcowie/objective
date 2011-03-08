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

import objective.domain.Amount;

import org.gnome.gtk.HBox;
import org.gnome.gtk.Label;

/**
 * Display an Amount. This widget wraps and delegates a Label, and:
 * 
 * <ul>
 * <li>Right aligns
 * <li>Pads the Label so that it vertically aligns with data in a column of
 * AmountEntry boxes.
 * </ul>
 * 
 * The implementation extends HBox so that the widget grows without swelling
 * the underlying Label as space is given to it.
 * 
 * @author Andrew Cowie
 * @see objective.ui.AmountEntry the complementary mutator widget.
 */
public class AmountDisplay extends HBox
{
    private final Label label;

    /**
     * Instantiate a new Label widget to display an Amount.
     */
    public AmountDisplay() {
        super(false, 0);

        label = new Label("");
        label.setAlignment(1.0f, 0.5f);
        label.setWidthChars(10);
        label.setMaxWidthChars(10);

        /*
         * Numbers on screen aren't much use if you can't copy them!
         */
        label.setSelectable(true);
        label.setCanFocus(false);

        /*
         * The following [2 in setPadding() and 5 in packStart()] pads the
         * Label similarly to the AmountEntry, so that things space equally in
         * a column of Amount{Entry,Label} widgets.
         */
        label.setPadding(0, 2);
        this.packStart(label, false, false, 5);
    }

    /**
     * Set or update the Amount displayed by this Widget.
     */
    public void setAmount(long amount) {
        final String str;

        str = Amount.numberToString(amount);

        label.setLabel(str);
    }
}
