package com.phault.artemis.essentials.shaperendering.commands;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Created by caspe on 14-10-2016.
 */
public class DrawCircleCommand extends DrawCommand {
    public float radius = 1;
    public float centerX, centerY;

    @Override
    public void execute(ShapeRenderer renderer) {
        super.execute(renderer);
        renderer.circle(centerX, centerY, radius);
    }

    @Override
    public void reset() {
        super.reset();
        radius = 1;
        centerX = 0;
        centerY = 0;
    }
}
