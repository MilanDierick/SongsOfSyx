package world.army;

import java.io.IOException;

import game.faction.FACTIONS;
import game.faction.Faction;
import init.config.Config;
import init.race.RACES;
import init.race.Race;
import settlement.army.DivisionBanners.DivisionBanner;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.StatsEquippables.EQUIPPABLE_MILITARY;
import settlement.stats.StatsEquippables.StatEquippableRange;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import util.dic.DicRes;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import world.World;
import world.army.WINDU.WDivGeneration;
import world.army.WINDU.WInduStored;
import world.entity.army.WArmy;

final class WDivMercenary implements WDIV {
	
	static final int type = 2;
	
	private byte race = 0;
	private short men;
	private short menTarget;
	private byte trainingM;
	private byte trainingR;
	private float exp;
	private short costPerMan;
	private short nameI;
	private short armyI = -1;
	private final int index;
	private final byte[] equip = new byte[STATS.EQUIP().military_all().size()]; 
	private short bannerI = 0;
			
	WDivMercenary(int index) {
		this.index = index;
	}
	
	void randomize() {
		report(-1);
		race = (byte) RACES.all().rnd().index;
		
		double am = 0;
		for (Race r : RACES.all()) {
			am += FACTIONS.player().kingdom().realm().population().get(r)+1;
		}
		double ri = RND.rFloat()*am;
		for (Race r : RACES.all()) {
			ri -= FACTIONS.player().kingdom().realm().population().get(r)+1;
			if (ri <= 0) {
				race = (byte) r.index;
				break;
			}
		}

		
		
		men = (short) CLAMP.i((int) ((0.5 + 0.5*RND.rFloat())*Config.BATTLE.MEN_PER_DIVISION), 1, Config.BATTLE.MEN_PER_DIVISION);
		menTarget = men;
		
		exp = (float) CLAMP.d(Math.pow(RND.rFloat(), 1.5)*STATS.BATTLE().COMBAT_EXPERIENCE.indu().max(null), 0, 1);
		
		
		nameI = (short) RND.rInt(race().info.armyNames.size());
		
		boolean ranged = RND.oneIn(5);
		
		double trai = Math.pow(RND.rFloat(), 1.5)*STATS.BATTLE().TRAINING_MELEE.indu().max(null);
		if (ranged) {
			trainingR = (byte) (trai*4/5);
			trainingM = (byte) (trai*1/5);
		}else {
			trainingR = (byte) (trai*1/5);
			trainingM = (byte) (trai*5/5);
		}
		
		for (EQUIPPABLE_MILITARY m : STATS.EQUIP().military()) {
			equip[m.indexMilitary()] = (byte) (Math.pow(RND.rFloat(), 1.5)*m.max());
		}
		
		if (ranged) {
			StatEquippableRange i = STATS.EQUIP().ammo().rnd(); 
			equip[i.indexMilitary()] = (byte) (1+RND.rInt(i.max()));
		}
		bannerI = RND.rShort();
		costPerMan = (short) (2 + ((double)provess()/men));
		
		report(1);
	}
	

	public void save(FilePutter file) {
		file.b(race);
		file.s(men);
		file.s(menTarget);
		file.b(trainingM);
		file.b(trainingR);
		file.f(exp);
		file.s(costPerMan);
		file.s(nameI);
		file.s(armyI);
		file.bs(equip);
		file.s(bannerI);
	}


	public void load(FileGetter file) throws IOException {
		race = file.b();
		men = file.s();
		menTarget = file.s();
		trainingM = file.b();
		trainingR = file.b();
		exp = file.f();
		costPerMan = file.s();
		nameI = file.s();
		armyI = file.s();
		file.bs(equip);
		bannerI = file.s();
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

	void clear() {
		armyI = -1;
	}

	@Override
	public int men() {
		return men;
	}
	
	@Override
	public void menSet(int m) {
		report(-1);
		men = (short) CLAMP.i(m, 0, menTarget());
		report(1);
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
		this.exp = (float) CLAMP.d(experiencePerMan, 0, 1);
		report(1);
	}
	
	@Override
	public void disband() {
		armySet(null);
	}
	
	@Override
	public int menTarget() {
		return menTarget;
	}
	
	@Override
	public double training_melee() {
		return (trainingM/(double)STATS.BATTLE().TRAINING_MELEE.indu().max(null));
	}
	
	@Override
	public double training_ranged() {
		return (trainingR/(double)STATS.BATTLE().TRAINING_MELEE.indu().max(null));
	}
	
	@Override
	public double experience() {
		return (exp)/(double)STATS.BATTLE().COMBAT_EXPERIENCE.indu().max(null);
	}
	
	@Override
	public Race race() {
		return RACES.all().get(race & 0x0FF);
	}

	@Override
	public int daysUntilMenArrives() {
		return 1;
	}

	@Override
	public int amountOfMenThatWillArrive() {
		return 10;
	}
	
	@Override
	public WArmy army() {
		if (armyI == -1)
			return null;
		return World.ENTITIES().armies.get(armyI);
	}
	
	private void report(int i){
		WARMYD.register(this, false, 0, costPerMan, i);
	}
	
	@Override
	public void hover(GBox b) {

		World.ARMIES().hoverer().hover(this, b);

		b.NL(8);
		
		b.textL(DicRes.¤¤InitialCost);
		b.tab(3);
		b.add(GFORMAT.i(b.text(), 2*costPerMan*men()));
		b.NL();
		
		b.textL(DicRes.¤¤Upkeep);
		b.tab(3);
		b.add(GFORMAT.i(b.text(), costPerMan*men()));
		b.NL();
		
	}
	
	public int costPerMan() {
		return costPerMan;
	}
	
	@Override
	public int type() {
		return type;
	}

	@Override
	public void reassign(WArmy a) {
		armySet(a);
	}
	
	@Override
	public CharSequence name() {
		return race().info.armyNames.get(nameI);
	}

	@Override
	public int equipTarget(EQUIPPABLE_MILITARY e) {
		return equip[e.indexMilitary()];
	}
	
	@Override
	public double equip(EQUIPPABLE_MILITARY e) {
		return equip[e.indexMilitary()];
	}

	@Override
	public boolean needSupplies() {
		return false;
	}
	
	@Override
	public DivisionBanner banner() {
		return SETT.ARMIES().banners.get(bannerI);
	}
	
	@Override
	public void bannerSet(int bi) {
		this.bannerI = (short) bi;
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
