package md.krab._3_hot_reload;

import org.lwjgl.opengl.GL46;

public class Framebuffer {
    int gl_id;
    public Texture texture;

    // Don't call this constructor, this just creates the default framebuffer with gl_id of 0.
    public Framebuffer() {
        gl_id = 0;
    }

    public Framebuffer(Texture texture) {
        gl_id = GL46.glCreateFramebuffers(); // creates one fb, but it's plural Framebuffers lol!

        this.texture = texture;

        GL46.glNamedFramebufferTexture(
                this.gl_id,
                GL46.GL_COLOR_ATTACHMENT0, // bind image to first "target" of the framebuffer. You can render to multiple images at once, each one is a target.
                texture.gl_handle,
                0 // mipmap level
        );
        GL46.glNamedFramebufferDrawBuffers(this.gl_id, GL46.GL_COLOR_ATTACHMENT0); // enable drawing to image at target 0

        int fbStatus = GL46.glCheckNamedFramebufferStatus(this.gl_id, GL46.GL_FRAMEBUFFER);
        if (fbStatus != GL46.GL_FRAMEBUFFER_COMPLETE) {
            System.out.println("ERROR");
            // error
        }
    }

    public void bind() {
        GL46.glBindFramebuffer(GL46.GL_FRAMEBUFFER, gl_id);
    }

    public void clear(float r, float g, float b, float a) {
        GL46.glClearNamedFramebufferfv(
                this.gl_id,
                GL46.GL_COLOR,
                0, // clear first image target
                new float[]{r, g, b, a}
        );
    }

    public void copy_to_other_fb(
            Framebuffer other,
            int bitmask
    ) {
        int resx = this.texture.resx;
        int resy = this.texture.resy;
        GL46.glBlitNamedFramebuffer(
                this.gl_id, other.gl_id,
                0, 0, resx, resy,
                0, 0, resx, resy,
                bitmask, GL46.GL_NEAREST
        );
    }
}
