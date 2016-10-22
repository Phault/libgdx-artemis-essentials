package com.phault.artemis.essentials.components;

import com.artemis.PooledComponent;
import com.artemis.annotations.Transient;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by Casper on 19-07-2016.
 */
@Transient
public class Sprite extends PooledComponent {
    public TextureRegion texture = null;
    public Vector2 origin = new Vector2(0.5f, 0.5f);
    public Color tint = new Color(1, 1, 1, 1);

    @Override
    protected void reset() {
        texture = null;
        origin.set(0.5f, 0.5f);
    }
}
