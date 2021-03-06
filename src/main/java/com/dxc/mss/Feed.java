package com.dxc.mss;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by rakshit on 10/03/2018.
 */
public class Feed {
    private static final DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    private int id;
    private String company;
    private String feedName;
    private String url;

    @JsonFormat (shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastUpdated;

    public int getId() { return this.id; }

    public String getCompany() {
        return this.company;
    }

    public String getFeedName() {
        return this.feedName;
    }

    public String getUrl() {
        return this.url;
    }

    public Date getLastUpdated() {
        return this.lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return getCompany() + " | " + getFeedName() + " | " + getUrl() + " | " + sdf.format(getLastUpdated());
    }
}