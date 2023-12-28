package view.main;

import view.interrupter.*;
import view.menu.IMenu;
import view.ui.message.Messages;

public class Interrupters {
	
	public final InterManager manager = new InterManager();
	public final IMenu menu = new IMenu(manager);
	public final ITextInput input = new ITextInput(manager);
	public final IPromtScreen fullScreen = new IPromtScreen(manager);
	public final IPromtYesNO yesNo = new IPromtYesNO(manager);
	public final IDebugPanel debugpanel;
//	public final ITmpPanel panelTmp = new ITmpPanel(manager);
	public final IMouseMessage mouseMessage = new IMouseMessage();
	public final InterGuisection section = new InterGuisection(manager);
	public final Messages messages;
	public final IPopup2 popup = new IPopup2(manager);
	public final IPopup2 popup2 = new IPopup2(manager);
	public final ILoadScreen load = new ILoadScreen(manager);
	
	
	
	public Interrupters(){
		messages = new Messages(manager);

		
		debugpanel = new IDebugPanel(manager);
	}
	
//	public InterManager getManager() {
//		return manager;
//	}
}