package settlement.room.main;

import java.io.Serializable;

import game.GAME;
import game.time.TIME;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.STATS;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.*;

public final class RoomEmploymentIns implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private short workersEmployed = 0;
	private short workersTarget = 0;
	private short workersTargetMax = 500;
	private final RoomInstance ins;
	
	private short effTickSucc,effTickTot;
	private float effTotAcc;
	private short effTotCount;
	private byte EffLast;
	
	private static double EffLastI = 1.0/100;
	private static double effTotCountM = (TIME.workSeconds/ROOMS.UPDATE_INTERVAL);
	
	private static ArrayListResize<Humanoid> employees = new ArrayListResize<>(512, 512*16);
	private static int employeesI = -1;
	
	
	
	RoomEmploymentIns(RoomInstance ins){
		this.ins = ins;
		EffLast = 1;
	}
	
	void update(boolean active, boolean day, boolean auto, double seconds) {
		if (blueprint().employment() == null)
			return;
		
		if (active) {
			
			if (workersTarget > max()) {
				neededSet(max());
			}
		}else
			return;
		
		if (employed() == 0) {
			if (EffLast != 100) {
				remove();
				EffLast = 100;
				effTotAcc = 0;
				effTotCount = 0;
				add();
			}
		}else if (effTickTot > 0) {
			effTotCount ++;
			effTotAcc += effTickSucc/(double)effTickTot;
		}
		effTickSucc = 0;
		effTickTot = 0;
		
		if (day) {
			
			byte newEff = 100;
			
			if (effTotCount > 0) {
				double d = CLAMP.d(2*effTotCount/effTotCountM, 0, 1);
				int n = (int) (d*Math.round(100*effTotAcc/effTotCount) + (1.0-d)*EffLast);
				n = CLAMP.i(n+1, 0, 100);
				newEff = (byte) n;
			}
			double last = efficiency();
			remove();
			EffLast = newEff;
			effTotAcc = 0;
			effTotCount = 0;
			add();
			
			if (auto) {

				double am = needed();
				if (am == 0) {
					neededSet(1);
				}else if (last >= 0.95 && EffLast >= 100 && needed()-employed() <= 1) {
					neededSet(needed()+1);

				}else if(am > 1 && employed()-needed() <= 1) {
					double p = Math.max(EffLast*EffLastI, last);
					int needed = (int) Math.ceil(p*(employed()+1.0));
					
					int fire = CLAMP.i(employed()-needed, 0, (int)Math.ceil(employed()/5.0));
					neededSet(needed()-fire);
					
				}	
				
			}
			
		}
	}
	

	void add() {
		if (ins.active() && blueprint().employment() != null) {
			blueprint().employment().register(this, 1);
		}
	}
	
	void remove() {
		if (ins.active() && blueprint().employment() != null)
			blueprint().employment().register(this, -1);
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
	
	public void dispose() {
		remove();
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
		}
		
	}
	
	public double efficiencySoFar() {
		if (effTotCount == 0)
			return 1;
		return effTotAcc/effTotCount;
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
		if (employeesI != GAME.updateI()) {
			employeesI = GAME.updateI();
			employees.clearSoft();
			return employees(ins, employees);
		}
		return employees;
	}
	
	public static LIST<Humanoid> employees(RoomInstance ins, LISTE<Humanoid> res){
		for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
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

}