package view.menu;

import game.VERSION;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.clickable.Scrollable.ScrollRow;
import snake2d.util.sprite.text.Font;
import util.colors.GCOLOR;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.save.SaveFile;

abstract class Savebutt extends CLICKABLE.ClickableAbs implements ScrollRow {

	private static GText version = new GText(UI.FONT().M, 16);
	int index = -1;
	
	public Savebutt() {
		body.setWidth(1000);
		body.setHeight(28);
	}
	
	@Override
	public void init(int index) {
		this.index = index;
	}
	
	private Font font() {
		return UI.FONT().H2;
	}
	
	@Override
	protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
		
		SaveFile s = save(index);
		
		if (s == null)
			return;
		
		version.clear();
		version.add(VERSION.versionMajor(s.version));
		version.add('.');
		version.add(VERSION.versionMinor(s.version));
		if (VERSION.VERSION_MAJOR != VERSION.versionMajor(s.version)) {
			COLOR.RED100.bind();
		}else if (s.problem() != null) {
			COLOR.YELLOW100.bind();
		}else {
			if (selected(index)) {
				GCOLOR.T().SELECTED.bind();
			}else if (isHovered){
				GCOLOR.T().HOVERED.bind();
			}
		}
		font().render(r, version, body().x1(), body().y1());
		
		if (selected(index)) {
			GCOLOR.T().SELECTED.bind();
		}else if (isHovered){
			GCOLOR.T().HOVERED.bind();
		}else {
			GCOLOR.T().CLICKABLE.bind();
		}
		
		font().render(r, s.name, body().x1() + 60, body().y1());
		
		
		version.clear().add('p').s();
		GFORMAT.i(version, s.pop);
		font().render(r, version, body().x1() + 740, body().y1());
		
		font().render(r, s.ago, body().x1() + 820, body().y1());
		COLOR.unbind();
	}

	@Override
	public void hoverInfoGet(GUI_BOX text) {
		SaveFile s = save(index);
		if (s != null) {
			CharSequence p = s.problem();
			if (p != null)
				((GBox)text).error(p);
		}		
	}
	
	protected abstract boolean selected(int index);
	protected abstract SaveFile save(int index);
	
}