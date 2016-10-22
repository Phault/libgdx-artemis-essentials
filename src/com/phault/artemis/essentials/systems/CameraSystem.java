package com.phault.artemis.essentials.systems;

import com.artemis.BaseSystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by Casper on 24-07-2016.
 */
public class CameraSystem extends BaseSystem {

    private OrthographicCamera camera;

    public CameraSystem() {
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false);
    }

    @Override
    protected void processSystem() {
        camera.update();
    }

    public Matrix4 getMatrix() {
        return camera.combined;
    }

    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    private final Vector3 tmpVector3 = new Vector3();
    public Vector2 screenToWorld(int x, int y, Vector2 result) {
        tmpVector3.set(x, y, 0);
        camera.unproject(tmpVector3);

        result.set(tmpVector3.x, tmpVector3.y);
        return result;
    }

    public Vector2 worldToScreen(float x, float y, Vector2 result) {
        tmpVector3.set(x, y, 0);
        camera.project(tmpVector3);

        result.set(tmpVector3.x, tmpVector3.y);
        return result;
    }

    public void setPosition(float x, float y) {
        camera.position.set(x, y, 0);
    }

    public Vector3 getPosition() {
        return camera.position;
    }

    public void setZoom(float value) {
        camera.zoom = value;
    }

    public float getZoom() {
        return camera.zoom;
    }
}
