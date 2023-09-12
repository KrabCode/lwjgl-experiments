package md.krab._3_hot_reload;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;

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

    // The window handle
    private long window;
    float time;
    FileTime fragLastModified;

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
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable
        glfwWindowHint(GLFW_FLOATING, GLFW_TRUE);
//        glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);


        String title = getClass().getSimpleName();
        // Create the window
        window = glfwCreateWindow(600, 600, title, NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });

        // Get the thread stack and push a new frame
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            if(vidmode != null){
                glfwSetWindowPos(
                        window,
                        vidmode.width() - pWidth.get(0) - 40,
                        40
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

    private void loop() throws IOException {
        Texture tex0 = new Texture(600,600);
        Texture tex1 = new Texture(600,600);

        Framebuffer default_framebuffer = new Framebuffer();
        Framebuffer custom_framebuffer_0 = new Framebuffer(tex0);
        Framebuffer custom_framebuffer_1 = new Framebuffer(tex1);
        Framebuffer[] custom_framebuffers = new Framebuffer[]{custom_framebuffer_0, custom_framebuffer_1};

        // OpenGL coordinates go from -1 to 1 on each axis.
        Buffer vertex_buffer = new Buffer(new float[]{
                -1.0f, -1.0f,
                 1.0f, -1.0f,
                -1.0f,  1.0f,

                 1.0f, 1.0f,
                 1.0f, -1.0f,
                -1.0f,  1.0f,
        });
        // Bind as "SSBO", that means we can read it from the shader.
        vertex_buffer.bind_as_SSBO(0);

        Path fragPath = Paths.get("src/main/java/md/krab/_3_hot_reload/frag.glsl");
        Path vertPath = Paths.get("src/main/java/md/krab/_3_hot_reload/vert.glsl");
        String vertBody;
        String fragBody;

        fragLastModified = Files.getLastModifiedTime(fragPath);
        fragBody = Files.readString(fragPath);
        vertBody = Files.readString(vertPath);


        // The "SSBO"
        ShaderProgram shader_program = new ShaderProgram(vertBody, fragBody);

        // Disable removal of clockwise triangles (culling)
        glDisable(GL_CULL_FACE);

        // Draw to this resolution.
        glViewport(0,0, 600, 600);

        int swapping_index = 0;

        while ( !glfwWindowShouldClose(window) ) {
            // swap the two buffers to achieve a backbuffer uniform inside the frag shader
            swapping_index++;
            swapping_index %= 2;
            Framebuffer fb_writeable = custom_framebuffers[0];
            Framebuffer fb_readable  = custom_framebuffers[1];
            if(swapping_index == 1){
                fb_writeable = custom_framebuffers[1];
                fb_readable  = custom_framebuffers[0];
            }

            fb_writeable.clear(0.4f,0,0,0);
            // Binding means subsequent drawing will happen to this framebuffer.
            fb_writeable.bind();

            // try hot reload if file has changed since last time we've compiled it
            FileTime fragLastModifiedCurrent;
            fragLastModifiedCurrent = Files.getLastModifiedTime(fragPath);
            if(fragLastModifiedCurrent.compareTo(fragLastModified) > 0){
                fragLastModified = fragLastModifiedCurrent;
                shader_program.setFragString(Files.readString(fragPath));
                shader_program.frag_shader.compile();
                shader_program.link();
                print("recompiled and relinked shader");
            }

            // apply shader program
            shader_program.use();

            // set uniform
            int program_id = shader_program.gl_id;
            time += 0.01f;
            int time_loc = glGetUniformLocation(program_id, "time");
            if(time_loc != -1 ){
                glProgramUniform1f(program_id, time_loc, time);
            }
            int res_loc = glGetUniformLocation(program_id, "res");
            if(res_loc!= -1) glProgramUniform2f(program_id, res_loc, 600f, 600f);

            fb_readable.texture.bindToUnit(0);
            int bb_loc = glGetUniformLocation(program_id, "bb");
            glProgramUniform1i(program_id, bb_loc, 0);

            // Issue 3 draw calls. A draw call is an invocation of a vertex shader. 3 of those define a trongle.
            glDrawArrays(GL_TRIANGLES,0,6);

            fb_writeable.copy_to_other_fb(default_framebuffer, GL_COLOR_BUFFER_BIT);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void print(Object any) {
        System.out.println(any);
    }

    public static void main(String[] args) {
        try {
            new _Main().run();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }





}
