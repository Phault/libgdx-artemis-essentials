package com.phault.artemis.essentials.shaperendering.commands;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Created by caspe on 14-10-2016.
 */
public class DrawRectLineCommand extends DrawLineCommand {
    public float width = 1;

    @Override
    public void execute(ShapeRenderer renderer) {
        renderer.setColor(color);
        renderer.rectLine(startX, startY, endX, endY, width);
    }

    @Override
    public void reset() {
        super.reset();
        width = 1;
    }
}
