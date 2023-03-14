package settlement.stats;

import java.io.IOException;

import init.D;
import init.boostable.BBoost;
import init.boostable.BOOSTABLES;
import init.race.RACES;
import init.race.Race;
import settlement.entity.humanoid.*;
import settlement.main.SETT;
import settlement.room.service.hearth.ROOM_HEARTH;
import settlement.room.service.hygine.well.ROOM_WELL;
import settlement.room.service.module.RoomServiceDataAccess;
import settlement.room.service.module.RoomServiceDataAccess.ROOM_SERVICE_ACCESS_HASER;
import settlement.stats.STANDING.StandingDef;
import settlement.stats.STAT.StatInfo;
import snake2d.util.file.FileGetter;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.*;
import util.data.INT_O.INT_OE;
import util.race.PERMISSION;

public final class StatsService extends StatCollection {

	public final StatServiceGroup EATING;
	public final StatServiceGroup DRINKING;
	public final StatServiceGroup HYGINE;
	public final StatServiceGroup SCHOOLS;
	private final ArrayList<StatServiceGroup> groups;

	private final ArrayList<StatService> singles;

	private final ArrayList<StatService> all = new ArrayList<>(SETT.ROOMS().SERVICES.all().size());
	private final LIST<STAT> stats;

	private static CharSequence ¤¤Access = "¤Access";
	private static CharSequence ¤¤AcessDesc = "¤The level of access this subject has to a service. Can be improved by building more service facilities and make sure they are close enough for your people to utilize.";
	private static CharSequence ¤¤Quality = "¤Quality";
	private static CharSequence ¤¤QualityDesc = "¤The quality of a subjects last visit to this facility. Often improved by placing special items in the rooms in question.";
	private static CharSequence ¤¤Distance = "¤Proximity";
	private static CharSequence ¤¤DistanceDesc = "¤The distance the subject has had to walk to reach service determines proximity. To improve, make sure your city has a good coverage of services.";
	private static CharSequence ¤¤TotalDesc = "¤The access and quality this subject group has. Can be improved by building more facilities, keeping them maintained, and also in some cases building them well.";
	
	private static CharSequence ¤¤More = "¤We want better access to {0}, of good quality.";
	
	static {
		D.ts(StatsService.class);
	}
	
	StatsService(Init init) {
		super(init, "SERVICE");
		
		D.gInit(this);

		ArrayList<RoomServiceDataAccess> services = new ArrayList<>(SETT.ROOMS().SERVICES.all());
		boolean[] added = new boolean[services.size()];

		EATING = new StatServiceGroup(all, init, SETT.ROOMS().EAT, D.g("Eating-Services"), false);
		HYGINE = new StatServiceGroup(all, init, SETT.ROOMS().HYGINE, D.g("Hygiene-Services"), false);
		DRINKING = new StatServiceGroup(all, init, SETT.ROOMS().DRINK, D.g("Drinking-Services"), false);
		groups = new ArrayList<>(EATING, HYGINE, DRINKING);

		SCHOOLS = new StatServiceGroup(all, init, SETT.ROOMS().EDUCATION, D.g("Schools"), true);
		
		for (StatServiceGroup g : groups) {
			for (StatService s : g.all())
				added[s.service.index()] = true;
		}
		for (StatService s :SCHOOLS.all())
			added[s.service.index()] = true;
		singles = new ArrayList<>(all.max() - all.size());

		for (RoomServiceDataAccess s : SETT.ROOMS().SERVICES) {
			if (!added[s.index()])
				singles.add(new StatService(all, s, init, false));
		}


		
		StatService[] allO = new StatService[all.size()];

		for (StatService s : all) {
			allO[s.index] = s;
		}

		all.clear();
		all.add(allO);

		stats = makeStats(init);
		
		new StatBoosterStat(SETT.ROOMS().PHYSICIAN.info.name, get(SETT.ROOMS().PHYSICIAN.service()).access, new BBoost(BOOSTABLES.PHYSICS().HEALTH, 3.0, true));
		
		
	}

	public StatService get(RoomServiceDataAccess data) {
		return all.get(data.index());
	}

	public LIST<StatService> allE() {
		return all;
	}

	public LIST<StatServiceGroup> groups() {
		return groups;
	}

	public LIST<StatService> others() {
		return singles;
	}

	public final class StatServiceGroup {

		private final LIST<StatService> all;
		public final CharSequence name;

		StatServiceGroup(ArrayList<StatService> all, Init init, LIST<ROOM_SERVICE_ACCESS_HASER> services,
				CharSequence name, boolean on) {
			this.name = name;
			ArrayList<StatService> all2 = new ArrayList<>(services.size());
			for (ROOM_SERVICE_ACCESS_HASER d : services) {
				all2.add(new StatService(all, d.service(), init, on));
			}
			this.all = all2;
		}

		public double getD(HCLASS c, Race race, int daysBack) {
			double g = 0;
			for (StatService s : all)
				g += s.total.data(c).getD(race, daysBack);
			return g;
		}

		public double maxHappiness(HCLASS c, Race race) {
			double g = 0;
			for (StatService s : all) {
				StandingDef d = s.total.standing().definition(race);
				g = CLAMP.d(d.get(c).max, g, d.get(c).max);
			}
			return g;
		}
		
		public double nomalized(HCLASS c, Race race) {
			double g = 0;
			for (StatService s : all) {
				g = Math.max(g, s.total.standing().normalized(c, race));
			}
			return g;
		}

		public LIST<StatService> all() {
			return all;
		}

	}

	public static final class StatService {

		public final static int TARGET_MAX = 16;
		private final STAT access;
		private final STAT quality;
		private final STAT proximity;
		private final STAT total;
		private final RoomServiceDataAccess service;
		private int index;
		public final boolean usesTarget;
		private final PERMISSION.Permission permission;

		StatService(LISTE<StatService> all, RoomServiceDataAccess service, Init init, boolean on) {
			index = service.index();
			all.add(this);
			this.service = service;
			usesTarget = service.usesAccess;
			access = new STAT.STATData(null, init, init.count.new DataBit(), new StatInfo(¤¤Access, ¤¤Access, ¤¤AcessDesc), null);
			access.info().setMatters(false, true);
			quality = new STAT.STATData(null, init, init.count.new DataNibble(), new StatInfo(¤¤Quality, ¤¤Quality, ¤¤QualityDesc), null);
			quality.info().setMatters(false, true);
			proximity = new STAT.STATData(null, init, init.count.new DataNibble(), new StatInfo(¤¤Distance, ¤¤Distance, ¤¤DistanceDesc), null);
			proximity.info().setMatters(false, true);
			
			INT_OE<Induvidual> indu = new INT_OE<Induvidual>() {

				@Override
				public int get(Induvidual t) {
					double a = access.indu().getD(t);
					if (a == 0)
						return 0;
					double q = quality.indu().getD(t);
					double p = proximity.indu().getD(t);
					
					return (int) (64*(a * (0.5 + 0.5 * q)
							* (0.5 + 0.5 * p)));
				}

				@Override
				public int min(Induvidual t) {
					return 0;
				}

				@Override
				public int max(Induvidual t) {
					return 64;
				}

				@Override
				public void set(Induvidual t, int i) {
					
				}
				
				
			};
			
			StatInfo info = new StatInfo(service.room().info.names, ¤¤TotalDesc);
			info.setOpinion(¤¤More, null);
			
			total = new STAT.STATFacade(service().room().key, init, info,
					service().standingDef, indu) {


				@Override
				double getDD(HCLASS s, Race r, int daysBack) {
					double a = access.data(s).getD(r, daysBack);
					if (a == 0)
						return 0;
					double q = quality.data(s).getD(r, daysBack)/a;
					double p = proximity.data(s).getD(r, daysBack)/a;
					
					return a * (0.5 + 0.5 * q)
							* (0.5 + 0.5 * p);
				}
			};
			permission = new PERMISSION.Permission(total.info()) {
				@Override
				public void clear() {
					for (HCLASS c : HCLASS.ALL) {
						for (Race r: RACES.all()) {
							if (service.room() instanceof ROOM_WELL || service.room() instanceof ROOM_HEARTH){
								permission.set(c, r, true);
							}else
								permission.set(c, r, on || total.standing().definition(r).get(c).max > 0);
						}
					}
				}
				
				@Override
				public void load(FileGetter file) throws IOException {
					super.load(file);
					for (Race r: RACES.all()) {
						if (service.room() instanceof ROOM_WELL || service.room() instanceof ROOM_HEARTH){
							permission.set(HCLASS.NOBLE, r, true);
						}
					}
				}
			};
			
			
			
			
			
			init.savables.add(permission);

		}
		
		public PERMISSION permission() {
			return permission;
		}

		public boolean access(Humanoid h) {
			return access.indu().get(h.indu()) == 1;
		}

		public double quality(Humanoid h) {
			return quality.indu().getD(h.indu());
		}

		public double proximity(Humanoid h) {
			return proximity.indu().getD(h.indu());
		}

		public double getBasePriority(Humanoid h) {
			return h.indu().race().stats().defNormalized(h.indu().hType().CLASS, total.standing());
		}

		public double total(Humanoid h) {
			return total(h.indu());
		}
		
		public double total(Induvidual a) {
			return access.indu().getD(a) * (0.5 + quality.indu().getD(a) * 0.5)
					* (0.5 + proximity.indu().getD(a) * 0.5);
		}

		protected double pdivider(HCLASS c, Race r, int daysback) {
			return STATS.POP().POP.data(c).get(r, daysback);
		}
		
		/**
		 * @param h
		 * @return
		 */
		public boolean accessRequest(Humanoid h) {
			if (h.indu().hType() == HTYPE.NOBILITY)
				return true;
			if (h.indu().hType() == HTYPE.TOURIST)
				return true;
			return permission.has(h);
		}

		public void setAccess(Humanoid h, boolean access, double quality, double proximity) {
			Induvidual i = h.indu();
			
			
			this.access.indu().set(i, access ? 1 : 0);
			this.quality.indu().setD(i, quality);
			this.proximity.indu().setD(i, proximity);
			
		}

		public void setProximity(Humanoid h, double proximity) {
			Induvidual i = h.indu();
			this.proximity.indu().setD(i, proximity);
		}

		public RoomServiceDataAccess service() {
			return service;
		}

		public STAT access() {
			return access;
		}

		public STAT quality() {
			return quality;
		}

		public STAT proximity() {
			return proximity;
		}

		public STAT total() {
			return total;
		}

	}

	@Override
	public LIST<STAT> all() {
		return stats;
	}

}
