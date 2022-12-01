package com.binotify.soap.service;

import java.net.http.HttpRequest.BodyPublishers;

import com.binotify.soap.enums.ServiceType;
import com.binotify.soap.utils.*;

import jakarta.jws.WebParam;
import jakarta.jws.WebService;

@WebService
public class SubscriptionService {
    public void TestMail(@WebParam(name="Destination") String dest, @WebParam(name="Subject") String subject, @WebParam(name="Message") String message) {
        Mailer.notifyAdminNewSub("New subscription from {sub_id} for {creat_id} just dropped!", "[{\"email\":\"" + dest + "\"}]");
    }

    public void TestHook(@WebParam(name="CreatorId") int creator_id, @WebParam(name="SubscriberId") int subscriber_id, @WebParam(name="IsApproved") boolean isApproved) {
        // Callback to the PHP service
        Request.POST(BodyPublishers.ofString("result=" + (isApproved ? "ACCEPTED" : "REJECTED")),
            ServiceType.PHP, "/webhook/subscribe/" + creator_id + "/" + subscriber_id + "/",
            System.out::println
        );
    }
}