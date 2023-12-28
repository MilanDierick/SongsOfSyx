package world.map.buildings;

import java.io.IOException;

import snake2d.SPRITE_RENDERER;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.map.MAP_INTE;
import snake2d.util.map.MAP_OBJECTE;
import snake2d.util.sets.LISTE;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;
import world.WORLD;

public abstract class WorldBuilding {

	private final int index;
//	public final CharSequence name;
	
	protected WorldBuilding(LISTE<WorldBuilding> all){
		index = all.add(this);
	}
	
	public final int index() {
		return index;
	}
	
//	protected abstract int fix(int tx, int ty);
	
	/**
	 * render directly on ground
	 * @param r
	 * @param it
	 * @param data
	 */
	protected void renderOnGround(SPRITE_RENDERER r, RenderData.RenderIterator it, int data) {
		
	}
	
	/**
	 * render above ground, but below trees, water and mountains
	 * @param r
	 * @param s
	 * @param it
	 * @param data
	 */
	protected void renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator it, int data) {
		
	}
	
	/**
	 * render above everything
	 * @param r
	 * @param s
	 * @param it
	 * @param data
	 */
	protected void renderAboveTerrain(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator it, int data) {
		
	}
	
	public boolean isVisible(int ran, int tile) {
		return true;
	}
	
	protected final MAP_INTE data() {
		return WORLD.BUILDINGS().dataM;
	}
	
	protected final MAP_OBJECTE<WorldBuilding> map() {
		return WORLD.BUILDINGS().map;
	}
	
	protected abstract void unplace(int tx, int ty);
	
//	public final MAP_PLACER placer = new MAP_PLACER() {
//		
//		@Override
//		public MAP_PLACER set(int tx, int ty) {
//			if (!IN_BOUNDS(tx, ty))
//				return this;
//			int tile = tx + ty*TWIDTH();
//			BUILDINGS().map.set(tile, WorldBuilding.this);
//			for (int i = 0; i < DIR.ORTHO.size(); i++) {
//				DIR d = DIR.ORTHO.get(i);
//				int dx = tx+d.x();
//				int dy = ty +d.y();
//				if (IN_BOUNDS(dx, dy))
//					BUILDINGS().map.set(dx+dy*TWIDTH(), BUILDINGS().map.get(dx, dy));
//			}
//			return this;
//		}
//		
//		@Override
//		public MAP_PLACER set(int tile) {
//			int tx = tile%TWIDTH();
//			int ty = tile/TWIDTH();
//			return set(tx, ty);
//		}
//		
//		@Override
//		public boolean is(int tx, int ty) {
//			return IN_BOUNDS(tx, ty) && is(tx+ty*TWIDTH());
//		}
//		
//		@Override
//		public boolean is(int tile) {
//			return BUILDINGS().map.is(tile, WorldBuilding.this);
//		}
//
//		@Override
//		public MAP_PLACER clear(int tile) {
//			return BUILDINGS().nothing.placer.set(tile);
//		}
//
//		@Override
//		public MAP_PLACER clear(int tx, int ty) {
//			return BUILDINGS().nothing.placer.set(tx, ty);
//		}
//	};
	
	protected abstract void save(FilePutter file);
	
	protected abstract void load(FileGetter file) throws IOException ;
	
	protected abstract void clear();

	protected void initBeforePlay() {
		
	}
	
}
