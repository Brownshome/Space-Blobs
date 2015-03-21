package event;

import org.lwjgl.input.Keyboard;

public class KeyboardEvent extends Event {
	public int keyCode;
	public boolean isPushed;
	public String name;
	
	public KeyboardEvent() {
		keyCode = Keyboard.getEventKey();
		isPushed = Keyboard.getEventKeyState();
	}
	
	public KeyboardEvent(boolean isPushed, int code) {
		keyCode = code;
		this.isPushed = isPushed;
	}
}
