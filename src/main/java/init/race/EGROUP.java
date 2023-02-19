package init.race;



import java.util.Arrays;

import settlement.entity.humanoid.HTYPE;
import settlement.stats.Induvidual;
import snake2d.util.sets.*;

public final class EGROUP implements INDEXED{

	private static LIST<EGROUP> ALL;
	private static final int[] toIndex = new int[HTYPE.ALL().size()];
	
	
	static void init(){
		ArrayList<HTYPE> l = new ArrayList<HTYPE>(20);
		for (HTYPE t : HTYPE.ALL())
			if (t.works)
				l.add(t);
		ArrayList<EGROUP> all = new ArrayList<EGROUP>(l.size()*RACES.all().size());
		Arrays.fill(toIndex, -1);
		for (int ci = 0; ci < l.size(); ci++) {
			toIndex[l.get(ci).index()] = ci;
			for (Race r : RACES.all()) {
				all.add(new EGROUP(ci*RACES.all().size()+r.index, l.get(ci), r));
			}
		}
		ALL = all;
	}
	
	public final HTYPE t;
	public final Race r;
	private final int index;
	
	public static LIST<EGROUP> ALL(){
		return ALL;
	}
	
	public static EGROUP get(HTYPE c, Race r) {
		int ci = toIndex[c.index()];
		if (ci < 0)
			throw new RuntimeException("" + c.name);
		return ALL.get(ci*RACES.all().size()+r.index);
	}
	
	public static EGROUP get(Induvidual i) {
		return get(i.hType(), i.race());
	}
	
	private EGROUP(int index, HTYPE t, Race r) {
		this.t = t;
		this.r = r;
		this.index = index;
	}
		
	@Override
	public int index() {
		return index;
	}
	
	@Override
	public String toString() {
		return r.info.name + " " + t.name;
	}
	
}
