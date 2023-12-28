package view.world.generator;

import game.faction.FACTIONS;
import game.faction.player.PTitles.PTitle;
import init.D;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayList;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import view.main.VIEW;

class StagePickTitles extends GuiSection{

	static CharSequence ¤¤title = "¤Select Titles";
	static CharSequence ¤¤spent = "¤Pick 5 unlocked titles to boost your name.";
	static CharSequence ¤¤YouSure = "¤You may still pick some unlocked titles. Start anyway?";
	
	static {
		D.ts(StagePickTitles.class);
	}
	
	StagePickTitles(WorldViewGenerator stage){
		
		addRelBody(4, DIR.S, new GText(UI.FONT().M, ¤¤spent));
		
		addRelBody(8, DIR.S, new GStat(UI.FONT().M) {
			
			@Override
			public void update(GText text) {
				GFORMAT.iofkInv(text, FACTIONS.player().titles.selected(), 5);
			}
			
		}.r(DIR.N));
		
		ArrayList<RENDEROBJ> rows = new ArrayList<>(FACTIONS.player().titles.all().size());
		
		for (PTitle t : FACTIONS.player().titles.all()) {
			rows.add(new Butt(t));
		}
		
		addRelBody(8, DIR.S, new GScrollRows(rows, rows.get(0).body().height()*6).view());
		
		addRelBody(16, DIR.S, new GButt.ButtPanel(DicMisc.¤¤confirm) {
			@Override
			protected void clickA() {
				ACTION no = new ACTION() {
					
					@Override
					public void exe() {
						// TODO Auto-generated method stub
						
					}
				};
				ACTION next = new ACTION() {
					
					@Override
					public void exe() {
						stage.hasSelectedTitles = true;
						stage.set();
					}
				};
				
				if (FACTIONS.player().titles.selected() < 5 && FACTIONS.player().titles.unlocked() > FACTIONS.player().titles.selected()) {
					VIEW.inters().yesNo.activate(¤¤YouSure, next, no, true);
				}else {
					next.exe();
				}
			}
		});
		
		stage.dummy.add(this, ¤¤title);
	}
	
	private static final class Butt extends GButt.ButtPanel {

		private final PTitle title;
		
		Butt(PTitle title){
			super(title.name);
			icon(title.icon.scaled(2));
			body.setDim(600, FACTIONS.player().titles.all().get(0).icon.height()*2+8);
			this.title = title;
		}
		
		@Override
		protected void renAction() {
			activeSet(title.unlocked());
			selectedSet(title.selected());
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			GBox b = (GBox) text;
			if (!title.unlocked()) {
				b.error(DicMisc.¤¤Unavailable);
			}else {
				b.title(title.name);
				b.text(title.desc);
				b.NL(6);
				
				b.sep();
				
				title.lockers.hover(text);
				
				title.boosters.hover(text, 1.0, -1);
				
				b.NL();
			}
			
			
			
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			super.render(r, ds, isActive, isSelected, isHovered);
		}
		
		@Override
		protected void clickA() {
			if (title.selected())
				title.select(!title.selected());
			else if(!title.unlocked())
				;
			else if(FACTIONS.player().titles.selected() >= 5)
				;
			else {
				title.select(!title.selected());
			}
			
		}
		
	}
	
}
