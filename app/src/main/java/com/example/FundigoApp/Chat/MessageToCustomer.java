package com.example.FundigoApp.Chat;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("MessageToCustomer")
public class MessageToCustomer extends ParseObject {

    public MessageToCustomer()
    {}

    public String getSenderId() {
        return getString ("senderId");
    }

    public String getBody() {
        return getString ("body");
    }

    public void setSenderId(String userId) {
        put ("senderId", userId);
    }

    public void setBody(String body) {
        put ("body", body);
    }

    public void setCustomer2(String customer2) {
        put ("customer2", customer2);
    }

    public void setCustomer1(String customer1) {
        put ("customer1", customer1);
    }

    public String getCustomer1() {
        return getString ("customer1");
    }
    public String getCustomer2() {
        return getString ("customer2");
    }

    public String getEventObjectId() {
        return getString ("eventObjectId");
    }

    public void setEventObjectId(String eventObjectId) {
        put ("eventObjectId", eventObjectId);
    }
}