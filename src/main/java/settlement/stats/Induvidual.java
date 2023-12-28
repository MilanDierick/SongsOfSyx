package settlement.stats;

import java.io.IOException;
import java.io.Serializable;

import game.GAME;
import game.boosting.*;
import game.faction.FACTIONS;
import game.faction.Faction;
import init.race.*;
import settlement.army.Army;
import settlement.army.Div;
import settlement.entity.humanoid.*;
import settlement.entity.humanoid.Humanoid.HumanoidResource;
import settlement.main.SETT;
import settlement.stats.equip.EquipRange;
import settlement.stats.law.PRISONER_TYPE;
import settlement.stats.stat.STAT;
import settlement.stats.util.CAUSE_ARRIVE;
import settlement.stats.util.CAUSE_LEAVE;
import snake2d.util.bit.Bit;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.Tuple;
import util.data.INT_O.INT_OE;

public final class Induvidual extends HumanoidResource implements Serializable, BOOSTABLE_O{

	private static final long serialVersionUID = 1L;
	final long[] data;
	private final byte race;
	private byte type;
	private byte da = 0;
	private final static Bit fav = new Bit(0b00000001);
	
	private boolean added = false;
	
	public Induvidual(HTYPE type, Race race){
		data = new long[STATS.IDataCount()];
		 
		this.race = (byte) race.index;
		this.type = (byte) type.index();
		
		if (type == HTYPE.SLAVE) {
			GAME.count().ENSLAVED.inc(1);
		}else if (type == HTYPE.PRISONER)
			STATS.LAW().prisonerType.set(this, PRISONER_TYPE.WAR);
		

		STATS.get().init(this);
	}
	
	public void copyFrom(Induvidual other) {
		
		STATS.RAN().copyFrom(this, other);
		for (INT_OE<Induvidual> ii : STATS.APPEARANCE().all) {
			ii.set(this, ii.get(other));
		}
		
		for (int si = 0; si < STATS.all().size(); si++) {
			STAT s = STATS.all().get(si);
			s.indu().set(this, s.indu().get(other));
		}
		STATS.BATTLE().basicTraining.set(this, STATS.BATTLE().basicTraining.get(other));
		for (int ei = 0; ei < STATS.EQUIP().RANGED().size(); ei++) {
			EquipRange r = STATS.EQUIP().RANGED().get(ei);
			if (r.stat().indu().get(this) > 0)
				r.ammunition.indu().setD(this, 1.0);
		}
	}
	
	@Override
	protected void add(Humanoid h, CAUSE_ARRIVE a) {
		if (added)
			return;
		added = true;
		for (Tuple<STAT, Double> t : race().stats().arrivalStats()) {
			t.a().indu().setD(this, CLAMP.d(RND.rFloat0(0.2)*t.b(), 0, 1));
		}
		STATS.get().add(this);
		
		STATS.POP().COUNT.reg(this, a);
	}
	
	@Override
	protected void cancel(Humanoid h) {
		if (!added)
			return;
		STATS.get().cancel(h);
		added = false;
	}
	
	public boolean added() {
		return added;
	}
	
	public Induvidual(FileGetter p) throws IOException{
		data = new long[STATS.IDataCount()];
		int s = p.i();
		if (s != STATS.IDataCount()) {
			long[] da = new long[s];
			p.ls(da);
			for (int i = 0; i < da.length && i < data.length; i++) {
				data[i] = da[i];
			}
			
		}else {
			p.ls(data);
		}
		
		
		race = p.b();
		type = p.b();
		da = p.b();
		added = p.bool();
	}
	
	@Override
	public void save(FilePutter p) {
		p.i(data.length);
		p.ls(data);
		p.b(race);
		p.b(type);
		p.b(da);
		p.bool(added);
	}
	

	
	public HTYPE hType() {
		return HTYPE.ALL().get(type&0x0FF);
	}
	
	public HCLASS clas() {
		return HTYPE.ALL().get(type&0x0FF).CLASS;
	}
	
	public POP_CL popCL() {
		return HTYPE.ALL().get(type&0x0FF).CLASS.get(race());
	}
	
	/**
	 * May only be called
	 * @param h
	 * @param t
	 * @param leave
	 * @param arr
	 */
	public void hTypeSet(Humanoid h, HTYPE t, CAUSE_LEAVE leave, CAUSE_ARRIVE arr) {

		if (t != hType()) {
			HTYPE old = hType();
			STATS.POP().COUNT.reg(h.indu(), leave);
			SETT.PATH().finders.entity.report(h, -1);
			STATS.WORK().EMPLOYED.set(h, null);
			STATS.HOME().dump(h);
			STATS.HOME().GETTER.set(h, null);
			STATS.BATTLE().ROUTING.indu().set(this, 0);
			Div d = STATS.BATTLE().DIV.get(h);
			STATS.BATTLE().DIV.set(h, null);
			
			STATS.get().remove(this);
			this.type = (byte) t.index();
			STATS.get().add(this);
			
			SETT.PATH().finders.entity.report(h, 1);
			if (keepDiv(old) && keepDiv(t)) {
				h.setDivision(d);
			}
			STATS.POP().COUNT.reg(h.indu(), arr);
			if (t == HTYPE.SLAVE) {
				GAME.count().ENSLAVED.inc(1);
			}else if (old == HTYPE.SLAVE)
				GAME.count().FREED_SLAVES.inc(1);
		}
		
	}
	
	private boolean keepDiv(HTYPE t) {
		return t == HTYPE.STUDENT || t == HTYPE.SUBJECT || t == HTYPE.RECRUIT;
	}
	
	public Race race() {
		return RACES.all().get(race);
	}
	
	public boolean isDead() {
		return false;
	}

	@Override
	protected void update(Humanoid h, int updateI, boolean newDay) {
		STATS.update(h, updateI, newDay);
	}

	@Override
	protected void update(Humanoid h, float ds) {
		throw new RuntimeException();
	}


	
	public boolean favorite() {
		return fav.is(da);
	}
	
	public void favSet(boolean fav) {
		if (fav)
			da = (byte) Induvidual.fav.set(da);
		else
			da = (byte) Induvidual.fav.clear(da);
	}
	
	public boolean player() {
		return hType().player;
	}
	
	public boolean hostile() {
		return hType().hostile;
	}
	
	public Army army() {
		return hType().hostile ? SETT.ARMIES().enemy() : SETT.ARMIES().player();
	}
	
	public Div division() {
		return STATS.BATTLE().DIV.get(this);
	}
	
//	public long randomness() {
//		return randomness;
//	}
//	
//	public long randomness2() {
//		return randomness2;
//	}
//	
//	public int ran(int scroll) {
//		long l1 = randomness;
//		long l2 = randomness2;
//		scroll = scroll & 127;
//		if (scroll >= 64) {
//			l2 = randomness;
//			l1 = randomness2;
//			scroll -= 64;
//		}
//		
//		l1 = l1 >>> scroll;
//		l2 = l2 << (63-scroll);
//		l1 |= l2;
//		return (int) l1;
//	}
//
//	public void randomness(long randomness, long randoness2) {
//		this.randomness = randomness;
//		this.randomness2 = randoness2;
//	}
	
	public Faction faction() {
		if (hType().player)
			return FACTIONS.player();
		return FACTIONS.otherFaction();
	}

	@Override
	public double boostableValue(Boostable bo, BValue v) {
		return v.vGet(this);
	}


	
	
}
