/*
 * SupplierLedger.java
 * 
 * See LICENCE file for usage and redistribution terms
 * Copyright (c) 2006 Operational Dynamics
 */
package accounts.domain;

import java.util.Set;

public class SupplierLedger extends CreditPositiveLedger implements ItemsLedger
{
    private Supplier supplier;

    public SupplierLedger() {
        super();
    }

    public SupplierLedger(Supplier supplier) {
        super();
        String name = supplier.getName();
        if ((name == null) || name.equals("")) {
            throw new IllegalArgumentException("Supplier object must at least have its name filled in");
        }
        super.setName(name);
        this.supplier = supplier;
    }

    public Set getItems() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setItems(Set items) {
    // TODO Auto-generated method stub

    }

    // TODO consider: should this move to ItemsLedger, generically as
    // getEntity?

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public String getClassString() {
        return "Supplier Ledger";
    }
}
