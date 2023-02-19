package init.race;

import snake2d.util.file.Json;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.KeyMap;
import util.data.DOUBLE.DOUBLE_MUTABLE;

public class RaceProp {
	
	public final Prop skinnyDips;
	public final Prop usesBench;
	
	RaceProp(Json json){

		final KeyMap<Prop> map = new KeyMap<>();

		skinnyDips = new Prop("SKINNY_DIPS", map);
		usesBench = new Prop("USES_BENCH", map);
		
		json = json.json("BEHAVIOUR");
		for (String k : json.keys()) {
			if (!map.containsKey(k)) {
				String a = " ";
				for (String s : map.keysSorted())
					a += s + ", ";
				json.error("No behaviour named; " + k + " Available: " + a, k);
			}
			Prop p = map.get(k);
			p.setD(json.d(k, 0, 1));
		}
		
	}
	
	public static class Prop implements DOUBLE_MUTABLE {

		private double d = 1;
		
		Prop(String key, KeyMap<Prop> m){
			m.put(key, this);
		}
		
		@Override
		public double getD() {
			return d;
		}

		@Override
		public DOUBLE_MUTABLE setD(double d) {
			d = CLAMP.d(d, 0, 1);
			return this;
		}
		
	}
	
}
