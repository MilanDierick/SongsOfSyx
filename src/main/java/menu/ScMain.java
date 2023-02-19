package menu;

import static menu.GUI.*;

import game.GameLoader;
import init.D;
import init.paths.PATHS;
import init.sprite.UI.UI;
import menu.GUI.Button;
import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;

class ScMain implements SCREEN{

	private final GuiSection first;
	private final GuiSection play;
	private final GuiSection credits;	
	private GuiSection current;
	private final RENDEROBJ.Sprite logo;
	private final Menu menu;

	ScMain(Menu menu) {
		
		D.t(this);
		this.menu = menu;
		first = getFirst(menu);
		play = getPlay(menu);
		play.body().moveY1(first.body().y1());
		credits = getCredits(menu);
		credits.body().moveY1(first.body().y1());
		
		logo = new RENDEROBJ.Sprite(RESOURCES.s().logo);
		logo.body().moveX2(left.x2());
		logo.body().centerY(left);
		logo.setColor(GUI.COLORS.menu);
		
		
		
		current = first;
		
	}
	
	private GuiSection getFirst(Menu menu){
		
		GuiSection current = new GuiSection();
		CLICKABLE text;
		
		text = getNavButt(D.g("play"));
		text.clickActionSet(new ACTION() {
			@Override
			public void exe() {
				switchNavigator(play);
			}
		});
		current.addDown(0, text);
		
		text = new Button(UI.FONT().H1S.getText(D.g("continue"))) {
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
					boolean isHovered) {
				activeSet(menu.load.hasSaves());
				super.render(r, ds, isActive, isSelected, isHovered);
			}
			
			@Override
			protected void clickA() {
				if (menu.load.hasSaves())
					menu.load.loadSave();
			}
		};
		current.addDown(8, text);
		
		text = getNavButt(D.g("settings"));
		text.clickActionSet(new ACTION() {
			@Override
			public void exe() {
				menu.switchScreen(menu.options);
			}
		});
		current.addDown(8, text);
		
		text = getNavButt(D.g("credits"));
		text.clickActionSet(new ACTION() {
			@Override
			public void exe() {
				switchNavigator(credits);
			}
		});
		current.addDown(8, text);
		
		text = getNavButt(D.g("quit"));
		text.clickActionSet(new ACTION() {
			@Override
			public void exe() {
				CORE.annihilate();
			}
		});
		current.addDown(8, text);
		
		current.body().moveX1(right.x1());
		current.body().centerY(right.y1(), right.y2());
		
//		RENDEROBJ rr = new RENDEROBJ.RenderImp(600, 400) {
//			
//			private final Str s = new Str("");
//			{
//				s.add("Quicksave").s().NL();
//				s.add("This is you captain speaking. Please ramian in your seats while I open this new conjak. I feel a bit dizzy, I think I'm loosing control of the plane");
//				s.NL();
//				s.add("abcdefghijklmnopqrstuvxyzåäöabcdefghijklmnopqrstuvxyzåäöabcdefghijklmnopqrstuvxyzåäöabcdefghijklmnopqrstuvxyzåäö");
//			}
//			double d = 0;
//			
//			@Override
//			public void render(SPRITE_RENDERER r, float ds) {
//				d += ds*0.5;
//				int st = (int) (d % DIR.ALL.size());
//				COLOR.RED100.render(r, body);
//				
//				Font f = UI.FONT().H2;
//				
//				
//				f.renderIn(r, body(), DIR.ALL.get(st), s, 2);
//				
//				
//			}
//		};
//		
//		current.addRelBody(8, DIR.W, rr);
		
		return current;
	}
	
	private GuiSection getCredits(Menu menu){
		
		GuiSection current = new GuiSection();
		CLICKABLE text;
		

		
		text = getNavButt(menu.hallOfLegends.¤¤name);
		text.clickActionSet(new ACTION() {
			@Override
			public void exe() {
				menu.switchScreen(menu.hallOfLegends);
			}
		});
		current.addDown(8, text);
		
		text = getNavButt(menu.hallOfHeroes.¤¤name);
		text.clickActionSet(new ACTION() {
			@Override
			public void exe() {
				menu.switchScreen(menu.hallOfHeroes);
			}
		});
		current.addDown(8, text);
		
		text = getNavButt(menu.hallOfFame.¤¤name);
		text.clickActionSet(new ACTION() {
			@Override
			public void exe() {
				menu.switchScreen(menu.hallOfFame);
			}
		});
		current.addDown(8, text);
		
		text = getNavButt(menu.credits.¤¤name);
		text.clickActionSet(new ACTION() {
			@Override
			public void exe() {
				menu.switchScreen(menu.credits);
			}
		});
		current.addDown(8, text);
		
		text = getBackArrow();
		text.clickActionSet(new ACTION() {
			@Override
			public void exe() {
				switchNavigator(first);
			}
		});
		current.addDown(10, text);
		
		current.body().moveX1(right.x1());
		current.body().centerY(right);
		
		return current;
	}
	
	
	private GuiSection getPlay(Menu menu){
		
		GuiSection current = new GuiSection();
		
		CLICKABLE text;
		
		text = getNavButt(ScLoad.¤¤name);
		text.clickActionSet(new ACTION() {
			@Override
			public void exe() {
				menu.switchScreen(menu.load);
			}
		});
		current.addDown(0, text);
		
		
		text = getNavButt(D.g("tutorial"));
		text.clickActionSet(new ACTION() {
			@Override
			public void exe() {
				menu.start(new GameLoader(PATHS.MISC().SAVES.get("_Tutorial"), "_Tutorial"));
			}
		});
		text.activeSet(PATHS.MISC().hasTutorial);
		current.addDown(8, text);
		
		text = getNavButt(menu.examples.¤¤name);
		text.clickActionSet(new ACTION() {
			@Override
			public void exe() {
				menu.switchScreen(menu.examples);
			}
		});
		current.addDown(8, text);
		
		text = getNavButt(menu.sandbox.¤¤name);
		text.clickActionSet(new ACTION() {
			@Override
			public void exe() {
				menu.switchScreen(menu.sandbox);
			}
		});
		current.addDown(8, text);
		
		text = getBackArrow();
		text.clickActionSet(new ACTION() {
			@Override
			public void exe() {
				switchNavigator(first);
			}
		});
		current.addDown(10, text);
		
		current.body().moveX1(right.x1());
		current.body().centerY(right);
		
		return current;
	}
	
	private void switchNavigator(GuiSection section){
		current = section;
		current.hover(menu.getMCoo());
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		logo.render(r, ds);
		current.render(r, ds);
		
	}

	@Override
	public boolean hover(COORDINATE mCoo) {
		return current.hover(mCoo);
	}

	@Override
	public boolean click() {
		return current.click();
	}
	
	@Override
	public boolean back(Menu menu) {
		if (current != first) {
			switchNavigator(first);
			return true;
		}
		return false;
	}
	

}
