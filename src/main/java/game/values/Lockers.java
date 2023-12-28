package game.values;

import game.GAME;
import snake2d.LOG;
import snake2d.util.file.Json;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import util.data.DOUBLE_O;
import util.dic.DicMisc;
import util.gui.misc.GBox;

public class Lockers{

	private final ArrayListGrower<Lock<?>> locks = new ArrayListGrower<>();
	public final CharSequence name;
	public final SPRITE icon;
	
	public Lockers(CharSequence name, SPRITE icon) {
		this.name = name;
		this.icon = icon;
	}
	

	public <T> void add(GVALUES<T> vv, Json json, DOUBLE_O<T> v) {
		add(vv, "UNLOCKS_" + vv.key, json, v);
	}
	
	public <T> void add(GVALUES<T> vv, String key, Json json, DOUBLE_O<T> v) {
		
		if (!json.has(key))
			return;
		
		for (String s : json.values(key)) {
			
			Locker<T> locker = new Locker<T>(name, icon) {

				@Override
				public boolean inUnlocked(T t) {
					return v.getD(t) >= 1;
				}
				
				@Override
				public void hover(GUI_BOX text, T t) {
					Lockers.this.hover(text, v.getD(t));
				}
				
				@Override
				public double progress(T t) {
					return v.getD(t);
				}
				
			};
			
			
			LPromise<T> p = new LPromise<T>();
			p.key = s;
			p.path = json.path() + " line:" + json.line(key);
			p.locker = locker;
			p.vv = vv;
			vv.LOCK.inits.add(p);
		}
		
	}
	
	protected void hover(GUI_BOX text, double value) {
		GBox b = (GBox) text;
		if (value == 1) {
			b.add(b.text().normalify2().add(name));
		}else {
			b.add(b.text().warnify().add(name));
		}
		b.NL();
	}
	
	public void hover(GUI_BOX text) {
		if (all().size() > 0) {
			GBox b = (GBox) text;
			b.textLL(DicMisc.¤¤Unlocks);
			b.NL();
			for (Lock<?> l : all()) {
				b.add(l.lockable.icon);
				b.text(l.lockable.name);
				b.NL();
			}
			
		}
	}
	
	public LIST<Lock<?>> all(){
		return locks;
	}
	
	private class LPromise<T> implements ACTION{
	
		String key;
		String path;
		Locker<T> locker;
		GVALUES<T> vv;
		
		@Override
		public void exe(){
			Lockable<T> lockable = vv.LOCK.get(key);
			if (lockable == null) {
				if (!vv.LOCK.hasSpewed)
					GAME.Warn(path + System.lineSeparator() + "no UNLOCKABLE " + vv.key + " named : " + key  + " available: " + System.lineSeparator() + vv.LOCK.available());
				else {
					LOG.ln(path + System.lineSeparator() + "no UNLOCKABLE " + vv.key + " named : " + key);
				}
				vv.LOCK.hasSpewed = true;
				return;
			}
			Lock<T> lock = new Lock<>(lockable, locker);
			lockable.res.add(lock);
			Lockers.this.locks.add(lock);

		}
		
	}
	
}
