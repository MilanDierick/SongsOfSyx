package view.ui.faction;

import game.faction.npc.FactionNPC;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.data.GETTER;
import util.dic.DicGeo;
import util.gui.misc.GStat;
import util.gui.misc.GText;

final class Banner extends GuiSection{

	
	Banner(GETTER<FactionNPC> f, int width){
		
		body().setWidth(700);
		
		addRelBody(2, DIR.S, new GStat(new GText(UI.FONT().M, 32)) {
			
			@Override
			public void update(GText text) {
				text.add(f.get().nameIntro);
			}
		}.r(DIR.N));
		
		addRelBody(6, DIR.S, new GStat(new GText(UI.FONT().H1, 32)) {
			
			@Override
			public void update(GText text) {
				text.add(f.get().name);
				text.lablify();
			}
		}.r(DIR.N));
		
		addRelBody(4, DIR.S, new GStat() {
			
			@Override
			public void update(GText text) {
				DicGeo.fType(f.get(), text);
				
			}
		}.r(DIR.N));
		
		RENDEROBJ rr = ban(f);
		rr.body().moveX1(0).moveCY(body().cY());
		add(rr);
		
		rr = ban(f);
		rr.body().moveX2(700).moveCY(body().cY());
		add(rr);
		
		RENDEROBJ info = Hoverer.facts(f, 1000, 110);
		addRelBody(4, DIR.S, info);
		
	}
	
	
	private RENDEROBJ ban(GETTER<FactionNPC> f) {
		return new RENDEROBJ.RenderImp(Icon.L*2) {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				f.get().banner().HUGE.render(r, body());
				
			}
		};
	}
	
	
	
}
