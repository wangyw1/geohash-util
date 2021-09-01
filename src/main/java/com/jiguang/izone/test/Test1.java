package com.jiguang.izone.test;

import com.jiguang.izone.util.CustomGrid;

public class Test1 {
    public static void main(String[] args) {


        CustomGrid customGrid = new CustomGrid(120, 30, 500, 500);
        double area = customGrid.getArea();
        String centerPoint = customGrid.getCenterPoint();
        String s = customGrid.toBase32();
        System.out.println("area:"+area);
        System.out.println("centerPoint:"+centerPoint);

        CustomGrid customGrid1 = CustomGrid.fromGridString(s);

        System.out.println(customGrid1.getArea());
        System.out.println(customGrid1.getCenterPoint());

        System.out.println();
    }
}