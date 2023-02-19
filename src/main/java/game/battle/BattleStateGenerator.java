package game.battle;

import static world.World.*;

import game.GAME;
import game.battle.Conflict.Side;
import init.RES;
import init.race.RACES;
import init.race.Race;
import settlement.entity.EntityIterator;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.main.SGenerationConfig;
import snake2d.Errors;
import snake2d.PathTile;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.Rec;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import world.World;
import world.army.WARMYD;
import world.army.WDIV;
import world.army.WINDU.WDivGeneration;
import world.entity.army.WArmy;
import world.entity.army.WArmyState;
import world.map.regions.CapitolPlacablity;
import world.map.regions.REGIOND;

final class BattleStateGenerator {

	ArrayList<WDivGeneration> side1;
	ArrayList<WDivGeneration> side2;
	private int a1;
	private int a2;
	private Race r1;
	private Race r2;
	
	void generate(BattleState s, Conflict conflict, Rec deploymentTiles) {
		
		side1 = makeSide(conflict.sideA, true);
		side2 = makeSide(conflict.sideB, false);
		genMap(conflict);
		clearMid(conflict);
		SETT.ROOMS().THRONE.init.place(SETT.TILE_BOUNDS.cX(), SETT.TILE_BOUNDS.cY(), 0);
		SETT.ARMY_AI().pause();
		genArmies(s, conflict, deploymentTiles);
		SETT.init();
		GAME.update(0, 0);
		

	}
	
	private ArrayList<WDivGeneration> makeSide(Side side, boolean sideA){
		int am = 0;
		double art = 0;
		if (side.garrison() != null) {
			am += REGIOND.MILITARY().divisions(side.garrison()).size();
			art += REGIOND.MILITARY().soldiers.get(side.garrison())/150;
		}
		
		for (WArmy a : side) {
			am += a.divs().size();
			if (a.state() == WArmyState.fortified)
				art += WARMYD.men(null).get(a)/150;
			else
				art += WARMYD.men(null).get(a)/300;
		}
		
		ArrayList<WDivGeneration> res = new ArrayList<>(am);
		
		if (side.garrison() != null) {
			for (WDIV d : REGIOND.MILITARY().divisions(side.garrison())) {
				res.add(d.generate());
			}
		}
		
		for (WArmy a : side) {
			for (int i = 0; i < a.divs().size(); i++) {
				res.add(a.divs().get(i).generate());
			}
		}
		Race race = getRace(res);
		if (sideA) {
			a1 = (int) art;
			r1 = race;
		}
		else {
			a2 = (int) art;
			r2 = race;
		}
		return res;
	}
	
	private static Race getRace(LIST<WDivGeneration> divs) {
		int[] amount = new int[RACES.all().size()];
		for (WDivGeneration d : divs) {
			amount[d.race] += d.indus.length;
		}
		
		int best = 0;
		int bestV = 0;
		for (Race r : RACES.all()) {
			if (amount[r.index] > bestV) {
				best = r.index;
				bestV = amount[r.index];
			}
				
		}
		return RACES.all().get(best);
	}
	
	private void genMap(Conflict conflict) {
		
		SGenerationConfig config = new SGenerationConfig();
		config.animals = false;
		config.minables = false;
		
		int cx;
		int cy;
		
		if (conflict.sideB.size() > 0) {
			cx = conflict.sideB.get(0).ctx();
			cy = conflict.sideB.get(0).cty();
		}else {
			cx = conflict.sideB.garrison().cx();
			cy = conflict.sideB.garrison().cy();
		}
		
		
		
		if (okWorldTile(cx, cy)) {
			GAME.s().CreateFromWorldMap(cx-CapitolPlacablity.TILE_DIM/2, cy-CapitolPlacablity.TILE_DIM/2, config);
			return;
		}
		
		RES.flooder().init(this);
		RES.flooder().pushSloppy(cx, cy, 0);
		int ts =0;
		while (RES.flooder().hasMore()) {
			PathTile c = RES.flooder().pollSmallest();
			ts ++;
			if (okWorldTile(c.x(), c.y())) {
				RES.flooder().done();
				GAME.s().CreateFromWorldMap(c.x()-CapitolPlacablity.TILE_DIM/2, c.y()-CapitolPlacablity.TILE_DIM/2, config);
				return;
			}
			for (int di = 0; di < DIR.ALL.size(); di++) {
				DIR d = DIR.ALL.get(di);
				if (World.IN_BOUNDS(c, d)) {
					RES.flooder().pushSmaller(c, d, c.getValue()+d.tileDistance());
				}
			}
			
		}
		
		RES.flooder().done();
		throw new Errors.GameError("Unable to find location for battle " + cx + " " + cy + " " + ts);		
		
		
	}
	
	private void clearMid(Conflict conflict) {
		
		RES.flooder().init(this);
		RES.flooder().pushSloppy(SETT.TILE_BOUNDS.cX(), SETT.TILE_BOUNDS.cY(), 0);
		

		while (RES.flooder().hasMore()) {
			PathTile c = RES.flooder().pollSmallest();
			if (c.getValue() > 250) {
				continue;
			}
			if (SETT.PATH().availability.get(c).player <= 0 && !SETT.TERRAIN().WATER.is(c)) {
				SETT.TERRAIN().NADA.placeFixed(c.x(), c.y());
				//GROUND().STEPPE.placeFixed(c.x(), c.y());
			}
			for (int di = 0; di < DIR.ALL.size(); di++) {
				DIR d = DIR.ALL.get(di);
				if (SETT.IN_BOUNDS(c, d)) {
					RES.flooder().pushSmaller(c, d, c.getValue() + d.tileDistance());
				}
			}
		}
		
		RES.flooder().done();
		GAME.update(0, 0);
		
	}
	
	private void genArmies(BattleState s, Conflict conflict, Rec tiles) {
		
		DIR d = DIR.N;
		if (conflict.sideA.size() == 0) {
			d = DIR.get(conflict.sideA.garrison().cx(), conflict.sideA.garrison().cy(), conflict.sideB.get(0).body().cX(), conflict.sideB.get(0).body().cY());
		}else if (conflict.sideB.size() == 0) {
			d = DIR.get(conflict.sideA.get(0).body().cX(), conflict.sideA.get(0).body().cY(), conflict.sideB.garrison().cx(), conflict.sideB.garrison().cy());
		}else {
			d = DIR.get(conflict.sideA.get(0).body().cX(), conflict.sideA.get(0).body().cY(), conflict.sideB.get(0).body().cX(), conflict.sideB.get(0).body().cY());
		}
		
		if (!d.isOrtho())
			d = d.next(1);
		
		tiles.set(
				SETT.TILE_BOUNDS.cX()+d.next(-2).x()*SETT.TWIDTH/2, SETT.TILE_BOUNDS.cX()+d.next(3).x()*SETT.TWIDTH/2, 
				SETT.TILE_BOUNDS.cY()+d.next(-2).y()*SETT.TWIDTH/2, SETT.TILE_BOUNDS.cY()+d.next(3).y()*SETT.TWIDTH/2);
		tiles.makePositive();
		
		
		
		BattleStateGenArmy.genArmy(side1, true, d.perpendicular(), a1, r1);
		BattleStateGenArmy.genArmy(side2, false, d, a2, r2);
		
		new EntityIterator.Humans() {
			
			@Override
			protected boolean processAndShouldBreakH(Humanoid a, int ie) {
				a.teleportAndInitInDiv();
				return false;
			}
		};
		
	}

	
	private boolean okWorldTile(int tx, int ty) {
		
		
		if (tx-CapitolPlacablity.TILE_DIM/2 < 0 || ty-CapitolPlacablity.TILE_DIM/2 < 2 || tx + CapitolPlacablity.TILE_DIM/2 >= TWIDTH() || ty+CapitolPlacablity.TILE_DIM/2 >= THEIGHT())
			return false;
		return !CapitolPlacablity.b(tx, ty);
		
	}
	
}
