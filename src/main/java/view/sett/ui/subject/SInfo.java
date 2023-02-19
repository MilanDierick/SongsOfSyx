package view.sett.ui.subject;

import init.sprite.UI.UI;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.colors.GCOLOR;
import util.gui.misc.GText;

final class SInfo{
	
	private final GuiSection section = new GuiSection();
	private final UISubject a;
	public static final int width = 560;
	
	SInfo(UISubject a, int height) {
		
		
		section.add(new SInfoPortrait(a).section);
		
		section.addRelBody(4, DIR.S, top());
		
		section.addRelBody(4, DIR.S, new SInfoDesc(a, height-section.body().height()));
		
		this.a = a;
		
		

		
	}
	
	private GuiSection top() {
		
		GuiSection s = new GuiSection();
		

		
		s.addRelBody(2, DIR.S, new RENDEROBJ.RenderImp(500, UI.FONT().H2.height()) {
			
			final GText name = new GText(UI.FONT().H2, 24);
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				GCOLOR.T().H2.bind();
				name.clear();
				name.add(STATS.APPEARANCE().name(a.a.indu()));
				name.setMaxWidth(550);
				name.setMultipleLines(false);
				name.lablify();
				name.adjustWidth();
				name.renderC(r, body().cX(), body().cY());
				
			}
			
		});
		
		s.addRelBody(2, DIR.S, new RENDEROBJ.RenderImp(400, UI.FONT().S.height()) {
			
			GText text = new GText(UI.FONT().S, 36);
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				text.clear();
				a.a.ai().getOccupation(a.a, text);
				text.normalify();
				text.adjustWidth();
				text.renderC(r, body().cX(), body().cY());
			}
			
		});
		
		
		return s;
	}
	
	GuiSection activate() {
		return section;
	}
	
}
