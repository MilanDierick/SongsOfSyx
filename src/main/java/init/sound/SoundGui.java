package init.sound;

import init.paths.PATHS;
import snake2d.CORE;
import snake2d.SoundEffect;
import util.gui.misc.GButt;

public final class SoundGui {

	public final SoundEffect click = get("Click");
	public final SoundEffect hover = get("Hover");
	
	SoundGui(){
		
		GButt.defaultHoverSound = hover; 
		GButt.defaultClickSound = click; 
		
	}
	
	private SoundEffect get(String name) {
		return CORE.getSoundCore().getEffect(PATHS.SOUND().gui.get(name));
	}
	
}
