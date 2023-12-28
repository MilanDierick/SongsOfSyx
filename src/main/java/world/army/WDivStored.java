package world.army;

import java.io.IOException;
import java.util.Arrays;

import init.config.Config;
import init.race.Race;
import settlement.army.Div;
import settlement.entity.humanoid.HTYPE;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBattle.StatTraining;
import settlement.stats.equip.Equip;
import settlement.stats.equip.EquipBattle;
import settlement.stats.stat.STAT;
import settlement.stats.util.CAUSE_ARRIVE;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import world.WORLD;

class WDivStored extends ADDiv {

	final static int type = 1;
	
	private int[] stats = new int[STATS.all().size()];
	private ArrayList<Induvidual> all = new ArrayList<>(Config.BATTLE.MEN_PER_DIVISION);
	
	WDivStored(int index){
		super(index);
	}
	

	@Override
	public void save(FilePutter file) {
		super.save(file);
		file.is(stats);
		
		file.i(all.size());
		for (Induvidual s : all)
			s.save(file);
		
	}

	@Override
	public void load(FileGetter file) throws IOException {
		super.load(file);
		file.is(stats);
		
		all.clear();
		int am = file.i();
		for (int i = 0; i < am; i++)
			all.add(new Induvidual(file));
	}

	void clear() {
		Arrays.fill(stats, 0);
		all.clear();
	}
	
	@Override
	public int equipTarget(EquipBattle e) {
		return e.target(div());
	}
	

	@Override
	public double equip(EquipBattle e) {
		return e.target(div())*AD.supplies().get(e).getD(army());
	}
	
	private Div div() {
		return SETT.ARMIES().player().divisions().get(index);
	}
	
	@Override
	public int men() {
		return all.size();
	}

	@Override
	public Race race() {
		return div().info.race();
	}

	@Override
	public int menTarget() {
		return SETT.ARMIES().player().divisions().get(index).info.men();
	}

	@Override
	public double training(StatTraining tr) {
		return stat(tr);
	}
	
	@Override
	public double trainingTarget(StatTraining tr) {
		return div().info.trainingD(tr.room).getD();
	}
	
	public double stat(STAT stat) {
		if (all.size() == 0)
			return 0;
		return (double)stats[stat.index()] / (stat.indu().max(null)*all.size());
	}

	@Override
	public double experience() {
		return stat(STATS.BATTLE().COMBAT_EXPERIENCE);
	}
	
	@Override
	public CharSequence name() {
		return div().info.name();
	}


	
	public int index() {
		return index;
	}
	
	void add(Humanoid indu) {
		add(indu.indu());
	}
	
	void add(Induvidual n) {
		report(-1);
		if (all.size() == 0)
			Arrays.fill(stats, 0);
		
		for (int si = 0; si < STATS.all().size(); si++) {
			stats[si] += STATS.all().get(si).indu().get(n);
		}
		
		all.add(n);
		
		report(1);
	}
	
	private void remove(Induvidual n) {
		report(-1);
		for (int si = 0; si < STATS.all().size(); si++) {
			stats[si] -= STATS.all().get(si).indu().get(n);
		}
		all.remove(n);
		report(1);
	}

	@Override
	public int daysUntilMenArrives() {
		return 0;
	}
	
	@Override
	protected void report(int d) {
		WORLD.ARMIES().cityDivs().amount += d*all.size();
		WORLD.ARMIES().cityDivs().ramounts[race().index] += d*all.size();
		super.report(d);

	}


//	@Override
//	public void disband() {
//		armySet(null);
//	}

	@Override
	public void resolve(Induvidual[] hs) {
		report(-1);
		clear();
		report(1);
		for (Induvidual i : hs)
			add(i);
	}
	
	@Override
	public void resolve(int surviviors, double experiencePerMan) {

		double dExperience = experiencePerMan - experience();
		dExperience*= surviviors;
		ArrayList<Induvidual> all = new ArrayList<>(surviviors);
		for (int i = 0; i < surviviors; i++) {
			Induvidual s = this.all.get(i);
			int a = (int) dExperience;
			if (dExperience - a > RND.rFloat())
				a++;
			STATS.BATTLE().COMBAT_EXPERIENCE.indu().inc(s, a);
			all.add(s);
		}
		report(-1);
		this.all.clear();
		report(1);
		for (Induvidual h : all)
			add(h);
	}

	@Override
	public void menSet(int amount) {
		amount = CLAMP.i(amount, 0, men());
		while (amount < men()) {
			Induvidual t = all.get(all.size()-1);
			remove(t);
		}
		while(amount > men()) {
			Induvidual i = new Induvidual(HTYPE.SUBJECT, race());
			add(i);
		}
	}
	
	public Humanoid popSoldier(int tx, int ty, HTYPE type) {
		Induvidual t = all.get(all.size()-1);
		remove(t);
		for (Equip e : STATS.EQUIP().allE()) {
			e.set(t, 0);
		}
		
		STATS.NEEDS().INJURIES.count.set(t, 0);
		
		Humanoid h = SETT.HUMANOIDS().create(t.race(), tx, ty, type, CAUSE_ARRIVE.SOLDIER_RETURN);
		
		if (!h.isRemoved()) {
			h.indu().copyFrom(t);
		}
		return h;
	}

	@Override
	public int type() {
		return type;
	}



	public void age() {
		
		
		for (int k = 0; k < all.size(); k++) {
			Induvidual i = all.get(k);
			if (STATS.POP().age.shouldDieOfOldAge(i)) {
				remove(i);
			}else if (STATS.POP().age.shoudRetire(i)) {
				remove(i);
				COORDINATE c = SETT.ENTRY().points.randomReachable();
				if (c != null) {
					Humanoid h = SETT.HUMANOIDS().create(i.race(), c.x(), c.y(), HTYPE.RETIREE, CAUSE_ARRIVE.SOLDIER_RETURN);
					if (h != null)
						h.indu().copyFrom(i);
				}
			}
		}
	}

	@Override
	public boolean needSupplies() {
		return true;
	}


	@Override
	public int bannerI() {
		return div().info.symbolI();
	}
	
	@Override
	public void bannerSet(int bi) {
		div().info.symbolSet(bi);
	}

	@Override
	public WDivGeneration generate() {
		WDivGeneration res = new WDivGeneration(this, all);
		return res;
	}






	
}