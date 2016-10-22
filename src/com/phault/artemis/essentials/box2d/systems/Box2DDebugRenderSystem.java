package com.phault.artemis.essentials.box2d.systems;

import com.artemis.BaseSystem;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.phault.artemis.essentials.systems.CameraSystem;

/**
 * Created by Casper on 18-08-2016.
 */
public class Box2DDebugRenderSystem extends BaseSystem {

    private CollisionSystem collisionSystem;
    private CameraSystem cameraSystem;

    private Box2DDebugRenderer renderer = new Box2DDebugRenderer();

    private SpriteBatch spriteBatch = new SpriteBatch();

    private final Matrix4 debugMatrix = new Matrix4();

    @Override
    protected void initialize() {
        super.initialize();
    }

    @Override
    protected void processSystem() {
        debugMatrix.set(cameraSystem.getMatrix())
                .scale(collisionSystem.getPixelsPerMeter(), collisionSystem.getPixelsPerMeter(), 1);

        spriteBatch.begin();
        renderer.render(collisionSystem.getPhysicsWorld(), debugMatrix);
        spriteBatch.end();
    }

    @Override
    protected void dispose() {
        super.dispose();

        spriteBatch.dispose();
        renderer.dispose();
    }
}
