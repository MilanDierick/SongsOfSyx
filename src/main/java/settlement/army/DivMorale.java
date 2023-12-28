package settlement.army;

import java.io.IOException;
import java.util.Arrays;

import game.boosting.*;
import game.boosting.BValue.BValueSimple;
import game.faction.Faction;
import init.D;
import init.config.Config;
import init.sprite.UI.UI;
import snake2d.util.file.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import util.data.DOUBLE_O.DOUBLE_OE;
import util.data.DataOL;
import util.dic.DicArmy;
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
	
	public static final DivMoraleFactor ARMY = new DivMoraleFactor(
			UI.icons().s.fist, DicArmy.¤¤Army, D.g("ArmyD", "Base Morale of your army. Affected by being outnumbered, and by being ill supplied"),
			0.5, 1, true) {

		@Override
		public double getD(Div t) {
			return t.army().morale();
		}

		@Override
		public DOUBLE_OE<Div> setD(Div t, double d) {
			return null;
		}
	
	};
	
	public static final DivMoraleFactor CASULTIES = new DivMoraleFactor(
			UI.icons().s.death, 
			DicArmy.¤¤Casualties, 
			D.g("CasultiesD", "The amount of soldiers that have been killed recently. Will go down slowly with time after a battle."),
			1.0, 0.1, true) {

		private final DOUBLE_OE<Div> da = data.new DataDouble();
		
		@Override
		public double getD(Div t) {
			return da.getD(t);
		}
		
		@Override
		public double vGet(Div t) {
			if (t.menNrOf() == 0)
				return da.getD(t) > 0 ? 1 : 0;
			double am = da.getD(t)*4;
			return am / (am+t.menNrOf());
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
	
	public static final DivMoraleFactor DESERTION = new DivMoraleFactor(
			UI.icons().s.speed, D.g("Routing"), D.g("RoutingD", "Once soldiers begin to desert in a division, others will as well"),
			1.0, 0.1, true) {

		private final DOUBLE_OE<Div> da = data.new DataDouble();
		
		@Override
		public double getD(Div t) {
			return da.getD(t);
		}
	
		@Override
		public double vGet(Div t) {
			if (t.menNrOf() == 0)
				return da.getD(t) > 0 ? 1 : 0;
			double am = da.getD(t)*8;
			return am / (am+t.menNrOf());
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
	
	public static final DivMoraleFactor IN_FORMATION = new DivMoraleFactor(
			UI.icons().s.armour, DicArmy.¤¤Formation, D.g("FormationD", "The integrity of the formation the division is currently in."),
			1, 2, true
			) {

		private final DOUBLE_OE<Div> da = data.new DataDouble();
		
		@Override
		public double getD(Div t) {
			return da.getD(t);
		}
	
		@Override
		void update(double ds, Div t) {
			
		}
		
		@Override
		public double vGet(Div t) {
			return da.getD(t);
		};

		@Override
		public DOUBLE_OE<Div> setD(Div t, double d) {
			da.setD(t, d);
			return this;
		};
	};
	
	public static final DivMoraleFactor PROJECTILES = new DivMoraleFactor(
			UI.icons().s.bow, DicArmy.¤¤Projectiles, D.g("ProjectilesD", "The amount of fire sustained recently."),
			1, 0.5, true
			) {

		private final DOUBLE_OE<Div> da = data.new DataDouble();
		
		@Override
		public double getD(Div t) {
			return da.getD(t);
		}
	
		@Override
		void update(double ds, Div t) {
			double d = da.getD(t);
			d -= ds*0.25;
			d = CLAMP.d(d, 0, t.menNrOf()*4);
			da.setD(t, d);
		}
		
		@Override
		public double vGet(Div t) {
			return CLAMP.d(da.getD(t)/(t.menNrOf()*4), 0, 1);
		};

		@Override
		public DOUBLE_OE<Div> setD(Div t, double d) {
			da.setD(t, d);
			return this;
		};
	};
	
	public static final DivMoraleFactor FLANKS = new DivMoraleFactor(
			UI.icons().s.expand, D.g("Flanks"), 
			D.g("FlanksD", "Attacks made on the flanks of a division can decrease morale"),
			1.0, 0.5, true) {

		private final DOUBLE_OE<Div> da = data.new DataDouble();
		
		@Override
		public double getD(Div t) {
			return da.getD(t);
		}
	
		@Override
		void update(double ds, Div t) {
			
		}
		
		@Override
		public double vGet(Div t) {
			return da.getD(t);
		};

		@Override
		public DOUBLE_OE<Div> setD(Div t, double d) {
			da.setD(t, d);
			return this;
		};
	};
	
	public static final DivMoraleFactor SITUATION = new DivMoraleFactor(
			UI.icons().s.eye, D.g("Situation"), D.g("The proximity, and amount of enemy troops and their quality."),
			0, -10, false
			) {

		private final DOUBLE_OE<Div> da = data.new DataDouble();
		
		@Override
		public double getD(Div t) {
			return da.getD(t);
		}
	
		@Override
		void update(double ds, Div t) {
			
		}
		
		@Override
		public double vGet(Div t) {
			return da.getD(t)/8.0;
		};

		@Override
		public DOUBLE_OE<Div> setD(Div t, double d) {
			da.setD(t, d);
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
	
	void update(double ds) {
		for (DivMoraleFactor f : factors) {
			f.update(1.0, div);
		}
		target = BOOSTABLES.BATTLE().MORALE.get(div);
		
		if (target < current) {
			current -= 1;
			current = CLAMP.d(current, target, 1.0);
		}
		else {
			current += 0.5;
			current = CLAMP.d(current, 0, target);
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
		current = BOOSTABLES.BATTLE().MORALE.get(div);
	}

	
	public abstract static class DivMoraleFactor implements DOUBLE_OE<Div>, BValueSimple{

		private final INFO info;
		
		DivMoraleFactor(SPRITE icon, CharSequence name, CharSequence desc, double from, double to, boolean isMul){
			info = new INFO(name, desc);
			new BoosterWrap(this, new BSourceInfo(name, icon), from, to, isMul).add(BOOSTABLES.BATTLE().MORALE);
		}
		
		@Override
		public INFO info() {
			return info;
		}
		
		void update(double ds, Div t) {
			
		}
		
		@Override
		public double vGet(Div div) {
			return getD(div);
		}
		
		@Override
		public double vGet(Faction f) {
			return 0;
		}
		
		@Override
		public boolean has(Class<? extends BOOSTABLE_O> b) {
			return b == Div.class;
		}
		
	}
	
}
