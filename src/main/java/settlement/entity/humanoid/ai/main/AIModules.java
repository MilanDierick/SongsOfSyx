package settlement.entity.humanoid.ai.main;

import static settlement.main.SETT.*;

import init.C;
import settlement.entity.humanoid.HTYPE;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.battle.AIModule_Battle;
import settlement.entity.humanoid.ai.crime.AIModule_Crime;
import settlement.entity.humanoid.ai.entertainment.AIModule_Entertainment;
import settlement.entity.humanoid.ai.equipment.AIModule_Equipment;
import settlement.entity.humanoid.ai.health.AIModule_Health;
import settlement.entity.humanoid.ai.health.AIModule_Hygine;
import settlement.entity.humanoid.ai.idle.AIModule_Idle;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.entity.humanoid.ai.needs.*;
import settlement.entity.humanoid.ai.subject.AIModule_Subject;
import settlement.entity.humanoid.ai.types.child.AIModule_Child;
import settlement.entity.humanoid.ai.types.insane.AIModule_Insane;
import settlement.entity.humanoid.ai.types.noble.AIModule_Noble;
import settlement.entity.humanoid.ai.types.prisoner.AIModule_Prisoner;
import settlement.entity.humanoid.ai.types.recruit.AIModule_Recruit;
import settlement.entity.humanoid.ai.types.retired.AIModule_Retired;
import settlement.entity.humanoid.ai.types.rioter.AIModule_Rioter;
import settlement.entity.humanoid.ai.types.slave.AIModule_Slave;
import settlement.entity.humanoid.ai.types.student.AIModule_Student;
import settlement.entity.humanoid.ai.types.tourist.AIModule_Tourist;
import settlement.entity.humanoid.ai.work.AIModule_Work;
import settlement.room.main.ROOMA;
import settlement.stats.*;
import snake2d.util.datatypes.COORDINATEE;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;
import snake2d.util.sets.*;
import util.data.INT_O.INT_OE;

public final class AIModules {
	
	private static AIDataModule data;
	{
		data = new AIDataModule(AI.data());
	}
	public final AIModule_Idle idle = new AIModule_Idle();
	final AIModule_Hunger hunger = new AIModule_Hunger();
	final AIModule drink = new AIModule_Drink();
	final AIModule religion = new AIModule_Religion();
	final AIModule_Subject subject = new AIModule_Subject();
	final AIModule health = new AIModule_Health();
	final AIModule equipment = new AIModule_Equipment();
	final AIModule_Hygine hygine = new AIModule_Hygine();
	final AIModule_Groom groom = new AIModule_Groom();
	public final AIModule_Work work = new AIModule_Work();
	public final AIModule_Battle battle = new AIModule_Battle();
	final AIModule_NeedsForDeed need4deed = new AIModule_NeedsForDeed();
	final AIModule noble = new AIModule_Noble();
	final AIModule slave = new AIModule_Slave();
	final AIModule_Student student = new AIModule_Student();
	public final AIModule_Entertainment entertainment = new AIModule_Entertainment();
	public final AIModule_Exposure exposure = new AIModule_Exposure(hygine);
	private final AIModule_Crime criminal = new AIModule_Crime();
	private final AIModule_Prisoner prisoner = new AIModule_Prisoner();
	private final AIModule_Recruit recruit = new AIModule_Recruit();
	private final AIModule_Child child = new AIModule_Child();
	private final AIModule_Rioter rioter = new AIModule_Rioter();
	private final AIModule_Home home = new AIModule_Home();
	private final AIModule_Retired retired = new AIModule_Retired();
	private final AIModule_Insane insane = new AIModule_Insane();
	private final AIModule_Tourist tourist = new AIModule_Tourist();
	
	private final LIST<AIModule> std = new ArrayList<>(
			hunger,
			drink,
			groom,
			religion,
			health,
			equipment,
			hygine,
			need4deed,
			exposure,
			entertainment
			);
	private final LIST<AIModule> pla = std.join(subject,home);
	
	private final AIModule[][] modules = new AIModule[HTYPE.ALL().size()][];
	/**
	 * Move to crime later
	 */

	private AIManager cooD;
	private final COORDINATEE moduleCoo = new COORDINATEE.Abs() {
		
		private final INT_OE<AIManager> x = AI.data().new DataShort();
		private final INT_OE<AIManager> y = AI.data().new DataShort();
		@Override
		public int y() {
			return (short)y.get(cooD);
		}
		
		@Override
		public int x() {
			return (short)x.get(cooD);
		}
		
		@Override
		public void ySet(double y) {
			this.y.set(cooD, ((int) y)&0x0FFFF);
		}
		
		@Override
		public void xSet(double x) {
			this.x.set(cooD, ((int) x)&0x0FFFF);
		}
	};
	
	public COORDINATEE coo(AIManager d) {
		cooD = d;
		return moduleCoo;
	}
	
	private final Sorter2 sorter = new Sorter2();

	AIModules() {
		
		
		modules[HTYPE.SUBJECT.index()] = make(pla.join(criminal, work, battle)); 
		modules[HTYPE.SLAVE.index()] = make(pla.join(work, slave));
		modules[HTYPE.RETIREE.index()] = make(pla.join(criminal, retired, battle));
		modules[HTYPE.RECRUIT.index()] = make(pla.join(criminal, recruit, battle)); 
		modules[HTYPE.STUDENT.index()] = make(pla.join(criminal, student, battle)); 
		modules[HTYPE.NOBILITY.index()] = make(pla.join(battle, noble));
		
		modules[HTYPE.TOURIST.index()] = make(std.join(tourist));
		
		modules[HTYPE.ENEMY.index()] = new AIModule[] {
			battle
		};
		modules[HTYPE.RIOTER.index()] = new AIModule[] {
			rioter,
		};
		modules[HTYPE.SOLDIER.index()] = new AIModule[] {
			battle
		};

		modules[HTYPE.PRISONER.index()] = new AIModule[] {
			prisoner,
			health,
		};
		
		modules[HTYPE.CHILD.index()] = new AIModule[] {
			child,
			health,
		};
		
		modules[HTYPE.DERANGED.index()] = new AIModule[] {
			insane,
			health,
			exposure,
		};
		
	}
	
	private AIModule[] make(LIST<AIModule> extra) {
		AIModule[] std = new AIModule[extra.size()];
		for (int i = 0; i < extra.size(); i++) {
			std[i] = extra.get(i);
		}
		return std;
	}

	public boolean isCriminal(Humanoid a) {
		return criminal.isCriminal(a);
	}
	
	public void makePrisoner(Humanoid h, AIManager m) {
		if (criminal.catchPrisoner(h))
			prisoner.makePrisoner(h, m);
	}

	void init(Humanoid a, AIManager d) {
		for (AIModule m : modules[a.indu().hType().index()])
			m.init(a, d);
	}
	
	void cancel(Humanoid a, AIManager d) {
		data.nextModule.set(d, 0);
		data.currentModule.set(d, 0);
		data.nextModulePrio.set(d, 0);
		for (AIModule m : modules[a.indu().hType().index()])
			m.cancel(a, d);
	}
	
	static void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int updateI) {
		
		AIModule next = null;
		int hprio = -1;
		
		
		AIModule[] modules = AI.modules().getModules(a, d);
		
		for (AIModule m : modules) {
			m.update(a, d, newDay, byteDelta, updateI);
			int prio = m.getPriority(a, d);
			if (prio > hprio) {
				next = m;
				hprio = prio;
			}
		}
		
		if (next != null)
			data.nextModule.set(d, next.index);
		else
			data.nextModule.set(d, data.currentModule.get(d));
		data.nextModulePrio.set(d, hprio);
		
	}
	
	private AIModule[] getModules(Humanoid a, AIManager d) {
		return modules[a.indu().hType().index()];
	}
	
	AiPlanActivation getNextPlan(Humanoid a, AIManager d) {

		data.nextModule.set(d, 0);
		
		AiPlanActivation p = untrap(a, d);
		if (p != null)
			return p;
		
		
		switchType(a, d);
		
		sorter.init(a, d, getModules(a, d));
		
		AIModule n = sorter.poll();
		while(n != null) {
			
			p = n.getPlan(a, d);
			if (p != null) {
				data.currentModule.set(d, n.index);
				data.nextModule.set(d, n.index);
				data.nextModulePrio.set(d, 0);
				return p;
			}
			n = sorter.poll();
		}
		p = idle.getPlan(a, d);
		data.currentModule.set(d, ((AIModule)idle).index);
		data.nextModule.set(d, ((AIModule)idle).index);
		data.nextModulePrio.set(d, 0);
		return p;
	}
	
	private AiPlanActivation untrap(Humanoid a, AIManager d) {
		if (!PATH().connectivity.is(a.physics.tileC())) {
			if (a.division() != null)
				a.division().reporter.reportReachable(a.divSpot(), false);
			
			if (PATH().comps.zero.get(a.tc()) == null) {
				for (int di = 0; di < DIR.ORTHO.size(); di++) {
					DIR dd = DIR.ORTHO.get(di);
					if (PATH().connectivity.is(a.physics.tileC(), dd)){
						int x = (a.tc().x()+dd.x())*C.TILE_SIZE + C.TILE_SIZEH;
						int y = (a.tc().y()+dd.y())*C.TILE_SIZE + C.TILE_SIZEH;
						a.physics.body().moveC(x, y);
						return null;
					}
				}
				return AI.plans().unreachable.activate(a, d);
			}
			
			if (RND.oneIn(5))
				return AI.plans().unreachable.activate(a, d);
			
		}else {
			STATS.POP().TRAPPED.indu().set(a.indu(), 0);
			if (a.division() != null)
				a.division().reporter.reportReachable(a.divSpot(), true);
		}
		return null;
	}
	
	private void switchType(Humanoid a, AIManager d) {
		if (a.indu().hType() == HTYPE.SUBJECT) {
			if(STATS.POP().shoudRetire(a.indu())) {
				switchType(a, d, HTYPE.RETIREE, null, null);
			}else if(recruit.canBecome(a, d)) {
				switchType(a, d, HTYPE.RECRUIT, null, null);
			}else if(student.tryInit(a, d))
				switchType(a, d, HTYPE.STUDENT, null, null);
		}else if (a.indu().hType() == HTYPE.RETIREE) {
			if(!STATS.POP().shoudRetire(a.indu())) {
				switchType(a, d, HTYPE.SUBJECT, null, null);
			}
		}else if (a.indu().hType() == HTYPE.RECRUIT) {
			if (!recruit.shouldRemain(a, d)) {
				switchType(a, d, HTYPE.SUBJECT, null, null);
			}
			if(STATS.POP().shoudRetire(a.indu())) {
				switchType(a,d, HTYPE.RETIREE, null, null);
			}
		}else if (a.indu().hType() == HTYPE.CHILD) {
			if (STATS.POP().AGE.indu().get(a.indu()) > a.race().physics.adultAt) {
				switchType(a,d, HTYPE.SUBJECT, null, null);
				STATS.RELIGION().setChildReligion(a);
			}
		}else if (a.indu().hType() == HTYPE.STUDENT) {
			if (!AIModule_Student.shouldContinue(a, d)) {
				
				switchType(a,d, HTYPE.SUBJECT, null, null);
			}
		}
	}
	
	private void switchType(Humanoid a, AIManager d, HTYPE type, CAUSE_LEAVE leave, CAUSE_ARRIVE arr) {
		cancel(a, d);
		a.indu().hTypeSet(a, type, leave, arr);
		init(a, d);
	}
	
	public static AIDataModule data() {
		return data;
	}
	
	public static AIModule next(AIManager d) {
		return AIModule.all.get(data.nextModule.get(d));
	}
	
	public static int nextPrio(AIManager d) {
		return data.nextModulePrio.get(d);
	}
	
	public static AIModule current(AIManager d) {
		return AIModule.all.get(data.currentModule.get(d));
	}
	
	public void evictFromRoom(Humanoid a, AIManager d, ROOMA r) {
		for (AIModule m : getModules(a, d))
			m.evictFromRoom(a, d, r);
	}
	
	private final static class Sorter2 {
		
		private final Tree<Node> sorter = new Tree<Node>(20) {

			@Override
			protected boolean isGreaterThan(Node current,
					Node cmp) {
				return current.prio < cmp.prio;
			}
			
		};
		
		private final Node[] nodes = new Node[20];
		
		Sorter2() {
			for (int i = 0; i < nodes.length; i++)
				nodes[i] = new Node();
		}
		
		void init(Humanoid a, AIManager d, AIModule[] modules) {
			sorter.clear();
			int ri = RND.rInt(modules.length);
			for (int i = 0; i < modules.length; i++) {
				AIModule m = modules[(i+ri)%modules.length];
				int prio = m.getPriority(a, d);
				if (prio <= 0)
					continue;
				Node n = nodes[i];
				n.m = m;
				n.prio = prio;
				sorter.add(n);
			}
			
		}
		
		AIModule poll() {
			if (sorter.hasMore())
				return sorter.pollSmallest().m;
			return null;
		}
	
		static class Node {
			
			int prio;
			AIModule m;
			
		}
		
	}

	public class AIDataModule {
		
		private AIManager cooD;
		public final INT_OE<AIManager> byte1;
		public final INT_OE<AIManager> byte2;
		public final INT_OE<AIManager> byte3;
		public final INT_OE<AIManager> x;
		public final INT_OE<AIManager> y;
		
		public final INT_OE<AIManager> nextModule;
		public final INT_OE<AIManager> currentModule;
		private final INT_OE<AIManager> nextModulePrio;


		private AIDataModule(AIData data) {
			byte1 = data.new DataByte();
			byte2 = data.new DataByte();
			byte3 = data.new DataByte();
			x = data.new DataShort();
			y = data.new DataShort();
			nextModule = data.new DataByte();
			currentModule = data.new DataByte();
			nextModulePrio = data.new DataByte();
		}
		
		private final COORDINATEE coo = new COORDINATEE.Abs() {
			
			@Override
			public int y() {
				return (short)y.get(cooD);
			}
			
			@Override
			public int x() {
				return (short)x.get(cooD);
			}
			
			@Override
			public void ySet(double dy) {
				y.set(cooD, (int) dy&0x0FFFF);
			}
			
			@Override
			public void xSet(double dx) {
				x.set(cooD, (int) dx&0x0FFFF);
			}
		};
		
		public COORDINATEE coo(AIManager d) {
			cooD = d;
			return coo;
		}
		
	}

}
