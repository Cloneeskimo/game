package gameobject;

import graphics.Camera;
import graphics.ShaderProgram;
import utils.Utils;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;

/**
 * Encapsulates a collection of game objects that react to a camera when rendered
 * Any objects added to this world are considered to be in world space when received and will maintained as such
 */
public class World {

    /**
     * Data
     */
    protected List<GameObject> gameObjects; // game objects
    protected Camera cam; // camera to use for view positioning
    protected ShaderProgram sp; // shader program to use for rendering
    private float ar; // the window's aspect ratio
    private boolean arAction; // aspect ratio action (see GameEngine.init)

    /**
     * Constructor
     * @param windowHandle the window's handle
     * @param ar the window's aspect ratio
     * @param arAction aspect ratio action (see GameEngine.init)
     */
    public World(long windowHandle, float ar, boolean arAction) {
        this.ar = ar; // save aspect ratio for rendering
        this.arAction = arAction; // save aspect ratio action for rendering
        this.gameObjects = new ArrayList<>(); // create game object list
        this.cam = new Camera(); // create camera
        this.initSP(); // initialize shader program
        glfwSetScrollCallback(windowHandle, (w, x, y) -> { // when the user scrolls
            this.cam.zoom(y > 0 ? 1.15f : 0.85f); // zoom on camera
        });
    }

    /**
     * Initializes the world's shader program
     */
    protected void initSP() {
        this.sp = new ShaderProgram("/shaders/worldV.glsl", "/shaders/worldF.glsl"); // create SP
        this.sp.registerUniform("x"); // register world x uniform
        this.sp.registerUniform("y"); // register world y uniform
        this.sp.registerUniform("scaleX"); // register x scale uniform
        this.sp.registerUniform("scaleY"); // register y scale uniform
        this.sp.registerUniform("ar"); // register aspect ratio uniform
        this.sp.registerUniform("arAction"); // register aspect ratio action uniform
        this.sp.registerUniform("isTextured"); // register texture flag uniform
        this.sp.registerUniform("color"); // register color uniform
        this.sp.registerUniform("blend"); // register blend uniform
        this.sp.registerUniform("texSampler"); // register texture sampler uniform
        this.sp.registerUniform("camX"); // register camera world x uniform
        this.sp.registerUniform("camY"); // register camera world y uniform
        this.sp.registerUniform("camZoom"); // register camera zoom uniform
    }

    /**
     * Updates the world's game objects and camera
     * @param interval the amount of time to account for
     */
    public void update(float interval) {
        for (GameObject o : this.gameObjects) o.update(interval); // update game objects
        this.cam.update(); // update camera
    }

    /**
     * Renders the world's game objects according to the camera
     */
    public void render() {
        this.sp.bind(); // bind shader program
        this.sp.setUniform("texSampler", 0); // set texture sampler uniform to use texture unit 0
        this.sp.setUniform("ar", this.ar); // set aspect ratio uniform
        this.sp.setUniform("arAction", this.arAction ? 1 : 0); // set aspect ratio action uniform
        this.sp.setUniform("camX", this.cam.getX()); // set camera x uniform
        this.sp.setUniform("camY", this.cam.getY()); // set camera y uniform
        this.sp.setUniform("camZoom", this.cam.getZoom()); // set camera zoom uniform
        for (GameObject o : this.gameObjects) o.render(this.sp); // render game objects
        this.sp.unbind(); // unbind shader program
    }

    /**
     * Handles a resize of the window
     * @param ar the new aspect ratio
     * @param arAction the new aspect ratio action (see GameEngine.init)
     */
    public void resized(float ar, boolean arAction) {
        this.ar = ar; // save new aspect ratio
        this.arAction = arAction; // save new aspect ratio action (see GameEngine.init)
    }

    /**
     * Adds the given game object to the world
     * @param o the game object to add
     */
    public void addObject(GameObject o) { this.gameObjects.add(o); }

    /**
     * Finds and returns the game object at the given index
     * @param i the index to find the game object at
     * @return the found game object
     */
    public GameObject getObject(int i) {
        try { // try to get item
            return this.gameObjects.get(i); // and return it
        } catch (Exception e) { // if exception
            Utils.handleException(e, "gameobjects.World", "getObject(i)", true); // handle exception
        }
        return null;
    }

    /**
     * @return the world's camera
     */
    public Camera getCam() { return this.cam; }

    /**
     * Cleans up the world
     */
    public void cleanup() {
        if (this.sp != null) this.sp.cleanup(); // cleanup shader program
        for (GameObject o : this.gameObjects) o.cleanup(); // cleanup game objects
    }
}
