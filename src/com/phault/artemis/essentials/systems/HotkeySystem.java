package com.phault.artemis.essentials.systems;

import com.artemis.BaseSystem;
import com.artemis.utils.Bag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by Casper on 18-09-2016.
 */
public class HotkeySystem extends BaseSystem implements InputProcessor {

    private InputSystem inputSystem;

    private final IntMap<Bag<Hotkey>> hotkeyMap = new IntMap<>();

    private Pool<Hotkey> hotkeyPool = new Pool<Hotkey>() {
        @Override
        protected Hotkey newObject() {
            return new Hotkey();
        }
    };

    @Override
    protected void initialize() {
        super.initialize();

        inputSystem.addProcessor(this);
    }

    @Override
    protected void processSystem() {

    }

    @Override
    protected void dispose() {
        super.dispose();

        inputSystem.removeProcessor(this);
    }

    private Hotkey getHotkey(int key, int modifiers) {
        Bag<Hotkey> hotkeys = hotkeyMap.get(key);

        if (hotkeys == null)
            return null;

        for (int i = 0; i < hotkeys.size(); i++) {
            Hotkey hotkey = hotkeys.get(i);
            if (hotkey.modifiers == modifiers)
                return hotkey;
        }

        return null;
    }

    public void addListener(int key, int modifiers, HotkeyListener listener) {
        Hotkey hotkey = getHotkey(key, modifiers);

        if (hotkey == null) {
            hotkey = hotkeyPool.obtain();
            hotkey.key = key;
            hotkey.modifiers = modifiers;
        }

        hotkey.listeners.add(listener);
        addHotkey(hotkey);
    }

    public void removeListener(int key, int modifiers, HotkeyListener listener) {
        Hotkey hotkey = getHotkey(key, modifiers);
        if (hotkey == null)
            return;

        hotkey.listeners.removeValue(listener, true);

        if (hotkey.listeners.size == 0)
            removeHotkey(hotkey);
    }

    public void removeListeners(int key, int modifiers) {
        Hotkey hotkey = getHotkey(key, modifiers);
        if (hotkey == null)
            return;
        removeHotkey(hotkey);
    }

    public void clearKey(int key) {
        Bag<Hotkey> hotkeys = hotkeyMap.get(key);

        if (hotkeys == null)
            return;

        for (int i = 0; i < hotkeys.size(); i++) {
            Hotkey hotkey = hotkeys.get(i);
            hotkey.listeners.clear();
            hotkeyPool.free(hotkey);
        }
        hotkeys.clear();
    }

    private void addHotkey(Hotkey hotkey) {
        Bag<Hotkey> hotkeys = hotkeyMap.get(hotkey.key);

        if (hotkeys == null) {
            hotkeys = new Bag<>();
            hotkeyMap.put(hotkey.key, hotkeys);
        }

        hotkeys.add(hotkey);
    }

    private void removeHotkey(Hotkey hotkey) {
        Bag<Hotkey> hotkeys = hotkeyMap.get(hotkey.key);
        if (hotkeys == null)
            return;

        hotkey.listeners.clear();

        hotkeys.remove(hotkey);
        hotkeyPool.free(hotkey);
    }

    @Override
    public boolean keyDown(int keycode) {

        Bag<Hotkey> hotkeys = hotkeyMap.get(keycode);

        if (hotkeys == null)
            return false;

        boolean handled = false;

        for (int i = 0; i < hotkeys.size(); i++) {
            Hotkey hotkey = hotkeys.get(i);

            if (areModifiersSatisfied(hotkey.modifiers, true)) {
                handled |= hotkey.execute();
            }
        }

        return handled;
    }

    private boolean areModifiersSatisfied(int modifiers, boolean strict) {
        int activeModifiers = getActiveModifiers();
        if (strict)
            return activeModifiers == modifiers;
        else
            return (activeModifiers & modifiers) != 0;
    }

    public int getActiveModifiers() {
        int modifiers = Modifiers.NONE;

        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT))
            modifiers |= Modifiers.CTRL;

        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT))
            modifiers |= Modifiers.SHIFT;

        if (Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT))
            modifiers |= Modifiers.ALT;

        return modifiers;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    public static class Modifiers {
        public static final int NONE = 0,
                                CTRL = 1,
                                SHIFT = 2,
                                ALT = 4;
    }

    public interface HotkeyListener {
        boolean execute();
    }

    private class Hotkey implements Pool.Poolable {
        int key;
        int modifiers = Modifiers.NONE;
        final Array<HotkeyListener> listeners = new Array<>();

        boolean execute() {
            for (HotkeyListener listener : listeners) {
                if (listener.execute())
                    return true;
            }

            return false;
        }

        @Override
        public void reset() {
            key = 0;
            modifiers = Modifiers.NONE;
            listeners.clear();
        }
    }
}