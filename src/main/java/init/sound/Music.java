package init.sound;

import game.time.TIME;
import init.paths.PATH;
import init.paths.PATHS;
import settlement.main.SETT;
import snake2d.*;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import view.interrupter.IDebugPanel;

public final class Music {

	private SoundStream current;
	private SoundStream[] currentA;
	private SoundStream[] nextA;
	
	private final SoundStream[] normal;
	private final SoundStream[] battle;
	private int r = 0;
	
	private double fade = 0;
	private boolean shuffle = false;
	
	Music() {
		
		normal = get("normal");
		battle = get("action");
		
		currentA = normal;
		nextA = normal;
		current = currentA[RND.rInt(currentA.length)];
		current.setGain(1f);
		fade = 1f;
		current.play();
		
		IDebugPanel.add("music shuffle", new ACTION() {
			@Override
			public void exe() {
				shuffle = true;
			}
		});
		
	}
	
	void update(float ds) {
		
		if (SETT.ARMIES().enemy().men() > 0) {
			if (shuffle || currentA != battle) {
				if (fade < 0) {
					current.stop();
					currentA = battle;
					shuffle = false;
					fade = 1;
				}else {
					current.setGain(fade);
				}
				fade -= ds;
			}else if(!current.isPlaying()){
				fade = CLAMP.d(fade+ds, 0, 1);
				r++;
				r %= currentA.length;
				current = currentA[r];
				current.setGain(fade);
				current.play();
			}
		}else if(TIME.light().dayIs()) {
			if (shuffle || currentA != normal) {
				if (fade < 0) {
					current.stop();
					currentA = normal;
					shuffle = false;
					fade = 1;
				}else {
					current.setGain(fade);
				}
				fade -= ds;
			}else if(!current.isPlaying()){
				fade = CLAMP.d(fade+ds, 0, 1);
				r++;
				r %= currentA.length;
				current = currentA[r];
				current.setGain(fade);
				current.play();
			}
		}else {
			if (currentA == battle) {
				if (fade < 0) {
					current.stop();
				}
				fade -= ds;
			}
		}
		
		
		
	}
	
	private SoundStream[] get(String folder) {
		PATH path = PATHS.SOUND().music.getFolder(folder);
		
		String[] p = path.getFiles();
		if (p == null)
			throw new Errors.DataError("no music exists! Supply some", PATHS.SOUND().music.getFolder(folder).get());
		SoundStream[] res = new SoundStream[p.length];
		int i = 0;
		for (String s : p) {
			res[i++] = CORE.getSoundCore().getStream(path.get(s), true);
		}
		for (int k = 0; k < res.length*4; k++) {
			int i1 = RND.rInt(res.length);
			int i2 = RND.rInt(res.length);
			SoundStream s = res[i1];
			res[i1] = res[i2];
			res[i2] = s;
		}
		
		return res;
		
	}
	
	private void set(SoundStream[] next) {
		if (nextA == next)
			return;
		nextA = next;
	}
	
	public void next() {
		currentA = null;
	}
	
	public void setNormal() {
		set(normal);
	}
	
	public void setBattle() {
		
	}
	
	
}
