package view.sett.ui.noble;

import game.GAME;
import game.nobility.Nobility;
import init.C;
import init.boostable.BBoost;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.entity.humanoid.HCLASS;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.gui.misc.*;
import util.info.GFORMAT;
import view.interrupter.ISidePanel;
import view.main.VIEW;

public final class UINobles extends ISidePanel{

	private static CharSequence ¤¤PH = "¤Nobles can be appointed to boost different aspects of the game. You appoint a noble by clicking a subject and there is a little winged button there. Nobles will be very expanded upon in the future, so this is very WIP.";
	
	public UINobles() {
		titleSet(HCLASS.NOBLE.names);
		
		int i = 0;
		for (Nobility n : GAME.NOBLE().ALL()){
			
			section.add(noble(n), (i%8)*64, (i/8)*70);
			i++;
		}
		
		section.addRelBody(16, DIR.N, new GText(UI.FONT().M, ¤¤PH).setMaxWidth(540));
		
		
	}
	
	private static RENDEROBJ noble(Nobility res) {
		
		
		
		return new CLICKABLE.ClickableAbs(C.SG*40+4, C.SG*68) {
			private int yoff = 52;
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
				COLOR.WHITE30.render(r, body());
				if (isHovered || (res.subject() != null && VIEW.s().ui.subjects.shows(res.subject())))
					ColorImp.TMP.set(res.color()).shadeSelf(1.5).render(r, body(), -1);
				else
					ColorImp.TMP.set(res.color()).shadeSelf(1.2).render(r, body(), -1);
				
				if (res.subject() != null) {
					STATS.APPEARANCE().portraitRender(r, res.subject().indu(), body().x1()+2, body().y1(), 1);
					
					GMeter.render(r, GMeter.C_GREENRED, res.happiness(), body().x1()+4, body().x2()-4, body().y1()+yoff, body().y1()+yoff+6);
					GMeter.render(r, GMeter.C_BLUE, res.skill(), body().x1()+4, body().x2()-4, body().y1()+yoff+7, body().y1()+yoff+13);
				}else {
					SPRITES.icons().l.mysteryman.renderC(r, body().cX(), body().y1() + C.SG*24);
				}
				
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				b.title(res.info().name);
				b.text(res.info().desc);
				if (res.subject() == null) {
					b.NL();
					b.error(¤¤PH);
				}else {
					b.NL(8);
					res.subject().hover(b);
				}
				
				b.NL(8);
				
				for (BBoost bo : res.BOOSTER.boosts()) {
					bo.hover(text);
				}
				
				b.NL(8);
//				
//				b.add(b.text().lablifySub().add(STANDINGS.CITIZEN().info().name));
//				b.add(GFORMAT.perc(b.text(),res.happiness()));
//				
				b.add(GFORMAT.perc(b.text(),res.skill()));
//				
				
				
			}
			
			@Override
			protected void clickA() {
				if (res.subject() != null)
				VIEW.s().ui.subjects.showSingle(res.subject());
				super.clickA();
			}
		};
		
		
	}
	
}
