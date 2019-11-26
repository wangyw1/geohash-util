package com.jiguang.izone.util;

import ch.hsr.geohash.BoundingBox;
import ch.hsr.geohash.GeoHash;
import com.jiguang.izone.model.Mode;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import org.geotools.geojson.geom.GeometryJSON;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class GeohashPolyUtil {

    private static final char[] base32 = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
    private static final int MAX_GEOAHSH_NUM=9;

    public boolean isPtInPoly(GeoHash geohash, Geometry geometry, Mode mode) {

        if (mode.equals(Mode.INTERSECT)) {
            return geometry.intersects(toJTSPolygon(geohash));
        } else if (mode.equals(Mode.INSIDE)) {
            return geometry.contains(toJTSPolygon(geohash));
        }
        return false;
    }

    public boolean isPtInPoly(GeoHash geohash, Geometry geometry) {

        return geometry.intersects(toJTSPolygon(geohash));
    }

    public Geometry toJTSPolygon(GeoHash geohash) {

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

    public Set<String> findPolygonGeohashList(Geometry geometry, int numberOfCharacters, Mode mode) {
        Set<String> polygonGeohash = findPolygonGeohash(geometry, numberOfCharacters);
        Set<String> result= new HashSet<String>();
        if (mode.equals(Mode.INTERSECT)) {
            result=polygonGeohash;
        } else if (mode.equals(Mode.INSIDE)) {
            Set<String> tmpSet = new HashSet<String>();
            polygonGeohash.forEach(geohashstr -> {
                if (isPtInPoly(GeoHash.fromGeohashString(geohashstr), geometry, mode)) {
                    tmpSet.add(geohashstr);
                }
            });
            result = tmpSet;
        }
        return result;
    }

    public Set<String> findPolygonGeohashList(Geometry geometry, Mode mode) {
        int numberOfCharacters = 1;
        boolean flag = true;
        Set<String> polygonGeohash = new HashSet<String>();
        while (flag) {
            //先从geohash2位开始找
            polygonGeohash = findPolygonGeohash(geometry, ++numberOfCharacters);
            //geohash 的数量大于50或者geohash的位数大于等于9 不在增加geohash的位数
            if (polygonGeohash.size() > 50 || numberOfCharacters >= MAX_GEOAHSH_NUM) {
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

        //存储所有的geohash值 用于合饼后判断geohash的总数量是大于500
        Set<String> mergeAllGeohash = new HashSet<>();
        mergeAllGeohash.addAll(result);
        mergeAllGeohash.addAll(geohashIntersect);
        mergeGeohash(mergeAllGeohash);
        //与围栏相交的geohash值增加geohash的位数 查找更细 直到geohash的数量大于500或者geohash的位数达到9位
        while (mergeAllGeohash.size() <= 500 && numberOfCharacters < MAX_GEOAHSH_NUM) {
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
            mergeAllGeohash.clear();
            mergeAllGeohash.addAll(result);
            mergeAllGeohash.addAll(geohashIntersect);
            mergeGeohash(mergeAllGeohash);
        }

        if (mode.equals(Mode.INTERSECT)) {
            result.addAll(geohashIntersect);
        }
        return mergeGeohash(result);
    }

    public Geometry jsonToWkt(String geoJson) {
        String wkt = null;
        GeometryJSON gjson = new GeometryJSON();
        Reader reader = new StringReader(geoJson);
        Geometry geometry = null;
        try {
            geometry = gjson.read(reader);
            wkt = geometry.toText();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return geometry;
    }

    /**
     * 获取围栏面积
     * @param geometry
     * @return
     */
    public double getArea(Geometry geometry){
        return  geometry.getArea();
    }
}
