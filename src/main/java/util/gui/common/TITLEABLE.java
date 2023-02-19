package util.gui.common;

import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.GBox;
import util.gui.misc.GHeader;
import util.info.INFO;

public interface TITLEABLE extends SPRITE{

	public default HOVERABLE hv(CharSequence name) {
		return new GHeader.HeaderVertical(name, this);
	}
	
	public default HOVERABLE hv(CharSequence name, CharSequence desc) {
		return new GHeader.HeaderVertical(name, this).hoverInfoSet(desc);
	}
	
	public default HOVERABLE hv(INFO info) {
		return new GHeader.HeaderVertical(info.name, this).hoverTitleSet(info.name).hoverInfoSet(info.desc);
	}
	
	public default HOVERABLE hv(SPRITE name) {
		return new GHeader.HeaderVertical(name, this);
	}
	
	public default HOVERABLE hh(CharSequence name) {
		return new GHeader.HeaderHorizontal(name, this);
	}
	
	public default HOVERABLE hh(INFO info) {
		return new GHeader.HeaderHorizontal(info.name, this).hoverInfoSet(info.desc);
	}
	
	public default HOVERABLE hh(SPRITE name) {
		return new GHeader.HeaderHorizontal(name, this);
	}
	
	public default HOVERABLE hh(SPRITE name, int width) {
		return new GHeader.HeaderHorizontal(name, this, width);
	}
	
	public default  GHeader.HeaderHorizontal hh(CharSequence name, int width) {
		
		return new GHeader.HeaderHorizontal(name, this, width);
	}
	
	public default HOVERABLE hh(CharSequence name, CharSequence desc, int width) {
		
		return new GHeader.HeaderHorizontal(name, this, width).hoverInfoSet(desc);
	}
	
	public default HOVERABLE hh(CharSequence name, CharSequence desc) {
		
		return new GHeader.HeaderHorizontal(name, this).hoverInfoSet(desc);
	}
	
	public default void hoverInfoGet(GBox b) {
		
	}
	
}
