package com.phault.artemis.essentials.shaperendering.components;

import com.artemis.PooledComponent;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by Casper on 13-09-2016.
 */
public abstract class RenderShape extends PooledComponent {

    public final Color color = new Color(1, 1, 1, 1);
    public final Vector2 origin = new Vector2(0.5f, 0.5f);

    @Override
    protected void reset() {
        color.set(1,1,1,1);
        origin.set(0.5f, 0.5f);
    }
}
