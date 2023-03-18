package game.faction.player;

import init.D;
import init.boostable.BOOSTABLE;
import init.boostable.BOOSTABLES;
import init.boostable.BOOST_LOOKUP.BOOSTER_LOOKUP_IMP;
import init.boostable.BOOST_LOOKUP.SIMPLE;
import snake2d.LOG;
import snake2d.util.sets.KeyMap;
import util.keymap.KEY_COLLECTION;

final class PBonusSetting extends BOOSTER_LOOKUP_IMP implements SIMPLE{

	private static CharSequence ¤¤name = "¤Advantage";
	static {
		D.ts(PBonusSetting.class);
	}

	private double[] add = new double[BOOSTABLES.all().size()];
	
	protected PBonusSetting(KeyMap<Double> boosts) {
		super(¤¤name);
		for (String k : boosts.keys()) {
			String scoll = k.split("_")[0];
			if (BOOSTABLES.collmap().containsKey(scoll)) {
				
				KEY_COLLECTION<? extends BOOSTABLE> col = BOOSTABLES.collmap().get(scoll);
				
				String bb = k.split("_")[1];

				
				BOOSTABLE b = col.tryGet(bb);
				
				if (b != null) {
					double d = boosts.get(k);
					if (d != 0) {
						maxAdd[b.index] = Math.max(0, d);
						minAdd[b.index] = Math.min(d, 0);
						add[b.index] = d;
					}
					
				}else {
					LOG.ln("no mapping: " + k + " " + bb);
				}
				
			}else {
				LOG.ln("no mapping: " + k);
			}
			
			
		}
		
		makeBoosters(this, true, false, true);
		
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
