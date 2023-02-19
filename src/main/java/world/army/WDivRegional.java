package world.army;

import java.io.IOException;
import java.util.Arrays;

import game.faction.Faction;
import init.RES;
import init.race.RACES;
import init.race.Race;
import settlement.army.DivisionBanners.DivisionBanner;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.StatsEquippables.EQUIPPABLE_MILITARY;
import settlement.stats.StatsEquippables.StatEquippableRange;
import snake2d.util.file.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;
import util.dic.DicArmy;
import util.dic.DicGeo;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import world.World;
import world.army.WINDU.WDivGeneration;
import world.army.WINDU.WInduStored;
import world.entity.army.WArmy;

public final class WDivRegional implements WDIV, SAVABLE{

	static final int type = 0;
	final int index;
	private short men;
	private short menTarget;
	private short armyI;
	private short ri;
	private byte trainingM;
	private byte trainingR;
	private float experience;
	private byte daysUntilNextArrival;
	private byte[] targets = new byte[STATS.EQUIP().military_all().size()];
	private short bannerI;
	
	WDivRegional(int index){
		this.index = index;
	}
	
	public void init(Race race, double amount, int training, int trainingR, WArmy a) {

		menTarget = (short) CLAMP.i((int)Math.ceil(amount*RES.config().BATTLE.MEN_PER_DIVISION), 0, RES.config().BATTLE.MEN_PER_DIVISION);
		this.trainingM = (byte) CLAMP.i(training, 0, 15);
		men = 0;
		armyI = a.armyIndex();
		ri = (short) race.index;
		this.trainingR = (byte) CLAMP.i(trainingR, 0, 15);
		experience = 0;
		daysUntilNextArrival = (byte) timer(training, trainingR);
		Arrays.fill(targets, (byte)0);
		armyI = -1;
		reassign(a);
	}
	
	public void randomize(double gear, int training) {
		report(-1);
		boolean ranged = RND.oneIn(5);
		
		for (EQUIPPABLE_MILITARY m : STATS.EQUIP().military()) {
			if (ranged) {
				targets[m.indexMilitary()] = (byte) CLAMP.d(gear*RND.rFloat()*m.max(), 0, m.max());
			}else {
				targets[m.indexMilitary()] = (byte) CLAMP.d(gear*RND.rFloat1(0.2)*m.max(), 0, m.max());
			}
		}
		
		if (ranged) {
			StatEquippableRange a = STATS.EQUIP().ammo().rnd();
			targets[a.indexMilitary()] = (byte) (1 + RND.rInt(a.max()));
		}else {
			for (StatEquippableRange a : STATS.EQUIP().ammo()) {
				targets[a.indexMilitary()] = 0;
			}
		}
		
		training = CLAMP.i(training, 1, STATS.BATTLE().COMBAT_EXPERIENCE.indu().max(null));
		
		if (ranged) {
			trainingM = (byte) CLAMP.d(training*RND.rFloat(), 1, STATS.BATTLE().COMBAT_EXPERIENCE.indu().max(null));
			trainingR = (byte) CLAMP.d(training*RND.rFloat1(0.2), 1, STATS.BATTLE().COMBAT_EXPERIENCE.indu().max(null));
		}else {
			trainingM = (byte) CLAMP.d(training*RND.rFloat1(0.2), 1, STATS.BATTLE().COMBAT_EXPERIENCE.indu().max(null));
			trainingR = (byte) 0;
		}
		bannerI = (short) RND.rInt(SETT.ARMIES().banners.size());
		report(1);
	}
	
	@Override
	public void save(FilePutter file) {
		file.s(men);
		file.s(menTarget);
		file.s(armyI);
		file.s(ri);
		file.b(trainingM);
		file.b(trainingR);
		file.f(experience);
		file.b(daysUntilNextArrival);
		file.bs(targets);
		file.s(bannerI);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		men = file.s();
		menTarget = file.s();
		armyI = file.s();
		ri = file.s();
		trainingM = file.b();
		trainingR = file.b();
		experience = file.f();
		daysUntilNextArrival = file.b();
		file.bs(targets);
		bannerI = file.s();
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int men() {
		return men;
	}

	@Override
	public int menTarget() {
		return menTarget;
	}

	@Override
	public void resolve(WInduStored[] hs) {
		double exp = 0;
		for (WInduStored i : hs)
			exp += WINDU.experience().statSelf.getD(i);
		if (hs.length > 0)
			exp /= hs.length;
		resolve(hs.length, exp);
	}
	
	@Override
	public void resolve(int surviviors, double experiencePerMan) {
		menSet(surviviors);
		report(-1);
		this.experience = (float) CLAMP.d(experiencePerMan, 0, 1);
		report(1);
	}
	
	@Override
	public void menSet(int amount) {
		report(-1);
		double exp = experience*men;
		men = (short) amount;
		experience = 0;
		if (men > 0)
			experience = (float) CLAMP.d(exp/men, 0, 1);
		report(1);
	}

	@Override
	public Race race() {
		return RACES.all().get(ri);
	}
	
	@Override
	public double training_melee() {
		return (double)trainingM/STATS.BATTLE().TRAINING_MELEE.indu().max(null);
	}
	
	@Override
	public double training_ranged() {
		return (double)trainingR/STATS.BATTLE().TRAINING_MELEE.indu().max(null);
	}

	@Override
	public int equipTarget(EQUIPPABLE_MILITARY e) {
		return targets[e.indexMilitary()];
	}
	
	@Override
	public double equip(EQUIPPABLE_MILITARY e) {
		return targets[e.indexMilitary()]*WARMYD.supplies().get(e).getD(army());
	}
	
	public void equipTargetset(EQUIPPABLE_MILITARY e, int t) {
		report(-1);
		targets[e.indexMilitary()] = (byte) t;
		report(1);
	}

	@Override
	public double experience() {
		return (double)experience/STATS.BATTLE().COMBAT_EXPERIENCE.indu().max(null);
	}

	@Override
	public int daysUntilMenArrives() {
		return daysUntilNextArrival;
	}

	@Override
	public int amountOfMenThatWillArrive() {
		return CLAMP.i(50, 0, menTargetActual()-men());
	}
	
	public int menTargetActual() {
		return (int) Math.ceil(menTarget()*WARMYD.penalty(army().faction(), race()));
	}
	

	@Override
	public void disband() {
		reassign(null);
	}

	@Override
	public void hover(GBox b) {
		World.ARMIES().hoverer().hover(this, b);
		
		b.NL(8);
		if (army().acceptsSupplies() && men() < menTargetActual()) {
			
			GText t = b.text();
			
			t.add(DicArmy.¤¤SoldiersAreTraining).insert(0, amountOfMenThatWillArrive());	
			
			b.NL();
			
			b.add(t);
			b.NL();
			
		}else if(men() < menTarget()){
			b.error(DicArmy.¤¤NotRecruiting);
		}
	}

	public static int timer(int training, int trainingR) {
		return 2 + 2*(training+trainingR);
	}
	
	public static int trainingDays(int training,  int trainingR, int men) {
		return (int) (timer(training, trainingR)*Math.ceil(men / 50.0));
	}
	
	void timerInc(int inc) {
		
		daysUntilNextArrival += inc;
		
	}
	
	void timerReset() {
		
		daysUntilNextArrival = (byte) timer(trainingM, trainingR);
		
	}
	
	@Override
	public int type() {
		return type;
	}

	@Override
	public void reassign(WArmy a) {
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
		
		armyI = a == null ? -1 : a.armyIndex();
		if (a != null) {
			if (a != army()) {
				throw new RuntimeException(a + " " + army() + " " + a.armyIndex() + " " + a.faction());
			}
			int i = army().divs().add();
			long d = WArmyDivs.BType.set(0, type());
			d |= index;
			army().divs().setData(i, d);
		}
		report(1);
		WARMYD.supplies().transfer(this, old, army());
	}

	@Override
	public CharSequence name() {
		return Str.TMP.clear().add(DicGeo.¤¤Regional).insert(0, DicArmy.¤¤Division);
	}

	@Override
	public WArmy army() {
		if (armyI == -1)
			return null;
		return World.ENTITIES().armies.get(armyI);
	}
	
	private void report(int i) {
		if (army() == null)
			return;
		WARMYD.register(this, true, menTarget(), 0, i);
	}

	@Override
	public boolean needSupplies() {
		return true;
	}
	
	@Override
	public void bannerSet(int bi) {
		this.bannerI = (short) bi;
	}
	
	@Override
	public DivisionBanner banner() {
		return SETT.ARMIES().banners.get(bannerI);
	}

	@Override
	public Faction faction() {
		if (army() == null)
			return null;
		return army().faction();
	}

	@Override
	public WDivGeneration generate() {
		return WINDU.generate(this, army());
	}

	@Override
	public int bannerI() {
		return bannerI;
	}

	
}
