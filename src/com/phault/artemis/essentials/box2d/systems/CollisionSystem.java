package com.phault.artemis.essentials.box2d.systems;

import com.artemis.*;
import com.artemis.utils.Bag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.phault.artemis.essentials.box2d.components.FixtureComponent;
import com.phault.artemis.essentials.box2d.components.Rigidbody;
import com.phault.artemis.essentials.hierarchy.systems.HierarchyManager;
import com.phault.artemis.essentials.scenegraph.systems.WorldTransformationManager;

/**
 * Created by Casper on 06-08-2016.
 */
public class CollisionSystem extends BaseEntitySystem implements ContactListener, WorldTransformationManager.TransformationChangedListener {

    private com.phault.artemis.essentials.scenegraph.systems.WorldTransformationManager transformManager;
    private HierarchyManager hierarchyManager;

    private float pixelsPerMeter = 100;
    private float metersPerPixel = 1/pixelsPerMeter;

    private Vector2 gravity = new Vector2(0, -10);
    private boolean isPaused = false;
    private World physicsWorld;

    private float timeStep = 1/60f;
    private int velocityIterations = 6;
    private int positionIterations = 2;
    private boolean isProcessingTransformations;

    private ComponentMapper<FixtureComponent> mFixtureComponent;
    private ComponentMapper<Rigidbody> mRigidbody;

    private Body staticBody;
    private final static BodyDef staticBodyDef = new BodyDef();

    private final static int GlobalListenerId = -1000;

    public CollisionSystem() {
        super(Aspect.all(com.phault.artemis.essentials.scenegraph.components.Transform.class, Rigidbody.class));
        Box2D.init();

        physicsWorld = new World(gravity, true);
//        physicsWorld.setContactListener(this);
        staticBody = physicsWorld.createBody(staticBodyDef);
    }

    private float accumulator = 0;

    private IntMap<Bag<CollisionListener>> listeners = new IntMap<>();

    @Override
    protected void initialize() {
        super.initialize();

        transformManager.registerListener(this);
    }

    @Override
    protected void dispose() {
        super.dispose();

        transformManager.unregisterListener(this);
    }

    @Override
    protected void processSystem() {
        if (isPaused)
            return;

        float frameTime = Math.min(Gdx.graphics.getRawDeltaTime(), 0.25f);
        accumulator += frameTime;

        while (accumulator >= timeStep) {
            physicsWorld.step(timeStep, velocityIterations, positionIterations);
            accumulator -= timeStep;
        }

        isProcessingTransformations = true;
        for (IntMap.Entry<Body> entry : bodyLinks) {
            int entityId = entry.key;
            Body body = entry.value;

            Vector2 position = body.getPosition();
            float rotation = MathUtils.radiansToDegrees * body.getAngle();

            transformManager.setWorldPosition(entityId, position.x * pixelsPerMeter, position.y * pixelsPerMeter);
            transformManager.setWorldRotation(entityId, rotation);
        }
        isProcessingTransformations = false;
    }

    private IntMap<Fixture> fixtureLinks = new IntMap<>();
    private IntMap<Body> bodyLinks = new IntMap<>();

    private void createLink(int entityId, Fixture fixture) {
        FixtureComponent component = mFixtureComponent.create(entityId);
        component.fixture = fixture;

        fixture.setUserData(entityId);
        fixtureLinks.put(entityId, fixture);
    }

    private void createLink(int entityId, Body body) {
        Rigidbody component = mRigidbody.create(entityId);
        component.body = body;

        body.setUserData(entityId);
        bodyLinks.put(entityId, body);
    }

    private void destroyFixtureLink(int entityId) {
        mFixtureComponent.remove(entityId);
        Fixture fixture = fixtureLinks.remove(entityId);
        if (fixture != null)
            fixture.setUserData(null);
    }

    private void destroyBodyLink(int entityId) {
        mRigidbody.remove(entityId);
        Body body = bodyLinks.remove(entityId);
        if (body != null)
            body.setUserData(null);
    }

    public Body createBody(int entityId, BodyDef definition) {
        Body body = physicsWorld.createBody(definition);
        createLink(entityId, body);
        return body;
    }

    public Fixture createFixture(int entityId, FixtureDef definition) {
        Body body = getAttachedBody(entityId);

        Fixture fixture = body.createFixture(definition);
        createLink(entityId, fixture);

        return fixture;
    }

    public Fixture createFixture(int entityId, Shape shape, float density) {
        Body body = getAttachedBody(entityId);

        Fixture fixture = body.createFixture(shape, density);
        createLink(entityId, fixture);

        return fixture;
    }

    public Body getAttachedBody(int entityId) {
        int rigidbodyId = hierarchyManager.getEntityWithComponentInParent(entityId, Rigidbody.class);

        if (rigidbodyId != -1)
            return bodyLinks.get(rigidbodyId);

        return staticBody;
    }

    public void destroyBody(int entityId) {
        Rigidbody rigidbody = mRigidbody.get(entityId);

        if (rigidbody == null)
            return;

        // since fixtures are destroyed as well, we'll have to clean up the components
        Array<Fixture> fixtures = rigidbody.body.getFixtureList();
        for (int i = 0; i < fixtures.size; i++) {
            Fixture fixture = fixtures.get(i);

            Object userData = fixture.getUserData();
            if (userData != null) {
                int fixtureId = (Integer) userData;
                destroyFixtureLink(fixtureId);
            }
        }

        physicsWorld.destroyBody(rigidbody.body);
        destroyBodyLink(entityId);
    }

    public void destroyBody(Body body) {
        int entityId = (Integer) body.getUserData();
        destroyBody(entityId);
    }

    public void destroyFixture(int entityId) {
        FixtureComponent component = mFixtureComponent.get(entityId);
        if (component == null)
            return;

        Body body = component.fixture.getBody();
        body.destroyFixture(component.fixture);
        destroyFixtureLink(entityId);
    }

    public void destroyFixture(Fixture fixture) {
        int entityId = (Integer) fixture.getUserData();
        destroyFixture(entityId);
    }

    public void addGlobalListener(CollisionListener listener) {
        addListener(GlobalListenerId, listener);
    }

    public void removeGlobalListener(CollisionListener listener) {
        removeListener(GlobalListenerId, listener);
    }

    public void addListener(int entityId, CollisionListener listener) {
        Bag<CollisionListener> listenerBag = listeners.get(entityId);

        if (listenerBag == null) {
            listenerBag = new Bag<>();
            listeners.put(entityId, listenerBag);
        }

        listenerBag.add(listener);
    }

    public void removeListener(int entityId, CollisionListener listener) {
        Bag<CollisionListener> listenerBag = listeners.get(entityId);

        if (listenerBag == null)
            return;

        listenerBag.remove(listener);
    }

    public void clearListeners() {
        listeners.clear();
    }

    public void clearListeners(int entityId) {
        listeners.remove(entityId);
    }

    @Override
    public void beginContact(Contact contact) {
        int idA = getBodyId(contact.getFixtureA());
        int idB = getBodyId(contact.getFixtureB());

        emitBeginContact(listeners.get(idA), idA, idB, contact);
        emitBeginContact(listeners.get(idB), idB, idA, contact);
        emitBeginContact(listeners.get(GlobalListenerId), idA, idB, contact);
    }

    private void emitBeginContact(Bag<CollisionListener> listeners, int thisId, int otherId, Contact contact) {
        if (listeners != null) {
            for (CollisionListener listener : listeners) {
                listener.onContactBegin(thisId, otherId, contact);
            }
        }
    }

    @Override
    public void endContact(Contact contact) {
        int idA = getBodyId(contact.getFixtureA());
        int idB = getBodyId(contact.getFixtureB());

        emitEndContact(listeners.get(idA), idA, idB, contact);
        emitEndContact(listeners.get(idB), idB, idA, contact);
        emitEndContact(listeners.get(GlobalListenerId), idA, idB, contact);
    }

    private void emitEndContact(Bag<CollisionListener> listeners, int thisId, int otherId, Contact contact) {
        if (listeners != null) {
            for (CollisionListener listener : listeners) {
                listener.onContactEnd(thisId, otherId, contact);
            }
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        int idA = getBodyId(contact.getFixtureA());
        int idB = getBodyId(contact.getFixtureB());

        // todo: fixture order inside the contact is currently reversed compared to thisId and otherId parameters

        emitPreSolve(listeners.get(idA), idA, idB, contact, oldManifold);
        emitPreSolve(listeners.get(idB), idB, idA, contact, oldManifold);
        emitPreSolve(listeners.get(GlobalListenerId), idA, idB, contact, oldManifold);
    }

    private void emitPreSolve(Bag<CollisionListener> listeners, int thisId, int otherId, Contact contact, Manifold oldManifold) {
        if (listeners != null) {
            for (CollisionListener listener : listeners) {
                listener.onPreSolve(thisId, otherId, contact, oldManifold);
            }
        }
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
        int idA = getBodyId(contact.getFixtureA());
        int idB = getBodyId(contact.getFixtureB());

        emitPostSolve(listeners.get(idA), idA, idB, contact, impulse);
        emitPostSolve(listeners.get(idB), idB, idA, contact, impulse);
        emitPostSolve(listeners.get(GlobalListenerId), idA, idB, contact, impulse);
    }

    private void emitPostSolve(Bag<CollisionListener> listeners, int thisId, int otherId, Contact contact, ContactImpulse impulse) {
        if (listeners != null) {
            for (CollisionListener listener : listeners) {
                listener.onPostSolve(thisId, otherId, contact, impulse);
            }
        }
    }

    public int getEntityId(Fixture fixture) {
        if (fixture.getUserData() == null)
            return -1;

        return (Integer) fixture.getUserData();
    }

    public int getEntityId(Body body) {
        if (body.getUserData() == null)
            return -1;

        return (Integer) body.getUserData();
    }

    private int getBodyId(Fixture fixture) {
        if (fixture != null)
            return getEntityId(fixture.getBody());

        return -1;
    }

    @Override
    protected void removed(int entityId) {
        super.removed(entityId);

        destroyBody(entityId);
    }

    public World getPhysicsWorld() {
        return physicsWorld;
    }

    public float getPixelsPerMeter() {
        return pixelsPerMeter;
    }
    public float getMetersPerPixel() {
        return metersPerPixel;
    }

    public void setPixelsPerMeter(float pixelsPerMeter) {
        this.pixelsPerMeter = pixelsPerMeter;
        metersPerPixel = 1 / pixelsPerMeter;
    }

    private final Vector2 tmpPosition = new Vector2();

    @Override
    public void onTransformationChanged(int entityId) {
        if (isProcessingTransformations)
            return;

        Body body = bodyLinks.get(entityId);

        if (body != null) {
            transformManager.getWorldPosition(entityId, tmpPosition);
            float rotation = MathUtils.degreesToRadians * transformManager.getWorldRotation(entityId);

            tmpPosition.scl(metersPerPixel);

            body.setTransform(tmpPosition, rotation);
        }

        // todo: update fixture
        // todo: recreate fixture with new scale (if changed)
    }


    private Body hitBody;
    private float pointQueryX, pointQueryY;
    private QueryCallback pointQueryCallback = new QueryCallback() {
        @Override
        public boolean reportFixture(Fixture fixture) {
            if (fixture.testPoint(pointQueryX, pointQueryY)) {
                hitBody = fixture.getBody();
                return false;
            }

            return true;
        }
    };

    public Body queryPoint(float x, float y) {
        hitBody = null;
        pointQueryX = x;
        pointQueryY = y;

        physicsWorld.QueryAABB(pointQueryCallback,
                x - 0.01f,
                y - 0.01f,
                x + 0.01f,
                y + 0.01f);

        return hitBody;
    }

    public static boolean isTriangleValid(float[] triangle) {
        if (triangle.length != 6)
            return false;

        float area = 0;

        for (int currentVert = 0; currentVert < triangle.length; currentVert += 2)
        {
            float x1 = triangle[currentVert];
            float y1 = triangle[currentVert+1];

            int nextVert = (currentVert + 2) < triangle.length ? currentVert + 2 : 0;
            float x2 = triangle[nextVert];
            float y2 = triangle[nextVert + 1];

            float D = x1 * y2 - y1 * x2;
            area += 0.5f * D;
        }

        return area > 0.00001f;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean paused) {
        isPaused = paused;
    }

    public interface CollisionListener {
        void onContactBegin(int thisId, int otherId, Contact contact);
        void onContactEnd(int thisId, int otherId, Contact contact);
        void onPreSolve(int thisId, int otherId, Contact contact, Manifold oldManifold);
        void onPostSolve(int thisId, int otherId, Contact contact, ContactImpulse impulse);
    }
}
