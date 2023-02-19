package menu;

import init.C;
import init.paths.PATHS;
import snake2d.*;
import util.gui.misc.GButt;

class RSound {

	public final SoundEffect hoverSound;
	public final SoundEffect clickSound;
	
	public boolean playing;
	public final  SoundStream music;
	public final  SoundStream s;
	
	public final SoundStream logo;
	
	RSound(){
		
		//expandSound = getEffect(paths.sound.MharshSwish_00_01);
		hoverSound = CORE.getSoundCore().getEffect(PATHS.SOUND().gui.get("Hover"));
		clickSound = CORE.getSoundCore().getEffect(PATHS.SOUND().gui.get("Click"));
		
		music = CORE.getSoundCore().getStream(PATHS.SOUND().music.getFolder("misc").get("_Menu"), true);
		
		s = CORE.getSoundCore().getStream(PATHS.SOUND().music.getFolder("misc").get("_Torch"), false);
		
		logo = CORE.getSoundCore().getStream(PATHS.SOUND().music.getFolder("misc").get("_Logo"), false);
		
		GButt.defaultHoverSound = hoverSound; 
		GButt.defaultClickSound = clickSound;
		
		CORE.getSoundCore().set(C.WIDTH()/2, C.HEIGHT()/2);
		
	}
	
	void play() {
		if (playing)
			return;
		logo.stop();
		music.setLooping(true);
		music.play();
		s.setLooping(true);
		s.play();
	}
	
}
