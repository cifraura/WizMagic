package net.wizmagic.client.fx;

import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.wizmagic.util.WandMath;

import java.util.List;

/** Multi stage impact effects, one per spell. Stages are scheduled on client ticks. */
final class ImpactFx {
	private ImpactFx() {
	}

	private static Vec3 unit(RandomSource r) {
		return FxPlayer.randomUnit(r);
	}

	private static Vec3 ring(Vec3 normal, double angle) {
		return FxPlayer.ringDir(normal, angle);
	}

	/** Random direction on the hemisphere around the normal, biased outwards. */
	private static Vec3 hemi(Vec3 normal, RandomSource r) {
		Vec3 w = unit(r);
		if (w.dot(normal) < 0) {
			w = w.scale(-1);
		}
		return w.scale(0.8).add(normal.scale(0.5)).normalize();
	}

	// ------------------------------------------------------------------ AQUA: big animated splash

	static Timeline aqua(Vec3 c, Vec3 normal, float radius, RandomSource r) {
		Timeline t = new Timeline();
		t.at(0, () -> {
			Fx.p("aqua_splash").at(c).size(radius * 0.55f).life(10).spawn();
			Fx.p("wand_glow").at(c).size(radius * 0.6f).life(4).color(0xBFE9FF).alpha(0.8f).spawn();
			Fx.p("water_ring").at(c).size(radius * 0.9f).life(11).spawn();
			for (int i = 0, n = Fx.n(70); i < n; i++) {
				Vec3 d = hemi(normal, r);
				double sp = 0.15 + r.nextDouble() * 0.45;
				Fx.p("aqua_droplet").at(c).vel(d.scale(sp)).size(0.05f + r.nextFloat() * 0.12f).life(16 + r.nextInt(16))
						.gravity(0.9f + r.nextFloat() * 0.5f).spawn();
			}
			for (int i = 0, n = Fx.n(16); i < n; i++) {
				Fx.p("aqua_mist").at(c.add(unit(r).scale(0.4))).vel(hemi(normal, r).scale(0.08)).size(0.4f + r.nextFloat() * 0.4f).spawn();
			}
			int ringCount = Fx.n(40);
			for (int i = 0; i < ringCount; i++) {
				double a = i * Math.PI * 2 / ringCount;
				Fx.p("aqua_spiral").at(c).vel(ring(normal, a).scale(0.32)).size(0.1f).life(9).drag(0.9f).spawn();
			}
		});
		t.at(3, () -> {
			Fx.p("water_ring").at(c).size(radius * 1.2f).life(12).alpha(0.7f).spawn();
			int ringCount = Fx.n(28);
			for (int i = 0; i < ringCount; i++) {
				double a = i * Math.PI * 2 / ringCount + 0.1;
				Fx.p("aqua_droplet").at(c).vel(ring(normal, a).scale(0.22).add(normal.scale(0.2)))
						.size(0.08f).life(18).gravity(1.0f).spawn();
			}
		});
		t.every(6, 16, () -> {
			for (int i = 0, n = Fx.n(4); i < n; i++) {
				Vec3 p = c.add(ring(normal, r.nextDouble() * Math.PI * 2).scale(r.nextDouble() * radius * 0.8)).add(0, 1.4, 0);
				Fx.p("aqua_droplet").at(p).vel(0, -0.05, 0).size(0.05f + r.nextFloat() * 0.05f).life(14).gravity(0.8f).spawn();
			}
		});
		return t;
	}

	// ------------------------------------------------------------------ LIGHTNING: circular electrical explosion

	static Timeline lightning(Vec3 c, Vec3 normal, float radius, RandomSource r) {
		boolean big = radius > 2f;
		float scale = big ? 1f : 0.45f;
		Timeline t = new Timeline();
		t.at(0, () -> {
			Fx.p("bolt_glow").at(c).size(1.5f * scale).life(5).alpha(0.9f).spawn();
			Fx.p("bolt_core").at(c).size(0.9f * scale).life(3).spawn();
			Fx.p("electric_ring").at(c).size(1.6f * scale).life(9).spawn();
			int count = Fx.n(big ? 48 : 18);
			for (int i = 0; i < count; i++) {
				double a = i * Math.PI * 2 / count;
				int color = i % 2 == 0 ? 0xFFFFFF : 0x4FA0FF;
				Fx.p("bolt_spark").at(c).vel(ring(normal, a).scale(0.55 * scale)).color(color).life(7).drag(0.88f).size(0.14f).spawn();
			}
			for (int i = 0, n = Fx.n(big ? 30 : 10); i < n; i++) {
				Fx.p("bolt_spark").at(c).vel(unit(r).scale(0.1 + r.nextDouble() * 0.25)).life(8 + r.nextInt(10)).spawn();
			}
			int arcs = big ? 8 : 3;
			for (int i = 0; i < arcs; i++) {
				Vec3 end = c.add(ring(normal, i * Math.PI * 2 / arcs + r.nextDouble() * 0.4).scale((1.0 + r.nextDouble() * 1.4) * scale));
				FxPlayer.drawPolyline(FxPlayer.jagged(c, end, r, 0.18, 3), 0.8f);
			}
		});
		if (big) {
			t.at(2, () -> {
				Fx.p("electric_ring").at(c).size(2.6f).life(10).alpha(0.7f).spawn();
				int count = Fx.n(32);
				for (int i = 0; i < count; i++) {
					double a = i * Math.PI * 2 / count + 0.2;
					Fx.p("bolt_spark").at(c).vel(ring(normal, a).scale(0.4)).color(0x9FD0FF).life(8).drag(0.9f).spawn();
				}
				for (int i = 0; i < 4; i++) {
					Vec3 end = c.add(unit(r).scale(1.6));
					FxPlayer.drawPolyline(FxPlayer.jagged(c, end, r, 0.15, 3), 0.7f);
				}
			});
			t.every(3, 8, () -> {
				for (int i = 0, n = Fx.n(5); i < n; i++) {
					Fx.p("bolt_spark").at(c.add(unit(r).scale(r.nextDouble() * 1.6))).vel(unit(r).scale(0.06)).life(8).spawn();
				}
			});
		}
		return t;
	}

	// ------------------------------------------------------------------ INFERNO: circular burst of magical fire

	static Timeline inferno(Vec3 c, Vec3 normal, float radius, RandomSource r) {
		Timeline t = new Timeline();
		t.at(0, () -> {
			Fx.p("fire_orb").at(c).size(radius * 0.4f).life(6).spawn();
			Fx.p("fire_burst").at(c).size(radius * 0.55f).life(9).spawn();
			Fx.p("wand_glow").at(c).size(radius * 0.9f).life(6).color(0xFF6A1A).alpha(0.6f).spawn();
			int count = Fx.n(50);
			for (int i = 0; i < count; i++) {
				double a = i * Math.PI * 2 / count + r.nextDouble() * 0.1;
				Fx.p("flame").at(c).vel(ring(normal, a).scale(0.2 + r.nextDouble() * 0.2)).size(0.25f + r.nextFloat() * 0.35f)
						.life(14 + r.nextInt(10)).drag(0.9f).spawn();
			}
			for (int i = 0, n = Fx.n(32); i < n; i++) {
				Fx.p("flame").at(c).vel(hemi(normal, r).scale(0.08 + r.nextDouble() * 0.2)).size(0.2f + r.nextFloat() * 0.35f).spawn();
			}
			for (int i = 0, n = Fx.n(80); i < n; i++) {
				Fx.p("ember").at(c).vel(unit(r).scale(0.1 + r.nextDouble() * 0.4)).gravity(0.04f + r.nextFloat() * 0.06f).spawn();
			}
			for (int i = 0, n = Fx.n(18); i < n; i++) {
				Fx.p("fire_smoke").at(c.add(unit(r).scale(0.4))).vel(hemi(normal, r).scale(0.05 + r.nextDouble() * 0.08)).size(0.4f + r.nextFloat() * 0.4f).spawn();
			}
		});
		t.at(3, () -> {
			Fx.p("fire_burst").at(c).size(radius * 0.85f).life(10).alpha(0.8f).spawn();
			int count = Fx.n(30);
			for (int i = 0; i < count; i++) {
				double a = i * Math.PI * 2 / count;
				Fx.p("fire_smoke").at(c).vel(ring(normal, a).scale(0.12)).size(0.5f).life(36).spawn();
			}
		});
		t.every(4, 36, () -> {
			for (int i = 0, n = Fx.n(1.5f); i < n; i++) {
				Vec3 p = c.add(unit(r).scale(r.nextDouble() * radius * 0.7));
				Fx.p("ember").at(p).vel(0, 0.02 + r.nextDouble() * 0.03, 0).gravity(-0.1f).life(40 + r.nextInt(30)).drag(0.97f).spawn();
			}
		});
		return t;
	}

	// ------------------------------------------------------------------ EXPULSO: cinematic multi stage magical explosion

	static Timeline expulso(Vec3 c, Vec3 normal, float radius, RandomSource r) {
		Timeline t = new Timeline();
		Vec3 up = new Vec3(0, 1, 0);
		Vec3 side = WandMath.perpendicular(normal);
		Vec3 side2 = normal.cross(side).normalize();

		// 1. bright central energy flash
		t.at(0, () -> {
			Fx.p("arcane_flash").at(c).size(radius * 0.9f).life(5).spawn();
			Fx.p("arcane_orb").at(c).size(radius * 0.5f).life(5).spawn();
			Fx.p("wand_glow").at(c).size(radius * 1.2f).life(6).color(0xE5C8FF).alpha(0.8f).spawn();
		});
		// 2. expanding purple energy rings
		for (int i = 0; i < 4; i++) {
			final int k = i;
			t.at(i * 2, () -> Fx.p("arcane_ring").at(c).size(radius * (0.7f + 0.25f * k)).life(12 + k).alpha(0.9f - 0.1f * k).spawn());
		}
		// 3. hundreds of custom particles spreading outward
		t.at(1, () -> {
			for (int i = 0, n = Fx.n(220); i < n; i++) {
				double sp = (0.15 + r.nextDouble() * r.nextDouble() * 0.9) * (radius / 4.5);
				String name = i % 5 == 0 ? "arcane_glow" : (i % 7 == 0 ? "wand_spark" : "arcane_mote");
				Fx.p(name).at(c).vel(unit(r).scale(sp)).spawn();
			}
			for (int plane = 0; plane < 3; plane++) {
				Vec3 axis = plane == 0 ? normal : (plane == 1 ? side : side2);
				int count = Fx.n(44);
				for (int i = 0; i < count; i++) {
					double a = i * Math.PI * 2 / count;
					Fx.p("arcane_mote").at(c).vel(ring(axis, a).scale(0.5 * radius / 4.5)).drag(0.9f).size(0.1f).life(26).spawn();
				}
			}
		});
		// 4. fading energy wave
		t.at(3, () -> {
			Fx.p("arcane_wave").at(c).size(radius * 1.15f).life(15).spawn();
			Fx.p("arcane_wave").at(c.add(up.scale(0.01))).size(radius * 0.8f).life(12).alpha(0.7f).spawn();
		});
		t.at(5, () -> {
			for (int i = 0, n = Fx.n(90); i < n; i++) {
				Fx.p("arcane_mote").at(c.add(unit(r).scale(radius * 0.4))).vel(unit(r).scale(0.1 + r.nextDouble() * 0.25)).spawn();
			}
		});
		// 5. small glowing particles slowly disappearing
		t.every(6, 38, () -> {
			for (int i = 0, n = Fx.n(7); i < n; i++) {
				Vec3 p = c.add(unit(r).scale(r.nextDouble() * radius * 0.85));
				Fx.p("arcane_glow").at(p).vel(unit(r).scale(0.01)).size(0.08f + r.nextFloat() * 0.12f).life(30 + r.nextInt(16)).spawn();
			}
		});
		return t;
	}

	@SuppressWarnings("unused")
	private static List<Vec3> unusedKeepImport(Vec3 a, Vec3 b, RandomSource r) {
		return FxPlayer.jagged(a, b, r, 0.1, 1);
	}
}
