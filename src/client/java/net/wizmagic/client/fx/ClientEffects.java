package net.wizmagic.client.fx;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class ClientEffects {
	private static final List<Timeline> ACTIVE = new ArrayList<>();

	private ClientEffects() {
	}

	/** Starts a timeline; its first stage runs immediately. */
	public static void start(Timeline timeline) {
		if (!timeline.step()) {
			ACTIVE.add(timeline);
		}
	}

	public static void tick() {
		Iterator<Timeline> it = ACTIVE.iterator();
		while (it.hasNext()) {
			if (it.next().step()) {
				it.remove();
			}
		}
	}

	public static void clear() {
		ACTIVE.clear();
	}
}
