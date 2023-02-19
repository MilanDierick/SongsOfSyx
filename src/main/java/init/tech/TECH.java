package init.tech;

import game.GAME;
import snake2d.util.file.Json;
import snake2d.util.sets.*;
import util.info.INFO;

public final class TECH extends Unlocks implements INDEXED{

	private final int index;
	public final int levelMax;
	public final int levelCost;
	public final int levelCostInc;
	public final String order;
	public final double levelCostMulInc;
	
	private LIST<TechRequirement> needs;
	private LIST<TechRequirement> needsPruned;
	public final INFO info;
	public final String category;
	
	TECH(LISTE<TECH> all, Json data, Json text){
		super("", data);
		info = new INFO(text);
		category = text.text("CATEGORY");
		index = all.add(this);
		
		order = data.has("TREE_ORDER") ? data.value("TREE_ORDER") : "ZZ";
		levelMax = data.i("LEVEL_MAX", 1, 10000, 1);
		levelCost = data.i("LEVEL_COST", 0, 100000);
		levelCostInc = data.i("LEVEL_COST_INC", 0, 100000, 0);
		levelCostMulInc = data.dTry("LEVEL_COST_INC_MUL", 1, 100000, 1);
		if (bonuses.size() == 0 && roomUnlocks.size() == 0 && industryUnlocks.size() == 0 && upgrades.size() == 0 && unlocksRoads().size() == 0)
			GAME.Warn("Worthless unlock. Needs to do something! " + data.path());
		
	}
	
//	TECH(LIST_MUTALBLE<TECH> all, BOOSTABLE b){
//		super(b, b.levelAmount);
//		info = new INFO(b.name, b.desc);
//		index = all.add(this);
//		levelMax = b.levelMax;
//		levelCost = b.levelCost;
//		levelCostInc = b.levelCostInc;
//	}

	@Override
	public int index() {
		return index;
	}
	
	public LIST<TechRequirement> requires(){
		return needs;
	}
	
	public LIST<TechRequirement> requiresNodes(){
		return needsPruned;
	}
	
	void set(LIST<TechRequirement> needs) {
		this.needs = needs;
	}
	
	void prune(LIST<TechRequirement> needs) {
		this.needsPruned = needs;
	}
	
	public boolean requires(TECH other, int level) {
		if (other == this)
			return false;
		for (int i = 0; i < needs.size(); i++) {
			TECH t = needs.get(i).tech;
			if (t == other || t.requires(other, needs.get(i).level))
				if (needs.get(i).level > level)
					return true;
		}
		return false;
	}
	
	public static final class TechRequirement {
		
		public final TECH tech;
		public final int level;
		
		TechRequirement(TECH t, int l) {
			this.tech = t;
			this.level = l;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof TechRequirement) {
				TechRequirement q = (TechRequirement) obj;
				return q.level == level && q.tech == tech;
			}
			return false;
		}
		
	}
	
}
