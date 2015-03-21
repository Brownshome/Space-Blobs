package render;

import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.GL_VERSION;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glGetError;
import static org.lwjgl.opengl.GL11.glGetString;
import static org.lwjgl.util.glu.GLU.gluErrorString;
import io.FileIO;
import io.user.Console;
import io.user.KeyIO;

import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.PixelFormat;

import physics.common.Vec2;

public class Renderer {
	public static final boolean CHECK_GL = true;
	public static final int ID_CASHE_AMOUNT = 32;
	public static boolean vSync = false;

	private static IntBuffer buffers = BufferUtils.createIntBuffer(ID_CASHE_AMOUNT);
	private static IntBuffer vertArrays = BufferUtils.createIntBuffer(ID_CASHE_AMOUNT);
	private static IntBuffer textures = BufferUtils.createIntBuffer(ID_CASHE_AMOUNT);
	
	private static ArrayList<Runnable> renderCalls = new ArrayList<>();
	
	/** The size of a 1 x 1 block in world space in screen space */
	public static Vec2 zoom = new Vec2(1.0 / 1.920, 1.0 / 1.080);
	/** The offset of the camera in world space */
	public static Vec2 position = new Vec2();

	static {
		buffers.flip();
		vertArrays.flip();
		textures.flip();
		
		KeyIO.addAction(Renderer::toggleVsync, Keyboard.KEY_V, KeyIO.KEY_PRESSED);
	}

	public static void toggleVsync() {
		vSync = !vSync;
		Display.setVSyncEnabled(vSync);
	}
	
	public static void cleanup() {
		
	}

	public static void render() {
		for(Runnable call : renderCalls)
			call.run();
	}

	public static int createShader(String name, int type) {
		int id = GL20.glCreateShader(type);

		GL20.glShaderSource(id, FileIO.readFileAsText("shaders/" + name + ".glsl"));
		GL20.glCompileShader(id);

		if(CHECK_GL && GL11.GL_FALSE == GL20.glGetShaderi(id, GL20.GL_COMPILE_STATUS)) {
			int length = GL20.glGetShaderi(id, GL20.GL_INFO_LOG_LENGTH);
			Console.error("Shader \"" + name + "\" did not compile: \n" + GL20.glGetShaderInfoLog(id, length), "RENDER");
		}

		return id;
	}

	public static int createProgram(int... shaders) {
		int id = GL20.glCreateProgram();
		for(int shader : shaders)
			GL20.glAttachShader(id, shader);

		GL20.glLinkProgram(id);

		if(CHECK_GL && GL11.GL_FALSE == GL20.glGetProgrami(id, GL20.GL_VALIDATE_STATUS)) {
			int length = GL20.glGetProgrami(id, GL20.GL_INFO_LOG_LENGTH);
			Console.error(GL20.glGetProgramInfoLog(id, length), "RENDER");
		}

		return id;
	}

	public static int getTextureID() {
		if(!textures.hasRemaining()) {
			textures.clear();
			GL11.glGenTextures(textures);
		}

		return textures.get();
	}

	public static void createContext(boolean fullscreen) {
		try {
			PixelFormat pixelFormat = new PixelFormat(0, 8, 0, 4);
			ContextAttribs contextAtrributes = new ContextAttribs(4, 4).withForwardCompatible(true).withProfileCore(true);

			DisplayMode[] modes = Display.getAvailableDisplayModes();

			boolean found = false;

			for (DisplayMode d : modes)
				if (isAcceptable(d)) {
					Display.setDisplayMode(d);
					Console.inform("Display Created.", "OPENGL");
					found = true;
					break;
				}

			if (!found)
				System.out.println("Display not found, making one anyway, tantrums may be thrown.");

			Display.setFullscreen(fullscreen);
			Display.setVSyncEnabled(true);

			Display.setResizable(true);
			
			Display.create(pixelFormat, contextAtrributes);

			glClearColor(0, 0, 0, 0);

			Console.inform("OpenGL Version: " + glGetString(GL_VERSION), "OPENGL");

		} catch (LWJGLException e) {
			Console.error("A LWJGL error occured.", e, "LWJGL");
		}
	}

	public static void checkGL(String message) {
		if(!CHECK_GL)
			return;

		int errorCheckValue = glGetError();
		if (errorCheckValue != GL_NO_ERROR)
			Console.error(message + gluErrorString(errorCheckValue), "OPENGL");
	}

	public static void checkGL() {
		checkGL("GL Error: ");
	}

	private static boolean isAcceptable(DisplayMode d) {
		return d.isFullscreenCapable()
			&& d.getBitsPerPixel() == Display.getDisplayMode().getBitsPerPixel()
			&& d.getFrequency() == Display.getDisplayMode().getFrequency()
			&& d.getHeight() == Display.getDisplayMode().getHeight()
			&& d.getWidth() == Display.getDisplayMode().getWidth();
	}

	public static int getBufferID() {
		if(!buffers.hasRemaining()) {
			buffers.clear();
			GL15.glGenBuffers(buffers);
		}

		return buffers.get();
	}

	public static int getVertexArrayID() {
		if(!vertArrays.hasRemaining()) {
			vertArrays.clear();
			GL30.glGenVertexArrays(vertArrays);
		}

		return vertArrays.get();
	}

	public static void setFullscreen(boolean state) {
	
	}

	public static void initialize() {
		createContext(true);
	}
}
