package md.krab._3_live_frag_reload;
import org.lwjgl.opengl.GL46;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

public class ShaderProgram {
	int gl_id = 0;

	Path vertPath;
	Path fragPath;
	FileTime fragLastModified;
	FileTime vertLastModified;

	Shader vert_shader;
	Shader frag_shader;
	String error;

	public ShaderProgram(Path vertPath, Path fragPath){
		String vertString;
		String fragString;
		this.vertPath = vertPath;
		this.fragPath = fragPath;
		try {
			vertString = Files.readString(vertPath);
			fragString = Files.readString(fragPath);
			vertLastModified = Files.getLastModifiedTime(vertPath);
			fragLastModified = Files.getLastModifiedTime(fragPath);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		vert_shader = new Shader(vertString,GL46.GL_VERTEX_SHADER);
		frag_shader = new Shader(fragString,GL46.GL_FRAGMENT_SHADER);
		link();
	}

	public void use(){
		tryLiveReload();
		GL46.glUseProgram(gl_id);
	}

	public void link(){
		boolean success;
		int new_gl_id = GL46.glCreateProgram();
		GL46.glAttachShader(new_gl_id, vert_shader.gl_id);
		GL46.glAttachShader(new_gl_id, frag_shader.gl_id);

		GL46.glLinkProgram(new_gl_id);
		if (GL46.glGetProgrami(new_gl_id, GL46.GL_LINK_STATUS) == 0) {
			success = false;
			error = GL46.glGetProgramInfoLog(new_gl_id, 1024);
			System.err.println(error);
		} else {
			GL46.glValidateProgram(new_gl_id);
			success = true;
			if (GL46.glGetProgrami(new_gl_id, GL46.GL_VALIDATE_STATUS) == 0) {
				error = GL46.glGetProgramInfoLog(new_gl_id, 1024);
				System.err.println("Warning validating Shader code: \n " + error + "\n");
			}
		}
		if (!success) {
			// Delete new program.
			if (new_gl_id > 0) GL46.glDeleteProgram(new_gl_id);
		} else {
			// Replace old pid with new pid.
			GL46.glDeleteProgram(gl_id);
			gl_id = new_gl_id;
			error = null;
			// Delete new shaders.
			GL46.glDetachShader(gl_id, vert_shader.gl_id);
			GL46.glDetachShader(gl_id, frag_shader.gl_id);
		}
	}

	private void tryLiveReload() {
		boolean eitherChanged = false;
		try {
			FileTime fragLastModifiedCurrent = Files.getLastModifiedTime(fragPath);
			if(fragLastModifiedCurrent.compareTo(fragLastModified) > 0){
				fragLastModified = fragLastModifiedCurrent;
				String newFragBody = Files.readString(fragPath);
				frag_shader = new Shader(newFragBody, GL46.GL_FRAGMENT_SHADER);
				frag_shader.compile();
				System.out.println("recompiled frag shader " + fragPath);
				eitherChanged = true;
			}
			FileTime vertLastModifiedCurrent = Files.getLastModifiedTime(vertPath);
			if(vertLastModifiedCurrent.compareTo(vertLastModified) > 0){
				vertLastModified = vertLastModifiedCurrent;
				String newVertBody = Files.readString(fragPath);
				vert_shader = new Shader(newVertBody, GL46.GL_FRAGMENT_SHADER);
				vert_shader.compile();
				System.out.println("recompiled vert shader " + vertPath);
				eitherChanged = true;
			}
			if(eitherChanged){
				link();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
