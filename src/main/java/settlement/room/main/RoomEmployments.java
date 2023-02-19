package settlement.room.main;

import java.io.IOException;
import java.util.Arrays;

import game.GAME;
import init.race.EGROUP;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.main.ROOMS.RoomResource;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import snake2d.LOG;
import snake2d.util.file.*;
import snake2d.util.sets.*;
import util.data.INT;
import util.data.INT_O;

public final class RoomEmployments extends RoomResource{

	private final LIST<RoomEmployment> all;
	private final LIST<RoomEmploymentSimple> allS;
	
	private int needed = 0;
	private int current = 0;
	private int target = 0;
	private int upI = 0;
	private final RaceGroup[] groups = new RaceGroup[EGROUP.ALL().size()];
	private int[] searchIS;
	final RoomEmploymentsPrios prios; 

	RoomEmployments(ROOMS rooms) {
		all = new ArrayList<>(RoomEmployment.WORK);
		allS = new ArrayList<>(RoomEmploymentSimple.WORK_ALL);
		RoomEmployment.WORK.clear();
		searchIS = new int[all.size()];
		
		for (EGROUP g : EGROUP.ALL()) {
			groups[g.index()] = new RaceGroup(g, this);
		}
		
		prios = new RoomEmploymentsPrios(all);
	}

	void changeCurrent(int current, EGROUP g) {
		this.current += current;
		groups[g.index()].change(current);
	}
	
	void changeNeeded(RoomBlueprintIns<?> b, int total) {
		this.needed += total;
	}
	
	void changeTarget(int current, EGROUP g) {
		
		this.target += current;
		groups[g.index()].target += current;
	}


	@Override
	protected void update(float ds) {
		
		groups[upI].update();
		upI++;
		if (upI == groups.length) {
			upI = 0;
			
		}
		
		prios.update(ds);
		
		
	}

	@Override
	protected void save(FilePutter file) {
		file.i(needed);
		file.i(current);
		file.i(target);
		for (RaceGroup g : groups)
			g.save(file);
		file.is(searchIS);
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		needed = file.i();
		current = file.i();
		target = file.i();
		for (RaceGroup g : groups)
			g.load(file);
		file.is(searchIS);
		
		
	}

	@Override
	protected void clear() {
		needed = 0;
		current = 0;
		target = 0;
		for (RaceGroup g : groups)
			g.clear();
		Arrays.fill(searchIS, 0);
		for (RoomEmployment e : all)
			e.setPrioOnSkill();
	}

	public void setWork(Humanoid h) {
		Induvidual i = h.indu();
		
		if (!i.hType().works)
			throw new RuntimeException();
		
		groups[EGROUP.get(i).index()].setWork(h, searchIS);
	}
	
//	public void setWork(Humanoid h, RoomInstance ins) {
//		if (STATS.WORK().EMPLOYED.get(h) != null)
//			throw new RuntimeException();
//		
//		ins.employees().remove();
//		ins.employees().workersEmployed++;
//		ins.employees().add();
//		STATS.WORK().EMPLOYED.set(h, ins);
//	}
	
	public boolean hasWork(Humanoid h) {
		Induvidual i = h.indu();
		
		if (!i.hType().works)
			return false;
		
		return groups[EGROUP.get(i).index()].hasWork(h);
	}
	
//	public void removeWork(RoomInstance ins, Induvidual i) {
//		ins.employees().remove();
//		ins.employees().workersEmployed--;
//		ins.employees().add();
//	}
	
	public LIST<RoomEmployment> ALL(){
		return all;
	}
	
	public LIST<RoomEmploymentSimple> ALLS(){
		return allS;
	}

	public INT NEEDED = new INT() {

		@Override
		public int get() {
			return needed;
		}

		@Override
		public int min() {
			return 0;
		}

		@Override
		public int max() {
			return needed;
		}

	};

	public INT_O<EGROUP> CURRENT = new INT_O<EGROUP>() {

		@Override
		public int get(EGROUP t) {
			if (t == null)
				return current;
			return groups[t.index()].current;
		}
		
		@Override
		public int min(EGROUP t) {
			return 0;
		};
		@Override
		public int max(EGROUP t) {
			return Integer.MAX_VALUE;
		};

	};
	
	public INT_O<EGROUP> TARGET = new INT_O<EGROUP>() {

		@Override
		public int get(EGROUP t) {
			if (t == null)
				return target;
			return groups[t.index()].target;
		}
		@Override
		public int min(EGROUP t) {
			return 0;
		};
		@Override
		public int max(EGROUP t) {
			return Integer.MAX_VALUE;
		};
	};
	

	private static class RaceGroup implements SAVABLE {

		int target;
		int current;
		final IntegerStack possibles;
		private final RoomEmployments es;
		private EGROUP group;

		public RaceGroup(EGROUP group, RoomEmployments es) {
			this.es = es;
			possibles = new IntegerStack(es.all.size());
			this.group = group;
		}

		void change(int current) {
			this.current += current;
		}

		boolean setWork(Humanoid i, int[] searchI) {

			RoomInstance old = STATS.WORK().EMPLOYED.get(i);
			
			if (old != null && old.blueprintI().employment() instanceof RoomEmployment) {
				RoomEmployment ee = (RoomEmployment) old.blueprintI().employment();
				if (ee.current().get(group) > ee.target.group(group)) {
					STATS.WORK().EMPLOYED.set(i, null);
					
				}
				if (old.employees().isOverstaffed()) {
					STATS.WORK().EMPLOYED.set(i, null);
				}
			}

			
			
			if (STATS.WORK().EMPLOYED.get(i) != null)
				return true;

			if (current >= target) {
				return false;
			}
			
			while(!possibles.isEmpty()) {
				RoomEmployment e = es.all.get(possibles.pop());
				if (e.current().get(null) < e.neededWorkers() && e.current().get(group) < e.target.group(group)) {
					possibles.push(e.index());
					return setWork(i,e, searchI);
				}
			}
			
			return false;
			
		}
		
		boolean hasWork(Humanoid i) {
			return current < target;
		}
		
		private boolean setWork(Humanoid i, RoomEmployment e, int[] searchI) {
			
			
			
			int am = e.blueprint().instancesSize();
			
			for (int k = 0; k < am; k++) {
				if (searchI[e.index()] >= am)
					searchI[e.index()] = 0;
				RoomInstance ins = e.blueprint().getInstance(searchI[e.index()]);
				if (ins.active() && ins.employees().employed() < ins.employees().target()) {
					STATS.WORK().EMPLOYED.set(i, ins);
					return true;
				}
				searchI[e.index()] ++;
				
			}
			
			GAME.Notify("oh no!" + e.blueprint().info.name + " " + i.race().info.name + " " + e.target.group(group) + " " + e.current().get(group) + " " + e.current().get(null) + " " + e.neededWorkers());
			for (int ii = 0; ii < am; ii++) {
				RoomInstance ins = e.blueprint().getInstance(ii);
				LOG.ln(ins.employees().employed()  + "  " + ins.employees().target());
			}
			
			return false;
			
		}

		void update() {
			possibles.clear();
			for (RoomEmployment p : SETT.ROOMS().employment.all) {
				if (p.current().get(null) < p.neededWorkers() && p.current().get(group) < p.target.group(group)) {
					possibles.push(p.index());
				}
			}
		}

		@Override
		public void save(FilePutter file) {
			file.i(current);
			file.i(target);
		}

		@Override
		public void load(FileGetter file) throws IOException {
			current = file.i();
			target = file.i();
			possibles.clear();
		}

		@Override
		public void clear() {
			current = 0;
			target = 0;
			possibles.clear();
			
		}

	}
	
	private final static class TargeterGeneral2 {


		private final ArrayList<Node> nodesTmp;
		private final Tree<Node> nodesTree;
		private ArrayList<Node> nodes;
		int available;

		private int[] allocated = new int[EGROUP.ALL().size()];
		private final ArrayList<EGROUP> gTmp;
		
		TargeterGeneral2(LIST<RoomEmployment> all){
			nodes = new ArrayList<>(all.size());
			nodesTmp = new ArrayList<>(all.size());
			for (RoomEmployment e : all)
				nodes.add(new Node(e));
			
			nodesTree = new Tree<Node>(all.size()) {
				@Override
				protected boolean isGreaterThan(Node current, Node cmp) {
					return current.prio > cmp.prio; 
				}
			};
			
			gTmp = new ArrayList<>(EGROUP.ALL().size());
		
			
		}
		
		void init() {
			

			nodesTree.clear();
			Arrays.fill(allocated, 0);
			for (Node n : nodes) {
				n.clear();
				
				nodesTree.add(n);
			}

			available = STATS.WORK().workforce();
			
			while(available > 0 && nodesTree.hasMore()) {
				{
					Node n = nodesTree.pollGreatest();
					nodesTmp.clearSloppy();
					nodesTmp.add(n);
					if (nodesTree.hasMore() && nodesTree.greatest().prio == n.prio) {
						nodesTmp.add(nodesTree.pollGreatest());
					}
				}
				double needed = 0;
				for (Node n : nodesTmp) {
					needed += n.neededWorkers();
				}
				double d = available/needed;
				if (needed == 0)
					d = 0;
				for (Node n : nodesTmp) {
					int am = (int) Math.ceil(n.neededWorkers()*d);
					if (am > n.neededWorkers())
						am = n.neededWorkers();
					if (am > available)
						am = available;
					available -= am;
					n.target = am;
					
				}
				
			}
			
			nodesTree.clear();
			for (Node n : nodes) {
				n.prio = getPriority(n);
				
				
				nodesTree.add(n);
			}
			
		}
		
		private int getPriority(Node n) {
			int hi = -10;
			for (EGROUP g : EGROUP.ALL()) {
				if (STATS.WORK().workforce(g) - allocated[g.index()] > 0)
					hi = Math.max(hi, n.e.priorities.get(g));
			}
			return hi;
		}
		
		private Node getNext() {
			if (!nodesTree.hasMore())
				return null;
			
			while(true) {
				Node n = nodesTree.pollGreatest();
				int p = getPriority(n);
				if (p == n.prio) {
					n.prio = p;
					return n;
				}
				n.prio = p;
				nodesTree.add(n);
			}
		}
		
		boolean allocate() {

			Node best = getNext();
			if (best == null)
				return false;
			while(allocate(best)) {
				best = getNext();
			}
			return true;
			
		}
		
		private boolean allocate(Node best) {
			gTmp.clear();
			double workersTotal = 0;
			for (EGROUP g : EGROUP.ALL()) {
				if (best.prio == best.e.priorities.get(g)) {
					double w = STATS.WORK().workforce(g) - allocated[g.index()];
					if (w > 0) {
						workersTotal+= w;
						gTmp.add(g);
					}
				}	
			}
			
			if (best.target == 0 || workersTotal <= 0 || best.prio == 0) {
				best.e.target.clear();
				for (EGROUP g : EGROUP.ALL()) {
					best.e.target.add(g, best.allocated[g.index()]);
				}
				return false;
			}

			int needed = best.target-best.allocatedTotal;
			double d = needed / workersTotal;
			
			for (EGROUP g : gTmp) {
				
				int workers = STATS.WORK().workforce(g) - allocated[g.index()];
				int w = (int) Math.ceil(d*workers);
				if (w > workers)
					w = workers;
				if (w > needed)
					w = needed;
				
				allocated[g.index()] += w;
				
				best.allocated[g.index()] += w;
				best.allocatedTotal += w;
				needed -= w;
			}
			
			if (needed == 0) {
				best.e.target.clear();
				for (EGROUP g : EGROUP.ALL()) {
					best.e.target.add(g, best.allocated[g.index()]);
				}
				return false;
			}else {
				best.prio = getPriority(best);
				nodesTree.add(best);
			}
			return true;
			
		}

		private static class Node {
			
			final RoomEmployment e;
			final int[] allocated = new int[EGROUP.ALL().size()];
			int allocatedTotal;
			int prio;
			int target;
			
			Node(RoomEmployment e){
				this.e = e;
			}
			
			void clear() {
				Arrays.fill(allocated, 0);
				allocatedTotal = 0;
				prio = e.priority.get();
				target = 0;
			}
			
			int neededWorkers() {
				if (prio == 0)
					return 0;
				return e.neededWorkers();
			}
			
		}
		
	}
	
	final static class RoomEmploymentsPrios {


		private final TargeterGeneral2 general;
		private int updateI = 0;
		private boolean updateAll = false;
		private double upD = 0;
		
		RoomEmploymentsPrios(LIST<RoomEmployment> all){
			general = new TargeterGeneral2(all);
		}
		
		void update(double ds) {
			if (updateAll) {
				updateI = 0;
				general.init();
				while(general.allocate());
				updateAll = false;
				return;
			}
			
			upD += ds;
			if (upD < 0.25)
				return;
			upD -= 1;
			
			if (updateI == 0) {
				general.init();
				updateI++;
			}else {
				if (!general.allocate())
					updateI = 0;
				
				
			}
		}
		
		void updateAll() {
			updateAll = true;
		}

	}
	
}
