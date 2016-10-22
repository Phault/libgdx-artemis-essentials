package com.phault.artemis.essentials.shaperendering.utils;

import com.badlogic.gdx.math.Vector2;

public class VertexArray {
    private float[] vertices;
    private int size;

    public VertexArray(int size) {
        this.size = size;
        vertices = new float[size * 2];
    }

    public int size() {
        return size;
    }

    public void set(int index, float x, float y) {
        setX(index, x);
        setY(index, y);
    }

    public void setX(int index, float value) {
        vertices[index * 2] = value;
    }

    public void setY(int index, float value) {
        vertices[index * 2 + 1] = value;
    }

    public Vector2 get(int index, Vector2 result) {
        result.set(getX(index), getY(index));
        return result;
    }

    public float getX(int index) {
        return vertices[index * 2];
    }

    public float getY(int index) {
        return vertices[index * 2 + 1];
    }

    public float[] getBackingArray() {
        return vertices;
    }
}
