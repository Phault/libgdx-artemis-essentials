package com.phault.artemis.essentials.shaperendering.commands;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ShortArray;

public class DrawPolygonCommand extends DrawCommand {

    public float[] polygon;
    public ShortArray triangulation;

    @Override
    public void execute(ShapeRenderer renderer) {
        super.execute(renderer);

        for (int i = 0; i < triangulation.size; i += 3) {
            int vert1 = triangulation.get(i) * 2;
            float x1 = polygon[vert1];
            float y1 = polygon[vert1 + 1];

            int vert2 = triangulation.get(i+1) * 2;
            float x2 = polygon[vert2];
            float y2 = polygon[vert2 + 1];

            int vert3 = triangulation.get(i+2) * 2;
            float x3 = polygon[vert3];
            float y3 = polygon[vert3 + 1];
            renderer.triangle(x1, y1, x2, y2, x3, y3);
        }
    }

    @Override
    public void reset() {
        super.reset();

        polygon = null;
        triangulation = null;
    }
}
