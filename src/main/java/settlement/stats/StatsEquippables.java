package settlement.stats;

import java.io.IOException;

import game.GAME;
import game.faction.FACTIONS;
import game.time.TIME;
import init.C;
import init.D;
import init.boostable.*;
import init.boostable.BBooster.BBoosterImp;
import init.config.Config;
import init.paths.PATH;
import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.army.ArmyManager;
import settlement.army.Div;
import settlement.entity.humanoid.HCLASS;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.main.*;
import settlement.stats.Init.Updatable;
import settlement.stats.STANDING.StandingDef;
import settlement.stats.STAT.StatInfo;
import settlement.thing.projectiles.Projectile;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.*;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.*;
import snake2d.util.sprite.text.Str;
import util.data.INT_O.INT_OE;
import util.dic.DicArmy;
import util.dic.DicMisc;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.info.INFO;
import util.rendering.ShadowBatch;

public final class StatsEquippables extends StatCollection implements Updatable {

	public final static String key = "EQUIP";
	private final ArrayList<EQUIPPABLE> all;
	private final ArrayList<StatEquippableBattle> military;
	private final LIST<EQUIPPABLE_MILITARY> military_all;
	private final ArrayList<StatEquippableRange> ammo;
	private final ArrayList<StatEquippableCivic> civic;
	private final ArrayList<StatEquippableWork> work;
	private final LIST<STAT> stats;
	public final EQUIPPABLE CLOTHES;
	private static CharSequence ¤¤Level = "¤{0} Level";
	private static CharSequence ¤¤Target = "¤{0} Target";
	private static CharSequence ¤¤Level_desc = "¤The target number of items each individual should equip. Special cases for this is tools, which is set at each industry. Weapons are also set separately for each division.";
	public static CharSequence ¤¤WearRate = "¤Wear Rate";
	public static CharSequence ¤¤WearRateD = "¤The rate at which each equipped item is worn out.";
	private static CharSequence ¤¤more = "would like to be allowed more {0}.";
	
	private static CharSequence ¤¤Equipped = "Equipped: {0}";
	static {
		D.ts(StatsEquippables.class);
	}
	
	StatsEquippables(Init init) {
		super(init, key);
		
		LinkedList<EQUIPPABLE> all = new LinkedList<>();
		
		PATH data = init.pd.getFolder("equip");
		
		{
			LinkedList<StatEquippableCivic> tmp = new LinkedList<>();
			PATH d = data.getFolder("civic");
			this.CLOTHES = new StatEquippableCivic("_CLOTHES", d, all, tmp, init);
			for (String k : d.getFiles()) {
				new StatEquippableCivic(k, d, all, tmp, init);
			}
			this.civic = new ArrayList<>(tmp);
		}
		
		{
			LinkedList<StatEquippableWork> tmp = new LinkedList<>();
			PATH d = data.getFolder("work");
			for (String k : d.getFiles()) {
				new StatEquippableWork(k, d, all, tmp, init);
			}
			this.work = new ArrayList<>(tmp);
		}
		
		LinkedList<EQUIPPABLE_MILITARY> mil = new LinkedList<>();
		{
			LinkedList<StatEquippableBattle> tmp = new LinkedList<>();
			PATH d = data.getFolder("battle");
			for (String k : d.getFiles()) {
				new StatEquippableBattle(k, d, all, tmp, mil, init);
			}
			this.military = new ArrayList<>(tmp);
		}
		
		{
			LinkedList<StatEquippableRange> tmp = new LinkedList<>();
			PATH d = data.getFolder("battle_ranged");
			for (String k : d.getFiles()) {
				new StatEquippableRange(k, d, all, tmp, mil, init);
			}
			this.ammo = new ArrayList<>(tmp);
		}
		
		this.military_all = new ArrayList<EQUIPPABLE_MILITARY>(mil);
		
		this.all = new ArrayList<>(all);

		D.t(this);

		init.updatable.add(this);

		stats = makeStats(init);
		
	}
	
	public void drop(Humanoid h) {
		
		for (EQUIPPABLE e : all) {
			int a = Math.round(e.stat().indu().get(h.indu())*RND.rFloat());
			if (a > 0) {
				SETT.THINGS().resources.create(h.physics.tileC(), e.resource(), a);
			}
		}
	}

	@Override
	public void update16(Humanoid h, int updateI, boolean day, int ui) {
		for (EQUIPPABLE t : all) {
			if (t instanceof StatEquippableAbs)
				((StatEquippableAbs)t).update16(h, updateI, updateI, day);
		}
	}

	public static abstract interface EQUIPPABLE extends INDEXED{
		
		public abstract STAT stat();
		public abstract LIST<BBoosterImp> boosts();
		public abstract int target(Humanoid h);
		public abstract int max(Induvidual i);
		public abstract double wearRate();
		public abstract int arrivalAmount();
		public abstract RESOURCE resource();
		public abstract void set(Induvidual t, int i);
		public default void inc(Induvidual t, int am) {
			set(t, stat().indu().get(t) + am);
		}
		
		public void hover(GUI_BOX text);
		public void hover(GUI_BOX box, Induvidual h);
		public void hover(GUI_BOX box, HCLASS cl, Race r);
	}
	
	public static abstract interface EQUIPPABLE_MILITARY extends EQUIPPABLE{
		
		public int target(Div d);
		public void targetSet(Div e, int t);
		public int max();
		public int indexMilitary();
		public void hover(GUI_BOX box, Div div);
		public int garrisonAmount();
		public int guardAmount();
	}
	
	
	private static abstract class StatEquippableAbs implements EQUIPPABLE {

		public final CharSequence sTarget;
		public final INFO targetInfo;
		protected final String key;
		
		public final RESOURCE resource;
		private final int index;
		public final double wearRate;
		private final int wearRateI;
		public final int equipMax;
		public final int arrivalAmount;
		public final int targetDefault;
		
		final STAT stat;
		final INT_OE<Induvidual> counter;
		
		StatEquippableAbs(String key, PATH path, LISTE<EQUIPPABLE> all, Init init) {
			this.key = key;
			Json data = new Json(path.get(key));
			index = all.add(this);
			resource = RESOURCES.map().get(data);
			wearRate = data.d("WEAR_RATE", 0, 100);
			equipMax = data.i("MAX_AMOUNT", 1, 15);
			arrivalAmount = data.i("ARRIVAL_AMOUNT", 0, equipMax);
			targetDefault = data.i("DEFAULT_TARGET");
			StandingDef standing = new StandingDef(data);
			
			wearRateI = (int) (Math.ceil(wearRate * 16));
			sTarget = new Str(¤¤Target).insert(0, resource.name).trim();
			{
				
				targetInfo = new INFO(new Str(¤¤Level).insert(0, resource.name).trim(), ¤¤Level_desc);
			}
			stat = new STAT.STATData(key, init, init.count.new DataNibble(equipMax), new StatInfo(resource.name, resource.names, resource.desc), standing);
			stat.info().setInt();
			counter = init.count.new DataNibble();
			
			LIST<BBoost> bb = BOOSTABLES.boosts(data);
			
			for (BBoost b : bb) {
				stat.boosts.add(new SBoost(init, this, b));
			}
			
		}

		@Override
		public void set(Induvidual t, int i) {
			if (i != stat.indu().get(t)) {
				if (i == 0)
					counter.set(t, 0);
				stat.indu().set(t, CLAMP.i(i, 0, max(t)));
			}
		}

		@Override
		public int index() {
			return index;
		}

		void update16(Humanoid h, int updateI, int updateR, boolean day) {

			if ((updateR) < wearRateI) {
				Induvidual i = h.indu();
				int c = counter.get(h.indu());
				c -= stat.indu().get(i);
				if (c < 0) {
					set(i, stat.indu().get(i) - 1);
					FACTIONS.player().res().outConsumed.inc(resource, 1);
					c += counter.max(i) + 1;
				}
				counter.set(i, c);
			}
		}

		@Override
		public STAT stat() {
			return stat;
		}
		
		protected abstract double value(Induvidual v);
		
		protected abstract double value(HCLASS c, Race r);
		
		protected abstract double value(Div v);

		@Override
		public LIST<BBoosterImp> boosts() {
			return stat.boosts();
		}

		@Override
		public double wearRate() {
			return wearRate;
		}

		@Override
		public int arrivalAmount() {
			return arrivalAmount;
		}

		@Override
		public RESOURCE resource() {
			return resource;
		}
		
		@Override
		public void hover(GUI_BOX box) {
			GBox b = (GBox) box;
			box.title(resource.name);
			b.textL(¤¤WearRate);
			b.tab(7);
			b.add(GFORMAT.f0(b.text(), -wearRate*16/TIME.years().bitConversion(TIME.days())));
			b.add(b.text().add('/').add(TIME.years().cycleName()));
			b.NL();
			b.text(¤¤WearRateD);
			b.NL(8);
			if (boosts().size() > 0) {
				for (BBooster s : boosts()) {
					s.boost.hover(b);
					b.NL();
				}
			}
		}
		
		@Override
		public void hover(GUI_BOX box, HCLASS cl, Race r) {
			GBox b = (GBox) box;
			box.title(resource.name);
			b.textL(¤¤WearRate);
			b.tab(7);
			b.add(GFORMAT.f0(b.text(), -wearRate*16/TIME.years().bitConversion(TIME.days())));
			b.add(b.text().add('/').add(TIME.years().cycleName()));
			b.NL();
			b.text(¤¤WearRateD);
			b.NL(8);
			for (BBoosterImp s : boosts()) {
				s.boost.hoverValue(b, s.pvalue(cl, r));
				b.NL();
			}
		}
		
		@Override
		public void hover(GUI_BOX box, Induvidual h) {
			GBox b = (GBox) box;
			box.title(resource.name);
			
			GText t = b.text();
			t.setFont(UI.FONT().M);
			b.add(GFORMAT.iofkInv(t, stat.indu().get(h), equipMax))
			.NL(2);
			
			b.textL(¤¤WearRate);
			b.tab(7);
			b.add(GFORMAT.f0(b.text(), -wearRate*16/TIME.years().bitConversion(TIME.days())));
			b.add(b.text().add('/').add(TIME.years().cycleName()));

			b.NL(8);
			for (BBoosterImp s : boosts()) {
				s.boost.hoverValue(b, s.pvalue(h));
				b.NL();
			}
			
		}
		
	}
	
	public static class StatEquippableCivic extends StatEquippableAbs {
		
		private final int[][] tars = new int[HCLASS.ALL().size()][RACES.all().size()];
		
		StatEquippableCivic(String key, PATH path, LISTE<EQUIPPABLE> all, LISTE<StatEquippableCivic> type, Init init) {
			super(key, path, all, init);
			type.add(this);
			SAVABLE s = new SAVABLE() {

				@Override
				public void save(FilePutter file) {
					file.isE(tars);
				}

				@Override
				public void load(FileGetter file) throws IOException {
					file.isE(tars);
				}

				@Override
				public void clear() {
					for (int[] is : tars) {
						for (int i = 0; i < is.length; i++)
							is[i] = targetDefault;
					}
				}
			};
			
			s.clear();
			
			init.savables.add(s);
			stat.info().setOpinion(¤¤more, null);
			
		}

		@Override
		public int target(Humanoid h) {
			return CLAMP.i(tars[h.indu().hType().CLASS.index()][h.indu().race().index], 0, max());
		}
		
		public int target(HCLASS c, Race type) {
			if (type == null) {
				int m = 0;
				for (int ri = 0; ri < RACES.all().size(); ri++) {
					Race r = RACES.all().get(ri);
					m = Math.max(m, target(c, r));
				}
				return m;
			}
			return CLAMP.i(tars[c.index()][type.index], 0, max());
		}
		
		public void targetSet(int target, HCLASS c, Race type) {
			if (type == null) {
				for (int ri = 0; ri < RACES.all().size(); ri++) {
					Race r = RACES.all().get(ri);
					targetSet(target, c, r);
				}
				return;
			}
			target = CLAMP.i(target, 0, equipMax);
			tars[c.index()][type.index] = target;
		}
		
		
		public int max() {
			return equipMax;
		}
		
		@Override
		public double value(HCLASS c, Race type) {
			return stat.data(c).getD(type);
		}

		@Override
		protected double value(Induvidual v) {
			return stat.indu().getD(v);
		}

		@Override
		protected double value(Div v) {
			return stat.div().getD(v);
		}

		@Override
		public int max(Induvidual i) {
			return equipMax;
		}


	
		
	}
	
	public static class StatEquippableWork extends StatEquippableAbs {
		
		private final int[] tars = new int[SETT.ROOMS().employment.ALLS().size()];
		private final int[] max = new int[SETT.ROOMS().employment.ALLS().size()];
		public final int tIndex;
		public final double maxBoost;
		
		StatEquippableWork(String key, PATH path, LISTE<EQUIPPABLE> all, LISTE<StatEquippableWork> type,  Init init) {
			super(key, path, all, init);
			tIndex = type.add(this);
			
			SAVABLE s = new SAVABLE() {

				@Override
				public void save(FilePutter file) {
					file.isE(tars);
				}

				@Override
				public void load(FileGetter file) throws IOException {
					file.isE(tars);
				}

				@Override
				public void clear() {
					for (int i = 0; i < tars.length; i++)
						tars[i] = targetDefault;
				}
			};
			
			double m = 0;
			for (BBooster b : boosts()) {
				if (b.boost.boostable instanceof BOOSTABLERoom) {
					
					BOOSTABLERoom r = (BOOSTABLERoom) b.boost.boostable;
					if (r.room instanceof RoomBlueprintIns<?>) {
						if (b.boost.value() > m)
							m = b.boost.value();
					}
					
				}
				
			}
			maxBoost = m;
			for (BBooster b : boosts()) {
				if (b.boost.boostable instanceof BOOSTABLERoom) {
				
					BOOSTABLERoom r = (BOOSTABLERoom) b.boost.boostable;
					if (r.room instanceof RoomBlueprintIns<?>) {
						RoomBlueprintIns<?> ri = (RoomBlueprintIns<?>) r.room;
						int am = (int) Math.ceil(equipMax*b.boost.value()/m);
						max[ri.employment().eindex()] = am;
					}
					
				}
				
			}
			
			
			s.clear();
			
			init.savables.add(s);
		}

		@Override
		public int target(Humanoid h) {
			RoomInstance i = STATS.WORK().EMPLOYED.get(h);
			if (i == null)
				return 0;
			RoomEmploymentSimple e = i.blueprintI().employment();
			return target(e);
		}
		
		public int target(RoomEmploymentSimple e) {
			return CLAMP.i(tars[e.eindex()], 0, max(e));
		}
		
		public int max(RoomEmploymentSimple e) {
			if (e == null)
				return 0;
			return max[e.eindex()];
		}
		
		public double maxBoost(RoomEmploymentSimple e) {
			if (e == null)
				return 0;
			return maxBoost*max[e.eindex()]/equipMax;
		}
		
		public void targetSet(RoomEmploymentSimple e, int t) {
			
			tars[e.eindex()] = CLAMP.i(t, 0, max(e));
		}
		
		@Override
		protected double value(Induvidual v) {
			RoomInstance i = STATS.WORK().EMPLOYED.get(v);
			if (i == null)
				return 0;
			if (max[i.blueprintI().employment().eindex()] == 0)
				return 0;
			return stat.indu().get(v)/(double)max[i.blueprintI().employment().eindex()];
		}

		@Override
		protected double value(HCLASS c, Race r) {
			return 0;
		}

		@Override
		protected double value(Div v) {
			return 0;
		}

		@Override
		public int max(Induvidual v) {
			RoomInstance i = STATS.WORK().EMPLOYED.get(v);
			if (i == null)
				return 0;
			return tars[i.blueprintI().employment().eindex()];
		}
		
		@Override
		public void hover(GUI_BOX box, Induvidual h) {
			GBox b = (GBox) box;
			box.title(resource.name);
			
			GText t = b.text();
			t.setFont(UI.FONT().M);
			b.add(GFORMAT.iofkInv(t, stat.indu().get(h), equipMax))
			.NL(2);
			
			b.textL(¤¤WearRate);
			b.tab(7);
			b.add(GFORMAT.f0(b.text(), -wearRate*16/TIME.years().bitConversion(TIME.days())));
			b.add(b.text().add('/').add(TIME.years().cycleName()));
			b.NL(8);
			
			if (STATS.WORK().EMPLOYED.get(h) != null) {
				b.NL();
				for (BBooster s : boosts()) {
					if (s.boost.boostable instanceof BOOSTABLERoom && ((BOOSTABLERoom)s.boost.boostable).room == STATS.WORK().EMPLOYED.get(h).blueprintI())
						s.boost.hoverValue(b, maxBoost);
						b.NL();
				}
			}
			
			
		}
		
	}
	
	public static class StatEquippableBattle extends StatEquippableAbs implements EQUIPPABLE_MILITARY{
		
		private final int[] tars = new int[ArmyManager.ARMIES*Config.BATTLE.DIVISIONS_PER_ARMY];
		private final int iMil;
		public final int amountInGarrison;
		public final int amountGuard;
		
		StatEquippableBattle(String key, PATH path, LISTE<EQUIPPABLE> all, LISTE<StatEquippableBattle> type, LISTE<EQUIPPABLE_MILITARY> mil, Init init) {
			this(key, path, all, mil, init);
			type.add(this);
			
		}
		
		StatEquippableBattle(String key, PATH path, LISTE<EQUIPPABLE> all, LISTE<EQUIPPABLE_MILITARY> mil, Init init) {
			super(key, path, all, init);
			iMil = mil.add(this);
			
			SAVABLE s = new SAVABLE() {

				@Override
				public void save(FilePutter file) {
					file.isE(tars);
				}

				@Override
				public void load(FileGetter file) throws IOException {
					file.isE(tars);
				}

				@Override
				public void clear() {
					for (int i = 0; i < tars.length; i++)
						tars[i] = targetDefault;
				}
			};
			
			s.clear();
			amountInGarrison = new Json(path.get(key)).i("AMOUNT_IN_GARRISON", 0, equipMax);
			amountGuard = new Json(path.get(key)).i("EQUIP_GUARDS", 0, equipMax);
			init.savables.add(s);
			stat.info().setMatters(false, true);
		}

		@Override
		public int target(Humanoid h) {
			Div i = STATS.BATTLE().DIV.get(h);
			if (i == null || !i.settings.mustering())
				return 0;
			return target(i);
		}
		
		@Override
		public double value(HCLASS c, Race type) {
			return stat.data(c).getD(type);
		}

		@Override
		protected double value(Induvidual v) {
			return stat.indu().getD(v);
		}

		@Override
		protected double value(Div v) {
			return stat.div().getD(v);
		}

		@Override
		public int max(Induvidual i) {
			return equipMax;
		}


		@Override
		public int target(Div d) {
			return CLAMP.i(tars[d.index()], 0, equipMax);
		}
		
		@Override
		public void targetSet(Div d, int t) {
			tars[d.index()] = CLAMP.i(t, 0, equipMax);
		}
		
		@Override
		public int max() {
			return equipMax;
		}

		@Override
		public int indexMilitary() {
			return iMil;
		}

		@Override
		public void hover(GUI_BOX box, Div div) {
			GBox b = (GBox) box;
			box.title(resource.name);
			
			GText t = b.text();
			t.setFont(UI.FONT().M);
			b.add(GFORMAT.fofkInv(t, stat.div().getD(div)*equipMax, equipMax))
			.NL(2);
			
			b.textL(¤¤WearRate);
			b.tab(7);
			b.add(GFORMAT.f0(b.text(), -wearRate*16/TIME.years().bitConversion(TIME.days())));
			b.add(b.text().add('/').add(TIME.years().cycleName()));

			b.NL(8);
			if (boosts().size() > 0) {
				b.NL();
				for (BBoosterImp s : boosts()) {
					s.boost.hoverValue(b, s.pvalue(div));
					b.NL();
				}
			}
			
		}

		@Override
		public int garrisonAmount() {
			return amountInGarrison;
		}

		@Override
		public int guardAmount() {
			return amountGuard;
		}
		
	}
	
	public static class StatEquippableRange extends StatEquippableBattle{
		
		
		public final STAT ammunition;
		private final INT_OE<Induvidual> ammoReplenishCount;
		public final Projectile projectile;
		public final int ammoMax;
		public final double ammoReplenishHours;
		private final double repCount;
		public final short tIndex;
		
		private final AStat TILE_SPEED;
		private final AStat RELOAD_PER_MINUTE;
		private final AStat ACCURACY;
		private final AStat PIERCE_DAMAGE;
		private final AStat MASS;
		private final AStat TILE_RADIUS_DAMAGE;
		
		public final LIST<AStat> stats;
		
		private double[] drawInters = new double[SETT.ARMIES().divisions().size()];
		private int[] drawIntersI = new int[SETT.ARMIES().divisions().size()];
		
		
		StatEquippableRange(String key, PATH path, LISTE<EQUIPPABLE> all, LISTE<StatEquippableRange> type, LISTE<EQUIPPABLE_MILITARY> mil, Init init) {
			super(key, path, all, mil, init);
			tIndex = (short) type.add(this);
			Json data = new Json(path.get(key));
			projectile = new Proj(data, equipMax);
			
			ammoMax = data.i("AMMUNITION_AMOUNT", 1, 255);
			ammoReplenishHours = data.d("AMMUNITION_REPLENISH_TIME_HOURS");
			ammunition = new STAT.STATData(null, init, init.count.new DataByte(ammoMax), new StatInfo(DicArmy.¤¤Ammunition, DicArmy.¤¤Ammunition, DicArmy.¤¤AmmoDesc), null);
			ammoReplenishCount = init.count.new DataBit();
			repCount = 2.0*16/TIME.hoursPerDay;
			
			TILE_SPEED = new AStat(BOOSTABLES.PHYSICS().SPEED.name, data, "TILE_SPEED", equipMax);
			RELOAD_PER_MINUTE = new AStat(DicArmy.¤¤ReloadTime, data, "RELOAD_PER_MINUTE", equipMax);
			ACCURACY = new AStat(DicMisc.¤¤Accuracy,  data, "ACCURACY", equipMax);
			PIERCE_DAMAGE = new AStat(BOOSTABLES.BATTLE().PIERCE_DAMAGE.name, data, "PIERCE_DAMAGE", equipMax);
			MASS = new AStat(BOOSTABLES.PHYSICS().MASS.name, data, "MASS", equipMax);
			TILE_RADIUS_DAMAGE = new AStat(DicArmy.¤¤SplashDamage, data, "TILE_RADIUS_DAMAGE", equipMax);
			stats = new ArrayList<StatsEquippables.StatEquippableRange.AStat>(
					TILE_SPEED, RELOAD_PER_MINUTE,ACCURACY,
					PIERCE_DAMAGE,
					MASS,
					TILE_RADIUS_DAMAGE
					);
		}
		
		

		private class Proj extends Projectile{
			
			private final COLOR[] cols = new COLOR[64];
			
			Proj(Json data, int equipMax){
				super(data);
				{
					COLOR col = new ColorImp(data.json("COLOR"));
					for (int i = 0; i < cols.length; i++) {
						cols[i] = new ColorImp(CLAMP.i(col.red()+RND.rInt(5), 0, 128), CLAMP.i(col.green()+RND.rInt(5), 0, 127), CLAMP.i(col.blue()+RND.rInt(5), 0, 127)).shadeSelf(RND.rFloat1(0.5));
					}
				}
			}
			
			@Override
			public void render(Renderer r, ShadowBatch s, double x, double y, int h, int ran, double dx, double dy, double dz, float ds,
					int zoomout) {
				if (zoomout < 2) {
					cols[ran&0b0111111].bind();
					double l = Math.sqrt(dx*dx+dy*dy+dz*dz*4);
					dx /= l;
					dy /= l;
					dx*= C.SCALE;
					dy*= C.SCALE;
					for (int k = 0; k < 8; k++) {
						r.renderParticle((int)x, (int)(y));
						x += dx;
						y += dy;
					}
				}
				
				s.setDistance2Ground(h/4);
				SPRITES.icons().s.dot.renderC(s, (int)x, (int)y);
			}
			
			@Override
			public double mass(int level) {
				return MASS.level[level];
			}
			
			@Override
			public double pierce(int level) {
				return PIERCE_DAMAGE.level[level];
			}
			
			@Override
			public double areaAttack(int level) {
				return TILE_RADIUS_DAMAGE.level[level];
			}
			
		}
		
		public static class AStat {
			
			public final double[] level;
			public final double boost;
			public CharSequence name;
			
			AStat(CharSequence name, Json data, String key, int equipMax){
				level = makeLevel(data, key, equipMax);
				boost = data.json("EXTRA_FROM_TRAINING").dTry(key, 0, 10000, 0);
				this.name = name;
			}
			
			private double[] makeLevel(Json data, String key, int equipMax) {
				double from = data.json("PROJECTILE").d(key);
				double to = data.json("LEVEL_MAX").dTry(key, 0, 10000, from);
				int l = equipMax;
				double[] res = new double[32];
				for (int i = 1; i <= l; i++) {
					double d = (double)(i)/(equipMax);
					d = Math.pow(d, 0.75);
					res[i] = from + (to-from)*d;
				}
				return res;
			}
			
			public double get(int level, Induvidual i) {
				return this.level[level] + boost*BOOSTABLES.BATTLE().RANGED_SKILL.get(i);
			}
			
			public double get(int level, Div div) {
				return this.level[level] + boost*BOOSTABLES.BATTLE().RANGED_SKILL.get(div);
			}
			
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
			}else if(2.0*repCount > RND.rFloat()*ammoReplenishHours){
				if (ammoReplenishCount.isMax(h.indu())) {
					ammunition.indu().inc(h.indu(), ammoMax/2);
					ammoReplenishCount.set(h.indu(), 0);
				}else {
					ammoReplenishCount.inc(h.indu(), 1);
				}
			}
			super.update16(h, updateI, updateR, day);
		}
		
		public double accuracyRND(Induvidual i) {
			int l = stat.indu().get(i);
			return 1.0-CLAMP.d(ACCURACY.get(l, i), 0, 1);
		}
		
		public double drawInter(Div div) {
			if ((GAME.updateI() & ~0b011) != drawIntersI[div.index()]) {
				double reloadSeconds = 60.0/(get(div, RELOAD_PER_MINUTE));
				double t = TIME.currentSecond();
				double inter = reloadSeconds;;
				double tt = t/inter;
				drawInters[div.index()] = tt-(int)tt;;
				drawIntersI[div.index()] = GAME.updateI() & ~0b011;
			}
			
			return drawInters[div.index()];
		}
		
		private double get(Div div, AStat st) {
			double d = stat.div().getD(div)*equipMax;
			int l = (int) d;
			d -= l;
			double s = st.level[l]*(1.0-d);
			if (l < equipMax)
				s += st.level[l+1]*d;
			s += BOOSTABLES.BATTLE().RANGED_SKILL.get(div)*st.boost;
			return s;
		}
		
		public double speed(Div div) {
			return get(div, TILE_SPEED)*C.TILE_SIZE;
		}
		
		@Override
		public void hover(GUI_BOX box, Div div) {
			super.hover(box, div);
			GBox b = (GBox) box;
			
			if (ammoReplenishHours > 0) {
				b.textL(DicArmy.¤¤Ammunition);
				b.add(GFORMAT.iofkInv(b.text(), (int)(ammunition.div().getD(div)*ammunition.dataDivider()), ammoMax));
				b.NL(8);
			}
			
			b.tab(6);
			b.textLL(DicMisc.¤¤Equipped);
			b.tab(9);
			b.textLL(BOOSTABLES.BATTLE().RANGED_SKILL.name);
			b.NL();
			double l = stat.div().getD(div)*max();
			double s = BOOSTABLES.BATTLE().RANGED_SKILL.get(div);
			stat(s, b, TILE_SPEED, l);
			stat(s, b, RELOAD_PER_MINUTE, l);
			stat(s, b, ACCURACY, l);
			stat(s, b, PIERCE_DAMAGE, l);
			stat(s, b, MASS, l);
			stat(s, b, TILE_RADIUS_DAMAGE, l);
			
		}
		
		@Override
		public void hover(GUI_BOX box, Induvidual i) {
			super.hover(box, i);
			GBox b = (GBox) box;
			
			if (ammoReplenishHours > 0) {
				b.textL(DicArmy.¤¤Ammunition);
				b.add(GFORMAT.iofkInv(b.text(), (int)(ammunition.indu().get(i)), ammoMax));
				b.NL(8);
			}
			
			b.tab(6);
			b.textLL(DicMisc.¤¤Equipped);
			b.tab(9);
			b.textLL(BOOSTABLES.BATTLE().RANGED_SKILL.name);
			b.NL();
			double l = stat.indu().get(i);
			double s = BOOSTABLES.BATTLE().RANGED_SKILL.get(i);
			stat(s, b, TILE_SPEED, l);
			stat(s, b, RELOAD_PER_MINUTE, l);
			stat(s, b, ACCURACY, l);
			stat(s, b, PIERCE_DAMAGE, l);
			stat(s, b, MASS, l);
			stat(s, b, TILE_RADIUS_DAMAGE, l);
			
		}
		
		private void stat(double skill, GBox b, AStat s, double level) {
			b.textL(s.name);
			b.tab(6);
			double d = (level-(int)level);
			b.add(GFORMAT.fofkInv(b.text(), s.level[(int)level]*(1.0-d)+s.level[(int)level+1]*d, s.level[equipMax]));
			b.tab(9);
			b.add(GFORMAT.f0(b.text(), skill*s.boost));
			b.NL();
		}
		
	}
	
	static class SBoost extends BBooster.BBoosterImp {

		private final StatEquippableAbs s;
		
		SBoost(Init init, StatEquippableAbs trait, BBoost b) {
			super(
					new Str(¤¤Equipped).insert(0, trait.resource.names),
					b, true, true, false);
			this.s = trait;
		}

		@Override
		public double pvalue(Induvidual v) {
			return s.value(v);
		}

		@Override
		public double pvalue(HCLASS c, Race r) {
			return s.value(c, r);
		}

		@Override
		public double pvalue(Div v) {
			return s.value(v);
		}

	}

	@Override
	public LIST<STAT> all() {
		return stats;
	}

	public LIST<EQUIPPABLE> allE() {
		return all;
	}
	
	public LIST<StatEquippableCivic> civics() {
		return civic;
	}
	
	public LIST<StatEquippableBattle> military() {
		return military;
	}
	
	public LIST<StatEquippableRange> ammo() {
		return ammo;
	}
	
	public LIST<EQUIPPABLE_MILITARY> military_all() {
		return military_all;
	}
	
	public LIST<StatEquippableWork> work() {
		return work;
	}

}
