package world.army;

import java.io.IOException;

import game.faction.Faction;
import init.config.Config;
import init.race.RACES;
import init.race.Race;
import settlement.entity.humanoid.HTYPE;
import settlement.main.SETT;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBattle.StatTraining;
import settlement.stats.equip.EquipBattle;
import settlement.stats.equip.EquipRange;
import settlement.stats.stat.STAT;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LIST;
import world.army.util.DIV_STATS;

public final class WDivGeneration{

	public final Induvidual[] indus;
	// public final double[] supplies = new
	// double[STATS.EQUIP().BATTLE_ALL().size()];
	public final short race;
	public final CharSequence name;
	public final int bannerI;
	public final boolean isRange;

	public WDivGeneration(DIV_STATS div, CharSequence name, int bannerI) {
		indus = new Induvidual[div.men()];
		race = (short) div.race().index();
		this.name = "" + name;
		this.bannerI = bannerI;
		if (div.men() > Config.BATTLE.MEN_PER_DIVISION)
			throw new RuntimeException();

		for (int i = 0; i < indus.length; i++) {
			Induvidual ii = new Induvidual(HTYPE.SUBJECT, div.race());
			init(div, ii);
			indus[i] = ii;

		}
		isRange = range(div);
	}

	public WDivGeneration() {
	
		Race race = RACES.all().rnd(); 
		
		this.race = (short) race.index();
		indus = new Induvidual[CLAMP.i(RND.rInt(Config.BATTLE.MEN_PER_DIVISION)+25, 1, Config.BATTLE.MEN_PER_DIVISION)];
		this.name = race.info.armyNames.rnd();
		
		this.bannerI = RND.rInt(SETT.ARMIES().banners.size());
		
		DIV_STATS stats = new DIV_STATS() {
			
			double tr = RND.rFloat();
			double eq = RND.rFloat();
			double ex = RND.rFloat();
			
			@Override
			public double training(StatTraining t) {
				return tr;
			}
			
			@Override
			public double equip(EquipBattle e) {
				return eq;
			}
			
			@Override
			public Race race() {
				return race;
			}
			
			@Override
			public int men() {
				return indus.length;
			}
			
			@Override
			public Faction faction() {
				return null;
			}
			
			@Override
			public double experience() {
				return ex;
			}
		};
		

		for (int i = 0; i < indus.length; i++) {
			Induvidual ii = new Induvidual(HTYPE.SUBJECT, race);
			init(stats, ii);
			indus[i] = ii;

		}
		isRange = range(stats);
	}
	
	public WDivGeneration(DIV_STATS div,  CharSequence name, int bannerI, LIST<Induvidual> all) {
		indus = new Induvidual[div.men()];
		race = (short) div.race().index();
		this.name = "" + name;
		this.bannerI = bannerI;

		for (int i = 0; i < indus.length; i++) {
			indus[i] = all.get(i);
			init(div, indus[i]);
		}
		isRange = range(div);
	}
	
	private static boolean range(DIV_STATS div) {
		for(EquipRange r : STATS.EQUIP().RANGED())
			if (div.equip(r) > 0)
				return true;
		return false;
	}
	
	
	public WDivGeneration(WDIV div) {
		this(div, div.name(), div.bannerI());
		
	}

	public WDivGeneration(WDIV div, LIST<Induvidual> all) {
		this(div, div.name(), div.bannerI(), all);
	}
	
	public WDivGeneration(FileGetter file) throws IOException{
		race = (short) file.i();
		name = file.chars();
		bannerI = file.i();
		isRange = file.bool();
		
		indus = new Induvidual[file.i()];
		for (int i = 0; i < indus.length; i++)
			indus[i] = new Induvidual(file);
		
	}
	
	public void save(FilePutter file) {
		
		file.i(race);
		file.chars(name);
		file.i(bannerI);
		file.bool(isRange);
		
		file.i(indus.length);
		for (Induvidual a : indus)
			a.save(file);
	}
	
	public Race race() {
		return RACES.all().get(race);
	}
	
	private void init(DIV_STATS div, Induvidual ii) {
		set(ii, STATS.BATTLE().COMBAT_EXPERIENCE, div.experience());
		for (StatTraining tt : STATS.BATTLE().TRAINING_ALL)
			set(ii, tt, div.training(tt));

		for (EquipBattle e : STATS.EQUIP().BATTLE_ALL()) {
			set(ii, e.stat(), div.equip(e));
		}
	}



	private static void set(Induvidual ii, STAT s, double d) {

		double ex = d * s.indu().max(ii);
		if (ex != (int) ex && (ex - (int) ex) > RND.rFloat())
			ex++;
		ex = CLAMP.d(ex, 0, s.indu().max(ii));

		s.indu().set(ii, (int) ex);

	}



}