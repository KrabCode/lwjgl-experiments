package md.krab._4_reaction_diffusion;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.windows.INPUT;

import java.io.IOException;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
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
    FileTime fragOutLastModified;
    int width = 1200;
    int height = 600;


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

    private void loop() throws IOException {

        Framebuffer default_framebuffer = new Framebuffer();
        Framebuffer custom_framebuffer_0 = new Framebuffer(new Texture(width, height));
        Framebuffer custom_framebuffer_1 = new Framebuffer(new Texture(width, height));
        Framebuffer[] swappable_framebuffers = new Framebuffer[]{custom_framebuffer_0, custom_framebuffer_1};

        Framebuffer custom_framebuffer_out = new Framebuffer(new Texture(width, height));

        // OpenGL coordinates go from -1 to 1 on each axis.
        Buffer vertex_buffer = new Buffer(new float[]{
                -1.0f, -1.0f,
                1.0f, -1.0f,
                -1.0f, 1.0f,

                1.0f, 1.0f,
                1.0f, -1.0f,
                -1.0f, 1.0f,
        });
        // Bind as "SSBO", that means we can read it from the shader.
        vertex_buffer.bind_as_SSBO(0);

        Path fragOutPath = Paths.get("src/main/java/md/krab/_4_reaction_diffusion/frag_out.glsl");
        Path fragPath = Paths.get("src/main/java/md/krab/_4_reaction_diffusion/frag.glsl");
        Path vertPath = Paths.get("src/main/java/md/krab/_4_reaction_diffusion/vert.glsl");

        // The "SSBO"
        ShaderProgram shader_program_swap = new ShaderProgram(vertPath, fragPath);
        ShaderProgram shader_program_out = new ShaderProgram(vertPath, fragOutPath);

        // Disable removal of clockwise triangles (culling)
        glDisable(GL_CULL_FACE);

        // Draw to this resolution.
        glViewport(0, 0, width, height);

        int swap_index = 0;

        while (!glfwWindowShouldClose(window)) {
            // swap the two buffers to achieve a backbuffer uniform inside the frag shader
            swap_index++;
            swap_index %= 2;
            Framebuffer fb_writeable = swappable_framebuffers[0];
            Framebuffer fb_readable = swappable_framebuffers[1];
            if (swap_index == 1) {
                fb_writeable = swappable_framebuffers[1];
                fb_readable = swappable_framebuffers[0];
            }

            fb_writeable.clear(0.4f, 0, 0, 0);
            // Binding means subsequent drawing will happen to this framebuffer.
            fb_writeable.bind();

            // try hot reload if file has changed since last time we've compiled it
            // apply shader program
            shader_program_swap.use();

            // set uniform
            int program_id = shader_program_swap.gl_id;
            time += 0.01f;
            int time_loc = glGetUniformLocation(program_id, "time");
            if (time_loc != -1) {
                glProgramUniform1f(program_id, time_loc, time);
            }

            int res_loc = glGetUniformLocation(program_id, "resolution");
            glProgramUniform2f(program_id, res_loc, (float) width, (float) height);

            DoubleBuffer xBuffer = BufferUtils.createDoubleBuffer(1);
            DoubleBuffer yBuffer = BufferUtils.createDoubleBuffer(1);
            glfwGetCursorPos(window, xBuffer, yBuffer);
            float x = (float) xBuffer.get(0);
            float y = (float) yBuffer.get(0);
            int mouse_loc = glGetUniformLocation(program_id, "mouse");
            glProgramUniform2f(program_id, mouse_loc, x, y);

            fb_readable.texture.bindToUnit(0);
            int bb_loc = glGetUniformLocation(program_id, "bb");
            glProgramUniform1i(program_id, bb_loc, 0);

            // draw quad to the currently bound swappable framebuffer
            glDrawArrays(GL_TRIANGLES, 0, 6);

            // prepare an intermediary output framebuffer for coloring the final output with the frag_out shader
            custom_framebuffer_out.bind();
            shader_program_out.use();

            // bind the result of the previous draw to a slot used as a uniform "img" by the frag_out shader
            fb_writeable.texture.bindToUnit(0);
            int img_loc = glGetUniformLocation(shader_program_out.gl_id, "img");
            glProgramUniform1i(shader_program_out.gl_id, img_loc, 0);

            // draw the final result to the intermediary output framebuffer using the frag_out shader
            glDrawArrays(GL_TRIANGLES, 0, 6);

            // copy the result to screen
            custom_framebuffer_out.copy_to_other_fb(default_framebuffer, GL_COLOR_BUFFER_BIT);

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
