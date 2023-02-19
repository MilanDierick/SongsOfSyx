package init.race;

import settlement.stats.STAT;
import snake2d.util.file.Json;
import snake2d.util.sprite.text.Str;
import util.dic.DicMisc;

public class Opinion {
	
	private static final CharSequence[] dm = new CharSequence[] {DicMisc.¤¤More + ": {0}"};
	private static final CharSequence[] dl = new CharSequence[] {DicMisc.¤¤Less + ": {0}"};
	
	CharSequence[] more = dm;
	CharSequence[] less = dl;
	
	public static final Opinion DEF = new Opinion(null, null);
	
	public Opinion(){
		
	}
	
	public Opinion(Json j, String key){
		if (j != null && j.has(key)) {
			j = j.json(key);
			if (j.has("MORE"))
				more = j.texts("MORE");
			if (j.has("LESS"))
				less = j.texts("LESS");
		}
	}
	
	public Opinion setMore(CharSequence more) {
		if (more != null)
			this.more = new CharSequence[] {more};
		return this;
	}
	
	public Opinion setLess(CharSequence more) {
		if (less != null)
			this.less = new CharSequence[] {more};
		return this;
	}
	
	void insert(Str prep, STAT stat) {
		prep.insert(0, stat.info().name);
	}
}