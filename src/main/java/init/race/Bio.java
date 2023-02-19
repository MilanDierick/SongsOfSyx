package init.race;

import init.paths.PATHS;
import settlement.entity.humanoid.HCLASS;
import settlement.entity.humanoid.Humanoid;
import settlement.stats.Induvidual;
import settlement.stats.standing.STANDINGS;
import snake2d.util.file.Json;
import snake2d.util.sets.*;
import snake2d.util.sprite.text.Str;

public final class Bio {

	private static final KeyMap<BioData> cachebio = new KeyMap<>();
	private static final KeyMap<BioOpinionData> cachedemand = new KeyMap<>();

	private final BioData data;
	private final BioOpinion improve;
	private final LIST<Str> tmp = new ArrayList<Str>(new Str(128));
	
	Bio(Json json, Race race) {

		String f = json.value("BIO_FILE");
		if (!cachebio.containsKey(f)) {
			Json j = new Json(PATHS.TEXT().getFolder("race").getFolder("bio").get(f));
			BioData d = new BioData(j);
			cachebio.put(f, d);
		}
		data = cachebio.get(f);

		
		
		improve = new BioOpinion(
				idata(json, "OPINION_FILE_SCARED"), 
				idata(json, "OPINION_FILE_NORMAL"),
				idata(json, "OPINION_FILE_CONFIDENT"),
				race);
	}
	
	private BioOpinionData idata(Json json, String key) {
		String f = json.value(key);
		if (!cachedemand.containsKey(f)) {
			json = new Json(PATHS.TEXT().getFolder("race").getFolder("opinion").get(f));
			BioOpinionData d = new BioOpinionData(json);
			cachedemand.put(f, d);
		}
		return cachedemand.get(f);
	}
	
	public LIST<BIO_LINE> lines(){
		return data.descs;
	}
	
	public CharSequence opinionTitle(Induvidual indu) {
		return improve.title(indu, STANDINGS.get(indu.clas()).current(indu));
	}
	
	public void opinions(LIST<Str> res, Induvidual indu) {
		improve.get(res, indu);
	}
	
	public void opinions(LIST<Str> res, HCLASS cl, Race race, long ran) {
		improve.get(res, cl, race, ran);
	}
	
	public CharSequence opinion(Induvidual indu) {
		improve.get(tmp, indu);
		return tmp.get(0);
	}
	
	public CharSequence houseProblem(Humanoid a) {
		for (BIO_LINE d : data.houseP) {
			CharSequence s = d.get(a);
			if (s != null) {
				return s;
			}
		}
		return null;
	}
	
	public interface BIO_LINE {
		
		public CharSequence get(Humanoid a);
		
		public boolean nl();
	}
	
}
