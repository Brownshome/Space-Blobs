package time;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.Sys;

import event.ClockEvent;
import event.EventListener;

public class Clock {
	public static final double DEFAULT_FRAME_DELTA = 1.0 / 60.0;

	Map<EventListener, Long> listeners = new HashMap<>();

	long frameNo = 0;
	long startTime = getABSTime(1000f);
	
	long lastAverage;
	int tps = 0;
	int count = 0;

	/** averaged over 20 frames in seconds (in seconds) */
	public double getAverageDelta() {
		return 1000.0 / tps;
	}

	public int getTPS() {
		return tps;
	}

	public void addClockEvent(EventListener l, long time) {
		listeners.put(l, time + getTime());
	}

	public void addABSClockEvent(EventListener l, long time) {
		listeners.put(l, time);
	}

	/** The time in 1 / resoloution of a second (Sys) */
	public static long getABSTime(float resoloution) {
		return (long) (Sys.getTime() * (resoloution / Sys.getTimerResolution()));
	}

	/** The time since started, in ms */
	public long getTime() {
		return getABSTime(1000f) - startTime;
	}

	public void tick() {
		for(Entry<EventListener, Long> entry : listeners.entrySet())
			if(entry.getValue() < getTime()) 
				entry.getKey().event(new ClockEvent());

		frameNo++;		
		count++;
		
		if(getTime() - lastAverage > 1000) {
			lastAverage = getTime();
			tps = count;
			count = 0;
		}
	}

	public long getFrame() {
		return frameNo;
	}
}
