package com.codewithjosh.Swift2k22.models;

import java.util.Comparator;
import java.util.Date;

public class TicketModel {

    public static Comparator<TicketModel> comparator = (bM1, bM2) -> bM1.getBus_timestamp().compareTo(bM2.getBus_timestamp());
    private String bus_id;
    private Date bus_timestamp;
    private String route_name;
    private String ticket_id;
    private String user_id;

    public TicketModel() {

    }

    public TicketModel(final String bus_id, final Date bus_timestamp, final String route_name, final String ticket_id, final String user_id)
    {

        this.bus_id = bus_id;
        this.bus_timestamp = bus_timestamp;
        this.route_name = route_name;
        this.ticket_id = ticket_id;
        this.user_id = user_id;

    }

    public String getBus_id() {

        return bus_id;

    }

    public Date getBus_timestamp() {

        return bus_timestamp;

    }

    public String getRoute_name() {

        return route_name;

    }

    public String getTicket_id() {

        return ticket_id;

    }

    public String getUser_id() {

        return user_id;

    }

}
