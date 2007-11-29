/*
 * LedgerTextOutput.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.ui;

import generic.ui.TextOutput;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import accounts.domain.Ledger;

/**
 * Output the contents of a Ledger in a human readable text form. The usual
 * use case is to be called by an AccountTextOutput, but can also be used for
 * debugging.
 * 
 * @author Andrew Cowie
 */
public class LedgerTextOutput extends TextOutput
{
    protected static final int descWidth;

    protected static final int idWidth = 6;

    protected static final int typeWidth;

    static {
        int piece = COLUMNS / 7;
        typeWidth = piece * 3;
        // desc gets the rest, less one to keep off the edge.
        descWidth = COLUMNS - idWidth - typeWidth - 1;
    }

    private Set ledgers;

    /**
     * @param ledgers
     *            the Set of accounts to be output.
     */
    public LedgerTextOutput(Set ledgers) {
        if (ledgers == null) {
            throw new IllegalArgumentException("Can't instantiate a LedgerTextOutput with a null Set");
        }
        Iterator iter = ledgers.iterator();
        while (iter.hasNext()) {
            Object o = iter.next();
            if (!(o instanceof Ledger)) {
                throw new IllegalArgumentException("The Set passed must only contain Ledger objects");
            }
        }
        this.ledgers = ledgers;
    }

    /**
     * @param ledger
     *            a single Ledger to run the outputter over. Use for spot
     *            debugging only.
     */
    public LedgerTextOutput(Ledger ledger) {
        ledgers = Collections.singleton(ledger);
    }

    /**
     * @param out
     *            the PrintWriter you want to send the output to.
     */
    public void toOutput(PrintWriter out) {
        if (ledgers.size() == 0) {
            return;
        }

        Iterator lI = ledgers.iterator();
        while (lI.hasNext()) {
            Ledger l = (Ledger) lI.next();

            out.print(pad("Ledger: \"" + chomp(l.getName(), descWidth + idWidth - 11) + "\" ", descWidth
                    + idWidth, LEFT));
            out.print(pad(l.getClassString(), typeWidth, RIGHT));
            out.println();

            EntryTextOutput entryOutputter = new EntryTextOutput(l);
            entryOutputter.toOutput(out);
        }
    }
}
