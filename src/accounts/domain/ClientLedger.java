/*
 * ClientLedger.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.domain;

import java.util.Set;

public class ClientLedger extends DebitPositiveLedger implements ItemsLedger
{
	private Client	client;

	public ClientLedger() {
		super();
	}

	public ClientLedger(Client client) {
		super();
		String name = client.getName();
		if ((name == null) || name.equals("")) {
			throw new IllegalArgumentException("Client object must at least have its name filled in");
		}
		super.setName(name);
		this.client = client;
	}

	public Set getItems() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setItems(Set items) {
		// TODO Auto-generated method stub

	}

	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}

	public String getClassString() {
		return "Client Ledger";
	}
}
