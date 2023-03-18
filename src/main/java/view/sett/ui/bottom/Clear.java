package view.sett.ui.bottom;

import static settlement.main.SETT.*;

import settlement.main.SETT;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.ACTION;
import util.data.GETTER.GETTER_IMP;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import view.keyboard.KEYS;
import view.keyboard.KeyButt;
import view.main.VIEW;
import view.sett.ui.room.construction.UIRoomPlacer;
import view.tool.PLACABLE;

final class Clear extends GuiSection{

	
	Clear(UIRoomPlacer placer, GETTER_IMP<ACTION> last){
		
		int i = 0;
		for (PLACABLE p : JOBS().clears) {
			ACTION a = new ACTION() {
				
				@Override
				public void exe() {
					VIEW.inters().popup.close();
					VIEW.s().tools.place(p);
				}
			};
			CLICKABLE c = new GButt.ButtPanel(p.name()){
				
				@Override
				protected void clickA() {
					a.exe();
				};
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					p.hoverDesc((GBox)text);
				}
				
				
			}.icon(p.getIcon()).setDim(Popup.width, Popup.bh);
			c = KeyButt.wrap(a, c, KEYS.SETT(), "JOB_" +i++, p.name(), p.name());
			SearchToolPanel.add(c, p.name());
			
			addDownC(0, c);
		}
		
		addDownC(0, PopupRooms.butt(SETT.ROOMS().BUILDER, placer, last));
		
		
	}
	
	public GuiSection get() {
		return this;
	}
	
}
