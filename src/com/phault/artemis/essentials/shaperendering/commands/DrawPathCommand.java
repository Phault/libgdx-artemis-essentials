package com.phault.artemis.essentials.shaperendering.commands;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Created by caspe on 14-10-2016.
 */
public class DrawPathCommand extends DrawCommand {

    public float[] path;
    public float width;
    public boolean loop;

    @Override
    public void execute(ShapeRenderer renderer) {
        super.execute(renderer);

        for (int i = 0; i < path.length - 2; i += 2)
            renderer.rectLine(path[i], path[i + 1], path[i + 2], path[i + 3], width);

        if (loop && path.length >= 4)
            renderer.rectLine(path[path.length - 2], path[path.length - 1], path[0], path[1], width);
    }

    @Override
    public void reset() {
        super.reset();
        path = null;
        width = 0;
        loop = false;
    }
}
