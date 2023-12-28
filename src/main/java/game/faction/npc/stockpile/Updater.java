package game.faction.npc.stockpile;

import game.faction.npc.FactionNPC;
import game.faction.npc.ruler.RTraits;
import game.time.TIME;
import init.race.Race;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.main.SETT;
import settlement.room.industry.module.Industry.IndustryResource;
import settlement.room.main.FlatIndustries.FlatIndustry;
import snake2d.LOG;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sprite.text.Str;
import world.regions.data.RD;

class Updater {

	final Consumer civic;

	final Producer ra = new Producer();
	final Needer needer2 = new Needer();

	final SOutput[] allO = new SOutput[RESOURCES.ALL().size()];
	final ArrayListGrower<SIns> allIns = new ArrayListGrower<>();
	
	private final SBonus bonus = new SBonus();
	
	public Updater() {
		
		civic = new Consumer();
		

		
		for (RESOURCE res : RESOURCES.ALL()) {
			allO[res.index()] = new SOutput(res);
				
		}
		
		for (int fi = 0; fi < SETT.ROOMS().FIndustries.all().size(); fi++) {
			FlatIndustry ins = SETT.ROOMS().FIndustries.all().get(fi);
			for (IndustryResource o : ins.industry.outs()) {
				
				SIns p = new SIns(o, ins, allO);
				
				allO[o.resource.index()].producers.add(p);
				
			}
			
			
		}

		for (SOutput o : allO) {
			for (SIns ii : o.producers) {
				allIns.add(ii);
			}
		}
		

	}
	
	public void update(NPCStockpile stockpile, FactionNPC faction, double seconds) {
		int wf =  RD.RACES().population.get(faction.capitolRegion());
		wf *= 0.125 + 0.25*(faction.court().king().roy().trait(RTraits.get().competence)*0.5);
		update(stockpile, faction, faction.race(), wf, seconds);
		
	}
	
	public void update(NPCStockpile stockpile, FactionNPC faction, Race race, int workforce, double ds) {
		
		ds*=TIME.secondsPerDayI;
		
		for (SOutput b : allO) {
			for (SIns ii : b.producers)
				ii.clear();
		}
		
//		tax(faction, stockpile);
		
		bonus.init(faction);
		ra.set(this, faction, bonus, stockpile, workforce*ds);
		needer2.setNeeds(this, race, stockpile);
		
		//needer.setNeeds(this, race, bonus);
		
		civic.consume(this, faction, ds, stockpile);
		
		//target.setValues(this, bonus, stockpile);
		
		//rater.setValues(all, bonus, stockpile, 1000);
		
		//producer.setValues(this, bonus, stockpile, workforce);
		
		//pricer.price(this, stockpile);
//		
//		ins.produce(all, stockpile);
//		pricer.price(all, stockpile);
	}

//	private void tax(Faction f, NPCStockpile stockpile) {
//		
//		for (RESOURCE r : RESOURCES.ALL())
//			allO[r.index()].taxed = 0;
//		
//		for (int rei = 0; rei < f.realm().regions(); rei++) {
//			Region r = f.realm().region(rei);
//			for (RESOURCE res : RESOURCES.ALL()) {
//				allO[res.index()].taxed += RD.OUTPUT().get(res).boost.get(r);
//			}
//		}
//		for (RESOURCE res : RESOURCES.ALL()) {
//			stockpile.inc(res, Math.ceil(allO[res.index()].taxed));
//		}
//	}

	
	public SOutput o(RESOURCE res) {
		return allO[res.index()];
	}
	
	public SOutput o(int resI) {
		return allO[resI];
	}
	
	public static class SOutput {
		public final RESOURCE res;
		public final ArrayListGrower<SIns> producers = new ArrayListGrower<>();
		
		public double needed;
		public double prodSpeed;
		public double amount;
		public double prio;
		public double produced;
		public double taxed;
		
		SOutput(RESOURCE res){
			this.res = res;
		}

		void debug(Str s, int inc) {
			s.add(LOG.WS(3*inc) + res).NL();
			for (int i = 0; i < producers.size(); i++) {
				SIns ins = producers.get(i);
				ins.debug(s, inc + 1);
			}
		}
		
		private boolean check(SOutput o) {
			if (o == this)
				return false;
			for (int i = 0; i < producers.size(); i++) {
				if (!producers.get(i).check(o))
					return false;
			}
			return true;
			
		}
		
	}
	
	public static class SIns {
		
		public final IndustryResource out;
		public final FlatIndustry ins;
		public final ArrayList<SOutput> inputs; 
		
		public double prodSpeed;
		public double prodSpeedBonus;
		public double prodSpeedTot;
		
		
		SIns(IndustryResource out, FlatIndustry ins, SOutput[] all){
			this.ins = ins;
			this.out = out;
			inputs = new ArrayList<SOutput>(ins.industry.ins().size());
			for (IndustryResource r : ins.industry.ins()) {
				inputs.add(all[r.resource.index()]);
			}
		}
		
		void clear() {
			prodSpeed = -1;
			prodSpeedBonus = -1;
			prodSpeedTot = -1;
		}

		private boolean check(SOutput o) {
			for (int i = 0; i < inputs.size(); i++)
				if (!inputs.get(i).check(o))
					return false;
			return true;
		}

		private void debug(Str s, int inc) {
			s.add(LOG.WS(3*inc)).add(ins.blue.toString()).NL();
			for (int i = 0; i < inputs.size(); i++) {
				SOutput ins = inputs.get(i);
				ins.debug(s, inc + 1);
			}
		}
		
	}


	
}
