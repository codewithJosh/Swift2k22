package com.codewithjosh.Swift2k22.models;

public class TicketModel
{

    private String bus_id;
    private String ticket_id;
    private String user_id;

    public TicketModel()
    {

    }

    public TicketModel(String bus_id, String ticket_id, String user_id)
    {
        this.bus_id = bus_id;
        this.ticket_id = ticket_id;
        this.user_id = user_id;
    }

    public String getBus_id()
    {

        return bus_id;

    }

    public String getTicket_id()
    {

        return ticket_id;

    }

    public String getUser_id()
    {

        return user_id;

    }

}
