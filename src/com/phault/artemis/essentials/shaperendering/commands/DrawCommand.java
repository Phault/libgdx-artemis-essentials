package com.phault.artemis.essentials.shaperendering.commands;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by caspe on 14-10-2016.
 */
public abstract class DrawCommand implements Pool.Poolable {
    public final Color color = Color.WHITE.cpy();

    public void execute(ShapeRenderer renderer) {
        renderer.setColor(color);
    }

    @Override
    public void reset() {
        color.set(Color.WHITE);
    }
}
