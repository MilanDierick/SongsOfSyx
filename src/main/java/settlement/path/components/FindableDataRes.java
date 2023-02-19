package settlement.path.components;

import static settlement.main.SETT.*;

import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.path.components.SCompFinder.SCompPatherFinder;
import snake2d.util.datatypes.DIR;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LinkedList;
import util.data.LONG_O.LONG_OE;

public final class FindableDataRes {

	static final LinkedList<FindableDataRes> all = new LinkedList<>();
	
	private final ArrayList<FindableData> datas;
	private final LONG_OE<SComponent> mask;
	public final CharSequence title;

	FindableDataRes(CharSequence title) {
		this.title = title;
		datas = new ArrayList<>(RESOURCES.ALL().size());
		for (int i = 0; i < RESOURCES.ALL().size(); i++) {
			datas.add(new Res(RESOURCES.ALL().get(i)));
		}
		mask = FindableData.datao.new DataLong();
		all.add(this);
	}

	void add(SComponent c, RESOURCE res) {
		datas.get(res.index()).add(c);
	}

	boolean remove(SComponent c, RESOURCE res) {
		return datas.get(res.index()).remove(c);
	}

	public int get(SComponent c, RESOURCE res) {
		return datas.get(res.index()).get(c);
	}
	
	public boolean overflow(SComponent c, RESOURCE res) {
		return datas.get(res.index()).overflow(c);
	}
	
	public int get(SComponent c, int res) {
		return datas.get(res).get(c);
	}

	public long bits(SComponent c) {
		return mask.get(c);
	}
	
	public long bits(int sx, int sy) {
		SComponent s = PATH().comps.zero.get(sx, sy);
		if (s == null)
			return 0;
		while(s.superComp() != null)
			s = s.superComp();
		return mask.get(s);
	}

	public boolean has(SComponent c, long mask) {
		return (bits(c) & mask) != 0;
	}
	
	public final void reportPresence(int tx, int ty, RESOURCE res) {
		datas.get(res.index()).reportPresence(tx, ty);
		
	}
	
	public final void reportAbsence(int tx, int ty, RESOURCE res) {
		datas.get(res.index()).reportAbsence(tx, ty);
	}
	
	private final DIR[] dirs = new DIR[] {
		DIR.C,DIR.N,DIR.E,DIR.S,DIR.W
	};
	
	public boolean has(int startX, int startY, long mask) {
		for (DIR d : dirs) {
			SComponent s = PATH().comps.zero.get(startX, startY, d);
			if (s == null)
				continue;
			while(s.superComp() != null)
				s = s.superComp();
			if(has(s, mask))
				return true;
		}
		return false;
	}
	
	public SCompPatherFinder finder(long mask) {
		fetchmask = mask;
		return finder;
	}
	
	private long fetchmask;
	private final SCompPatherFinder finder = new SCompPatherFinder() {

		@Override
		public boolean isInComponent(SComponent c, double distance) {
			return (mask.get(c) & fetchmask) != 0;
		}
		
	};
	
	private final class Res extends FindableData {
		
		private final long bit;
		
		Res(RESOURCE res) {
			super(res.name);
			this.bit = res.bit;
		}
		
		@Override
		void add(SComponent c) {
			super.add(c);
			long m = mask.get(c);
			m |= bit;
			mask.set(c, m);
		}
		
		@Override
		boolean remove(SComponent c) {
			boolean ret = super.remove(c);
			if (get(c) == 0) {
				long m = mask.get(c);
				m &= ~bit;
				mask.set(c, m);
			}
			return ret;
		}
	}

}