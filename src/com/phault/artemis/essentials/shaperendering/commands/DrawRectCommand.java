package com.phault.artemis.essentials.shaperendering.commands;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Created by caspe on 14-10-2016.
 */
public class DrawRectCommand extends DrawCommand {
    public float x, y, width, height;

    @Override
    public void execute(ShapeRenderer renderer) {
        super.execute(renderer);
        renderer.rect(x, y, width, height);
    }

    @Override
    public void reset() {
        super.reset();

        x = 0;
        y = 0;
        width = 0;
        height = 0;
    }
}
