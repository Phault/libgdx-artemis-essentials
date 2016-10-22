package com.phault.artemis.essentials.shaperendering.commands;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Created by caspe on 14-10-2016.
 */
public class DrawLineCommand extends DrawCommand {
    public float startX, startY;
    public float endX, endY;

    @Override
    public void execute(ShapeRenderer renderer) {
        super.execute(renderer);
        renderer.line(startX, startY, endX, endY);
    }

    @Override
    public void reset() {
        super.reset();
        startX = 0;
        startY = 0;
        endX = 0;
        endY = 0;
    }
}
