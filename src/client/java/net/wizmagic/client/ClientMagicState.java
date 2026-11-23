
package net.wizmagic.client;

import net.wizmagic.net.ManaPayload;

public final class ClientMagicState {
    public static float mana = 100f;
    public static float maxMana = 100f;
    public static int selected = 0;
    public static final float[] COOLDOWN = new float[4];
    public static final float[] COOLDOWN_TOTAL = {1, 1, 1, 1};
    public static long lastChange = 0;

    private ClientMagicState() {
    }

    public static void apply(ManaPayload p) {
        mana = p.mana();
        maxMana = p.maxMana();
        if (selected != p.selected()) {
            lastChange = System.currentTimeMillis();
        }
        selected = p.selected();
        for (int i = 0; i < 4; i++) {
            COOLDOWN[i] = p.cooldown(i);
            COOLDOWN_TOTAL[i] = Math.max(1, p.total(i));
        }
    }

    public static void selectLocally(int index) {
        selected = index;
        lastChange = System.currentTimeMillis();
    }

    public static void tick() {
        for (int i = 0; i < 4; i++) {
            if (COOLDOWN[i] > 0) {
                COOLDOWN[i] -= 1f;
            }
        }
    }
}
