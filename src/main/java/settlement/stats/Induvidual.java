package settlement.stats;

import java.io.IOException;
import java.io.Serializable;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.Faction;
import init.race.RACES;
import init.race.Race;
import settlement.army.Army;
import settlement.army.Div;
import settlement.entity.humanoid.*;
import settlement.entity.humanoid.Humanoid.HumanoidResource;
import settlement.main.SETT;
import settlement.stats.law.PRISONER_TYPE;
import snake2d.util.bit.Bit;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.Tuple;

public final class Induvidual extends HumanoidResource implements Serializable{

	private static final long serialVersionUID = 1L;
	final long[] data;
	long randomness;
	long randomness2;
	private final byte race;
	private byte type;
	private byte da = 0;
	private final static Bit fav = new Bit(0b00000001);
	
	
	public Induvidual(HTYPE type, Race race, CAUSE_ARRIVE a){
		data = new long[STATS.IDataCount()];
		this.randomness =  RND.rLong();
		this.randomness2 = RND.rLong();
		 
		this.race = (byte) race.index;
		this.type = (byte) type.index();
		
		STATS.get().add(this);
		STATS.get().init(this);
		STATS.POP().COUNT.reg(this, a);
		
		if (type == HTYPE.SLAVE) {
			GAME.stats().ENSLAVED.inc(1);
		}else if (type == HTYPE.PRISONER)
			STATS.LAW().prisonerType.set(this, PRISONER_TYPE.WAR);
		
		for (Tuple<STAT, Double> t : race.stats().arrivalStats()) {
			t.a().indu().setD(this, CLAMP.d(RND.rFloat0(0.2)*t.b(), 0, 1));
		}
	}
	
	public void copy(Induvidual other) {
		randomness = other.randomness;
		randomness2 = other.randomness2;
		for (STAT s : STATS.all()) {
			s.indu().set(this, s.indu().get(other));
		}		
	}
	
	public Induvidual(FileGetter p) throws IOException{
		
		data = new long[STATS.IDataCount()];
		p.ls(data);
		randomness = p.l();
		randomness2 = p.l();
		race = p.b();
		type = p.b();
		da = p.b();
		this.randomness2 = RND.rLong();
	}
	
	@Override
	public void save(FilePutter p) {
		p.ls(data);
		p.l(randomness);
		p.l(randomness2);
		p.b(race);
		p.b(type);
		p.b(da);
	}
	

	
	public HTYPE hType() {
		return HTYPE.ALL().get(type&0x0FF);
	}
	
	public HCLASS clas() {
		return HTYPE.ALL().get(type&0x0FF).CLASS;
	}
	
	
	public void hTypeSet(Humanoid h, HTYPE t, CAUSE_LEAVE leave, CAUSE_ARRIVE arr) {
		if (t != hType()) {
			HTYPE old = hType();
			STATS.POP().COUNT.reg(h.indu(), leave);
			SETT.PATH().finders.entity.report(h, -1);
			STATS.WORK().EMPLOYED.set(h, null);
			STATS.HOME().dump(h);
			STATS.HOME().GETTER.set(h, null);
			Div d = STATS.BATTLE().DIV.get(h);
			STATS.BATTLE().DIV.set(h, null);
			
			STATS.get().remove(this);
			this.type = (byte) t.index();
			STATS.get().add(this);
			
			SETT.PATH().finders.entity.report(h, 1);
			STATS.APPEARANCE().changeHtype(this, old);
			if (keepDiv(old) && keepDiv(t)) {
				h.setDivision(d);
			}
			STATS.POP().COUNT.reg(h.indu(), arr);
			if (t == HTYPE.SLAVE) {
				GAME.stats().ENSLAVED.inc(1);
			}else if (old == HTYPE.SLAVE)
				GAME.stats().FREED_SLAVES.inc(1);
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

	@Override
	protected void cancel(Humanoid h) {
		STATS.get().cancel(h);
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
	
	public long randomness() {
		return randomness;
	}
	
	public long randomness2() {
		return randomness2;
	}
	
	public int ran(int scroll) {
		long l1 = randomness;
		long l2 = randomness2;
		scroll = scroll & 127;
		if (scroll >= 64) {
			l2 = randomness;
			l1 = randomness2;
			scroll -= 64;
		}
		
		l1 = l1 >>> scroll;
		l2 = l2 << (63-scroll);
		l1 |= l2;
		return (int) l1;
	}

	public void randomness(long randomness, long randoness2) {
		this.randomness = randomness;
		this.randomness2 = randoness2;
	}
	
	public Faction faction() {
		if (hType().player)
			return FACTIONS.player();
		return FACTIONS.other();
	}


	
	
}
