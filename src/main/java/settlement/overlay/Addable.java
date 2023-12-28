package settlement.overlay;

import init.C;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import settlement.main.SETT;
import settlement.tilemap.floor.Floors.Floor;
import settlement.tilemap.terrain.TBuilding;
import settlement.tilemap.terrain.Terrain.TerrainTile;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.DIR;
import snake2d.util.sets.LISTE;
import util.colors.GCOLORS_MAP;
import util.info.INFO;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;
import view.main.VIEW;

public abstract class Addable extends INFO{

	static final int iSize = Icon.M*2;
	static final int iOff = (C.TILE_SIZE-iSize)/2;
	
	boolean added = false;
	final boolean above;
	final boolean under;
	public final String key;
	protected boolean exclusive = false;
	
	Addable(LISTE<Addable> all, String key, CharSequence name, CharSequence desc) {
		this(all, key, name, desc, false, true);
	}
	
	Addable(LISTE<Addable> all, String key, CharSequence name, CharSequence desc, boolean underling, boolean above) {
		super(name,desc);
		all.add(this);
		this.under = underling;
		this.above = above;
		this.key = key;
	}
	
	public void add() {
		added = true;
	}
	
	public boolean added() {
		return added;
	}
	
	public void initBelow(RenderData data) {
		
	}
	
	public void finishBelow() {
		
	}
	
	public void initAbove(RenderData data) {
		
	}
	
	public void finishAbove() {
		
	}
	
	public boolean render(Renderer r, RenderIterator it) {
		return false;
	}
	
	public void renderBelow(Renderer r, RenderIterator it) {
		
	}
	
	public static void renderUnder(double v, Renderer r, RenderIterator it) {
		renderUnder(v, r, it, true);
	}
	
	public static boolean renderAbove(double v, Renderer r, RenderIterator it, boolean pluses) {
		

		COLOR c = COLOR.WHITE05;
		
		if (v >= 0) {
			c = ColorImp.TMP.interpolate(COLOR.WHITE05, GCOLORS_MAP.bestOverlay, v);
		}else {
			c  = ColorImp.TMP.interpolate(COLOR.WHITE05, GCOLORS_MAP.worstOverlay, -v);
		}
		if (!renderAbove(c, r, it))	
			return false;
		
		if (pluses && VIEW.s().getWindow().zoomout() <= 1) {
			int am = (int) Math.round(v*4.0);
			for (int i = 0; i < am; i++) {
				SPRITES.icons().s.plus.renderScaled(r, it.x()+16*(i%2), it.y()+16 + (i/2)*16, 2);
			}
		}
		return true;
	}
	
	public static void renderUnder(double v, Renderer r, RenderIterator it, boolean pluses) {
		

		COLOR c = COLOR.WHITE05;
		
		if (v >= 0) {
			c = ColorImp.TMP.interpolate(COLOR.WHITE05, GCOLORS_MAP.bestOverlay, v);
		}else {
			c  = ColorImp.TMP.interpolate(COLOR.WHITE05, GCOLORS_MAP.worstOverlay, -v);
		}
		if (!renderUnder(c, r, it))	
			return;
		
		if (pluses && VIEW.s().getWindow().zoomout() <= 1) {
			int am = (int) Math.round(v*4.0);
			for (int i = 0; i < am; i++) {
				SPRITES.icons().s.plus.renderScaled(r, it.x()+16*(i%2), it.y()+16 + (i/2)*16, 2);
			}
		}
		it.hiddenSet();
	}
	
	public static void renderPluses(double v, Renderer r, RenderIterator it) {
		if (VIEW.s().getWindow().zoomout() <= 1) {
			int am = (int) Math.round(v*4.0);
			for (int i = 0; i < am; i++) {
				SPRITES.icons().s.plus.renderScaled(r, it.x()+16*(i%2), it.y()+16 + (i/2)*16, 2);
			}
		}
	}
	
	public static void renderColor(double v, Renderer r, RenderIterator it, boolean pluses) {
		
		COLOR c = COLOR.WHITE05;
		
		if (v >= 0) {
			c = ColorImp.TMP.interpolate(COLOR.WHITE05, GCOLORS_MAP.bestOverlay, v);
		}else {
			c  = ColorImp.TMP.interpolate(COLOR.WHITE05, GCOLORS_MAP.worstOverlay, -v);
		}
		c.bind();
		SPRITES.cons().BIG.filled.render(r, 0x0F, it.x(), it.y());
		if (pluses && VIEW.s().getWindow().zoomout() <= 1) {
			int am = (int) Math.round(v*4.0);
			for (int i = 0; i < am; i++) {
				SPRITES.icons().s.plus.renderScaled(r, it.x()+16*(i%2), it.y()+16 + (i/2)*16, 2);
			}
		}
		it.hiddenSet();
	}
	
	public static boolean renderUnder(COLOR c, Renderer r, RenderIterator it) {
		
		if (VIEW.s().getWindow().zoomout() >= 3 && SETT.TERRAIN().get(it.tile()).miniDepth() > 0)
			return false;
		
		if (SETT.ROOMS().placement.embryo.is(it.tile()))
			return false;
		c.bind();
		
		int m =0x0F;
		
		Floor f = SETT.FLOOR().getter.get(it.tile());
		
		if (f != null) {
			COLOR.unbind();
			SETT.FLOOR().renderSimple(r, it, f);
			c.bind();
			m = rMask(it);
			SPRITES.cons().BIG.filled_striped.render(r, m, it.x(), it.y());
		}else if( SETT.ROOMS().map.is(it.tile())) {
			m = rMask(it);
			SPRITES.cons().BIG.filled_striped.render(r, m, it.x(), it.y());
			
		}else {
			SPRITES.cons().BIG.filled.render(r, m, it.x(), it.y());
		}
		
		it.hiddenSet();
		return true;
	}
	
	public static boolean renderAbove(COLOR c, Renderer r, RenderIterator it) {
		
		if (SETT.ROOMS().placement.embryo.is(it.tile()))
			return false;
		
		TerrainTile tt = SETT.TERRAIN().get(it.tile());
		
		if (tt != SETT.TERRAIN().NADA && !(tt instanceof TBuilding.Ceiling)) {
		
			c.bind();
			
			
			SPRITES.cons().BIG.filled_striped.render(r, 0, it.x(), it.y());
			return true;
		}
		return false;
		
	}
	
	private static int rMask(RenderIterator it) {
		int m = 0;
		for (DIR d : DIR.ORTHO) {
			int tx = it.tx() + d.x();
			int ty = it.ty() + d.y();
			
			if (SETT.FLOOR().getter.get(tx, ty) != null || SETT.ROOMS().map.is(tx, ty)) {
				m |= d.mask();
			}
		}
		return m;
	}
	
}