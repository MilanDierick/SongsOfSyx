package world.overlay;

import static world.World.*;

import init.C;
import init.D;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.main.RenderData.RenderIterator;
import snake2d.CORE;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.DIR;
import snake2d.util.sprite.text.Font;
import snake2d.util.sprite.text.Str;
import util.rendering.ShadowBatch;
import world.World;
import world.map.regions.REGIOND;
import world.map.regions.Region;

class OverlayRegion extends WorldOverlayer{

	private final Str str = new Str(32);
	private final COLOR cNone = new ColorImp(100, 100, 100);

	private static CharSequence ¤¤name = "¤Regions";
	private static CharSequence ¤¤desc = "¤show region borders";
	static {
		D.ts(OverlayRegion.class);
	}
	
	
	OverlayRegion() {
		super(¤¤name, ¤¤desc);
	}
	
	public void render(Renderer r, ShadowBatch s, RenderIterator it, Region hovered) {
		int m = 0;
		Region a = World.REGIONS().getter.get(it.tile());
		
		if (a != null) {
			
			if (REGIOND.REALM(a) == null)
				cNone.bind();
			else
				REGIOND.REALM(a).faction().banner().colorBG().bind();
			
			if (a == hovered) {
				
				
				for (DIR d : DIR.ORTHO) {
					int ii = it.tile()+d.x()+d.y()*TWIDTH();
					Region r2 = World.REGIONS().getter.get(ii);
					if (!IN_BOUNDS(it.tx(), it.ty(), d) || a == r2)
						m |= d.mask();
				}
				if (m != 0x0F) {
					s.setHeight(0).setDistance2GroundUI(6);
					SPRITES.cons().BIG.outline_dashed.render(r, m, it.x(), it.y());
					SPRITES.cons().BIG.outline_dashed.render(s, m, it.x(), it.y());
				}
				
			}else {
				for (DIR d : DIR.ORTHO) {
					int ii = it.tile()+d.x()+d.y()*TWIDTH();
					Region r2 = World.REGIONS().getter.get(ii);
					if (!IN_BOUNDS(it.tx(), it.ty(), d) || a == r2)
						m |= d.mask();
				}
				if (m != 0x0F) {
					SPRITES.cons().BIG.outline_dashed_small.render(r, m, it.x(), it.y());
				}
				if (a.faction() != null && a.faction().capitolRegion() == a && CORE.renderer().getZoomout() != 0 && a.fontSize() != 0 && it.tx() == a.textx() && it.ty() == a.texty()) {
					Font f = UI.FONT().H1;
					str.clear().add(a.name());
					int scale = C.SCALE;
					if (a.name().length() > 20) {
						scale = 1;
					}if (a.name().length() > 16) {
						scale = 2;
					}else if(a.name().length() > 10) {
						scale = 3;
					}
					s.setHeight(0).setDistance2GroundUI(14);
					f.render(r, str, it.x(), it.y(), scale);
					f.render(s, str, it.x(), it.y(), scale);
				}
			}
		}
		
	}
	
}
