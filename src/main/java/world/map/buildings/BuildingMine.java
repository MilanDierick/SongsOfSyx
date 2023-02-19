package world.map.buildings;

import game.GAME;
import settlement.main.RenderData.RenderIterator;
import snake2d.SPRITE_RENDERER;
import snake2d.util.sets.LISTE;
import util.rendering.ShadowBatch;
import world.World;
import world.map.regions.REGIOND;
import world.map.regions.Region;

final class BuildingMine extends WorldBuildingSimple{

	BuildingMine(LISTE<WorldBuilding> all) {
		super(all, "mine");
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
			World.BUILDINGS().sprites.farms.render(r, 8*3+t, x, y);
		}
		
	}
	
	@Override
	protected void renderAboveTerrain(SPRITE_RENDERER r, ShadowBatch s, RenderIterator it, int data) {
		
		if (!isVisible(it.ran(), it.tile()))
			return;
		
		int i = it.ran()&0b1;
		i*= 4;
		
		int m = (GAME.intervals().get02() + (it.ran()>>1))&0b0111;
		if (m >= 4) {
			m -= 4;
			m = 3-m;
		}
		
		i += m;
		
		
		World.BUILDINGS().sprites.mines.render(r, i, it.x(), it.y());
	}
	
	@Override
	public boolean isVisible(int ran, int tile) {
		
		Region r = World.REGIONS().getter.get(tile);
		if (r != null) {
			double v = REGIOND.RES().total_mining.getD(r); 
			int k = (int) (0x0FFFF*v);
			return (ran & 0x0FFFF) <= k;
		}
		
		return false;
	}
	
}
