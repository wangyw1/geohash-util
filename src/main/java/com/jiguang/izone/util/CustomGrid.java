package com.jiguang.izone.util;


import java.io.Serializable;


//自定义网格
public class CustomGrid implements Serializable {

    private static final Double MIN_LAT = 15.0;
    private static final Double MIN_LNG = 70.0;
    private static final int RADIX = 36;
    private static final String STRING_SPLIT = "&";
    //经度边长间隔度数
    private double lngLength;
    //维度边长间隔度数
    private double latLength;

    private Double centerLat;
    //经度边长 单位m
    private int lngSideLength;
    //维度边长 单位m
    private int latSideLength;
    //经度在第几个网格内
    private int x;
    //维度在第几个网格内
    private int y;

    private static DistanceUtil distanceUtil = new DistanceUtil();

    private CustomGrid(int x, int y, int lngSideLength, int latSideLength, double centerLat) {
        this.lngSideLength = lngSideLength;
        this.latSideLength = latSideLength;
        this.centerLat = centerLat;

        this.lngLength = calculateLngLength(centerLat, lngSideLength);
        this.latLength = calculateLatLength(latSideLength);

        this.x = x;
        this.y = y;
    }

    public double getArea() {
        return lngSideLength * lngSideLength;
    }

    public CustomGrid(double lng, double lat, int lngSideLength, int latSideLength) {
        this.lngSideLength = lngSideLength;
        this.latSideLength = latSideLength;

        this.latLength = calculateLatLength(latSideLength);

        if (lat >= MIN_LAT) {
            this.y = Double.valueOf(Math.ceil((lat - MIN_LAT) / latLength)).intValue();
        } else {
            this.y = Double.valueOf(Math.floor((lat - MIN_LAT) / latLength)).intValue();
        }

        Double centerLat = y * latLength - 0.5 * latLength + MIN_LAT;

        this.centerLat = (double) Math.round(centerLat * 1000000) / 1000000;

        this.lngLength = calculateLngLength(centerLat, lngSideLength);

        if (lng >= MIN_LNG) {
            this.x = Double.valueOf(Math.ceil((lng - MIN_LNG) / lngLength)).intValue();
        } else {
            this.x = Double.valueOf(Math.floor((lng - MIN_LNG) / lngLength)).intValue();
        }

    }


    private void calculateLngLength() {
        double v = lngSideLength * 0.1 / distanceUtil.getDistance(MIN_LNG + "," + centerLat, (MIN_LNG + 0.1) + "," + centerLat);
        this.lngLength = (double) Math.round(v * 1000000) / 1000000;
    }

    private void calculateLatLength() {
        double v = latSideLength * 0.1 / distanceUtil.getDistance(MIN_LNG + "," + MIN_LAT, MIN_LNG + "," + (MIN_LAT + 0.1));
        this.latLength = (double) Math.round(v * 1000000) / 1000000;
    }

    private static double calculateLngLength(Double centerLat, int lngSideLength) {
        double v = lngSideLength * 0.1 / distanceUtil.getDistance(MIN_LNG + "," + centerLat, (MIN_LNG + 0.1) + "," + centerLat);
        return (double) Math.round(v * 1000000) / 1000000;
    }

    private static double calculateLatLength(int latSideLength) {
        double v = latSideLength * 0.1 / distanceUtil.getDistance(MIN_LNG + "," + MIN_LAT, MIN_LNG + "," + (MIN_LAT + 0.1));
        return (double) Math.round(v * 1000000) / 1000000;
    }


    public static CustomGrid withCharacterPrecision(double longitude, double latitude, int sideLength) {
        if (sideLength < 100) {
            throw new IllegalArgumentException("The minimum side length is 100m");
        } else if (Math.abs(latitude) > 90.0D || Math.abs(longitude) > 180.0D) {
            throw new IllegalArgumentException("Longitude and latitude are out of range");
        } else {
            return new CustomGrid(longitude, latitude, sideLength, sideLength);
        }
    }

    public static String geoGridStringWithCharacterPrecision(double longitude, double latitude, int sideLength) {
        CustomGrid customGrid = withCharacterPrecision(longitude, latitude, sideLength);
        return customGrid.toBase32();
    }

    public String toBase32() {

        long centerLongLat = Math.round(centerLat * 1000000);

        return Integer.toString(x, RADIX) + STRING_SPLIT + Integer.toString(y, RADIX) + STRING_SPLIT +
                Integer.toString(lngSideLength, RADIX) + STRING_SPLIT + Integer.toString(latSideLength, RADIX) +
                STRING_SPLIT + Long.toString(centerLongLat, RADIX);
    }


    public static CustomGrid fromGridString(String grid) {
        if (grid == null) {
            return null;
        }
        String[] split = grid.split(STRING_SPLIT);
        if (split.length != 5) {
            return null;
        } else {
            int x = Integer.valueOf(split[0], RADIX);
            int y = Integer.valueOf(split[1], RADIX);
            int lngSideLength = Integer.valueOf(split[2], RADIX);
            int latSideLength = Integer.valueOf(split[3], RADIX);
            double centerLat = Long.valueOf(split[4], RADIX)/1000000.0;
            if (centerLat > 90 || centerLat < -90 || lngSideLength < 100 || latSideLength < 100) {
                return null;
            }

            return new CustomGrid(x, y, lngSideLength, latSideLength, centerLat);
        }
    }

    private static String getCenterPoint(int x, int y, double lngLength, double latLength) {
        double centerLng;
        double centerLat;
        if (x > 0) {
            centerLng = x * lngLength - 0.5 * lngLength + MIN_LNG;
            centerLng = (double) Math.round(centerLng * 1000000) / 1000000;
        } else {
            centerLng = x * lngLength + 0.5 * lngLength + MIN_LNG;
            centerLng = (double) Math.round(centerLng * 1000000) / 1000000;
        }

        if (y > 0) {
            centerLat = y * latLength - 0.5 * latLength + MIN_LAT;
            centerLat = (double) Math.round(centerLat * 1000000) / 1000000;
        } else {
            centerLat = y * latLength + 0.5 * latLength + MIN_LAT;
            centerLat = (double) Math.round(centerLat * 1000000) / 1000000;
        }
        return centerLng + "," + centerLat;
    }

    public String getCenterPoint() {

        double centerLng;
        double centerLat;
        if (x > 0) {
            centerLng = x * lngLength - 0.5 * lngLength + MIN_LNG;
        } else {
            centerLng = x * lngLength + 0.5 * lngLength + MIN_LNG;
        }

        if (y > 0) {
            centerLat = y * latLength - 0.5 * latLength + MIN_LAT;
        } else {
            centerLat = y * latLength + 0.5 * latLength + MIN_LAT;
        }
        return (double) Math.round(centerLng * 1000000) / 1000000 + "," + (double) Math.round(centerLat * 1000000) / 1000000;
    }


    public double getMinLng() {

        double gridMinLng;
        if (x > 0) {
            gridMinLng = x * lngLength - 1.0 * lngLength + MIN_LNG;
        } else {
            gridMinLng = x * lngLength + MIN_LNG;
        }
        return (double) Math.round(gridMinLng * 1000000) / 1000000;
    }

    public double getMaxLng() {

        double gridMaxLng;
        if (x > 0) {
            gridMaxLng = x * lngLength + MIN_LNG;
        } else {
            gridMaxLng = x * lngLength + 1.0 * latLength + MIN_LNG;
        }
        return (double) Math.round(gridMaxLng * 1000000) / 1000000;
    }

    public double getMinLat() {

        double gridMinLat;
        if (y > 0) {
            gridMinLat = y * latLength - 1.0 * latLength + MIN_LAT;
        } else {
            gridMinLat = y * latLength + MIN_LAT;
        }
        return (double) Math.round(gridMinLat * 1000000) / 1000000;
    }

    public double getMaxLat() {

        double gridMaxLat;
        if (y > 0) {
            gridMaxLat = y * latLength + MIN_LAT;
        } else {
            gridMaxLat = y * latLength + 1.0 * latLength + MIN_LAT;
        }
        return (double) Math.round(gridMaxLat * 1000000) / 1000000;
    }
}
