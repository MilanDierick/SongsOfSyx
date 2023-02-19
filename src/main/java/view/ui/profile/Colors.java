package view.ui.profile;

import game.faction.player.PlayerColors;
import game.faction.player.PlayerColors.PlayerColor;
import init.sprite.UI.UI;
import snake2d.util.color.ColorImp;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Text;
import util.gui.misc.*;
import util.gui.table.GScrollRows;
import view.main.VIEW;

class Colors extends GuiSection{

	private GuiSection popup = new GuiSection();
	private PlayerColor color;
	
	Colors(int height){
		
		popup.add(new GColorPicker(false) {
			
			@Override
			public ColorImp color() {
				return color.color;
			}
		});
		
		LinkedList<RENDEROBJ> rows = new LinkedList<>();
		
		for (String cat : PlayerColors.cats().keysSorted()) {
			rows.add(new GTextR(new GText(UI.FONT().S, cat).lablify()));
			for (PlayerColor c : PlayerColors.cats().get(cat)) {
				Text t = new Text(UI.FONT().S, c.name);
				t.setMaxChars(10);
				rows.add(new GButt.Glow((SPRITE) t){
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						text.text(c.name);
					}
					
					@Override
					protected void clickA() {
						color = c;
						VIEW.inters().popup.show(popup, this);
					}
				});
			}
		}
		
		add(new GScrollRows(rows, height).view());
		
	}


	
}
