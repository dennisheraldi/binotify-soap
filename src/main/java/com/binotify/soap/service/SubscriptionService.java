package com.binotify.soap.service;

import java.net.http.HttpRequest.BodyPublishers;
import java.sql.SQLException;
import java.util.List;

import com.binotify.soap.database.models.Subscription;
import com.binotify.soap.enums.ServiceType;
import com.binotify.soap.utils.*;

import jakarta.annotation.Resource;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import jakarta.xml.ws.WebServiceContext;

@WebService
public class SubscriptionService {
    @Resource
    WebServiceContext context;

    public int RequestSub(@WebParam(name="CreatorId") int creator_id, @WebParam(name="SubscriberId") int subscriber_id) throws SQLException {
        Logger.log(context,
            "[PHP] Requesting PENDING subscription from subscriber id " + subscriber_id +
            " for creator id " + creator_id , 
            "/binotify/Subscription{RequestSub}"
        );
        if (Authentication.IsAuthenticated(context, ServiceType.PHP)) {
            Subscription s = Subscription.get(creator_id, subscriber_id);
            if (s == null) {
                s = new Subscription();
                s.creator_id = creator_id;
                s.subscriber_id = subscriber_id;
                s.save();
                // Notify admin
                Request.GET(ServiceType.REST, "/admins", (String adminJson) -> {
                    Mailer.notifyAdminNewSub(
                        "There is a new subscription request from subscriber id " + subscriber_id +
                        " for creator id " + creator_id,
                        adminJson
                    );
                });
                return 1; // Success adding new 
            }
            return 0; // Subscriber already exist
        }
        throw new SQLException("Not authenticated");
    }

    public int ApprovalSub(@WebParam(name="CreatorId") int creator_id, @WebParam(name="SubscriberId") int subscriber_id, @WebParam(name="IsApproved") boolean isApproved) throws SQLException {
        Logger.log(context, 
            "[REST] Changing PENDING approval subscription to " + (isApproved ? "ACCEPTED" : "REJECTED") +
            "from subscriber id " + subscriber_id + " for creator id " + creator_id,
            "/binotify/Subscription{ApprovalSub}"
        );
        if (Authentication.IsAuthenticated(context, ServiceType.REST)) {
            Subscription s = Subscription.get(creator_id, subscriber_id);
            if (s != null) {
                s.status = isApproved ? Subscription.Status.ACCEPTED : Subscription.Status.REJECTED;
                s.save();
                // Callback to the PHP service
                Request.POST(BodyPublishers.ofString("result=" + (isApproved ? "ACCEPTED" : "REJECTED")),
                    ServiceType.PHP, "/webhook/subscribe/" + creator_id + "/" + subscriber_id + "/",
                    System.out::println
                );
                return 1; // Success changing status
            }
            return 0; // Subscription not found
        }
        throw new SQLException("Not authenticated");
    }

    public List<Subscription> ListSubs() throws SQLException {
        Logger.log(context, "[REST] Get all subscription list for Admin", "/binotify/Subscription{ListSubs}");
        if (Authentication.IsAuthenticated(context, ServiceType.REST)) {
            List<Subscription> s = Subscription.find(1, -1, -1);
            return s;
        }
        throw new SQLException("Not authenticated");
    }

    public List<Subscription> GetSubsList(@WebParam(name="SubscriberId") int subscriber_id) throws SQLException {
        Logger.log(context, "[REST] Get subscribed creators for subscriber id " + subscriber_id, "/binotify/Subscription{GetSubsList}");
        if (Authentication.IsAuthenticated(context, ServiceType.REST)) {
            List<Subscription> s = Subscription.find(-1, subscriber_id, -1);
            return s;
        }
        throw new SQLException("Not authenticated");
    }

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