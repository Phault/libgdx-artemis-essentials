package com.phault.artemis.essentials.shaperendering.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
import com.phault.artemis.essentials.scenegraph.components.Transform;
import com.phault.artemis.essentials.scenegraph.systems.WorldTransformationManager;
import com.phault.artemis.essentials.shaperendering.commands.*;
import com.phault.artemis.essentials.shaperendering.components.*;
import com.phault.artemis.essentials.shaperendering.utils.VertexArray;
import com.phault.artemis.essentials.systems.CameraSystem;

import java.util.EnumMap;

/**
 * Created by Casper on 13-09-2016.
 */
public class ShapeRenderSystem extends IteratingSystem {

    private ComponentMapper<RenderRectangle> mRectangles;
    private ComponentMapper<RenderCircle> mCircles;
    private ComponentMapper<RenderTriangle> mTriangles;
    private ComponentMapper<RenderPolygon> mPolygons;

    private CameraSystem cameraSystem;
    private WorldTransformationManager transformManager;

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();

    private final EnumMap<ShapeRenderer.ShapeType, Queue<DrawCommand>> renderQueues = new EnumMap<>(ShapeRenderer.ShapeType.class);
    private final ObjectMap<Class, Pool> commandPools = new ObjectMap<>();

    private final Color color = Color.WHITE.cpy();
    private ShapeRenderer.ShapeType shapeType = ShapeRenderer.ShapeType.Filled;

    public ShapeRenderSystem() {
        super(Aspect.all(Transform.class)
                .one(
                        RenderCircle.class,
                        RenderRectangle.class,
                        RenderTriangle.class,
                        RenderPolygon.class
                ));
    }

    @Override
    protected void begin() {
        super.begin();

        shapeRenderer.setProjectionMatrix(cameraSystem.getMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    }

    @Override
    protected void process(int entityId) {
        shapeRenderer.getTransformMatrix().set(transformManager.getLocalToWorldMatrix(entityId));
        shapeRenderer.updateMatrices();

        RenderShape shape = mRectangles.get(entityId);
        if (shape != null)
            renderRectangle((RenderRectangle) shape);

        shape = mCircles.get(entityId);
        if (shape != null)
            renderCircle((RenderCircle) shape);

        shape = mTriangles.get(entityId);
        if (shape != null)
            renderTriangle((RenderTriangle) shape);

        shape = mPolygons.get(entityId);
        if (shape != null)
            renderPolygon((RenderPolygon) shape);

        shapeRenderer.identity();
    }

    private void processQueue(ShapeRenderer.ShapeType shapeType) {
        Queue<DrawCommand> queue = renderQueues.get(shapeType);

        if (queue != null) {
            shapeRenderer.begin(shapeType);
            while (queue.size > 0) {
                DrawCommand command = queue.removeFirst();
                command.execute(shapeRenderer);
                free(command);
            }
            shapeRenderer.end();
        }
    }

    private void renderRectangle(RenderRectangle rectangle) {
        float originX = rectangle.origin.x * rectangle.width;
        float originY = rectangle.origin.y * rectangle.height;

        shapeRenderer.setColor(rectangle.color);
        shapeRenderer.rect(-originX,
                -originY,
                rectangle.width,
                rectangle.height);
    }

    private void renderCircle(RenderCircle circle) {
        float diameter = circle.radius * 2;
        float originX = (-0.5f + circle.origin.x) * diameter;
        float originY = (-0.5f + circle.origin.y) * diameter;

        shapeRenderer.setColor(circle.color);
        shapeRenderer.circle(originX, originY, circle.radius);
    }

    private void renderTriangle(RenderTriangle triangle) {
        shapeRenderer.setColor(triangle.color);
        shapeRenderer.triangle(triangle.points[0].x, triangle.points[0].y,
                triangle.points[1].x, triangle.points[1].y,
                triangle.points[2].x, triangle.points[2].y);
    }

    private final Vector2[] tmpTriangle = new Vector2[] {
            new Vector2(),
            new Vector2(),
            new Vector2(),
    };

    private void renderPolygon(RenderPolygon polygon) {
        shapeRenderer.setColor(polygon.color);

        VertexArray vertices = polygon.vertices;
        ShortArray triangulation = polygon.triangulation;

        for (int i = 0; i < triangulation.size; i += 3) {
            vertices.get(triangulation.get(i), tmpTriangle[0]);
            vertices.get(triangulation.get(i + 1), tmpTriangle[1]);
            vertices.get(triangulation.get(i + 2), tmpTriangle[2]);
            shapeRenderer.triangle(tmpTriangle[0].x, tmpTriangle[0].y,
                    tmpTriangle[1].x, tmpTriangle[1].y,
                    tmpTriangle[2].x, tmpTriangle[2].y);
        }
    }

    public void drawLine(float startX, float startY, float endX, float endY) {
        DrawLineCommand command = obtain(DrawLineCommand.class);

        command.color.set(color);
        command.startX = startX;
        command.startY = startY;
        command.endX = endX;
        command.endY = endY;

        enqueue(command);
    }

    public void drawRectLine(float startX, float startY, float endX, float endY, float width) {
        DrawRectLineCommand command = obtain(DrawRectLineCommand.class);

        command.color.set(color);
        command.startX = startX;
        command.startY = startY;
        command.endX = endX;
        command.endY = endY;
        command.width = width;

        enqueue(command);
    }

    public void drawCircle(float x, float y, float radius) {
        DrawCircleCommand command = obtain(DrawCircleCommand.class);

        command.color.set(color);
        command.centerX = x;
        command.centerY = y;
        command.radius = radius;

        enqueue(command);
    }

    public void drawRect(float x, float y, float width, float height) {
        DrawRectCommand command = obtain(DrawRectCommand.class);

        command.color.set(color);
        command.x = x;
        command.y = y;
        command.width = width;
        command.height = height;

        enqueue(command);
    }

    public void drawPolygon(VertexArray polygon, ShortArray triangulation) {
        drawPolygon(polygon.getBackingArray(), triangulation);
    }

    public void drawPolygon(float[] polygon, ShortArray triangulation) {
        DrawPolygonCommand command = obtain(DrawPolygonCommand.class);

        command.color.set(color);
        command.polygon = polygon;
        command.triangulation = triangulation;

        enqueue(command);
    }

    public void drawTriangle(float[] triangle) {
        drawTriangle(triangle[0], triangle[1],
                triangle[2], triangle[3],
                triangle[4], triangle[5]);
    }

    public void drawTriangle(float x1, float y1, float x2, float y2, float x3, float y3) {
        DrawTriangleCommand command = obtain(DrawTriangleCommand.class);

        command.color.set(color);
        command.triangle[0] = x1;
        command.triangle[1] = y1;
        command.triangle[2] = x2;
        command.triangle[3] = y2;
        command.triangle[4] = x3;
        command.triangle[5] = y3;

        enqueue(command);
    }

    public void drawPath(float[] path, float width, boolean loop) {
        DrawPathCommand command = obtain(DrawPathCommand.class);

        command.color.set(color);
        command.path = path;
        command.width = width;
        command.loop = loop;

        enqueue(command);
    }

    private <T extends DrawCommand> T obtain(final Class<T> type) {
        Pool pool = commandPools.get(type);

        if (pool == null) {
            pool = new ReflectionPool<>(type);
            commandPools.put(type, pool);
        }

        return (T) pool.obtain();
    }

    private void free(DrawCommand command) {
        Class type = command.getClass();
        if (commandPools.containsKey(type))
            commandPools.get(type).free(command);
    }

    private void enqueue(DrawCommand command) {
        Queue<DrawCommand> queue = renderQueues.get(shapeType);

        if (queue == null) {
            queue = new Queue<>();
            renderQueues.put(shapeType, queue);
        }

        queue.addLast(command);
    }

    @Override
    protected void end() {
        super.end();

        shapeRenderer.end();

        processQueue(ShapeRenderer.ShapeType.Filled);
        processQueue(ShapeRenderer.ShapeType.Line);
        processQueue(ShapeRenderer.ShapeType.Point);
    }

    @Override
    protected void dispose() {
        super.dispose();

        shapeRenderer.dispose();
    }

    public void setColor(Color color) {
        this.color.set(color);
    }

    public Color getColor() {
        return color;
    }

    public void setShapeType(ShapeRenderer.ShapeType type) {
        shapeType = type;
    }

    public ShapeRenderer.ShapeType getShapeType() {
        return shapeType;
    }
}

