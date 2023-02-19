package view.interrupter;

import init.C;
import init.sprite.UI.UI;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.STRING_RECIEVER;
import snake2d.util.sprite.text.Font;
import snake2d.util.sprite.text.StringInputSprite;
import util.dic.DicMisc;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import view.keyboard.KEYS;

public class ITextInput extends Interrupter{

	private final StringInputSprite input = new StringInputSprite(300, UI.FONT().M);
	
	private final HOVERABLE.Sprite title;
	private final HOVERABLE.Sprite textSprite;
	private final Font font;
	private GuiSection s = new GuiSection();
	private final CLICKABLE ok = new GButt.Glow(DicMisc.¤¤OK) {
		@Override
		protected void clickA() {
			hide();
			client.acceptString(input.text());
		};
	};
	private final CLICKABLE cancel = new GButt.Glow(DicMisc.¤¤cancel) {
		@Override
		protected void clickA() {
			hide();
			client.acceptString(null);
		};
	};
	
	private STRING_RECIEVER client;
	private final InterManager m;

	
	public ITextInput(InterManager m){
		this.m = m;
		
		font = UI.FONT().H2; 
		
		title = new HOVERABLE.Sprite(font.getText("Enter Shit"));
		title.body().centerX(0, C.WIDTH());
		title.body().moveY1(C.HEIGHT()/2 - C.SCALE*20);
		
		textSprite = new HOVERABLE.Sprite(input).setAlign(DIR.C);
	}
	
	@Override
	protected boolean render(Renderer r, float ds) {
		
		ColorImp.BLACK.render(r, 0, C.WIDTH(), 0, C.HEIGHT());
		input.listen();
		
		
//		title.render(r, ds);
//
//		textSprite.setSprite(input);
//		textSprite.body().centerX(C.DIM());
//		textSprite.body().moveY1(title.body().y2()+10);
//		
//		textSprite.render(r, ds);
		s.render(r, ds);
		
		return false;
		
		
	}
	
	private void setTitle(CharSequence title){
		this.title.replaceSprite(font.getText(title), DIR.NW);
		this.title.body().centerX(0, C.WIDTH());
	}
	
	
	public void requestInput(STRING_RECIEVER client, CharSequence title){
		input.text().clear();
		input.listen();
		setTitle(title);
		this.client = client;
		
		s.clear();
		s.add(this.title);
		s.addDownC(16, textSprite);
		s.addDownC(32, ok);
		s.addDownC(8, cancel);
		s.body().centerIn(C.DIM());
		
		super.show(m);
		
	}
	
	public void requestInput(STRING_RECIEVER client, CharSequence title, CharSequence placeholder){
		requestInput(client, title);
		input.text().add(placeholder);
		input.marker = placeholder.length();
		
	}
	
	@Override
	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
		s.hover(mCoo);
		return true;
		
	}

	@Override
	protected void hoverTimer(GBox text) {
		s.hoverInfoGet(text);
	}
	@Override
	protected void mouseClick(MButt button) {
		s.click();
	}

	@Override
	protected boolean update(float ds) {
		
		if (KEYS.MAIN().ESCAPE.consumeClick()){
			client.acceptString(null);
			input.listen();
			hide();
		}else if (KEYS.MAIN().ENTER.consumeClick()) {
			hide();
			client.acceptString(input.text());
		}
		KEYS.clear();
		
		return false;
	}
	
}
