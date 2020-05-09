package com.jiguang.izone.util;

import ch.hsr.geohash.BoundingBox;
import ch.hsr.geohash.GeoHash;
import com.jiguang.izone.model.Mode;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import org.geotools.geojson.geom.GeometryJSON;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.*;


public class GeohashPolyUtil {

    private static final char[] base32 = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
    private static final int MAX_GEOAHSH_NUM = 9;

    /**
     * 判断围栏是否包含geohash块或者相交
     *
     * @param geohash
     * @param geometry
     * @param mode
     * @return
     */
    private boolean isPtInPoly(GeoHash geohash, Geometry geometry, Mode mode) {

        if (mode.equals(Mode.INTERSECT)) {
            return geometry.intersects(toJTSPolygon(geohash));
        } else if (mode.equals(Mode.INSIDE)) {
            return geometry.contains(toJTSPolygon(geohash));
        }
        return false;
    }

    private boolean isPtInPoly(GeoHash geohash, Geometry geometry) {

        return geometry.intersects(toJTSPolygon(geohash));
    }

    /**
     * @param geohash
     * @return
     */
    private Geometry toJTSPolygon(GeoHash geohash) {

        BoundingBox boundingBox = geohash.getBoundingBox();

        Coordinate[] coordinates = new Coordinate[]{
                new Coordinate(boundingBox.getMinLon(), boundingBox.getMinLat()),
                new Coordinate(boundingBox.getMinLon(), boundingBox.getMaxLat()),
                new Coordinate(boundingBox.getMaxLon(), boundingBox.getMaxLat()),
                new Coordinate(boundingBox.getMaxLon(), boundingBox.getMinLat()),
                new Coordinate(boundingBox.getMinLon(), boundingBox.getMinLat())
        };

        return new GeometryFactory().createPolygon(coordinates);
    }


    private Set<String> findPolygonGeohash(Geometry geometry, int numberOfCharacters) {
        Set<String> set = new HashSet<>();

        //添加中心点所在个geohash
        Point centroid = geometry.getCentroid();
        GeoHash centroidGeohash = GeoHash.withCharacterPrecision(centroid.getY(), centroid.getX(), numberOfCharacters);
        set.add(centroidGeohash.toBase32());

        //添加定点所在的geohash
        Coordinate[] coordinates = geometry.getCoordinates();
        for (int i = 0; i < coordinates.length; i++) {
            set.add(GeoHash.geoHashStringWithCharacterPrecision(coordinates[i].y, coordinates[i].x, numberOfCharacters));
        }
        boolean flag = true;
        Set<String> set2 = new HashSet<String>();//用来存放geohash(周围的geohash都被找出来的)
        while (flag) {
            flag = false;//用来判断所有的geohash是否都找到
            Set<String> set1 = new HashSet<String>();
            for (String str : set) {
                boolean flag1 = true;//用来判断每个geohash周围的是否都找到
                //正东方向
                GeoHash easternNeighbour = GeoHash.fromGeohashString(str).getEasternNeighbour();
                String easternNeighbourStr = easternNeighbour.toBase32();
                if (!set.contains(easternNeighbourStr) && !set2.contains(easternNeighbourStr)) {
                    if (isPtInPoly(easternNeighbour, geometry)) {
                        flag = true;
                        flag1 = false;
                        set1.add(easternNeighbourStr);
                    }
                }
                //东南方向
                GeoHash easternSouthernNeighbour = GeoHash.fromGeohashString(str).getEasternNeighbour().getSouthernNeighbour();
                String easternSouthernNeighbourStr = easternSouthernNeighbour.toBase32();
                if (!set.contains(easternSouthernNeighbourStr) && !set2.contains(easternSouthernNeighbourStr)) {
                    if (isPtInPoly(easternSouthernNeighbour, geometry)) {
                        flag = true;
                        flag1 = false;
                        set1.add(easternSouthernNeighbourStr);
                    }
                }

                //正北方向
                GeoHash northernNeighbour = GeoHash.fromGeohashString(str).getNorthernNeighbour();
                String northernNeighbourStr = northernNeighbour.toBase32();
                if (!set.contains(northernNeighbourStr) && !set2.contains(northernNeighbourStr)) {
                    if (isPtInPoly(northernNeighbour, geometry)) {
                        flag = true;
                        flag1 = false;
                        set1.add(northernNeighbourStr);
                    }
                }
                //东北方向
                GeoHash easternNorthernNeighbour = GeoHash.fromGeohashString(str).getNorthernNeighbour().getEasternNeighbour();
                String easternNorthernNeighbourStr = easternNorthernNeighbour.toBase32();
                if (!set.contains(easternNorthernNeighbourStr) && !set2.contains(easternNorthernNeighbourStr)) {
                    if (isPtInPoly(easternNorthernNeighbour, geometry)) {
                        flag = true;
                        flag1 = false;
                        set1.add(easternNorthernNeighbourStr);
                    }
                }
                //正南方向
                GeoHash southernNeighbour = GeoHash.fromGeohashString(str).getSouthernNeighbour();
                String southernNeighbourStr = southernNeighbour.toBase32();
                if (!set.contains(southernNeighbourStr) && !set2.contains(southernNeighbourStr)) {
                    if (isPtInPoly(southernNeighbour, geometry)) {
                        flag = true;
                        flag1 = false;
                        set1.add(southernNeighbourStr);
                    }
                }

                //西南方向
                GeoHash westernSouthernNeighbour = GeoHash.fromGeohashString(str).getSouthernNeighbour().getWesternNeighbour();
                String westernSouthernNeighbourStr = westernSouthernNeighbour.toBase32();
                if (!set.contains(westernSouthernNeighbourStr) && !set2.contains(westernSouthernNeighbourStr)) {
                    if (isPtInPoly(westernSouthernNeighbour, geometry)) {
                        flag = true;
                        flag1 = false;
                        set1.add(westernSouthernNeighbourStr);
                    }
                }
                //正西方向
                GeoHash westernNeighbour = GeoHash.fromGeohashString(str).getWesternNeighbour();
                String westernNeighbourStr = westernNeighbour.toBase32();
                if (!set.contains(westernNeighbourStr) && !set2.contains(westernNeighbourStr)) {
                    if (isPtInPoly(westernNeighbour, geometry)) {
                        flag = true;
                        flag1 = false;
                        set1.add(westernNeighbourStr);
                    }
                }
                //西北方向
                GeoHash westernNorthernNeighbour = GeoHash.fromGeohashString(str).getWesternNeighbour().getNorthernNeighbour();
                String westernNorthernNeighbourStr = westernNorthernNeighbour.toBase32();
                if (!set.contains(westernNorthernNeighbourStr) && !set2.contains(westernNorthernNeighbourStr)) {
                    if (isPtInPoly(westernNorthernNeighbour, geometry)) {
                        flag = true;
                        flag1 = false;
                        set1.add(westernNorthernNeighbourStr);
                    }
                }
                if (flag1) {
                    set2.add(str);
                } else {
                    set1.add(str);
                }
            }
            set = set1;
        }

//      判断顶点所在geohash是否在围栏内
        for (int i = 0; i < coordinates.length; i++) {
            GeoHash geoHash = GeoHash.withCharacterPrecision(coordinates[i].y, coordinates[i].x, numberOfCharacters);
            if (!isPtInPoly(geoHash, geometry)) {
                set2.remove(geoHash.toBase32());
            }
        }
        //判断中心点所在的geohash是否在围栏内
        if (!isPtInPoly(centroidGeohash, geometry)) {
            set2.remove(centroidGeohash.toBase32());
        }
        return set2;
    }

    public Set<String> mergeGeohash(Set<String> set) {
        int mergeBeforeSize = 1;
        int mergeAfterSize = 0;
        while (mergeAfterSize < mergeBeforeSize) {
            mergeBeforeSize = set.size();

            Map<String, Set<String>> geohashSetMap = new HashMap<String, Set<String>>();

            set.forEach(geohash -> {
                String geohashSubstr = geohash.substring(0, (geohash.length() - 1));

                if (geohashSetMap.containsKey(geohashSubstr)) {
                    Set<String> geohashSet = geohashSetMap.get(geohashSubstr);
                    geohashSet.add(geohash);
                } else {
                    Set<String> geohashSet = new HashSet<String>();
                    geohashSet.add(geohash);
                    geohashSetMap.put(geohashSubstr, geohashSet);
                }

            });
            set.clear();
            geohashSetMap.forEach((k, v) -> {

                if (v.size() < 32) {
                    set.addAll(v);
                } else {
                    set.add(k);
                }
            });
            mergeAfterSize = set.size();
        }
        return set;
    }

    public Set<String> findPolygonGeohashSet(Geometry geometry, int numberOfCharacters, Mode mode) {
        Map<Integer, Set<String>> polygonGeohashMap = findPolygonGeohashMap(geometry, numberOfCharacters, mode);

        Set<String> result = new HashSet<>();
        polygonGeohashMap.forEach((k, v) -> {

            if (k == numberOfCharacters) {
                result.addAll(v);
            }
            if (k > numberOfCharacters) {
                v.forEach(geohash -> {
                    result.add(geohash.substring(0, numberOfCharacters));
                });
            }
            if (k < numberOfCharacters) {

                int j=k;
                do{
                    Set<String> tmpSet = new HashSet<>();
                    v.forEach(geohashStr -> {
                        for (int i=0;i<base32.length;i++){
                            tmpSet.add(geohashStr+base32[i]);
                        }
                    });
                    v=tmpSet;
                    j++;
                }while(j < numberOfCharacters);

                result.addAll(v);
            }
        });

        return result;
    }

    public Map<Integer, Set<String>> findPolygonGeohashMap(Geometry geometry, int maxGeohashNum, Mode mode) {
        int numberOfCharacters = 1;
        boolean flag = true;
        Set<String> polygonGeohash = new HashSet<String>();
        while (flag) {
            //先从geohash2位开始找
            polygonGeohash = findPolygonGeohash(geometry, ++numberOfCharacters);
            //geohash 的数量大于50或者geohash的位数大于等于9 不在增加geohash的位数
            if (polygonGeohash.size() > 50 || numberOfCharacters >= maxGeohashNum) {
                flag = false;
            }
        }

        Set<String> geohashInside = new HashSet<String>();
        Set<String> geohashIntersect = new HashSet<String>();

        //区分出完全包含在围栏和围栏相交的geohash
        for (String geohashStr : polygonGeohash) {
            if (isPtInPoly(GeoHash.fromGeohashString(geohashStr), geometry, Mode.INSIDE)) {
                geohashInside.add(geohashStr);
            } else {
                geohashIntersect.add(geohashStr);
            }
        }

        Set<String> result = mergeGeohash(geohashInside);
        //与围栏相交的geohash值增加geohash的位数 查找更细 直到geohash的位数达到num
        while (numberOfCharacters < maxGeohashNum) {
            numberOfCharacters++;
            Set<String> geohashSetTmp = new HashSet<String>();
            for (String geohashStr : geohashIntersect) {
                for (int i = 0; i < base32.length; i++) {
                    String geohashTmp = geohashStr + base32[i];
                    if (isPtInPoly(GeoHash.fromGeohashString(geohashTmp), geometry, Mode.INSIDE)) {
                        result.add(geohashTmp);
                    } else if (isPtInPoly(GeoHash.fromGeohashString(geohashTmp), geometry, Mode.INTERSECT)) {
                        geohashSetTmp.add(geohashTmp);
                    }
                }
            }
            geohashIntersect = geohashSetTmp;
        }

        if (mode.equals(Mode.INTERSECT)) {
            result.addAll(geohashIntersect);
        }

        Set<String> mergeResult = mergeGeohash(result);

        Map<Integer, Set<String>> map = new HashMap<>();
        mergeResult.forEach(str -> {
            int length = str.length();
            if (map.containsKey(length)) {
                map.get(length).add(str);
            } else {
                Set<String> set = new HashSet<>();
                set.add(str);
                map.put(length, set);
            }
        });
        return map;
    }

    public Geometry jsonToWkt(String geoJson) {
//        String wkt = null;
        GeometryJSON gjson = new GeometryJSON();
        Reader reader = new StringReader(geoJson);
        Geometry geometry = null;
        try {
            geometry = gjson.read(reader);
//            wkt = geometry.toText();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return geometry;
    }

    public double getArea(Geometry geometry) {
        List<Point2D.Double> list = new ArrayList<>();
        Coordinate[] coordinates = geometry.getCoordinates();
        Arrays.stream(coordinates).forEach(coordinate -> {
            list.add(new Point2D.Double(coordinate.x, coordinate.y));
        });
        return getArea(list).doubleValue();
    }

    private BigDecimal getArea(List<Point2D.Double> ring) {
        double sJ = 6378245.0;
        double Hq = 0.017453292519943295;
        double c = sJ * Hq;
        double d = 0;

        if (3 > ring.size()) {
            return new BigDecimal(0);
        }

        for (int i = 0; i < ring.size() - 1; i++) {
            Point2D.Double h = ring.get(i);
            Point2D.Double k = ring.get(i + 1);
            double u = h.x * c * Math.cos(h.y * Hq);
            double hhh = h.y * c;
            double v = k.x * c * Math.cos(k.y * Hq);
            d = d + (u * k.y * c - v * hhh);
        }
        Point2D.Double g1 = ring.get(ring.size() - 1);
        Point2D.Double point = ring.get(0);
        double eee = g1.x * c * Math.cos(g1.y * Hq);
        double g2 = g1.y * c;
        double k = point.x * c * Math.cos(point.y * Hq);
        d += eee * point.y * c - k * g2;
        return new BigDecimal(0.5 * Math.abs(d)).divide(new BigDecimal(1));
    }

}
