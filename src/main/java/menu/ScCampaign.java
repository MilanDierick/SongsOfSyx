package menu;

import static menu.GUI.*;

import init.D;
import init.paths.PATHS;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;

class ScCampaign extends GuiSection implements SC{

	private final int margin;
	final CharSequence ¤¤name = "¤campaigns";
	
	
	ScCampaign(Menu menu){
		
		D.gInit(this);
		
		Screener screen = new Screener(¤¤name, GUI.labelColor) {
			
			@Override
			protected void back() {
				menu.switchScreen(menu.main);
			}
		};
		
		add(screen);
		
		margin = getSmallText("aaaaaaaaaaaaaaaa").width();
		
		
		
		GuiSection options = new GuiSection();
		
		options.body().moveY1(left.y1());
		int x1 = 0;
		
		String[] camps = PATHS.MISC().CAMPAIGNS.folders();
		
		for (String s : camps){
			CLICKABLE b = getSmallButt(s);
			if (options.getLastY2() > left.y2()){
				x1 += margin + margin*0.2;
				b.body().moveX1Y1(x1, left.y1());
			}else{
				b.body().moveX1Y1(x1, options.getLastY2());
			}
			b.activeSet(false);
			options.add(b);
		}
		
		options.body().centerX(bounds);
		
		add(options);
		
	}

	@Override
	public boolean back(Menu menu) {
		menu.switchScreen(menu.main);
		return true;
	}

	
}
