package com.phault.artemis.essentials.box2d.utils;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;

/**
 * Created by Casper on 10-10-2016.
 */
public class RaycastClosestCallback implements RayCastCallback {

    public Fixture fixture;
    public float fraction;
    public final Vector2 point = new Vector2();
    public final Vector2 normal = new Vector2();

    @Override
    public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
        this.fixture = fixture;
        this.fraction = fraction;
        this.point.set(point);
        this.normal.set(normal);

        return fraction;
    }

    public void reset() {
        fixture = null;
    }
}
