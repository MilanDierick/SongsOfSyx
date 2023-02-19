package util.gui.misc;

import init.D;
import init.sprite.SPRITES;
import snake2d.Mouse;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.StringInputSprite;
import util.colors.GCOLOR;
import util.gui.panel.GFrame;
import view.main.VIEW;

public class GInput extends CLICKABLE.ClickableAbs {

	private final StringInputSprite input;
	private static CharSequence ¤¤clear = "¤Clear all text. (Also by clicking 'del' on the keyboard)";
	
	static {
		D.ts(GInput.class);
	}
	
	public GInput(StringInputSprite input){
		this.input = input;
		while (input.text().spaceLeft() > 0) {
			input.text().add('m');
		}
		int w = input.width();
		body.setWidth(w+24);
		body.setHeight(input.height()+8);
		input.text().clear();
	}
	
	@Override
	protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
		
		input.renAction();
		if (Mouse.currentClicked == this)
			input.listen();
		
		if (isHovered || Mouse.currentClicked == this) {
			GCOLOR.UI().NORMAL.hovered.render(r, body());
		}
		
		if (text().length() == 0) {
			COLOR.WHITE65.bind();
		}
		
		int x1 = body().x1()+4;
		int y1 = body().y1() + (body().height()-input.height())/2;
		
		input.render(r, x1, y1);
		
		
		if (isHovered && VIEW.mouse().x() > body().x2()-20) {
			COLOR.WHITE100.bind();
		}else {
			COLOR.WHITE65.bind();
		}
		SPRITES.icons().s.cancel.renderC(r, body().x2()-11, body().cY());
		COLOR.unbind();
		GFrame.render(r, 0, body());
	}
	
	@Override
	public boolean click() {
		if (super.click()) {
			if (VIEW.mouse().x() > body().x2()-20) {
				input.del();
				input.listen();
				return true;
			}
			Mouse.currentClicked = this;
			input.listen();
			input.marker = input.text().length();
			return true;
		}
		return false;
	}
	
	public void focus() {
		Mouse.currentClicked = this;
		input.listen();
	}
	
	public Str text() {
		return input.text();
	}
	
	@Override
	public void hoverInfoGet(GUI_BOX text) {
		if (VIEW.mouse().x() > body().x2()-20) {
			
			text.text(¤¤clear);
		}
	}
	

}
