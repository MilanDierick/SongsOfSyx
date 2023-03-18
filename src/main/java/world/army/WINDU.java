package world.army;

import java.io.IOException;
import java.util.Arrays;

import game.time.TIME;
import init.boostable.BOOSTABLES;
import init.config.Config;
import init.race.RACES;
import init.race.Race;
import settlement.army.Div;
import settlement.entity.humanoid.Humanoid;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.StatsEquippables.EQUIPPABLE_MILITARY;
import settlement.stats.StatsEquippables.StatEquippableRange;
import settlement.stats.StatsTraits.StatTrait;
import snake2d.util.file.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.data.DataO;
import util.data.INT_O.INT_OE;
import world.entity.army.WArmy;

public class WINDU {

	private static WINDU self;
	public final StoredStat ageDays;
	public final StoredStat experience;
	public final StoredStat trainingM;
	public final StoredStat trainingR;
	
	public final LIST<StoredStat> all;
	public final int count;
	
	WINDU(){
		self = this;
		DataO<WInduStored> init = new DataO<WInduStored>() {
			@Override
			protected int[] data(WInduStored t) {
				return t.data;
			}
		};
		
		ageDays = new StoredStat(init, STATS.POP().AGE.indu(), Integer.MAX_VALUE);
		experience = new StoredStat(init, STATS.BATTLE().COMBAT_EXPERIENCE.indu());
		trainingM = new StoredStat(init, STATS.BATTLE().TRAINING_MELEE.indu());
		trainingR = new StoredStat(init, STATS.BATTLE().TRAINING_ARCHERY.indu());
		LIST<StoredStat> all = new ArrayList<>(
				ageDays,
				experience,
				trainingM,
				trainingR,
				new StoredStat(init, STATS.BATTLE().ENEMY_KILLS.indu())
				);
		ArrayList<StoredStat> traits = new ArrayList<>(STATS.TRAITS().all().size());
		for (StatTrait t : STATS.TRAITS().all()) {
			traits.add(new StoredStat(init, t.stat));
		}
		ArrayList<StoredStat> app = new ArrayList<>(STATS.APPEARANCE().all.size());
		for (INT_OE<Induvidual> t : STATS.APPEARANCE().all) {
			app.add(new StoredStat(init, t));
		}
		
		all = all.join(new StoredStat(init, STATS.EDUCATION().EDUCATION.indu()), new StoredStat(init, STATS.EDUCATION().INDOCTRINATION.indu()));
		
		INT_OE<Induvidual> rel = new INT_OE<Induvidual>() {
			
			@Override
			public int min(Induvidual t) {
				return 0;
			}
			
			@Override
			public int max(Induvidual t) {
				return STATS.RELIGION().ALL.size()-1;
			}
			
			@Override
			public int get(Induvidual t) {
				return STATS.RELIGION().getter.get(t).index();
			}
			
			@Override
			public void set(Induvidual t, int i) {
				STATS.RELIGION().getter.set(t, STATS.RELIGION().ALL.get(i));
			}
		};
		
		all = all.join(new StoredStat(init, rel), new StoredStat(init, STATS.RELIGION().TEMPLE_ACCESS.indu()), new StoredStat(init, STATS.RELIGION().TEMPLE_QUALITY.indu()));
		
		this.all = all.join(traits).join(app).join();
		
		
		
		count = init.intCount();
	}
	
	public static StoredStat ageDays() {
		return self.ageDays;
	}
	public static StoredStat experience(){
		return self.experience;
	}
	public static StoredStat trainingM() {
		return self.trainingM;
	}
	public static StoredStat trainingR() {
		return self.trainingR;
	}
	
	public static LIST<StoredStat> all(){
		return self.all;
	}
	
	public static WDivGeneration generate(WDIV d, WArmy army) {
		
		WDivGeneration res = new WDivGeneration(d.men());
		for (int i = 0; i < d.men(); i++) {
			WInduStored ii = new WInduStored();
			int a = (int) (BOOSTABLES.PHYSICS().DEATH_AGE.race(d.race())*TIME.years().bitConversion(TIME.days()));
			a -= d.race().physics.adultAt;
			a = d.race().physics.adultAt + RND.rInt(1 + a/2);
			ageDays().statSelf.set(ii, a);
			set(ii, experience(), d.experience());
			set(ii, trainingM(), d.training_melee());
			set(ii, trainingR(), d.training_ranged());
			res.indus[i] = ii;
			res.race = (short) d.race().index;
			res.bannerI = d.bannerI();
		}
		boolean isRange = false;
		if (d.needSupplies() && army != null) {
			for (EQUIPPABLE_MILITARY m : STATS.EQUIP().military_all()) {
				res.supplies[m.indexMilitary()] = WARMYD.supplies().get(m).getD(army)*d.equipTarget(m);
				if (m instanceof StatEquippableRange && res.supplies[m.indexMilitary()] > 0)
					isRange = true;
					
			}
		}else if (d.needSupplies() && army == null) {
			Arrays.fill(res.supplies, 0);
		}else if (!d.needSupplies()) {
			for (EQUIPPABLE_MILITARY m : STATS.EQUIP().military_all()) {
				res.supplies[m.indexMilitary()] = d.equipTarget(m);
			}
		}
		res.name = ""+d.name();
		res.isRange = isRange;
		return res;
	}
	
	private static void set(WInduStored ii, StoredStat s, double d) {
		d *= s.statStats.max(null);
		int a = (int) d;
		if (d-a > RND.rFloat())
			a++;
		a = CLAMP.i(a, 0, s.statStats.max(null));
		s.statSelf.set(ii, a);
	}
	
	public final static class WInduStored implements SAVABLE{
		
		final int[] data;	
		public long randomness;
		public long randomness2;
		private byte race;
		private byte popType;

		public WInduStored() {
			data = new int[self.count];
		}
		
		public WInduStored(WInduStored o) {
			data = new int[self.count];
			for (int i = 0; i < data.length; i++) {
				data[i] = o.data[i];
			}
			randomness = o.randomness;
			randomness2 = o.randomness2;
			race = o.race;
			popType = o.popType;
		}
		
		public WInduStored(Div div) {
			data = new int[self.count];
			randomness = RND.rLong();
			double a = (TIME.years().bitConversion(TIME.days())*BOOSTABLES.PHYSICS().DEATH_AGE.race(div.info.race()));
			self.ageDays.statSelf.set(this, (int) (a*0.2 + RND.rFloat()*a*0.3));
			self.experience.statSelf.set(this, div.info.experienceT.get());
			self.trainingM.statSelf.setD(this, div.info.training.toStat());
			self.trainingR.statSelf.setD(this, div.info.trainingR.toStat());
			race = (byte) div.info.race().index;
		}
		
		public WInduStored(Humanoid i) {
			data = new int[self.count];
			randomness = i.indu().randomness();
			randomness2 = i.indu().randomness2();
			for (StoredStat s : self.all) {
				s.copy(i.indu(), this);
			}
			race = (byte) i.race().index;
			popType = (byte) STATS.POP().TYPE.get(i.indu()).index;
		}
		
		public void paste(Humanoid i) {
			i.indu().randomness(randomness, randomness2);
			for (StoredStat s : self.all) {
				s.paste(i.indu(), this);
			}
			STATS.POP().TYPE.getByIndex(popType).set(i.indu());
		}

		public Race race() {
			return RACES.all().get(race);
		}
		
		@Override
		public void save(FilePutter file) {
			file.l(randomness);
			file.l(randomness2);
			file.is(data);
			file.b(race);
			file.b(popType);
		}


		@Override
		public void load(FileGetter file) throws IOException {
			randomness = file.l();
			randomness2 = file.l();
			file.is(data);
			race = file.b();
			popType = file.b();
		}


		@Override
		public void clear() {
			// TODO Auto-generated method stub
			
		}
		

		 
		
	}
	
	public static final class StoredStat {
		
		public final INT_OE<WInduStored> statSelf;
		private final INT_OE<Induvidual> statStats;
		
		StoredStat(DataO<WInduStored> count, INT_OE<Induvidual> stat){
			statStats = stat;
			statSelf = count.create(stat.max(null));
		}
		
		StoredStat(DataO<WInduStored> count, INT_OE<Induvidual> stat, int max){
			statStats = stat;
			statSelf = count.create(max);
		}
		
		public void copy(Induvidual a, WInduStored stored) {
			statSelf.set(stored, statStats.get(a));
		}
		
		public void paste(Induvidual a, WInduStored stored) {
			statStats.set(a, statSelf.get(stored));
		}
		
	}
	
	public static final class WDivGeneration {
		
		public final WInduStored[] indus;
		public final double[] supplies = new double[STATS.EQUIP().military_all().size()];
		public short race = 0;
		public String name;
		public boolean isRange;
		public int bannerI;
		
		public WDivGeneration(int men) {
			indus = new WInduStored[men];
			
			if (men > Config.BATTLE.MEN_PER_DIVISION)
				throw new RuntimeException();
		}
		
	}
	
}
