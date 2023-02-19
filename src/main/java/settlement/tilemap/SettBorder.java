package settlement.tilemap;

import java.io.IOException;

import init.C;
import init.sprite.SPRITES;
import settlement.main.*;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.*;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.rnd.RND;
import snake2d.util.sets.*;



public final class SettBorder extends TileMap.Resource{

	private static final int width = Math.min(SETT.TWIDTH/2, SETT.THEIGHT/2);

	
	public SettBorder() {

	}
	
	public final MAP_BOOLEAN is = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tx, int ty) {
			if (tx == 0 || tx == SETT.TWIDTH-1) {
				ty -= (SETT.THEIGHT-width)/2;
				return ty >= 0 && ty < width;
			}else if (ty == 0 || ty == SETT.THEIGHT-1) {
				tx -= (SETT.TWIDTH-width)/2;
				return tx >= 0 && tx < width;
			}
			return false;
		}
		
		@Override
		public boolean is(int tile) {
			return is(tile%SETT.TWIDTH, tile/SETT.TWIDTH);
		}
	};
	
	private final ArrayList<Border> all = new ArrayList<>(
			new Border(0, DIR.N),
			new Border(1, DIR.E),
			new Border(2, DIR.S),
			new Border(3, DIR.W)
			);
	

	protected void generate(CapitolArea area) {
		for (Border b : all)
			generate(b);
	}
	
	public LIST<Border> all(){
		return all;
	}
	
	public Border get(DIR d) {
		return all.get((d.id()+RND.rInt(2)/2));
	}
	
	private void generate(Border border) {
//		RES.flooder().init(this);
//		for (COORDINATE c : border.body()) {
//			double v = 8;
//			v += 8*RND.rExpo();
//			RES.flooder().pushSloppy(c, v);
//		}
//		
//		while(RES.flooder().hasMore()) {
//			PathTile t = RES.flooder().pollGreatest();
//			if (t.getValue() <= 0)
//				continue;
//			SETT.TERRAIN().clearing.get(t).destroy(t.x(), t.y());
//			
//			for (DIR d : DIR.ALL) {
//				int dx = t.x()+d.x();
//				int dy = t.y()+d.y();
//				if (SETT.IN_BOUNDS(dx, dy)) {
//					RES.flooder().pushGreater(dx, dy, t.getValue()-d.tileDistance());
//				}
//				
//			}
//			
//		}
//		
//		RES.flooder().done();
	}
	
	public void clear(Border border) {
//		for (COORDINATE c : border.body()) {
//			if (SETT.PATH().availability.get(c).isSolid(SETT.BATTLE2().enemy())) {
//				SETT.BATTLE2().map.breakIt(c.x(), c.y());
//			}
//		}
	}

	
	public final class Border implements INDEXED, BODY_HOLDER{
		
		private final int index;
		private final Rec edge = new Rec();
		public final DIR dir;
		
		Border(int index, DIR d){
			this.dir = d;
			this.index = index;
			if (d == DIR.N) {
				edge.setDim(width, 1);
				edge.moveY1(0);
				edge.centerX(0, SETT.TWIDTH);
			}else if (d == DIR.S) {
				edge.setDim(width, 1);
				edge.moveY2(SETT.THEIGHT);
				edge.centerX(0, SETT.TWIDTH);
			}else if (d == DIR.E) {

				
				edge.setDim(1, width);
				edge.moveX2(SETT.TWIDTH);
				edge.centerY(0, SETT.TWIDTH);
			}else {
				
				edge.setDim(1, width);
				edge.moveX1(0);
				edge.centerY(0, SETT.TWIDTH);
			}
		}
		


		@Override
		public int index() {
			return index;
		}
		
		@Override
		public RECTANGLE body() {
			return edge;
		}
	}

	public void render(Renderer r, RenderData renData) {
		
		COLOR.ORANGE100.bind();
		for (Border b : all) {
			if (renData.tBounds().touches(b)) {
				for (COORDINATE c : b.body()) {
					if (renData.tBounds().holdsPoint(c)) {
						SPRITES.cons().ICO.scratch.render(r, c.x()*C.TILE_SIZE-renData.offX1(), c.y()*C.TILE_SIZE-renData.offY1());
					}
				}
			}
		}
		COLOR.unbind();
		
	}

	@Override
	protected void save(FilePutter saveFile) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void load(FileGetter saveFile) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void clearAll() {
		// TODO Auto-generated method stub
		
	}
	
}
