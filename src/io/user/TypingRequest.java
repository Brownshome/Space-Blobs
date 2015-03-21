package io.user;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.lwjgl.input.Keyboard;

import event.Event;
import event.EventListener;

public class TypingRequest extends Event {
	/** A request to be used when avoiding null pointer exceptions */
	public static final TypingRequest DUMMY_REQUEST = new TypingRequest(0, null, false);

	static final int FRAMES_OF_GRACE = 20;
	static final int POST_GRACE_REFRESH_RATE = 1;

	int ignoreCount = 0;
	Map<Integer, Supplier<String>> specialActions = new HashMap<>();

	static {
		DUMMY_REQUEST.done = true;
	}

	String text = "";
	final boolean blockInput;
	boolean done = false;
	final int terminator; //the key that closes the text box

	int position = 0;
	int selection = -1; //-1 means the nothing is selected

	EventListener l;

	/** l can be null, if that is the case no event will be fired upon termination */
	TypingRequest(int terminator, EventListener l, boolean blockInput) {
		this.terminator = terminator;
		this.l = l;
		this.blockInput = blockInput;
	}

	public void ignore(int n) {
		if(n < 0) Console.error("Cannot ignore negative numbers: " + n, "TEXT IO");
		ignoreCount += n;
	}

	public int getPosition() {
		return position;
	}

	public int getSelectionIndex() {
		return selection;
	}

	public String getText() {
		return text;
	}

	void press(int key, char character) {
		if(ignoreCount != 0) {
			ignoreCount--;
			return;
		}

		if(key == terminator) {
			if(l != null) l.event(this);
			KeyIO.request = null;
			done = true;
			return;
		}

		switch(key) {
			case Keyboard.KEY_BACK:
				if(position != 0)
					if(selection == -1) {
						text = text.substring(0, position - 1) + text.substring(position);
						position--;
					} else {
						text = text.substring(0, selection) + text.substring(position);
						position = selection;
						selection = -1;
					}

				return;
			case Keyboard.KEY_DELETE:
				if(position != 0)
					if(selection == -1) {
						if(position == text.length()) {
							text = text.substring(0, position - 1);
							position--;
						} else text = text.substring(0, position) + text.substring(position + 1);

					} else {
						text = text.substring(0, selection) + text.substring(position);
						position = selection;
						selection = -1;
					}

				return;
			case Keyboard.KEY_V:
				if(!KeyIO.isCtrlDown()) break;

				String clipText = ClipboardWrapper.getClipboardContents();
				if(selection != -1) {
					text = text.substring(0, selection) + clipText + text.substring(position);
					position = selection + clipText.length();
					selection = -1;
				} else {
					text = text.substring(0, position) + clipText + text.substring(position);
					position += clipText.length();
				}
				return;
			case Keyboard.KEY_C:
				if(!KeyIO.isCtrlDown() || selection == -1) break;

				ClipboardWrapper.setClipboardContents(text.substring(selection, position));
				return;
			case Keyboard.KEY_X:
				if(!KeyIO.isCtrlDown() || selection == -1) break;

				ClipboardWrapper.setClipboardContents(text.substring(selection, position));
				text = text.substring(0, selection) + text.substring(position);
				position = selection;
				selection = -1;
				return;
			case Keyboard.KEY_A:
				if(!KeyIO.isCtrlDown()) break;

				selection = 0;
				position = text.length();
				return;
			case Keyboard.KEY_LEFT:
				if(KeyIO.isShiftDown()) {
					if(selection == -1 && position > 0) selection = position - 1;
					if(selection > 0) selection--;
				} else {
					if(position > 0) position--;
					selection = -1;
				}

				return;

			case Keyboard.KEY_RIGHT:
				if(selection == -1)
					selection = position;

				if(!KeyIO.isShiftDown())
					selection = -1;

				if(position < text.length())
					position++;

				return;
		}

		String addition = specialActions.getOrDefault(key, () -> (character == 0 ? "" : "" + character)).get();

		if(selection == -1) {
			text = text.substring(0, position) + addition + text.substring(position);
			position += addition.length();
		} else {
			text = text.substring(0, selection) + addition + text.substring(position);
			position = selection + addition.length();
			selection = -1;
		}
	}

	public boolean isDone() {
		return done;
	}

	public void setText(String string) {
		text = string;
		if(position > text.length()) position = text.length();
		if(selection > text.length()) selection = text.length();
	}

	public void addSpecialKey(int key, Supplier<String> task) {
		specialActions.put(key, task);
	}
}
