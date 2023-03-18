package snake2d.util.sprite.text;

import snake2d.*;
import snake2d.Input.CHAR_LISTENER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TextureCoords;

public class StringInputSprite extends CHAR_LISTENER implements SPRITE{

	private Font f;
	private static final String promt = "|";
	private CharSequence placeholder;
	public int marker = 0;
	private static final Str tmp = new Str(512);
	
	public StringInputSprite(int size, Font font) {
		super(size);
		this.f = font;
	}
	
	public StringInputSprite placeHolder(CharSequence ph) {
		this.placeholder = ph;
		return this;
	}

	public StringInputSprite font(Font f) {
		this.f = f;
		return this;
	}
	
	@Override
	protected void acceptChar(char c) {

		if (text().spaceLeft() > 0 && listening()) {
			
			if (marker == text().length()) {
				text().add(c);
			}else {
				tmp.clear().add(text());
				text().clear();
				int k = 0;
				for (int i = 0; i < tmp.length(); i++) {
					if (k++ == marker) {
						text().add(c);
						i--;
					}else {
						text().add(tmp.charAt(i));
					}
				}
			}
			marker++;
			
			change();
		}
	}

	@Override
	protected void backspace() {
		
		if (marker > text().length())
			marker = text().length();
		
		if (marker > 0 && text().length() > 0 && listening()) {
			
			tmp.clear().add(text());
			text().clear();
			for (int i = 0; i < tmp.length(); i++) {
				if (i+1 == marker) {
					;
				}else {
					text().add(tmp.charAt(i));
				}
			}
			marker--;
			
		}
		change();
	}
	
	@Override
	public void left() {
		marker --;
		if (marker < 0)
			marker = 0;
	}

	@Override
	public void right() {
		marker ++;
		if (marker > text().length())
			marker = text().length();
	}
	

	
	@Override
	public int width() {
		if (text().length() == 0) {
			if (placeholder != null && !listening())
				return f.getDim(placeholder).x();
			else
				return 0;
		}
		return f.getDim(text()).x();
	}
	
	@Override
	protected void enter() {
		Mouse.currentClicked = null;
	}

	@Override
	public int height() {
		return f.height();
	}

	@Override
	public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
		if (marker > text().length())
			marker = text().length();
		renAction();
		if (text().length() == 0 && !listening()) {
			if (placeholder != null)
				f.render(r, placeholder, X1, Y1, X2-X1, 1);
		}else if (listening()) {
			f.render(CORE.renderer(), text(),X1, Y1,0,marker,  1);
			
			X1 += f.getDim(text(), 0, marker, Integer.MAX_VALUE, 1.0).x();
			COLOR.BLACK2WHITE.bind();
			f.render(r, promt, X1, Y1);
			COLOR.unbind();
			X1 += 8;
			if (marker < text().length()) {
				f.render(CORE.renderer(), text(),X1, Y1,marker,text().length(),  1);
			}
			
		}else {
			f.render(r, text(), X1, Y1, X2-X1, 1);
		}
	}
	
	public void renAction() {
		
	}
	
	@Override
	protected void change() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
		// TODO Auto-generated method stub
		
	}
	
	public InputClickable c(DIR d) {
		return new InputClickable(this, d);
	}
	
	public static class InputClickable extends CLICKABLE.ClickableAbs {

		private final StringInputSprite input;
		private COLOR hoverC = COLOR.WHITE2WHITE;
		private COLOR color = COLOR.WHITE100;
		private COLOR active = color.shade(0.7);
		private DIR rep;
		
		InputClickable(StringInputSprite input, DIR rep){
			this.input = input;
			while (input.text().spaceLeft() > 0) {
				input.text().add('n');
			}
			int w = input.width();
			body.setWidth(w);
			body.setHeight(input.height());
			input.text().clear();
			this.rep = rep;
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			
			input.renAction();
			if (Mouse.currentClicked == this)
				input.listen();
			
			int dx = (body().width()-input.width())/2;
			
			int x1 = body().x1() + (rep.x()+1)*dx;
			if (!isActive)
				active.bind();
			if (isHovered || Mouse.currentClicked == this)
				hoverC.bind();
			else
				color.bind();
			input.render(r, x1, body.y1());
			COLOR.unbind();
		}
		
		@Override
		public boolean click() {
			if (super.click()) {
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
		
		public InputClickable colors(COLOR normal, COLOR hover) {
			this.hoverC = hover;
			this.color = normal;
			this.active = color.shade(0.7);
			return this;
		}
		
	}


}
