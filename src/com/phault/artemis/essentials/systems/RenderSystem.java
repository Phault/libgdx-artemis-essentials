package com.phault.artemis.essentials.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.phault.artemis.essentials.components.Sprite;
import com.phault.artemis.essentials.scenegraph.components.Transform;

/**
 * Created by Casper on 19-07-2016.
 */
public class RenderSystem extends IteratingSystem
{
    private com.phault.artemis.essentials.scenegraph.systems.WorldTransformationManager transformManager;
    private ComponentMapper<Sprite> mSprite;
    private ComponentMapper<Transform> mTransform;

    private CameraSystem cameraSystem;

    private SpriteBatch spriteBatch;
    private final Color originalTint = new Color();

    public RenderSystem() {
        super(Aspect.all(Sprite.class, Transform.class));

        spriteBatch = new SpriteBatch();
    }

    @Override
    protected void initialize() {
        super.initialize();
    }

    @Override
    protected void begin() {
        originalTint.set(spriteBatch.getColor());
        spriteBatch.setProjectionMatrix(cameraSystem.getMatrix());
        spriteBatch.begin();
    }

    private final Vector2 pos = new Vector2();
    private final Vector2 scale = new Vector2();

    @Override
    protected void process(int entityId) {
        Sprite sprite = mSprite.get(entityId);

        transformManager.getWorldPosition(entityId, pos);
        transformManager.getWorldScale(entityId, scale);
        float rotation = transformManager.getWorldRotation(entityId);

        float width = sprite.texture.getRegionWidth();
        float height = sprite.texture.getRegionHeight();

        float originX = sprite.origin.x * width;
        float originY = sprite.origin.y * height;

        spriteBatch.setColor(sprite.tint);

        spriteBatch.draw(sprite.texture,
                pos.x - originX,
                pos.y - originY,
                originX,
                originY,
                width,
                height,
                scale.x,
                scale.y,
                rotation);
    }

    @Override
    protected void end() {
        spriteBatch.end();
        spriteBatch.setColor(originalTint);
    }

    @Override
    protected void dispose() {
        super.dispose();
        spriteBatch.dispose();
    }
}
