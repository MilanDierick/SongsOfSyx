package init.disease;

import init.biomes.CLIMATE;
import init.biomes.CLIMATES;
import init.paths.PATH;
import init.paths.PATHS;
import snake2d.util.file.Json;
import snake2d.util.rnd.RND;
import snake2d.util.sets.*;

public class DISEASES {

	private final LIST<DISEASE> all;
	private final double[] climate = new double[CLIMATES.ALL().size()];
	private static DISEASES s;
	private final double occAll;
	
	public DISEASES() {
		s = this;
		PATH pd = PATHS.INIT().getFolder("disease");
		PATH ps = PATHS.TEXT().getFolder("disease");
		
		{
			Json c = new Json(pd.get("_CONFIG"));
			CLIMATES.MAP().fill(climate, c, 0, 1000);
		}
		
		LinkedList<DISEASE> all = new LinkedList<>();
		
		for (String k : pd.getFiles(1, 120)) {
			new DISEASE(all, k, new Json(pd.get(k)), new Json(ps.get(k)));
		}
		
		this.all = new ArrayList<>(all);
		
		double d = 0;
		for (DISEASE di : all) {
			d += di.occurence;
		}
		
		occAll = d;
		
	}
	
	public static double climate(CLIMATE c) {
		return s.climate[c.index()];
	}
	
	public static LIST<DISEASE> all(){
		return s.all;
	}
	
	public static DISEASE random() {
		double lim = RND.rFloat()*s.occAll;
		double d = 0;
		for (int i = 0; i < s.all.size(); i++) {
			DISEASE dd = s.all.get(i);
			d += dd.occurence;
			if (d >= lim)
				return dd;
		}
		return s.all.get(s.all.size()-1);
	}
	
}
