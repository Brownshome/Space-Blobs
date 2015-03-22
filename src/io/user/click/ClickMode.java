package io.user.click;

import java.io.IOException;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.opengl.CursorLoader;

import physics.common.Vec2;
import io.user.Console;
import io.user.KeyBinds;
import io.user.KeyIO;

/** These are cycled through by clicking click.cycle, different cycles may be defined by making sepperate loops */
public abstract class ClickMode {
	public static ClickMode CREATIVE_BUILD_MODE = new CreativeBuildMode();
	public static ClickMode MOVE_MODE = new MoveMode();
	static ClickMode current = CREATIVE_BUILD_MODE;
	
	public static void initialize() {
		KeyBinds.add(ClickMode::primary, 0, KeyIO.MOUSE_BUTTON_PRESSED, "click.primary");
		KeyBinds.add(ClickMode::secondary, 1, KeyIO.MOUSE_BUTTON_PRESSED, "click.secondary");
		KeyBinds.add(ClickMode::cycle, 2, KeyIO.MOUSE_BUTTON_PRESSED, "click.cycle");
		CREATIVE_BUILD_MODE.next = CREATIVE_BUILD_MODE.previous = MOVE_MODE;
		MOVE_MODE.next = MOVE_MODE.previous = CREATIVE_BUILD_MODE;
		//TODO mod entry point here
		current.activate();
	}
	
	public static void tick() {
		ClickTest test = new ClickTest();
		current.hover(test.fixture == null ? null : (Clickable) test.fixture.getUserData(), test.point);
	}
	
	/** select and unselect will always be called */
	public static void set(ClickMode c) {
		current.deactivate();
		current = c;
		c.activate();
	}
	
	public static void cycle() {
		current.deactivate();
		current = KeyIO.isCtrlDown() ? current.previous : current.next;
		current.activate();
	}
	
	public static void secondary() {
		ClickTest test = new ClickTest();
		current.secondary(test.fixture == null ? null : (Clickable) test.fixture.getUserData(), test.point);
	}
	
	public static void primary() {
		ClickTest test = new ClickTest();
		current.primary(test.fixture == null ? null : (Clickable) test.fixture.getUserData(), test.point);
	}
	
	ClickMode next;
	ClickMode previous;
	Cursor icon;
	
	/** icon is not set, so you had better do it */
	ClickMode() {
		next = previous = this;
	}
	
	ClickMode(String pic, int x, int y) {
		this();
		
		try {
			icon = CursorLoader.get().getCursor("textures/" + pic.replace(".", "/") + ".png", x, y);
		} catch (IOException | LWJGLException e) {
			Console.error("Loading of the cursor " + pic + " failed.", e, "RENDER");
		}
	}
	
	/** adds a new clickmode into the cycle after this clickmode */
	public void add(ClickMode c) {
		c.next = next;
		next = c;
		c.previous = this;
	}
	
	/** clickable may be null, point is the point in world space that was clicked, called by click.primary */
	public abstract void primary(Clickable c, Vec2 point);
	
	/** clickable may be null, point is the point in world space that was clicked, called by click.secondary */
	public abstract void secondary(Clickable c, Vec2 point);
	
	/** clickable may be null, point is the point in world space that the mouse is, called every frame */
	public abstract void hover(Clickable c, Vec2 point);
	
	/** called when this clickmode is activated */
	public void activate() {
		try {
			Mouse.setNativeCursor(icon);
		} catch (LWJGLException e) {
			Console.error("error setting cursor", e, "RENDER");
		}
	}
	
	/** called when this clickmode is deactivated */
	public void deactivate() {
		
	}
}
