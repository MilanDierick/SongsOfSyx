package settlement.army.ai.divs;

import java.io.IOException;
import java.util.Arrays;

import settlement.army.ai.divs.Plans.Plan;
import settlement.army.formation.DIV_FORMATION;
import settlement.army.order.DivTData;
import settlement.army.order.DivTDataTask.DIVTASK;
import settlement.main.SETT;
import snake2d.util.file.*;

final class AIManager implements SAVABLE{

	final long[] data;
	private boolean hasOrderedWhenFighting = false;
	private int planI = -1;
	final DivTData order;
	
	AIManager(int size, DivTData order){
		this.data = new long[size];
		this.order = order;
	}
	
	public Plan plan() {
		if (planI == -1)
			return null;
		return ARMY_AI_DIVS.self.plans.all.get(planI);
	}
	
	private Plan getplan() {
		if (Plan.status.isFighting()) {
			if (Plan.task.task() != DIVTASK.FIGHTING && !hasOrderedWhenFighting) {
				Plan.task.interruptAttack();
				order.task.set(Plan.task);
			}
			hasOrderedWhenFighting = true;
		}else {
			hasOrderedWhenFighting = false;
		}
		
		
		
		switch(Plan.task.task()) {
		case ATTACK_MELEE:
			return ARMY_AI_DIVS.self.plans.attack;
		case ATTACK_RANGED:
			return ARMY_AI_DIVS.self.plans.range;
		case MOVE:
			return ARMY_AI_DIVS.self.plans.walk_to_dest;
		case STOP:
			return ARMY_AI_DIVS.self.plans.stop;
		case ATTACK_BUILDING:
			return ARMY_AI_DIVS.self.plans.attackTile;
		case CHARGE:
			return ARMY_AI_DIVS.self.plans.charge;
		case FIGHTING:
			return ARMY_AI_DIVS.self.plans.fight;
		}
		
		throw new RuntimeException(""+Plan.task.task());
		
	}
	
	public void update(int updateI, int gamemillis) {
		
		if (!order.active())
			return;
		
		order.dest.get(Plan.dest);
		if (Plan.dest.deployed() == 0)
			return;

		order.info.get(Plan.info);
		order.next.get(Plan.next);
		order.current.get(Plan.current);
		order.path.get(Plan.path);
		order.status.get(Plan.status);
		order.task.get(Plan.task);
		order.trajectory.get(Plan.traj);
		Plan.shouldFire = false;
		Plan.charging = false;
		Plan.shouldBreak = false;
		
		Plan.settings = SETT.ARMIES().divisions().get(order.index).settings;
		if (Plan.settings.formation == null)
			Plan.settings.formation = DIV_FORMATION.LOOSE;
		Plan.a = SETT.ARMIES().divisions().get(order.index).army();
		Plan.m = this;

		Plan p = plan();
		Plan pp = getplan();
		
		if (p != pp) {
			pp.init();
			planI = pp.index();
		}

		pp.update(updateI, gamemillis);
		SETT.ARMIES().divisions().get(order.index).settings.shouldFire = Plan.shouldFire;
		SETT.ARMIES().divisions().get(order.index).settings.charging = Plan.charging;
		SETT.ARMIES().divisions().get(order.index).settings.shouldbreak = Plan.shouldBreak;
	}

	@Override
	public void save(FilePutter file) {
		file.i(planI);
		file.ls(data);
		file.bool(hasOrderedWhenFighting);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		planI = file.i();
		file.ls(data);
		hasOrderedWhenFighting = file.bool();
	}
	
	@Override
	public void clear() {
		Arrays.fill(data, 0);
		planI = -1;
		hasOrderedWhenFighting = false;
	}
	
	public void clearData() {
		Arrays.fill(data, 0);
	}
	
}
