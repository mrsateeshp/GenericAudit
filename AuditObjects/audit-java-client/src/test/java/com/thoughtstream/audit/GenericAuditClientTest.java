package com.thoughtstream.audit;

import com.thoughtstream.audit.bean.AuditMessage;
import com.thoughtstream.audit.demo.User;
import com.thoughtstream.audit.exception.AuditMessageSaveFailed;
import com.thoughtstream.audit.utils.GenericAuditUtils;

/**
 * @author Sateesh
 * @since 04/01/2015
 */
public class GenericAuditClientTest {

    public static void main(String[] args) {
        User tony = new User(123, "tony");
        User.Address address = new User.Address("26 May St", "FL11 3TY");
        tony.setAddress(address);
        tony.addFriend(new User(345,"rohitm"));

        GenericAuditClient client = new GenericAuditClient("localhost:8080");

        try {
            client.postAuditMessage(new AuditMessage(GenericAuditUtils.getDataSnapshot(tony)));
        } catch (AuditMessageSaveFailed auditMessageSaveFailed) {
            auditMessageSaveFailed.printStackTrace();
        }

        System.out.println("posted the message successfully!");
    }
}
