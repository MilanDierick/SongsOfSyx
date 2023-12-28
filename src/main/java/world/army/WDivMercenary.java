package world.army;

import java.io.IOException;

import game.faction.FACTIONS;
import init.config.Config;
import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCES;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBattle.StatTraining;
import settlement.stats.equip.EquipBattle;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import world.army.util.DivSpec;
import world.army.util.DivType;
import world.regions.data.RD;
import world.regions.data.pop.RDRace;

final class WDivMercenary extends ADDiv {
	
	static final int type = 2;
	
	private byte race = 0;
	private short men;
	private short menTarget;
	private final DivSpec spec = new DivSpec();
	private float exp;
	private short costPerMan;
	private short nameI;
	private short bannerI;
			
	WDivMercenary(int index) {
		super(index);
	}
	
	void randomize() {
		report(-1);
		race = (byte) RACES.all().rnd().index;
		
		double am = 0;
		for (RDRace r : RD.RACES().all) {
			am += r.pop.faction().get(FACTIONS.player()) +1;
		}
		double ri = RND.rFloat()*am;
		for (RDRace r : RD.RACES().all) {
			ri -= r.pop.faction().get(FACTIONS.player())+1;
			if (ri <= 0) {
				race = (byte) r.race.index;
				break;
			}
		}

		
		
		men = (short) CLAMP.i((int) ((0.5 + 0.5*RND.rFloat())*Config.BATTLE.MEN_PER_DIVISION), 1, Config.BATTLE.MEN_PER_DIVISION);
		menTarget = men;
		
		exp = (float) CLAMP.d(Math.pow(RND.rFloat(), 1.5), 0, 1);
		
		
		nameI = (short) RND.rInt(race().info.armyNames.size());
		
		DivType type = AD.UTIL().types.rnd(race(), null, RND.rFloat());
		
		spec.copy(type, 0.1 + 0.9*Math.pow(RND.rFloat(), 1.5), 0.1 + 0.9*Math.pow(RND.rFloat(), 1.5));
		
		bannerSet(RND.rShort());
		
		
		costPerMan = (short) (2 + 0.5*RND.rFloat1(0.2)*100*RESOURCES.ALL().size()*((double)provess()/(100.0*men)));
		
		report(1);
	}
	
	@Override
	public void save(FilePutter file) {
		super.save(file);
		file.b(race);
		file.s(men);
		file.s(menTarget);
		file.f(exp);
		file.s(costPerMan);
		file.s(nameI);
		file.s(bannerI);
		spec.save(file);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		super.load(file);
		race = file.b();
		men = file.s();
		menTarget = file.s();
		exp = file.f();
		costPerMan = file.s();
		nameI = file.s();
		bannerI = file.s();
		spec.load(file);
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
	public void resolve(Induvidual[] hs) {
		double exp = 0;
		for (Induvidual i : hs)
			exp += STATS.BATTLE().COMBAT_EXPERIENCE.indu().getD(i);
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
	public int menTarget() {
		return menTarget;
	}
	
	@Override
	public double training(StatTraining tr) {
		return spec.training[tr.tIndex];
	}
	
	@Override
	public double trainingTarget(StatTraining tr) {
		return training(tr);
	}
	
	@Override
	public double experience() {
		return exp;
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
	public int costPerMan() {
		return costPerMan;
	}
	
	@Override
	public int type() {
		return type;
	}

	
	@Override
	public CharSequence name() {
		return race().info.armyNames.get(nameI);
	}

	@Override
	public int equipTarget(EquipBattle e) {
		return (int) (spec.equip[e.indexMilitary()]*e.max());
	}
	
	@Override
	public double equip(EquipBattle e) {
		return spec.equip[e.indexMilitary()];
	}

	@Override
	public boolean needSupplies() {
		return false;
	}


	@Override
	public WDivGeneration generate() {
		return new WDivGeneration(this);
	}

	@Override
	public int bannerI() {
		return bannerI;
	}

	@Override
	public void bannerSet(int bi) {
		this.bannerI = (short) bi;
	}







	
}
