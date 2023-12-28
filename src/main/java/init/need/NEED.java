package init.need;

import game.boosting.*;
import init.D;
import init.paths.PATHS.ResFolder;
import init.sprite.UI.UI;
import settlement.main.SETT;
import settlement.room.service.module.RoomServiceGroup;
import settlement.stats.STATS;
import settlement.stats.colls.StatsNeeds.StatNeed;
import snake2d.util.file.Json;
import snake2d.util.sets.INDEXED;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.text.Str;

public final class NEED implements INDEXED{
	
	private static CharSequence ¤¤rateD = "The rate at which the need of {0} increases daily.";
	static {
		D.ts(NEED.class);
	}
	
	public final CharSequence nameNeed;
	public final String key;
	public final Boostable rate;
	private final int index;
	public final boolean resets;
	
	NEED(String key, ResFolder f, LISTE<NEED> all, BoostableCat cat, boolean resets) {
		this.index = all.add(this);
		this.key = key;
		Json jt = new Json(f.text.get(key));
		Json jd = new Json(f.init.get(key));
		this.nameNeed = jt.text("NAME_NEED");
		this.rate = BOOSTING.push(key, jd.d("RATE"), jt.text("NAME_RATE"), ""+Str.TMP.clear().add(¤¤rateD).insert(0, nameNeed), UI.icons().s.clock, cat);
		this.resets = resets;
	}
	
	@Override
	public int index() {
		return index;
	}
	
	@Override
	public String toString() {
		return key;
	}
	
	public StatNeed stat() {
		return STATS.NEEDS().SNEEDS.get(index);
	}
	
	public RoomServiceGroup sGroup() {
		return SETT.ROOMS().SERVICE.get(this);
	}
	


}
