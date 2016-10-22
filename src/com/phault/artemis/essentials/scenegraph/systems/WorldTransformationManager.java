package com.phault.artemis.essentials.scenegraph.systems;

import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.utils.Bag;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Pool;
import com.phault.artemis.essentials.scenegraph.components.Transform;
import com.phault.artemis.essentials.hierarchy.systems.HierarchyManager;

/**
 * Created by Casper on 19-07-2016.
 */
public class WorldTransformationManager extends BaseSystem implements HierarchyManager.HierarchyChangedListener {

    private ComponentMapper<Transform> mTransform;
    private HierarchyManager hierarchyManager;

    private final IntMap<Matrix3> localToParentCache = new IntMap<>();
    private final IntMap<Matrix3> localToWorldCache = new IntMap<>();
    private final IntMap<Matrix3> worldToLocalCache = new IntMap<>();

    private final Pool<Matrix3> matrixPool = new Pool<Matrix3>() {
        @Override
        protected Matrix3 newObject() {
            return new Matrix3();
        }

        @Override
        protected void reset(Matrix3 object) {
            object.idt();
        }
    };

    @Override
    protected void initialize() {
        super.initialize();

        hierarchyManager.registerListener(this);
    }

    @Override
    protected void dispose() {
        super.dispose();

        localToParentCache.clear();
        localToWorldCache.clear();
        worldToLocalCache.clear();
        matrixPool.clear();

        hierarchyManager.unregisterListener(this);
    }

    @Override
    protected void processSystem() {

    }

    //region Local Properties
    public Vector2 getLocalPosition(int entityId, Vector2 localPosition) throws IllegalArgumentException {
        Transform transform = mTransform.get(entityId);

        if (transform == null)
            throw new IllegalArgumentException("Entity has no Transform component");

        return localPosition.set(transform.position);
    }

    public void setLocalPosition(int entityId, float x, float y) throws IllegalArgumentException {
        Transform transform = mTransform.get(entityId);

        if (transform == null)
            throw new IllegalArgumentException("Entity has no Transform component");

        if (transform.position.epsilonEquals(x, y, MathUtils.FLOAT_ROUNDING_ERROR))
            return;

        transform.position.set(x, y);
        markDirty(entityId);

        hierarchyManager.runActionRecursively(entityId, emitTransformationChanged);
    }

    public float getLocalRotation(int entityId) throws IllegalArgumentException {
        Transform transform = mTransform.get(entityId);

        if (transform == null)
            throw new IllegalArgumentException("Entity has no Transform component");

        return transform.rotation;
    }

    public void setLocalRotation(int entityId, float degrees) throws IllegalArgumentException {
        Transform transform = mTransform.get(entityId);

        if (transform == null)
            throw new IllegalArgumentException("Entity has no Transform component");

        if (MathUtils.isEqual(transform.rotation, degrees))
            return;

        transform.rotation = degrees;
        markDirty(entityId);

        hierarchyManager.runActionRecursively(entityId, emitTransformationChanged);
    }

    public Vector2 getLocalScale(int entityId, Vector2 localScale) throws IllegalArgumentException {
        Transform transform = mTransform.get(entityId);

        if (transform == null)
            throw new IllegalArgumentException("Entity has no Transform component");

        return localScale.set(transform.scale);
    }

    public void setLocalScale(int entityId, float x, float y) {
        Transform transform = mTransform.get(entityId);

        if (transform == null)
            throw new IllegalArgumentException("Entity has no Transform component");

        if (transform.scale.epsilonEquals(x, y, MathUtils.FLOAT_ROUNDING_ERROR))
            return;

        transform.scale.set(x, y);
        markDirty(entityId);

        hierarchyManager.runActionRecursively(entityId, emitTransformationChanged);
    }
    //endregion

    //region World Properties
    public Vector2 getWorldPosition(int entityId, Vector2 result) {
        getLocalPosition(entityId, result);

        int parent = hierarchyManager.getParent(entityId);
        if (parent != -1)
            return transformPoint(parent, result);

        return result;
    }

    private static final Vector2 tmpVector = new Vector2();

    public void setWorldPosition(int entityId, float x, float y) {
        int parent = hierarchyManager.getParent(entityId);
        if (parent != -1)
        {
            inverseTransformPoint(parent, x, y, tmpVector);
            setLocalPosition(entityId, tmpVector.x, tmpVector.y);
        }
        else
            setLocalPosition(entityId, x, y);
    }

    public float getWorldRotation(int entityId) {
        float localRot = getLocalRotation(entityId);

        int parent = hierarchyManager.getParent(entityId);
        if (parent != -1)
            return getLocalRotation(parent) + localRot;

        return localRot;
    }

    public void setWorldRotation(int entityId, float degrees) {
        int parent = hierarchyManager.getParent(entityId);
        if (parent != -1)
            setLocalRotation(entityId, degrees - getLocalRotation(parent));
        else
            setLocalRotation(entityId, degrees);
    }

    public Vector2 getWorldScale(int entityId, Vector2 result) {
        getLocalScale(entityId, result);

        int parent = hierarchyManager.getParent(entityId);
        if (parent != -1)
            return transformVector(parent, result);

        return result;
    }

    public void setWorldScale(int entityId, float x, float y) {
        int parent = hierarchyManager.getParent(entityId);
        if (parent != -1)
        {
            inverseTransformVector(parent, x, y, tmpVector);
            setLocalScale(entityId, tmpVector.x, tmpVector.y);
        }
        else
            setLocalScale(entityId, x, y);
    }
    //endregion

    // todo: might not be needed anymore due to LinkListener
//    public void setParent(int entityId, int parent)
//    {
//        Parented parented = mParented.create(entityId);
//        parented.target = parent;
//        markDirty(entityId);
//    }
//
//    public void removeParent(int entityId)
//    {
//        mParented.remove(entityId);
//        markDirty(entityId);
//    }

    //region Matrices
    public Matrix3 getLocalToParentMatrix(int entityId) {
        if (localToParentCache.containsKey(entityId))
            return localToParentCache.get(entityId);

        Transform transform = mTransform.get(entityId);

        if (transform == null)
            throw new IllegalArgumentException("Entity has no Transform component");

        Matrix3 matrix = matrixPool.obtain();
        matrix.translate(transform.position.x, transform.position.y);
        matrix.scale(transform.scale.x, transform.scale.y);
        matrix.rotate(transform.rotation);

        localToParentCache.put(entityId, matrix);

        return matrix;
    }

    public Matrix3 getLocalToWorldMatrix(int entityId) {
        if (localToWorldCache.containsKey(entityId))
            return localToWorldCache.get(entityId);

        Matrix3 localToParentMatrix = getLocalToParentMatrix(entityId);
        int parent = hierarchyManager.getParent(entityId);

        Matrix3 result = matrixPool.obtain().set(localToParentMatrix);

        if (parent != -1)
            result.mulLeft(getLocalToWorldMatrix(parent));

        localToWorldCache.put(entityId, result);

        return result;
    }

    public Matrix3 getWorldToLocalMatrix(int entityId) {
        if (worldToLocalCache.containsKey(entityId))
            return worldToLocalCache.get(entityId);

        Matrix3 result = matrixPool.obtain().set(getLocalToWorldMatrix(entityId)).inv();
        worldToLocalCache.put(entityId, result);
        return result;
    }
    //endregion

    //region Matrix transformation helpers
    public Vector2 transformPoint(int entityId, float x, float y, Vector2 transformedPoint) {
        return transformedPoint.set(x, y).mul(getLocalToWorldMatrix(entityId));
    }

    public Vector2 transformPoint(int entityId, Vector2 localPoint) {
        return transformPoint(entityId, localPoint.x, localPoint.y, localPoint);
    }

    public Vector2 inverseTransformPoint(int entityId, float x, float y, Vector2 transformedPoint) {
        return transformedPoint.set(x, y).mul(getWorldToLocalMatrix(entityId));
    }

    public Vector2 inverseTransformPoint(int entityId, Vector2 worldPoint) {
        return inverseTransformPoint(entityId, worldPoint.x, worldPoint.y, worldPoint);
    }

    public Vector2 transformDirection(int entityId, Vector2 localDirection) {
        Matrix3 matrix = getLocalToWorldMatrix(entityId);
        return mulNormal(localDirection, matrix);
    }

    public Vector2 inverseTransformDirection(int entityId, Vector2 worldDirection) {
        Matrix3 matrix = getWorldToLocalMatrix(entityId);
        return mulNormal(worldDirection, matrix);
    }

    private Vector2 mulNormal(Vector2 vec, Matrix3 matrix) {
        float tmpX = vec.x;
        float tmpY = vec.y;
        vec.x = (tmpX * matrix.val[Matrix3.M11]) + (tmpY * matrix.val[Matrix3.M21]);
        vec.y = (tmpX * matrix.val[Matrix3.M12]) + (tmpY * matrix.val[Matrix3.M22]);
        return vec;
    }

    public Vector2 transformVector(int entityId, float x, float y, Vector2 result) {
        Matrix3 matrix = getLocalToWorldMatrix(entityId);
        matrix.getScale(result);
        result.scl(x, y);
        return result;
    }

    public Vector2 transformVector(int entityId, Vector2 localVector) {
        return transformVector(entityId, localVector.x, localVector.y, localVector);
    }

    public Vector2 inverseTransformVector(int entityId, float x, float y, Vector2 result) {
        Matrix3 matrix = getWorldToLocalMatrix(entityId);
        matrix.getScale(result);
        result.scl(x, y);
        return result;
    }

    public Vector2 inverseTransformVector(int entityId, Vector2 worldVector) {
        return inverseTransformVector(entityId, worldVector.x, worldVector.y, worldVector);
    }
    //endregion

    private HierarchyManager.RecursiveAction markDirtyAction = new HierarchyManager.RecursiveAction() {
        @Override
        public void run(int entityId) {
            Matrix3 localToParentMatrix = localToParentCache.remove(entityId);
            Matrix3 localToWorldMatrix = localToWorldCache.remove(entityId);
            Matrix3 worldToLocalMatrix = worldToLocalCache.remove(entityId);

            if (localToParentMatrix != null)
                matrixPool.free(localToParentMatrix);
            if (localToWorldMatrix != null)
                matrixPool.free(localToWorldMatrix);
            if (worldToLocalMatrix != null)
                matrixPool.free(worldToLocalMatrix);
        }
    };

    private void markDirty(int entityId) {
        hierarchyManager.runActionRecursively(entityId, markDirtyAction);
    }

    @Override
    public void onParentChanged(int child, int prevParent, int newParent) {
        markDirty(child);
    }

    @Override
    public void onParentDied(int child, int deadParent) {
        // onParentChanged is called when the parent dies as well, so no reason to mark dirty here
    }

    private final Bag<TransformationChangedListener> listeners = new Bag<>();

    public void registerListener(TransformationChangedListener listener)
    {
        listeners.add(listener);
    }

    public void unregisterListener(TransformationChangedListener listener)
    {
        listeners.remove(listener);
    }

    private HierarchyManager.RecursiveAction emitTransformationChanged = new HierarchyManager.RecursiveAction() {
        @Override
        public void run(int entityId) {
            for (TransformationChangedListener listener : listeners)
                listener.onTransformationChanged(entityId);
        }
    };

    public interface TransformationChangedListener {
        void onTransformationChanged(int entityId);
    }
}
