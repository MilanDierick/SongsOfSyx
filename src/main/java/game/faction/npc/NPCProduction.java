package game.faction.npc;

import java.util.Arrays;
import java.util.HashMap;

import game.faction.FACTIONS;
import game.faction.player.PTech;
import init.boostable.BOOSTABLE;
import init.boostable.BOOSTABLES;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.tech.TECH;
import init.tech.TECHS;
import settlement.main.SETT;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.Industry.IndustryResource;
import settlement.room.knowledge.laboratory.ROOM_LABORATORY;
import settlement.room.knowledge.library.ROOM_LIBRARY;
import settlement.room.main.RoomBlueprint;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.*;
import snake2d.util.sprite.text.Str;

final class NPCProduction {

	private static LIST<Output> all;
	
	LIST<Output> create() {
		double bb[] = bonuses();
		
		LIST<RoomBlueprint> ref = new ArrayList<RoomBlueprint>().join(SETT.ROOMS().REFINERS).join(SETT.ROOMS().WORKSHOPS);
		
		LISTE<Industry> ins = new LinkedList<>();
		
		for (RoomBlueprint b : ref) {
			if (b instanceof INDUSTRY_HASER) {
				for (Industry i : ((INDUSTRY_HASER) b).industries()) {
					if (b == SETT.ROOMS().HUNTER)
						continue;
					if (i.outs().size() == 0)
						continue;
					ins.add(i);
				}
			}
		}
		
		HashMap<RESOURCE, LinkedList<Recipe>> map = new HashMap<>();
		
		
		for (RESOURCE res : RESOURCES.ALL()) {
			
			for (Industry i : ins) {
				double bo = bb[i.bonus().index];
				
				for (IndustryResource o : i.outs()) {
					if (o.resource == res) {
						
						ArrayList<Input> inp = new ArrayList<>(i.ins().size());
						for (IndustryResource ri : i.ins()) {
							double rate = ri.rate / (bo*o.rate);
							Input input = new Input(ri.resource, rate);
							inp.add(input);
						}
						Recipe rec = new Recipe(bo*o.rate, inp);
						if (!map.containsKey(o.resource))
							map.put(o.resource, new LinkedList<>());
						map.get(o.resource).add(rec);
					}
				}
			}
			
		}
		
		LinkedList<Output> all = new LinkedList<>();
		
		for (RESOURCE res : RESOURCES.ALL()) {
			
			if (!map.containsKey(res))
				continue;
			all.add(new Output(res, map.get(res)));
			
		}
		
		{
			double max = 0;
			for (Output o : all) {
				for (Recipe r : o.inputs) {
					max = Math.max(r.speed, max);
				}
			}
			for (Output o : all) {
				for (Recipe r : o.inputs) {
					double d = r.speed / max;
					d = Math.sqrt(d);
					r.dspeed = d;
				}
			}
		}
		
		
//		for (Output o : all)
//			LOG.ln(o);
		
		return all;
	}
	
	public static LIST<Output> all(){
		if (all == null)
			all = new NPCProduction().create();
		return all;
	}
	
	public static void produce(FactionNPC f) {
		double pop = f.capitol().population.total().get()/30000.0;
		
		
		
		
		pop = CLAMP.d(pop, 0, pop);
		pop = Math.sqrt(pop);
		
		for (Output output : all()) {
			
			double target = 0;
			double rate = 0;
			
			for (Recipe rec : output.inputs) {
				double value = 0;
				for (Input in : rec.inputs) {
					double raw = 0;
					for (RESOURCE res : in.res) {
						raw = Math.max(raw, f.buyer().amount(res));
					}
					double rPrice = 1.0/(raw+1);
					rPrice*= in.rateRatio;
					value += rPrice;
				}
				value = 1.0/(value* (1 + 0.1*rec.inputs.size()));
				if (value*rec.speed > target) {
					target = value;
					rate = rec.dspeed;
				}
			}
			
	

			target = CLAMP.d(target-f.buyer().amount(output.res), 0, Integer.MAX_VALUE);
			
			target*= pop*rate;
			
			
			f.buyer().add(output.res, (int)target);
			
		}
		
	}
	
	static final class Output {
		
		public final RESOURCE res;
		public final LIST<Recipe> inputs;
		
		Output(RESOURCE res, LIST<Recipe> inputs){
			this.res = res;
			this.inputs = new ArrayList<Recipe>(inputs);
		}
		
		@Override
		public String toString() {
			Str s = new Str(128);
			s.add(res.toString()).s();
			s.NL();
			for (Recipe r : inputs) {
				s.s().s().s().add(r.speed).s().add(r.dspeed);
				for (Input i : r.inputs) {
					s.NL();
					s.s().s().s();
					for (RESOURCE ri : i.res) {
						s.add(ri.toString()).s();
					}
					s.add(i.rateRatio).s();
				}
				
				s.NL().add('-').add('-').add('-').NL();
			}
			return ""+s;
		}
		
	}
	
	private static class Recipe {
		
		public final double speed;
		public double dspeed;
		public final LIST<Input> inputs;
		
		Recipe(double speed, LIST<Input> inputs){
			this.speed = speed;
			this.inputs = new ArrayList<Input>(inputs);
		}
		
	}
	
	private static class Input {
		
		public final LIST<RESOURCE> res;
		public final double rateRatio;
		
		Input(RESOURCE rr, double rateRatio){
			LinkedList<RESOURCE> res = new LinkedList<>(rr);
			for (RESOURCE r : rr.tradeSameAs())
				res.add(r);;
			this.res = new ArrayList<RESOURCE>(res);
			this.rateRatio = rateRatio;
		}
		
	}
	
	private double[] bonuses() {
		double techCost = 0;
		double[] add = new double[BOOSTABLES.all().size()];
		double[] mul = new double[BOOSTABLES.all().size()];
		
		Arrays.fill(mul, 1);
		
		for (Industry i : SETT.ROOMS().INDUSTRIES) {
			if (i.bonus() != null)
				mul[i.bonus().index] *= i.blue.upgrades().boost(i.blue.upgrades().max())-1;
		}
		
		for (TECH t : TECHS.ALL()) {
			techCost = PTech.costTotal(t, t.levelMax);
		}
		{
		
			double la = 0;
			for (ROOM_LABORATORY l : SETT.ROOMS().LABORATORIES)
				la = Math.max(la, l.knowledgePerStation());
			
			double li = 0;
			for (ROOM_LIBRARY l : SETT.ROOMS().LIBRARIES)
				li = Math.max(li, l.boostPerStation());
			
			double pop = 2000;
			
			double labs = (1+(li*pop))/(2*li);
			double libs = pop-labs;
			double know = labs*la*(1.0+libs*li);
			
			double d = CLAMP.d(know/techCost, 0, 1);
			
			for (BOOSTABLE b : BOOSTABLES.all()) {
				add[b.index()] += FACTIONS.player().tech.BOOSTER.maxAdd(b)*d;
				mul[b.index()] *= 1 + (FACTIONS.player().tech.BOOSTER.maxMul(b)-1)*d;
			}
		
		}
		
		for (BOOSTABLE b : BOOSTABLES.all()) {
			add[b.index()] += FACTIONS.player().bonus().maxAdd(b)-FACTIONS.player().tech.BOOSTER.maxAdd(b);
			mul[b.index()] *= (FACTIONS.player().bonus().maxMul(b)/FACTIONS.player().tech.BOOSTER.maxMul(b)-1);
		}
		
		
		
		
		
		for (BOOSTABLE b : BOOSTABLES.all()) {
			add[b.index] *= (mul[b.index]+1);
		}
		

		return add;
	}
	
}
