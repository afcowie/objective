/*
 * ObjectiveUserInterface.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.ui;

import generic.persistence.DataClient;
import generic.ui.PrimaryWindow;
import generic.ui.UserInterface;

import java.util.HashMap;
import java.util.Map;

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
	private Map	idsToEditors;
	private Map	editorsToIds;

	public ObjectiveUserInterface() {
		idsToEditors = new HashMap();
		editorsToIds = new HashMap();
	}

	/**
	 * Overrides (but calls)
	 * {@link generic.ui.UserInterface#deregisterWindow(generic.ui.PrimaryWindow)}.
	 * Removes w from the list of present windows if an Editor Window. The calls
	 * super implementation.
	 */
	protected void deregisterWindow(PrimaryWindow w) {
		if (w instanceof EditorWindow) {
			if (editorsToIds.containsKey(w)) {
				Long ID = (Long) editorsToIds.remove(w);
				idsToEditors.remove(ID);
			}
		}
		super.deregisterWindow(w);
	}

	/**
	 * Launch a new window. The primary reason for this singleton class to exist
	 * is to provide a central point which disparate event handlers can poke in
	 * order to cause UI windows to be launched. This in inherited from (and
	 * overrides) generic.ui.UserInterface to provide functionality specific to
	 * ObjectiveAccounts.
	 * 
	 * @param store
	 *            the DataClient which target resides in
	 * @param target
	 *            the object wish determines what UI element is to be launched.
	 */
	public void launch(DataClient store, Object target) {
		long id = store.getID(target);

		if (target instanceof Transaction) {
			launch(id, (Transaction) target);
		} else if (target instanceof Currency) {
			throw new UnsupportedOperationException("This here just for kicks");
		}
	}

	/**
	 * Transactions are assumed to have EditorWindows to view them; this version
	 * of launch() selects the approriate window type and instantiates it for
	 * editing. Along the way it keeps a reference to the editor by both
	 * EditorWindow and id, so that a subsequent call to launch an editor for
	 * that id will present the already open editor.
	 * 
	 * @param id
	 *            database id of the target object
	 * @param target
	 *            the Transaction object you are editing. This is NOT passed to
	 *            launched editors, but is used to discriminate between
	 *            PayrollTransaction, GenericTransaction, etc.
	 */
	private void launch(long id, Transaction target) {
		EditorWindow editor = null;

		Long ID = new Long(id);

		if (idsToEditors.containsKey(ID)) {
			editor = (EditorWindow) idsToEditors.get(ID);
			editor.present();
			return;
		}

		if (target instanceof PayrollTransaction) {
			editor = new AustralianPayrollEditorWindow(id);
		} else if (target instanceof ReimbursableExpensesTransaction) {
			editor = new ReimbursableExpensesEditorWindow();
			throw new UnsupportedOperationException(
				"ReimbursableExpensesEditorWindow not yet configured for editing");
		} else if (target instanceof GenericTransaction) {
			throw new UnsupportedOperationException("No editor for GenericTransaction yet");
		}

		idsToEditors.put(ID, editor);
		editorsToIds.put(editor, ID);
	}
}
