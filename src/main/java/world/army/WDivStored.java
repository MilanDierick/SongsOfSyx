package world.army;

import java.io.IOException;

import game.faction.FACTIONS;
import game.faction.Faction;
import init.RES;
import init.boostable.BOOSTABLES;
import init.race.Race;
import settlement.army.Div;
import settlement.army.DivisionBanners.DivisionBanner;
import settlement.entity.humanoid.*;
import settlement.main.SETT;
import settlement.stats.CAUSE_ARRIVE;
import settlement.stats.STATS;
import settlement.stats.StatsEquippables.EQUIPPABLE_MILITARY;
import settlement.stats.StatsEquippables.StatEquippableRange;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import util.dic.DicArmy;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import world.World;
import world.army.WINDU.WDivGeneration;
import world.army.WINDU.WInduStored;
import world.entity.army.WArmy;

class WDivStored implements WDIV, SAVABLE{

	final static int type = 1;
	
	private int induviduals = 0;
	private int armyI = -1;
	private final int index;
	private ArrayList<WInduStored> all = new ArrayList<>(RES.config().BATTLE.MEN_PER_DIVISION);
	private int experience;
	private int trainingM;
	private int trainingR;
	
	
	WDivStored(int index){
		this.index = index;
	}
	
	@Override
	public void save(FilePutter file) {
		file.i(armyI);
		file.i(experience);
		file.i(trainingM);
		file.i(trainingR);
		file.i(induviduals);
		
		
		file.i(all.size());
		for (WInduStored s : all)
			s.save(file);
		
	}

	@Override
	public void load(FileGetter file) throws IOException {
		armyI = file.i();
		experience = file.i();
		trainingM = file.i();
		trainingR = file.i();
		induviduals = file.i();
		
		all.clear();
		int am = file.i();
		for (int i = 0; i < am; i++)
			all.add(new WInduStored());
		for (WInduStored s : all)
			s.load(file);
	}

	@Override
	public void clear() {
		induviduals = 0;
		armyI = -1;
		experience = 0;
		trainingM = 0;
		trainingR = 0;
		all.clear();
	}
	
	@Override
	public int equipTarget(EQUIPPABLE_MILITARY e) {
		return e.target(div());
	}
	

	@Override
	public double equip(EQUIPPABLE_MILITARY e) {
		return e.target(div())*WARMYD.supplies().get(e).getD(army());
	}
	
	private Div div() {
		return SETT.ARMIES().player().divisions().get(index);
	}
	
	@Override
	public int men() {
		return induviduals;
	}

	@Override
	public Race race() {
		return div().info.race();
	}

	@Override
	public int menTarget() {
		return SETT.ARMIES().player().divisions().get(index).info.target();
	}

	@Override
	public double training_melee() {
		return trainingM/(induviduals*15.0);
	}
	
	@Override
	public double training_ranged() {
		return trainingR/(induviduals*15.0);
	}

	@Override
	public double experience() {
		return experience/(induviduals*15.0);
	}
	
	@Override
	public CharSequence name() {
		return div().info.name();
	}


	
	public int index() {
		return index;
	}
	
	@Override
	public WArmy army() {
		if (armyI == -1)
			return null;
		return World.ENTITIES().armies.get(armyI);
	}
	
	void armySet(WArmy e) {
		report(-1);
		
		WArmy old = army();
		if (old != null) {
			for (int i = 0; i < old.divs().size(); i++) {
				if (old.divs().get(i) == this) {
					
					old.divs().remove(i);
					break;
				}
			}
		}
		
		armyI = e == null ? -1 : e.armyIndex();
		if (e != null) {
			
			int i = army().divs().add();
			long d = WArmyDivs.BType.set(0, type);
			d |= index;
			army().divs().setData(i, d);
		}
		report(1);
	}
	
	void add(Humanoid indu) {
		add(new WInduStored(indu));
	}
	
	void add(WInduStored n) {
		report(-1);
		induviduals ++;
		experience += WINDU.experience().statSelf.get(n);
		trainingM += WINDU.trainingM().statSelf.get(n);
		trainingR += WINDU.trainingR().statSelf.get(n);
		all.add(n);
		report(1);
	}
	
	private void remove(WInduStored n) {
		report(-1);
		induviduals --;
		experience -= WINDU.experience().statSelf.get(n);
		trainingM -= WINDU.trainingM().statSelf.get(n);
		trainingR -= WINDU.trainingR().statSelf.get(n);
		all.remove(n);
		report(1);
	}

	@Override
	public int daysUntilMenArrives() {
		return 0;
	}

	@Override
	public int amountOfMenThatWillArrive() {
		return STATS.BATTLE().RECRUIT.inDiv(SETT.ARMIES().player().divisions().get(index));
	}
	
	private void report(int d) {
		World.ARMIES().cityDivs().amount += d*induviduals;
		World.ARMIES().cityDivs().ramounts[race().index] += d*induviduals;
		
		WARMYD.register(this, true, 0, 0, d);
	}


	@Override
	public void disband() {
		armySet(null);
	}

	@Override
	public void resolve(WInduStored[] hs) {
		report(-1);
		induviduals = 0;
		all.clear();
		experience = 0;
		trainingM = 0;
		trainingR = 0;
		report(1);
		for (WInduStored h : hs)
			add(h);
	}
	
	@Override
	public void resolve(int surviviors, double experiencePerMan) {
		
		double dExperience = experiencePerMan - experience();
		dExperience*= surviviors;
		ArrayList<WInduStored> all = new ArrayList<>(surviviors);
		for (int i = 0; i < surviviors; i++) {
			WInduStored s = this.all.get(i);
			int a = (int) dExperience;
			if (dExperience - a > RND.rFloat())
				a++;
			WINDU.experience().statSelf.inc(s, a);
			all.add(s);
		}
		report(-1);
		induviduals = 0;
		this.all.clear();
		experience = 0;
		trainingM = 0;
		trainingR = 0;
		report(1);
		for (WInduStored h : all)
			add(h);
	}

	@Override
	public void menSet(int amount) {
		amount = CLAMP.i(amount, 0, men());
		while (amount < men()) {
			WInduStored t = all.get(all.size()-1);
			remove(t);
		}
		while(amount > men()) {
			add(new WInduStored(SETT.ARMIES().player().divisions().get(index)));
		}
	}
	
	public Humanoid popSoldier(int tx, int ty, HTYPE type) {
		WInduStored t = all.get(all.size()-1);
		remove(t);
		Humanoid h = SETT.HUMANOIDS().create(race(), tx, ty, type, CAUSE_ARRIVE.SOLDIER_RETURN);
		if (!h.isRemoved()) {
			t.paste(h);
		}
		return h;
	}

	@Override
	public int type() {
		return type;
	}

	@Override
	public void reassign(WArmy a) {
		WArmy oldA = army();
		double sup = WARMYD.supplies().all.get(0).current().get(army());
		if(sup > 0) {
			sup = menTarget()/sup;
		}
		
		armySet(a);
		
		WARMYD.supplies().transfer(this, oldA, army());
	}

	public void age() {
		
		for (int k = 0; k < all.size(); k++) {
			WInduStored i = all.get(k);
			double age = WINDU.ageDays().statSelf.get(i);
			if (STATS.POP().shouldDieOfOldAge(age, BOOSTABLES.PHYSICS().DEATH_AGE.race(race()))) {
				remove(i);
			}else if (STATS.POP().shoudRetire(HCLASS.CITIZEN, race(),BOOSTABLES.PHYSICS().DEATH_AGE.race(race()), age)) {
				remove(i);
				COORDINATE c = SETT.PATH().entryPoints.rnd();
				if (c != null) {
					Humanoid h = SETT.HUMANOIDS().create(race(), c.x(), c.y(), HTYPE.RETIREE, null);
					if (h != null)
						i.paste(h);
				}
			}
		}
	}
	
	@Override
	public void hover(GBox b) {
		World.ARMIES().hoverer().hover(this, b);
		b.NL(8);
		
		if (men() < menTarget()) {
			
			GText t = b.text();
			
			t.add(DicArmy.¤¤SoldiersAreTraining).insert(0, amountOfMenThatWillArrive());	
			
			b.NL();
			
			b.add(t);
			b.NL();
			
		}
	}

	@Override
	public boolean needSupplies() {
		return true;
	}

	@Override
	public DivisionBanner banner() {
		return SETT.ARMIES().banners.get(div().info.symbolI());
	}

	@Override
	public void bannerSet(int bi) {
		div().info.symbolSet(bi);
	}

	@Override
	public Faction faction() {
		return FACTIONS.player();
	}

	@Override
	public WDivGeneration generate() {
		WDivGeneration res = new WDivGeneration(men());
		
		for (int i = 0; i < men() ; i++) {
			res.indus[i] = new WInduStored(all.get(i));
		}
		
		res.isRange = false;
		
		
		for (EQUIPPABLE_MILITARY m : STATS.EQUIP().military_all()) {
			res.supplies[m.indexMilitary()] = equip(m);
			if (m instanceof StatEquippableRange && res.supplies[m.indexMilitary()] > 0)
				res.isRange = true;
		}
		
		res.name = ""+name();
		res.race = (short) race().index();
		res.bannerI = bannerI();
		
		
		return res;
	}

	@Override
	public int bannerI() {
		return div().info.symbolI();
	}





	
}