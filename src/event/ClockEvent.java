package event;

import time.Clock;

public class ClockEvent extends Event {
	public final float timeOfCreation = Clock.getABSTime(1000);
}
