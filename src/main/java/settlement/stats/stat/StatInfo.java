package settlement.stats.stat;

import init.race.Opinion;
import snake2d.util.file.Json;
import util.info.INFO;

public class StatInfo extends INFO {

	private boolean isInt = false;
	private boolean matters = true;
	private boolean hasIndu = true;
	public Opinion defOpinion = Opinion.DEF;

	public StatInfo(StatInfo other) {
		super(other.name, other.names, other.desc, null);
		this.isInt = other.isInt;
		this.matters = other.matters;
		this.hasIndu = other.hasIndu;
		defOpinion = other.defOpinion;
	}
	
	public StatInfo(Json json) {
		super(json);
		defOpinion = new Opinion(json, "_OPINION");
	}

	public StatInfo(CharSequence name, CharSequence desc) {
		super(name, desc);
	}

	public StatInfo(CharSequence name, CharSequence names, CharSequence desc) {
		super(name, names, desc, null);
	}

	public boolean isInt() {
		return isInt;
	}

	public void setInt() {
		isInt = true;
	}
	
	public void setOpinion(CharSequence more, CharSequence desc) {
		defOpinion = new Opinion().setMore(more).setLess(more);
	}
	
	public void setMatters(boolean matters, boolean hasIndu) {
		this.matters = matters;
		this.hasIndu = hasIndu;
	}
	
	public boolean indu() {
		return hasIndu;
	}
	
	public boolean matters() {
		return matters;
	}

}