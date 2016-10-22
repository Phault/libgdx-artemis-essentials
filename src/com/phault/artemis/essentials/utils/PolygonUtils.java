package com.phault.artemis.essentials.utils;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.FloatArray;

/**
 * Created by Casper on 12-10-2016.
 */
public class PolygonUtils {

    private static final Vector2 tmpCenter = new Vector2();
    public static void centerPolygon(float[] polygon) {
        getPolygonCenter(polygon, tmpCenter);
        offsetPolygon(polygon, -tmpCenter.x, -tmpCenter.y);
    }

    public static void centerPolygon(FloatArray polygon) {
        getPolygonCenter(polygon, tmpCenter);
        offsetPolygon(polygon, -tmpCenter.x, -tmpCenter.y);
    }

    public static void offsetPolygon(float[] polygon, float x, float y) {
        for (int i = 0; i < polygon.length; i += 2) {
            polygon[i] += x;
            polygon[i+1] += y;
        }
    }

    public static void offsetPolygon(FloatArray polygon, float x, float y) {
        for (int i = 0; i < polygon.size; i += 2) {
            polygon.set(i, polygon.get(i) + x);
            polygon.set(i + 1, polygon.get(i + 1) + y);
        }
    }

    public static Vector2 getPolygonCenter(float[] polygon, Vector2 result) {
        result.setZero();
        for (int i = 0; i < polygon.length; i += 2) {
            result.x += polygon[i];
            result.y += polygon[i+1];
        }

        result.x /= polygon.length / 2;
        result.y /= polygon.length / 2;

        return result;
    }

    public static Vector2 getPolygonCenter(FloatArray polygon, Vector2 result) {
        result.setZero();
        for (int i = 0; i < polygon.size; i += 2) {
            result.x += polygon.get(i);
            result.y += polygon.get(i+1);
        }

        result.x /= polygon.size / 2;
        result.y /= polygon.size / 2;

        return result;
    }
}
