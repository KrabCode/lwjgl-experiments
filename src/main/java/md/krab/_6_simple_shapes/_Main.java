package md.krab._6_simple_shapes;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL45;
import org.lwjgl.opengl.GL46;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL46.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Graciously written by <a href="https://github.com/wrightwriter">wrighter</a>
 * as a tutorial very different from every GL tutorial, because they suck
 * Edited by Krab to learn.
 */
@SuppressWarnings("DuplicatedCode")
public class _Main {

    private static final float TAU = 6.283185f;
    // The window handle
    private long window;
    float time;
    int frameCount;
    int width = 800;
    int height = 800;

    public void run() throws IOException {
        init();
        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }


    private void init() {
        System.out.println("Started!");
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable
        glfwWindowHint(GLFW_FLOATING, GLFW_TRUE);
        glfwWindowHint(GLFW_AUTO_ICONIFY, GLFW_FALSE);
        glfwWindowHint(GLFW_REFRESH_RATE, 144);
        glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);


        String title = getClass().getPackageName();
        boolean fullscreen = false;
        //noinspection ConstantValue
        window = glfwCreateWindow(width, height, title, fullscreen ? glfwGetMonitors().get(1) : NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });


        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            if (vidmode != null) {
                glfwSetWindowPos(
                        window,
                        vidmode.width() - pWidth.get(0),
                        0
                );
            }

        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);

        GL.createCapabilities();

        glClearColor(0.4f, 0.0f, 0.0f, 0.0f);

        int vao = glGenVertexArrays();
        glBindVertexArray(vao);

    }

    private void loop() {
        Framebuffer framebuffer_out = new Framebuffer();
        Framebuffer framebuffer = new Framebuffer(new Texture(width, height));

        // OpenGL coordinates go from -1 to 1 on each axis.
        Buffer vertex_buffer = new Buffer(new float[]{
                -1.0f, -1.0f,
                1.0f, -1.0f,
                -1.0f, 1.0f,

                1.0f, 1.0f,
                1.0f, -1.0f,
                -1.0f, 1.0f,
        });

        // Disable removal of clockwise triangles (culling)
        glDisable(GL_CULL_FACE);

        // Draw to this resolution.
        glViewport(0, 0, width, height);

        int swap_index = 0;

        HashMap<Integer, float[]> positions = new HashMap<>();

        while (!glfwWindowShouldClose(window)) {
            framebuffer.clear(0.1f,0.1f,0.1f, 0);
            // Binding means subsequent drawing will happen to this framebuffer.
            framebuffer.bind();

    int count = 10000;
    for(int i = 0; i < count; i++){
        glPushMatrix();
        if(!positions.containsKey(i)){
            positions.put(i, new float[]{(float) (-1+2*Math.random()), (float)(-1+2*Math.random())} );
        }
        float[] pos = positions.get(i);
        glTranslatef(pos[0], pos[1], 0);
        glBegin(GL_TRIANGLE_FAN);
            glColor3f(1.f, 1.f, 1.f);
            glVertex2d(0,0);
            int vertCount = 4;
            float radius = 0.01f;
            for (int j = 0; j <= vertCount; j++) {
                float angle = (j / (float) vertCount) * TAU;
                glVertex2d(radius*Math.cos(angle), radius*Math.sin(angle));
            }
        glEnd();
        glPopMatrix();
    }

            // copy the result to screen
            framebuffer.copy_to_other_fb(framebuffer_out, GL_COLOR_BUFFER_BIT);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        try {
            new _Main().run();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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
        ) {
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
            GL46.glTextureParameteri(gl_handle, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
            GL46.glTextureParameteri(gl_handle, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);

            // Allocate tex
            GL46.glTextureStorage2D(
                    gl_handle,
                    1, // mipmap levels
                    internal_format,
                    resx,
                    resy
            );
        }

        public void bindToUnit(int unit) {
            GL46.glBindTextureUnit(unit, this.gl_handle);
        }
    }


    public class ShaderProgram {
        int gl_id = 0;

        Path vertPath;
        Path fragPath;
        FileTime fragLastModified;
        FileTime vertLastModified;

        Shader vert_shader;
        Shader frag_shader;
        String error;

        public ShaderProgram(Path vertPath, Path fragPath) {
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
            vert_shader = new Shader(vertString, GL46.GL_VERTEX_SHADER);
            frag_shader = new Shader(fragString, GL46.GL_FRAGMENT_SHADER);
            link();
        }

        public void use() {
            tryLiveReload();
            GL46.glUseProgram(gl_id);
        }

        public void link() {
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
                if (fragLastModifiedCurrent.compareTo(fragLastModified) > 0) {
                    fragLastModified = fragLastModifiedCurrent;
                    String newFragBody = Files.readString(fragPath);
                    frag_shader = new Shader(newFragBody, GL46.GL_FRAGMENT_SHADER);
                    frag_shader.compile();
                    System.out.println("recompiled frag shader " + fragPath);
                    eitherChanged = true;
                }
                FileTime vertLastModifiedCurrent = Files.getLastModifiedTime(vertPath);
                if (vertLastModifiedCurrent.compareTo(vertLastModified) > 0) {
                    vertLastModified = vertLastModifiedCurrent;
                    String newVertBody = Files.readString(vertPath);
                    vert_shader = new Shader(newVertBody, GL46.GL_FRAGMENT_SHADER);
                    vert_shader.compile();
                    System.out.println("recompiled vert shader " + vertPath);
                    eitherChanged = true;
                }
                if (eitherChanged) {
                    link();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public class Shader {
        int gl_id = 0;
        int shader_type;
        String error = "";
        boolean compilation_success = false;
        String code;

        public Shader(String code, int shader_type) {
            this.shader_type = shader_type;
            this.code = code;
            this.compile();
        }

        public void compile() {
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

}
