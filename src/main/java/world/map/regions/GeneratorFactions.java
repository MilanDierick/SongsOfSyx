package world.map.regions;

import java.util.Arrays;

import game.faction.FACTIONS;
import init.RES;
import snake2d.PathTile;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import util.dic.DicMisc;
import world.World;
import world.WorldGen;
import world.entity.caravan.Shipment;

final class GeneratorFactions {

	private boolean[] busy = new boolean[Regions.MAX];
	
	public GeneratorFactions() {

	}
	
	public void generate(WorldGen gen) {
		Arrays.fill(busy, false);
		
		RES.loader().print(DicMisc.¤¤Generating);
		

		generatePlayer(gen.playerX, gen.playerY);
		generateKingdoms();
	}

	
	private void generatePlayer(int playerX, int playerY) {
		Region r = World.REGIONS().getter.get(playerX, playerY);
		REGIOND.OWNER().setCapitol(playerX, playerY, FACTIONS.player());
		r.name().clear().add(FACTIONS.player().appearence().name());
		
		for (RegionDecree d : REGIOND.ALL()) {
			d.set(r, 0);
		}


		int amount = 2;
		for (int i = 0; i < r.distances() && amount > 0; i++) {
			Region n = r.distanceNeigh(i);
			if (n == null)
				continue;
			if (!n.isWater()) {
				FRegions k2 = REGIOND.REALM(n);
				if (k2 == null) {
					busy[n.index()] = true;
					amount --;
				}
			}
			
		}
		
		
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
				Region r = World.REGIONS().getter.get(tx, ty);
				if (r != null && !r.isWater && REGIOND.REALM(r) == null && !busy[r.index()] &&!r.isWater()) {
					REGIOND.OWNER().setCapitol(r.cx(), r.cy(), FACTIONS.getByIndex(factionI));
					r.name().clear().add(FACTIONS.getByIndex(factionI).appearence().name());
					
					spread(tx, ty, FACTIONS.getByIndex(factionI).kingdom().realm());
					factionI++;
					break;
				}
			}
			RES.loader().print(DicMisc.¤¤Generating + " " + amounts);
		}
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


	

	
}
