package world.map.pathing;

import static world.WORLD.*;

import init.sprite.SPRITES;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.misc.ACTION;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;
import world.WORLD;
import world.map.pathing.WGenPorts.Port;
import world.map.pathing.WGenPorts.PortDist;
import world.overlay.WorldOverlays;
import world.regions.Region;

public class WGenPath {

	
	public void generateAll(int px, int py, ACTION astep) {
		astep.exe();
		clear();
		for (Region r : REGIONS().all()) {
			if (r.info.area() > 0 ) {
				WORLD.ROADS().ROAD.set(r.cx(), r.cy());
				for (DIR d : DIR.ORTHO)
					if (WTRAV.isGoodLandTile(r.cx()+d.x(), r.cy()+d.y()))
						WORLD.ROADS().ROAD.set(r.cx(), r.cy(), d);
			}
		}
		astep.exe();
		WGenUtil util = new WGenUtil(astep);
		astep.exe();
		WGenPorts pp = new WGenPorts(util);
		astep.exe();
		WORLD.OVERLAY().debug = new WorldOverlays.OverlayTile(true, false) {
			
			@Override
			protected void renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderIterator it) {
				if (util.tmpRoute.is(it.tile())) {
					COLOR.ORANGE100.bind();
					SPRITES.cons().BIG.line.render(r, 0, it.x(), it.y());
					COLOR.unbind();
				}
//				Port p = pp.ports.get(it.tx(), it.ty());
//				if (p != null && p.group != null) {
//					COLOR.UNIQUE.getC(p.group.id).render(r, it.x(), it.y());
//					UI.FONT().H1.render(r, ""+p.group.id, it.x(), it.y(), 2);
//				}
			}
		};

		WGenRoad rr = new WGenRoad(util, pp);
		astep.exe();
		new WGenPortNeighs(util, pp);
		astep.exe();
		new WGenPortCondense(util, pp, rr);
		astep.exe();
		new WGenConnectPlayer(util, px, py);
		astep.exe();
		new WGenRoadRandom(util);
		
		for (Port p : pp.allports) {
			if (!WORLD.ROADS().HARBOUR.is(p.coo))
				continue;
			int totDist = 0;
			for (PortDist d : p.dists) {
				totDist += d.dist;
			}
			if (totDist < 250)
				WORLD.ROADS().minified.set(p.coo, true);
		}
		

		

		astep.exe();
		
		
		
//		
//		
		WORLD.PATH().generate(util.tmpRoute);
		WORLD.OVERLAY().debug = new DebugOverlay();
		astep.exe();
		removeUnusedRoads();
		WORLD.OVERLAY().debug = null;
	}
	
	public void clear() {
		WORLD.ROADS().clear();
	}
	
	private void removeUnusedRoads() {
		for (COORDINATE c : TBOUNDS()) {
			
			if (WORLD.PATH().route.is(c))
				continue;
			if (WORLD.ROADS().ROAD.is(c)) {
				int am = 0;
				for (DIR d : DIR.ORTHO) {
					if (WORLD.PATH().route.is(c, d)) {
						am++;
					}
					
				}
				if (am < 2)
					WORLD.ROADS().minified.set(c, true);
			}
		}
		
		for (COORDINATE c : TBOUNDS()) {
			
			if (WORLD.PATH().route.is(c))
				continue;
			if (WORLD.ROADS().minified.is(c)) {
				boolean needed = false;
				boolean canBeRemoved = false;
				for (DIR d : DIR.ORTHO) {
					if (WORLD.ROADS().is(c, d) && WORLD.ROADS().is(c, d.next(2))) {
						if (!WORLD.ROADS().is(c, d.next(1))) {
							needed = true;
							break;
						}else
							canBeRemoved = true;
						
					}	
				}
				if (!needed && canBeRemoved) {
					WORLD.ROADS().ROAD.clear(c);
				}
			}
		}
	}
	
	


}
