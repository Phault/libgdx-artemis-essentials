package com.phault.artemis.essentials.shaperendering.components;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by Casper on 13-09-2016.
 */
public class RenderTriangle extends RenderShape {
    public final Vector2[] points = new Vector2[] {
        new Vector2(-50, -50),
        new Vector2(0, 50),
        new Vector2(50, -50)
    };

    @Override
    protected void reset() {
        super.reset();

        points[0].set(-50, -50);
        points[1].set(0, 50);
        points[2].set(50, -50);
    }
}
