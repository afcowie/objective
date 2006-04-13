/*
 * TwoColumnTable.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package generic.ui;

import org.gnu.gtk.Table;
import org.gnu.gtk.Widget;

/**
 * A Table container specialized to simply do two columns of aligned rows. GTK's
 * immensely powerful Table layout widget has terrific fine grain placement
 * control, but this unsurprisingly makes it a bit ugly to use for simple cases.
 * This class is a Table which simply has a left side and a right side and
 * aligns rows to be equally spaced. [Two VBoxes in an HBox wouldn't achieve the
 * same result because the rows on each side would not be parallel; a series of
 * HBoxes in a VBox would lack a common column boundary].
 * 
 * @author Andrew Cowie
 */
public class TwoColumnTable extends Table
{
	private int		rowCount;
	private int		rowCapacity;
	private Align	lastSide	= null;
	private boolean	rowComplete;

	/**
	 * 
	 * @param initialNumberOfRows
	 *            A (preferably good) guess of how many rows the table will
	 *            have. If you're too low, rows will be added though
	 *            incrementally (and therefore inefficiently); if you're too
	 *            high, then you waste memory.
	 */
	public TwoColumnTable(int initialNumberOfRows) {
		// rows, two columns, don't space homogeneously (ie pack tightly)
		super(initialNumberOfRows, 2, false);

		/*
		 * Pad the left column with a few pixels so the labels that are
		 * typically there don't crowd the mutator widgets typically in the
		 * right column.
		 */
		super.setColumnSpacing(0, 3);

		if (initialNumberOfRows < 1) {
			throw new IllegalArgumentException("Initial size estimate must be positive");
		}

		this.rowCount = 1; // first attach goes into this first row.
		this.rowCapacity = initialNumberOfRows;
		this.rowComplete = false;
	}

	/**
	 * This takes the trouble to figure out whether or not you're attaching in
	 * the current row (ie, the other Side) or starting a new row (ie you've
	 * mentioned the same Side twice in sequence).
	 * 
	 * @param widget
	 *            the Widget to add to the Table.
	 * @param leftOrRight
	 *            which side of the Table widget should go on. Use the public
	 *            constants {@link Align.LEFT} and {@link Align.RIGHT}, or
	 *            {@link Align.CENTER} to indicate you want it to span both
	 *            columns.
	 */
	public void attach(Widget widget, Align leftOrRight) {
		if ((widget == null) || (leftOrRight == null)) {
			throw new IllegalArgumentException("Can't use null as an argument");
		}

		if ((leftOrRight == lastSide) || (leftOrRight == Align.CENTER)) {
			rowCount++;
			rowComplete = false;
		} else if (rowComplete) {
			rowCount++;
			rowComplete = false;
		}

		if (rowCount > rowCapacity) {
			/*
			 * Admittedly this is inefficient, expanding by unit size each time
			 * this code is hit, but this can be avoided by specifying an
			 * appropriate capacity off the marks.
			 */
			super.resize(rowCount, 2);
		}

		/*
		 * Now we attach the widget; hiding the complexity of working out the
		 * boundaries is the whole point of this subclass.
		 */

		if (leftOrRight == Align.LEFT) {
			super.attach(widget, 0, 1, rowCount - 1, rowCount);
		} else if (leftOrRight == Align.RIGHT) {
			super.attach(widget, 1, 2, rowCount - 1, rowCount);
		} else if (leftOrRight == Align.CENTER) {
			super.attach(widget, 0, 2, rowCount - 1, rowCount);
		} else {
			throw new IllegalArgumentException("Specified alignment not valid here");
		}

		if ((lastSide != null) && (leftOrRight != lastSide)) {
			lastSide = null;
			rowComplete = true;
		} else {
			lastSide = leftOrRight;
		}
	}
}
