package world.overlay;

import game.faction.FACTIONS;
import game.faction.FBanner;
import init.C;
import init.D;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.DIMENSION;
import snake2d.util.datatypes.DIR;
import snake2d.util.misc.IntChecker;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Text;
import util.colors.GCOLOR;
import util.data.BOOLEAN.BOOLEANImp;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;
import world.WORLD;
import world.overlay.WorldOverlays.Overlay;
import world.regions.Region;
import world.regions.WREGIONS;
import world.regions.centre.WCentre;
import world.regions.data.RD;
import world.regions.map.RegionInfo;

public class OverlayRegnames extends Overlay{

	private final IntChecker check = new IntChecker(WREGIONS.MAX);
	private final Text text = new Text(UI.FONT().H1, RegionInfo.nameSize);
	public static CharSequence ¤¤name = "¤Regions";
	public static CharSequence ¤¤desc = "¤show region names and info";
	static {
		D.ts(OverlayRegnames.class);
	}
	
	public final BOOLEANImp active = new BOOLEANImp(true);
	
	public OverlayRegnames() {
		text.setMultipleLines(false);
	}
	
	public void exclude(Region r) {
		check.isSetAndSet(r.index());
	}

	@Override
	public void renderAbove(Renderer r, ShadowBatch s, RenderData data) {
		if (!active.is())
			return;
		
		for (Region reg : WORLD.REGIONS().active()) {
			if (!check.isSet(reg.index())){
				for (DIR d : DIR.NORTHO) {
					if (data.tBounds().holdsPoint(reg.cx()+d.x()*5, reg.cy()+d.y()*5)) {
						render(reg, r, s, data);
						break;
					}
				}
				
				
			}
			
			
		}
		check.init();
		
	}
	
	public void render(Region reg, Renderer r, ShadowBatch s, RenderData data) {
		
		if (r.getZoomout() >= 2) {
			text.setFont(UI.FONT().S);
			text.setScale(4.0);
		}else {
			text.setFont(UI.FONT().H1);
			text.setScale(1.0);
		}
		
		text.clear().add(reg.info.name());
		text.adjustWidth();
		text.setMaxWidth(C.T_PIXELS*24);
		
		int cx = reg.info.cx()*C.TILE_SIZE+C.TILE_SIZE/2;
		int cy = reg.info.cy()*C.TILE_SIZE+C.TILE_SIZE/2;
		
		int dy = (int) (C.TILE_SIZE*(WCentre.TILE_DIM/2.5 + (reg.capitol() ? 0.5 : 0)));
		
		int x1 = cx-text.width()/2;
		int y1 = (int) (8 + cy+dy);
		
		if (canRender(reg, text, x1, y1)) {
			render(reg, x1, y1, r, s, data);
		}else {
			int yy1 = (int) (cy - text.height() - dy);
			if (canRender(reg, text, x1, yy1)) {
				render(reg, x1, yy1, r, s, data);
			}else {
				render(reg, x1, y1, r, s, data);
			}
		}
		
		COLOR.unbind();
		
	}

	private boolean canRender(Region reg, DIMENSION dim, int x1, int y1) {
		int y2 = y1 + dim.height();
		int x2 = x1 + dim.width();
		if (is(reg, x1, y1) && is(reg, x2, y1) && is(reg, x2, y2) && is(reg, x1, y2)) {
			return true;
		}
		return false;
	}
	
	private void render(Region reg,int x1, int y1, Renderer r, ShadowBatch s, RenderData data) {

		s.setHard().setDistance2Ground(0).setHeight(0);
		x1 = data.transformGX(x1);
		y1 = data.transformGY(y1);
		
		int width = text.height();
		COLOR.WHITE25.render(s, x1-8, x1+text.width()+width+8, y1-8, y1+text.height()+8);
		
		if (reg.capitol()) {

			UI.icons().s.crown.render(r, x1, x1+text.height(), y1, y1+text.height());
		}else if (reg.faction() == FACTIONS.player()) {
			double loy = RD.RACES().loyaltyAll.getD(reg);
			
			SPRITE sp = UI.icons().s.faces[(int) Math.round(loy*(UI.icons().s.faces.length-1))];
			
			GCOLOR.UI().badToGood(ColorImp.TMP, loy);
			ColorImp.TMP.bind();
			sp.render(r, x1, x1+text.height(), y1, y1+text.height());
			COLOR.unbind();
		}else if (reg.faction() != null) {
			
			if (FACTIONS.DIP().war.is(FACTIONS.player(), reg.faction())) {
				width += text.height();
				GCOLOR.UI().BAD.hovered.bind();
				SPRITES.icons().s.sword.render(r, x1, x1+text.height(), y1, y1+text.height());
			}else {
				FBanner.render(r, reg.faction(), x1, x1+text.height(), y1, y1+text.height());
				if (FACTIONS.DIP().trades(FACTIONS.player(), reg.faction())) {
					SPRITES.icons().s.money.render(r, x1, x1+text.height()/2, y1, y1+text.height()/2);
				}
			}
		}else {
			FBanner.render(r, null, x1, x1+text.height(), y1, y1+text.height());
		}
		COLOR.WHITE25.render(s, x1-8, x1+text.width()+width+8, y1-8, y1+text.height()+8);
		
		if (reg.faction() != null) {	
			reg.faction().banner().colorBGBright().bind();
		}else {
			COLOR.WHITE65.bind();
		}
		
		
		renderText(r, x1+width+2, y1);

		COLOR.unbind();
		
	}
	
	private void renderText(Renderer r, int x1, int y1) {
		text.render(r, x1, y1);
//		COLOR.WHITE100.bind();
//		text.render(r, x1-1, y1-1);
	}

	private boolean is(Region reg, int x, int y) {
		x = x >> C.T_SCROLL;
		y = y >> C.T_SCROLL;
		return WORLD.REGIONS().map.is(x, y, reg);
	}

	@Override
	public boolean renderBelow(Renderer r, ShadowBatch s, RenderData data) {
		// TODO Auto-generated method stub
		return false;
	}

}
