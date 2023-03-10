package snake2d;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

class GraphicsCardGetter {

	private String error = null;
	private String version = null;
	
	public GraphicsCardGetter() {
		try {
			// Setup an error callback. The default implementation
			// will print the error message in System.err.
			GLFWErrorCallback.createPrint(System.out).set();

			// Initialize GLFW. Most GLFW functions will not work before doing this.
			if (!glfwInit())
				throw new IllegalStateException("Unable to initialize GLFW");

			// Configure GLFW
			glfwDefaultWindowHints(); // optional, the current window hints are already the default
			glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation

			// Create the window
			long window = NULL;
			window = glfwCreateWindow(300, 300, "test", NULL, NULL);
			
			if (window == NULL)
				throw new IllegalStateException("No window returned");

			// Make the OpenGL context current
			glfwMakeContextCurrent(window);

			GL.createCapabilities();
			version = glGetString(GL_VENDOR) + ", " + glGetString(GL_RENDERER) + System.lineSeparator()
					+ "openGL max version: " + glGetString(GL_VERSION);
			glfwTerminate();
			glfwSetErrorCallback(null).free();
		}catch(Exception e) {
			e.printStackTrace(System.out);
			error = e.getMessage();
		}
	}
	
	public String version() {
		return version;
	}
	
	public String error() {
		return error;
	}

}
