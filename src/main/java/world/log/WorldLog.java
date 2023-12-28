package world.log;

import java.io.IOException;

import game.faction.Faction;
import game.time.TIME;
import init.sprite.UI.Icons.S.IconS;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import world.WORLD.WorldResource;

public final class WorldLog extends WorldResource{

	public final int MAX = 256;
	private final ArrayList<LogEntry> all = new ArrayList<>(MAX);

	
	
	private LogEntry next() {
		if (!all.hasRoom()) {
			LogEntry e = all.get(0);
			all.shiftLeft();
			return e;
		}
		return new LogEntry();
	}
	
	public void log(Faction a, Faction b, IconS icon, CharSequence message, int tx, int ty) {
		
		int day = TIME.days().bitsSinceStart();
		short fa = (short) (a == null ? -1 : a.index());
		short fb = (short) (b == null ? -1 : b.index());
		short ii = (short) (icon == null ? -1 : icon.index);
		for (int i = all.size()-1; i >= 0; i--) {
			LogEntry o = all.get(i);
			if (o.day != day)
				break;
			if (o.ii == ii && o.fa == fa && o.fb == fb && o.message.equals(message))
				return;
		}
		
		LogEntry e = next();
		e.ii = ii;
		e.day = day;
		e.fa = fa;
		e.fb = fb;
		e.tx = (short) tx;
		e.ty = (short) ty;
		e.message.clear().add(message);
		all.add(e);
	}
	
	public LIST<LogEntry> all(){
		return all;
	}

	

	@Override
	protected void save(FilePutter file) {
		file.i(all.size());
		for (LogEntry e : all) {
			e.save(file);
		}
		
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		int am = file.i();
		all.clear();
		for (int i = 0; i < am; i++) {
			all.add(new LogEntry(file));
		}
		
	}
	
}
