package settlement.army;

import java.io.IOException;
import java.util.Arrays;

import init.D;
import init.boostable.BOOSTABLES;
import init.config.Config;
import snake2d.util.file.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.data.DOUBLE_O.DOUBLE_OE;
import util.data.DataOL;
import util.dic.DicArmy;
import util.dic.DicMisc;
import util.info.INFO;

public final class DivMorale {

	static {
		D.t(DivMorale.class);
	}
	
	private static DataOL<Div> data = new DataOL<Div>() {
		@Override
		protected long[] data(Div t) {
			return t.morale.d;
		}
	};
	
	public static final DivMoraleFactor BASE = new DivMoraleFactor(DicMisc.¤¤Base, D.g("BaseD", "Base Morale of your division. Increased by race, training and other factors")) {

		@Override
		public double getD(Div t) {
			return BOOSTABLES.BATTLE().MORALE.get(t);
		}

		@Override
		public DOUBLE_OE<Div> setD(Div t, double d) {
			return this;
		}
	
	};
	
	public static final DivMoraleFactor ARMY = new DivMoraleFactor(DicArmy.¤¤Army, D.g("ArmyD", "Base Morale of your army. Affected by being outnumbered, and by being ill supplied")) {

		@Override
		public double getD(Div t) {
			return t.army().morale();
		}

		@Override
		public DOUBLE_OE<Div> setD(Div t, double d) {
			return null;
		}
	
	};
	
	public static final DivMoraleFactor CASULTIES = new DivMoraleFactor(DicArmy.¤¤Casualties, D.g("CasultiesD", "The amount of soldiers that have been killed recently. Will go down slowly with time after a battle.")) {

		private final DOUBLE_OE<Div> da = data.new DataDouble();
		
		@Override
		public double getD(Div t) {
			return da.getD(t);
		}
		
		@Override
		public double getFactor(Div t) {
			if (t.menNrOf() == 0)
				return da.getD(t) > 0 ? 0 : 1;
			double am = da.getD(t)*4;
			return 1 - am / (am+t.menNrOf());
		};
	
		@Override
		void update(double ds, Div t) {
			
			if(t.army().enemy().men() == 0 || t.army().men() == 0) {
				if (t.menNrOf() == 0)
					setD(t, 0);
				else {
					double am = da.getD(t) - ds/2.0;
					setD(t, am);
				}
				
			}
		}

		@Override
		public DOUBLE_OE<Div> setD(Div t, double d) {
			ArmyMorale.CASULTIES.inc(t.army(), -(int)da.getD(t));
			
			da.setD(t, CLAMP.d(d, 0, Config.BATTLE.MEN_PER_DIVISION));
			ArmyMorale.CASULTIES.inc(t.army(), (int)da.getD(t));
			return this;
		};
	};
	
	public static final DivMoraleFactor DESERTION = new DivMoraleFactor(D.g("Routing"), D.g("RoutingD", "Once soldiers begin to desert in a division, others will as well")) {

		private final DOUBLE_OE<Div> da = data.new DataDouble();
		
		@Override
		public double getD(Div t) {
			return da.getD(t);
		}
	
		@Override
		public double getFactor(Div t) {
			if (t.menNrOf() == 0)
				return da.getD(t) > 0 ? 0 : 1;
			double am = da.getD(t)*8;
			return 1 - am / (am+t.menNrOf());
		};
		
		@Override
		void update(double ds, Div t) {
			
			if(t.army().enemy().men() == 0 || t.army().men() == 0) {
				if (t.menNrOf() == 0)
					setD(t, 0);
				else {
					double am = da.getD(t) - ds/2.0;
					setD(t, am);
				}
				
			}
			
		}

		@Override
		public DOUBLE_OE<Div> setD(Div t, double d) {
			ArmyMorale.DESERTION.inc(t.army(), -(int)da.getD(t));
			
			da.setD(t, CLAMP.d(d, 0, Config.BATTLE.MEN_PER_DIVISION));
			ArmyMorale.DESERTION.inc(t.army(), (int)da.getD(t));
			return this;
		};

	};
	
	public static final DivMoraleFactor IN_FORMATION = new DivMoraleFactor(DicArmy.¤¤Formation, D.g("FormationD", "The integrity of the formation the division is currently in.")) {

		private final DOUBLE_OE<Div> da = data.new DataDouble();
		
		@Override
		public double getD(Div t) {
			return da.getD(t);
		}
	
		@Override
		void update(double ds, Div t) {
			
		}
		
		@Override
		public double getFactor(Div t) {
			return 0.75 + 0.25*da.getD(t);
		};

		@Override
		public DOUBLE_OE<Div> setD(Div t, double d) {
			da.setD(t, d);
			return this;
		};
	};
	
	public static final DivMoraleFactor PROJECTILES = new DivMoraleFactor(DicArmy.¤¤Projectiles, D.g("ProjectilesD", "The amount of fire sustained recently.")) {

		private final DOUBLE_OE<Div> da = data.new DataDouble();
		
		@Override
		public double getD(Div t) {
			return da.getD(t);
		}
	
		@Override
		void update(double ds, Div t) {
			double d = da.getD(t);
			d -= ds*2;
			d = CLAMP.d(d, 0, t.menNrOf()*4);
			da.setD(t, d);
		}
		
		@Override
		public double getFactor(Div t) {
			return 1.0 - 0.25*CLAMP.d(da.getD(t)/(t.menNrOf()*4), 0, 1);
		};

		@Override
		public DOUBLE_OE<Div> setD(Div t, double d) {
			da.setD(t, d);
			return this;
		};
	};
	
	public static final DivMoraleFactor FLANKS = new DivMoraleFactor(D.g("Flanks"), D.g("FlanksD", "Attacks made on the flanks of a division can decrease morale")) {

		private final DOUBLE_OE<Div> da = data.new DataDouble();
		
		@Override
		public double getD(Div t) {
			return da.getD(t);
		}
	
		@Override
		void update(double ds, Div t) {
			
		}
		
		@Override
		public double getFactor(Div t) {
			return 1.0 -  0.5*da.getD(t);
		};

		@Override
		public DOUBLE_OE<Div> setD(Div t, double d) {
			da.setD(t, d);
			return this;
		};
	};
	
	public static final DivMoraleFactor SITUATION = new DivMoraleFactor(D.g("Situation"), D.g("The proximity, and amount of enemy troops and their quality.")) {

		private final DOUBLE_OE<Div> da = data.new DataDouble();
		
		@Override
		public double getD(Div t) {
			return da.getD(t);
		}
	
		@Override
		void update(double ds, Div t) {
			
		}
		
		@Override
		public double getFactor(Div t) {
			return da.getD(t)/8.0;
		};

		@Override
		public DOUBLE_OE<Div> setD(Div t, double d) {
			da.setD(t, d);
			return this;
		};
	};
	
	public static final DivMoraleFactor CURRENT = new DivMoraleFactor(DicArmy.¤¤Morale, DicArmy.¤¤MoraleD) {

		@Override
		public double getD(Div t) {
			return t.morale.current;
		}
	
		@Override
		void update(double ds, Div t) {
			
		}

		@Override
		public DOUBLE_OE<Div> setD(Div t, double d) {
			return this;
		};
	};
	
	public static final LIST<DivMoraleFactor> factors = new ArrayList<DivMoraleFactor>(ARMY, CASULTIES, DESERTION, PROJECTILES, IN_FORMATION, FLANKS);
	
	private long[] d = new long[data.longCount()];
	
	private final Div div;
	private double updateTimer = -1;
	private double current;
	private double target;
	
	DivMorale(Div div){
		this.div = div;
		saver.clear();
	}
	
	public double get() {
		return current;
	}
	
	public double target() {
		return target;
	}
	
	void update(float ds) {
		updateTimer -= ds;
		
		if (updateTimer < 0) {
			target = BASE.getFactor(div);
			for (DivMoraleFactor f : factors) {
				f.update(1.0, div);
			}
			for (DivMoraleFactor f : factors) {
				target *= f.getFactor(div);
			}
			target -= SITUATION.getFactor(div);
			
//			if (target <= 0)
//				GAME.Notify(div.index());
			
			if (target < current)
				current -= 0.1;
			else {
				current += 0.05;
			}
			current = CLAMP.d(current, -1, target);
			
			updateTimer += 1.0;
			
		}
	}
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			file.ls(d);
			file.d(updateTimer);
			file.d(current);
			file.d(target);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			file.ls(d);
			updateTimer = file.d();
			current = file.d();
			target = file.d();
		}
		
		@Override
		public void clear() {
			Arrays.fill(d, 0);
			updateTimer = -1;
			current = 1.0;
			target = 1.0;
		}
	};
	
	public void init() {
		for (DivMoraleFactor g : factors)
			g.setD(div, 0);
	}

	
	public abstract static class DivMoraleFactor implements DOUBLE_OE<Div>{

		private final INFO info;
		
		DivMoraleFactor(CharSequence name, CharSequence desc){
			info = new INFO(name, desc);
		}
		
		@Override
		public INFO info() {
			return info;
		}
		
		void update(double ds, Div t) {
			
		}
		
		public double getFactor(Div t) {
			return getD(t);
		}
		
	}
	
}
