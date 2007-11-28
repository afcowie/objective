/*
 * TextEntry.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package generic.ui;

import org.gnome.gtk.Entry;

/**
 * A simple wrapper aroung GTK's Entry. Hooks up its listeners and fires off our
 * local ChangeListener on an actual user derived change to the Entry's content,
 * as opposed to a programatic one.
 * 
 * @author Andrew Cowie
 */
/*
 * While the "only do something if focused" pattern is a common one, one of the
 * motivations for this was to not have to avoid by fully qualified class names
 * in UI classes dealing with accounts.domain.Entry objects.
 */
public class TextEntry extends Entry
{
	private ChangeListener	changeListener	= null;
	private Entry			self			= null;

	/**
	 * Create a new widget, hooking up listener to fire
	 * {@link ChangeListener#userChangedData()} when the contents of the
	 * GtkEntry are changed.
	 * 
	 */
	public TextEntry() {
		super();
		self = this;

		addListener(new EntryListener() {

			public void entryEvent(EntryEvent event) {
				if (event.getType() == EntryEvent.Type.CHANGED) {
					if (!self.hasFocus()) {
						return;
					}

					if (changeListener != null) {
						changeListener.userChangedData();
					}
				}
			}

		});
	}

	public void addListener(ChangeListener listener) {
		if (changeListener != null) {
			throw new IllegalStateException("Can only add one ChangeListener");
		}
		changeListener = listener;
	}
}