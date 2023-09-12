package md.krab._2_backbuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL45;
import org.lwjgl.opengl.GL46;

public class Texture {
	int gl_handle;

	int internal_format;
	int format;
	int type;

	int resx;
	int resy;
	public Texture(
			int resx,
			int resy
	){
		// GL is retarded so you have a separate format, internal format and type.
		this.internal_format = GL46.GL_RGBA32F;
		this.format = GL46.GL_RGBA;
		this.type = GL46.GL_FLOAT;
		this.resx = resx;
		this.resy = resy;

		this.gl_handle = GL45.glCreateTextures(GL46.GL_TEXTURE_2D);

		// How it interpolates and wraps pixels when you sample from a shader.
		GL46.glTextureParameteri(gl_handle, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL46.glTextureParameteri(gl_handle, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL46.glTextureParameteri(gl_handle, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		GL46.glTextureParameteri(gl_handle, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

		// Allocate tex
		GL46.glTextureStorage2D(
				gl_handle,
				1, // mipmap levels
				internal_format,
				resx,
				resy
        );
	}
}