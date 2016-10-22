package com.phault.artemis.essentials.shaperendering.components;

/**
 * Created by Casper on 13-09-2016.
 */
public class RenderRectangle extends RenderShape {

    public float width = 100, height = 100;

    @Override
    protected void reset() {
        super.reset();
        width = 100;
        height = 100;
    }
}
