package com.phault.artemis.essentials.shaperendering.commands;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Created by caspe on 14-10-2016.
 */
public class DrawTriangleCommand extends DrawCommand {

    public final float[] triangle = new float[6];

    @Override
    public void execute(ShapeRenderer renderer) {
        super.execute(renderer);

        renderer.triangle(triangle[0], triangle[1],
                triangle[2], triangle[3],
                triangle[4], triangle[5]);
    }

    @Override
    public void reset() {
        super.reset();

        for (int i = 0; i < triangle.length; i++)
            triangle[i] = 0;
    }
}
