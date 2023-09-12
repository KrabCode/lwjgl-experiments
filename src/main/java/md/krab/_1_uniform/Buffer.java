package md.krab._1_uniform;

import org.lwjgl.opengl.GL46;

public class Buffer {
    int gl_id;
    float[] data;

    public Buffer(float[] data) {
        this.data = data;
        create();
    }

    public void bind_as_SSBO(int bind_idx) {
        GL46.glBindBufferBase(GL46.GL_SHADER_STORAGE_BUFFER, bind_idx, this.gl_id);
    }

    public void create() {
        gl_id = GL46.glCreateBuffers();
        // GL_DYNAMIC_STORAGE_BIT literally doesn't matter nor do anything that matters.
        GL46.glNamedBufferStorage(gl_id, this.data, GL46.GL_DYNAMIC_STORAGE_BIT);
    }

    public void clear() {
        // Dumbass GL makes you write out "formats" when clearing a buffer.
        // A buffer is just some numbers, either int, uint or float.
        // whyyyyyyyy
        GL46.glClearNamedBufferData(
                gl_id,
                GL46.GL_R32F,
                GL46.GL_RED,
                GL46.GL_FLOAT,
                new float[]{0.0f}
        );
    }

}
