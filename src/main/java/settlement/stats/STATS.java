package settlement.stats;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import game.Profiler;
import game.time.TIME;
import game.values.GVALUES;
import init.D;
import init.paths.PATH;
import init.race.RACES;
import init.race.Race;
import settlement.entity.humanoid.HCLASS;
import settlement.entity.humanoid.Humanoid;
import settlement.main.CapitolArea;
import settlement.main.SETT.SettResource;
import settlement.stats.StatsInit.*;
import settlement.stats.colls.*;
import settlement.stats.equip.EquipBattle;
import settlement.stats.equip.StatsEquip;
import settlement.stats.law.StatsLaw;
import settlement.stats.muls.StatsMultipliers;
import settlement.stats.muls.StatsMultipliers.StatMultiplier;
import settlement.stats.standing.StatStanding;
import settlement.stats.standing.StatStanding.StandingDef;
import settlement.stats.stat.STAT;
import settlement.stats.stat.StatCollection;
import settlement.stats.util.StatBooster.StatBoosterStat;
import settlement.stats.util.StatsJson;
import snake2d.util.file.*;
import snake2d.util.misc.Dictionary;
import snake2d.util.sets.*;
import util.data.DataRandom;
import util.data.INT_O;
import util.info.INFO;
import util.updating.IUpdater;

public final class STATS extends SettResource {

	public static final int DAYS_SAVED = 32;

	private static STATS s;

	private final StatsBattle battle;
	private final StatsEnv environment;
	private final StatsAccess access;
	private final StatsPopulation population;
	private final StatsGovern govern;
	private final StatsLaw law;
	private final StatsWork work;
	private final StatsHome home;
	private final StatsService services;
	private final StatsNeeds needs;
	private final StatsFood food;
	private final StatsTraits traits;
	private final StatsEquip equipables;
	private final StatsEducation education;
	private final StatsStored stored;
	private final StatsBurial burial;
	private final StatsReligion religion;
	private final StatsMultipliers multipliers;
	private final StatsAppearance appearance;
	private final DataRandom<Induvidual> random;
	final int dataIntCount;
	private final LIST<SAVABLE> savables;
	private final LIST<Addable> addables;
	private final LIST<Pushable> pushables;
	private final LIST<StatUpdatableI> updaters;
	private final LIST<StatUpdatable> uppers;

	private final LIST<StatInitable> initable;
	private final LIST<StatDisposable> disposables;
	private final LIST<STAT> stats;
	private final LIST<StatCollection> collections;
	private final KeyMap<StatCollection> mapColl;
	private final KeyMap<STAT> mapStat;

	private final IUpdater upper;

//	final ArrayList<StatGlobal> statistics;
//	final ArrayList<StatStat> stats;

	private final short[] iOff = new short[256];

	public final INFO iStats;

	public static void create() throws IOException {
		new STATS();
	}

	private STATS() throws IOException {
		StatsInit init = new StatsInit();

		s = this;

		D.gInit(this);
		iStats = new INFO(D.g("Status"),
				D.g("desc", "Miscellaneous statistics about your city. Some affecting your happiness."));

		random = new DataRandom<Induvidual>(init.count, 4);
		init.initable.add(new StatInitable() {

			@Override
			public void init(Induvidual h) {
				random.randomize(h);
			}
		});
		
		CharSequence nn = D.g("random", "Random Chance");

		for (int i = 0; i < 8; i++) {
			final int k = 8 * i;
			
			INT_O<Induvidual> o = new INT_O<Induvidual>() {

				@Override
				public int get(Induvidual t) {
					return random.get(t, 64 + k);
				}

				@Override
				public int min(Induvidual t) {
					return 0;
				}

				@Override
				public int max(Induvidual t) {
					return 0x0FF;
				}

			};
			GVALUES.INDU.push("RANDOM_" + i + "_F", nn, o);
			GVALUES.INDU.pushI("RANDOM_" + i + "_I", nn, o);
		}

		population = new StatsPopulation(init);
		law = new StatsLaw(init);
		govern = new StatsGovern(init);
		equipables = new StatsEquip(init);
		work = new StatsWork(init, equipables);
		home = new StatsHome(init);
		food = new StatsFood(init);
		services = new StatsService(init);
		environment = new StatsEnv(init);
		access = new StatsAccess(init);
		
		battle = new StatsBattle(init);
		needs = new StatsNeeds(init);
		education = new StatsEducation(init);
		traits = new StatsTraits(init);
		stored = new StatsStored(init);
		burial = new StatsBurial(init);
		religion = new StatsReligion(init);
		multipliers = new StatsMultipliers(init, services);

		appearance = new StatsAppearance(init);
		
		addables = new ArrayList<>(init.addable);

		savables = new ArrayList<>(init.savables);
		updaters = new ArrayList<>(init.updatable);
		initable = new ArrayList<>(init.initable);
		disposables = new ArrayList<>(init.disposable);
		stats = new ArrayList<STAT>(init.stats);
		collections = new ArrayList<>(init.holders);
		pushables = new ArrayList<>(init.pushable);
		uppers = new ArrayList<>(init.upers);
		{
			Arrays.fill(iOff, (short) -1);
			int v = 255;
			int div = 2;

			while (v >= 0) {
				int i = 256 / div;
				for (int k = 1; k < div; k++) {
					if (iOff[k * i] == -1) {
						iOff[k * i] = (short) v;
						v--;
					}
				}
				div++;
			}
		}

		dataIntCount = init.count.longCount();

		mapColl = init.collMap;
		mapStat = init.statMap;

		upper = new IUpdater(uppers.size(), 8) {

			@Override
			protected void update(int i, double timeSinceLast) {
				uppers.get(i).update(timeSinceLast);
			}
		};

		
		
		{
			PATH p = init.pd.getFolder("loyalty");
			for (String f : p.getFiles()) {
				
				Json file = new Json(p.get(f));
				new StatsJson(file) {

					@Override
					public void doWithMultiplier(StatMultiplier m, Json j, String key) {
						handleFault(j, key);
					}

					@Override
					public void doWithTheJson(STAT s, Json j, String key) {
						
						double def = s.standing() != null ? s.standing().defaultInput : 0;
						s.standing = new StatStanding(s, def, new StandingDef(j.json(key)));
						
					}
				};
			}
			
			for (STAT s : all()) {
				if (s.standing == null)
					s.standing = new StatStanding(s, 0);
			}
			
		}
		{
			PATH p = init.pd.getFolder("bonus");
			for (String f : p.getFiles()) {
				
				Json file = new Json(p.get(f));
				new StatsJson(file) {

					@Override
					public void doWithMultiplier(StatMultiplier m, Json j, String key) {
					
						multipliers.setBoost(m, j, key);
						
					}

					@Override
					public void doWithTheJson(STAT s, Json j, String key) {
						s.boosters.push(key, j, new StatBoosterStat(s, false));
						
					}
				};
			}
		}

		new SValues();

//		if (GAME.version() < VERSION.version(54, 7))
//			new Fixer(addables);
	}

	public static StatsBattle BATTLE() {
		return s.battle;
	}

	public static StatsEnv ENV() {
		return s.environment;
	}

	public static StatsAccess ACCESS() {
		return s.access;
	}

	public static StatsPopulation POP() {
		return s.population;
	}

	public static StatsLaw LAW() {
		return s.law;
	}

	public static StatsGovern GOVERN() {
		return s.govern;
	}

	public static StatsFood FOOD() {
		return s.food;
	}

	public static StatsAppearance APPEARANCE() {
		return s.appearance;
	}

	public static StatsTraits TRAITS() {
		return s.traits;
	}

	public static StatsEquip EQUIP() {
		return s.equipables;
	}

	public static StatsWork WORK() {
		return s.work;
	}

	public static StatsEducation EDUCATION() {
		return s.education;
	}

	public static StatsMultipliers MULTIPLIERS() {
		return s.multipliers;
	}

	public static StatsNeeds NEEDS() {
		return s.needs;
	}
	

	public static StatsHome HOME() {
		return s.home;
	}

	public static StatsBurial BURIAL() {
		return s.burial;
	}

	public static StatsReligion RELIGION() {
		return s.religion;
	}

	public static StatsService SERVICE() {
		return s.services;
	}

	public static StatsStored STORED() {
		return s.stored;
	}

	public static LIST<StatCollection> COLLECTIONS() {
		return s.collections;
	}

	public static StatCollection COLLECTION(String key) {
		return s.mapColl.get(key);
	}
	
	public static STAT STAT(String key) {
		return s.mapStat.get(key);
	}

	public static DataRandom<Induvidual> RAN() {
		return s.random;
	}

//	public static HISTORY_INT_OBJECT<Race> POP() {
//		return s.population.POPULATION.data(null);
//	}

	public static LIST<STAT> all() {
		return s.stats;
	}

	public static LIST<STAT> createThoseThatMatters(Race r) {
		ArrayList<STAT> res = new ArrayList<STAT>(all().size());

		for (STAT s : all()) {
			boolean added = false;
			for (HCLASS c : HCLASS.ALL) {
				if (!added && s.key() != null && s.standing().max(c, r) > 0) {
					res.add(s);
					added = true;
				}
			}
		}

		return res;
	}

	public static LIST<STAT> createThoseThatMatters() {
		ArrayList<STAT> res = new ArrayList<STAT>(all().size());

		for (STAT s : all()) {
			boolean added = false;
			for (HCLASS c : HCLASS.ALL) {
				for (Race r : RACES.all()) {
					if (!added && s.key() != null && s.standing().max(c, r) > 0) {
						res.add(s);
						added = true;
					}
				}

			}
		}

		return res;
	}

	public static LIST<STAT> createMatterList(boolean indu, boolean standing, Race race) {
		ArrayList<STAT> res = new ArrayList<STAT>(all().size());

		LIST<Race> races = race == null ? RACES.all() : new ArrayList<>(race);

		for (STAT s : all()) {
			if (s.key() == null)
				continue;
			if (!s.info().matters())
				continue;
			if (indu && !s.info().indu())
				continue;

			if (standing) {
				boolean added = false;
				for (HCLASS c : HCLASS.ALL) {
					for (Race r : races) {
						if (!added && s.key() != null && s.standing().max(c, r) > 0) {
							res.add(s);
							added = true;
							break;
						}
					}

				}
			} else {
				res.add(s);
			}
		}

		res.sort(new Comparator<STAT>() {

			@Override
			public int compare(STAT o1, STAT o2) {
				return Dictionary.compare(o1.info().name, o2.info().name);
			}
		});

		return res;
	}

	static LIST<Addable> addables() {
		return s.addables;
	}

	public static INFO info() {
		return s.iStats;
	}

	static int IDataCount() {
		return s.dataIntCount;
	}

	static STATS get() {
		return s;
	}

	static void update(Humanoid h, int updateI, boolean day) {
		int updateR = s.iOff[updateI];
		for (StatUpdatableI u : s.updaters) {
			u.update16(h, updateR, day, updateI);
			if (h.isRemoved())
				return;
		}
	}

	void add(Induvidual h) {
		for (Addable s : addables) {
			s.addH(h);
		}
	}

	void remove(Induvidual i) {
		for (Addable s : addables) {
			s.removeH(i);
		}
	}

	void init(Induvidual i) {
		for (StatInitable in : s.initable) {
			in.init(i);
		}
		for (StatUpdatableI u : s.updaters)
			u.init(i);
	}

	void cancel(Humanoid h) {
		for (StatDisposable i : s.disposables) {
			i.dispose(h);
		}
		remove(h.indu());

	}

	@Override
	protected void save(FilePutter file) {
		for (SAVABLE s : savables)
			s.save(file);
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		int pos = file.getPosition();
		try {
			for (SAVABLE s : savables) {
				s.load(file);
			}
		}catch(Exception e) {
			file.setPosition(pos);
			EquipBattle.fixifixshit = true;
			for (SAVABLE s : savables) {
				s.load(file);
			}
			
		}
		
	}

	@Override
	protected void clearBeforeGeneration(CapitolArea area) {
		for (SAVABLE s : savables)
			s.clear();
	}
	
	@Override
	protected void init(boolean loaded) {
		for (StatUpdatable i : uppers)
			i.update(1);
	}

	private int day = TIME.days().bitCurrent();
	private int gi = 0;

	@Override
	protected void update(float ds, Profiler profiler) {
		if (ds > 0) {
			if (gi < pushables.size()) {
				pushables.get(gi++).pushday();
			}

			if (day != TIME.days().bitCurrent()) {
				day = TIME.days().bitCurrent();
				gi = 0;
			}

		}
		upper.update(ds);
	}

}
