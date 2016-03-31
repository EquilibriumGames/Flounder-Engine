package flounder.devices;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * Manages the creation, updating and destruction of the display, as well as timing and frame times.
 */
public class DeviceDisplay {
	private final GLFWWindowCloseCallback callbackWindowClose;
	private final GLFWWindowFocusCallback callbackWindowFocus;
	private final GLFWWindowPosCallback callbackWindowPos;
	private final GLFWWindowSizeCallback callbackWindowSize;
	private final GLFWFramebufferSizeCallback callbackFramebufferSize;

	private long window;
	private int displayWidth;
	private int displayHeight;
	private String displayTitle;
	private boolean displayVSync;
	private boolean antialiasing;
	private boolean displayFullscreen;
	private int displayPositionX, displayPositionY;
	private boolean displayFocused;
	private boolean closeRequested;

	/**
	 * Creates a new GLFW window.
	 *
	 * @param displayWidth The window width in pixels.
	 * @param displayHeight The window height in pixels.
	 * @param displayTitle The window title.
	 * @param displayVSync If the window will use vSync..
	 * @param antialiasing If OpenGL will use altialiasing.
	 * @param displayFullscreen If the window will start fullscreen.
	 */
	protected DeviceDisplay(final int displayWidth, final int displayHeight, final String displayTitle, final boolean displayVSync, final boolean antialiasing, final boolean displayFullscreen) {
		this.displayWidth = displayWidth;
		this.displayHeight = displayHeight;
		this.displayTitle = displayTitle;
		this.displayVSync = displayVSync;
		this.antialiasing = antialiasing;
		this.displayFullscreen = displayFullscreen;
		displayFocused = true;
		closeRequested = false;

		// Initialize the library.
		if (glfwInit() != GLFW_TRUE) {
			System.out.println("Could not init GLFW!");
			System.exit(-1);
		}

		// Configures the window.
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
		glfwWindowHint(GLFW_VISIBLE, GL_FALSE); // The window will stay hidden until after creation.
		glfwWindowHint(GLFW_RESIZABLE, displayFullscreen ? GL_FALSE : GL_TRUE); // The window will be resizable depending on if its createDisplay.
		// glfwWindowHint(GLFW_SAMPLES, 8);
		// glfwWindowHint(GLFW_REFRESH_RATE, 60);

		// Gets the resolution of the primary monitor.
		final GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

		// Create a windowed mode window and its OpenGL context.
		window = glfwCreateWindow(displayWidth, displayHeight, displayTitle, NULL, NULL);

		// Sets the display to fullscreen or windowed.
		setDisplayFullscreen(displayFullscreen);

		// Gets any window errors.
		if (window == NULL) {
			System.out.println("Could not create the window!");
			glfwTerminate();
			System.exit(-1);
		}

		// Creates the OpenGL context.
		glfwMakeContextCurrent(window);

		// LWJGL will detect the context that is current in the current thread, creates the GLCapabilities instance and makes the OpenGL bindings available for use.
		GL.createCapabilities();

		// Gets any OpenGL errors.
		final long glError = glGetError();

		if (glError != GL_NO_ERROR) {
			System.out.println("OpenGL Error: " + glError);
			glfwDestroyWindow(window);
			glfwTerminate();
			System.exit(-1);
		}

		// Enables VSync if requested.
		setDisplayVSync(displayVSync);

		// Centers the window position.
		glfwSetWindowPos(window, (displayPositionX = (vidmode.width() - displayWidth) / 2), (displayPositionY = (vidmode.height() - displayHeight) / 2));

		// Shows the OpenGl window.
		glfwShowWindow(window);

		// Sets the displays callbacks.
		glfwSetWindowCloseCallback(window, callbackWindowClose = new GLFWWindowCloseCallback() {
			@Override
			public void invoke(long window) {
				closeRequested = true;
			}
		});

		glfwSetWindowFocusCallback(window, callbackWindowFocus = new GLFWWindowFocusCallback() {
			@Override
			public void invoke(long window, int focused) {
				displayFocused = focused == GL_TRUE;
			}
		});

		glfwSetWindowPosCallback(window, callbackWindowPos = new GLFWWindowPosCallback() {
			@Override
			public void invoke(long window, int xpos, int ypos) {
				displayPositionX = xpos;
				displayPositionY = ypos;
			}
		});

		glfwSetWindowSizeCallback(window, callbackWindowSize = new GLFWWindowSizeCallback() {
			@Override
			public void invoke(long window, int width, int height) {
				DeviceDisplay.this.displayWidth = width;
				DeviceDisplay.this.displayHeight = height;
			}
		});

		glfwSetFramebufferSizeCallback(window, callbackFramebufferSize = new GLFWFramebufferSizeCallback() {
			@Override
			public void invoke(long window, int width, int height) {
				glViewport(0, 0, width, height);
			}
		});
	}

	/**
	 * Polls for window events. The key callback will only be invoked during this call.
	 */
	protected void pollEvents() {
		glfwPollEvents();
	}

	/**
	 * Updates the display image by swaping the colour buffers.
	 */
	protected void swapBuffers() {
		glfwSwapBuffers(window);
	}

	/**
	 * @return The current GLFW window.
	 */
	public long getWindow() {
		return window;
	}

	/**
	 * @return The width of the display in pixels.
	 */
	public int getDisplayWidth() {
		return displayWidth;
	}

	/**
	 * @return The height of the display in pixels.
	 */
	public int getDisplayHeight() {
		return displayHeight;
	}

	/**
	 * @return The aspect ratio between the displays width and height.
	 */
	public float getDisplayAspectRatio() {
		return ((float) displayWidth) / ((float) displayHeight);
	}

	/**
	 * @return The window's title.
	 */
	public String getDisplayTitle() {
		return displayTitle;
	}

	/**
	 * @return If the display is using vSync.
	 */
	public boolean isDisplayVSync() {
		return displayVSync;
	}

	/**
	 * Set the display to use VSync or not.
	 *
	 * @param displayVSync Weather or not to use vSync.
	 */
	public void setDisplayVSync(final boolean displayVSync) {
		this.displayVSync = displayVSync;
		glfwSwapInterval(this.displayVSync ? 1 : 0);
	}

	/**
	 * @return If the display requests antialiased images.
	 */
	public boolean isAntialiasing() {
		return antialiasing;
	}

	/**
	 * Requests the display to antialias.
	 *
	 * @param antialiasing If the display should antialias.
	 */
	public void setAntialiasing(final boolean antialiasing) {
		this.antialiasing = antialiasing;
	}

	/**
	 * @return Weather the display is fullscreen or not.
	 */
	public boolean isDisplayFullscreen() {
		return displayFullscreen;
	}

	/**
	 * Set the display to fullscreen or windowed.
	 *
	 * @param displayFullscreen Weather or not to be fullscreen.
	 */
	public void setDisplayFullscreen(final boolean displayFullscreen) {
		this.displayFullscreen = displayFullscreen;
		// TODO: Put display in fullscreen!
		glfwWindowHint(GLFW_RESIZABLE, this.displayFullscreen ? GL_FALSE : GL_TRUE);
	}

	/**
	 * @return The x pos of the display in pixels.
	 */
	public int getXPos() {
		return displayPositionX;
	}

	/**
	 * @return The y pos of the display in pixels.
	 */
	public int getYPos() {
		return displayPositionY;
	}

	/**
	 * @return If the GLFW display is selected.
	 */
	public boolean isFocused() {
		return displayFocused;
	}

	/**
	 * @return If the GLFW display is open or if close has not been requested.
	 */
	public boolean isOpen() {
		return !closeRequested && glfwWindowShouldClose(window) != GL_TRUE;
	}

	/**
	 * Indicates that the game has been requested to close. At the end of the current frame the main game loop will exit.
	 */
	public void requestClose() {
		closeRequested = true;
	}

	/**
	 * @return The current GLFW time time in seconds (by running glfwGetTime() * 1000.0f).
	 */
	public float getTime() {
		return (float) (glfwGetTime() * 1000.0f);
	}

	/**
	 * Closes the GLFW display, do not renderObjects after calling this.
	 */
	protected void dispose() {
		callbackWindowClose.release();
		callbackWindowFocus.release();
		callbackWindowPos.release();
		callbackWindowSize.release();
		callbackFramebufferSize.release();
		glfwDestroyWindow(window);
		glfwTerminate();
	}
}
