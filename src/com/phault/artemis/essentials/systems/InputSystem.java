package com.phault.artemis.essentials.systems;

import com.artemis.BaseSystem;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Casper on 18-09-2016.
 */
public class InputSystem extends BaseSystem implements InputProcessor {
    private InputMultiplexer multiplexer = new InputMultiplexer();

    @Override
    protected void processSystem() {

    }

    public void addProcessor (int index, InputProcessor processor) {
        multiplexer.addProcessor(index, processor);
    }

    public void removeProcessor (int index) {
        multiplexer.removeProcessor(index);
    }

    public void addProcessor (InputProcessor processor) {
        multiplexer.addProcessor(processor);
    }

    public void removeProcessor (InputProcessor processor) {
        multiplexer.removeProcessor(processor);
    }

    public int size () {
        return multiplexer.size();
    }

    public void clear () {
        multiplexer.clear();
    }

    public void setProcessors (Array<InputProcessor> processors) {
        multiplexer.setProcessors(processors);
    }

    public Array<InputProcessor> getProcessors () {
        return multiplexer.getProcessors();
    }

    public boolean keyDown (int keycode) {
        return multiplexer.keyDown(keycode);
    }

    public boolean keyUp (int keycode) {
        return multiplexer.keyUp(keycode);
    }

    public boolean keyTyped (char character) {
        return multiplexer.keyTyped(character);
    }

    public boolean touchDown (int screenX, int screenY, int pointer, int button) {
        return multiplexer.touchDown(screenX, screenY, pointer, button);
    }

    public boolean touchUp (int screenX, int screenY, int pointer, int button) {
        return multiplexer.touchUp(screenX, screenY, pointer, button);
    }

    public boolean touchDragged (int screenX, int screenY, int pointer) {
        return multiplexer.touchDragged(screenX, screenY, pointer);
    }

    public boolean mouseMoved (int screenX, int screenY) {
        return multiplexer.mouseMoved(screenX, screenY);
    }

    public boolean scrolled (int amount) {
        return multiplexer.scrolled(amount);
    }
}
