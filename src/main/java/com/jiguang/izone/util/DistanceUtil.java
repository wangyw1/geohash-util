package com.jiguang.izone.util;

import java.io.Serializable;

public class DistanceUtil implements Serializable {

    //地球半径单位KM
    private static final Double EARTH_RADIUS = 6378.137;

    public double getDistance(String coo1, String coo2) {

        Double lng1 = Double.valueOf(coo1.split(",")[0]);
        Double lat1 = Double.valueOf(coo1.split(",")[1]);

        Double lng2 = Double.valueOf(coo2.split(",")[0]);
        Double lat2 = Double.valueOf(coo2.split(",")[1]);

        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);

        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
                Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS * 1000;
        return s;
    }

    private double rad(double d) {
        return d * Math.PI / 180.0;
    }
}
