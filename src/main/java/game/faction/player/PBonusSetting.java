package game.faction.player;

import init.D;
import init.boostable.BOOSTABLE;
import init.boostable.BOOSTABLES;
import init.boostable.BOOSTER_COLLECTION.BOOSTER_COLLECTION_IMP;
import init.boostable.BOOSTER_COLLECTION.SIMPLE;
import snake2d.util.sets.KeyMap;

final class PBonusSetting extends BOOSTER_COLLECTION_IMP implements SIMPLE{

	private static CharSequence ¤¤name = "¤Advantage";
	static {
		D.ts(PBonusSetting.class);
	}

	private double[] add = new double[BOOSTABLES.all().size()];
	
	protected PBonusSetting(KeyMap<Double> boosts) {
		super(¤¤name);
		for (String k : boosts.keys()) {
			BOOSTABLE b = BOOSTABLES.CIVICS().tryGet(k);
			
			if (b != null) {
				double d = boosts.get(k);
				if (d != 0) {
					maxAdd[b.index] = Math.max(0, d);
					minAdd[b.index] = Math.min(d, 0);
					add[b.index] = d;
				}
				
			}
		}
		
	}
	
	@Override
	public double add(BOOSTABLE b) {
		return add[b.index];
	}

	@Override
	public double mul(BOOSTABLE b) {
		return 1.0;
	}

}
