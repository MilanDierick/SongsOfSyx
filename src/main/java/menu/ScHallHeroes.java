package menu;

import static menu.GUI.*;

import init.C;
import init.D;
import init.sprite.UI.UI;
import menu.GUI.COLORS;
import menu.ScHallLegends.Credit;
import menu.screens.Screener;
import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.light.PointLight;
import snake2d.util.misc.ACTION;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Text;

class ScHallHeroes extends GuiSection implements SCREEN{

	private final Credit[] credits = new Credit[]{
		new Credit("Laki 95", "Bringer of Suggestions"),
		new Credit("Dr. Kelloggs", "Titles..."),
		new Credit("Licher", "The King of the People and Protector of Syx"),
		new Credit("Qbjik", "The Lazy Panda, Lord of Quokkas"),
		new Credit("Mathedarius & Daniella", "The Wise, The Wolves, Breaker of Chains"),
		new Credit("Sparrow", "the Sweet, the Chonk, Purrveyor of Mews"),
		new Credit("Mathias Dietrich", "Beacon of the Free"),
		new Credit("Felix Ungman", "Shogun"),
	};
	private Text sname = UI.FONT().H1S.getText(200);
	private Text stitles = UI.FONT().H2.getText(200);
	
	private int current = 0;
	private final CLICKABLE next;
	private final CLICKABLE prev;
	private final int set = 10;
	private final int textY1;
	
	private final HOVERABLE frames;
	final CharSequence ¤¤name = "¤hall of heroes";
	
	
	ScHallHeroes(Menu menu){
		
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
		

		
		textY1 = (int) (bounds.y2()- sname.height()*2.5);
		
		add(new Screener(¤¤name, GUI.labelColor) {
			
			@Override
			protected void back() {
				menu.switchScreen(menu.main);
			}
		});
		
		frames = new HOVERABLE.HoverableAbs() {
			
			private final int m = 6;
			private int hovered = -1;
			private final SPRITE frame = RESOURCES.s().creditsSmallFrame;
			private final SPRITE[] ps = RESOURCES.s().creditsSmall;
			private final PointLight light = new PointLight();
			
			{
				light.setRadius(150);
				light.setZ(150);
				light.setRed(1).setGreen(1).setBlue(1);
				body.setWidth(set*frame.width()/2 + (set/2-1)*m);
				body.setHeight(frame.height()*2 + m);
			}
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				for (int y = 0; y < 2; y++) {
					for (int x = 0; x < set/2; x++) {
						int i = y*set/2 + x;
						if (i+current*set >= credits.length)
							break;
						
						if (i == hovered) {
							light.set(body.x1()+x*(m + frame.width())+ frame.width()/2, body.y1()+y*(m+frame.height()) + frame.width()/2);
							light.register();
						}else {
							
						}
						
						ps[i+current*set].render(r, body.x1()+x*(m + frame.width()), body.y1()+y*(m+frame.height()));
						
			
						frame.render(r, body.x1()+x*(m + frame.width()), body.y1()+y*(m+frame.height()));

						
					}
				}
			}
			
			@Override
			public boolean hover(COORDINATE mCoo) {
				hovered = -1;
				sname.clear();
				if (super.hover(mCoo)) {
					int i = (mCoo.x() - body.x1()) / (m + frame.width());
					i += mCoo.y() > body.cY() ? set/2 : 0;
					
					int h = current*set + i;
					if (h >= 0 && h < credits.length) {
						hovered = h;
						sname.set(credits[h].name);
						stitles.clear().set(credits[h].titles);
					}

					return true;
				}
				return false;
			}

		};
		
		frames.body().centerIn(bounds);
		frames.body().incrY(-40);
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
	
	
}
