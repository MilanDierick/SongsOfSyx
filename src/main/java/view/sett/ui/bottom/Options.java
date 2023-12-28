package view.sett.ui.bottom;

import static settlement.main.SETT.*;

import init.D;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.main.SETT;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.ACTION;
import util.gui.misc.GBox;
import util.gui.misc.GGrid;
import view.keyboard.KEYS;
import view.keyboard.KeyButt;
import view.main.VIEW;
import view.tool.PLACABLE;

final class Options extends SPanel{

	private static CharSequence ¤¤CopyArea = "Copy Area";
	
	static {
		D.ts(Options.class);
	}
	
	Options(){

		D.gInit(this);
		
		body().setWidth(BButt.WIDTH*2);
		GGrid grid = new GGrid(this, 2);
		
		{
			ACTION a = new ACTION() {
				
				@Override
				public void exe() {
					VIEW.inters().popup.close();
					VIEW.s().misc.copier.activate();
				}
			};
			CLICKABLE c = new BButt(SPRITES.icons().m.copy, ¤¤CopyArea){
				
				@Override
				protected void clickA() {
					a.exe();
				};
				
				
			};
			c = KeyButt.wrap(a, c, KEYS.SETT(), "COPY_SUPER", ¤¤CopyArea, "");
			SearchToolPanel.add(c, ¤¤CopyArea);
			
			grid.add(c);
		}
		make("COPY_ROOM", ROOMS().copy.copy(), grid);
		
		CLICKABLE c = new BButt(UI.icons().m.time, D.g("Planning")) {
			
			@Override
			protected void clickA() {
				SETT.JOBS().planMode.toggle();
			};
			
			@Override
			protected void renAction() {
				selectedSet(SETT.JOBS().planMode.is());
			};
			
			
		};
		c.hoverInfoSet(D.g("PlanningD", "When enabled, placed jobs will not be performed until manually activated by your grace."));
		grid.add(c);
		
		
		make("REPAIR", JOBS().tool_repair, grid);
		
		make("ACTIVATE", JOBS().tool_activate, grid);
		make("DORMANT", JOBS().tool_dormant, grid);
		
		make("MAINTENANCE_ON", SETT.MAINTENANCE().enablePlacer, grid);
		make("MAINTENANCE_OFF", SETT.MAINTENANCE().enablePlacer.getUndo(), grid);
		
		make("DIAGONALIZE", TERRAIN().diagonal.placer, grid);
		make("SQUAREIFY", TERRAIN().diagonal.undo, grid);
		
		addRelBody(8, DIR.S, new UISavedRooms());

		pad(8, 8);
	}
	
	private void make(String code, PLACABLE p, GGrid grid) {
		ACTION a = new ACTION() {
			
			@Override
			public void exe() {
				VIEW.inters().popup.close();
				VIEW.s().tools.place(p);
			}
		};
		CLICKABLE c = new BButt(p.getIcon(), p.name()){
			
			@Override
			protected void clickA() {
				a.exe();
			};
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				p.hoverDesc((GBox) text);
			}
			
			
		};
		c = KeyButt.wrap(a, c, KEYS.SETT(), code, p.name(), "");
		SearchToolPanel.add(c, p.name());
		grid.add(c);
	}
	
	public GuiSection ge2t() {
		return this;
	}
	
}
