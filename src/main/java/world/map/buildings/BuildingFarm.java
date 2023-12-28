package world.map.buildings;

import java.io.IOException;

import game.GAME;
import game.time.TIME;
import init.C;
import init.D;
import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LISTE;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;
import world.WORLD;
import world.regions.Region;
import world.regions.centre.WorldRaceSheet;
import world.regions.data.RD;

final class BuildingFarm extends WorldBuildingSimple{


	private static CharSequence ¤¤name = "¤Farm";
	static {
		D.ts(BuildingFarm.class);
	}
	
	BuildingFarm(LISTE<WorldBuilding> all) throws IOException {
		super(all, ¤¤name);
	}

	@Override
	protected int fix(int tx, int ty) {
		int da = RND.oneIn(3) ? 1 : 0;
		int n = 0;
		for (DIR d : DIR.ORTHO) {
			if (WORLD.BUILDINGS().getter().get(tx, ty, d) != this)
				n++;
		}
		
		if (RND.rInt(n+1) != 0) {
			da |= 0b10;
		}
		
		return da;
	}

	@Override
	protected void renderOnGround(SPRITE_RENDERER r, RenderIterator it, int data) {
		if (isVisible(it.ran(), it.tile())) {
			int ran = (data>>1)*8 + (it.ran() % 8);
			OPACITY.O50.bind();
			WORLD.BUILDINGS().sprites.farms.render(r, ran, it.x(), it.y());
			OPACITY.unbind();
		}
		
	}

	@Override
	protected void renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderIterator it, int data) {
		if ((data & 1) == 1 && isVisible(it.ran(), it.tile())) {
			int ran = ((it.ran()>>9) & 31);
			int x = it.x()+((it.ran()>>10)&7)*C.SCALE;
			int y = it.y()+((it.ran()>>14)&7)*C.SCALE;
			WorldRaceSheet.Village sh = RD.RACES().visuals.vRace(WORLD.REGIONS().map.get(it.tile()), it.ran()).appearance().world.village;
			
			sh.render(r, s, ran, x, y);
			
			if (TIME.light().nightIs() && (TIME.light().partOfCircular()*16 > (it.ran()&0x07))) {
				x += C.TILE_SIZEH/2+(GAME.intervals().get05()+it.ran() & 0b11);
				y += C.TILE_SIZEH/2+(GAME.intervals().get05()+(it.ran()>>4) & 0b11);
				CORE.renderer().renderUniLight(x, y, 2, 128);
			}
		}
	}
	
	@Override
	public boolean isVisible(int ran, int tile) {
		if (WORLD.FOREST().amount.get(tile) == 1)
			return false;
		if (WORLD.WATER().isBig.is(tile))
			return false;
		Region r = WORLD.REGIONS().map.get(tile);
		if (r != null) {
			double v = RD.BUILDINGS().levelFarm.get(r)*(1.0-RD.DEVASTATION().current.getD(r));
			int k = (int) (0x0FFFF*v);
			return (ran & 0x0FFFF) <= k;
		}
		
		return false;
	}
	
}
