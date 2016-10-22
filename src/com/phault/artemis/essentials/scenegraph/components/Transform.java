package com.phault.artemis.essentials.scenegraph.components;

import com.artemis.PooledComponent;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by Casper on 19-07-2016.
 */
public class Transform extends PooledComponent {
    public Vector2 position = new Vector2(0, 0);
    public float rotation = 0;
    public Vector2 scale = new Vector2(1, 1);

    @Override
    protected void reset() {
        position.set(0, 0);
        rotation = 0;
        scale.set(1, 1);
    }
}
