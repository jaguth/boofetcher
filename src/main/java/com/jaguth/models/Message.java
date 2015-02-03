package com.jaguth.models;

import java.util.Date;

public class Message
{
    public long id;
    public String UserName;
    public String GeoLocation;
    public String CountryCode;
    public String TimeZone;
    public String Language;
    public Date MessageDate;
    public int Likes;
    public int Forwards;
    public String Message;
    public String DataSource;

    public Message(long id,
                   String UserName,
                   String GeoLocation,
                   String CountryCode,
                   String TimeZone,
                   String Language,
                   Date MessageDate,
                   int Likes,
                   int Forwards,
                   String Message,
                   String DataSource)
    {
        this.id = id;
        this.UserName = UserName;
        this.GeoLocation = GeoLocation;
        this.CountryCode = CountryCode;
        this.TimeZone = TimeZone;
        this.Language = Language;
        this.MessageDate = MessageDate;
        this.Likes = Likes;
        this.Forwards = Forwards;
        this.Message = Message;
        this.DataSource = DataSource;
    }
}
