/*
 * DummyInts.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2005 Operational Dynamics
 */
package accounts.persistence;

/**
 * Something silly but storable to use when unit testing persistence.
 * 
 * @author Andrew Cowie
 */
public class DummyInts
{
	int	num;

	DummyInts(int i) {
		this.num = i;
	}

	int getNum() {
		return num;
	}

	void setNum(int num) {
		this.num = num;
	}

	public String toString() {
		return Integer.toString(num);
	}
}