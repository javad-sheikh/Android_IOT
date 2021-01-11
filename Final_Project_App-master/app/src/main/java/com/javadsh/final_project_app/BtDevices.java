package com.javadsh.final_project_app;

/**
 * Created by javad Sh on 2/2/2018.
 */

public class BtDevices
{
    String mac_id;
    String name ;
    Integer status ;

    public BtDevices (String mac_id,String name,Integer status )
    {
        this.mac_id = mac_id ;
        this.name = name ;
        this.status = status ;
    }


    public void setMac_id (String mac_id)
    {
        this.mac_id = mac_id ;
    }
    public void setName (String name)
    {
        this.name = name ;
    }
    public void setStatus (Integer status)
    {
        this.status = status ;
    }

    public String getMac_id()
    {
        return this.mac_id ;
    }
    public String getName()
    {
        return this.name;
    }
    public Integer getStatus()
    {
        return this.status;
    }
}
