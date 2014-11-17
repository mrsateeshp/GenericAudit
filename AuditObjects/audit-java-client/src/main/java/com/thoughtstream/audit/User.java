package com.thoughtstream.audit;

import com.thoughtstream.audit.anotation.AuditableEntity;
import com.thoughtstream.audit.anotation.AuditableField;
import com.thoughtstream.audit.anotation.AuditableId;
import com.thoughtstream.audit.anotation.AuditableValueObject;

/**
 * @author Sateesh
 * @since 16/11/2014
 */
@AuditableEntity("User")
public class User {
    @AuditableValueObject("Address")
    public static class Address{
        @AuditableField
        private String fLine;
        @AuditableField
        private String postcode;

        public Address(String fLine, String postcode) {
            this.fLine = fLine;
            this.postcode = postcode;
        }

        public String getfLine() {
            return fLine;
        }

        public String getPostcode() {
            return postcode;
        }
    }

    @AuditableId
    private int uid;

    @AuditableField
    private String name;

    @AuditableField
    private Address address = new Address("11 June St", "MA2 3HG");

    public User(int uid, String name) {
        this.uid = uid;
        this.name = name;
    }

    public int getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}


