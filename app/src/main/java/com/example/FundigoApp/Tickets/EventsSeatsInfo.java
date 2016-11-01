package com.example.FundigoApp.Tickets;

import android.graphics.Bitmap;

import com.example.FundigoApp.Events.EventInfo;
import com.parse.ParseObject;

import java.util.Date;

public class EventsSeatsInfo {
    private String ticketName;
    private Bitmap QR;
    private Date purchaseDate;
    private int price;
    private EventInfo eventInfo;
    private ParseObject soldTickets;
    private String qrFilePath;

    public EventsSeatsInfo(String ticketName,
                          // Bitmap QR, //09.10 assaf
                           Date purchaseDate,
                           int price,
                           EventInfo eventInfo,
                           ParseObject soldTickets,String qrFilePath ) {//09.10 assaf - qrfilepath added
        this.ticketName = ticketName;
      //  this.QR = QR;
        this.purchaseDate = purchaseDate;
        this.price = price;
        this.eventInfo = eventInfo;
        this.soldTickets = soldTickets;
        this.qrFilePath = qrFilePath;
    }

    public String getTicketName() {
        return ticketName;
    }

    public Bitmap getQR() {
        return QR;
    }

    public Date getPurchaseDate() {
        return purchaseDate;
    }

    public int getPrice() {
        return price;
    }

    public EventInfo getEventInfo() {
        return eventInfo;
    }

    public void setEventInfo(EventInfo eventInfo) {
        this.eventInfo = eventInfo;
    }

    public ParseObject getSoldTickets() {
        return soldTickets;
    }

    public void setSoldTickets(ParseObject soldTickets) {
        this.soldTickets = soldTickets;
    }

    public String getQRPath  () //09.10 assaf
    {
        return qrFilePath;
    }

    public void setQRPath (String qrPath) //09.10 assaf
    {
       this.qrFilePath = qrPath;
    }


}
