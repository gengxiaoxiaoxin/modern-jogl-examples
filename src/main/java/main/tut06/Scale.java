
package main.tut06;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.GLBuffers;
import main.framework.Framework;
import main.framework.Semantic;
import glm.mat.Mat4;
import glm.vec._3.Vec3;
import glm.vec._4.Vec4;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL2ES3.GL_DEPTH;
import static glm.GlmKt.glm;
import static uno.buffer.UtilKt.destroyBuffers;
import static uno.glsl.UtilKt.programOf;

/**
 * @author gbarbieri
 */
public class Scale extends Framework {

    public static void main(String[] args) {
        new Scale().setup("Tutorial 06 - Scale");
    }

    private interface Buffer {

        int VERTEX = 0;
        int INDEX = 1;
        int MAX = 2;
    }

    private int theProgram, modelToCameraMatrixUnif, cameraToClipMatrixUnif;

    private Mat4 cameraToClipMatrix = new Mat4(0.0f);
    private float frustumScale = calcFrustumScale(45.0f);

    private float calcFrustumScale(float fovDeg) {
        float fovRad = glm.radians(fovDeg);
        return 1.0f / glm.tan(fovRad / 2.0f);
    }

    private IntBuffer bufferObject = GLBuffers.newDirectIntBuffer(Buffer.MAX), vao = GLBuffers.newDirectIntBuffer(1);
    private final int numberOfVertices = 8;

    private final float[] GREEN_COLOR = {0.0f, 1.0f, 0.0f, 1.0f}, BLUE_COLOR = {0.0f, 0.0f, 1.0f, 1.0f},
            RED_COLOR = {1.0f, 0.0f, 0.0f, 1.0f}, BROWN_COLOR = {0.5f, 0.5f, 0.0f, 1.0f};

    private float[] vertexData = {

            +1.0f, +1.0f, +1.0f,
            -1.0f, -1.0f, +1.0f,
            -1.0f, +1.0f, -1.0f,
            +1.0f, -1.0f, -1.0f,

            -1.0f, -1.0f, -1.0f,
            +1.0f, +1.0f, -1.0f,
            +1.0f, -1.0f, +1.0f,
            -1.0f, +1.0f, +1.0f,


            GREEN_COLOR[0], GREEN_COLOR[1], GREEN_COLOR[2], GREEN_COLOR[3],
            BLUE_COLOR[0], BLUE_COLOR[1], BLUE_COLOR[2], BLUE_COLOR[3],
            RED_COLOR[0], RED_COLOR[1], RED_COLOR[2], RED_COLOR[3],
            BROWN_COLOR[0], BROWN_COLOR[1], BROWN_COLOR[2], BROWN_COLOR[3],

            GREEN_COLOR[0], GREEN_COLOR[1], GREEN_COLOR[2], GREEN_COLOR[3],
            BLUE_COLOR[0], BLUE_COLOR[1], BLUE_COLOR[2], BLUE_COLOR[3],
            RED_COLOR[0], RED_COLOR[1], RED_COLOR[2], RED_COLOR[3],
            BROWN_COLOR[0], BROWN_COLOR[1], BROWN_COLOR[2], BROWN_COLOR[3]};

    private short[] indexData = {

            0, 1, 2,
            1, 0, 3,
            2, 3, 0,
            3, 2, 1,

            5, 4, 6,
            4, 5, 7,
            7, 6, 4,
            6, 7, 5};

    private long start;

    @Override
    public void init(GL3 gl) {

        initializeProgram(gl);
        initializeVertexBuffers(gl);

        gl.glGenVertexArrays(1, vao);
        gl.glBindVertexArray(vao.get(0));

        int colorDataOffset = Vec3.SIZE * numberOfVertices;
        gl.glBindBuffer(GL_ARRAY_BUFFER, bufferObject.get(Buffer.VERTEX));
        gl.glEnableVertexAttribArray(Semantic.Attr.POSITION);
        gl.glEnableVertexAttribArray(Semantic.Attr.COLOR);
        gl.glVertexAttribPointer(Semantic.Attr.POSITION, Vec3.length, GL_FLOAT, false, Vec3.SIZE, 0);
        gl.glVertexAttribPointer(Semantic.Attr.COLOR, Vec4.length, GL_FLOAT, false, Vec4.SIZE, colorDataOffset);
        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferObject.get(Buffer.INDEX));

        gl.glBindVertexArray(0);

        gl.glEnable(GL_CULL_FACE);
        gl.glCullFace(GL_BACK);
        gl.glFrontFace(GL_CW);

        gl.glEnable(GL_DEPTH_TEST);
        gl.glDepthMask(true);
        gl.glDepthFunc(GL_LEQUAL);
        gl.glDepthRange(0.0f, 1.0f);

        start = System.currentTimeMillis();
    }

    private void initializeProgram(GL3 gl) {

        theProgram = programOf(gl, getClass(), "tut06", "pos-color-local-transform.vert", "color-passthrough.frag");

        modelToCameraMatrixUnif = gl.glGetUniformLocation(theProgram, "modelToCameraMatrix");
        cameraToClipMatrixUnif = gl.glGetUniformLocation(theProgram, "cameraToClipMatrix");

        float zNear = 1.0f, zFar = 61.0f;

        cameraToClipMatrix.v00(frustumScale);
        cameraToClipMatrix.v11(frustumScale);
        cameraToClipMatrix.v22((zFar + zNear) / (zNear - zFar));
        cameraToClipMatrix.v23(-1.0f);
        cameraToClipMatrix.v32((2 * zFar * zNear) / (zNear - zFar));

        gl.glUseProgram(theProgram);
        gl.glUniformMatrix4fv(cameraToClipMatrixUnif, 1, false, cameraToClipMatrix.to(matBuffer));
        gl.glUseProgram(0);
    }

    private void initializeVertexBuffers(GL3 gl) {

        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        ShortBuffer indexBuffer = GLBuffers.newDirectShortBuffer(indexData);

        gl.glGenBuffers(Buffer.MAX, bufferObject);

        gl.glBindBuffer(GL_ARRAY_BUFFER, bufferObject.get(Buffer.VERTEX));
        gl.glBufferData(GL_ARRAY_BUFFER, vertexBuffer.capacity() * Float.BYTES, vertexBuffer, GL_STATIC_DRAW);
        gl.glBindBuffer(GL_ARRAY_BUFFER, 0);

        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferObject.get(Buffer.INDEX));
        gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer.capacity() * Short.BYTES, indexBuffer, GL_STATIC_DRAW);
        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        destroyBuffers(vertexBuffer, indexBuffer);
    }

    @Override
    public void display(GL3 gl) {

        gl.glClearBufferfv(GL_COLOR, 0, clearColor.put(0, 0.0f).put(1, 0.0f).put(2, 0.0f).put(3, 0.0f));
        gl.glClearBufferfv(GL_DEPTH, 0, clearDepth.put(0, 1.0f));

        gl.glUseProgram(theProgram);

        gl.glBindVertexArray(vao.get(0));

        float elapsedTime = (System.currentTimeMillis() - start) / 1_000f;
        for (Instance instance : instanceList) {

            Mat4 transformMatrix = instance.constructMatrix(elapsedTime);

            gl.glUniformMatrix4fv(modelToCameraMatrixUnif, 1, false, transformMatrix.to(matBuffer));
            gl.glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
        }

        gl.glBindVertexArray(0);
        gl.glUseProgram(0);
    }

    @Override
    public void reshape(GL3 gl, int w, int h) {

        cameraToClipMatrix.v00(frustumScale * (h / (float) w));
        cameraToClipMatrix.v11(frustumScale);

        gl.glUseProgram(theProgram);
        gl.glUniformMatrix4fv(cameraToClipMatrixUnif, 1, false, cameraToClipMatrix.to(matBuffer));
        gl.glUseProgram(0);

        gl.glViewport(0, 0, w, h);
    }

    @Override
    public void end(GL3 gl) {

        gl.glDeleteProgram(theProgram);
        gl.glDeleteBuffers(Buffer.MAX, bufferObject);
        gl.glDeleteVertexArrays(1, vao);

        destroyBuffers(vao, bufferObject);
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {

        switch (keyEvent.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
                quit();
                break;
        }
    }

    private ScaleFun NullScale = (elapsedTime) -> new Vec3(1f);

    private ScaleFun StaticUniformScale = (elapsedTime) -> new Vec3(4f);

    private ScaleFun StaticNonUniformScale = (elapsedTime) -> new Vec3(0.5f, 1.0f, 10.0f);

    private ScaleFun DynamicUniformScale = (elapsedTime) -> {
        float loopDuration = 3.0f;
        return new Vec3(glm.mix(1.0f, 4.0f, calculateLerpFactor(elapsedTime, loopDuration)));
    };

    private ScaleFun DynamicNonUniformScale = (elapsedTime) -> {
        float xLoopDuration = 3.0f;
        float zLoopDuration = 5.0f;
        return new Vec3(
                glm.mix(1.0f, 0.5f, calculateLerpFactor(elapsedTime, xLoopDuration)),
                1.0f,
                glm.mix(1.0f, 10.0f, calculateLerpFactor(elapsedTime, zLoopDuration)));
    };

    private float calculateLerpFactor(float elapsedTime, float loopDuration) {
        float value = elapsedTime % loopDuration / loopDuration;
        if (value > 0.5f) {
            value = 1.0f - value;
        }
        return value * 2.0f;
    }

    private Instance[] instanceList = {
            new Instance(NullScale, new Vec3(0.0f, 0.0f, -45.0f)),
            new Instance(StaticUniformScale, new Vec3(-10.0f, -10.0f, -45.0f)),
            new Instance(StaticNonUniformScale, new Vec3(-10.0f, 10.0f, -45.0f)),
            new Instance(DynamicUniformScale, new Vec3(10.0f, 10.0f, -45.0f)),
            new Instance(DynamicNonUniformScale, new Vec3(10.0f, -10.0f, -45.0f))};

    @FunctionalInterface
    private interface ScaleFun {
        Vec3 execute(Float elapsedTime);
    }

    private class Instance {

        private ScaleFun calcScale;
        private Vec3 offset;
        private Vec3 vec = new Vec3();

        Instance(ScaleFun scaleFun, Vec3 offset) {
            calcScale = calcScale;
            this.offset = offset;
        }

        Mat4 constructMatrix(float elapsedTime) {

            Vec3 theScale = calcScale.execute(elapsedTime);
            Mat4 theMat = new Mat4(theScale, 1.0f);
            theMat.set(3, new Vec4(offset, 1.0f));

            return theMat;
        }
    }
}
