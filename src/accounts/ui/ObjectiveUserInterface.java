/*
 * ObjectiveUserInterface.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.ui;

import generic.ui.EditorWindow;
import generic.ui.PrimaryWindow;
import generic.ui.UserInterface;
import accounts.domain.Currency;
import accounts.domain.GenericTransaction;
import accounts.domain.PayrollTransaction;
import accounts.domain.ReimbursableExpensesTransaction;
import accounts.domain.Transaction;
import country.au.ui.AustralianPayrollEditorWindow;

/**
 * Central callback point for all ObjectiveAccounts specific UI events. Most
 * importantly, we implement launch().
 * 
 * @author Andrew Cowie
 */
public class ObjectiveUserInterface extends UserInterface
{

	public ObjectiveUserInterface() {
		super();
	}

	/**
	 * Overrides (but calls)
	 * {@link generic.ui.UserInterface#deregisterWindow(generic.ui.PrimaryWindow)}.
	 * Removes w from the list of present windows if an Editor Window. The calls
	 * super implementation.
	 */
	protected void deregisterWindow(PrimaryWindow w) {
		super.deregisterWindow(w);
	}

	/**
	 * Launch a new window. The primary reason for this singleton class to exist
	 * is to provide a central point which disparate event handlers can poke in
	 * order to cause UI windows to be launched. This in inherited from (and
	 * overrides) generic.ui.UserInterface to provide functionality specific to
	 * ObjectiveAccounts.
	 * 
	 * @param id
	 *            database id of the target object
	 * @param target
	 *            the object you are editing. This is NOT passed to launched
	 *            editors, but is used to discriminate between
	 *            PayrollTransaction, GenericTransaction, etc.
	 */
	protected EditorWindow launchEditor(long id, Object target) {
		EditorWindow editor = null;

		if (target instanceof Transaction) {

			if (target instanceof PayrollTransaction) {
				editor = new AustralianPayrollEditorWindow(id);
			} else if (target instanceof ReimbursableExpensesTransaction) {
				editor = new ReimbursableExpensesEditorWindow(id);
			} else if (target instanceof GenericTransaction) {
				throw new UnsupportedOperationException("No editor for GenericTransaction yet");
			}

		} else if (target instanceof Currency) {
			throw new UnsupportedOperationException("This here just for kicks");
		}

		return editor;
	}
}
