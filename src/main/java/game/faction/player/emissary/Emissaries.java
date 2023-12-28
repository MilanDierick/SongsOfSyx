package game.faction.player.emissary;

import java.io.IOException;

import game.faction.npc.ruler.Royalty;
import game.time.TIME;
import init.D;
import settlement.entity.ENTETIES;
import settlement.main.SETT;
import snake2d.util.file.*;
import snake2d.util.sets.*;
import util.updating.IUpdater;
import view.ui.message.MessageText;
import world.regions.Region;

public class Emissaries {

	public static CharSequence ¤¤name = "Emissary";
	public static CharSequence ¤¤names = "Emissaries";
	public static CharSequence ¤¤desc = "Emissaries are used to influence foreign courts, or to increase support in regions outside of your realm. Emissaries are trained in an embassy, and can be assigned in the faction panel or the regional panel.";
	
	public static CharSequence ¤¤low = "We do no longer employ as many emissaries as are needed. As a result, their assignations will suffer a penalty. We should cancel a few missions so that there is no shortage.";
	private int mDay = -60;
	static {
		D.ts(Emissaries.class);
	}
	
	private final ArrayListResize<EMission> all = new ArrayListResize<>(256, ENTETIES.MAX);
	private final Stack<EMission> free = new Stack<>(64);
	private final IUpdater uper = new IUpdater(ENTETIES.MAX, 120) {
		
		@Override
		protected void update(int i, double timeSinceLast) {
			if (i < all.size()) {
				EMission e = all.get(i);
				double d = (emissaries()+1.0)/(missions()+1.0);
				if (d < 1) {
					d*= d;
				}else {
					d = 1;
				}
				if (!e.mission().update(e, timeSinceLast*d)) {
					all.remove(i);
					if (!free.isFull())
						free.push(e);
				}
				if (d < 1 && Math.abs(TIME.days().bitsSinceStart()-mDay) > 10) {
					new MessageText(¤¤names).paragraph(¤¤low).send();
					mDay = TIME.days().bitsSinceStart();
				}
			}		
		}
		
	};
	
	void remove(EMission m) {
		all.remove(m);
		if (!free.isFull())
			free.push(m);
	}
	
	public Emissaries() {
		
	}
	
	public LIST<EMission> all(){
		return all;
	}
	
	public void assign(EMissionType m, Region reg, Royalty roy, int am) {

		if (am < 0) {
			for (EMission a : all) {
				if (a.mission() != null && a.mission().targetIs(a, reg, roy)) {
					remove(a);
					am++;
					if (am == 0)
						return;
				}
			}
		}else {
			while(am > 0) {
				if (free.isEmpty())
					free.push(new EMission());
				EMission a = free.pop();
				a.assign(reg, roy, m);
				all.add(a);
				am--;
			}
		}
		
	}
	
	public int active(EMissionType e, Region reg, Royalty roy) {
		int am = 0;
		for (EMission a : all) {
			if (a.mission() == e && e.targetIs(a, reg, roy))
				am ++;
		}
		return am;
	}
	
	public int emissaries() {
		return SETT.ROOMS().EMBASSY.employment().employed();
	}
	
	public int available() {
		return emissaries()-missions();
	}
	
	public int missions() {
		return all.size();
	}
	
	public void update(double ds) {
		uper.update(ds);
	}
	
	public final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			file.i(all.size());
			for(EMission m : all) {
				file.d(m.dataD);
				file.i(m.data1);
				file.i(m.data2);
				file.b(m.type);
			}
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			all.clear();
			int am = file.i();
			for (int i = 0; i < am; i++) {
				EMission m = new EMission();
				m.dataD = file.d();
				m.data1 = file.i();
				m.data2 = file.i();
				m.type = file.b();
				all.add(m);
			}			
		}
		
		@Override
		public void clear() {
			all.clear();
		}
	};
}
