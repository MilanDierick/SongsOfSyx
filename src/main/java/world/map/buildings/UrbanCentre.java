package world.map.buildings;

import game.GAME;
import game.time.TIME;
import init.C;
import init.RES;
import settlement.main.RenderData;
import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.misc.CLAMP;
import util.rendering.ShadowBatch;
import world.World;
import world.map.regions.REGIOND;
import world.map.regions.Region;

class UrbanCentre {
	
	private final Urban urban = new Urban();
	private final Capitol capitol = new Capitol();

	protected void renderOnGround(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator it) {
		Region reg = World.REGIONS().getter.get(it.tile());
		if (reg == null)
			return;
		int data = getData(it.tx(), it.ty());
		
		if (REGIOND.faction(reg) != null && REGIOND.faction(reg).kingdom().realm().capitol() == reg) {
			capitol.renderOnGround(r, it, data);
		}else {
			
			urban.renderOnGround(r, it, data);
			
			
		}
	}
	
	protected void renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator it) {
		
		Region reg = World.REGIONS().getter.get(it.tile());
		if (reg == null)
			return;
		
		int data = getData(it.tx(), it.ty());
		
		if (REGIOND.faction(reg) != null && REGIOND.faction(reg).kingdom().realm().capitol() == reg) {
			capitol.renderAbove(r, s, it, data);
		}else {
			urban.renderAbove(r, s, it, data);
		}
		
	}
	
	protected void renderAboveTerrain(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator it) {
		
	}
	
	private int getData(int tx, int ty) {
		Region r = World.REGIONS().getter.get(tx, ty);
		if (r == null)
			return 0;
		int dx = CLAMP.i(1 + tx-r.cx(), 0, 3);
		int dy = CLAMP.i(1 + ty-r.cy(), 0, 3);
		return dx+dy*4;
	}
	
	private void renderSiege(Region reg, SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator it, int data) {

		if (!reg.besieged())
			return;
		int tx = (data%4);
		int ty = (data/4);
		
		
		if (tx >= 1 && ty >= 1){
			
			tx -= 1;
			ty -= 1;
			
			int dx = tx -1;
			int dy = ty -1;
			
			int d = (int) ((C.TILE_SIZEH/2 + 16));
			
			int x = -C.TILE_SIZE + d*dx;
			int y = -C.TILE_SIZE + d*dy;
			
			
			int t = tx+ty*3;
			World.BUILDINGS().sprites.siege.render(r, t, it.x()+x, it.y()+y);
			s.setHard();
			s.setHeight(4).setDistance2Ground(0);
			World.BUILDINGS().sprites.siege.render(s, t, it.x()+x, it.y()+y);
			
			
		}
	}
	
	private class Urban {
		
		private final int[][] mTownHouses = new int[][] {
			{-1, 0, 1, 0,-1,-1,-1,-1},
			{ 0, 2, 3, 2, 0,-1,-1,-1},
			{ 1, 3, 4, 3, 1,-1,-1,-1},
			{ 0, 2, 3, 2, 0,-1,-1,-1},
			{-1, 0, 1, 0,-1,-1,-1,-1},
			{-1,-1,-1,-1,-1,-1,-1,-1},
			{-1,-1,-1,-1,-1,-1,-1,-1},
			{-1,-1,-1,-1,-1,-1,-1,-1},
		};
		
		private final int[][] mTownFarms = new int[][] {
			{-1, 0,-1, 0,-1,-1,-1,-1},
			{-1,-1,-1,-1,-1,-1,-1,-1},
			{ 0,-1,-1,-1, 0,-1,-1,-1},
			{-1,-1,-1,-1,-1,-1,-1,-1},
			{-1, 0,-1, 0,-1,-1,-1,-1},
			{-1,-1,-1,-1,-1,-1,-1,-1},
			{-1,-1,-1,-1,-1,-1,-1,-1},
			{-1,-1,-1,-1,-1,-1,-1,-1},
		};
		
		void renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator it, int data) {
			
			int ran = it.ran();
			
			int tx = (data%4)*2;
			int ty = (data/4)*2;
			Region reg = World.REGIONS().getter.get(it.tile());
			double p = REGIOND.POP().popValue(reg);
			s.setSoft();
			s.setHeight(1);
			
			{
				int dd = C.TILE_SIZEH/2;
				for (int y = 0; y < 2; y++) {
					for (int x = 0; x < 2; x++) {
						int t = mTownHouses[y+ty][x+tx];
						if (t != -1) {
							t = t - 4 + (int)(p*4) + (ran & 0b011);
							ran = ran >> 2;
							if (t < 0)
								continue;
							if (t > 3)
								t = 3;
							int x1 = it.x()+x*C.TILE_SIZEH+dd;
							int y1 = it.y()+y*C.TILE_SIZEH+dd;
							World.BUILDINGS().sprites.houses.render(r, 16*t+(ran&0x0F),x1, y1);
							World.BUILDINGS().sprites.houses.render(s, 16*t+(ran&0x0F),x1, y1);
							ran = ran >> 4;
							if (TIME.light().nightIs() && (TIME.light().partOfCircular()*4 > (ran&0x07))) {
								x1 += C.TILE_SIZEH/2+(GAME.intervals().get05()+it.ran() & 0b11);
								y1 += C.TILE_SIZEH/2+(GAME.intervals().get05()+(it.ran()>>4) & 0b11);
								CORE.renderer().renderUniLight(x1, y1, 2, 64);
								
							}
							ran = ran >> 3;
						}
						
					}
				}
			}
			
			if (tx == 2 && ty == 2){
				int m = (int) Math.round(REGIOND.MILITARY().soldiers.getD(reg)*4);
				if (m > 0) {
					m -= 1;
					World.BUILDINGS().sprites.centre.render(r, m, it.x()-16, it.y()-16);
					s.setHard();
					s.setHeight(1).setDistance2Ground(0);
					World.BUILDINGS().sprites.centre.render(s, m, it.x()-16, it.y()-16);
				}
					
				
				
			}
			
			renderSiege(reg, r, s, it, data);
			
		}
		
		void renderOnGround(SPRITE_RENDERER r, RenderData.RenderIterator it, int data) {
			int tx = (data%4)*2;
			int ty = (data/4)*2;
			Region reg = World.REGIONS().getter.get(it.tile());
			int p = (int) (REGIOND.POP().popValue(reg)*8);
			
			for (int y = 0; y < 2; y++) {
				for (int x = 0; x < 2; x++) {
					int t = mTownFarms[y+ty][x+tx];
					if (t != -1 && p >= (it.ran()&0x07)) {
						World.BUILDINGS().sprites.farms.render(r, it.ran() % 24, it.x()+x*C.TILE_SIZEH, it.y()+y*C.TILE_SIZEH);
					}
					
				}
			}
		}
		
		
		
	}
	
	private class Capitol {
		

		private final int[][] mTownHouses = new int[][] {
			{-1, 0, 1, 1, 1, 1, 0,-1},
			{ 0, 1, 2, 2, 2, 2, 1, 0},
			{ 1, 2, 3, 4, 4, 3, 2, 1},
			{ 1, 2, 4, 5, 5, 4, 2, 1},
			{ 1, 2, 4, 5, 5, 4, 2, 1},
			{ 1, 2, 3, 4, 4, 3, 2, 1},
			{ 0, 1, 2, 2, 2, 2, 1, 0},
			{-1, 0, 1, 1, 1, 1, 0,-1},
		};
		
		private final int[][] mTownFarms = new int[][] {
			{-1, 0,-1, 0,-1, 0,-1, 0},
			{-1,-1,-1,-1,-1,-1,-1,-1},
			{ 0,-1,-1,-1,-1,-1, 0,-1},
			{-1,-1,-1,-1,-1,-1,-1,-1},
			{ 0,-1,-1,-1,-1,-1, 0,-1},
			{-1,-1,-1,-1,-1,-1,-1,-1},
			{-1, 0,-1, 0,-1, 0,-1,-1},
			{-1,-1,-1,-1,-1,-1,-1,-1},
		};
		

		
		private final int shadow = 9*4;
		
		void renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator it, int data) {
			
			int ran = it.ran();
			
			int tx = (data%4);
			int ty = (data/4);
			
			Region reg = World.REGIONS().getter.get(it.tile());
			double p = REGIOND.faction(reg).capitol().population.total().get()/(double)RES.config().WORLD.POPULATION_MAX_CAPITOL;

			s.setSoft();
			s.setHeight(1);
			
			{
				for (int y = 0; y < 2; y++) {
					for (int x = 0; x < 2; x++) {
						int t = mTownHouses[y+ty*2][x+tx*2];
						if (t != -1) {
							t = t - 5 + (int)(p*4) + (ran & 0b011);
							ran = ran >> 2;
							if (t < 0)
								continue;
							if (t > 3)
								t = 3;
							int x1 = it.x()-C.TILE_SIZEH+x*C.TILE_SIZEH;
							int y1 = it.y()-C.TILE_SIZEH+y*C.TILE_SIZEH;
							World.BUILDINGS().sprites.houses.render(r, 16*t+(ran&0x0F),x1, y1);
							World.BUILDINGS().sprites.houses.render(s, 16*t+(ran&0x0F),x1, y1);
							ran = ran >> 4;
							if (TIME.light().nightIs() && (TIME.light().partOfCircular()*4 > (ran&0x07))) {
								x1 += C.TILE_SIZEH/2+(GAME.intervals().get05()+it.ran() & 0b11);
								y1 += C.TILE_SIZEH/2+(GAME.intervals().get05()+(it.ran()>>4) & 0b11);
								CORE.renderer().renderUniLight(x1, y1, 2, 64);
								
							}
							ran = ran >> 3;
						}
						
					}
				}
			}
			
			
			
			if (tx >= 1 && ty >= 1){
				
				int m = (int) Math.round(CLAMP.d(REGIOND.MILITARY().soldiers.getD(reg), 0, 1)*4);
				tx -= 1;
				ty -= 1;
				
				int dx = tx -1;
				int dy = ty -1;
				
				int d = (int) ((C.TILE_SIZEH/2)*p);
				
				int x = -C.TILE_SIZE + d*dx;
				int y = -C.TILE_SIZE + d*dy;
				
				int t = tx+ty*3 + 9*m;
				World.BUILDINGS().sprites.wallCity.render(r, t, it.x()+x, it.y()+y);
				s.setHard();
				s.setHeight(4).setDistance2Ground(0);
				World.BUILDINGS().sprites.wallCity.render(s, t+shadow, it.x()+x, it.y()+y);
				
				
			}
			
			renderSiege(reg, r, s, it, data);
			
		}
		
		void renderOnGround(SPRITE_RENDERER r, RenderData.RenderIterator it, int data) {
			int tx = (data%4)*2;
			int ty = (data/4)*2;
			Region reg = World.REGIONS().getter.get(it.tile());
			int p = (int) (REGIOND.POP().popValue(reg)*8);
			for (int y = 0; y < 2; y++) {
				for (int x = 0; x < 2; x++) {
					int t = mTownFarms[y+ty][x+tx];
					if (t != -1 && (it.ran()&0x07) >= p) {
						World.BUILDINGS().sprites.farms.render(r, it.ran() % 24, it.x()+x*C.TILE_SIZEH-C.TILE_SIZEH, it.y()+y*C.TILE_SIZEH-C.TILE_SIZEH);
					}
					
				}
			}
		}
		
		
	}

	
}
