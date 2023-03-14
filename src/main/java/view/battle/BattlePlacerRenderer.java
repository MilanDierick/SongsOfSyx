package view.battle;

import static settlement.main.SETT.*;

import init.C;
import init.config.Config;
import init.sprite.SPRITES;
import settlement.army.Div;
import settlement.army.ai.util.DivTDataStatus;
import settlement.main.ON_TOP_RENDERABLE;
import settlement.main.RenderData;
import settlement.main.RenderData.RenderIterator;
import settlement.room.military.artillery.ArtilleryInstance;
import settlement.thing.projectiles.Trajectory;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import util.colors.GCOLORS_MAP;
import util.rendering.ShadowBatch;
import view.main.VIEW;

final class BattlePlacerRenderer extends ON_TOP_RENDERABLE{

	private final DivTDataStatus stat = new DivTDataStatus();
	private final BattlePlacer b;
	
	private final double[] xs = new double[Config.BATTLE.DIVISIONS_PER_ARMY];
	private final double[] ys = new double[Config.BATTLE.DIVISIONS_PER_ARMY];
	private final double[] ranges = new double[Config.BATTLE.DIVISIONS_PER_ARMY];
	private int ri;
	
	private boolean hovered;
	
	public BattlePlacerRenderer(BattlePlacer b) {
		this.b = b;
	}
	
	void add(boolean hovered) {
		this.hovered = hovered;
		this.add();
	}
	
	@Override
	public void render(Renderer r, ShadowBatch shadowBatch, RenderData data) {
		remove();
		
		//deployment bounds
		if (VIEW.b().state() != null && VIEW.b().state().deploying()) {
			RenderIterator it = data.onScreenTiles();
			GCOLORS_MAP.ok.bind();
			while(it.has()) {
				if (VIEW.b().state().deploymentBounds().holdsPoint(it.tx(), it.ty())) {
					int m = 0;
					for (int di = 0; di < DIR.ORTHO.size(); di++) {
						DIR d = DIR.ORTHO.get(di);
						if (VIEW.b().state().deploymentBounds().holdsPoint(it.tx()+d.x(), it.ty()+d.y())) {
							m |= d.mask();
						}
					}
					if (m != 0x0F) {
						SPRITES.cons().BIG.outline.render(r, m, it.x(), it.y());
					}
				}
				
				it.next();
				
			}
			COLOR.unbind();
		}
		
		//trajectory
		{
			
			ri = 0;
			for (Div d : b.s.selection()) {
				if (ri >= ranges.length)
					break;
				if(d.settings.ammo() != null) {
					d.order().status.get(stat);
					xs[ri] = stat.currentPixelCX()>>C.T_SCROLL;
					ys[ri] = stat.currentPixelCY()>>C.T_SCROLL;
					ranges[ri] = Trajectory.range(TERRAIN().get((int)xs[ri], (int)ys[ri]).heightEnd((int)xs[ri], (int)ys[ri]), d.settings.ammo().speed(d));
					ranges[ri] = (int)ranges[ri]/C.TILE_SIZE;
					//ranges[ri] *= ranges[ri];
					
					ri++;
				}
			}
			
			if (ri > 0) {
				
				GCOLORS_MAP.map_ok.bind();
				RenderIterator it = data.onScreenTiles();
				while(it.has()) {
					renderRange(it, r);
					
					it.next();
					
				}
				COLOR.unbind();
			}
		}
		
		renderArtillery(r, shadowBatch, data);
		
		
		
		if (hovered)
			b.current.render(r, shadowBatch, data);
		
	}
	
	private void renderRange(RenderIterator it, Renderer r) {
		for (int i = 0; i < ri; i++) {
			double x = Math.abs(xs[i]-it.tx());
			double y = Math.abs(ys[i]-it.ty());
			int l = (int)Math.sqrt(x*x+y*y);
			if (l == ranges[i]) {
				SPRITES.cons().BIG.dots.render(r, 0, it.x(), it.y());
			}
		}
	}
	
	private void renderArtillery(Renderer r, ShadowBatch shadowBatch, RenderData data) {
		
		for (ArtilleryInstance ins : b.s.artillery.selection()) {
			COORDINATE c = ins.centre();
			int sx = c.x();
			int sy = c.y();
			int rangeMin = ins.rangeMin();
			int rangeMax = ins.rangeMax();
			
			for (int di = -1; di <= 1; di++){
				DIR d = ins.dir().next(di);
				boolean in = false;
				int min = (int) (rangeMin/d.tileDistance());
				int max = (int) (rangeMax/d.tileDistance());
				for (int i = min; i <= max; i+= C.TILE_SIZE) {
					int tx = (sx + i*d.x())>>C.T_SCROLL;
					int ty = (sy + i*d.y())>>C.T_SCROLL;
					if (data.tBounds().holdsPoint(tx, ty)) {
						in = true;
						SPRITES.cons().ICO.arrows2.get(d.id()).render(r, tx*C.TILE_SIZE-data.offX1(), ty*C.TILE_SIZE-data.offY1());
					}else if (in)
						break;
				}
			}
			
		}
		
	}
	


	
}
