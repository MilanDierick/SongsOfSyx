package view.world.generator;

import game.faction.FACTIONS;
import game.faction.player.PTitles.PTitle;
import init.D;
import init.sprite.SPRITES;
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

	private static CharSequence ¤¤spent = "¤Pick 5 unlocked titles to boost your name.";
	private static CharSequence ¤¤YouSure = "¤You may still pick some unlocked titles. Start anyway?";
	
	static {
		D.ts(StagePickTitles.class);
	}
	
	StagePickTitles(Stages stage){
		
		ArrayList<RENDEROBJ> rows = new ArrayList<>(FACTIONS.player().titles.all().size());
		
		
		for (PTitle t : FACTIONS.player().titles.all()) {
			rows.add(new Butt(t));
		}
		
		add(new GScrollRows(rows, 400).view());
		
		
		addRelBody(8, DIR.N, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.iofkInv(text, FACTIONS.player().titles.selected(), 5);
			}
			
		}.r(DIR.N));
		addRelBody(4, DIR.N, new GHeader(¤¤spent));
		
		GuiSection s = new GuiSection();
		
		s.add(new GButt.ButtPanel(SPRITES.icons().m.arrow_left) {
			@Override
			protected void clickA() {
				new StagePickRace(stage);
			}
		}.hoverInfoSet(DicMisc.¤¤Previous));
		
		s.addRightC(2, new GButt.ButtPanel(SPRITES.icons().m.arrow_right) {
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
						stage.reset();
						stage.terrain();
					}
				};
				
				if (FACTIONS.player().titles.selected() < 5 && FACTIONS.player().titles.unlocked() > FACTIONS.player().titles.selected()) {
					VIEW.inters().yesNo.activate(¤¤YouSure, next, no, true);
				}else {
					next.exe();
				}
			}
		}.hoverInfoSet(DicMisc.¤¤Next));
		addRelBody(16, DIR.S, s);
		
		stage.dummy.add(this);
	}
	
	private static final class Butt extends GButt.ButtPanel {

		private final PTitle title;
		
		Butt(PTitle title){
			super(title.name);
			body.setDim(400, 32);
			this.title = title;
		}
		
//		@Override
//		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
//			ColorImp c = ColorImp.TMP;
//			if (title.selected())
//				c.set(GCOLOR.T().IGREAT);
//			else if(!title.unlocked())
//				c.set(GCOLOR.T().ERROR);
//			else if(FACTIONS.player().titles.selected() >= 5)
//				c.set(GCOLOR.T().INACTIVE);
//			else {
//				c.set(isHovered ? GCOLOR.T().HOVERED : GCOLOR.T().HOVERABLE);
//			}
//			
//			c.bind();
//			UI.FONT().M.renderCX(r, body().cX(), body().y1(), title.name);
//			
//			COLOR.unbind();
//			
//		}
		
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
				
				title.unlock.hoverInfoGet(b);
				b.NL();
			}
			
			
			
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
