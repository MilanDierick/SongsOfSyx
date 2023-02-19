package world;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.Faction;
import init.RES;
import settlement.main.SGenerationConfig;
import snake2d.CORE;
import snake2d.PathTile;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import util.dic.DicMisc;
import view.main.VIEW;
import world.entity.caravan.Shipment;
import world.map.regions.*;

public final class WorldGenerator {

	private boolean[] busy = new boolean[Regions.MAX];
	
	public WorldGenerator(int playerX, int playerY) {
		
		RES.loader().print(DicMisc.¤¤Generating);
		
		if (CapitolPlacablity.whole(playerX-1, playerY-1) != null) {
			throw new RuntimeException("" + CapitolPlacablity.whole(playerX-1, playerY-1));
		}
		
		generatePlayer(playerX, playerY);
		
		
		Region r = World.REGIONS().getter.get(playerX, playerY);
		if (CapitolPlacablity.whole(playerX-1, playerY-1) == null) {
			REGIOND.OWNER().setCapitol(playerX, playerY, FACTIONS.player());
			r.name().clear().add(FACTIONS.player().appearence().name());
		}
		
		GAME.world().generateInit();
		VIEW.world().activate();
		generateKingdoms();
		GAME.s().CreateFromWorldMap(playerX-1, playerY-1, new SGenerationConfig());
		World.ARMIES().mercenaries().randmoize();
		GAME.saveNew();
		
	}
	
	private void generatePlayer(int playerX, int playerY) {
		Region r = World.REGIONS().getter.get(playerX, playerY);
		REGIOND.OWNER().setCapitol(playerX, playerY, FACTIONS.player());
		r.name().clear().add(FACTIONS.player().appearence().name());
		
		for (RegionDecree d : REGIOND.ALL()) {
			d.set(r, 0);
		}

		RES.flooder().init(this);
		RES.flooder().pushSloppy(playerX,playerY, 0);
		
		int amount = 5 + RND.rInt(15);
		
		while(RES.flooder().hasMore() && amount > 0) {
			PathTile c = RES.flooder().pollSmallest();
			r = World.REGIONS().getter.get(c);
			if (r == null)
				continue;
			if (!r.isWater()) {
				FRegions k2 = REGIOND.REALM(r);
				if (k2 == null) {
					busy[r.index()] = true;
					amount --;
				}
			}
				
			
			
			for (int i = 0; i < r.distances(); i++) {
				Region n = r.distanceNeigh(i);
				if (REGIOND.REALM(n) == null) {
					double dist = c.getValue() + r.distance(i);
					if (dist < Shipment.MAX_DISTANCE) {
						RES.flooder().pushSmaller(n.cx(), n.cy(), dist);
					}
				}
				
			}
			
		}
		RES.flooder().done();
		initFaction(FACTIONS.player());
		
	}
	
	private void generateKingdoms() {
		
		RES.loader().init();
		
		
		int amounts = 25*(World.TWIDTH()*World.THEIGHT())/(256*256);
		RES.loader().print(DicMisc.¤¤Generating + " " + amounts);
		
		int factionI = FACTIONS.all().get(1).index();
		
		while(amounts-- > 0 && factionI < FACTIONS.MAX) {
			
			for (int i = 1000; i>= 0; i--) {
				int tx = RND.rInt(World.TWIDTH());
				int ty = RND.rInt(World.THEIGHT());
				
				if (CapitolPlacablity.whole(tx, ty) == null) {
					Region r = World.REGIONS().getter.get(tx, ty);
					
					
					if (r != null && REGIOND.REALM(r) == null && !busy[r.index()] &&!r.isWater()) {
						
						REGIOND.OWNER().setCapitol(r.cx(), r.cy(), FACTIONS.getByIndex(factionI));
						r.name().clear().add(FACTIONS.getByIndex(factionI).appearence().name());
						spread(tx, ty, FACTIONS.getByIndex(factionI).kingdom().realm());
						initFaction(FACTIONS.getByIndex(factionI));
						factionI++;
						break;
					}
					
				}
			}
			RES.loader().print(DicMisc.¤¤Generating + " " + amounts);
		}
		World.MINIMAP().repaint();
		RES.loader().print(DicMisc.¤¤Generating);
		GAME.factions().prime();
		CORE.getInput().clearAllInput();
	}
	
	private void spread(int tx, int ty, FRegions k) {
		
		int amount = (int) (RND.rInt(4) + RND.rExpo()*20);
		ArrayList<Region> list = new ArrayList<>(amount);
		RES.flooder().init(this);
		RES.flooder().pushSloppy(tx,ty, 0);
		while(RES.flooder().hasMore() && amount > 0) {
			PathTile c = RES.flooder().pollSmallest();
			Region r = World.REGIONS().getter.get(c);
			if (r == null || busy[r.index()])
				continue;
			if (r.isWater()) {
				amount--;
				continue;
			}
			FRegions k2 = REGIOND.REALM(r);
			if (k2 == null) {
				list.add(r);
				amount --;
			}
			
			for (int i = 0; i < r.distances(); i++) {
				Region n = r.distanceNeigh(i);
				if (REGIOND.REALM(n) == null && !busy[n.index()]) {
					double dist = c.getValue() + r.distance(i);
					if (dist < Shipment.MAX_DISTANCE) {
						RES.flooder().pushSmaller(n.cx(), n.cy(), dist*(r.isWater()?2:1));
					}
				}
				
			}
			
		}
		RES.flooder().done();
		for (Region r : list) {
			REGIOND.OWNER().realm.set(r, k);
			REGIOND.MILITARY().soldiers.set(r, (int)REGIOND.MILITARY().soldiers_target.next(r));
		}
	}
	
	private void initFaction(Faction f) {
		World.ARMIES().init(f);
		World.ai().init(f);
	}
	

	
}
