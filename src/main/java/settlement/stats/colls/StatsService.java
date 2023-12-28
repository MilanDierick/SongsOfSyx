package settlement.stats.colls;

import java.io.IOException;
import java.util.Arrays;

import game.boosting.*;
import game.time.TIME;
import init.D;
import init.race.*;
import settlement.entity.humanoid.*;
import settlement.main.SETT;
import settlement.room.service.module.*;
import settlement.room.service.module.RoomServiceAccess.ROOM_SERVICE_ACCESS_HASER;
import settlement.stats.*;
import settlement.stats.standing.StatStanding;
import settlement.stats.standing.StatStanding.StandingDef;
import settlement.stats.stat.*;
import settlement.stats.util.StatBooster;
import snake2d.util.MATH;
import snake2d.util.file.*;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.*;
import util.data.INT_O.INT_OE;
import util.info.INFO;

public final class StatsService extends StatCollection {

	public final StatServiceGroup SCHOOLS;
	private final ArrayListGrower<StatServiceGroup> groups = new ArrayListGrower<>();

	private final ArrayList<StatService> singles;

	private final ArrayList<StatService> all = new ArrayList<>(SETT.ROOMS().SERVICE.all().size());

	private static CharSequence ¤¤Access = "¤Access";
	private static CharSequence ¤¤AcessDesc = "¤The level of access this subject has to a service. Can be improved by building more service facilities and make sure they are close enough for your people to utilize.";
	private static CharSequence ¤¤Quality = "¤Quality";
	private static CharSequence ¤¤QualityDesc = "¤The quality of a subjects last visit to this facility. Often improved by placing special items in the rooms in question.";
	private static CharSequence ¤¤Distance = "¤Proximity";
	private static CharSequence ¤¤DistanceDesc = "¤The distance the subject has had to walk to reach service determines proximity. To improve, make sure your city has a good coverage of services.";
	private static CharSequence ¤¤TotalDesc = "¤The access and quality this subject group has. Can be improved by building more facilities, keeping them maintained, and also in some cases building them well.";
	
	private static CharSequence ¤¤More = "¤We want better access to {0}, of good quality.";
	private static CharSequence ¤¤perm = "¤Permission";
	
	
	static {
		D.ts(StatsService.class);
	}
	
	public StatsService(StatsInit init) {
		super(init, "SERVICE");
		
		D.gInit(this);

		ArrayList<RoomServiceAccess> services = new ArrayList<>(SETT.ROOMS().SERVICE.access());
		boolean[] added = new boolean[services.size()];

		for (RoomServiceGroup g : SETT.ROOMS().SERVICE.groups()) {
			if (g.all().size() > 1) {
				groups.add(new StatServiceGroup(all, init, g.all(), g.need.nameNeed, false));
			}
		}

		SCHOOLS = new StatServiceGroup(all, init, SETT.ROOMS().EDUCATION, D.g("Schools"));
		
		for (StatServiceGroup g : groups) {
			for (StatService s : g.all())
				added[s.service.indexAccess()] = true;
		}
		for (StatService s : SCHOOLS.all())
			added[s.service.indexAccess()] = true;
		singles = new ArrayList<>(all.max() - all.size());

		for (RoomServiceAccess s : SETT.ROOMS().SERVICE.access()) {
			if (!added[s.indexAccess()])
				singles.add(new StatService(all, s, init, false));
		}


		
		StatService[] allO = new StatService[all.size()];

		for (StatService s : all) {
			allO[s.index] = s;
		}

		all.clear();
		all.add(allO);
		
		
	}

	public StatService get(RoomServiceAccess data) {
		return all.get(data.indexAccess());
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

		StatServiceGroup(ArrayList<StatService> all, StatsInit init, LIST<RoomServiceNeed> services,
				CharSequence name, boolean on) {
			this.name = name;
			ArrayList<StatService> all2 = new ArrayList<>(services.size());
			for (RoomServiceNeed d : services) {
				all2.add(new StatService(all, d, init, on));
			}
			this.all = all2;
		}
		
		StatServiceGroup(ArrayList<StatService> all, StatsInit init, LIST<ROOM_SERVICE_ACCESS_HASER> services,
				CharSequence name) {
			this.name = name;
			ArrayList<StatService> all2 = new ArrayList<>(services.size());
			for (ROOM_SERVICE_ACCESS_HASER d : services) {
				all2.add(new StatService(all, d.service(), init, true));
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
		private final RoomServiceAccess service;
		private int index;
		public final boolean usesTarget;
		private final Perm permission;
		
		StatService(LISTE<StatService> all, RoomServiceAccess service, StatsInit init, boolean on) {
			index = service.indexAccess();
			all.add(this);
			this.service = service;
			usesTarget = service.usesAccess;

			
			access = new STATData(null, init, init.count.new DataBit(),  new StatInfo(¤¤Access, ¤¤Access, ¤¤AcessDesc));
	
			access.info().setMatters(false, true);
			quality = new STATData(null, init, init.count.new DataNibble(), new StatInfo(¤¤Quality, ¤¤Quality, ¤¤QualityDesc));
			quality.info().setMatters(false, true);
			proximity = new STATData(null, init, init.count.new DataNibble(), new StatInfo(¤¤Distance, ¤¤Distance, ¤¤DistanceDesc));
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
			
			total = new STATFacade(service().room().key, init, indu, info) {


				@Override
				protected double getDD(HCLASS s, Race r, int daysBack) {
					double a = access.data(s).getD(r, daysBack);
					if (a == 0)
						return 0;
					double q = quality.data(s).getD(r, daysBack)/a;
					double p = proximity.data(s).getD(r, daysBack)/a;
					
					return a * (0.5 + 0.5 * q)
							* (0.5 + 0.5 * p);
				}
			};
			total.standing = new StatStanding(total, 0, service().standingDef);
			permission = new Perm(¤¤perm, ¤¤perm + ": " + service.room().info.names);
			
			BOOSTING.connecter(new ACTION() {
				
				@Override
				public void exe() {
					for (BoostSpec t : service.boosts.all()) {
						
						BValue v = new StatBooster.StatBoosterStat(total, true);
						BSourceInfo in = new BSourceInfo(service.room().info.names, service.room().icon.small);
						
						total.boosters.push(new BoosterWrap(v, in, t.booster.from(), t.booster.to(), t.booster.isMul), t.boostable);
						
						
						
					}
				}
			});
			
			
			
			
			
			
			init.savables.add(permission);

		}
		
		public INT_OE<POP_CL> permission() {
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
			int ii = TIME.days().bitsSinceStart() + STATS.RAN().get(h.indu(), 9);
			ii = MATH.mod(ii, permission.max(h.indu().popCL()));
			return permission.get(h.indu().popCL())-1 >= ii;
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

		public RoomServiceAccess service() {
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

	private static class Perm implements INT_OE<POP_CL>, SAVABLE{
		
		private final byte[] access = new byte[RACES.cls().size()];
		private final INFO info;
		
		public Perm(CharSequence name, CharSequence desc){
			this.info = new INFO(name, desc);
			Arrays.fill(access, (byte)10);
		}
		
		@Override
		public int get(POP_CL t) {
			if (t.race == null) {
				int m = 0;
				for (Race r : RACES.all()) {
					m = Math.max(m, get(t.cl.get(r)));
				}
				return m;
			}
			return access[t.index];
		}
		
		@Override
		public int max(POP_CL t) {
			return 10;
		}
		
		@Override
		public int min(POP_CL t) {
			return 0;
		}

		@Override
		public void set(POP_CL t, int value) {
			if (t.race == null) {
				for (Race r : RACES.all()) {
					set(t.cl.get(r), value);
				}
			}else {
				access[t.index] = (byte) CLAMP.i(value, 0, 10);
			}
		}

		@Override
		public INFO info() {
			return info;
		}

		@Override
		public void save(FilePutter file) {
			file.bs(access);
		}

		@Override
		public void load(FileGetter file) throws IOException {
			file.bs(access);
		}

		@Override
		public void clear() {
			Arrays.fill(access, (byte)10);
		}
		
	}
	
}
