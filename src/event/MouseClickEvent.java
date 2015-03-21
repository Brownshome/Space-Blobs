package event;

import org.lwjgl.input.Mouse;

public class MouseClickEvent extends Event {
	int x;
	int y;
	int button;
	boolean isPressed;
	boolean isClick;
	
	public MouseClickEvent() {
		this.x = Mouse.getEventX();
		this.y = Mouse.getEventY();
		this.button = Mouse.getEventButton();
		this.isPressed = Mouse.getEventButtonState();
	}
	
	public MouseClickEvent(int button, boolean isPressed) {
		this.x = Mouse.getX();
		this.y = Mouse.getY();
		this.button = button;
		this.isPressed = isPressed;
	}
}
