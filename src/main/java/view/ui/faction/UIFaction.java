package view.ui.faction;

import game.faction.diplomacy.Deal;
import game.faction.npc.FactionNPC;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.colors.GCOLOR;
import util.data.GETTER.GETTER_IMP;
import util.dic.DicGeo;
import util.dic.DicMisc;
import util.gui.misc.GButt;

final class UIFaction extends GuiSection{

	private final GETTER_IMP<FactionNPC> f;
	private final CLICKABLE.ClickSwitch sw;
	private final Diplomacy dip; 
	
	UIFaction(GETTER_IMP<FactionNPC> f, Deal deal, final int WIDTH, final int HEIGHT){

		this.f = f;
		this.body().setWidth(WIDTH).setHeight(1);
		addRelBody(0, DIR.S, new Banner(this.f, WIDTH));
		
		
		

		
		GuiSection butts = new GuiSection();
		
		
		
		{
			CLICKABLE c = new Court(f, WIDTH, HEIGHT-40);
			sw = new CLICKABLE.ClickSwitch(c);
			sw.setD(DIR.N);
			butts.addRightC(0, sb(DicMisc.¤¤Court, c));
					
		}
		int hi = HEIGHT - body().height()-butts.body().height()-24;
		dip = new Diplomacy(f, deal, hi);
		butts.addRightC(0, sb(DicGeo.¤¤Realm, new Realm(f, hi)));
		butts.addRightC(0, sb(DicMisc.¤¤goods, new Goods(f, hi)));
		butts.addRightC(0, sb(DicMisc.¤¤Boosts, new Bonus(f, hi)));
		butts.addRightC(0, sb(DicMisc.¤¤Diplomacy, dip));
		
		addRelBody(8, DIR.S, butts);
		addRelBody(0, DIR.S, new RENDEROBJ.RenderImp(WIDTH-128, 16) {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				GCOLOR.UI().border(r, body().x1(), body().x2(), body().y1()+5, body().y1()+8);
			}
		});
		addRelBody(0, DIR.S, sw);
		body().setWidth(WIDTH).setHeight(HEIGHT);
	}
	
	public void dip() {
		sw.set(dip);
	}

	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		if (f.get() == null)
			return;
		super.render(r, ds);
	}
	
	private CLICKABLE sb(CharSequence name, CLICKABLE s) {
		
		GButt.ButtPanel b = new GButt.ButtPanel(name) {
			
			@Override
			protected void clickA() {
				sw.set(s);
			}
			
			@Override
			protected void renAction() {
				selectedSet(sw.current() == s);
			}
			
		};
		
		b.body.setWidth(140);
		return b;
	}
}
