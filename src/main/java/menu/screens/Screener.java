package menu.screens;

import init.C;
import init.D;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.*;
import snake2d.util.datatypes.*;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;

public abstract class Screener extends GuiSection{

	static CharSequence ¤¤back = "¤< back";
	static {
		D.ts(Screener.class);
	}
	private static final RECTANGLE bounds = new Rec(1200, 500);
	
	private final GuiSection bottombutts = new GuiSection();
	
	
	public Screener(CharSequence title, COLOR color) {
	
		body().set(bounds);
		body().centerIn(C.DIM());
		
		
		RENDEROBJ s = UI.decor().frame(this.body(), color);
		s.body().centerIn(this.body());
		add(s);
		s = UI.decor().decorate(title, color);
		s.body().centerIn(C.DIM());
		s.body().moveY2(getLastY1());
		add(s);
		
		
		
		ScreenButton b = new ScreenButton(UI.FONT().H1S.getText(¤¤back)) {
			
			@Override
			protected void clickA() {
				Screener.this.back();
			}
			
		};
		b.body().moveX2(body().x2()-80);
		b.body().moveY1(body().y1()+8);
		add(b);
		addRelBody(14, DIR.S, bottombutts);
		
	}
	
	public void addButt(RENDEROBJ obj) {
		bottombutts.addRightC(24, obj);
		bottombutts.body().centerX(this);
	}
	
	protected abstract void back();
	
	public static class ScreenButton extends CLICKABLE.ClickableAbs {

		private final SPRITE s;
		static COLOR normal = COLOR.WHITE100;
		static COLOR hover = new ColorShifting(new ColorImp(127,127,65),
				new ColorImp(110,90,45));
		static COLOR selected = new ColorShifting(new ColorImp(127,20,10),
				new ColorImp(20,127,10)).setSpeed(2.0f);
		static COLOR hover_selected = COLOR.GREEN100;
		static COLOR inactive = new ColorImp(80, 70, 60); //COLOR.BROWN; //72, 58, 33
		
		public ScreenButton(CharSequence name){
			this((SPRITE) UI.FONT().H1S.getText(name));
		}
		
		public ScreenButton(SPRITE s) {
			this.s = s;
			body.setWidth(s.width()).setHeight(s.height());
		}

		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive,
				boolean isSelected, boolean isHovered) {
			if (!isActive)
				GCOLOR.T().INACTIVE.bind();
			else if (isHovered && isSelected)
				GCOLOR.T().HOVER_SELECTED.bind();
			else if (isHovered)
				GCOLOR.T().HOVERED.bind();
			else if (isSelected)
				GCOLOR.T().SELECTED.bind();
			else
				GCOLOR.T().CLICKABLE.bind();
			s.render(r, body);
			COLOR.unbind();

		}
	}
	
}
