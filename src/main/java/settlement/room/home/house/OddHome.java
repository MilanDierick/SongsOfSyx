package settlement.room.home.house;

import java.io.IOException;

import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.home.HOME;
import settlement.room.home.HOMET;
import settlement.room.home.HomeSettings.HomeSetting;
import snake2d.util.file.*;
import snake2d.util.sets.QueueInteger;

public final class OddHome {

	private final QueueInteger[] queues = new QueueInteger[HOMET.ALL().size()];
	
	OddHome(){
		for (int y = 0; y < queues.length; y++) {
			queues[y] = new QueueInteger(256);
		}
	}
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			for (QueueInteger q : queues)
				q.save(file);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			for (QueueInteger q : queues) {
				q.load(file);
			}
		}
		
		@Override
		public void clear() {
			for (QueueInteger q : queues)
				q.clear();
		}
	};
	
	void update(int tx, int ty) {
		
		HOME h = test(tx, ty, this);
		if (h == null)
			return;
		HomeSetting s = h.availability();
		h.done();
		for (int ti = 0; ti < HOMET.ALL().size(); ti++) {
			if (s.is(ti)) {
				HOMET t = HOMET.ALL().get(ti);
				QueueInteger i = queues[t.index()];
				if (!i.hasRoom())
					i.poll();
				i.push(tx+ty*SETT.TWIDTH);
			}
		}
	}

	
	public HomeHouse get(Humanoid h, Object user) {
		HOMET t = HOMET.get(h);
		while(queues[ t.index()].hasNext()) {
			int i = queues[t.index()].peek();
			int tx = i%SETT.TWIDTH;
			int ty = i/SETT.TWIDTH;
			
			HomeHouse ho = test(tx, ty, user);
			if (ho != null && ho.availability() != null && ho.availability().is(h)) {
				return ho;
			}else {
				queues[t.index()].poll();
			}
			if (ho != null)
				ho.done();
		}
		return null;
	}
	
	public boolean has(Humanoid h) {
		HOMET t = HOMET.get(h);
		while(queues[t.index()].hasNext()) {
			int i = queues[t.index()].peek();
			int tx = i%SETT.TWIDTH;
			int ty = i/SETT.TWIDTH;
			
			HomeHouse ho = test(tx, ty, this);
			if (ho != null) {
				ho.done();
				return true;
			}
			queues[t.index()].poll();
		}
		return false;
	}
	
	private HomeHouse test(int tx, int ty, Object user) {
		
		if (!SETT.PATH().connectivity.is(tx, ty))
			return null;
		
		HomeHouse h = SETT.ROOMS().HOMES.HOME.house(tx, ty, this);
		if (h == null)
			return null;
		
		if (!h.service().isSameAs(tx, ty)) {
			h.done();
			return null;
		}
			
		if (h.occupants() >= h.occupantsMax()) {
			h.done();
			return null;
		}
		return h;
	}
	
}
