package com.codewithjosh.Swift2k22.models;

import java.util.Comparator;
import java.util.Date;

public class BusModel
{

    public static Comparator<BusModel> comparator = (bM1, bM2) -> bM1.getBus_timestamp().compareTo(bM2.getBus_timestamp());
    private int bus_fare;
    private String bus_id;
    private String bus_number;
    private int bus_slots;
    private Date bus_timestamp;
    private String route_id;

    public BusModel()
    {

    }

    public BusModel(int bus_fare, String bus_id, String bus_number, int bus_slots, Date bus_timestamp, String route_id)
    {

        this.bus_fare = bus_fare;
        this.bus_id = bus_id;
        this.bus_number = bus_number;
        this.bus_slots = bus_slots;
        this.bus_timestamp = bus_timestamp;
        this.route_id = route_id;

    }

    public int getBus_fare()
    {

        return bus_fare;

    }

    public String getBus_id()
    {

        return bus_id;

    }

    public String getBus_number()
    {

        return bus_number;

    }

    public int getBus_slots()
    {

        return bus_slots;

    }

    public Date getBus_timestamp()
    {

        return bus_timestamp;

    }

    public String getRoute_id()
    {

        return route_id;

    }

}
