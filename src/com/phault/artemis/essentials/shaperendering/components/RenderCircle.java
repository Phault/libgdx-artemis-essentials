package com.phault.artemis.essentials.shaperendering.components;

/**
 * Created by Casper on 13-09-2016.
 */
public class RenderCircle extends RenderShape {

    public float radius = 100;

    @Override
    protected void reset() {
        super.reset();

        radius = 100;
    }
}
