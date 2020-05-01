package gameobject.gameworld;

import graphics.MSAT;
import graphics.Material;
import graphics.Model;
import utils.PhysicsEngine;

/**
 * Extends world objects by directly interacting with a multi-state animated texture in order to allow different
 * animations for different actions an entity might do
 */
public class Entity extends WorldObject {

    /**
     * Memberss
     */
    private boolean right;    // whether the entity is facing to the right
    private boolean airborne; // whether the entity is airborne
    private boolean moving;   // whether the entity is moving

    /**
     * Constructor
     *
     * @param model    the model to use
     * @param material the material to use. This model's texture should be an MSAT with six states representing the
     *                 following: (0) entity non-airborne and facing to the left, (1) entity non-airborne and facing to
     *                 the right, (2) entity airborne and facing to the left, (3) entity airborne and facing to the
     *                 right, (4) entity non-airborne and moving to the left, (5) entity non-airborne and moving to the
     *                 right
     */
    public Entity(Model model, Material material) {
        super(model, material);
    }

    /**
     * Tells the entity which way to face
     *
     * @param right whether the entity should face right (false makes the entity face left)
     */
    public void setFacing(boolean right) {
        if (right != this.right) {
            this.right = right; // update flag
            this.updateTextureState(); // update MSAT state
        }
    }

    /**
     * Updates the entity's MSAT based on what actions the entity is taking
     */
    private void updateTextureState() {
        if (this.material.getTexture() instanceof MSAT) { // if the material's texture is even an MSAT
            MSAT t = (MSAT) this.material.getTexture(); // get an MSAT reference to it
            int state = (right ? 1 : 0) + (airborne ? 2 : 0); // state based on left/right and airborne/non-airborne
            if (!this.airborne && this.moving) state += 4; // state based on moving
            t.setState(state); // set state
        }
    }

    /**
     * Updates the entity's moving flag which will update the animation
     *
     * @param moving the new moving flag
     */
    public void setIsMoving(boolean moving) {
        if (moving != this.moving) { // if moving changed
            this.moving = moving; // update moving flag
            this.updateTextureState(); // and update state in MSAT
        }
    }

    /**
     * Updates the entity
     *
     * @param interval the amount of time to account for
     */
    @Override
    public void update(float interval) {
        super.update(interval);
        boolean airborne = !PhysicsEngine.nextTo(this, 0f, -1f); // check if airborne
        if (airborne != this.airborne) { // if airborne value is different
            this.airborne = airborne; // update flag
            this.updateTextureState(); // and update state in MSAT
        }
    }
}