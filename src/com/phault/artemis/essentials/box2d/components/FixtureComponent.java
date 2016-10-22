package com.phault.artemis.essentials.box2d.components;

import com.artemis.PooledComponent;
import com.artemis.annotations.Transient;
import com.badlogic.gdx.physics.box2d.Fixture;

/**
 * Created by Casper on 09-08-2016.
 */
@Transient
public class FixtureComponent extends PooledComponent {

    public Fixture fixture;

    @Override
    protected void reset() {
        fixture = null;
    }
}
