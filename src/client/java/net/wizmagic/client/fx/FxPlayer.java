package net.wizmagic.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.wizmagic.item.WandType;
import net.wizmagic.magic.SpellType;
import net.wizmagic.net.FxPayload;
import net.wizmagic.util.WandMath;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns the compact server messages into the actual visuals. Every effect is built from the custom
 * particles only: orbs, streams, spirals, branching lightning, flames, embers, rings and multi stage explosions.
 */
public final class FxPlayer {
	private FxPlayer() {
	}

	public static void handle(FxPayload p) {
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null) {
			return;
		}
		SpellType spell = SpellType.byIndex(p.spell() & 15);
		int wandIdx = Math.min((p.spell() >> 4) & 15, WandType.VALUES.length - 1);
		int wandColor = WandType.VALUES[wandIdx].glow;
		Vec3 a = new Vec3(p.ax(), p.ay(), p.az());
		Vec3 b = new Vec3(p.bx(), p.by(), p.bz());
		RandomSource r = RandomSource.create(p.seed());

		switch (p.kind()) {
			case FxPayload.CHARGE -> charge(level, spell, wandColor, a, b, p.f(), r);
			case FxPayload.RELEASE -> release(spell, wandColor, a, b, r);
			case FxPayload.TRAIL -> trail(spell, a, b, p.f(), r);
			case FxPayload.ARC -> arc(a, b, p.f() > 0.5f, r);
			case FxPayload.AURA -> aura(a, b, r);
			case FxPayload.IMPACT -> impact(spell, a, b, p.f(), r);
			default -> {
			}
		}
	}

	// ------------------------------------------------------------------ helpers

	static Vec3 randomUnit(RandomSource r) {
		double z = r.nextDouble() * 2 - 1;
		double t = r.nextDouble() * Math.PI * 2;
		double s = Math.sqrt(1 - z * z);
		return new Vec3(s * Math.cos(t), z, s * Math.sin(t));
	}

	static Vec3 cross(Vec3 axis, RandomSource r) {
		return WandMath.perpendicular(axis);
	}

	static int mix(int a, int b, float t) {
		return net.wizmagic.client.particle.MagicParticle.lerpColor(a, b, t);
	}

	static int spellColor(SpellType s, RandomSource r) {
		return mix(s.primary, s.secondary, r.nextFloat());
	}

	/** Ring of points in the plane perpendicular to normal. */
	static Vec3 ringDir(Vec3 normal, double angle) {
		Vec3 u = WandMath.perpendicular(normal);
		Vec3 v = normal.cross(u).normalize();
		return u.scale(Math.cos(angle)).add(v.scale(Math.sin(angle)));
	}

	// ------------------------------------------------------------------ charge (sphere forming at the wand)

	private static void charge(ClientLevel level, SpellType s, int wandColor, Vec3 tip, Vec3 look, float prog, RandomSource r) {
		float ease = prog * prog * (3 - 2 * prog);
		float orb = 0.05f + 0.21f * ease;
		long time = level.getGameTime();

		switch (s) {
			case AQUA -> Fx.p("aqua_orb").at(tip).size(orb).spawn();
			case LIGHTNING -> {
				Fx.p("bolt_glow").at(tip).size(orb * 1.5f).life(3).spawn();
				Fx.p("bolt_core").at(tip).size(orb * 0.7f).life(3).spawn();
			}
			case INFERNO -> Fx.p("fire_orb").at(tip).size(orb).spawn();
			case EXPULSO -> Fx.p("arcane_orb").at(tip).size(orb).spawn();
		}
		Fx.p("wand_glow").at(tip).size(orb * 2.4f).life(3).color(s.primary).alpha(0.45f).spawn();

		// energy converging into the orb
		float radius = 1.0f - 0.7f * prog;
		for (int i = 0, n = Fx.n(3); i < n; i++) {
			Vec3 d = randomUnit(r);
			Vec3 start = tip.add(d.scale(radius));
			String name = switch (s) {
				case AQUA -> "aqua_spiral";
				case LIGHTNING -> "bolt_spark";
				case INFERNO -> "ember";
				case EXPULSO -> "arcane_mote";
			};
			Fx.p(name).at(start).vel(d.scale(-radius / 6.0)).life(6).drag(1f).gravity(0f)
					.color(i % 2 == 0 ? spellColor(s, r) : wandColor).spawn();
		}
		// the wand's own colour
		for (int i = 0, n = Fx.n(1.5f); i < n; i++) {
			Vec3 d = randomUnit(r).scale(0.14);
			Fx.p("wand_spark").at(tip.add(d)).vel(d.scale(0.12)).color(wandColor).life(12).spawn();
		}

		Vec3 u = WandMath.perpendicular(look);
		Vec3 v = look.cross(u).normalize();
		switch (s) {
			case AQUA -> {
				for (int i = 0; i < 3; i++) {
					double ang = time * 0.55 + i * 2.094;
					double rad = orb * 1.7 + 0.08;
					Vec3 pos = tip.add(u.scale(Math.cos(ang) * rad)).add(v.scale(Math.sin(ang) * rad));
					Fx.p("aqua_droplet").at(pos).size(0.05f).life(3).gravity(0f).spawn();
				}
			}
			case LIGHTNING -> {
				for (int i = 0, n = Fx.n(3); i < n; i++) {
					Vec3 d = randomUnit(r).scale(0.22);
					Fx.p("bolt_spark").at(tip.add(d)).vel(d.scale(0.1)).life(5).spawn();
				}
				if (time % 2 == 0) {
					Vec3 end = tip.add(randomUnit(r).scale(0.45));
					drawPolyline(jagged(tip, end, r, 0.08, 3), 0.6f);
				}
			}
			case INFERNO -> {
				for (int i = 0, n = Fx.n(1.5f); i < n; i++) {
					Vec3 d = randomUnit(r).scale(orb * 0.9);
					Fx.p("flame").at(tip.add(d)).vel(0, 0.015, 0).size(0.08f + 0.1f * ease).life(9).spawn();
				}
			}
			case EXPULSO -> {
				for (int i = 0; i < 2; i++) {
					double ang = time * 0.7 + i * Math.PI;
					double rad = orb * 1.6 + 0.12;
					Vec3 pos = tip.add(u.scale(Math.cos(ang) * rad)).add(v.scale(Math.sin(ang) * rad));
					Fx.p("arcane_glow").at(pos).size(0.07f).life(5).spawn();
				}
			}
		}
		if (prog < 0.17f) {
			String ring = switch (s) {
				case AQUA -> "water_ring";
				case LIGHTNING -> "electric_ring";
				case INFERNO -> "fire_burst";
				case EXPULSO -> "arcane_ring";
			};
			Fx.p(ring).at(tip).size(0.45f).life(8).alpha(0.6f).spawn();
		}
	}

	private static void release(SpellType s, int wandColor, Vec3 tip, Vec3 dir, RandomSource r) {
		Fx.p("wand_glow").at(tip).size(0.55f).life(6).color(s.secondary).alpha(0.9f).spawn();
		Fx.p("wand_glow").at(tip).size(0.9f).life(5).color(s.primary).alpha(0.5f).spawn();
		for (int i = 0, n = Fx.n(14); i < n; i++) {
			Vec3 d = dir.add(randomUnit(r).scale(0.35)).normalize().scale(0.1 + r.nextDouble() * 0.25);
			Fx.p("wand_spark").at(tip).vel(d).life(10 + r.nextInt(8)).color(i % 3 == 0 ? wandColor : spellColor(s, r)).spawn();
		}
		String ring = switch (s) {
			case AQUA -> "water_ring";
			case LIGHTNING -> "electric_ring";
			case INFERNO -> "fire_burst";
			case EXPULSO -> "arcane_ring";
		};
		Fx.p(ring).at(tip.add(dir.scale(0.2))).size(0.6f).life(9).spawn();
	}

	// ------------------------------------------------------------------ projectile trails

	private static void trail(SpellType s, Vec3 from, Vec3 to, float distBefore, RandomSource r) {
		Vec3 seg = to.subtract(from);
		double len = seg.length();
		if (len < 1.0E-4) {
			return;
		}
		Vec3 dir = seg.scale(1.0 / len);
		Vec3 u = WandMath.perpendicular(dir);
		Vec3 v = dir.cross(u).normalize();
		double spacing = 0.4 / Math.max(0.25f, Fx.density());

		switch (s) {
			case AQUA -> trailAqua(from, to, dir, u, v, len, distBefore, spacing, r);
			case INFERNO -> trailInferno(from, to, dir, u, v, len, spacing, r);
			case EXPULSO -> trailExpulso(from, to, dir, u, v, len, distBefore, spacing, r);
			default -> {
			}
		}
	}

	private static void trailAqua(Vec3 from, Vec3 to, Vec3 dir, Vec3 u, Vec3 v, double len, float dist, double spacing, RandomSource r) {
		for (double t = 0; t <= len; t += spacing) {
			Vec3 c = from.add(dir.scale(t));
			Fx.p("aqua_orb").at(c).size(0.5f + 0.06f * (float) r.nextGaussian()).life(2).alpha(0.9f).spawn();
			Vec3 jitter = u.scale((r.nextDouble() - 0.5) * 0.18).add(v.scale((r.nextDouble() - 0.5) * 0.18));
			Fx.p("aqua_stream").at(c.add(jitter)).vel(jitter.scale(0.05)).size(0.26f + r.nextFloat() * 0.18f).spawn();
			// two strands of blue energy spiralling around the jet
			double ang = (dist + t) * 3.4;
			for (int k = 0; k < 2; k++) {
				double a = ang + k * Math.PI;
				Vec3 off = u.scale(Math.cos(a) * 0.42).add(v.scale(Math.sin(a) * 0.42));
				Fx.p("aqua_spiral").at(c.add(off)).life(7).spawn();
			}
		}
		for (int i = 0, n = Fx.n(5); i < n; i++) {
			Vec3 c = from.add(dir.scale(r.nextDouble() * len));
			Vec3 out = randomUnit(r).scale(0.05 + r.nextDouble() * 0.12);
			Fx.p("aqua_droplet").at(c).vel(out.add(dir.scale(0.12))).size(0.05f + r.nextFloat() * 0.06f).spawn();
		}
		for (int i = 0, n = Fx.n(1.2f); i < n; i++) {
			Vec3 c = from.add(dir.scale(r.nextDouble() * len));
			Fx.p("aqua_mist").at(c).vel(randomUnit(r).scale(0.01)).spawn();
		}
		Fx.p("wand_glow").at(to).size(1.0f).life(3).color(0x3F9BFF).alpha(0.4f).spawn();
	}

	private static void trailInferno(Vec3 from, Vec3 to, Vec3 dir, Vec3 u, Vec3 v, double len, double spacing, RandomSource r) {
		for (double t = 0; t <= len; t += spacing) {
			Vec3 c = from.add(dir.scale(t));
			Fx.p("fire_orb").at(c).size(0.45f + 0.05f * (float) r.nextGaussian()).life(3).spawn();
			for (int k = 0, n = Fx.n(2); k < n; k++) {
				Vec3 off = u.scale((r.nextDouble() - 0.5) * 0.5).add(v.scale((r.nextDouble() - 0.5) * 0.5));
				Fx.p("flame").at(c.add(off)).vel(dir.scale(-0.04).add(0, 0.02, 0)).size(0.16f + r.nextFloat() * 0.2f).spawn();
			}
			for (int k = 0, n = Fx.n(1.5f); k < n; k++) {
				Vec3 sp = randomUnit(r).scale(0.04 + r.nextDouble() * 0.09);
				Fx.p("ember").at(c).vel(sp).spawn();
			}
		}
		for (int i = 0, n = Fx.n(1.2f); i < n; i++) {
			Fx.p("fire_smoke").at(from.add(dir.scale(r.nextDouble() * len))).vel(0, 0.01, 0).size(0.25f + r.nextFloat() * 0.2f).spawn();
		}
		Fx.p("wand_spark").at(to).size(0.18f).color(0xFFE2A0).life(8).spawn();
		Fx.p("wand_glow").at(to).size(1.1f).life(3).color(0xFF6A1A).alpha(0.4f).spawn();
	}

	private static void trailExpulso(Vec3 from, Vec3 to, Vec3 dir, Vec3 u, Vec3 v, double len, float dist, double spacing, RandomSource r) {
		for (double t = 0; t <= len; t += spacing) {
			Vec3 c = from.add(dir.scale(t));
			Fx.p("arcane_orb").at(c).size(0.5f).life(2).spawn();
			Fx.p("arcane_trail").at(c).vel(dir.scale(-0.02)).spawn();
			if (r.nextInt(2) == 0) {
				Vec3 sp = randomUnit(r).scale(0.03 + r.nextDouble() * 0.05).add(dir.scale(-0.04));
				Fx.p("arcane_mote").at(c.add(u.scale((r.nextDouble() - 0.5) * 0.3))).vel(sp).spawn();
			}
		}
		Fx.p("arcane_glow").at(to).size(1.1f).life(3).alpha(0.5f).spawn();
		// distortion rings along the way
		if (((int) (dist / 1.5f)) != ((int) ((dist + len) / 1.5f))) {
			Fx.p("arcane_ring").at(to).size(0.7f).life(10).spawn();
		}
	}

	// ------------------------------------------------------------------ lightning

	/** Midpoint displacement: a jagged polyline from a to b. */
	static List<Vec3> jagged(Vec3 a, Vec3 b, RandomSource r, double amplitude, int levels) {
		List<Vec3> pts = new ArrayList<>();
		pts.add(a);
		pts.add(b);
		double disp = amplitude;
		for (int l = 0; l < levels; l++) {
			List<Vec3> next = new ArrayList<>(pts.size() * 2);
			for (int i = 0; i < pts.size() - 1; i++) {
				Vec3 p0 = pts.get(i);
				Vec3 p1 = pts.get(i + 1);
				Vec3 d = p1.subtract(p0);
				Vec3 dn = d.lengthSqr() < 1.0E-8 ? new Vec3(0, 1, 0) : d.normalize();
				Vec3 pu = WandMath.perpendicular(dn);
				Vec3 pv = dn.cross(pu).normalize();
				double ang = r.nextDouble() * Math.PI * 2;
				Vec3 off = pu.scale(Math.cos(ang)).add(pv.scale(Math.sin(ang))).scale(r.nextDouble() * disp);
				next.add(p0);
				next.add(p0.add(p1).scale(0.5).add(off));
			}
			next.add(pts.get(pts.size() - 1));
			pts = next;
			disp *= 0.55;
		}
		return pts;
	}

	static void drawPolyline(List<Vec3> pts, float thickness) {
		for (int i = 0; i < pts.size() - 1; i++) {
			Vec3 p0 = pts.get(i);
			Vec3 p1 = pts.get(i + 1);
			double len = p0.distanceTo(p1);
			Vec3 dir = len < 1.0E-6 ? Vec3.ZERO : p1.subtract(p0).scale(1.0 / len);
			double step = 0.34 / Math.max(0.3f, Fx.density());
			for (double t = 0; t < len; t += step) {
				Vec3 c = p0.add(dir.scale(t));
				Fx.p("bolt_core").at(c).size(0.09f * thickness).life(3).spawn();
			}
			if (i % 2 == 0) {
				Fx.p("bolt_glow").at(p0).size(0.3f * thickness).life(4).alpha(0.7f).spawn();
			}
		}
	}

	private static void arc(Vec3 a, Vec3 b, boolean chain, RandomSource r) {
		double len = a.distanceTo(b);
		if (len < 0.05) {
			return;
		}
		int levels = Mth.clamp((int) Math.ceil(Math.log(Math.max(1.0, len / 0.7)) / Math.log(2)), 2, 7);
		double amp = Math.min(0.9, 0.12 + len * 0.03);
		List<Vec3> main = jagged(a, b, r, amp, levels);
		float thick = chain ? 0.7f : 1f;
		drawPolyline(main, thick);

		// branches
		int branches = chain ? 1 : 2 + (int) (len / 9);
		for (int i = 0; i < branches && main.size() > 4; i++) {
			int idx = 1 + r.nextInt(main.size() - 2);
			Vec3 base = main.get(idx);
			Vec3 dir = b.subtract(a).normalize().add(randomUnit(r).scale(1.1)).normalize();
			double bl = 1.0 + r.nextDouble() * 2.5;
			Vec3 end = base.add(dir.scale(bl));
			List<Vec3> br = jagged(base, end, r, 0.25, 3);
			drawPolyline(br, 0.55f * thick);
			if (r.nextFloat() < 0.35f) {
				Vec3 sub = br.get(br.size() / 2);
				Vec3 end2 = sub.add(randomUnit(r).scale(bl * 0.5));
				drawPolyline(jagged(sub, end2, r, 0.15, 2), 0.4f * thick);
			}
		}
		// electric particles drifting around the beam
		for (int i = 0, n = Fx.n(chain ? 3 : 6); i < n; i++) {
			Vec3 c = main.get(r.nextInt(main.size()));
			Fx.p("bolt_spark").at(c).vel(randomUnit(r).scale(0.1)).life(6 + r.nextInt(6)).spawn();
		}
		Fx.p("bolt_glow").at(b).size(chain ? 0.6f : 1.1f).life(2).alpha(0.8f).spawn();
		if (!chain) {
			Fx.p("bolt_glow").at(a).size(0.7f).life(2).alpha(0.8f).spawn();
			for (int i = 0, n = Fx.n(3); i < n; i++) {
				Fx.p("bolt_spark").at(a).vel(randomUnit(r).scale(0.12)).life(6).spawn();
			}
		}
	}

	private static void aura(Vec3 feet, Vec3 size, RandomSource r) {
		double width = Math.max(0.4, size.x);
		double height = Math.max(0.6, size.y);
		for (int i = 0, n = Fx.n(4); i < n; i++) {
			double ang = r.nextDouble() * Math.PI * 2;
			Vec3 pos = feet.add(Math.cos(ang) * width * 0.65, r.nextDouble() * height, Math.sin(ang) * width * 0.65);
			Fx.p("bolt_spark").at(pos).vel(randomUnit(r).scale(0.05)).life(6).spawn();
		}
		if (r.nextInt(2) == 0) {
			Vec3 p0 = feet.add((r.nextDouble() - 0.5) * width, r.nextDouble() * height, (r.nextDouble() - 0.5) * width);
			Vec3 p1 = p0.add(randomUnit(r).scale(0.5 + r.nextDouble() * 0.5));
			drawPolyline(jagged(p0, p1, r, 0.1, 3), 0.55f);
		}
		Fx.p("bolt_glow").at(feet.add(0, height * 0.5, 0)).size((float) width).life(2).alpha(0.3f).spawn();
	}

	// ------------------------------------------------------------------ impacts

	private static void impact(SpellType s, Vec3 c, Vec3 n, float radius, RandomSource r) {
		Vec3 normal = n.lengthSqr() < 1.0E-6 ? new Vec3(0, 1, 0) : n.normalize();
		if (radius < 1.0f) {
			fizzle(s, c, r);
			return;
		}
		switch (s) {
			case AQUA -> ClientEffects.start(ImpactFx.aqua(c, normal, radius, r));
			case LIGHTNING -> ClientEffects.start(ImpactFx.lightning(c, normal, radius, r));
			case INFERNO -> ClientEffects.start(ImpactFx.inferno(c, normal, radius, r));
			case EXPULSO -> ClientEffects.start(ImpactFx.expulso(c, normal, radius, r));
		}
	}

	private static void fizzle(SpellType s, Vec3 c, RandomSource r) {
		Fx.p("wand_glow").at(c).size(0.5f).life(5).color(s.secondary).spawn();
		for (int i = 0, n = Fx.n(10); i < n; i++) {
			Fx.p("wand_spark").at(c).vel(randomUnit(r).scale(0.12)).life(10).color(spellColor(s, r)).spawn();
		}
	}
}
