package org.axiom.streamTest;

import java.awt.Color;
import java.awt.Point;
import java.io.File;

import org.axiom.streamTest.BezierImageGen.PointF;

public class Main {

    public static void main(String[] args) {
        BezierImageGen image = new BezierImageGen(new Point(1920, 1080), Color.CYAN);

        image.drawBezierLine(new PointF[]{
                new PointF(811.f, 53.f),
                new PointF(500.f,450.f),
                new PointF(520.f, 811.f),
                new PointF(301.f, 1000.f)}, Color.red, BezierImageGen.MODIFICATIONS.ROTATE, 6.f);
        image.drawBezierLine(new PointF[]{
                new PointF(611.f, 54.f),
                new PointF(311.f, 90.f),
                new PointF(1001.f, 721.f),
                new PointF(512.f, 312.f),
                new PointF(20.f, 720.f),
                new PointF(32.f, 411.f),
                new PointF(311.f, 23.f),
                new PointF(911.f, 912.f),
                new PointF(421.f, 512.f),
                new PointF(1501.f, 1011.f)}, Color.BLACK);

        image.writeIntoFile(new File("output.png"));
    }
}
