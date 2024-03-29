package view.sett.ui.bottom;

import static settlement.main.SETT.*;

import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.ACTION;
import util.gui.misc.GBox;
import view.keyboard.KEYS;
import view.keyboard.KeyButt;
import view.main.VIEW;

final class Delete extends SPanel{
	
	protected Delete() {
		
		{	
			ACTION a = new ACTION() {
				
				@Override
				public void exe() {
					VIEW.inters().popup.close();
					VIEW.s().tools.place(JOBS().tool_remove_smartl);;
				}
			};
			CLICKABLE c = new BButt(JOBS().tool_remove_smartl.getIcon(), JOBS().tool_remove_smartl.name()){
				
				@Override
				protected void clickA() {
					a.exe();
				};
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					JOBS().tool_remove_smartl.hoverDesc((GBox) text);
				}
				
				
			};
			
			c = KeyButt.wrap(a, c, KEYS.SETT(), "REMOVE_SMART", JOBS().tool_remove_smartl.name(), "");
			addDownC(0, c);
		}
		
		{	
			ACTION a = new ACTION() {
				
				@Override
				public void exe() {
					VIEW.inters().popup.close();
					VIEW.s().tools.place(JOBS().tool_remove_all);;
				}
			};
			CLICKABLE c = new BButt(JOBS().tool_remove_all.getIcon(), JOBS().tool_remove_all.name()){
				
				@Override
				protected void clickA() {
					a.exe();
				};
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					JOBS().tool_remove_all.hoverDesc((GBox) text);
				}
				
				
			};
			c = KeyButt.wrap(a, c, KEYS.SETT(), "REMOVE_ALL", JOBS().tool_remove_all.name(), "");
			addDownC(0, c);
		}
		
		{	
			ACTION a = new ACTION() {
				
				@Override
				public void exe() {
					VIEW.inters().popup.close();
					VIEW.s().tools.place(JOBS().tool_clear);
				}
			};
			CLICKABLE c = new BButt(JOBS().tool_clear.getIcon(), JOBS().tool_clear.name()){
				
				@Override
				protected void clickA() {
					a.exe();
				};
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					JOBS().tool_clear.hoverDesc((GBox) text);
				}
				
			};
			c = KeyButt.wrap(a, c, KEYS.SETT(), "REMOVE_JOB", JOBS().tool_clear.name(), "");
			addDownC(0, c);
		}
		
		{	
			ACTION a = new ACTION() {
				
				@Override
				public void exe() {
					VIEW.inters().popup.close();
					VIEW.s().tools.place(ROOMS().DELETE);
				}
			};
			CLICKABLE c = new BButt(ROOMS().DELETE.getIcon(), ROOMS().DELETE.name()){
				
				@Override
				protected void clickA() {
					a.exe();
				};
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					ROOMS().DELETE.hoverDesc((GBox) text);
				}
				
			};
			c = KeyButt.wrap(a, c, KEYS.SETT(), "REMOVE_ROOM", ROOMS().DELETE.name(), "");
			addDownC(0, c);
		}
		
		pad(3, 8);
		
	}
	
	
}
