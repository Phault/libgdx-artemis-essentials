package com.phault.artemis.essentials.box2d.components;

import com.artemis.PooledComponent;
import com.artemis.annotations.Transient;
import com.badlogic.gdx.physics.box2d.Body;

/**
 * Created by Casper on 06-08-2016.
 */
@Transient
public class Rigidbody extends PooledComponent{
    public Body body;

    @Override
    protected void reset() {
        body = null;
    }
}
