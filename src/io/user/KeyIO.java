package io.user;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import event.EventListener;

public class KeyIO {
	private static class KeyCombo {
		int code;
		int flags;
		Runnable action;

		public KeyCombo(Runnable action, int code, int flags) {
			this.action = action;
			this.code = code;
			this.flags = flags;
		}
	}

	public static float dx;
	public static float dy;
	public static float dz;

	public static boolean holdMouse = true;

	public static final int KEY_DOWN = 16;
	public static final int KEY_PRESSED = 64;
	public static final int KEY_RELEASED = 128;
	public static final int KEY_UP = 32;

	/** Only works for KEY_PRESSED and KEY_RELEASED */
	public static final int ALL_KEYS = Integer.MIN_VALUE;

	public static final int MOUSE_BUTTON_DOWN = 2;
	public static final int MOUSE_BUTTON_PRESSED = 4;
	public static final int MOUSE_BUTTON_RELEASED = 8;
	public static final int MOUSE_BUTTON_UP = 1;

	private static Map<Integer, KeyCombo> mouseClickListeners = new HashMap<>();
	private static Map<Integer, KeyCombo> keyListeners = new HashMap<>();

	private static Map<Integer, int[]> down = new HashMap<>();

	static TypingRequest request; //will be null when there is no active request

	static boolean ctrl = false;
	static boolean shift = false;

	public static TypingRequest getText(EventListener l) {
		return getText(l, Keyboard.KEY_RETURN, true);
	}

	public static TypingRequest getText() {
		return getText(null, Keyboard.KEY_RETURN, true);
	}

	public static TypingRequest getText(boolean block) {
		return getText(null, Keyboard.KEY_RETURN, block);
	}

	public static TypingRequest getText(int terminator) {
		return getText(null, terminator, true);
	}

	public static TypingRequest getText(int terminator, boolean block) {
		return getText(null, terminator, block);
	}

	public static TypingRequest getText(EventListener l, int terminator, boolean block) {
		return request = new TypingRequest(terminator, l, block);
	}
	
	static int id = Integer.MIN_VALUE;
	
	/** remove action */
	public static void removeAction(int id) {
		mouseClickListeners.remove(id);
		keyListeners.remove(id);
	}
	
	/** returns an index to be used to remove it */
	public static int addAction(Runnable action, int code, int flags) {
		if ((flags & (MOUSE_BUTTON_UP | MOUSE_BUTTON_DOWN | MOUSE_BUTTON_RELEASED | MOUSE_BUTTON_PRESSED)) != 0)
			mouseClickListeners.put(id, new KeyCombo(action, code, flags & (MOUSE_BUTTON_UP | MOUSE_BUTTON_DOWN | MOUSE_BUTTON_RELEASED | MOUSE_BUTTON_PRESSED)));

		if ((flags & (KEY_UP | KEY_DOWN | KEY_PRESSED | KEY_RELEASED)) != 0)
			keyListeners.put(id, new KeyCombo(action, code, flags & (KEY_DOWN | KEY_UP | KEY_PRESSED | KEY_RELEASED)));
		
		return id++;
	}

	public static void tick() {
		// Using poll as a 1 tick updater
		if(request == null || !request.blockInput) {
			dx = Mouse.getDX();
			dy = Mouse.getDY();
			dz = Mouse.getDWheel();

			if(request == null || !request.blockInput) {
				for (KeyCombo l : mouseClickListeners.values()) {
					if ((l.flags & MOUSE_BUTTON_DOWN) != 0
						&& Mouse.isButtonDown(l.code))
						l.action.run();

					if ((l.flags & MOUSE_BUTTON_UP) != 0 && !Mouse.isButtonDown(l.code))
						l.action.run();
				}

				for (KeyCombo l : keyListeners.values()) {
					if ((l.flags & KEY_DOWN) != 0 && Keyboard.isKeyDown(l.code))
						l.action.run();

					if ((l.flags & KEY_UP) != 0 && !Keyboard.isKeyDown(l.code))
						l.action.run();
				}
			}
		}

		shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
		ctrl = Keyboard.isKeyDown(Keyboard.KEY_RCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_LCONTROL);

		// start poll for press / release
		while (Keyboard.next()) {
			int key = Keyboard.getEventKey();
			char c = Keyboard.getEventCharacter();

			if(Keyboard.getEventKeyState())
				down.put(key, new int[] {0, c});
			else
				down.remove(key);

			if(request != null && request.blockInput) continue;

			for (KeyCombo l : keyListeners.values()) {
				if (key != l.code && l.code != ALL_KEYS) continue;
				if ((l.flags & KEY_PRESSED) != 0 && Keyboard.getEventKeyState()) {
					l.action.run();
					continue;
				}

				if ((l.flags & KEY_RELEASED) != 0 && !Keyboard.getEventKeyState()) {
					l.action.run();
					continue;
				}
			}
		}

		if(request != null)
			for(Entry<Integer, int[]> e : down.entrySet()) {

				int f = e.getValue()[0]; //not micro-optimization, just making the line not so ruddy long

				if(f == 0 || f > TypingRequest.FRAMES_OF_GRACE && f % TypingRequest.POST_GRACE_REFRESH_RATE == 0)
					request.press(e.getKey(), (char) e.getValue()[1]);

				e.getValue()[0]++;
			}

		if(request == null || !request.blockInput)
			while (Mouse.next()) {
				int button = Mouse.getEventButton();
				
				if (button == -1) 
					continue;
				
				for (KeyCombo l : mouseClickListeners.values()) {
					if (button != l.code) 
						continue;
					
					if ((l.flags & MOUSE_BUTTON_PRESSED) != 0
						&& Mouse.getEventButtonState()) {
						l.action.run();
						continue;
					}

					if ((l.flags & MOUSE_BUTTON_RELEASED) != 0
						&& !Mouse.getEventButtonState()) {
						l.action.run();
						continue;
					}
				}
			}
	}

	public static boolean isCtrlDown() {
		return ctrl;
	}

	public static boolean isShiftDown() {
		return shift;
	}
}
