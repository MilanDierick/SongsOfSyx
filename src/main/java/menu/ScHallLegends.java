package menu;

import static menu.GUI.*;

import init.C;
import init.D;
import init.sprite.UI.UI;
import menu.GUI.COLORS;
import menu.screens.Screener;
import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.light.PointLight;
import snake2d.util.misc.ACTION;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Text;

class ScHallLegends extends GuiSection implements SCREEN{
	
	private final Credit[] credits = new Credit[]{
		new Credit("Jake", "Supreme Developer, Creator of worlds, Bringer of Syxians"),
		new Credit("Natalia Jasinska", "Mistress of soundtracks"),
		new Credit("Gianluca Borg", "High Councelor, Spokesman of the Plebs, Guardian of History"),
		new Credit("Superwutz", "Sacred voice of modability, Father of the Agonosh, He whose name is hard to remember"),
		new Credit("ProRt", "First knighted, Finder of bugs"),
		new Credit("Connor Bryant", "Generous benefactor"),
		new Credit("JollyWarhammer", "Warrior Monk"),
		new Credit("Bendigeidfran", "Champion of Art"),
	};
	private Text sname = UI.FONT().H1S.getText(200);
	private Text stitles = UI.FONT().H2.getText(200);
	
	private int current = 0;
	private final CLICKABLE next;
	private final CLICKABLE prev;
	private final int set = 4;
	
	private final int textY1;
	
	private final HOVERABLE frames;
	final CharSequence ¤¤name = "¤hall of legends";

	ScHallLegends(Menu menu){
		
		D.t(this);
		
		prev = getNavButt("<<");
		prev.clickActionSet(new ACTION() {
			@Override
			public void exe() {
				if (current > 0)
					current --;
				prev.activeSet(current > 0);
				next.activeSet(current*set + set < credits.length);
				
			}
		});
		prev.activeSet(current > 0);
		add(prev);
		
		next = getNavButt(">>");
		next.clickActionSet(new ACTION() {
			@Override
			public void exe() {
				if (current < credits.length)
					current ++;
				prev.activeSet(current > 0);
				next.activeSet(current*set + set < credits.length);
			}
		});
		next.activeSet(current*set + set < credits.length);
		addRightC(20, next);
		
		body().centerX(C.DIM());
		body().moveY1(bottomY);
		
		add(new Screener(¤¤name, GUI.labelColor) {
			
			@Override
			protected void back() {
				menu.switchScreen(menu.main);
			}
		});
		
		frames = new HOVERABLE.HoverableAbs() {
			
			private final int m = 10;
			private int hovered = -1;
			private final SPRITE frame = RESOURCES.s().creditsBigFrame;
			private final SPRITE[] ps = RESOURCES.s().creditsBig;
			private final PointLight light = new PointLight();
			
			{
				light.setRadius(200);
				light.setZ(200);
				double i = 1.5;
				light.setRed(i).setGreen(i).setBlue(i);
				body.setWidth(set*frame.width() + (set-1)*m);
				body.setHeight(frame.height());
			}
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				for (int i = 0; i < set; i++) {
					if (i+current*set >= credits.length)
						break;
					
					if (i == hovered) {
						light.set(body.x1()+i*(m + frame.width()) + frame.width()/2, body.cY());
						light.register();
					}else {
						
					}
					
					ps[i+current*set].render(r, body.x1()+i*(m + frame.width()), body.y1());
					COLOR.unbind();
					frame.render(r, body.x1()+i*(m + frame.width()), body.y1());
				}
			}
			
			@Override
			public boolean hover(COORDINATE mCoo) {
				
				hovered = -1;
				sname.clear();
				if (super.hover(mCoo)) {
					for (int i = 0; i < set; i++) {
						if (mCoo.x() >= body.x1()+i*(m + frame.width()))
							if (mCoo.x() < body.x1()+i*(m + frame.width()) + frame.width()) {
								hovered = i;
								if (hovered + current*set < credits.length) {
									sname.set(credits[hovered + current*set].name);
									stitles.clear().set(credits[hovered + current*set].titles);
								}
								
							}
					}
					
					return true;
				}
				return false;
			}

		};
		
		frames.body().centerIn(bounds);
		frames.body().incrY(-30);
		frames.body().incrY(8);
		
		textY1 = (int) (bounds.y2()-32*2.5);
		//add(frames);
		
	}
	
	@Override
	public boolean hover(COORDINATE mCoo) {
		frames.hover(mCoo);
		return super.hover(mCoo);
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		super.render(r, ds);
		if (sname.length()!= 0) {
			
			COLORS.label.bind();
			int x1 = C.DIM().cX() - (sname.width())/2;
			sname.render(r, x1, textY1);
			x1 = C.DIM().cX() - (stitles.width())/2;
			COLORS.copper.bind();
			stitles.render(r, x1, textY1+35);
			
			COLOR.unbind();
		}
	}
	
	@Override
	public void renderBackground(Background back, float ds, COORDINATE mCoo) {
		
		back.renderFame(CORE.renderer(), ds, mCoo, 0);
		frames.render(CORE.renderer(), ds);
		
	}
	
	@Override
	public boolean back(Menu menu) {
		menu.switchScreen(menu.main);
		return true;
	}
	
	final static class Credit {
		
		final String name;
		final String titles;
		
		Credit(String name, String titles){
			this.name = name.toLowerCase();
			this.titles = titles;
		}
		
	}
	
}
