package world.map.terrain;

import static world.World.*;

import java.io.IOException;

import init.C;
import init.biomes.*;
import init.sprite.ICON;
import settlement.main.RenderData;
import settlement.main.RenderData.RenderIterator;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.AREA;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.map.MAP_DOUBLEE;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.Bitsmap1D;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TILE_SHEET;
import util.rendering.ShadowBatch;
import view.tool.*;
import view.world.IDebugPanelWorld;
import world.World;
import world.World.WorldResource;

public class WorldForest extends WorldResource{
	
	private final TILE_SHEET sheet = World.sprites().forest;
	
	private final Bitsmap1D data = new Bitsmap1D(0, 4, TAREA());
	private static final int SET = 16;
	
	
	private final int max = 11;

	private final static int colorA = 64;
	private final COLOR[][] colors = new COLOR[colorA][colorA];
	public final PLACABLE placer;
	
	public final SPRITE icon;
	
	public WorldForest(World m){
		
		PLACABLE CLEAR = new PlacableMulti("clear forest") {
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				amount.set(tx, ty, 0);
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				return null;
			}
		};
		
		
		placer = new PlacableMulti("forest") {

			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				return WorldForest.this.placable.is(tx, ty) ? null : "";
			}

			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				int i = tx+ty*TWIDTH();
				data.set(i, CLAMP.i(data.get(i)+1, 0, max));
			}
		};
		IDebugPanelWorld.add(placer);
		IDebugPanelWorld.add(CLEAR);
		
		{
			
			final ColorImp wa = new ColorImp();
			final ColorImp wb = new ColorImp();
			
			final int seasons = 4;
			final double stepsPerSeason = colorA/seasons;
			
			final int randoms = 4;
			final double stepsPerRandom = colorA/randoms;
			LIST<COLOR> cols = World.sprites().forest_colors;
			for (double y = 0; y < colorA; y++) {
				
				int seasonFrom = (int) Math.floor(y/stepsPerSeason);
				int seasonTo = (int) Math.ceil(y/stepsPerSeason);
				seasonTo %= seasons;
				double seasonDelta = (y-seasonFrom*stepsPerSeason)/stepsPerSeason;
				for (double r = 0; r < colorA; r++) {
					int ranFrom = (int) Math.floor(r/stepsPerRandom);
					int ranTo = (int) Math.ceil(r/stepsPerRandom);
					ranTo %= randoms;
					double ranDelta = (r-ranFrom*stepsPerRandom)/stepsPerRandom;
					
					wa.interpolate(cols.get(seasonFrom*randoms + ranFrom), cols.get(seasonFrom*randoms + ranTo), ranDelta);
					wb.interpolate(cols.get(seasonTo*randoms + ranFrom), cols.get(seasonTo*randoms + ranTo), ranDelta);
					ColorImp res = new ColorImp();
					res.interpolate(wa, wb, seasonDelta);
					colors[(int) y][(int) r] = res;
				}
			}
		}
		
		icon = new SPRITE.Imp(ICON.BIG.SIZE) {
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				double scale = (double)(Y2-Y1)/height;
				
				int d = (int) (scale*(ICON.BIG.SIZE-24)/2);
				
				World.sprites().forest_colors.get(0).bind();
				int t = 10*16;
				sheet.render(r, t, X1+d, X2-d, Y1+d, Y2-d);
				COLOR.unbind();
			}
		};
	}
	
	public final MAP_DOUBLEE amount = new MAP_DOUBLEE() {

		private final double amI = 1.0/max;
		
		@Override
		public double get(int tile) {
			return data.get(tile) * amI;
		}

		@Override
		public double get(int tx, int ty) {
			if (IN_BOUNDS(tx, ty))
				return get(tx+ty*TWIDTH());
			return 0;
		}

		@Override
		public MAP_DOUBLEE set(int tile, double value) {
			data.set(tile, CLAMP.i((int) Math.ceil(value*max), 0, max));
			return this;
		}

		@Override
		public MAP_DOUBLEE set(int tx, int ty, double value) {
			if (IN_BOUNDS(tx, ty))
				set(tx+ty*TWIDTH(), value);
			return this;
		}
	};
	
	public final MAP_BOOLEAN is = new MAP_BOOLEAN() {
		@Override
		public boolean is(int tx, int ty) {
			return amount.get(tx, ty) > 0;
		}
		
		@Override
		public boolean is(int tile) {
			return amount.get(tile) > 0;
		}
	};
	
	public final MAP_BOOLEAN placable = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tx, int ty) {
			if (!IN_BOUNDS(tx, ty))
				return false;
			if (GROUND().DESERT.is(tx, ty))
				return false;
			if (WATER().coversTile.is(tx, ty))
				return false;
			return true;
		}
		
		@Override
		public boolean is(int tile) {
			return is(tile%TWIDTH(), tile/TWIDTH());
		}
	};
	
	@Override
	protected void save(FilePutter saveFile){
		data.save(saveFile);
	}
	
	@Override
	protected void load(FileGetter saveFile) throws IOException {
		data.load(saveFile);
	}
	
	@Override
	protected void update(float ds){
		
		
	}
	
	private int[] cols = new int[3];
	
	public void render(SPRITE_RENDERER r, ShadowBatch s, RenderData data){
		
		//SPRITES.world().map.forest_colors.get((int) (TIME.year().partOf()*16)).bind();
		
		
		for (CLIMATE z : CLIMATES.ALL()) {
			cols[z.index()] = (int)((colorA-colorA/4)+z.getPartOfYear()*colorA)%colorA;
		}

		
		int off = (C.SCALE*24 - C.TILE_SIZE)/2;
		int rMask = colorA-1;
		
		RenderIterator it = data.onScreenTiles(2,2,2,0);
		s.setHeight(4);
		s.setDistance2Ground(0);
		s.setSoft();
		while(it.has()) {
			int t = this.data.get(it.tile());
			if (t != 0){
				
				t-= 1;
				
				if (REGIONS().isCentre.is(it.tx(), it.ty()) || BUILDINGS().getter().get(it.tile()).isVisible(it.ran(), it.tile()))
					t = CLAMP.i(t/4, 1, 16);
				else if (BUILDINGS().roads.is(it.tile()))
					t = CLAMP.i(t/2, 1, 16);
				
				
//				else if (!BUILDINGS().nothing.placer.is(it.tile()))
//					t = CLAMP.i(t/2, 1, 16);
				
				if (t >= max)
					it.hiddenSet();
				
				t *= SET;
				t += it.ran()&0x0F;
				int x = it.x()-off;
				int y = it.y()-off;
				x += (((it.ran()>>4)&0x0F)-7)&0b1100;;
				y += (((it.ran()>>8)&0x0F)-7)&0b1100;
				
				colors[cols[CLIMATE().getter.get(it.tile()).index()]][it.ran()&rMask].bind();
				
				sheet.render(r, t, x, y);
				sheet.render(s, t, x, y);
				
			}
			it.next();
		}	
		COLOR.unbind();
		
	}
	
	double add(WorldTerrainInfo info, int tx, int ty) {
		info.add(TERRAINS.FOREST(), amount.get(tx, ty));
		return amount.get(tx, ty);
	}
	
}
