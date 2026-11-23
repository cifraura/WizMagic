package net.wizmagic.client.fx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** A multi stage effect: runnables scheduled on client ticks (used for the cinematic explosions and splashes). */
public final class Timeline {
	private final Map<Integer, List<Runnable>> events = new TreeMap<>();
	private int tick = 0;
	private int end = 0;

	public Timeline at(int when, Runnable action) {
		events.computeIfAbsent(when, k -> new ArrayList<>()).add(action);
		end = Math.max(end, when);
		return this;
	}

	public Timeline every(int from, int to, Runnable action) {
		for (int t = from; t <= to; t++) {
			at(t, action);
		}
		return this;
	}

	/** Runs this tick's events. Returns true when the whole timeline is done. */
	public boolean step() {
		List<Runnable> now = events.get(tick);
		if (now != null) {
			for (Runnable r : now) {
				r.run();
			}
		}
		tick++;
		return tick > end;
	}
}
