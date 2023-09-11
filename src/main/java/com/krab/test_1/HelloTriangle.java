package com.krab.test_1;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL46;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Graciously written by <a href="https://github.com/wrightwriter">wrighter</a>
 * as a tutorial very different from every GL tutorial, because they suck
 */
public class HelloTriangle {

    // The window handle
    private long window;

    public void run() {
        System.out.println("Hello Triangle!" + Version.getVersion());

        init();
        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }


    private void loop() {
        Texture tex = new Texture(400,400);

        Framebuffer default_framebuffer = new Framebuffer();
        Framebuffer custom_framebuffer = new Framebuffer(tex);

        // Trongle goes from -1 to 1, because OpenGL coordinates go from -1 to 1 on each axis.
        Buffer trongle_buffer = new Buffer(new float[]{
                -1.0f,-1.0f,
                1.0f,-1.0f,
                0.0f,1.0f
        });
        // Bind as "SSBO", that means we can read it from the shader.
        trongle_buffer.bind_as_SSBO(0);

        ShaderProgram trongle_shader_prog = new ShaderProgram(
                // vert
                "#version 460\n" +
                        "\n" +
                        "layout (std430, binding = 0)  buffer ssbo {\n" + // The "SSBO"
                        "    vec2[] verts;\n" +
                        "};\n" +
                        "out vec2 uv;\n" +
                        "\n" +
                        "void main(){\n" +
                        "gl_Position = vec4(verts[gl_VertexID],0.,1.);\n" +
                        "uv = gl_Position.xy;\n" +
                        "}\n",
                // frag
                "#version 460\n" +
                        "in vec2 uv;\n" +
                        "out vec4 fragColor;\n" +
                        "\n" +
                        "void main(){\n" +
                        "fragColor = vec4(uv.xyx,1.0);\n" +
                        "}\n"
        );

        // Disable removal of clockwise triangles (culling)
        GL46.glDisable(GL_CULL_FACE);

        // Draw to this resolution.
        GL46.glViewport(0,0, 400, 400);

        while ( !glfwWindowShouldClose(window) ) {
            custom_framebuffer.clear(0.4f,0,0,0);
            // Binding means subsequent drawing will happen to this framebuffer.
            custom_framebuffer.bind();

            trongle_shader_prog.use();
            // Issue 3 draw calls. A draw call is an invocation of a vertex shader. 3 of those define a trongle.
            GL46.glDrawArrays(GL46.GL_TRIANGLES,0,3);

            custom_framebuffer.copy_to_other_fb(default_framebuffer, GL46.GL_COLOR_BUFFER_BIT);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void init() {
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

        // Create the window
        window = glfwCreateWindow(400, 400, "Hello World!", NULL, NULL);
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

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);

        GL.createCapabilities();

        glClearColor(1.0f, 0.0f, 0.0f, 0.0f);

        int vao = GL46.glGenVertexArrays();
        GL46.glBindVertexArray(vao);

    }

    public static void main(String[] args) {
        new HelloTriangle().run();
    }

}
