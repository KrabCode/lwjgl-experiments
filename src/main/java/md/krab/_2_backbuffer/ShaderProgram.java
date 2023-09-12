package md.krab._2_backbuffer;
import org.lwjgl.opengl.GL46;

public class ShaderProgram {
	int gl_id = 0;
	Shader vert_shader;
	Shader frag_shader;
	String error;
	public ShaderProgram(String vert_string, String frag_string){
		vert_shader = new Shader(vert_string,GL46.GL_VERTEX_SHADER);
		frag_shader = new Shader(frag_string,GL46.GL_FRAGMENT_SHADER);
		link();
	}
	public void use(){
		GL46.glUseProgram(gl_id);
	}
	public boolean link(){
		boolean success = false;
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
		return success;
	}
}
