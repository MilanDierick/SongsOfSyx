package view.ui.wiki;

import init.C;
import init.sprite.SPRITES;
import snake2d.util.color.COLOR;
import snake2d.util.gui.GuiSection;
import snake2d.util.sets.LIST;
import util.gui.misc.GButt;
import util.gui.misc.GHeader;
import view.main.VIEW;
import view.ui.manage.IFullView;

abstract class Article {
	
	public final String key;
	public final CharSequence title;
	public final CharSequence category;
	public final GuiSection section = new GuiSection() {
		@Override
		public void render(snake2d.SPRITE_RENDERER r, float ds) {
			Colors.border.render(r, body(), -5);
			COLOR.WHITE05.render(r, body(), -6);
			super.render(r, ds);
		};
	};
	public static final int HEIGHT = C.HEIGHT()-IFullView.TOP_HEIGHT*2-12;
	
	Article(CharSequence title, CharSequence category){
		this.title = title;
		this.category = category;
		this.key = ""+category + title;
	}
	
	final void init(LIST<Article> all, int width) {
		
		section.body().setWidth(width);
		section.body().setHeight(C.HEIGHT()-IFullView.TOP_HEIGHT);
		GHeader h = new GHeader(title);
		h.body().centerX(section.body());
		h.body().centerY(0, IFullView.TOP_HEIGHT);
		section.add(h);
		GButt.Glow b = new GButt.Glow(SPRITES.icons().m.exit) {
			@Override
			protected void clickA() {
				VIEW.UI().wiki.remove(Article.this);
			};
		};
		b.body.centerY(0, IFullView.TOP_HEIGHT);
		b.body.moveX2(width-8);
		section.add(b);
		GuiSection s = makeSection(all, width-48);
		s.body().moveY1(IFullView.TOP_HEIGHT+6);
		s.body().moveX1(24);
		section.add(s);
	}
	
	abstract GuiSection makeSection(LIST<Article> all, int width);
	
}
