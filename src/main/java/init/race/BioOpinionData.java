package init.race;

import settlement.entity.humanoid.HCLASS;
import settlement.stats.*;
import settlement.stats.StatsMultipliers.StatMultiplier;
import snake2d.util.MATH;
import snake2d.util.file.Json;
import snake2d.util.sprite.text.Str;

final class BioOpinionData {

	private final CharSequence[] funnies;
	private final CharSequence[] full;
	private final Opinion[] all = new Opinion[STATS.all().size()];
	private final Str stmp = new Str(256);
	
	private final CharSequence[][] titles;
	
	BioOpinionData(Json json){
		
		titles = new CharSequence[][] {
			json.texts("HAPPY"),
			json.texts("HAPPY_SOSO"),
			json.texts("HAPPY_NO")
		};
		
		Opinion def = new Opinion(null, null);
		for (int i = 0; i < all.length; i++)
			if (all[i] == null)
				all[i] = def;
		for (STAT s : STATS.all()) {
			all[s.index()] = s.info().defOpinion;
		}
		
		
		funnies = json.texts("FUNNY");
		full = json.texts("NOTHING");
		
		new StatsJson(json) {

			@Override
			public void doWithTheJson(StatCollection col, STAT s, Json j, String key) {
				Opinion i = new Opinion(j, key);
				all[s.index()] = i;
			}

			@Override
			public void doWithMultiplier(StatMultiplier m, Json j, String key) {
				// TODO Auto-generated method stub
				
			}
			
		};
		

		
		
	}
	
	public CharSequence get(STAT s, HCLASS cl, Race race, long ran) {

		if (s.standing().definition(race).get(cl).from > s.standing().definition(race).get(cl).to) {
			return less(s, ran);
		}else {
			return more(s, ran);
		}
	}
	
	public CharSequence title(Induvidual h, double value) {
		if (value > 0.95)
			return get(titles[0], h.randomness());
		if (value > 0.8)
			return get(titles[1], h.randomness());
		return get(titles[2], h.randomness());
	}
	
	public CharSequence funny(long ran) {
		return funnies[MATH.mod((int) ran, funnies.length)];
	}
	
	public CharSequence full(long ran) {
		return full[MATH.mod((int) ran, full.length)];
	}
	
	private CharSequence get(CharSequence[] r, long ran) {
		return r[MATH.mod((int) ran, r.length)];
	}
	
	private CharSequence more(STAT stat, long ran) {
		Opinion i = all[stat.index()];
		CharSequence s = i.more[MATH.mod((int)ran, i.more.length)];
		stmp.clear().add(s);
		i.insert(stmp, stat);
		return stmp;
	}
	
	private CharSequence less(STAT stat, long ran) {
		Opinion i = all[stat.index()];
		CharSequence s = i.less[MATH.mod((int)ran, i.less.length)];
		stmp.clear().add(s);
		i.insert(stmp, stat);
		return stmp;
	}
	
}
