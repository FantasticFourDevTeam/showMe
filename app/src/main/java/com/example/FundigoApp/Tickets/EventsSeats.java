package com.example.FundigoApp.Tickets;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

import java.util.Date;

@ParseClassName("EventsSeats")
public class EventsSeats extends ParseObject {
    public int getPrice() {
        return getInt ("price");
    }

    public void setPrice(int price) {
        put ("price", price);
    }

    public int getEventObjectId() {
        return getInt ("eventObjectId");
    }

    public void setEventObjectId(String eventObjectId) {
        put ("eventObjectId", eventObjectId);
    }

    public String getSeatNumber() {
        return getString ("seatNumber");
    }

    public void setSeatNumber(String seatNumber) {
        put ("seatNumber", seatNumber);
    }

    public ParseFile getQR_CodeFile() {
        return getParseFile ("QR_Code");
    }

    public void setQR_CodeFile(ParseFile QR_CodeFile) {
        put ("QR_Code", QR_CodeFile);
    }

    public Date getPurchaseDate() {
        return getDate ("purchase_date");
    }

    public void setPurchaseDate(Date purchaseDate) {
        put ("purchase_date", purchaseDate);
    }

    public int getIntPrice() {
        return getInt ("price");
    }

    public String getCustomerPhone() {
        return getString ("CustomerPhone");
    }

    public void setCustomerPhone(String customerPhone) {
        put ("CustomerPhone", customerPhone);
    }

    public ParseObject getSoldTicketsPointer() {
        return getParseObject ("soldTicketsPointer");
    }

    public void setSoldTicketsPointer(ParseObject soldTicketsPointer) {
        put ("soldTicketsPointer", soldTicketsPointer);
    }

    public boolean getIsSold() {
        return getBoolean ("sold");
    }

    public void setIsSold(boolean isSold) {
        put ("sold", isSold);
    }
}

