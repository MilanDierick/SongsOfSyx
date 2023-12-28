package util.gui.table;

import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;

public class GRows {

	private GuiSection s = null;
	private final LinkedList<RENDEROBJ> rows = new LinkedList<>();
	private int ii = 0;
	private final int max;
	
	public GRows(int rowSize) {
		this.max = rowSize;
	}
	
	public void add(RENDEROBJ obj) {
		
		if (ii % max == 0) {
			s = new GuiSection();
			rows.add(s);
			ii = 0;
		}
		
		s.addRight(0, obj);
		ii++;
	}
	
	public LIST<RENDEROBJ> rows(){
		return rows;
	}
	
}
