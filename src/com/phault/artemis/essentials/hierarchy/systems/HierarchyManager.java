package com.phault.artemis.essentials.hierarchy.systems;

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;
import com.artemis.Component;
import com.artemis.ComponentMapper;
import com.artemis.link.EntityLinkManager;
import com.artemis.link.LinkListener;
import com.artemis.utils.Bag;
import com.artemis.utils.IntBag;
import com.phault.artemis.essentials.hierarchy.components.Children;
import com.phault.artemis.essentials.hierarchy.components.Parented;

/**
 * Created by Casper on 12-08-2016.
 */
public class HierarchyManager extends BaseEntitySystem implements LinkListener {

    private ComponentMapper<Parented> mParented;
    private ComponentMapper<Children> mChildren;

    private Bag<HierarchyChangedListener> listeners = new Bag<>();

    public HierarchyManager() {
        super(Aspect.all(Parented.class));
    }

    @Override
    protected void initialize() {
        super.initialize();
        world.getSystem(EntityLinkManager.class).register(Parented.class, this);
    }

    @Override
    protected void processSystem() {

    }

    public int getRoot(int entityId) {
        int prevId = -1;

        for (int id = entityId; id != -1; prevId = id, id = getParent(id));

        return prevId;
    }

    public int getParent(int entityId) {
        Parented parented = mParented.get(entityId);
        return parented != null
                ? parented.target
                : -1;
    }

    private final static IntBag emptyIntBag = new IntBag(0);

    public IntBag getChildren(int entityId) {
        Children children = mChildren.get(entityId);
        return children != null
                ? children.targets
                : emptyIntBag;
    }

    public <T extends Component> int getEntityWithComponentInParent(int childId, Class<T> type) {
        ComponentMapper<T> mapper = world.getMapper(type);

        for (int id = childId; id != -1; id = getParent(id)) {
            if (mapper.has(id))
                return id;
        }

        return -1;
    }

    public <T extends Component> T getComponentInParent(int childId, Class<T> type) {
        ComponentMapper<T> mapper = world.getMapper(type);

        int entityId = getEntityWithComponentInParent(childId, type);

        if (entityId == -1)
            return null;

        return mapper.get(entityId);
    }

//    public <T extends Component> T getComponentInChildren(int entityId, Class<T> type) {
//        ComponentMapper<T> mapper = world.getMapper(type);
//
//        T component = mapper.get(entityId);
//        if (component != null)
//            return component;
//
//        IntBag children = getChildren(entityId);
//        if (children != null) {
//            for (int i = 0; i < children.size(); i++) {
//                int childId = children.get(i);
//                component = mapper.get(childId);
//
//                if (component != null)
//                    return component;
//            }
//
//            for (int i = 0; i < children.size(); i++) {
//                int childId = children.get(i);
//                component = getComponentInChildren(childId, type);
//
//                if (component != null)
//                    return component;
//            }
//        }
//
//        return null;
//    }

    public void setParent(int entityId, int parentId) {
        Parented parented = mParented.create(entityId);
        parented.target = parentId;
    }

    @Override
    public void onLinkEstablished(int sourceId, int targetId) {
        Children children = mChildren.create(targetId);
        children.targets.add(sourceId);
        onParentChanged(sourceId, -1, targetId);
    }

    @Override
    public void onLinkKilled(int sourceId, int targetId) {
        removeFromChildren(sourceId, targetId);

        onParentChanged(sourceId, targetId, -1);
    }

    @Override
    public void onTargetDead(int sourceId, int deadTargetId) {
        mParented.remove(sourceId);
        onParentChanged(sourceId, deadTargetId, -1);
        onParentDied(sourceId, deadTargetId);
    }

    @Override
    public void onTargetChanged(int sourceId, int targetId, int oldTargetId) {
        Children children = mChildren.create(targetId);
        children.targets.add(sourceId);
        removeFromChildren(sourceId, oldTargetId);

        onParentChanged(sourceId, oldTargetId, targetId);
    }

    private void removeFromChildren(int childId, int parentId) {
        Children children = mChildren.get(parentId);
        if (children != null)
        {
            children.targets.removeValue(childId);
            if (children.targets.size() == 0)
                mChildren.remove(parentId);
        }
    }

    private void onParentChanged(int child, int prevParent, int newParent) {
        for (HierarchyChangedListener listener : listeners) {
            listener.onParentChanged(child, prevParent, newParent);
        }
    }

    private void onParentDied(int child, int deadParent) {
        for (HierarchyChangedListener listener : listeners) {
            listener.onParentDied(child, deadParent);
        }
    }

    public void registerListener(HierarchyChangedListener listener) {
        listeners.add(listener);
    }

    public void unregisterListener(HierarchyChangedListener listener) {
        listeners.remove(listener);
    }

    public interface HierarchyChangedListener {
        void onParentChanged(int child, int prevParent, int newParent);
        void onParentDied(int child, int deadParent);
    }

    public void runActionRecursively(int entityId, RecursiveAction action) {
        action.run(entityId);

        IntBag children = getChildren(entityId);
        for (int i = 0; i < children.size(); i++)
            runActionRecursively(children.get(i), action);
    }

    public interface RecursiveAction {
        void run(int entityId);
    }
}
