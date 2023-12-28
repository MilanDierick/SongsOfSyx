package world.map.buildings;

import java.io.IOException;

import game.GAME;
import game.time.TIME;
import init.C;
import init.D;
import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.sets.LISTE;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;
import world.WORLD;
import world.regions.Region;
import world.regions.centre.WorldRaceSheet;
import world.regions.data.RD;

final class BuildingVillage extends WorldBuildingSimple{
	
	private static CharSequence ¤¤name = "¤Village";
	static {
		D.ts(BuildingVillage.class);
	}
	
	BuildingVillage(LISTE<WorldBuilding> all) throws IOException {
		super(all, ¤¤name);
	}

	@Override
	protected int fix(int tx, int ty) {
		return 0;
	}

	@Override
	protected void renderOnGround(SPRITE_RENDERER r, RenderIterator it, int data) {
		
		if (isVisible(it.ran(), it.tile())) {
			
			int x = it.x();
			int y = it.y();
			int t = it.ran()&0x07;
			WORLD.BUILDINGS().sprites.farms.render(r, t, x, y);
		}
		
	}
	
	@Override
	protected void renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderIterator it, int data) {
		
		if (isVisible(it.ran(), it.tile())) {
			
		
			int ran = ((it.ran()) % 32);
			
			int x = it.x() + (((it.ran() >> 4)&0x07))*C.SCALE;
			int y = it.y() + (((it.ran() >> 8)&0x07))*C.SCALE;
			
			WorldRaceSheet.Village sh = RD.RACES().visuals.vRace(WORLD.REGIONS().map.get(it.tile()), ran).appearance().world.village;
			
			sh.render(r, s, ran>>10, x, y);
			
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
			int v = (int) (0x0FFFF*RD.RACES().popSize(r));
			return (ran & 0x0FFFF) <= v;
		}
		
		return false;
	}
	
}
