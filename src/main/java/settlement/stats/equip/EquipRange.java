package settlement.stats.equip;

import java.io.IOException;

import game.GAME;
import game.boosting.*;
import game.time.TIME;
import init.C;
import init.D;
import init.paths.PATH;
import init.sprite.UI.UI;
import settlement.army.Div;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.Induvidual;
import settlement.stats.StatsInit;
import settlement.stats.stat.*;
import settlement.thing.projectiles.*;
import snake2d.util.file.Json;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LISTE;
import util.data.INT_O.INT_OE;
import util.dic.*;
import util.gui.misc.GBox;
import util.info.GFORMAT;

public class EquipRange extends EquipBattle {

	public final STAT ammunition;
	private final INT_OE<Induvidual> ammoReplenishCount;
	public final Projectile projectile;
	public final int ammoMax;
	public final double ammoReplenishHours;
	public final short tIndex;

	private final double dt;
	private final int dtI;
	private double[] drawInters = new double[SETT.ARMIES().divisions().size()];
	private int[] drawIntersI = new int[SETT.ARMIES().divisions().size()];
	public final Boostable boostable;
	
	private static CharSequence ¤¤ammoC = "Ammunition (Current)";
	private static CharSequence ¤¤ammoR = "Am. Replenish Time (hours)";
	private static CharSequence ¤¤ammoU = "Unlimited ammunition!";
	static {
		D.ts(EquipRange.class);
	}

	EquipRange(String key, PATH path, LISTE<Equip> all, LISTE<EquipRange> type, LISTE<EquipBattle> mil, StatsInit init)
			throws IOException {
		super("RANGED", key, path, all, mil, init);
		tIndex = (short) type.add(this);
		Json data = new Json(path.get(key));

		ammoMax = data.i("AMMUNITION_AMOUNT", 1, 255);
		ammoReplenishHours = data.d("AMMUNITION_REPLENISH_TIME_HOURS");
		ammunition = new STATData(null, init, init.count.new DataByte(ammoMax),
				new StatInfo(DicArmy.¤¤Ammunition, DicArmy.¤¤Ammunition, DicArmy.¤¤AmmoDesc));
		ammoReplenishCount = init.count.new DataBit();

		boostable = BOOSTING.push("RANGED_" + key, 0.1, DicMisc.¤¤Skill + ": " + resource.name,
				DicMisc.¤¤Skill + ": " + resource.name, resource.icon(), BOOSTABLES.BATTLE());
		projectile = new Projectile.ProjectileImp(data);
		
		double time = ammoMax*2.0*(TIME.hoursPerDay/(ammoReplenishHours*16.0));
		dtI = (int) (time);
		
		dt = time-dtI;
	}

	private int upI = -1;
	private double bmax;

	public double ref(Induvidual a) {
		if (upI != GAME.updateI()) {
			bmax = 1.0 / boostable.max(Induvidual.class);
			upI = GAME.updateI();
		}
		return ref(stat.indu().getD(a), boostable.get(a) * bmax);
	}

	public double ref(Div div) {
		if (upI != GAME.updateI()) {
			bmax = 1.0 / boostable.max(Induvidual.class);
			upI = GAME.updateI();
		}
		return ref(stat.div().getD(div), boostable.get(div) * bmax);
	}

	public double ref(double equip, double skill) {
		return CLAMP.d(equip * (0.2 + skill * 0.8), 0, 1);
	}

	public void use(Induvidual t) {
		if (ammoReplenishHours <= 0)
			return;
		ammunition.indu().inc(t, -1);
		ammoReplenishCount.set(t, 0);
	}

	@Override
	public void set(Induvidual t, int i) {
		if (i > stat.indu().get(t)) {
			ammunition.indu().set(t, ammoMax);
		}
		super.set(t, i);
	}

	@Override
	void update16(Humanoid h, int updateI, int updateR, boolean day) {
		if (stat.indu().get(h.indu()) == 0)
			ammunition.indu().set(h.indu(), 0);
		else if (ammoReplenishHours <= 0) {
			ammunition.indu().set(h.indu(), ammoMax);
		} else if (!ammunition.indu().isMax(h.indu())){
			for (int i = 0; i < dtI; i++) {
				if (ammoReplenishCount.isMax(h.indu())) {
					ammunition.indu().inc(h.indu(), 1);
					ammoReplenishCount.set(h.indu(), 0);
				} else {
					ammoReplenishCount.inc(h.indu(), 1);
				}
			}
			if (dt > RND.rFloat()) {
				if (ammoReplenishCount.isMax(h.indu())) {
					ammunition.indu().inc(h.indu(), 1);
					ammoReplenishCount.set(h.indu(), 0);
				} else {
					ammoReplenishCount.inc(h.indu(), 1);
				}
			}
		}
		super.update16(h, updateI, updateR, day);
	}

	public void launch(Humanoid a, Trajectory j) {
		double ref = ref(a.indu());
		double ran = 1.0 - projectile.accuracy(ref);
		int x = a.body().cX() + a.speed.dir().x() * C.TILE_SIZEH;
		int y = a.body().cY() + a.speed.dir().y() * C.TILE_SIZEH;
		int h = SProjectiles.releaseHeight(a.tc().x(), a.tc().y());

		SETT.PROJS().launch(x, y, h, j, a.division().settings.ammo().projectile, ran, ref);
	}

	public double drawInter(Div div) {
		if ((GAME.updateI() & ~0b011) != drawIntersI[div.index()]) {
			double reloadSeconds = projectile.reloadSeconds(ref(div));
			double t = TIME.currentSecond();
			double inter = reloadSeconds;
			;
			double tt = t / inter;
			drawInters[div.index()] = tt - (int) tt;
			;
			drawIntersI[div.index()] = GAME.updateI() & ~0b011;
		}

		return drawInters[div.index()];
	}

	@Override
	public void hover(GUI_BOX box) {

		super.hover(box);
		GBox b = (GBox) box;
		b.sep();
		projectile.hover(box, resource.name);
		b.NL(8);
		if (ammoReplenishHours > 0) {
			b.textL(DicArmy.¤¤Ammunition);
			b.tab(6);
			b.add(GFORMAT.i(b.text(), ammoMax));
			b.tab(8);
			b.add(UI.icons().s.clock);
			b.add(GFORMAT.f(b.text(), ammoReplenishHours));
			b.text(DicTime.¤¤Hours);
		}
		b.NL(8);
	}

	private void hover(GUI_BOX box, double ref, double ammo) {

		
		GBox b = (GBox) box;
		b.sep();
		projectile.hover(box, resource.name, ref);
		b.NL(8);
		
		
		
		b.sep();
		
		if (ammoReplenishHours > 0) {
			
			b.textLL(¤¤ammoC);
			b.tab(7);
			b.add(GFORMAT.fofkInv(b.text(), ammo, ammoMax));
			b.NL();
			
			b.textL(¤¤ammoR);
			b.tab(7);
			b.add(GFORMAT.f(b.text(), ammoReplenishHours));

		}else {
			b.textLL(¤¤ammoU);
		}
	}

	@Override
	public void hover(GUI_BOX box, Div div) {
		super.hover(box, div);
		hover(box, ref(div), ammunition.div().getD(div)*ammunition.indu().max(null));

	}

	@Override
	public void hover(GUI_BOX box, Induvidual i) {
		super.hover(box, i);
		hover(box, ref(i), ammunition.indu().get(i));

	}

}