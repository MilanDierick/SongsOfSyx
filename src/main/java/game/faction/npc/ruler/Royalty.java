package game.faction.npc.ruler;

import java.io.IOException;
import java.util.Arrays;

import game.VERSION;
import game.boosting.*;
import game.faction.npc.ruler.RTraits.RTrait;
import game.faction.npc.ruler.RTraits.Title;
import game.time.TIME;
import init.race.Race;
import settlement.entity.humanoid.HTYPE;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.colls.StatsNeeds.StatNeed;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.text.Str;

public class Royalty implements BOOSTABLE_O{

	private static int id = 0;
	public final Induvidual induvidual;
	public final NPCCourt court;
	private float[] traitD = new float[RTraits.get().all().size()];
	private ArrayListGrower<Title> traits = new ArrayListGrower<>();
	public final OpinionData data = ROpinions.createData();
	final int deathDay;
	public final int ID;
	
	Royalty(NPCCourt court, Race race){
		ID = id;
		id++;
		induvidual = new Induvidual(HTYPE.NOBILITY, race);
		this.court = court;
		//CharSequence first = race.appearance().types.get(STATS.APPEARANCE().gender.get(induvidual)).names.firstNames.rnd();
		//CharSequence last = race.appearance().lastNamesNoble.rnd();
		//name.clear().add(last);
		for (StatNeed s : STATS.NEEDS().SNEEDS) {
			s.fixMax(induvidual);
		}
		
		int[] tt = new int[RTraits.get().all().size()];
		for (int i = 0; i < tt.length; i++) {
			tt[i] = i;
		}
		for (int i = 0; i < tt.length; i++) {
			int l = RND.rInt(tt.length);
			int k = tt[i];
			tt[i] = tt[l];
			tt[l] = k;
		}
		
		Arrays.fill(traitD, 1);
		
		for (int i = RND.rInt(3); i >= 0; i--) {
			RTrait t = RTraits.get().all().get(tt[i]);
			traitD[t.index()] = (float) (0.05+(0.95*RND.rFloat()));
		}
		
		addTraits();
		int ls = lifespan();
		int min = ls/4;
		int dd = ls-2*min;
		int birthDay = TIME.days().bitsSinceStart()-(min + RND.rInt(dd));
		deathDay = birthDay + min+dd+RND.rInt(min);
		STATS.POP().age.BIRTH_DATE.set(induvidual, birthDay);
	}
	
	int lifespan() {
		return (int) (BOOSTABLES.PHYSICS().DEATH_AGE.get(induvidual.race())*TIME.years().bitConversion(TIME.days()));
	}
	
	private void addTraits() {
		for (RTrait t : RTraits.get().all()) {
			if (traitD[t.index()] < 1)
				traits.add(t.bad);
			else if (traitD[t.index()] > 1)
				traits.add(t.good);
		}
	}
	
	Royalty(NPCCourt court, FileGetter file) throws IOException {
		this.court = court;
		induvidual = new Induvidual(file);
		file.fsE(traitD);
		addTraits();
		deathDay = file.i();
		ID = file.i();
		if (VERSION.versionIsBefore(64, 15))
			;
		else
			data.load(file);
	}
	
	void update(double seconds) {
		ROpinions.update(this, seconds);
	}
	
	void save(FilePutter file) {
		induvidual.save(file);
		file.fsE(traitD);
		file.i(deathDay);
		file.i(ID);
		data.save(file);
	}
	
	public double trait(RTrait t) {
		return traitD[t.index()];
	}
	
	public void ascendThrone(boolean sendMessage) {
		if (isKing())
			return;
		
		court.all.swap(0, court.all.indexOf(this));
		court.king().init();
		
	}
	
	public void kill(boolean sendMessage) {
		court.kill(this);
	}
	
	public boolean isKing() {
		return court.king().roy() == this;
	}
	
	public LIST<Title> traits(){
		return traits;
	}
	
	public CharSequence name() {
		if (isKing())
			return court.king().name;
		return STATS.APPEARANCE().nameLast(induvidual);
	}
	
	public Str nameFull(Str s) {
		if (isKing())
			return court.king().name;
		else
			s.add(STATS.APPEARANCE().nameFirst(induvidual)).s().add(STATS.APPEARANCE().nameLast(induvidual));
		return s;
	}
	
	public int successionI() {
		return court.all.indexOf(this);
	}

	@Override
	public double boostableValue(Boostable bo, BValue v) {
		return v.vGet(this);
	}
	
}
