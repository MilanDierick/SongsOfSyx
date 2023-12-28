package menu;

import static menu.GUI.*;

import java.util.LinkedList;

import init.D;
import init.settings.S;
import init.settings.S.Setting;
import menu.GUI.OptionLine;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import util.gui.misc.GText;

final class ScOptions extends GuiSection implements SC{

	private final LinkedList<Option> options = new LinkedList<Option>();
	private final CLICKABLE revert;
	
	final CharSequence ¤¤name = "¤settings";
	final CharSequence ¤¤revert = "¤revert";
	final CharSequence ¤¤default = "¤restore";
	
	ScOptions(Menu menu) {
		
		D.t(this);
		
		Screener screen = new Screener(¤¤name, GUI.labelColor) {
			
			@Override
			protected void back() {
				menu.switchScreen(menu.main);
			}
		};
		
		revert = new Screener.ScreenButton(¤¤revert) {
			@Override
			protected void clickA() {
				revert();
			}
		};
		
		screen.addButt(revert);
		
		add(screen);

		GuiSection keys = new GuiSection() {
			@Override
			public boolean click() {
				if (super.click()) {
					revert.activeSet(true);
				}
				return false;
			}
		};
		keys.body().moveY1(left.y1());
		int x1 = 0;
		
		for (init.settings.S.Setting s : S.get().all()){
			
			Option kc = new Option(s);
			if (keys.getLastY2() > left.y2()){
				x1 += margin*2 + margin*0.2;
				kc.body().moveY1(keys.body().y1());
			}else{
				kc.body().moveY1(keys.getLastY2());
			}
			kc.body().moveX2(x1);
			keys.add(kc);
		}
		keys.body().centerIn(bounds);
		
		keys.body().centerIn(this);
		
		add(keys);

	}
	
	void make(Setting s, CharSequence name, GuiSection keys, int i) {
		
	}
	
	private class Option extends OptionLine{

		private final Setting sett;
		
		protected Option(Setting s) {
			super(s, s.name);
			sett = s;
			options.add(this);
		}
		
		@Override
		protected void setValue(GText str) {
			sett.getValue(str);
		}
		
		@Override
		public boolean click() {
			if (super.click()) {
				S.get().applyRuntimeConfigs();
				return true;
			}
			return false;
		}
		
	}
	
	private void revert(){
		S.get().revert();
		S.get().applyRuntimeConfigs();
	}

	@Override
	public void hoverInfoGet(GUI_BOX text) {
		
	}
	
	@Override
	public boolean back(Menu menu) {
		menu.switchScreen(menu.main);
		return true;
	}
	
}
