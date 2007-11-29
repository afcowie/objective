/*
 * AccountLedgerDisplay.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.ui;

import generic.ui.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gnome.gtk.HBox;
import org.gnome.gtk.Label;
import org.gnome.gtk.Requisition;
import org.gnome.gtk.Widget;

import accounts.domain.Account;
import accounts.domain.Ledger;

/**
 * Display the title and name of an Account / Ledger pair. This widget wraps
 * and delegates to a Label, whose String is a complex series of Pango markup
 * used to achieve the correct colours and spacing.
 * <p>
 * AccountLedgerDisplay sets a minimum width that is very small. It is up to
 * the code using this widget to call
 * {@link org.gnu.gtk.Widget#setMinimumSize(int, int)} with a width for this
 * Display widget that will work out as appropriate to its Window.
 * 
 * @author Andrew Cowie
 */
/*
 * "minimum width" corresponds to gtk_widget_set_size_request().
 */
public class AccountLedgerDisplay extends HBox
{
    private AccountLedgerDisplay self;

    private Ledger ledger;

    private Label label;

    private int width;

    private int needed;

    private int lastAllocatedWidth = 0;

    /**
     * Shows an Account's title and Ledger's name. Delegating to a Label, this
     * widget applies appropriate Debit/Credit colour markup an
     * 
     */
    public AccountLedgerDisplay() {
        super(false, 0);
        self = this;

        label = new Label("");
        label.setUseMarkup(true);
        label.setAlignment(0.0f, 0.5f);

        this.packStart(label, false, false, 0);

        /*
         * 60 works out to be just enough room for "A… » C…".
         */
        this.setMinimumSize(60, -1);

        this.addListener(new ExposeListener() {
            public boolean exposeEvent(ExposeEvent event) {
                if (event.getType() == ExposeEvent.Type.EXPOSE) {
                    resize();
                }
                return false;
            }
        });
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
    public void setLedger(Ledger ledger) {
        this.ledger = ledger;
        Account account = ledger.getParentAccount();
        if (account == null) {
            throw new IllegalArgumentException("The parent Account of the passed Ledger must be set");
        }

        int al = account.getTitle().length();
        int ll = ledger.getName().length();

        /*
         * Have to use twice the greater of the two widths rather than al + ll
         * because otherwise when the total width gets divided into 2, half
         * for each side, it's not wide enough for the longer label. This
         * overshoots, but that is ok because it is used as an upper bound to
         * decrement down from when trying to find a working width that does
         * fit within the Allocation.
         */
        needed = (al > ll ? al : ll) * 2;
        width = needed;
        label.setMarkup(toMarkup());
    }

    /**
     * For the current Ledger, generate a Pango marked-up String which nicely
     * shows the Account's title and the Ledger's name in the appropriate
     * colours.
     */
    private String toMarkup() {
        if (ledger == null) {
            return "Select";
        }
        return markupTitleName(ledger, width);
    }

    /**
     * Evaluate the space allocated to the Box that is this Widget and the
     * size requested by the underlying Label in order to display the markup.
     * Use this information to adjust the width variable as necessary and to
     * continue iterating until a solution is found.
     */
    private void resize() {
        int boxAllocatedPixels;
        int labelAllocatedPixels;
        int labelRequestedPixels;

        boxAllocatedPixels = self.getAllocation().getWidth();
        labelAllocatedPixels = label.getAllocation().getWidth();

        // Debug.print("debug", "resize() boxAllocated " + boxAllocatedPixels
        // +
        // ", lastAllocated "
        // + lastAllocatedWidth + "; needed " + needed + ", width " + width);

        boolean wider = (boxAllocatedPixels > lastAllocatedWidth);
        lastAllocatedWidth = boxAllocatedPixels;

        if (wider) {
            // Debug.print("debug", "wider!");

            if (width > needed) {
                return;
            } else {
                width = needed;
            }
        } else {
            if (boxAllocatedPixels >= labelAllocatedPixels) {
                return;
            }
        }

        do {
            label.setMarkup(toMarkup());

            Requisition req = new Requisition(0, 0);
            Widget.gtk_widget_size_request(label.getHandle(), req.getHandle());
            labelRequestedPixels = req.getWidth();

            // Debug.print("debug", " --> requisited " + labelRequestedPixels
            // +
            // ", allocated "
            // + boxAllocatedPixels + "; width " + width);

            if (labelRequestedPixels <= boxAllocatedPixels) {
                break;
            }

            width--;
        } while (width > 4);
    }

    private static final Pattern regexAmp = Pattern.compile("&");

    private static final String CHARCOAL = "#575757";

    /**
     * Given a Ledger (and hence a parent Account), render a Pango marked-up
     * String which safely shows the Account's title and the Ledger's name in
     * the appropriate colours with the appropriate separator character. This
     * is a class method which can be used by other Widgets. and Ledger will
     * be have their title/name marked up and used as the text of the Label
     * underlying this Display widget.
     * 
     * @param ledger
     *            a Ledger whose parentAccount is set whose name (and
     *            parentAccount title) will be rendered.
     * @param width
     *            the maximum width, in characters. This isn't precise, per
     *            se, but is internally consistent as the number perterbs up
     *            or down.
     */
    public static String markupTitleName(Ledger ledger, int width) {
        if (ledger == null) {
            return "";
        }
        Account account = ledger.getParentAccount();

        StringBuffer buf = new StringBuffer();

        buf.append("<span color='");
        buf.append(account.getColor(false));
        buf.append("'>");
        String title = Text.chomp(account.getTitle(), width / 2);

        Matcher ma = regexAmp.matcher(title);
        buf.append(ma.replaceAll("&amp;"));
        buf.append("</span>");
        /*
         * We use » \u00bb. Other possibilities: ∞ \u221e, and ⑆ \u2446.
         */

        buf.append("<span color='" + CHARCOAL + "'>");
        buf.append(" \u00bb ");
        buf.append("</span>");

        buf.append("<span color='");
        buf.append(ledger.getColor(false));
        buf.append("'>");

        String name = Text.chomp(ledger.getName(), width / 2);
        Matcher ml = regexAmp.matcher(name);
        buf.append(ml.replaceAll("&amp;"));
        buf.append("</span>");

        return buf.toString();
    }

}
