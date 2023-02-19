package init.sound;

import init.paths.PATH;
import snake2d.*;

public class Util {

	private Util() {
		
	}
	
	static SoundEffect[] sounds(PATH path, String prefix){
		
		prefix += "_";
		String[] files = path.getFiles();
		int size = 0;
		for (String s : files) {
			if (s.startsWith(prefix)) {
				size++;
			}
		}
		SoundEffect[] sounds = new SoundEffect[size];
		
		for (String s: files) {
			
			if (!s.startsWith(prefix))
				continue;
			
			String abs = ""+path.get(s);
			
			try {
				String[] ss = s.split("_|\\.");
				if (ss.length < 2)
					throw new NumberFormatException();
				int nr = Integer.valueOf(ss[1]);
				if (sounds[nr] != null)
					throw new Errors.DataError(
							"Duplicate sound file: " + s + ", Remove one/several of them until you only have one with this number.", 
							path.get(s));
				sounds[nr] = CORE.getSoundCore().getEffect(path.get(s));
			}catch(NumberFormatException e) {
				throw new Errors.DataError("Crappy filename! sound files should be named accordingly: " + prefix + "NNN_desc.wav, where NNN is a number 000-999 and desc is something optional/arbitrary. Remanme the file, or delete it.", abs);
			}catch(ArrayIndexOutOfBoundsException e) {
				e.printStackTrace();
				throw new Errors.DataError("The series of sounds is incompleate. You need to provide files in an unbroken succession, eg. 000, 001, 002, 003... You can' skip a number", abs);
			}
			
		}
		
		if (sounds.length == 0) {
			throw new Errors.DataError("Sound file is missing!", path.get(prefix + "_000"));
		}
		
		return sounds;
		
	}
	
}
