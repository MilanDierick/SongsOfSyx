package view.ui.manage;

import init.C;
import snake2d.util.gui.GuiSection;
import view.main.VIEW;

public class IFullView {
	
	public final static int TOP_HEIGHT = IManager.TOP_HEIGHT+8;
	public static final int WIDTH = C.WIDTH()-32;
	public static final int HEIGHT = C.HEIGHT()-TOP_HEIGHT-8;
	
	public final CharSequence title;
	protected GuiSection section = new GuiSection();
	
	public IFullView(CharSequence name){
		this.title = name;
	}
	
	
	

	
	public void activate() {
		VIEW.UI().manager.show(this);
	}

	public boolean back() {
		return false;
	}
	


	
}