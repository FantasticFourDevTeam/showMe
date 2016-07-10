package com.example.FundigoApp.Tickets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.FundigoApp.R;

import java.util.List;

public class TicketAdapter extends ArrayAdapter<EventsSeats> {
    Context context;
    public List<EventsSeats> tickets;


    public TicketAdapter(Context context, List<EventsSeats> tickets) {
        super (context, 0, tickets);
        this.context = context;
        this.tickets = tickets;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        LayoutInflater inflater = LayoutInflater.from (context);
        if (convertView == null) {
            convertView = inflater.inflate (R.layout.ticket_item, null);

            holder = new ViewHolder ();
            holder.tv_price = (TextView) convertView.findViewById (R.id.ticketItem_tv_price);
            holder.tv_ticket = (TextView) convertView.findViewById (R.id.ticketItem_tv_ticket);
            holder.tv_sold = (TextView) convertView.findViewById (R.id.ticketItem_tv_sold);

            convertView.setTag (holder);
        } else {
            holder = (ViewHolder) convertView.getTag ();
        }

        EventsSeats ticket = tickets.get (position);
        holder.tv_price.setText (ticket.getPrice () + "₪");
        holder.tv_ticket.setText (ticket.getSeatNumber ());
        if (!ticket.getIsSold ()) {
            holder.tv_sold.setText ("available");
        } else{
            holder.tv_sold.setText ("sold");
        }
        return convertView;
    }

    class ViewHolder {
        TextView tv_price;
        TextView tv_ticket;
        TextView tv_sold;
    }
}
