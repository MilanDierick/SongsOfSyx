package world.army;

import java.io.IOException;
import java.util.Arrays;

import game.faction.Faction;
import init.config.Config;
import init.race.RACES;
import init.race.Race;
import settlement.main.SETT;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBattle.StatTraining;
import settlement.stats.equip.EquipBattle;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;
import util.dic.DicArmy;
import util.dic.DicGeo;
import world.army.util.DivType;
import world.entity.army.WArmy;

public final class WDivRegional extends ADDiv {

	public static int DAYS_TO_TRAIN = 3;
	
	static final int type = 0;

	private short men;
	private short menTarget;
	private short ri;
	private final float[] training = new float[STATS.BATTLE().TRAINING_ALL.size()];
	private final byte[] trainingTarget = new byte[STATS.BATTLE().TRAINING_ALL.size()];
	private float experience;
	private byte trainingDay;
	private short bannerI;
	private final byte[] targets = new byte[STATS.EQUIP().BATTLE_ALL().size()];

	
	WDivRegional(int index){
		super(index);
	}
	
	public void init(Race race, double amount, WArmy a) {

		
		
		menTarget = (short) CLAMP.i((int)Math.ceil(amount*Config.BATTLE.MEN_PER_DIVISION), 0, Config.BATTLE.MEN_PER_DIVISION);

		men = 0;
		
		
		ri = (short) race.index;
		experience = 0;
		trainingDay = 0;
		Arrays.fill(targets, (byte)0);
		
		reassign(a);
	}
	
	public void randomize(double training, double gear) {
		report(-1);
		
		
		DivType type = AD.UTIL().types.rnd(race(), faction(), RND.rFloat());

		for (EquipBattle m : STATS.EQUIP().BATTLE_ALL()) {
			targets[m.indexMilitary()] = (byte) CLAMP.d(type.equip(m)*gear*m.max(), 0, m.max());
		}
		
		for (StatTraining t : STATS.BATTLE().TRAINING_ALL) {
			this.training[t.tIndex] = (byte) CLAMP.d(type.training(t)*training*StatTraining.MAX, 0, StatTraining.MAX);
		}
		
		bannerSet(RND.rInt(SETT.ARMIES().banners.size()));

		report(1);
	}
	
	@Override
	public void save(FilePutter file) {
		super.save(file);
		file.s(men);
		file.s(menTarget);
		file.s(ri);
		file.bs(trainingTarget);
		file.fs(training);
		file.f(experience);
		file.b(trainingDay);
		file.s(bannerI);
		file.bs(targets);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		super.load(file);
		men = file.s();
		menTarget = file.s();
		ri = file.s();
		file.bs(trainingTarget);
		file.fs(training);
		experience = file.f();
		trainingDay = file.b();
		bannerI = file.s();
		file.bs(targets);
	}


	@Override
	public int men() {
		return men;
	}

	@Override
	public int menTarget() {
		return menTarget;
	}
	
	public void menTargetSet(int am) {
		report(-1);
		menTarget = (short) CLAMP.i(am, 0, Config.BATTLE.MEN_PER_DIVISION);
		trainingDay = 0;
		men = (short) CLAMP.i(men, 0, menTarget);
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
	public double training(StatTraining tr) {
		return training[tr.tIndex]*StatTraining.MAXI;
	}
	
	public void trainingSet(StatTraining tr, double am) {
		report(-1);
		training[tr.tIndex] = (float) (StatTraining.MAX*CLAMP.d(am, 0, 1));
		report(1);
	}
	
	@Override
	public double trainingTarget(StatTraining tr) {
		return trainingTarget[tr.tIndex]*StatTraining.MAXI;
	}
	
	public void trainingTargetSet(StatTraining tr, double am) {
		trainingTarget[tr.tIndex] = (byte) Math.round(StatTraining.MAX*CLAMP.d(am, 0, 1));
	}


	@Override
	public int equipTarget(EquipBattle e) {
		return targets[e.indexMilitary()];
	}
	
	@Override
	public double equip(EquipBattle e) {
		return targets[e.indexMilitary()]*AD.supplies().get(e).getD(army());
	}
	
	public void equipTargetset(EquipBattle e, int t) {
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
		return DAYS_TO_TRAIN-trainingDay;
	}

	

	@Override
	public void disband() {
		reassign(null);
	}



	public void updateDay() {
		
		if (men() < menTarget() && army().acceptsSupplies()) {
			int ava = AD.conscripts().canTrainI(race(), faction());
			if (ava > 0) {
				trainingDay ++;
				if (trainingDay >= DAYS_TO_TRAIN) {
					trainingDay = 0;
					int men = menTarget()-men();
					if (faction() != null) {
						men = CLAMP.i(men, 0, ava);
					}
					if (men > 0) {
						double vv = 1.0 - men()/(men()+men);
						menSet(men()+men);
						report(-1);
						experience*= vv;
						for (int ti = 0; ti < training.length; ti++) {
							training[ti] = (float) CLAMP.d(training[ti]*vv, 0, StatTraining.MAX);
						}
						report(1);
						return;
					}
				}
			}else {
				trainingDay = 0;
			}
			
		}
		
		for (int ti = 0; ti < STATS.BATTLE().TRAINING_ALL.size(); ti++) {
			StatTraining st =  STATS.BATTLE().TRAINING_ALL.get(ti);
			double tr = training[ti];
			double ta = trainingTarget[ti];
			if (tr < ta) {
				double n = tr + StatTraining.MAX*0.75*st.room.bonus().get(faction())/st.room.TRAINING_DAYS;
				
				n = CLAMP.d(n, 0, ta);
				report(-1);
				training[ti] = (float) n;
				
				report(1);
				return;
			}
		}
		
	}
	
	public static int trainingDays(StatTraining tr, double delta, Faction faction) {
		if (delta <= 0)
			return 0;
		return (int) Math.ceil(delta*tr.room.TRAINING_DAYS/(0.75*tr.room.bonus().get(faction)));
	}
	
	public static double tD(StatTraining tr, double delta, Faction faction) {

		return delta*tr.room.TRAINING_DAYS/(0.75*tr.room.bonus().get(faction));
	}
	
	@Override
	public int type() {
		return type;
	}



	@Override
	public CharSequence name() {
		return Str.TMP.clear().add(DicGeo.¤¤Regional).insert(0, DicArmy.¤¤Division);
	}



	@Override
	public boolean needSupplies() {
		return true;
	}
	


	@Override
	public WDivGeneration generate() {
		return new WDivGeneration(this);
	}
	
	@Override
	public boolean needConscripts() {
		return true;
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
