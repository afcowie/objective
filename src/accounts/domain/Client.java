/*
 * Client.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.domain;

/**
 * A client who buys products or services from us
 * 
 * @author Andrew Cowie
 */
public class Client extends Entity
{
	public Client() {
		// for searching
	}

	public Client(String name) {
		super(name);
	}

}
