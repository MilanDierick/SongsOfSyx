package world.army;

import static settlement.main.SETT.*;

import java.io.IOException;
import java.util.Iterator;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.FResources.RTYPE;
import game.faction.Faction;
import init.config.Config;
import init.race.Race;
import init.resources.RESOURCES;
import settlement.army.Div;
import settlement.army.DivisionBanners.DivisionBanner;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.misc.util.RESOURCE_TILE;
import settlement.room.main.Room;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBattle.StatTraining;
import settlement.stats.equip.EquipBattle;
import settlement.stats.util.CAUSE_LEAVE;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.*;
import world.WORLD;

final class WDivsCapitol implements LIST<WDIV>, SAVABLE {

	private int updateTick = -1;
	private final ArrayList<WDivCity> list = new ArrayList<WDivCity>(Config.BATTLE.DIVISIONS_PER_ARMY);
	private final WDivCity[] all = new WDivCity[Config.BATTLE.DIVISIONS_PER_ARMY];

	@Override
	public void save(FilePutter file) {

	}

	@Override
	public void load(FileGetter file) throws IOException {
		updateTick = -1;
	}

	@Override
	public void clear() {
		updateTick = -1;
	}

	WDivsCapitol() {
		for (int di = 0; di < ARMIES().player().divisions().size(); di++) {
			all[di] = new WDivCity(ARMIES().player().divisions().get(di));
		}
		upI = -1;
	}

	private void init() {

		if (updateTick == GAME.updateI())
			return;

		updateTick = GAME.updateI();
		list.clear();

		for (int di = 0; di < ARMIES().player().divisions().size(); di++) {
			Div d = ARMIES().player().ordered().get(di);
			if ((WORLD.ARMIES().cityDivs().attachedArmy(d) == null && STATS.BATTLE().DIV.stat().div().get(d) > 0)) {
				list.add(all[d.indexArmy()]);
			}
		}
	}

	@Override
	public Iterator<WDIV> iterator() {
		init();
		ii = 0;
		return iterer;
	}

	private int ii;
	private final Iterator<WDIV> iterer = new Iterator<WDIV>() {

		@Override
		public boolean hasNext() {
			return ii < size();
		}

		@Override
		public WDIV next() {
			WDIV d = list.get(ii);
			ii++;
			return d;
		}

	};

	private static double[] supplies;
	private static int upI = -1;

	private static void initSupplies() {
		if (supplies == null || upI != GAME.updateI()) {
			upI = GAME.updateI();
			int[] suppliesHave = new int[RESOURCES.ALL().size()];
			int[] suppliesNeeded = new int[RESOURCES.ALL().size()];
			supplies = new double[STATS.EQUIP().BATTLE_ALL().size()];

			for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
				if (e instanceof Humanoid) {
					Humanoid a = (Humanoid) e;
					Div d = STATS.BATTLE().DIV.get(a);
					if (d != null && d.army() == SETT.ARMIES().player()) {
						for (EquipBattle s : STATS.EQUIP().BATTLE_ALL()) {
							suppliesNeeded[s.resource().index()] += s.target(d);
							suppliesHave[s.resource().index()] += s.stat().indu().get(a.indu());
						}

					}
				}

			}

			for (EquipBattle s : STATS.EQUIP().BATTLE_ALL()) {
				if (suppliesNeeded[s.resource().index()] != 0) {
					suppliesHave[s.resource().index()] += SETT.ROOMS().STOCKPILE.tally().amountReservable(s.resource());
					supplies[s.indexMilitary()] = (double) (suppliesHave[s.resource().index()]) / (suppliesNeeded[s.resource().index()]);
				} else {
					supplies[s.indexMilitary()] = 0;
				}
				supplies[s.indexMilitary()] = CLAMP.d(supplies[s.indexMilitary()], 0, 1);
			}
		}
	}

	public void extractLostEquipment(int[] amounts) {

		for (COORDINATE c : TILE_BOUNDS) {
			Room r = ROOMS().STOCKPILE.get(c.x(), c.y());
			if (r == null)
				continue;
			RESOURCE_TILE cr = (RESOURCE_TILE) r.storage(c.x(), c.y());
			if (cr != null) {
				for (EquipBattle s : STATS.EQUIP().BATTLE_ALL()) {
					if (amounts[s.indexMilitary()] <= 0)
						continue;
					if (cr.resource() == s.resource()) {
						while (amounts[s.indexMilitary()] > 0 && cr.reservable() > 0) {
							cr.findableReserve();
							cr.resourcePickup();
							amounts[s.index()]--;
							FACTIONS.player().res().inc(cr.resource(), RTYPE.SPOILS, -1);
						}
					}
				}
			}
		}
		
		for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
			if (e instanceof Humanoid) {
				Humanoid a = (Humanoid) e;
				Div d = STATS.BATTLE().DIV.get(a);
				if (d != null && d.army() == SETT.ARMIES().player()) {
					for (EquipBattle s : STATS.EQUIP().BATTLE_ALL()) {
						if (amounts[s.indexMilitary()] <= 0)
							continue;
						if (s.stat().indu().get(a.indu()) > 0) {
							int am = CLAMP.i(s.stat().indu().get(a.indu()), 0, amounts[s.indexMilitary()]);
							s.stat().indu().inc(a.indu(), -am);
							amounts[s.indexMilitary()] -= am;
						}
					}

				}
			}

		}

	}

	@Override
	public WDIV get(int index) {
		init();
		return list.get(index);
	}

	
	
	@Override
	public boolean contains(int i) {
		init();
		return list.contains(i);
	}

	@Override
	public boolean contains(WDIV object) {
		if (object instanceof WDivCity)
			return list.contains((WDivCity) object);
		return false;
	}

	@Override
	public int size() {
		init();
		return list.size();
	}

	@Override
	public boolean isEmpty() {
		init();
		return list.size() == 0;
	}

	public class WDivCity implements WDIV {

		private final int di;

		WDivCity(Div div) {
		
			this.di = div.index();

		}

		private Div div() {
			return SETT.ARMIES().division((short) di);
		}

		@Override
		public int men() {
			return div().menNrOf();
		}

		@Override
		public Race race() {
			return div().info.race();
		}

		@Override
		public int menTarget() {
			return div().menNrOf();
		}

		@Override
		public double training(StatTraining tr) {
			return tr.div().getD(div());
		}
		
		@Override
		public double trainingTarget(StatTraining tr) {
			return div().info.trainingD(tr.room).getD();
		}

		@Override
		public int equipTarget(EquipBattle e) {
			return e.target(div());
		}
		
		@Override
		public double equip(EquipBattle e) {
			initSupplies();
			return e.target(div())*supplies[e.indexMilitary()];
		}

		@Override
		public double experience() {
			return STATS.BATTLE().COMBAT_EXPERIENCE.div().getD(div());
		}

		@Override
		public WDivGeneration generate() {

			ArrayList<Induvidual> inus = new ArrayList<Induvidual>(men());
			for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
				if (e instanceof Humanoid) {
					Humanoid a = (Humanoid) e;
					Div d = STATS.BATTLE().DIV.get(a);
					if (d == div()) {
						inus.add(a.indu());
					}
				}

			}
			WDivGeneration res = new WDivGeneration(this, inus);
			return res;
		}
		
		@Override
		public void resolve(Induvidual[] hs) {
			
			KeyMap<Induvidual> map = new KeyMap<>();
			for (Induvidual ii : hs) {
				String k = "" + STATS.RAN().getL(ii, 0);
				if (!map.containsKey(k))
					map.put(k, ii);
			}
			for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
				if (e instanceof Humanoid) {
					Humanoid a = (Humanoid) e;
					if (STATS.BATTLE().DIV.get(a) == div()) {
						String k = "" + STATS.RAN().getL(a.indu(), 0);
						if (map.containsKey(k)) {
							a.indu().copyFrom(map.get(k));
						} else {
							STATS.POP().COUNT.reg(a.indu(), CAUSE_LEAVE.SLAYED);
							a.helloMyNameIsInigoMontoyaYouKilledMyFatherPrepareToDie();
						}

					}

				}
			}
		}

		@Override
		public void resolve(int surviviors, double experiencePerMan) {

			
			
			double dExperience = experiencePerMan - experience();
			dExperience *= surviviors;

			int deaths = men() - surviviors;
			
			
			for (ENTITY e : SETT.ENTITIES().getAllEnts()) {

				if (e instanceof Humanoid) {
					Humanoid a = (Humanoid) e;

					if (STATS.BATTLE().DIV.get(a) == div()) {
						if (deaths <= 0) {
							int am = (int) dExperience;
							if (dExperience - am > RND.rFloat())
								STATS.BATTLE().COMBAT_EXPERIENCE.indu().inc(a.indu(), am);
						} else {
							STATS.POP().COUNT.reg(a.indu(), CAUSE_LEAVE.SLAYED);
							a.helloMyNameIsInigoMontoyaYouKilledMyFatherPrepareToDie();
							deaths--;
						}
					}
				}
			}

		}

		@Override
		public int daysUntilMenArrives() {
			return 0;
		}

		@Override
		public CharSequence name() {
			return div().info.name();
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
		public int bannerI() {
			return div().info.symbolI();
		}

	}

}
