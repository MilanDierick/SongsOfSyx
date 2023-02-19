package settlement.entity.humanoid.ai.main;

import static settlement.main.SETT.*;

import java.io.IOException;
import java.util.Arrays;

import game.GAME;
import init.C;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.settings.S;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.*;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.Humanoid.HumanoidResource;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.entity.humanoid.spirte.HSprite;
import settlement.path.finder.SPath;
import settlement.stats.CAUSE_ARRIVE;
import settlement.stats.CAUSE_LEAVE;
import snake2d.LOG;
import snake2d.util.datatypes.*;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sprite.text.Str;
import util.gui.misc.GBox;

public final class AIManager extends HumanoidResource implements HAI {

	final long[] longs = new long[AI.data().longCount()];
	
	private transient AISTATE state = null;
	private transient AISUB sub = null;
	private transient AIPLAN plan = null;
	
	private byte stateI = 0;


	/**
	 * Use internally by an AIPLAN. DO not touch
	 */
	byte planResumerByte;
	/**
	 * Free to use
	 */
	public byte planByte1;
	public byte planByte2;
	public byte planByte3;
	public byte planByte4;
	public final ShortCoo planTile = new ShortCoo();
	public int planObject;

	public float X;
	public float Y;

	private int otherEntity = -1;
	public float stateTimer = 0;
	public short subPathByte = 0;
	public byte subPathByte2;
	public byte subByte = 0;

	
	private byte interType = -1;
	private short subInterIndex = -1;
	private byte subByteI = 0;
	private short subPathByteI = 0;
	private byte subPathByte2I;
	
	private byte resource = -1;
	private byte resourceA = 0;
	public final SPath path = new SPath();

	public AIManager(Humanoid a) {
		setPlan(AI.first().activate(a, this), a);
	}
	
	public AIManager(FileGetter file) throws IOException {
		file.ls(longs);
		state = (AISTATE) AI.get(file.i());
		sub = (AISUB) AI.get(file.i());
		plan = (AIPLAN) AI.get(file.i());
		
		stateI = file.b();
		planResumerByte = file.b();
		planByte1 = file.b();
		planByte2 = file.b();
		planByte3 = file.b();
		planByte4 = file.b();
		planTile.load(file);
		planObject = file.i();
		X = file.f();
		Y = file.f();
		otherEntity = file.i();
		stateTimer = file.f();
		subPathByte2 = file.b();
		subByte = file.b();
		subPathByte = file.s();
		
		interType = file.b(); 
		subInterIndex = file.s();
		subByteI = file.b();
		subPathByteI = file.s();
		subPathByte2I = file.b();
		
		resource = file.b();
		resourceA = file.b();
		path.load(file);
		
	}

	@Override
	public void save(FilePutter file) {
		file.ls(longs);
		
		file.i(state.index());
		file.i(sub.index());
		file.i(plan.index());
		
		file.b(stateI);
		file.b(planResumerByte);
		file.b(planByte1);
		file.b(planByte2);
		file.b(planByte3);
		file.b(planByte4);
		planTile.save(file);
		file.i(planObject);
		file.f(X);
		file.f(Y);
		file.i(otherEntity);
		file.f(stateTimer);
		file.b(subPathByte2);
		file.b(subByte);
		file.s(subPathByte);
		
		file.b(interType); 
		file.s(subInterIndex);
		file.b(subByteI);
		file.s(subPathByteI);
		file.b(subPathByte2I);
		
		file.b(resource);
		file.b(resourceA);
		path.save(file);
	}
	
	private boolean setPlan(AiPlanActivation p, Humanoid a) {
		if (p == null) {
			return false;
		}
		
		AIPLAN plan = p.plan();
		AISubActivation sub = p.sub();
		
		if (plan == null || sub == null) {
			throw new RuntimeException("" + plan + " " + sub);
		}
		
		this.plan = plan;
		setSub(sub);
		return true;
	}
	
	private boolean setSub(AISubActivation s) {
		if (s == null) {
			return false;
		}
			
		sub = s.get();
		state = s.state();
		if (state == null)
			throw new RuntimeException(sub.getClass().getName());
		
		return true;
	}

	public void init(Humanoid a) {
		AI.modules().init(a, this);
//		if (!a.stats.isEnemy() && !a.stats.isZombie()) {
//			plan = AI.plans.GoToThrone.activate(a, this);
//			state = sub.resume(a, this);
//			if (state == null) {
//				debug(a, "no good");
//				throw new RuntimeException();
//			}
//			return;
//		}
		setPlan(AI.first().activate(a, this), a);
	}

	
	public void interrupt(Humanoid a, HEvent.HEventData event) {
		
		this.interType = (byte) event.event.ordinal();
		if (this.subInterIndex == -1) {
			
			this.subInterIndex = (short) this.sub.index();
			this.subByteI = this.subByte;
			this.subPathByteI = this.subPathByte;
			this.subPathByte2I = this.subPathByte2;
			this.sub = null;
			this.state = null;
		} else {
			sub.cancel(a, this);
		}
	}

	public void overwrite(Humanoid a, AISubActivation sub) {
		if (!setSub(sub))
			debug(a, "nono");
	}
	
	public void changeType(Humanoid a, HTYPE t, CAUSE_LEAVE leave, CAUSE_ARRIVE arr) {
		AIPLAN plan = AI.plans().NOP;
		sub.cancel(a, this);
		if (subInterIndex != -1) {
			this.subByte = this.subByteI;
			this.subPathByte = this.subPathByteI;
			this.subPathByte2 = this.subPathByte2I;
			sub = (AISUB) AI.get(subInterIndex);
			sub.cancel(a, this);
		}
		this.plan.cancel(a, this);
		this.subInterIndex = -1;
		this.interType = -1;
		AiPlanActivation p = plan.activate(a, this);
		
		AI.modules().cancel(a, this);
		if (!setPlan(p, a)) {
			throw new RuntimeException();
		}
		a.indu().hTypeSet(a, t, leave, arr);
		AI.modules().init(a, this);
	}
	
	public void overwrite(Humanoid a, AIPLAN plan) {
		sub.cancel(a, this);
		if (subInterIndex != -1) {
			this.subByte = this.subByteI;
			this.subPathByte = this.subPathByteI;
			this.subPathByte2 = this.subPathByte2I;
			sub = (AISUB) AI.get(subInterIndex);
			sub.cancel(a, this);
		}
		this.plan.cancel(a, this);
		this.subInterIndex = -1;
		this.interType = -1;
		AiPlanActivation p = plan.activate(a, this);
		
		if (!setPlan(p, a)) {
			newPlan(a);
		}
		
	}
	
	public AISubActivation resumeOtherPlan(Humanoid a, AIPLAN plan) {
		sub.cancel(a, this);
		if (subInterIndex != -1) {
			this.subByte = this.subByteI;
			this.subPathByte = this.subPathByteI;
			this.subPathByte2 = this.subPathByte2I;
			sub = (AISUB) AI.get(subInterIndex);
			sub.cancel(a, this);
		}
		this.plan.cancel(a, this);
		this.subInterIndex = -1;
		this.interType = -1;
		AiPlanActivation p = plan.activate(a, this);
		if (p == null)
			throw new RuntimeException();
		AISubActivation sub = p.sub();
		this.plan = plan;
		return sub;
		
	}
	
	public void overwrite(Humanoid a, AISTATE state) {
		this.state = state;
		if (state == null) {
			throw new RuntimeException();
		}
			
	}




	boolean isInterrupted() {
		return subInterIndex != -1;
	}

	private static class Opti {
		
		private AIPLAN pl;
		private int ri;
		
		private int i;
		
		private long[] times;
		private int[] invokes;
		
		long now;
		@SuppressWarnings("unused")
		public void time(Humanoid a, AIManager d) {
			if (times == null) {
				times = new long[AI.ALL().size()*20];
				invokes = new int[AI.ALL().size()*20];
			}
			
			pl = d.plan;
			ri = d.planResumerByte;
			now = System.currentTimeMillis();
			
		}
		
		@SuppressWarnings("unused")
		public void flush(Humanoid a) {
			
			if (pl instanceof AIPLAN.PLANRES) {
				long t = System.currentTimeMillis()-now;
				if (t > 1) {
					int in = 20*pl.index() + ri;
					times[in] += t;
					invokes[in] ++;
					i++;
					
					if (i > 200) {
						for (int k = 0; k < times.length; k++) {
							if (times[k] > 0) {
								LOG.ln(k + " " + AI.ALL().get(k/20) + " -> " + (k%20) + " " + times[k] + " " + invokes[k] + " " + (double)(times[k])/invokes[k]);
							}
							
						}
						LOG.ln();
						Arrays.fill(times, 0);
						Arrays.fill(invokes, 0);
						i = 0;
					}
				}
			}
			
		}
		
		
	}
	@SuppressWarnings("unused")
	private static Opti opti = new Opti();
	
	@Override
	protected void update(Humanoid a, float ds) {
		
		if (state == null) {
			debug(a, "State!");
		}
		
		state.sprite(a).tick(a, ds);
		if (state.update(a, this, ds))
			return;

		//opti.time(a, this);

		
		setNextState(a, ds);
		//opti.flush(a);
		return;

	}
	
	@Override
	protected void update(Humanoid a, int updateI, boolean newDay) {

		AIModules.update(a, this, newDay, HumanoidResource.byteDelta, (updateI & 0x0FF));
		
		if (state == null) {
			debug(a, "State!");
		}
		return;

	}
	
	private boolean setNextState(Humanoid a, float ds) {

		
		if (stateI != 30)
			stateI++;

		if (subInterIndex != -1) {
			return handleIterruption(a);
		}

		if (sub == null)
			LOG.ln(plan.className + " " + planResumerByte);
		
		state = sub.resume(a, this);
		
		if (plan == null)
			LOG.err("REMOVE this " + sub.name(a, this));
		
		if (state != null) {
			if (stateI == 30) {
				stateI = 0;
				if (!plan.shouldContinue(a, this)) {
//					st.clear();
//					plan.name(a, this, st);
//					plan.cancel(a, this);
//					sub.cancel(a, this);
					
//					if ((subPathByte & 0x0FF) < SFinderRoomService.all().size() && 
//							SFinderRoomService.get(subPathByte) != null 
//							&& SFinderRoomService.get(subPathByte).getReserved(path.destX(), path.destY()) != null 
//							&& SFinderRoomService.get(subPathByte).getReserved(path.destX(), path.destY()).findableReservedIs()) {
//						debug(a, SFinderRoomService.get(subPathByte) + " " + path.toDebugString());
//						LOG.ln("1: " + plan + " " + st + " " + a.id() + path.toDebugString());
//					}
					resourceDrop(a);
					newPlan(a);
				}
				
			}
		}else {
			if (plan == null) {
				LOG.ln(sub + " " + sub.name(a, this));
				debug(a, "WHAT?");
			}
			
//			if (plan instanceof AIPLAN.PLANRES && planResumerByte >= ((AIPLAN.PLANRES)plan).resumers.size()) {
//				LOG.ln(plan + " " + sub + " " + ((AIPLAN.PLANRES)plan).resumers.get(0) + " " + ((AIPLAN.PLANRES)plan).resumers.get(0).index);
//			}
			
			if (!sub.isSuccessful(a, this) || !plan.shouldContinue(a, this)) {
				if (S.get().developer)
					debug(a, "hello " + " s:" + sub.isSuccessful(a, this) + " p:" + plan.shouldContinue(a, this));
				plan.cancel(a, this);
				sub.cancel(a, this);
				resourceDrop(a);
				newPlan(a);
			}else {
				AISubActivation s = plan.resume(a, this);
				if (!setSub(s)) {
					resourceDrop(a);
					newPlan(a);
				}
				
			}
		}
		
		return true;
	}

	private boolean handleIterruption(Humanoid a) {
		state = sub.resume(a, this);
		if (state != null)
			return true;

		sub = (AISUB) AI.get(subInterIndex);
		//inter = (HInterractor) AI.get(subInterInterIndex);
		subInterIndex = -1;
		subByte = subByteI;
		this.subPathByte = this.subPathByteI;
		this.subPathByte2 = this.subPathByte2I;
		byte it = interType;
		interType = -1;
		state = sub.resumeInterrupted(a, this, HEvent.all.get(it));
		
		if (state != null)
			return true;
		
		sub.cancel(a, this);
		if (setSub(plan.resumeFailed(a, this, HEvent.all.get(it))))
			return true;
		
		plan.cancel(a, this);
		sub = null;
		newPlan(a);
		return true;
	}

	private void newPlan(Humanoid a) {
		setPlan(AI.modules().getNextPlan(a, this), a);
	}

	public AIPLAN plan() {
		return plan;
	}

	public AISUB plansub() {
		return sub;
	}

	public AISTATE state() {
		return state;
	}

	@Override
	public RESOURCE resourceCarried() {
		if (resource >= 0)
			return RESOURCES.ALL().get(resource);
		return null;
	}

	public void resourceCarriedSet(RESOURCE r) {
		if (r == null) {
			resource = -1;
			resourceA = 0;
		} else {
			if (resource == r.bIndex()) {
				resourceA++;
			} else {
				resource = (byte) r.bIndex();
				resourceA = 1;
			}
		}
	}

	public void resourceAInc(int a) {
		resourceA += a;
		if (resourceA <= 0)
			resource = -1;
	}
	
	@Override
	public int resourceA() {
		return resourceA;
	}

	public void resourceDrop(Humanoid a) {
		if (resource >= 0 && resourceA > 0) {
			THINGS().resources.create(a.physics.tileC(), resourceCarried(), resourceA);
			resource = -1;
			resourceA = 0;
		}
	}

	public Humanoid otherEntity() {
		ENTITY e = ENTITIES().getByID(otherEntity);
		if (e != null && e instanceof Humanoid)
			return (Humanoid) e;
		otherEntity = -1;
		return null;
	}

	public Humanoid otherEntitySet(Humanoid o) {
		if (o == null)
			otherEntity = -1;
		else
			otherEntity = o.id();
		return o;
	}

	@Override
	protected void cancel(Humanoid a) {
		sub.cancel(a, this);
		
		if (subInterIndex != -1) {
			sub = (AISUB) AI.get(subInterIndex);
			subByte = subByteI;
			this.subPathByte = this.subPathByteI;
			this.subPathByte2 = this.subPathByte2I;
			sub.cancel(a, this);
			subInterIndex = -1;
		}

		plan.cancel(a, this);
		
		AI.modules().cancel(a, this);
		resourceDrop(a);
		
		AiPlanActivation pa = AI.plans().dead.activate(a, this);
		plan = pa.plan();
		AISubActivation ac = pa.sub();
		sub = ac.get();
		state = ac.state();
	}
	
	public void muster(Humanoid a) {
		sub.cancel(a, this);
		
		if (subInterIndex != -1) {
			sub = (AISUB) AI.get(subInterIndex);
			subByte = subByteI;
			this.subPathByte = this.subPathByteI;
			this.subPathByte2 = this.subPathByte2I;
			sub.cancel(a, this);
			subInterIndex = -1;
		}

		plan.cancel(a, this);
		
		resourceDrop(a);
		
		newPlan(a);
	}
	
	public void debug(Humanoid a, CharSequence message) {
		if (S.get().developer) {
			String n = System.getProperty("line.separator");
			String res = message + n;
			res += "id: " + a.id() + " ctile: " + a.physics.tileC().x() + " " + a.physics.tileC().y() + n + " " + ((int)(X)>>C.T_SCROLL) + " " + ((int)(Y)>>C.T_SCROLL);
			res += "Plan: ";
			if (plan == null)
				res += "null" + n;
			else
				res += plan.debug(a, this) + " plantile: " + planTile.x() + " " + planTile.y() + n;
			res += "Sub: " ;
			if (sub == null)
				res += "null" + n;
			else
				res += sub + " " + subByte + n;
			res += "inter: ";
			if (interType == -1)
				res += "null" + n;
			else
				res += HEvent.all.get(interType).name() + n;	
					
			res += "State: " + state + n;
			res += path.toDebugString();
			res += n + "remove: " + a.isRemoved();
			res += "isDead: " + dead;
			res += n + "res: " + resourceCarried();
			res += n + "inter: " + subInterIndex;
			GAME.Notify(res);
		}
		
	}
	
	@Override
	public void getOccupation(Humanoid a, Str string) {
		if (subInterIndex != -1)
			string.add(sub.name(a, this));
		else
			plan().name(a, this, string);
	}
	
	private final static Coo dest = new Coo();
	
	@Override
	public COORDINATE getDestination() {
		if (!AI.SUBS().walkTo.isWalking(this))
			return null;
		dest.set(path.destX(), path.destY());
		return dest;
	}
	
	public SPath path() {
		return path;
	}
	
	public HSprite sprite(Humanoid h) {
		return state().sprite(h);
	}
	
	@Override
	public void hoverInfoSet(Humanoid a, GBox text) {

		if (S.get().developer) {
			text.NL();
			text.text(plan.className);
			if (plan instanceof AIPLAN.PLANRES) {
				text.text(((AIPLAN.PLANRES) plan).getResumer(this).getClass().getName());
				text.add(text.text().add(planResumerByte));
			}else
				LOG.ln("false");
			text.NL();
			text.text(sub.className);
			text.NL();
			text.text(state.className);
			text.NL();
			text.text(interType == -1 ? "" : HEvent.all.get(interType).name());
			text.NL();
			
			if (sub.name(a, this)!= null)
				text.text(sub.name(a, this));
			text.text(state().name());
			text.add(text.text().add(AI.modules().work.suspender.get(this)));
		}
		
	}

	public boolean event(Humanoid h, HEventData e) {
		if (subInterIndex != -1) {
			return sub.event(h, this, e);
		}
		return plan.event(h, this, e);
	}

	public double poll(Humanoid h, HPollData e) {
		if (subInterIndex != -1) {
			return sub.poll(h, this, e);
		}
		return plan.poll(h, this, e);
	}



}
