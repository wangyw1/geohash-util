package com.jiguang.izone.test;

import com.jiguang.izone.model.Mode;
import com.jiguang.izone.util.GeohashPolyUtil;
import com.vividsolutions.jts.geom.Geometry;

import java.util.Set;

public class Test {
    public static void main(String[] args) {


        String geojson = "{\"type\":\"Polygon\",\"coordinates\":[[120.00,30.00],[120.01,30.00],[120.01,30.01],[120.00,30.01],[120.00,30.00]]}";
        GeohashPolyUtil geohashPolyUtil = new GeohashPolyUtil();
        Geometry geometry = geohashPolyUtil.jsonToWkt(geojson);
        Set<String> polygonGeohashSet = geohashPolyUtil.findPolygonGeohashSet(geometry, 8, Mode.INTERSECT);
        System.out.println(polygonGeohashSet.toString());

    }
}