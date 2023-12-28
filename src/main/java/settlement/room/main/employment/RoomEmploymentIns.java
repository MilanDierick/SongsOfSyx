package settlement.room.main.employment;

import java.io.Serializable;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.FResources.RTYPE;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.main.*;
import settlement.room.main.RoomInstance.SecretEmployment;
import settlement.stats.STATS;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.*;

public final class RoomEmploymentIns extends SecretEmployment implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private short workersEmployed = 0;
	private short workersTarget = 0;
	private short workersTargetMax = 500;
	private final RoomInstance ins;
	
	private short effTickSucc,effTickTot;
	private short effAccI = 0;
	private float effAcc = 0;
	private byte EffLast;
	private static double EffLastI = 1.0/100;
	
	private static ArrayListResize<Humanoid> employees = new ArrayListResize<>(512, 512*16);
	private static int employeesI = -1;
	private static Object employeesO = null;
	
	private boolean active = false;
	
	public RoomEmploymentIns(RoomInstance ins){
		this.ins = ins;
		EffLast = 1;
	}
	
	@Override
	protected void update(boolean active, boolean day, boolean auto, double seconds) {
		if (blueprint().employment() == null)
			return;
		
		if (workersTarget > max()) {
			neededSet(max());
		}
		
		updateEff(active, day, auto, seconds);	
		
		if (day) {
			remove();
			for (RoomEquip w : SETT.ROOMS().employment.tools.ALL) {
				updateTools(w, true);
			}
			add();
		}
			
		
	}
	
	private void updateTools(RoomEquip w, boolean expire) {
		
		if (expire) {
			int exp = toolsToExpire(w);
			exp = CLAMP.i(exp, 0, tools(w));
			int am = tools(w)-exp;
			toolISet(w, toolI, am);
			toolISet(w, toolToExpireI, 0);
		}
		
		
		int nn = toolsNeeded(w);
		if (nn < 0) {
			int newAm = tools(w)+nn;
			SETT.THINGS().resources.create(ins.mX(), ins.mY(), w.resource, -nn);
			FACTIONS.player().res().inc(w.resource, RTYPE.EQUIPPED, nn);
			toolISet(w, toolI, newAm);
		}
		
		if (expire) {
			double am = tools(w);
			am *= w.degradePerDay;
			int a = (int) am;
			am -= a;
			if (RND.rFloat() < am)
				a++;
			toolISet(w, toolToExpireI, a);
		}
	}
	
	private void updateEff(boolean active, boolean day, boolean auto, double seconds) {
		if (employed() == 0) {
			if (EffLast != 100) {
				remove();
				EffLast = 100;
				effAcc = 0;
				effAccI = 0;
				effTickSucc = 0;
				effTickTot = 0;
				add();
			}
			return;
		}else if (effTickTot > 0) {
			effAcc += effTickSucc/effTickTot;
			effAccI ++;
			effTickSucc = 0;
			effTickTot = 0;
		}
		
		if (day) {
			if (effAccI == 0)
				return;
			
//			LOG.ln(EffLast);
//			LOG.ln(effAcc / effAccI);
			
			remove();
			double last = EffLast/100.0;
			double d =  effAcc / effAccI;
			effAcc = 0;
			effAccI = 0;
			EffLast = (byte) CLAMP.i((int) Math.ceil(d*100), 0, 100);
			add();
			if (active && auto)
				adjustAuto(d, last);
		}
	}
	
	private void adjustAuto(double workload, double last) {
		double am = needed();
		if (am == 0) {
			neededSetAdjustWorkload(1);
		}else if (last >= 1.0 && workload >= 1.0 && needed()-employed() <= 1) {
			neededSetAdjustWorkload(needed()+1);

		}else if(am > 1 && last < 1.0 && workload < 1.0 && employed()-needed() <= 1) {
			double p = (workload+last)/2.0;
			int needed = (int) Math.ceil(p*(employed()+1.0));
			
			int fire = CLAMP.i(employed()-needed, 0, (int)Math.ceil(employed()/10.0));
			if (fire > 0)
				neededSetAdjustWorkload(needed()-fire);
			
		}	
	}

	@Override
	protected void activate(boolean active) {
		if (active == this.active) {
			return;
		}
		
		remove();
		
		if (!active) {
			
			
			EffLast = 100;
			effAcc = 0;
			effAccI = 0;
			effTickSucc = 0;
			effTickTot = 0;
		}
		this.active = active;
		
		add();
		
		
		
		
	}
	
	@Override
	protected void dispose() {
		if (blueprint().employment() != null) {
			if (employed() > 0) {
				int rem = 0;
				int added = 0;
				for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
					if (e instanceof Humanoid && STATS.WORK().EMPLOYED.get(((Humanoid) e).indu()) == ins) {
						STATS.WORK().EMPLOYED.set(((Humanoid) e), null);
						rem++;
						if (!e.isRemoved())
							added ++;
					}
				}
				if (employed() != 0) {
					throw new RuntimeException(rem + " " + added + " "  + employed());
				}
					
			}
			for (RoomEquip w : SETT.ROOMS().employment.tools.ALL) {
				updateTools(w, false);
			}
		}
	}

	private void remove() {
		if (this.active && blueprint().employment() != null) {
			blueprint().employment().register(this, -1);
		}
	}
	
	private void add() {
		if (this.active && blueprint().employment() != null) {
			blueprint().employment().register(this, 1);
		}
	}

	
	public void maxSet(int max) {
		if (max < 0 || max > Short.MAX_VALUE)
			throw new RuntimeException(ins().name(0,0) + " " + max + " " + ins().mX() + " " + ins().mY());
		this.workersTargetMax = (short) max;
	}
	
	public int max() {
		return workersTargetMax;
	}
	
	public final void reportWorkSuccess(boolean success) {
		
		
		if (effTickSucc > Short.MAX_VALUE || effTickTot > Short.MAX_VALUE) {
			effTickSucc = (short) (effTickSucc >>> 1);
			effTickTot = (short) (effTickSucc >>> 1);
		}

		if (success)
			effTickSucc++;
		effTickTot++;
		
	}
	
	public void neededSet(int target) {
		
		target = CLAMP.i(target, 0, max());
		if (target != workersTarget) {
			remove();
			workersTarget = (short) target;
			add();
		}
	}
	
	private void neededSetAdjustWorkload(int target) {
		
		target = CLAMP.i(target, 0, max());
		if (target != workersTarget) {
			remove();
			if (target > workersTarget) {
				double d = 0.75*workersTarget/target;
				EffLast = (byte) CLAMP.i(EffLast-(int)(d*100), 0, 100);
			}else {
				EffLast = 100;
			}
			workersTarget = (short) target;
			add();
		}
	}
	
	public int needed() {
		if (!ins.active())
			return 0;
		return workersTarget;
	}
	
	public int hardTarget() {
		return workersTarget;
	}
	
	public int target() {
		return (int) Math.ceil(workersTarget*blueprint().employment().getFill());
	}
	
	public final boolean isOverstaffed() {
		return (!ins().active() && workersEmployed > 0) || workersEmployed > target();
	}
	
	public final int employed() {
		return workersEmployed;
	}
	
	/**
	 * Only called from stat
	 * @param h
	 */
	public void employ(Humanoid h) {
		
		remove();
		workersEmployed ++;
		if (ins.blueprint().employment() != null) {
			ins.blueprint().employment().employ(h, 1);
		}
		add();
	}
	
	/**
	 * Only called from stat
	 * @param h
	 */
	public void fire(Humanoid h) {
		remove();
		workersEmployed --;
		if (ins.blueprint().employment() != null) {
			ins.blueprint().employment().employ(h, -1);
		}
		add();
	}
	
	
	public double efficiencySoFar() {
		if (effAccI == 0)
			return EffLast*EffLastI;
		return effAcc/effAccI;
	}
	
	public double efficiency() {
		return EffLast*EffLastI;
	}
	
	public final RoomInstance ins(){
		return ins;
	}
	
	public final RoomBlueprintIns<?> blueprint(){
		return (RoomBlueprintIns<?>)(ins.blueprint());
	}
	
	public LIST<Humanoid> employees(){
		return employees(this.ins);
	}
	
	public LIST<Humanoid> employees(LISTE<Humanoid> res){
		return employees(this.ins, res);
	}
	
	public static LIST<Humanoid> employees(RoomInstance ins){
		if (echeck(ins)) {
			return employees(ins, employees);
		}
		return employees;
	}
	
	public static LIST<Humanoid> employess(RoomBlueprint imp){
		if (echeck(imp)) {
			return employees(imp, employees);
		}
		return employees;
	}
	
	private static boolean echeck(Object o) {
		if (employeesI != GAME.updateI() || o != employeesO) {
			employeesI = GAME.updateI();
			employeesO = o;
			employees.clearSoft();
			return true;
		}
		return false;
	}
	
	public static LIST<Humanoid> employees(RoomInstance ins, LISTE<Humanoid> res){
		for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
			if (!res.hasRoom())
				break;
			if (e instanceof Humanoid) {
				Humanoid a = (Humanoid) e;
				if (STATS.WORK().EMPLOYED.get(a) == ins) {
					res.add(a);
					if (!res.hasRoom())
						break;
					
				}
				
			}
		}
		return res;
	}
	
	public static LIST<Humanoid> employees(RoomBlueprint bb, LISTE<Humanoid> res){
		for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
			if (e instanceof Humanoid) {
				Humanoid a = (Humanoid) e;
				if (STATS.WORK().EMPLOYED.get(a) != null && STATS.WORK().EMPLOYED.get(a).blueprint() == bb) {
					res.add(a);
					if (!res.hasRoom())
						break;
					
				}
				
			}
		}
		return res;
	}
	
	private int[] equipData = new int[0];
	private static final int toolI = 0;
	private static final int toolToExpireI = 1;
	private static final int toolReservedI = 2;
	
	public int tools(RoomEquip w) {
		return toolI(w, toolI);
	}
	
	public int toolsTarget(RoomEquip w) {
		return employed()*w.target(ins.blueprintI().employment()).get();
	}
	
	public int toolsTargetMax(RoomEquip w) {
		return employed()*w.target(ins.blueprintI().employment()).max();
	}
	
	public double toolD(RoomEquip w) {
		double t = toolsTargetMax(w);
		if (t == 0)
			return 0;
		return CLAMP.d(tools(w)/t, 0, 1);
	}
	
	public double toolsPerPerson(RoomEquip w) {
		double t = employed();
		if (t == 0)
			return 0;
		return CLAMP.d(tools(w)/t, 0, toolsTargetMax(w));
	}
	
	public int toolsToExpire(RoomEquip w) {
		return toolI(w, toolToExpireI);
	}
	
	public int toolsNeeded(RoomEquip w) {
		return toolsTarget(w) - tools(w) + toolsToExpire(w) - toolReserved(w);
	}
	
	public int toolReserved(RoomEquip w) {
		return toolI(w, toolReservedI);
	}

	public void toolReserve(RoomEquip w, int am) {
		int aa = toolI(w, toolReservedI) + am;
		if (aa < 0)
			throw new RuntimeException();
		toolISet(w, toolReservedI, aa);
	}
	
	public void toolDeliver(RoomEquip w, int am) {
		if (am < 0)
			throw new RuntimeException();
		remove();
		FACTIONS.player().res().inc(w.resource, RTYPE.EQUIPPED, -am);
		int aa = toolI(w, toolI) + am;
		toolISet(w, toolI, aa);
		add();
	}
	
	private int toolI(RoomEquip w, int ii) {
		if (equipData.length != SETT.ROOMS().employment.tools.ALL.size()*3)
			equipData = new int[SETT.ROOMS().employment.tools.ALL.size()*3];
		return equipData[w.index()*3+ii];
	}
	
	private void toolISet(RoomEquip w, int ii, int value) {
		if (equipData.length != SETT.ROOMS().employment.tools.ALL.size()*3)
			equipData = new int[SETT.ROOMS().employment.tools.ALL.size()*3];
		equipData[w.index()*3+ii] = value;
	}
	
	
	
}