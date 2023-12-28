package settlement.room.main;


import game.GAME;
import game.faction.FACTIONS;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.UI.UI;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.industry.module.*;
import settlement.room.industry.module.Industry.IndustryResource;
import settlement.stats.STATS;
import snake2d.util.sets.*;
import snake2d.util.sprite.SPRITE;
import util.dic.DicRes;
import world.regions.data.RD;

public class RoomProduction {

	private final ArrayList<Res> producers = new ArrayList<Res>(RESOURCES.ALL().size());
	private final ArrayList<Res> consumers = new ArrayList<Res>(RESOURCES.ALL().size());
	
	
	RoomProduction(ROOMS rooms){
		for(RESOURCE res : RESOURCES.ALL()) {
			producers.add(new Res(res));
			consumers.add(new Res(res));
		}

		for (RoomBlueprint h : rooms.all()) {
			
			if (h instanceof INDUSTRY_HASER) {
				INDUSTRY_HASER ii = (INDUSTRY_HASER) h;
				for (Industry ins : ii.industries()) {
					for (IndustryResource oo : ins.outs()) {
						SourceR i = new SourceR(oo.resource, (RoomBlueprintImp)h, ins);
						producers.get(i.res.index()).ins.add(i);
						producers.get(i.res.index()).all.add(i);
					}
					for (IndustryResource oo : ins.ins()) {
						SourceR i = new SourceR(oo.resource, (RoomBlueprintImp)h, ins);
						consumers.get(i.res.index()).ins.add(i);
						consumers.get(i.res.index()).all.add(i);
					}
				}	
			}
		}
		
		for (RESOURCE res : RESOURCES.ALL()) {
			producers.get(res.index()).all.add(new SourceReg(res));
		}
		
		for (Res r : producers)
			r.init();
		for (Res r : consumers)
			r.init();
	}
	
	private void update(int ticks) {
		ENTITY[] es = SETT.ENTITIES().getAllEnts();
		
		int tott = ticks*200;
		if (tott < 0 || tott > es.length)
			tott= es.length;
		
		for (int i = 0; i < tott; i++) {
			
			if (ui >= es.length) {
				
				for (Res r : producers) {
					double tot = 0;
					for (SourceR in : r.ins) {
						in.am = in.old;
						in.old = 0;
						tot += in.am;
					}
					for (Source in : r.all) {
						if (in instanceof SourceReg)
							tot += in.am();
					}
					r.am = tot;
				}
				for (Res r : consumers) {
					double tot = 0;
					for (SourceR in : r.ins) {
						in.am = in.old;
						in.old = 0;
						tot += in.am;
					}
					r.am = tot;
				}
				
				ui = 0;
				break;
			}
			
			ENTITY e = es[ui];
			if (e != null && e instanceof Humanoid) {
				Humanoid h = (Humanoid) e;
				RoomInstance ins = STATS.WORK().EMPLOYED.get(h);
				if (ins != null && ins instanceof ROOM_PRODUCER) {
					ROOM_PRODUCER p = (ROOM_PRODUCER) ins;
					Industry in = p.industry();
					for (IndustryResource oo : in.outs()) {
						RESOURCE res = oo.resource;
						double d = p.productionRate(ins, h, in, oo);
						for (SourceR ii : producers.get(res.index()).ins) {
							if (ii.blue == in.blue && ii.ins == in) {
								ii.old += d;
							}
						}
					}
					for (IndustryResource oo : in.ins()) {
						RESOURCE res = oo.resource;
						double d = p.productionRate(ins, h, in, oo);
						for (SourceR ii : consumers.get(res.index()).ins) {
							if (ii.blue == in.blue && ii.ins == in) {
								ii.old += d;
							}
						}
					}
				}
			}
			ui++;
			
		}
	}
	
	int ui = 0;
	int upI = 0;
	
	public double produced(RESOURCE res) {
		if (Math.abs(upI-GAME.updateI()) > 1) {
			update(Math.abs(upI-GAME.updateI()));
			upI = GAME.updateI();
		}
		return producers.get(res.index()).am;
	}
	
	public double consumed(RESOURCE res) {
		if (Math.abs(upI-GAME.updateI()) > 1) {
			update(Math.abs(upI-GAME.updateI()));
			upI = GAME.updateI();
		}
		return consumers.get(res.index()).am;
	}
	
	public LIST<Source> producers(RESOURCE res) {
		return producers.get(res.index()).all;
	}
	
	public LIST<Source> consumers(RESOURCE res) {
		return consumers.get(res.index()).all;
	}
	
	private class Res {
		
		private final ArrayListGrower<Source> all = new ArrayListGrower<>();
		private final ArrayListGrower<SourceR> ins = new ArrayListGrower<>();
		private double am;
		
		Res(RESOURCE res){

		}
		
		private void init() {
			for (int i1 = 0; i1 < ins.size(); i1++) {
				for (int i2 = 0; i2 < ins.size(); i2++) {
					if (i2 == i1)
						continue;
					if (ins.get(i1).blue == ins.get(i2).blue) {
						ins.get(i1).multiple = true;
						ins.get(i2).multiple = true;
					}
					
				}
				
			}
		}
		
	}
	
	
	
	public abstract static class Source {
		
		public final RESOURCE res;

		
		Source(RESOURCE res){
			this.res = res;
		}
	
		public abstract double am();
		
		public Industry thereAreMultipleIns() {
			return null;
		}
		
		public abstract SPRITE icon();
		
		public abstract CharSequence name();
		
	}
	
	
	public class SourceReg extends Source{
		
		SourceReg(RESOURCE res){
			super(res);
			
		}
	
		@Override
		public double am() {
			int am = 0;
			for (int i = 0; i < FACTIONS.player().realm().regions(); i++) {
				am += RD.OUTPUT().get(res).getDelivery(FACTIONS.player().realm().region(i));
			}
			return am;
		}
		
		@Override
		public Industry thereAreMultipleIns() {
			return null;
		}
		
		@Override
		public SPRITE icon() {
			return UI.icons().s.money;
		}
		
		@Override
		public CharSequence name() {
			return DicRes.¤¤Taxes;
		}
		
	}

	public class SourceR extends Source{
	
		private final RoomBlueprintImp blue;
		private final Industry ins;
		private double old;
		private double am;
		private boolean multiple = false;
		
		SourceR(RESOURCE res, RoomBlueprintImp blue, Industry ins){
			super(res);
			this.blue = blue;
			this.ins = ins;
		}
	
		@Override
		public double am() {
			if (Math.abs(upI-GAME.updateI()) > 1) {
				update(Math.abs(upI-GAME.updateI()));
				upI = GAME.updateI();
			}
			return am;
		}
		
		@Override
		public Industry thereAreMultipleIns() {
			return multiple ? ins : null;
		}
		
		@Override
		public SPRITE icon() {
			return blue.icon.small;
		}
		
		@Override
		public CharSequence name() {
			return blue.info.names;
		}
		
	}
	
	
}
