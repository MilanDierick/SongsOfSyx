package world.regions.data;

import java.util.Arrays;

import game.GAME;
import game.boosting.Boostable;
import game.boosting.Booster;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LISTE;
import world.regions.Region;
import world.regions.WREGIONS;

public final class RDDefis {
	
	private final LISTE<RDDef> all =  new ArrayListGrower<>(); 
	
	private final int[] cacheIs = new int[WREGIONS.MAX];
	
	private boolean calcing = false;
	
	public void register(Boostable bo) {
		all.add(new RDDef(bo));
	}
	
	public RDDef get(Boostable bo, Booster lb) {
		if (lb.to() >= 0)
			return null;
		for (RDDef d : all) {
			if (d.bo == bo) {
				return d;
			}
		}
		return null;
	}
	
	private void calc(Region t) {
		
		if (cacheIs[t.index()] == GAME.updateI())
			return;
		
		cacheIs[t.index()] = GAME.updateI();
		for (RDDef d : all) {
			d.values[t.index()] = 1;
		}
		int deadLock = 0;
		while(calcAll(t) && deadLock++ < 100)
			;
		
	}
	
	private boolean calcAll(Region t) {
		
		if (calcing)
			return true;
		calcing = true;
		boolean ret = false;
		for (RDDef d : all) {
			ret |= d.pcalc(t);
		}
		calcing = false;
		return ret;
	}
	
	
	public class RDDef{

		private final Boostable bo;
		
		private double[] values = new double[WREGIONS.MAX];
		
		
		public RDDef(Boostable bo) {
			this.bo = bo;
			Arrays.fill(values, 1);
		}

		public double get(Region t) {
			calc(t);
			return values[t.index()];
		}
		
		private boolean pcalc(Region t) {

			
			double v = value(t);
			
			boolean ret = v != values[t.index()];
			values[t.index()] = v;
			return ret;
			
		}
		
		private double value(Region t) {
			double am = bo.get(t);
			double m = max(t);
			if (am >= 0)
				return 1;
			double d = 1 + (am)/m;
			return CLAMP.d(d, 0, 1);
			
		}

		public double max(Region t) {
			double m = 1;
			for (int i = 0; i < bo.muls().size(); i++) {
				if (bo.muls().get(i).get(t) > 1)
					m *= bo.muls().get(i).get(t);
			}
			double a = bo.baseValue;
			for (int i = 0; i < bo.adds().size(); i++) {
				if (bo.adds().get(i).get(t) > 0)
					a += bo.adds().get(i).get(t);
			}
			return m*a;
		}
		
	}
	
}
