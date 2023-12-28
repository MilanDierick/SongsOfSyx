package game.faction.player;

import java.io.IOException;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import game.time.TIME;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import snake2d.util.file.*;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.dic.DicRes;
import util.info.INFO;
import util.statistics.HistoryResource;
import world.regions.Region;
import world.regions.data.RD;

public class PRel {

	private final ArrayList<FactionNPC> neighs = new ArrayList<>(FACTIONS.MAX);
	private final ArrayList<FactionNPC> traders = new ArrayList<>(FACTIONS.MAX);
	private final ArrayList<FactionNPC> tradersPotential = new ArrayList<>(FACTIONS.MAX);
	private int upI = -121;

	public final HistoryResource pricesBuy;
	public final HistoryResource pricesSell;
	
	PRel() {
		pricesBuy = new HistoryResource(
				new INFO(DicRes.¤¤buyPrice, ""),
				32, TIME.seasons(), true);
		pricesSell = new HistoryResource(
				new INFO(DicRes.¤¤sellPrice, ""),
				32, TIME.seasons(), true);
		
		new RD.RDOwnerChanger() {
			
			@Override
			public void change(Region reg, Faction oldOwner, Faction newOwner) {
				upI = -121;
			}
		};
	}

	int ri = 0;
	
	void update(double ds) {
		init();
		ri %= RESOURCES.ALL().size();
		RESOURCE res = RESOURCES.ALL().get(ri);
		
		int s = 0;
		int m = Integer.MAX_VALUE;
		if (traders.size() == 0) {
			for (FactionNPC f : tradersPotential) {
				s = Math.max(s, f.seller().priceBuyP(res));
				m = Math.min(m, f.buyer().priceSellP(res));
			}
		}else {
			for (FactionNPC f : traders) {
				s = Math.max(s, f.seller().priceBuyP(res));
				m = Math.min(m, f.buyer().priceSellP(res));
			}
		}
		
		
		
		if (m == Integer.MAX_VALUE)
			m = 0;
		pricesSell.set(res, s);
		pricesBuy.set(res, m);
		
		ri ++;
		

	}
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			pricesBuy.save(file);
			pricesSell.save(file);
			file.i(ri);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			pricesBuy.load(file);
			pricesSell.load(file);
			ri = file.i();
			upI = -1;
		}
		
		@Override
		public void clear() {
			pricesBuy.clear();
			pricesSell.clear();
			upI = -1;
		}
	};
	
	public LIST<FactionNPC> neighs(){
		init();
		return neighs;
	}
	
	public LIST<FactionNPC> traders(){
		init();
		return traders;
	}
	
	public LIST<FactionNPC> tradersPotential(){
		init();
		return tradersPotential;
	}
	
	private void init() {

		if (FACTIONS.player().capitolRegion() == null)
			return;
		if (Math.abs(upI- GAME.updateI()) < 120)
			return;
		upI = GAME.updateI();
		neighs.clearSloppy();
		traders.clearSloppy();
		tradersPotential.clearSloppy();
		
		
		for (FactionNPC f : FACTIONS.NPCs()) {
			if (RD.DIST().factionBordersPlayer(f)) {
				neighs.add(f);
				tradersPotential.add(f);
				if (FACTIONS.DIP().trades(FACTIONS.player(), f))
					traders.add(f);
			}
		}
		
	}
	

	
}
