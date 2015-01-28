package com.jaguth.fetcher;


public class GeoLocation
{
    public double Latitude;
    public double Longitude;

    public GeoLocation(double Latitude, double Longitude)
    {
        this.Latitude = Latitude;
        this.Longitude = Longitude;
    }

    @Override
    public String toString()
    {
        return Double.toString(Latitude) + "," + Double.toString(Longitude);
    }
}
