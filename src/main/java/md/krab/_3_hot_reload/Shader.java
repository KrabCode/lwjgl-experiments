package md.krab._3_hot_reload;

import org.lwjgl.opengl.GL46;

public class Shader {
	int gl_id = 0;
	int shader_type;
	String error = "";
	boolean compilation_success = false;
	String code;
	public Shader(String code, int shader_type){
		this.shader_type = shader_type;
		this.code = code;
		this.compile();
	}
	public void compile(){
		// delete prev iteration of this shader, if doing live reloading
		if (gl_id > 0) {
			GL46.glDeleteShader(gl_id);
		}
		gl_id = GL46.glCreateShader(this.shader_type);
		GL46.glShaderSource(gl_id, code);
		GL46.glCompileShader(gl_id);
		if (GL46.glGetShaderi(gl_id, GL46.GL_COMPILE_STATUS) == 0) {
			error = GL46.glGetShaderInfoLog(gl_id, 1024);
			System.out.println(error);
			GL46.glDeleteShader(gl_id);
			compilation_success = false;
		} else {
			error = null;
			compilation_success = true;
		}
	}
}
