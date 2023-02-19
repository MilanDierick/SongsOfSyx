package settlement.room.home.house;

import java.io.IOException;

import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.home.HOME;
import settlement.room.home.HOME_TYPE;
import snake2d.util.file.*;
import snake2d.util.sets.QueueInteger;

public final class OddHome {

	private final QueueInteger[] queues = new QueueInteger[HOME_TYPE.ALL().size()];
	private HOME_TYPE[] types = new HOME_TYPE[3];
	
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
		HOME_TYPE t = h.availability();
		h.done();
		QueueInteger i = queues[t.index()];
		if (!i.hasRoom())
			i.poll();
		i.push(tx+ty*SETT.TWIDTH);
	}

	
	public HomeHouse get(Humanoid h, Object user) {
		types[0] = HOME_TYPE.getGeneral(h);
		types[1] = HOME_TYPE.getSpecific(h);
		types[2] = HOME_TYPE.getSpecific2(h);
		
		for (HOME_TYPE t : types) {
			while(queues[t.index()].hasNext()) {
				int i = queues[t.index()].peek();
				int tx = i%SETT.TWIDTH;
				int ty = i/SETT.TWIDTH;
				
				HomeHouse ho = test(tx, ty, user);
				if (ho != null && ho.availability() != null && ho.availability().isValid(h)) {
					return ho;
				}else {
					queues[t.index()].poll();
				}
				if (ho != null)
					ho.done();
			}
		}
		return null;
	}
	
	public boolean has(Humanoid h) {
		types[0] = HOME_TYPE.getGeneral(h);
		types[1] = HOME_TYPE.getSpecific(h);
		types[2] = HOME_TYPE.getSpecific2(h);
		
		for (HOME_TYPE t : types) {
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
