package settlement.stats.stat;

import game.boosting.BoostSpecs;
import init.race.Race;
import init.sprite.UI.UI;
import settlement.entity.humanoid.HCLASS;
import settlement.stats.*;
import settlement.stats.standing.StatStanding;
import snake2d.util.sets.INDEXED;
import util.data.INT_O.INT_OE;

public abstract class STAT implements INDEXED, SETT_STATISTICS {

	private StatDecree decree;
	private final int index;
	protected final String key;
	protected final StatInfo info;
	public StatStanding standing;
	public final BoostSpecs boosters;
	
	protected STAT(String key, StatsInit init, StatInfo info) {
		index = init.stats.add(this);
		init.coll.all.add(this);
		if (key != null) {
			key = init.coll.key + "_" + key;
			key = key.replace("__", "_");
		}
		this.key = key;
		if (info == null && key != null)
			info = new StatInfo(init.dText.json(key));
		if (key != null)
			init.statMap.put(key, this);
		if (info != null)
			this.info = new StatInfo(info);
		else {
			this.info = new StatInfo("no use","no use");
			this.info.setMatters(false, false);
		}
		boosters = new BoostSpecs(info == null ? "" : info.name, UI.icons().s.human, true);
		
	}
	
	abstract public INT_OE<Induvidual> indu();

	@Override
	public final StatInfo info() {
		return info;
	}

	public final StatStanding standing() {
		return standing;
	}

	public final String key() {
		return key;
	}

	public int pdivider(HCLASS c, Race r, int daysback) {
		return STATS.POP().POP.data(c).get(r, daysback);
	}

	public void addDecree(StatDecree d) {
		this.decree = d;
	}

	public StatDecree decree() {
		return decree;
	}

	public boolean hasIndu() {
		return false;
	}
	
	@Override
	public final int index() {
		return index;
	}

}
