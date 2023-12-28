package world.army.util;

import game.boosting.BOOSTING;
import game.faction.Faction;
import init.paths.PATH;
import init.paths.PATHS;
import init.race.RACES;
import init.race.Race;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBattle.StatTraining;
import settlement.stats.equip.EquipBattle;
import snake2d.LOG;
import snake2d.util.file.Json;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import world.army.AD;

public class DivTypes {

	
	private final ArrayListGrower<DivType> types = new ArrayListGrower<>();
	private final double occMax;
	private final double[] occMaxs = new double[RACES.all().size()];
	
	DivTypes() {
		PATH p = PATHS.INIT().getFolder("battle").getFolder("divType");
		
		for (String f : p.getFiles()) {
			Json j = new Json(p.get(f));
			Json[] mm = j.jsons("TYPES");
			
			for (Json jj : mm) {
				double occ = jj.d("OCCURENCE");
				LIST<StatTraining> tr = STATS.BATTLE().TRAINING_MAP.getMany(jj);
				Json eqs = jj.json("EQUIPMENT");
				LIST<String> ekeys = eqs.keys();
				occ /= ekeys.size();
				for (String k : ekeys) {
					
					LIST<EquipBattle> eqps = STATS.EQUIP().militaryColl.getMany(k, eqs);
					types.add(new DivType(occ, tr, eqps));
				}
				
				
				
				
			}
		}
		
		double m = 0;
		for (DivType t : types)
			m += t.occurence;
		this.occMax = m;
		
		BOOSTING.connecter(new AA());
		
	}
	
	public DivType rnd(Race race, Faction f, double ran) {
		
		ran -= (int) ran;
		ran *= occMax;
		for (int i = 0; i < types.size(); i++) {
			ran -= types.get(i).occurence;
			if (ran <= 0)
				return types.get(i);
		}
		return types.get(0);
	}
	
	void debug() {
		for (DivType t : types) {
			LOG.ln(t.occurence);
			for (StatTraining tr : STATS.BATTLE().TRAINING_ALL)
				LOG.ln(tr.stats.info().name + " " + t.training(tr));
			for (EquipBattle e : STATS.EQUIP().BATTLE_ALL())
				LOG.ln(e.resource.name + " " + t.equip(e));
			
			for (Race r : RACES.all()) {
				LOG.ln(r.key + " " + t.roccurence[r.index()]/occMaxs[r.index()]);
			}
			LOG.ln();
		}
	}
	
	
	public LIST<DivType> ALL(){
		return types;
	}
	
	private class AA implements ACTION {

		private Race race;
		private DivType type;
		
		private DIV_STATS stats = new DIV_STATS() {
			
			@Override
			public double training(StatTraining tr) {
				return type.training(tr);
			}
			
			@Override
			public double equip(EquipBattle e) {
				return type.equip(e);
			}
			
			@Override
			public Race race() {
				return race;
			}
			
			@Override
			public int men() {
				return 10;
			}
			
			@Override
			public Faction faction() {
				return null;
			}
			
			@Override
			public double experience() {
				return 0.2;
			}
		};
		
		
		@Override
		public void exe() {
			for (DivType t : types) {
				type = t;
				for (int ri = 0; ri < RACES.all().size(); ri++) {
					race = RACES.all().get(ri);
					type.roccurence[ri] = type.occurence*AD.UTIL().power.get(stats);
					occMaxs[ri] += type.roccurence[ri];
				}
			}
			
			//debug();
		}
		
		
	}
	
}
