package init.race;

import java.util.Arrays;
import java.util.HashMap;

import game.boosting.*;
import game.faction.Faction;
import game.faction.npc.NPCBonus;
import game.faction.npc.ruler.Royalty;
import init.D;
import init.religion.Religion;
import init.religion.Religions;
import init.sprite.UI.UI;
import settlement.army.Div;
import settlement.main.SETT;
import settlement.room.main.employment.RoomEmploymentSimple;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import snake2d.LOG;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.KeyMap;
import world.regions.Region;
import world.regions.data.RD;
import world.regions.data.pop.RDRace;

public final class RaceBoosts {

	public static CharSequence ¤¤name = "¤Race Work Skill";
	public static CharSequence ¤¤desc = "¤Species have different talents in different professions. The higher the work skill, the more production.";
	static {
		D.ts(RaceBoosts.class);
	}
	
	private double[][] priorities;
	private double[][] skillRelative;
	private  double[][] religions;

	public final BoostSpecs boosters = new BoostSpecs(RACES.name(), UI.icons().s.citizen, true);
	private final KeyMap<BV> bvmap = new KeyMap<BV>();
	
	
	RaceBoosts(){
		
		ACTION a = new ACTION() {
			
			@Override
			public void exe() {
				
				for (Race c : RACES.all()) {
					
					for (BoostSpec s : c.boosts.all()) {
						String k = s.boostable.key + s.booster.isMul;
						if (!bvmap.containsKey(k)) {
							bvmap.put(k, new BV(boosters, s.boostable, s.booster.isMul));
						}
						bvmap.get(k).set(c, s.booster.to());
					}
				}
				setPrio();
				setRel();
			}
		};
		BOOSTING.connecter(a);
		

	}
	
	public BoostSpec pushIfDoesntExist(Race c, double v, Boostable bo, boolean isMul) {
		String k = bo.key + isMul;
		double none = isMul ? 1 : 0;
		if (bvmap.containsKey(k) && bvmap.get(k).dd[c.index()] == none)
			return null;
		
		if (!bvmap.containsKey(k)) {
			bvmap.put(k, new BV(boosters, bo, isMul));
		}
		
		BV bv = bvmap.get(k);
		bv.set(c, v);
		setPrio();
		setRel();
		return bv.spec;
	}
	
	void setPrio() {
		
		HashMap<String, RoomEmploymentSimple> map = new HashMap<>();
		
		for (RoomEmploymentSimple p : SETT.ROOMS().employment.ALLS()) {
			if (p.blueprint().bonus() != null) {
				map.put(p.blueprint().bonus().key, p);
			}
		}
		
		{
			
			double max = 0;
			double min = 10000;
			
			priorities = new double[RACES.all().size()][SETT.ROOMS().employment.ALLS().size()];
			for (Race r : RACES.all()) {
				
				for (BoostSpec s : r.boosts.all()) {
					RoomEmploymentSimple e = map.get(s.boostable.key);
					if (e == null)
						continue;
					double v = s.booster.isMul ? (s.booster.to()-1)*5 : s.booster.to();
					max = Math.max(v, max);
					min = Math.min(min, v);
				}
			}
			
			for (Race r : RACES.all()) {
				double[] vv = priorities[r.index()];
				Arrays.fill(vv, 0.5);
				if (min > max) {
					continue;
				}
				for (RoomEmploymentSimple p : SETT.ROOMS().employment.ALLS()) {
					if (p.blueprint().bonus() != null) {
						double v = p.blueprint().bonus().get(r);
						v -= min;
						v /= (max-min);
						vv[p.eindex()] = v;
					}
				}
				

			}
			
			
		}
		
		
		
		
		
		skillRelative = new double[RACES.all().size()][SETT.ROOMS().employment.ALLS().size()];
		
		for (RoomEmploymentSimple e : SETT.ROOMS().employment.ALLS()) {
			
			if (e.blueprint().bonus() != null) {
				Boostable bo = e.blueprint().bonus();
				double ave = 0;
				for (Race r : RACES.all()) {
					ave += bo.get(r)-1;
				}
				if (ave != 0)
					ave /= RACES.all().size();
				ave += 1;
				
				for (Race r : RACES.all()) {
					double[] vv = skillRelative[r.index()];
					double v = bo.get(r)/ave;
					v = CLAMP.d(v, 0, 2);
					v /= 2;

					vv[e.eindex()] = v;
				}
				
			}else {
				for (Race r : RACES.all()) {
					double[] vv = skillRelative[r.index()];
					vv[e.eindex()] = 1.0;
					
				}
			}
		}

		
	}
	
	void setRel() {
		
		HashMap<String, Religion> map = new HashMap<>();
		
		for (Religion r : Religions.ALL()) {
			map.put(r.boostable.key, r);
		}
		religions = new double[RACES.all().size()][Religions.ALL().size()];
		for (Race r : RACES.all()) {
			
			
			double[] vv = religions[r.index()];
			Arrays.fill(vv, 0);
			for (Religion rel : Religions.ALL()) {
				vv[rel.index()] = rel.inclination;
			}
			
			
			for (BoostSpec s : r.boosts.all()) {
				Religion e = map.get(s.boostable.key);
				
				if (e == null)
					continue;
				if (e.boostable != s.boostable)
					continue;
				
				double v = s.booster.to();
				
				if (s.booster.isMul)
					vv[e.index()] *= v;
				else
					vv[e.index()] += v;
				
			}
			double min = Double.MAX_VALUE;
			for (Religion rel : Religions.ALL()) {
				min = Math.min(min, vv[rel.index()]);
			}
			
			double tot = 0;
			for (int i = 0; i < vv.length; i++) {
				vv[i] = Math.max(vv[i], 0);
				tot += vv[i];
			}
			
			if (tot > 0) {
				for (int i = 0; i < vv.length; i++) {
					vv[i]/= tot;
					vv[i] = CLAMP.d(vv[i], 0, 1);
				}
			}else {
				Arrays.fill(vv, 1.0/(vv.length));
			}
		}
	}

	public void debug() {
		
		LOG.ln("RACEBOOST");
		
		for (Race r : RACES.all())
			LOG.ln(r.key + " " + r.boosts.all().size());
		
		for (BoostSpec b : boosters.all()) {
			String s = b.boostable.key + " " + b.booster.from() + " " + b.booster.to();
			
			LOG.ln(s);
		}
		
	}
	

	public double getNorSkill(Race race, RoomEmploymentSimple e) {
		return skillRelative[race.index()][e.eindex()];
	}
	
	public double religion(Race race, Religion r) {
		return religions[race.index()][r.index()];
	}
	

	
	private static class BV extends Booster{

		private double from;
		private double to;
		private final double[] dd = new double[RACES.all().size()];
		private final boolean isMul;
		public final BoostSpec spec;
		
		BV(BoostSpecs bos, Boostable target, boolean isMul){
			super(new BSourceInfo(RACES.name(), UI.icons().s.citizen), isMul);
			this.isMul = isMul;
			if (isMul)
				Arrays.fill(dd, 1.0);
			set();
			
			spec = bos.push(this, target);
		}
		
		void set(Race c, double value) {
			dd[c.index()] = value;
			set();
		}
		
		private void set() {
			if (isMul) {
				from = 1.0;
				to = 1.0;
			}else {
				from = 0;
				to = 0;
			}
			
			for (double v : dd) {
				from = Math.min(v, from);
				to = Math.max(v, to);
			}
		}
		
		@Override
		public double get(Boostable bo, BOOSTABLE_O o) {
			return o.boostableValue(bo, this);
		}
		
		@Override
		public double vGet(Region reg) {
			double acc = 0;
			double tot = 0;
			for (int ri = 0; ri < RD.RACES().all.size(); ri++) {
				RDRace r = RD.RACES().all.get(ri);
				double pop = r.pop.get(reg);
				acc += dd[r.race.index()]*pop;
				tot += pop;
			}
			if (tot == 0)
				return 0;
			return acc/tot;
		}

		@Override
		public double vGet(Induvidual indu) {
			return dd[indu.race().index()];
		}

		@Override
		public double vGet(Div div) {
			return dd[div.info.race().index()];
		}

		@Override
		public double vGet(Faction f) {
			return isMul ? 1 : 0;
		}
		
		@Override
		public double vGet(NPCBonus bonus) {
			return vGet(bonus.faction.capitolRegion());
		}

		@Override
		public double vGet(POP_CL reg) {
			
			if (reg.race == null) {
				double acc = 0;
				double tot = 0;
				for (int ri = 0; ri < RACES.all().size(); ri++) {
					Race r = RACES.all().get(ri);
					double pop = STATS.POP().POP.data(reg.cl).get(r);
					
					acc += dd[r.index()]*pop;
					tot += pop;
				}
				if (tot == 0)
					return 0;
				return acc/tot;
			}
			return dd[reg.race.index()];
		}

		@Override
		public double vGet(Royalty roy) {
			return dd[roy.induvidual.race().index()];
		}

		@Override
		public boolean has(Class<? extends BOOSTABLE_O> b) {
			return b != Faction.class;
		}

		@Override
		public double from() {
			return from;
		}

		@Override
		public double to() {
			return to;
		}

		@Override
		public double vGet(Race race) {
			return dd[race.index];
		}
		
		
	}
	
	
}
