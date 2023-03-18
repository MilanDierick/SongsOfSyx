package view.sett.ui.bottom;

import init.C;
import init.D;
import init.sprite.ICON;
import init.sprite.SPRITES;
import settlement.main.SETT;
import snake2d.*;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.LinkedList;
import util.data.GETTER.GETTER_IMP;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import view.interrupter.InterManager;
import view.interrupter.Interrupter;
import view.keyboard.KEYS;
import view.keyboard.KeyButt;
import view.main.VIEW;
import view.sett.ui.room.construction.UIRoomPlacer;
import view.tool.PLACABLE;

public final class UIBuildPanel extends Interrupter{
	
	private final GuiSection section = new GuiSection();
	private GETTER_IMP<ACTION> lastClicked = new GETTER_IMP<>();
	private SearchToolPanel searchPanel; 
	
	public UIBuildPanel(UIRoomPlacer placer, InterManager m) {
		pin();
		m.add(this);
		SearchToolPanel.all = new LinkedList<>();
		SETT.addGeneratorHook(new ACTION() {
			
			@Override
			public void exe() {
				SearchToolPanel.all = new LinkedList<>();
			}
		});
		
		section.add(SPRITES.specials().lowerPanel(), 0, 0);
		
		section.body().centerX(0, C.WIDTH());
		section.body().moveY2(C.HEIGHT());
		
		GuiSection s = new GuiSection();
		D.gInit(this);
		int width = 60;
		int height = 32;

		

		{
			CLICKABLE c = new GButt.ButtPanel(SPRITES.icons().m.search).setDim(width-16, height);
			ACTION sa = new ACTION() {
				
				@Override
				public void exe() {
					searchPanel.open(c);
					
				}
			};
			c.clickActionSet(sa);
			CLICKABLE cc = KeyButt.wrap(sa, c, KEYS.SETT(), "toolSearch", D.g("Search"), D.g("SearchD", "Search for tools and rooms"), KEYCODES.KEY_LEFT_CONTROL, KEYCODES.KEY_F);
			s.addRightC(8, cc);
		}
		
		
		CLICKABLE cc = new GButt.ButtPanel(SPRITES.icons().m.building) {
			
			Popup p;
			ACTION rebuild = new ACTION() {
				@Override
				public void exe() {
					p = PopupRooms.civic(placer, lastClicked);
				}
			};
			{
				rebuild.exe();
				SETT.addGeneratorHook(rebuild);
			}
			
			@Override
			protected void clickA() {
				VIEW.inters().popup.show(p.get(), this);
			}
		
			
		}.setDim(width, height);  
		s.addRightC(8, cc);
		
		{
			Clear sec = new Clear(placer, lastClicked);
			CLICKABLE c = new GButt.ButtPanel(SPRITES.icons().m.shovel) {
				
				@Override
				protected void clickA() {
					VIEW.inters().popup.show(sec.get(), this);
				}
				
			}.setDim(width, height);   
			s.addRightC(0, c);
		}
		
		{
			Options sec = new Options();
			CLICKABLE c = new GButt.ButtPanel(SPRITES.icons().m.menu2) {
				
				@Override
				protected void clickA() {
					VIEW.inters().popup.show(sec.get(), this);
				}
				
			}.setDim(width, height);   
			s.addRightC(0, c);
		}
		
		{
			Delete delete = new Delete();
			CLICKABLE c = new GButt.ButtPanel(SPRITES.icons().m.cancel) {
				
				@Override
				protected void clickA() {
					VIEW.inters().popup.show(delete, this);
				}
				
			}.setDim(width, height);   
			s.addRightC(0, c);
		}
	
		
		s.body().centerIn(section);
		s.body().incrY(4);
		section.add(s);
		
		searchPanel = new SearchToolPanel();
		SETT.addGeneratorHook(new ACTION() {
			
			@Override
			public void exe() {
				searchPanel = new SearchToolPanel();
			}
		});
	
		
	}

	@Override
	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
		return section.hover(mCoo);
	}

	@Override
	protected void mouseClick(MButt button) {
		if (button == MButt.LEFT)
			section.click();
		
	}

	@Override
	protected void hoverTimer(GBox text) {
		section.hoverInfoGet(text);
	}

	@Override
	protected boolean render(Renderer r, float ds) {
		
		section.render(r, ds);
		
		return true;
	}
	
	
	

	@Override
	protected boolean update(float ds) {
		return true;
	}
	
	protected static class Butt extends GButt.Panel {
		
		private final PLACABLE p;
		
		Butt(PLACABLE p){
			super(p.getIcon());
			this.p = p;
		}
		
		Butt(PLACABLE p, ICON.MEDIUM icon){
			super(p.getIcon());
			this.p = p;
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			p.hoverDesc((GBox)text);
		}
		
		@Override
		protected void clickA() {
			VIEW.s().tools.place(p);
		}
		
		@Override
		protected void renAction() {
			selectedSet(p == VIEW.s().tools.placer.getCurrent());
		}
		
	}
	

	
}
