package snake2d;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.Configuration;

import snake2d.Displays.DisplayMode;
import snake2d.Errors.GameError;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.file.FileManager;
import snake2d.util.file.SnakeImage;
import snake2d.util.process.Proccesser;

/**
 * The magic entrance to the game. The mother of classes. Sets up the display,
 * gl-contex, input and renderer
 * 
 * @author mail__000
 *
 */
public class GraphicContext {

	public final int nativeWidth;
	public final int nativeHeight;
	public final int displayWidth;
	public final int displayHeight;
	public final COORDINATE blitArea;
	private boolean windowIsFocused = true;
	final int refreshRate;

	private final GlHelper gl;
	private final long window;
	final Renderer renderer;
	final String screenShotPath;
	static boolean diagnosing = false;
	private static int diagnoseTimer = 0;
	private final boolean debugAll;

	GraphicContext(SETTINGS sett) {

		debugAll = sett.debugMode();
		Configuration.DEBUG.set(debugAll);
		Configuration.DEBUG_STREAM.set(System.out);
		Configuration.DEBUG_MEMORY_ALLOCATOR.set(debugAll);
		Configuration.DEBUG_STACK.set(debugAll);

		Error error = new Error();

		{
			if (sett.getScreenshotFolder() != null)
				screenShotPath = sett.getScreenshotFolder();
			else {
				File f = new File("screenshots");
				if (f.exists() && !f.isDirectory()) {
					f.delete();
				}
				if (!f.exists())
					f.mkdirs();
				screenShotPath = f.getAbsolutePath() + File.separator;
			}
		}

		if (sett.getPointSize() != 1 && sett.getPointSize() % 2 != 0)
			throw new RuntimeException("pointsize must be a power of two!");

		if (debugAll)
			GLFWErrorCallback.createPrint(System.out).set();

		if (!glfwInit())
			throw new IllegalStateException("Unable to initialize GLFW");

		// window hints
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_FOCUSED, GLFW_TRUE);
		glfwWindowHint(GLFW_AUTO_ICONIFY, GLFW_FALSE);
		glfwWindowHint(GLFW_FLOATING, GLFW_FALSE);

		// FB hints
		glfwWindowHint(GLFW_RED_BITS, 8);
		glfwWindowHint(GLFW_GREEN_BITS, 8);
		glfwWindowHint(GLFW_BLUE_BITS, 8);
		glfwWindowHint(GLFW_ALPHA_BITS, 8);
		glfwWindowHint(GLFW_DEPTH_BITS, 0);
		glfwWindowHint(GLFW_STENCIL_BITS, 0);

		// other hints
		glfwWindowHint(GLFW_SAMPLES, 0);
		glfwWindowHint(GLFW_REFRESH_RATE, GLFW_DONT_CARE);
		glfwWindowHint(GLFW_STEREO, GLFW_FALSE);
		glfwWindowHint(GLFW_SRGB_CAPABLE, GLFW_FALSE);
		glfwWindowHint(GLFW_DOUBLEBUFFER, GLFW_TRUE);
		glfwWindowHint(GLFW_CLIENT_API, GLFW_OPENGL_API);

		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		glfwWindowHint(GLFW_CONTEXT_ROBUSTNESS, GLFW_NO_ROBUSTNESS);
		glfwWindowHint(GLFW_CONTEXT_RELEASE_BEHAVIOR, GLFW_ANY_RELEASE_BEHAVIOR);
		glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL11.GL_TRUE);
		glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, debugAll ? GLFW_TRUE : GLFW_FALSE);

		// if (Platform.get() == Platform.MACOSX) {
		// glfwWindowHint(GLFW_COCOA_RETINA_FRAMEBUFFER, GL11.GL_FALSE);
		// }

		new Displays();

		printSettings(sett);

		Printer.ln("GRAPHICS");

		DisplayMode wanted = sett.display();
		int dispWidth = wanted.width;
		int dispHeight = wanted.height;

		nativeWidth = sett.getNativeWidth();
		nativeHeight = sett.getNativeHeight();

		refreshRate = wanted.refresh;
		glfwWindowHint(GLFW_REFRESH_RATE, refreshRate);

		if (!wanted.fullScreen) {

			if (dispWidth > Displays.current(sett.monitor()).width
					|| dispHeight > Displays.current(sett.monitor()).height) {
				dispWidth = Displays.current(sett.monitor()).width;
				dispHeight = Displays.current(sett.monitor()).height;
			}

			Printer.ln("---created window: " + dispWidth + " " + dispHeight);

		}
		displayWidth = dispWidth;
		displayHeight = dispHeight;

		boolean dec = sett.decoratedWindow();
		dec &= !wanted.fullScreen && displayWidth < Displays.current(sett.monitor()).width
				&& displayHeight < Displays.current(sett.monitor()).height;
		glfwWindowHint(GLFW_DECORATED, dec ? GLFW_TRUE : GLFW_FALSE);

		try {
			Printer.ln("---attempting resolution: " + displayWidth + "x" + dispHeight + " " + wanted.fullScreen + " "
					+ refreshRate + " " + sett.monitor());
			window = glfwCreateWindow(displayWidth, displayHeight, sett.getWindowName(),
					wanted.fullScreen ? Displays.pointer(sett.monitor()) : NULL, NULL);
		} catch (Exception e) {
			e.printStackTrace();
			throw error.get();
		}

		if (window == NULL) {
			throw error.get();
		}

		{
			int[] dx = new int[1];
			int[] dy = new int[1];

			glfwGetMonitorPos(Displays.pointer(sett.monitor()), dx, dy);
			if (!wanted.fullScreen && dec) {
				int x1 = (Displays.current(sett.monitor()).width - displayWidth) / 4;
				int y1 = (Displays.current(sett.monitor()).height - displayHeight) / 4;
				if (x1 < 0)
					x1 = 0;
				if (y1 < 0)
					y1 = 0;

				if (sett.decoratedWindow())
					y1 += 30;
				glfwSetWindowPos(window, x1 + dx[0], y1 + dy[0]);
			} else {
				glfwSetWindowPos(window, dx[0], dy[0]);
			}
		}

		String icons = sett.getIconFolder();

		if (icons != null)
			_IconLoader.setIcon(window, icons);
		else
			Printer.ln("NOTE: no icon-folder specified");

		// Make the OpenGL context current
		try {
			glfwMakeContextCurrent(window);
		} catch (Exception e) {
			e.printStackTrace();
			throw error.get();
		}

		GLFWVidMode current = glfwGetVideoMode(glfwGetPrimaryMonitor());
		Printer.ln("---Setting FPS to " + current.refreshRate());
		int swapInterval = 0;

		if (sett.getVSynchEnabled()) {
			swapInterval = 1;
			int r = refreshRate;
			while (current.refreshRate() >= r * 2) {
				r *= 2;
				swapInterval++;
			}
			if (glfwExtensionSupported("WGL_EXT_swap_control_tear")
					|| glfwExtensionSupported("GLX_EXT_swap_control_tear")) {
				Printer.ln("---'Adaptive' Vsync enabled (" + swapInterval + ")");
				swapInterval *= -1;
			}
		}

		glfwSwapInterval(swapInterval);

		Printer.ln("---created resolution: " + current.width() + "x" + current.height() + ", " + current.refreshRate()
				+ "Hz" + (sett.getVSynchEnabled() ? ", vsync: " + swapInterval : ""));
		Printer.ln("---LWJGL: " + org.lwjgl.Version.getVersion());
		Printer.ln("---GLFW: " + glfwGetVersionString());

		gl = new GlHelper(sett.getNativeWidth(), sett.getNativeHeight(), debugAll);
		if (!GL.getCapabilities().OpenGL33)
			throw error.get();

		if (Proccesser.isMac()) {
			blitArea = new Coo(GlHelper.FBSize());
		} else {
			IntBuffer w = BufferUtils.createIntBuffer(1);
			IntBuffer h = BufferUtils.createIntBuffer(1);
			glfwGetFramebufferSize(window, w, h);
			Coo sc = (new Coo(w.get(), h.get()));
			blitArea = new Coo(sc.x(), sc.y());
		}

		Printer.ln("---BLIT: " + blitArea.x() + "x" + blitArea.y());
		Printer.fin();

		switch (sett.getRenderMode()) {
		case 0:
			renderer = new Renderer(new RenderModeDebug(sett), sett.getPointSize());
			break;
		default:
			renderer = new Renderer(new RenderModeDeffered(sett), sett.getPointSize());
			break;
		}

		glfwFocusWindow(window);

		GlHelper.checkErrors();

	}

	private class Error {

		private String mess;

		public Error() {

			GraphicsCardGetter g = new GraphicsCardGetter();
			mess = "The game failed setting up openGl on your graphics card. This is likeley "
					+ "because your graphics card has no opengl 3.3 support. Some PC's have multiple graphics cards. In this case, try configuring the app to use the other."
					+ "graphics card in graphic card's control panel. (You may need to do this for java as well.) ";

			mess += System.lineSeparator();

			mess += "Current graphics card: ";
			mess += g.version();

			mess += System.lineSeparator();
			mess += System.lineSeparator();

			mess += "If your graphics card does not support opengl 3.3 or higher, please do not report this error.";
			if (g.version() == null)
				throw get();

		}

		Errors.GameError get() {
			return new GameError(mess);
		}

	}

	public String render() {
		return GlHelper.renderer;
	}

	public String renderV() {
		return GlHelper.rendererV;
	}

	private void printSettings(SETTINGS sett) {
		Printer.ln("SETTINGS");
		Printer.ln("Degug: " + sett.debugMode());
		Printer.ln("Native Screen: " + sett.getNativeWidth() + "x" + sett.getNativeHeight());
		Printer.ln("Display: " + sett.display());
		Printer.ln("Full: " + sett.display().fullScreen);
		Printer.ln("Mode: " + sett.getRenderMode());
		Printer.ln("Fit: " + sett.getFitToScreen());
		Printer.ln("Linear: " + sett.getLinearFiltering());
		Printer.ln("VSync: " + sett.getVSynchEnabled());
		Printer.fin();
	}

	void makeVisable() {
		glfwShowWindow(window);
		glfwFocusWindow(window);
	}

	void flushRenderer() {
		renderer.flush();
	}

	boolean swapAndCheckClose() {
		if (debugAll)
			GlHelper.checkErrors();
		int i = glGetInteger(GL_READ_FRAMEBUFFER_BINDING);
		glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
		glfwSwapBuffers(window);
		glBindFramebuffer(GL_READ_FRAMEBUFFER, i);
		windowIsFocused = glfwGetWindowAttrib(window, GLFW_FOCUSED) == 1;
		diagnose(false);
		if (debugAll)
			GlHelper.checkErrors();
		return !glfwWindowShouldClose(window);
	}

	private void diagnose(boolean force) {
		if (diagnosing) {
			if (force)
				Printer.ln("force");

			diagnoseTimer++;
			if (diagnoseTimer == 60 || force) {
				diagnoseTimer = 0;
				GlHelper.diagnozeMem();

			}
			GlHelper.checkErrors();
		}
	}

	public boolean focused() {
		return windowIsFocused;
	}

	void pollEvents() {
		glfwPollEvents();
	}

	long getWindow() {
		return window;
	}

	void dis() {

		if (renderer != null)
			renderer.dis();

		gl.dispose();

		Callbacks.glfwFreeCallbacks(window);
		glfwDestroyWindow(window);

		glfwTerminate();
		GLFWErrorCallback e = glfwSetErrorCallback(null);
		if (e != null)
			e.free();

		Printer.ln(GraphicContext.class + " was sucessfully destroyed");

	}

	static void terminate() {
		// glfwTerminate();
	}

	boolean isFocused() {
		return windowIsFocused;
	}

	void takeScreenShot() {
		String s = FileManager.NAME.timeStampString(screenShotPath + "shot");
		SnakeImage image = new SnakeImage(nativeWidth, nativeHeight);
		copyFB(image, 0, 0);
		image.save(s + ".png");
		System.gc();

	}

	public void makeScreenShot() {
		if (screenShotPath == null)
			return;
		new CORE.GlJob() {
			@Override
			protected void doJob() {
				takeScreenShot();
			}
		}.perform();
		;

	}

	void copyFB(SnakeImage image, int startX, int startY) {
		ByteBuffer buff = gl.getFramePixels(nativeWidth, nativeHeight);
		for (int x = 0; x < nativeWidth; x++) {
			int x1 = startX + x;
			if (x1 >= image.width)
				continue;
			for (int y = 0; y < nativeHeight; y++) {
				int y1 = startY + nativeHeight - (y + 1);
				if (y1 >= image.height)
					continue;
				int i = (x + (nativeWidth * (y))) * 4;
				int r = buff.get(i) & 0xFF;
				int g = buff.get(i + 1) & 0xFF;
				int b = buff.get(i + 2) & 0xFF;
				image.rgb.set(x1, y1, r, g, b, 0xFF);

			}
		}
	}

	void copyFB(SnakeImage image, int startX, int startY, int scale) {
		ByteBuffer buff = gl.getFramePixels(nativeWidth, nativeHeight);
		for (int x = 0; x < nativeWidth / scale; x++) {
			int x1 = startX + x;
			if (x1 >= image.width)
				continue;
			for (int y = 0; y < nativeHeight / scale; y++) {
				int y1 = startY + nativeHeight / scale - (y + 1);
				if (y1 >= image.height)
					continue;
				int i = (x * scale + (nativeWidth * (y) * scale)) * 4;
				int r = buff.get(i) & 0xFF;
				int g = buff.get(i + 1) & 0xFF;
				int b = buff.get(i + 2) & 0xFF;
				image.rgb.set(x1, y1, r, g, b, 0xFF);

			}
		}
	}

}
