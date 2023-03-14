package init.disease;

import init.biomes.CLIMATE;
import init.paths.PATH;
import init.paths.PATHS;
import snake2d.util.file.Json;
import snake2d.util.rnd.RND;
import snake2d.util.sets.*;

public class DISEASES {

	private final LIST<DISEASE> all;
	private static DISEASES s;
	
	public static double DISEASE_PER_YEAR;
	public static double EPIDEMIC_CHANCE;
	public static int SQUALOR_POPULATION_MIN;
	public static int SQUALOR_POPULATION_DELTA;
	
	public DISEASES() {
		s = this;
		PATH pd = PATHS.INIT().getFolder("disease");
		PATH ps = PATHS.TEXT().getFolder("disease");
		
		{
			Json c = new Json(pd.get("_CONFIG"));
			DISEASE_PER_YEAR = c.d("DISEASE_PER_YEAR", 0, 10);
			EPIDEMIC_CHANCE = c.d("EPIDEMIC_CHANCE", 0, 10);
			SQUALOR_POPULATION_MIN = c.i("SQUALOR_POPULATION_MIN", 0, 8000);
			SQUALOR_POPULATION_DELTA = c.i("SQUALOR_POPULATION_DELTA", 0, 8000);
		}
		
		LinkedList<DISEASE> all = new LinkedList<>();
		
		for (String k : pd.getFiles(1, 120)) {
			new DISEASE(all, k, new Json(pd.get(k)), new Json(ps.get(k)));
		}
		
		this.all = new ArrayList<>(all);
		
		
		
	}
	
	public static LIST<DISEASE> all(){
		return s.all;
	}
	
	public static DISEASE randomEpidemic(CLIMATE c) {
		double lim = 0;
		for (int i = 0; i < s.all.size(); i++) {
			DISEASE dd = s.all.get(i);
			if (!dd.epidemic)
				continue;
			lim += dd.occurence(c);
		}
		lim *= RND.rFloat();
		double d = 0;
		for (int i = 0; i < s.all.size(); i++) {
			DISEASE dd = s.all.get(i);
			if (!dd.epidemic)
				continue;
			d += dd.occurence(c);
			if (d >= lim)
				return dd;
		}
		return s.all.get(s.all.size()-1);
	}
	
	public static DISEASE randomRegular(CLIMATE c) {
		double lim = 0;
		for (int i = 0; i < s.all.size(); i++) {
			DISEASE dd = s.all.get(i);
			if (!dd.regular)
				continue;
			lim += dd.occurence(c);
		}
		lim *= RND.rFloat();
		double d = 0;
		for (int i = 0; i < s.all.size(); i++) {
			DISEASE dd = s.all.get(i);
			if (!dd.regular)
				continue;
			d += dd.occurence(c);
			if (d >= lim)
				return dd;
		}
		return s.all.get(s.all.size()-1);
	}
	
}
