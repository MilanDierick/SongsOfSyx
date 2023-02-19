package settlement.tilemap;

import static settlement.main.SETT.*;

import java.io.IOException;

import init.C;
import init.paths.PATHS;
import init.sprite.SPRITES;
import settlement.main.RenderData;
import settlement.main.SETT;
import snake2d.CORE;
import snake2d.Renderer;
import snake2d.util.color.*;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.map.MAP_DOUBLEE;
import snake2d.util.map.MAP_INTE;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.Bitsmap1D;
import snake2d.util.sprite.TILE_SHEET;
import snake2d.util.sprite.TextureCoords;
import snake2d.util.sprite.TileTexture.TileTextureScroller;
import util.spritecomposer.*;
import util.spritecomposer.ComposerThings.ITileSheet;
import view.sett.IDebugPanelSett;
import view.tool.*;

public class Grass extends TileMap.Resource{
	
	private final Bitsmap1D data = new Bitsmap1D(0, 4, TAREA);
	
	private final static int SET = 16;
	public final static int TYPES = 0x0F;
	private final static double TYPESI = 1.0/TYPES;
	private final TILE_SHEET sheet;
	private final TILE_SHEET sheetMask;
	private final TILE_SHEET moss;
	
	private final Colors colors;
	private final TileTextureScroller dis2 = SPRITES.textures().dis_low.scroller(12*6, -12*5.5);

	private final int[] tts = {
		-1,0,0,1,1,2,2,3,3,4,4,5,5,6,7,7,
	};
	
	public Grass() throws IOException{
		
		new ComposerThings.IInit(PATHS.SPRITE_SETTLEMENT_MAP().get("Grass"), 972, 390);
		
		colors = new Colors();
		
		sheetMask = (new ITileSheet() {

			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				ComposerDests.Tile t = d.s24;
				final ComposerSources.Full f = s.full;
				f.init(0, f.body().y2(), 1, 1, 4, 4, t);
				f.setVar(0).paste(true);
				return t.saveGame();

			}
		}).get();
		
		sheet = (new ITileSheet() {

			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				ComposerDests.Tile t = d.s24;
				final ComposerSources.Singles f = s.singles;
				f.init(0, s.full.body().y2(), 1, 1, 16, 8, t);
				f.setVar(0).paste(true);
				return t.saveGame();

			}
		}).get();
		
		moss = (new ITileSheet(PATHS.SPRITE_SETTLEMENT_MAP().get("Moss"), 792, 108) {

			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				ComposerDests.Tile t = d.s24;
				final ComposerSources.Full f = s.full;
				f.init(0, 0, 1, 1, 16, 4, t);
				f.setVar(0).paste(true);
				return t.saveGame();

			}
		}).get();
		
		final PlacableMulti ppu = new PlacableMulti("Remove") {
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				data.set(tx+ty*TWIDTH, CLAMP.i(data.get(tx+ty*TWIDTH)-1, 0, TYPES));
				
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				return null;
			}
		};
		
		final PlacableMulti pp = new PlacableMulti("Grass") {
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				data.set(tx+ty*TWIDTH, CLAMP.i(data.get(tx+ty*TWIDTH)+1, 0, TYPES));
				
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				return null;
			}
			
			@Override
			public PLACABLE getUndo() {
				return ppu;
			}
		};
		
		IDebugPanelSett.add(pp);
		
	}
	
	public void grow(int tx, int ty) {
		grow (tx, ty, 1 + RND.rInt(2));
	}
	
	private final double[] treepenalty = new double[] {
		0.1,0.20,0.20,0.1
	};
	
	public void grow(int tx, int ty, int amount) {
		int tile = tx+ty*TWIDTH;
		double d = SETT.FERTILITY().baseD.get(tile) + SETT.ENV().environment.WATER_SWEET.get(tile);
		if (d > 0.4) {
			for (int i = 0; i < treepenalty.length; i++) {
				if (SETT.TERRAIN().TREES.isTree(tx, ty+i)) {
					d = CLAMP.d(d -treepenalty[i], 0.4, d);
				}
			}
		}
		
		
		int b = CLAMP.i((int) (TYPES*d), 0, TYPES);
		int c = data.get(tile);
		
		if (c < b) {
			c += amount;
			if (c > b)
				c = b;
			
		}else if (c > b) {
			c -= amount;
			if (c < b)
				c = b;
			
		}
		
		data.set(tile, c);
	}
	
	@Override
	protected void update(float ds) {
		colors.update(ds);
		double w = Math.pow(SETT.WEATHER().wind.getD(), 1.5);
		if (w > 0.1)
			dis2.update(ds*w);
	}
	
	private final OPACITY[] op = new OPACITY[TYPES];
	{
		for (int i = 0; i < TYPES; i++) {
			int p = (int) (127*(i+1.0)/TYPES);
			op[i] = new OpacityImp(p);
 		}
	}
	
	void render(Renderer r, RenderData data) {
		
		RenderData.RenderIterator i = data.onScreenTiles(1,1,1,1);
		
//		double growth = 1.0;
//		{
//			double m = SETT.WEATHER().moisture.getD();
//			if (m < 0.25) {
//				growth  = m/0.25;
//			}
//		}
//		growth *= SETT.WEATHER().growth.getD();
//		
//		growth = CLAMP.d(growth, 0.25, 1.0);
		double w = Math.pow(SETT.WEATHER().wind.getD(), 1.5)*0.5;
		while(i.has()) {
			
			int ran = i.ran();
			
			int colC = this.data.get(i.tile());
			int c = tts[colC];
			
			if (colC == 1)
				c -= (ran&0b011);
			if (colC == 2)
				c -= (ran&0b001);
			if (colC == 3)
				c -= (ran&0b01);
			
			if (colC == 5)
				c -= (ran&0b0001);
			
			
			ran = ran>>2;
			
			
			
			
			
			if (c >= 0) {
				
				int m = SETT.MINERALS().amountInt.get(i.tile())>>1;
				c = CLAMP.i(c-m, 0, c);
				
				
				int d = (int) (((ran&0x07)-7)*C.SCALE);
				ran = ran >> 3;
				int x = i.x() + d;
				d = (int) (((ran&0x07)-7)*C.SCALE);
				ran = ran >> 3;
				int y = i.y() + d;
				if (TERRAIN().get(i.tile()).roofIs()) {
					COLOR.unbind();
					c = (c+1)>>3;
					moss.render(r, c*SET+(i.ran()&0x0F), x, y);
				}else {
					if (SETT.FLOOR().getter.is(i.tile()))
						c = CLAMP.i(c, 0, 1);
					
					if (!SETT.ROOMS().map.is(i.tile())) {
//						c = CLAMP.i((int) ((c+(ran&0b011))*growth), 0, c);
//						ran = ran >> 2;
					}
//					
					int mm = 0;
					if (CORE.renderer().getZoomout() <= 1) {
						if (current.get(i.tx(), i.ty(), DIR.W) == 0) {
							mm |= 1;
							x = i.x()-8*C.SCALE;
						}else if (current.get(i.tx(), i.ty(), DIR.E) == 0)
							x = i.x()-8*C.SCALE;
						if (current.get(i.tx(), i.ty(), DIR.N) == 0) {
							mm |= 2;
							y = i.y()-8*C.SCALE;
						}else if(current.get(i.tx(), i.ty(), DIR.S) == 0)
							y = i.y()-8*C.SCALE;
						
						mm += 4*(ran&3);
						ran = ran >> 2;
					}
					
					
					
					colors.get(colC-1, i.ran()).bind();
					//colorMask.bind();
					sheetMask.renderTextured(sheet.getTexture(c*SET+(ran&0x0F)), mm, x, y);
					
					op[c].bind();
					TextureCoords t = SPRITES.textures().dots.get(i.tx(), i.ty(), 0, 0);
					
					CORE.renderer().renderDisplaced(i.x(), i.x()+C.TILE_SIZE, i.y(), i.y()+C.TILE_SIZE, w, dis2.get(i.tx(), i.ty()), t);
					OPACITY.unbind();
//					if (c >= 4 && mm == 0)
//						i.hiddenSet();
				}
				
			}
			i.next();
		}
		COLOR.unbind();
		
	}
	
	public COLOR color(int ran) {
		return colors.get(4, ran);
	}
	
	
	@Override
	protected void save(FilePutter saveFile) {
		data.save(saveFile);
		
	}

	@Override
	protected void load(FileGetter saveFile) throws IOException {
		data.load(saveFile);
	}
	
	
	@Override
	protected void clearAll() {
		data.clear();
	}
	
	public final MAP_DOUBLEE current = new MAP_DOUBLEE.DoubleMapImp(TWIDTH, THEIGHT) {
		
		@Override
		public double get(int tile) {
			 return (double) data.get(tile)*TYPESI;
		}
		
		@Override
		public MAP_DOUBLEE set(int tile, double value) {
			currentI.set(tile, (int) (value*TYPES));
			return this;
		}
	};
	
	public final MAP_INTE currentI = new MAP_INTE.INT_MAPEImp(TWIDTH, THEIGHT) {
		
		@Override
		public int get(int tile) {
			return data.get(tile);
		}
		
		@Override
		public MAP_INTE set(int tile, int value) {
			value = CLAMP.i(value, 0, TYPES);
			data.set(tile, value);
			return this;
		}
	};
	
	private static final class Colors {
		
		private final static int RAN = 4;
		
		private final COLOR[][] c_base = new COLOR[TYPES][RAN];
		private final ColorImp[][] c_current = new ColorImp[TYPES][RAN];
		
		private final COLOR dry;
		private final COLOR winter;

		
		Colors() throws IOException{
			COLOR fertile = new ComposerThings.IColorSamplerSingle() {
				
				@Override
				protected COLOR init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.full.init(0, 0, 1, 1, 8, 1, d.s24);
					return s.full.sample();
				}
			}.getHalf();
			
			COLOR infertile = new ComposerThings.IColorSamplerSingle() {
				
				@Override
				protected COLOR init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					
					s.full.setSkip(1, 1);
					return s.full.sample();
				}
			}.getHalf();
			
			dry = new ComposerThings.IColorSamplerSingle() {
				
				@Override
				protected COLOR init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					
					s.full.setSkip(1, 2);
					return s.full.sample();
				}
			}.getHalf();
			
			winter = new ComposerThings.IColorSamplerSingle() {
				
				@Override
				protected COLOR init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					
					s.full.setSkip(1, 3);
					return s.full.sample();
				}
			}.getHalf();
			
			int skip = 5;
			
			COLOR[] cols = COLOR.interpolate(infertile, fertile, TYPES-4);
			
			
			
			for (int i = 0; i < TYPES; i++) {
				COLOR c = cols[CLAMP.i(i-skip, 0, i)];
				
				c_base[i][0] = c;
				c_current[i][0] = new ColorImp(c_base[i][0]);
				for (int k = 1; k < RAN; k++) {
					c_base[i][k] = c.shade(RND.rFloat1(0.05));
					c_current[i][k] = new ColorImp(c_base[i][k]);
				}
				
			}
		}
		
		private COLOR get(int c, int ran) {
			return c_current[c][ran & (RAN-1)];
		}
		
		void update(float ds) {
			double m = SETT.WEATHER().moisture.getD();
			if (m < 0.25) {
				m = 0.05;
			}else if (m < 0.5) {
				m -= 0.25;
				m /= 0.25;
				m = CLAMP.d(m, 0.05, 1);
			}else {
				m = 1;
			}
			set(m, 1.0-SETT.WEATHER().growth.getD());
		}
		
		private final ColorImp tmp = new ColorImp();
		
		private void set(double moist, double winter) {
			for (int a = 0; a < TYPES; a++) {
				for (int b = 0; b < RAN; b++) {
					tmp.interpolate(dry, c_base[a][b], moist);
					c_current[a][b].interpolate(tmp, this.winter, winter*0.75);
				}
			}
		}
		
	}
	
}