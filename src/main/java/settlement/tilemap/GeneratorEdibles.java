package settlement.tilemap;

import static settlement.main.SETT.*;

import init.RES;
import init.resources.Growable;
import init.resources.RESOURCES;
import settlement.main.CapitolArea;
import settlement.main.SETT;
import snake2d.PathTile;
import snake2d.util.datatypes.*;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LinkedList;

class GeneratorEdibles {
	
	GeneratorEdibles(CapitolArea area, GeneratorUtil util, LinkedList<COORDINATE> caves) {
		
	
		RES.flooder().init(this);
		double fTot = 0;
		for (COORDINATE c : SETT.TILE_BOUNDS) {
			double ff = getValue(c.x(), c.y());
			if (ff >= 0.1) {
				fTot += ff;
				RES.flooder().pushSloppy(c, RND.rFloat()).setValue2(0);
			}
			
		}
		fTot /= SETT.TAREA;
		fTot = Math.pow(fTot, 0.5);
		fTot *= util.json.d("EDIBLES_AMOUNT", 0, 1);
		
		LinkedList<Coo> spots = new LinkedList<>();
		util.polly.checkInit();
		while(RES.flooder().hasMore()) {
			PathTile c = RES.flooder().pollGreatest();
			if (util.polly.checker.is(c))
				continue;
			util.polly.checker.set(c, true);
			spots.add(new Coo(c));
		}
		
		int[] amounts = new int[RESOURCES.growable().all().size()];
		
		fTot *= 4000;
		fTot /= RESOURCES.growable().all().size();
		
		for (Growable g : RESOURCES.growable().all()) {
			amounts[g.index] = (int) (Math.sqrt(g.growthValue)*g.availability(area.climate())*fTot);
		}

		RES.flooder().done();
		
		while(!spots.isEmpty()) {
			COORDINATE c = spots.removeFirst();
			if (!place(c.x(), c.y(), amounts))
				break;
		}
		
	}
	
	private boolean place(int x, int y, int[] amounts) {
		int roff = RND.rInt(RESOURCES.growable().all().size());
		
		for (int i = 0; i < RESOURCES.growable().all().size(); i++) {
			int k = (roff+i)%amounts.length;
			int am = amounts[k];
			if (am > 0) {
				am -= mineralize(x, y, SETT.TERRAIN().GROWABLES.get(k), am);
				amounts[k] = am;
				return true;
			}
		}
		return false;
	}
	
	private double getValue(int x, int y) {
		if (!SETT.IN_BOUNDS(x, y))
			return -1;
		if (!TERRAIN().NADA.is(x, y))
			return -1;
		if (MINERALS().getter.is(x, y))
			return -1;
		
		double ff = SETT.FERTILITY().baseD.get(x, y);
		if (ff > 0.2) {
			ff -= 0.2;
			ff/= 0.8;
			return ff;
		}
		return -1;
	}
	
	private int mineralize(int x, int y, TGrowable g, int amount) {
		
		
		Rec bound = new Rec();
		bound.moveX1Y1(x, y);
		double base = getValue(x, y);
		if (base < 0)
			return 0;

		double radius = 1.0/(2 + RND.rFloat()*10);
		
		RES.flooder().init(this);
		RES.flooder().pushSloppy(x, y, 1.0);
		
		double values = 0;
		int size = 0;
		while(RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollGreatest();
			double v = getValue(t.x(), t.y());
			if (v < 0)
				continue;
			double pValue = t.getValue();
			
			size++;
			values += pValue*getValue(t.x(), t.y());
			bound.unify(t.x(), t.y());
			
			double dValue = (0.5 + Math.abs(base-v)*32)*radius;
			
			int di = RND.rInt(DIR.ALL.size());
			for (int i = 0; i < DIR.ALL.size(); i++) {
				DIR d = DIR.ALL.getC(i+di);
				double vv = pValue - dValue*d.tileDistance();
				if (SETT.IN_BOUNDS(t, d) && vv > 0) {
					RES.flooder().pushSmaller(t, d, vv);
				}
			}
			
		}
		
		
	
		if (values <= 0 || size <= 0) {
			RES.flooder().done();
			return 0;
		}
		
		double value = 1.0;
		values *= g.size.max;
		if (values > amount) {
			value = amount/values;
		}
		
		int total = 0;
		
		for (COORDINATE c : bound) {
			x = c.x();
			y = c.y();
			if (RES.flooder().hasBeenPushed(x,y)) {
				double v = RND.rFloat1(0.2)*value*RES.flooder().getValue(x,y)*getValue(x,y);
				v *= g.size.max;
				int am = (int) v;
				double d = v - am;
				if (RND.rFloat() < d)
					am++;
				
				if (am > 0) {
					total += am;
					g.placeFixed(x, y);
					g.size.set(x, y, am);
					g.resource.set(x, y, am);
				}
				
			}
		}
	
		RES.flooder().done();
		
		if (total > 0 && amount < 3)
			return amount;
		return total;
	}

}
