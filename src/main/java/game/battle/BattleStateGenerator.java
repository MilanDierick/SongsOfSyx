package game.battle;

import static world.WORLD.*;

import game.GAME;
import game.battle.PlayerBattleSpec.SpecSide;
import init.RES;
import init.race.RACES;
import init.race.Race;
import settlement.entity.EntityIterator;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.tilemap.generator.Generator;
import snake2d.Errors;
import snake2d.PathTile;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.Rec;
import snake2d.util.sets.LIST;
import world.WORLD;
import world.army.WDivGeneration;
import world.map.pathing.WTRAV;
import world.regions.centre.WCentre;

final class BattleStateGenerator {

	SpecSide side1;
	SpecSide side2;
	private int a1;
	private int a2;
	private Race r1;
	private Race r2;
	
	void generate(BattleState s, PlayerBattleSpec spec, Rec deploymentTiles) {
		
		side1 = makeSide(spec.player, true);
		side2 = makeSide(spec.enemy, false);
		
		DIR d = DIR.N;
		d = DIR.get(spec.player.wCoo, spec.enemy.wCoo);

		if (d == DIR.C)
			d = DIR.N;
		
		if (!d.isOrtho())
			d = d.next(1);
		
		genMap(spec.player.wCoo.x(), spec.player.wCoo.y(), d);
		SETT.ROOMS().THRONE.init.place(SETT.TILE_BOUNDS.cX(), SETT.TILE_BOUNDS.cY(), 0);
		SETT.ARMY_AI().pause();
		genArmies(s, spec.player, spec.enemy, deploymentTiles, d);
		SETT.init();
		Generator.paintMinimap();
		GAME.update(0, 0);
		

	}
	
	private SpecSide makeSide(SpecSide side, boolean sideA){

		double art = side.artillery;
		
	

		
		Race race = getRace(side.divs);
		if (sideA) {
			a1 = (int) art;
			r1 = race;
		}
		else {
			a2 = (int) art;
			r2 = race;
		}
		return side;
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
	
	private void genMap(int cx, int cy, DIR eDir) {
		
		RES.flooder().init(this);
		RES.flooder().pushSloppy(cx, cy, 0);
		int ts =0;
		while (RES.flooder().hasMore()) {
			PathTile c = RES.flooder().pollSmallest();
			ts ++;
			if (okWorldTile(c.x(), c.y()) && okWorldTile(c.x()+eDir.x(), c.y()+eDir.y())) {
				RES.flooder().done();
				GAME.s().CreateFromWorldMap(c.x()-WCentre.TILE_DIM/2, c.y()-WCentre.TILE_DIM/2, true);
				return;
			}
			for (int di = 0; di < DIR.ALL.size(); di++) {
				DIR d = DIR.ALL.get(di);
				if (WORLD.IN_BOUNDS(c, d)) {
					RES.flooder().pushSmaller(c, d, c.getValue()+d.tileDistance());
				}
			}
			
		}
		
		RES.flooder().done();
		throw new Errors.GameError("Unable to find location for battle " + cx + " " + cy + " " + ts);		
		
		
	}
	
	private void genArmies(BattleState s, SpecSide a, SpecSide b, Rec tiles, DIR d) {
		
		
		
		
		tiles.set(
				SETT.TILE_BOUNDS.cX()+d.next(-2).x()*SETT.TWIDTH/2, SETT.TILE_BOUNDS.cX()+d.next(3).x()*SETT.TWIDTH/2, 
				SETT.TILE_BOUNDS.cY()+d.next(-2).y()*SETT.TWIDTH/2, SETT.TILE_BOUNDS.cY()+d.next(3).y()*SETT.TWIDTH/2);
		tiles.makePositive();
		
		
		
		BattleStateGenArmy.genArmy(side1.divs, true, d.perpendicular(), a1, r1, side1.moraleBase);
		BattleStateGenArmy.genArmy(side2.divs, false, d, a2, r2, side2.moraleBase);
		
		new EntityIterator.Humans() {
			
			@Override
			protected boolean processAndShouldBreakH(Humanoid a, int ie) {
				a.teleportAndInitInDiv();
				return false;
			}
		}.iterate();
		
	}

	
	private boolean okWorldTile(int tx, int ty) {
		
		
		if (tx-WCentre.TILE_DIM/2 < 0 || ty-WCentre.TILE_DIM/2 < 2 || tx + WCentre.TILE_DIM/2 >= TWIDTH() || ty+WCentre.TILE_DIM/2 >= THEIGHT())
			return false;
		for (int di = 0; di < DIR.ALLC.size(); di++) {
			DIR d = DIR.ALLC.get(di);
			if (!WTRAV.isGoodLandTile(tx+d.x(), ty+d.y()))
				return false;
		}
		return true;
	}
	
}
