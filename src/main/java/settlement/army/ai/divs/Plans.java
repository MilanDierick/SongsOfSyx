package settlement.army.ai.divs;

import settlement.army.Army;
import settlement.army.DivSettings;
import settlement.army.ai.fire.DivTrajectory;
import settlement.army.ai.util.DivTDataStatus;
import settlement.army.formation.DivFormation;
import settlement.army.formation.DivPosition;
import settlement.army.order.DivTDataInfo;
import settlement.army.order.DivTDataTask;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.sets.*;
import util.data.DataOL;
import util.data.INT_O.INT_OE;

class Plans {

	public final LIST<Plan> all;
	public final PlanWalkToDest walk_to_dest;
	public final PlanAttackDiv attack;
	public final PlanAttackTile attackTile;
	public final PlanCharge charge;
	public final PlanFireDiv range;
	public final PlanStop stop;
	public final PlanFight fight;
	public final int longSize;
	
	Plans(Tools tools){
		
		ArrayList<Plan> all = new ArrayList<>(100);
		
		Data d = null;
		int lc = 0;
		
		d = new Data();
		walk_to_dest = new PlanWalkToDest(tools, all, d);
		if (d.longCount() > lc)
			lc = d.longCount();
		
		d = new Data();
		attack = new PlanAttackDiv(tools, all, d);
		if (d.longCount() > lc)
			lc = d.longCount();
		
		d = new Data();
		range = new PlanFireDiv(tools, all, d);
		if (d.longCount() > lc)
			lc = d.longCount();
		
		d = new Data();
		stop = new PlanStop(tools, all, d);
		if (d.longCount() > lc)
			lc = d.longCount();
		
		d = new Data();
		attackTile = new PlanAttackTile(tools, all, d);
		if (d.longCount() > lc)
			lc = d.longCount();
		
		d = new Data();
		charge = new PlanCharge(tools, all, d);
		if (d.longCount() > lc)
			lc = d.longCount();
		
		d = new Data();
		fight = new PlanFight(tools, all, d);
		if (d.longCount() > lc)
			lc = d.longCount();
		
		this.longSize = lc;
		
		this.all = new ArrayList<>(all);
		
	}
	
	static class Data extends DataOL<AIManager> {

		@Override
		protected long[] data(AIManager t) {
			return t.data;
		}
		
	}
	
	static abstract class Plan implements INDEXED{

		protected final Tools t;
		private final int index;
		public static AIManager m;
		public static final DivTDataInfo info = new DivTDataInfo();
		public static final DivPosition current = new DivPosition();
		public static final DivFormation dest = new DivFormation();
		public static final DivFormation next = new DivFormation();
		public static final VectorImp vec = new VectorImp();
		public static final PathDiv path = new PathDiv();
		public static final DivTDataStatus status = new DivTDataStatus();
		public static DivSettings settings;
		public static final DivFormation tmp = new DivFormation();
		public static final DivTDataTask task = new DivTDataTask();
		public static final DivTrajectory traj = new DivTrajectory();
		public static boolean shouldFire;
		public static boolean charging;
		public static boolean shouldBreak;
		
		public static Army a;
		private final INT_OE<AIManager> stateI;
		private final ArrayList<STATE> states = new ArrayList<>(256);
		
		public Plan(Tools tools, ArrayList<Plan> all, Data data) {
			this.t = tools;
			index = all.add(this);
			stateI = data. new DataByte();
		}
		
		@Override
		public int index() {
			return index;
		}
		
		abstract void init();
		abstract void update(int updateI, int gameMillis);
		
		protected STATE state(AIManager m) {
			return states.get(stateI.get(m));
		}
		
		abstract class STATE {
			
			private final int index = states.add(this);
			
			STATE(){
				
			}
			
			boolean set() {
				stateI.set(m, index);
				return setAction();
			}
			
			abstract boolean setAction();
			
			abstract void update(int updateI, int gameMillis);
			
		}
		
	}
	

	
}
