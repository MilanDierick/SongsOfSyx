package settlement.tilemap.floor;

import static settlement.main.SETT.*;

import java.io.IOException;

import init.biomes.CLIMATES;
import init.paths.PATHS;
import settlement.main.SETT;
import settlement.tilemap.TileMap;
import settlement.tilemap.TileMap.SMinimapGetter;
import snake2d.LOG;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.map.MAP_OBJECT;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.TILE_SHEET;
import snake2d.util.sprite.TextureCoords;
import util.gui.misc.*;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;
import util.spritecomposer.*;
import view.interrupter.ISidePanel;
import view.main.VIEW;
import view.sett.IDebugPanelSett;
import view.tool.*;

public class Ground extends TileMap.Resource{

	private static final int FERTILITIES = 8;
	private static final double FERTILITYI = 1.0/FERTILITIES;
	
	public final static int VARS = 16*4;
	
	private final byte[] data = new byte[SETT.TWIDTH*SETT.TWIDTH];
	private final ArrayList<GROUND> all = new ArrayList<GROUND>(15);
	public final Minables minerals = new Minables();

	public final GROUND ROCK;
	
	private final TILE_SHEET s_masks;
	private final TILE_SHEET s_masks_hard;
	private final TILE_SHEET s_normal;
	private final TILE_SHEET s_rock;
	private final TILE_SHEET s_sand;
	
	private final ColorImp cdry = new ColorImp();
	private final ColorImp cwet = new ColorImp();
	
	private final static int breakPoint = FERTILITIES/2;
	
	public Ground(TileMap m) throws IOException{
		
		
		s_masks = new ComposerThings.ITileSheet(PATHS.SPRITE_SETTLEMENT_MAP().get("Ground"), 576, 372) {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.house.init(0, 0, 4, 1, d.s16);
				for (int i = 0; i < 4; i++)
					s.house.setVar(i).paste(true);
				return d.s16.saveGame();
			}
		}.get();
		
		s_masks_hard = new ComposerThings.ITileSheet() {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.house.init(0, s.house.body().y2(), 2, 1, d.s16);
				for (int i = 0; i < 2; i++)
					s.house.setVar(i).paste(true);
				return d.s16.saveGame();
			}
		}.get();
		
		s_normal = new ComposerThings.ITileSheet() {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.full.init(0, s.house.body().y2(), 1, 1, 16, 4, d.s16);
				s.full.paste(true);
				return d.s16.saveGame();
			}
		}.get();
		
		s_rock = new ComposerThings.ITileSheet() {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.full.init(0, s.full.body().y2(), 1, 1, 16, 4, d.s16);
				s.full.paste(true);
				return d.s16.saveGame();
			}
		}.get();
		
		s_sand = new ComposerThings.ITileSheet() {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.full.init(0, s.full.body().y2(), 1, 1, 16, 4, d.s16);
				s.full.paste(true);
				return d.s16.saveGame();
			}
		}.get();
		
		for (int i = 0; i < FERTILITIES; i++) {
			
			if (i == 0)
				new GROUND(0, 1, s_sand, s_masks, false);
			else
				new GROUND(i, bg(i), s_normal, s_masks, false);
		}
		
		ROCK = new GROUND(breakPoint, breakPoint, s_rock, s_masks_hard, true);
		
		
		PLACABLE p = new PlacableMulti("ground") {
			
			GROUND g = all.get(0);
			private final LIST<CLICKABLE> cl;
			{
				GDropDown<CLICKABLE> dr = new GDropDown<>("type");
				for (int i = 0; i < FERTILITIES; i++) {
					final int k = i;
					dr.add(new GButt.Glow("" + i) {
						
						@Override
						protected void clickA() {
							g = all.get(k);
						};
						
						
					});
				}
				dr.add(new GButt.Glow("rock") {
						
						@Override
						protected void clickA() {
							g = ROCK;
						};
						
						
					});
				dr.init();
				cl = new ArrayList<>(dr);
				
			}
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				g.placeFixed(tx, ty);
				
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				return SETT.IN_BOUNDS(tx,ty) ? null : E; 
			}
			
			@Override
			public LIST<CLICKABLE> getAdditionalButt() {
				return cl;
			}
		};
		IDebugPanelSett.add(p);
		
		IDebugPanelSett.add("Change ground color", new ACTION() {
			int i = 0;
			
			@Override
			public void exe() {
				i++;
				i%= CLIMATES.ALL().size();
				COLOR wet = CLIMATES.ALL().get(i).colorGroundWet;
				COLOR dry = CLIMATES.ALL().get(i).colorGroundDry;
				LOG.ln(i);
				setColors(dry, wet);
			}
		});
		
		IDebugPanelSett.add("ground color", new ACTION() {
			
			@Override
			public void exe() {
				VIEW.s().panels.add(new Debug(), true);
			}
		});
		
	}
	
	public void setColors(COLOR dry, COLOR wet) {
		this.cwet.set(wet);
		this.cdry.set(dry);
		
//		this.cwet.set(SETT.ENV().climate().colorGroundWet);
//		this.cdry.set(SETT.ENV().climate().colorGroundDry);

		for (int i = 0; i < FERTILITIES; i++) {
			
			double d = i/(FERTILITIES-1.0);
			ColorImp cwet = new ColorImp().interpolate(dry, wet, CLAMP.d(0.75 + d*0.25, 0, 1));
			
			ColorImp cdry = new ColorImp().interpolate(wet, dry, CLAMP.d(0.25 + 1.0-d, 0, 1)); 
			
			ColorImp miniC = new ColorImp().interpolate(dry, wet, 0.65*d);
			
			if (i == 0) {
				cwet = dry.shade(0.75);
			}
			
			all.get(i).dry.set(cdry);
			all.get(i).wet.set(cwet);
			all.get(i).miniC.set(miniC);
			all.get(i).tmp.set(cwet);
		}
		
		ROCK.miniC.set(COLOR.WHITE25);
		
	}
	
	@Override
	protected void update(float ds) {
		double d = SETT.WEATHER().moisture.getD();
		for (GROUND g : all) {
			g.tmp.interpolate(g.dry, g.wet, d);
			if (g.special)
				g.tmp.set(COLOR.WHITE100);
			
		}
		
		super.update(ds);
	}
	
	public GROUND fertilityGet(double fer) {
		fer = CLAMP.d(fer, 0, 1);
		return all.get((int) Math.ceil(fer*(FERTILITIES-1)));
	}
	
	public GROUND fertilityGet(int tx, int ty) {
		GROUND t = fertilityGet(SETT.FERTILITY().baseD.get(tx, ty)+SETT.ENV().environment.WATER_SWEET.get(tx, ty));
		return t;
	}
	
	void render(Renderer r, RenderIterator it) {
		int tile = it.tile();
		int ran = it.ran();
		int x = it.x();
		int y = it.y();
		getter.get(tile).render(r, data[tile]&0x0F, ran, x, y);
		COLOR.unbind();
		minerals.render(r, tile, ran, x, y);
		
	}
	
	public void renderMinerals(Renderer r, int tile, int ran, int x, int y) {
		minerals.render(r, tile, ran, x, y);
	}
	
	public TextureCoords getTexture(int tile, int ran) {
		return getter.get(tile).getTexture(ran);
	}
	
	public final class GROUND {
		
		protected final int index;
		protected final TILE_SHEET sheet;
		protected final TILE_SHEET mask;

		public final int fertility;
		protected final int ferBG;
		
		protected final ColorImp miniC = new ColorImp();;
		protected final ColorImp dry = new ColorImp();
		protected final ColorImp wet = new ColorImp();
		protected final ColorImp tmp = new ColorImp();
		
		public final boolean special;
		
		protected GROUND(int fer, int ferBg, TILE_SHEET sheet, TILE_SHEET mask, boolean special){
			index = all.add(this);
			tmp.set(wet);
			fertility = fer;
			this.ferBG = ferBg;
			this.sheet = sheet;
			this.mask = mask;
			this.special = special;
		}
		
		void render(Renderer r, int mask, int ran, int x, int y) {
			if (mask == 0x0F) {
				tmp.bind();
				sheet.render(r, ran&(VARS-1), x, y);
			}else {
			 	all.get(ferBG).tmp.bind();
				s_normal.render(r, ran&(VARS-1), x, y);
				tmp.bind();
				mask += 16*(ran&(this.mask.tiles()/16)-1);
				ran = ran >> 2;
				this.mask.renderTextured(sheet.getTexture(ran&(VARS-1)), mask, x, y);
			}
			
		}
		
		public COLOR col() {
			return tmp;
		}
		
		public void placeRaw(int x, int y) {
			if (IN_BOUNDS(x, y))
				data[y*SETT.TWIDTH+x] = (byte) (index<<4);
		}
		
	
		COLOR getColor() {
			return miniC;
		}

		
		public void placeFixed(int x, int y) {
			
			GROUND old = getter.get(x, y);
			place(x, y);
			if (old != getter.get(x, y)) {
				SETT.TILE_MAP().miniCUpdate(x, y);
				for (int i = 0; i < DIR.ORTHO.size(); i++){
					DIR d = DIR.ORTHO.get(i);
					int tx = x+d.x();
					int ty = y+d.y();
					if (!IN_BOUNDS(tx, ty))
						continue;
					getter.get(tx, ty).placeFixed(tx, ty);
				}
			}
			
		}
		
		public boolean is(int x, int y) {
			return getter.get(x, y) == this;
		}
		
		public double fertility() {
			return fertility*FERTILITYI;
		}
		
		private void place(int x, int y) {
			
			if (fertility == breakPoint) {
				int m = 0;
				for (int i = 0; i < DIR.ORTHO.size(); i++){
					DIR d = DIR.ORTHO.get(i);
					int tx = x+d.x();
					int ty = y+d.y();
					if (!IN_BOUNDS(tx, ty) || getter.get(tx, ty) == this) {
						m |= d.mask();
						
					}
				}
				m |= index << 4;
				data[x+y*SETT.TWIDTH] = (byte) m;
				return;
			}
			
			int hi = fertility;
			int lo = fertility;
			
			
			for (int i = 0; i < DIR.ORTHO.size(); i++){
				DIR d = DIR.ORTHO.get(i);
				int tx = x+d.x();
				int ty = y+d.y();
				if (!IN_BOUNDS(tx, ty))
					continue;
				int g = getter.get(tx, ty).fertility;
				hi = Math.max(hi, g);
				lo = Math.min(lo, g);
			}
			
			if(fertility < breakPoint && hi-1 > fertility) {
				int f = hi-1;
				f = Math.min(f, breakPoint);
				all.get(f).placeFixed(x, y);
			}else if (fertility > breakPoint && lo < fertility-1) {
				int f = lo+1;
				
				f = Math.max(f, breakPoint);
				all.get(f).placeFixed(x, y);
			}else {
				setCode(x, y);
			}
		}
		
		void setCode(int x, int y) {
			int m = 0;
			for (int i = 0; i < DIR.ORTHO.size(); i++){
				DIR d = DIR.ORTHO.get(i);
				int tx = x+d.x();
				int ty = y+d.y();
				if (!IN_BOUNDS(tx, ty) || getter.get(tx, ty).fertility == fertility || getter.get(tx, ty).ferBG == fertility) {
					m |= d.mask();
					
				}
			}
			m |= index << 4;
			data[x+y*SETT.TWIDTH] = (byte) m;
		}

		TextureCoords getTexture(int ran) {
			return sheet.getTexture(ran&15);
		}
	}
	
	private static int bg(int fer) {
		if (fer < breakPoint)
			return fer+1;
		else if (fer > breakPoint)
			return fer-1;
		else
			return fer;
	}
	
	public void adjust(int tile, int tx, int ty) {
		GROUND g = getter.get(tile);
		if (g.special)
			return;
		GROUND t = fertilityGet(SETT.FERTILITY().baseD.get(tile)+SETT.ENV().environment.WATER_SWEET.get(tile));
		int d = CLAMP.i(t.fertility-g.fertility, -1, 1);
		if (d != 0) {
			all.get(g.fertility + d).placeFixed(tx, ty);
		}
	}
	

	
	@Override
	protected void save(FilePutter saveFile) {
		saveFile.bs(data);
		minerals.save(saveFile);
		cdry.save(saveFile);
		cwet.save(saveFile);
	}

	@Override
	protected void load(FileGetter saveFile) throws IOException {
		saveFile.bs(data);
		minerals.load(saveFile);
		cdry.load(saveFile);
		cwet.load(saveFile);
		setColors(cdry, cwet);
	}
	
	
	public final MAP_OBJECT<GROUND> getter = new MAP_OBJECT<Ground.GROUND>() {
		
		@Override
		public GROUND get(int tx, int ty) {
			if (SETT.IN_BOUNDS(tx, ty))
				return all.get((data[ty*SETT.TWIDTH+tx] >> 4)&0x0F);
			return null;
		}
		
		@Override
		public GROUND get(int tile) {
			return all.get((data[tile] >> 4)&0x0F);
		}
	};
	
	@Override
	protected void clearAll() {
		for (int y = 0; y < SETT.TWIDTH; y++){
			for (int x = 0; x < SETT.TWIDTH; x++){
				data[y*SETT.TWIDTH+x] = 0;
				minerals.CLEAR.place(x, y);
			}
		}
	} 

	private static class Debug extends ISidePanel {
		
		private final ColorImp dry = new ColorImp(COLOR.WHITE50);
		private final ColorImp wet = new ColorImp(COLOR.WHITE50);
		
		Debug(){
			titleSet("ground color");
			dry.set(SETT.GROUND().cdry);
			wet.set(SETT.GROUND().cwet);
			section.addDown(2, new GColorPicker(false, "dry") {
				
				@Override
				public ColorImp color() {
					return dry;
				}
				
				@Override
				public void change() {
					SETT.GROUND().setColors(dry, wet);
				}
				
			});
			
			section.addDown(2, new GColorPicker(false, "wet") {
				
				@Override
				public ColorImp color() {
					return wet;
				}
				
				@Override
				public void change() {
					SETT.GROUND().setColors(dry, wet);
				}
				
			});
		}
		
	}

	public final SMinimapGetter minimap = new SMinimapGetter() {
		
		@Override
		public COLOR miniColorPimped(ColorImp origional, int x, int y, boolean northern, boolean southern) {
			if (minerals.getter.is(x, y)) {
				return minerals.miniC(origional, getter.get(x, y).miniC, x, y);
			}
			
			for (DIR d : DIR.ALL) {
				if (TERRAIN().WATER.is.is(x+d.x(), y+d.y()) || TERRAIN().MOUNTAIN.is(x+d.x(), y+d.y())) {
					origional.shadeSelf(0.75);
					return origional;
				}
			}
			if (northern || southern)
				origional.shadeSelf(0.9);
			return origional;
		}
		
		@Override
		public COLOR miniC(int x, int y) {
			return getter.get(x, y).miniC;
		}
	};

	public void render(Renderer r, float ds, ShadowBatch s, RenderData data) {
		RenderData.RenderIterator i = data.onScreenTiles();

		while (i.has()) {
			GROUND().render(r, i);
			GROUND().renderMinerals(r, i.tile(), i.ran(), i.x(), i.y());
			//FLOOR().renderGroundEdge(i);
			i.next();
		}
		
	}
	
}
