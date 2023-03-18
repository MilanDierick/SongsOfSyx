package world.overlay;

import init.C;
import init.D;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.main.RenderData.RenderIterator;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.sprite.text.Font;
import util.rendering.ShadowBatch;
import world.World;
import world.map.landmark.WorldLandmark;

class OverlayLandmark extends WorldOverlayer{


	private static CharSequence ¤¤name = "¤Landmarks";
	private static CharSequence ¤¤desc = "¤show region borders";
	static {
		D.ts(OverlayLandmark.class);
	}
	
	
	OverlayLandmark() {
		super(¤¤name, ¤¤desc);
	}
	
	public void render(Renderer r, ShadowBatch s, RenderIterator it, WorldLandmark hovered) {
		WorldLandmark a = World.LANDMARKS().setter.get(it.tile());
		
		if (a == null)
			return;
			
		if (a == hovered) {
			COLOR.WHITE2WHITE.bind();
			SPRITES.cons().BIG.solid.render(r, 0x0F, it.x(), it.y());
		}
		COLOR.WHITE85.bind();
		if (a != null && a.textSize != 0 && it.tx() == a.cx && it.ty() == a.cy) {
			Font f = UI.FONT().H1;
			int scale = C.SCALE;
			if (a.name.length() <= 6) {
				
			}else if(a.name.length() <= 12) {
				scale = 3;
			}else {
				scale = 2;
			}
			f.renderCX(r, it.x(), it.y(), a.name, scale);
			f.renderCX(s, it.x(), it.y(), a.name, scale);
			
		}
		
	}
	
}
