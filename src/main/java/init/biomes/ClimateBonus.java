package init.biomes;

import java.util.Arrays;

import init.boostable.*;
import init.boostable.BOOST_LOOKUP.BOOSTER_LOOKUP_IMP;
import init.paths.PATHS;
import settlement.main.SETT;
import snake2d.util.file.Json;
import snake2d.util.sets.*;

public final class ClimateBonus extends BOOSTER_LOOKUP_IMP implements BOOST_LOOKUP.SIMPLE{

	ClimateBonus(BOOSTABLES bo, double[][] climates) {
		super(CLIMATES.INFO().name);

		Json j = new Json(PATHS.CONFIG().get(CLIMATES.KEY));
		
		
		for (CLIMATE c : CLIMATES.ALL()) {
			
			Boost b = new Boost(c, j.json(c.key), climates);
			c.booster = b;
			init(c);
		}
		
		makeBoosters(this, true, true, true);
		
		
		
		
	}

	@Override
	public double add(BOOSTABLE b) {
		return SETT.ENV().climate().booster.add[b.index()];
	}

	@Override
	public double mul(BOOSTABLE b) {
		return SETT.ENV().climate().booster.mul[b.index()];
	}
	
	public double add(BOOSTABLE b, CLIMATE t) {
		return t.booster.add[b.index()];
	}

	public double mul(BOOSTABLE b, CLIMATE t) {
		return t.booster.mul[b.index()];
	}
	
	static class Boost {

		final double[] mul = new double[BOOSTABLES.all().size()];
		final double[] add = new double[BOOSTABLES.all().size()];
		final LIST<BBoost> boosts;
		
		public Boost(CLIMATE c, Json json, double[][] climates) {
			LinkedList<BBoost> boosts = new LinkedList<>();
			LIST<BBoost> bo = BOOSTABLES.boosts(json);
			for (BBoost b : bo) {
				if (climates[b.boostable.index][c.index()] != 1)
					continue;
				boosts.add(b);
			}
			Arrays.fill(mul, 1);
			for (BOOSTABLE b : BOOSTABLES.all()) {
				if (climates[b.index][c.index()] != 1)
					boosts.add(new BBoost(b, climates[b.index][c.index()], true));
			}
			
			this.boosts = new ArrayList<BBoost>(boosts);
			for (BBoost b : boosts) {
				if (b.isMul()) {
					mul[b.boostable.index()] = b.value();
				}else
					add[b.boostable.index()] = b.value();
			}
			
			
			
		}
		
	}
}