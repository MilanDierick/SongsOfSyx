package settlement.stats.stat;

import settlement.stats.StatsInit;
import snake2d.util.sets.*;
import util.info.INFO;

public abstract class StatCollection implements INDEXED {

	public final String key;
	public final INFO info;
	private final int index;
	final ArrayListGrower<STAT> all = new ArrayListGrower<>();
	
	protected StatCollection(StatsInit init, String key){
		index = init.holders.add(this);
		init.init(key, this);
		this.key = key;
		this.info = new INFO(init.dText.json(key + "_INFO"));
	}
	
	public LIST<STAT> all(){
		return all;
	}
	

	
	@Override
	public int index() {
		return index;
	}
	
}
