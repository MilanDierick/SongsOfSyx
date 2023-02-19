package menu;

import static menu.GUI.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import init.C;
import init.D;
import init.paths.PATHS;
import init.sprite.UI.UI;
import menu.screens.Screener;
import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.light.PointLight;
import snake2d.util.misc.ACTION;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TextureCoords;

class ScHallFame extends GuiSection implements SCREEN{
	
	

	private final PointLight light = new PointLight();

	
	
	
	private int current = 0;
	private final CLICKABLE next;
	private final CLICKABLE prev;
	
	private final ArrayList<RENDEROBJ> screens = new ArrayList<>(100);
	
	final CharSequence ¤¤name = "¤hall of fame";

	ScHallFame(Menu menu){
		
		D.t(this);
		
		double in = 1.0;
		light.setRed(in).setGreen(in).setBlue(in);
		light.setRadius(900);
		light.setZ(200);
		
		try {
			fill(D.g("nobility"), new String(Files.readAllBytes(PATHS.BASE().DATA.get("Nobles")),StandardCharsets.UTF_8).split(System.lineSeparator()));
			fill(D.g("knights"), new String(Files.readAllBytes(PATHS.BASE().DATA.get("Knights")), StandardCharsets.UTF_8).split(System.lineSeparator()));
			fill(D.g("citizens"), new String(Files.readAllBytes(PATHS.BASE().DATA.get("Citizens")), StandardCharsets.UTF_8).split(System.lineSeparator()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		prev = getNavButt("<<");
		prev.clickActionSet(new ACTION() {
			@Override
			public void exe() {
				if (current > 0)
					current --;
				prev.activeSet(current > 0);
				next.activeSet(current < screens.size()-1);
				
			}
		});
		prev.activeSet(current > 0);
		add(prev);
		
		next = getNavButt(">>");
		next.clickActionSet(new ACTION() {
			@Override
			public void exe() {
				if (current < screens.size())
					current ++;
				prev.activeSet(current > 0);
				next.activeSet(current < screens.size()-1);
			}
		});
		next.activeSet(current < screens.size()-1);
		addRightC(20, next);
		
		
		body().centerX(C.DIM());
		body().moveY1(bottomY);
		
		

		Screener screen = new Screener(¤¤name, GUI.labelColor) {
			
			@Override
			protected void back() {
				menu.switchScreen(menu.main);
			}
		};
		
		add(screen);
		moveLastToBack();
		
	}
	
	
	private void fill(CharSequence title, String[] names) {
		
		
		int x1 = inner.x1();
		int x2 = inner.x2()+100;
		
		
		
		int i = 0;
		
		int scale = 2;
		
		int margin = 20;
		int height = UI.FONT().H2.height()*scale;
		
		int y2 = inner.y2()-height;
		
		while (i < names.length) {
			
			GuiSection s = new GuiSection();
			s.add(new RENDEROBJ.Sprite(UI.FONT().H1S.getText((Object)title).setScale(2)));
			s.body().moveY1(inner.y1());
			s.body().centerX(inner);
			int y1 = s.body().y2()+10;
			int x = x1;
			while(i < names.length) {
				x += RND.rInt(20);
				RENDEROBJ.Sprite o = new RENDEROBJ.Sprite(new Name(names[i]));
				i++;
				o.setColor(COLOR.WHITE100);
				if (x + o.body().width() > x2) {
					y1 += height;
					if (y1 > y2)
						break;
					x = x1 +RND.rInt(30);
				}
				o.body().moveX1(x).moveY1(y1+RND.rInt(20));
				x+= o.body().width() + margin;
				s.add(o);
				
			}
			screens.add(s);
			
		}
		
		
	}
	
	private static final class Name implements SPRITE {

		private final int width;
		private final CharSequence name;
		
		Name(CharSequence s){
			this.name = s;
			width = UI.FONT().H2.getDim(s).x()*2;
		}
		
		@Override
		public int width() {
			return width;
		}

		@Override
		public int height() {
			return UI.FONT().H2.height()*2;
		}

		@Override
		public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
			
			UI.FONT().H2.render(r, name, X1, Y1, 2);
		}

		@Override
		public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	
	@Override
	public void renderBackground(Background back, float ds, COORDINATE mCoo) {
		back.renderFame(CORE.renderer(), ds, mCoo, current);
		light.set(inner.cX(), inner.cY());
		light.register();
		OPACITY.O99.bind();
		screens.get(current).render(CORE.renderer(), ds);
		OPACITY.unbind();
	}

	
	@Override
	public boolean hover(COORDINATE mCoo) {
		
		return super.hover(mCoo);
	}
	
	@Override
	public boolean back(Menu menu) {
		menu.switchScreen(menu.main);
		return true;
	}

	
	
}
