package graphics;

import org.apache.commons.io.IOUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL32;
import org.lwjgl.system.MemoryStack;
import utils.Utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.stb.STBImage.*;

/*
 * Texture.java
 * Ambulare
 * Jacob Oaks
 * 4/16/20
 */

/**
 * Represents a Texture
 */
public class Texture {

    /**
     * Members
     */
    private final int id, w, h; // texture ID, width, and height

    /**
     * Constructor
     *
     * @param path    the path to the image
     * @param resRelative whether the given path is resource-relative
     */
    public Texture(String path, boolean resRelative) {

        // create buffers to hold texture info
        ByteBuffer buf = null; // create buffer for texture data
        MemoryStack stack = MemoryStack.stackPush(); // push memory stack for buffers
        IntBuffer w = stack.mallocInt(1); // create buffer for texture width
        IntBuffer h = stack.mallocInt(1); // create buffer for texture height
        IntBuffer channels = stack.mallocInt(1); // create buffer to hold channel amount (4 if rgba)

        // attempt to load texture
        try {
            ByteBuffer buff = Utils.fileToByteBuffer(path, resRelative, 1024); // convert image to byte buffer
            buf = stbi_load_from_memory(buff, w, h, channels, 4); // load image into texture buffer
        } catch (Exception e) { // if exception
            Utils.handleException(new Exception("Unable to load texture with " + (resRelative ? ("resource-relative ") :
                            "") + "path '" + path + "' for reason: " + e.getMessage()), "graphics.Texture",
                    "Texture(String)", true); // throw exception if unable to load texture
        }

        // save info, create texture, cleanup
        this.w = w.get(); // save width
        this.h = h.get(); // save height
        this.id = glGenTextures(); // generate texture object

        glBindTexture(GL_TEXTURE_2D, id); // bind new texture object
        glPixelStoref(GL_UNPACK_ALIGNMENT, 1); // tell GL that each component will be one byte in size
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST); // this makes pixels clear and un-blurred
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST); // this makes pixels clear and un-blurred
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, this.w, this.h, 0, GL_RGBA, GL_UNSIGNED_BYTE,
                buf); // generate texture
        glGenerateMipmap(GL_TEXTURE_2D); // generate mip maps
        stbi_image_free(buf); // cleanup by freeing image memory
    }

    /**
     * Construct a texture with the given GL texture ID, width and height.
     * @param id the OpenGL id of the texture
     * @param w the width of the texture in pixels
     * @param h the height of the texture in pixels
     */
    public Texture(int id, int w, int h) {
        this.id = id; // save id as member
        this.w = w; // save width as member
        this.h = h; // save height as member
    }

    /**
     * Turns a texture into an animated one using the given animation properties
     * @param frames how many frames the animation should have
     * @param frameTime how much time (in seconds) each frame should last
     * @param randStart whether the animation should start at a random point or not
     * @return the texture turned into an animated texture. Note that this will still be using the same OpenGL texture
     * id as the non-animated texture before
     */
    public AnimatedTexture animate(int frames, float frameTime, boolean randStart) {
        return new AnimatedTexture(this.id, this.w, this.h, frames, frameTime, randStart);
    }

    /**
     * @return the texture's ID
     */
    public int getID() {
        return this.id;
    }

    /**
     * @return the texture's width
     */
    public int getWidth() {
        return this.w;
    }

    /**
     * @return the texture's height
     */
    public int getHeight() {
        return this.h;
    }

    /**
     * Calculates and returns appropriate model coords for a model to have the correct size to use the texture in its
     * native aspect ratio
     *
     * @param relativeTo what to calculate the model coordinates relative to. For example, if this is set to 32 and the
     *                   texture's width is 64 and the height is 128, the model will be 2 wide and 4 tall. Smaller
     *                   values of relativeTo will lead to larger models
     * @return the model coordinates described above
     */
    public float[] getModelCoords(float relativeTo) {
        float w2 = (this.w / relativeTo / 2); // calculate half width of model
        float h2 = (this.h / relativeTo / 2); // calculate half height of model
        return new float[]{ // create array with correct model coordinates and return it
                -w2, -h2, // bottom left
                -w2, h2, // top left
                w2, h2, // top right
                w2, -h2 // bottom right
        };
    }

    /**
     * Cleans up the texture
     */
    public void cleanup() {
        glDeleteTextures(this.id);
    }
}
