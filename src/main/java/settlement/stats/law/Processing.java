package settlement.stats.law;

import java.io.IOException;
import java.util.Arrays;

import game.VERSION;
import game.time.TIME;
import init.paths.PATHS;
import init.race.RACES;
import init.race.Race;
import init.sprite.SPRITES;
import settlement.entity.humanoid.HCLASS;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.law.PUNISHMENT_SERVICE;
import settlement.room.main.RoomBlueprintImp;
import settlement.stats.STATS;
import snake2d.util.file.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.*;
import snake2d.util.sprite.SPRITE;
import util.data.BOOLEAN_OBJECT.BOOLEAN_OBJECTE;
import util.info.INFO;
import util.statistics.*;

public final class Processing {

	final HistoryRace punishAll = new HistoryRace(STATS.DAYS_SAVED, TIME.days(), false);
	
	private Json json = new Json(PATHS.TEXT_MISC().get("Law")).json("PUNISHMENT");
	private final LinkedList<SAVABLE> savers = new LinkedList<>();
	{savers.add(punishAll);}
	private LinkedList<Punishment> al = new LinkedList<>();
	
	public final Punishment exile = new Punishment(al, json, "EXILE", null, SPRITES.icons().m.arrow_left, 0.5);
	public final Punishment pardoned = new Punishment(al, json, "PARDONED", null, SPRITES.icons().m.admin, 0.1);
	public final PunishmentDec prison = new PunishmentDec(al, json, "PRISON", SETT.ROOMS().PRISON, 1.5);
	public final PunishmentDec execution = new PunishmentDec(al, json, "EXECUTION", SETT.ROOMS().EXECUTION, 2.0);
	public final PunishmentDec enslaved = new PunishmentDec(al, json, "ENSLAVED", SETT.ROOMS().SLAVER, 1.0);

	public final Extra stocks = new ExtraImp(json, "STOCKS", SETT.ROOMS().STOCKS);
	public final Extra judgement = new ExtraImp(json, "JUDGEMENT", SETT.ROOMS().COURT);
	public final Extra arrests = new ExtraImp(json, "ARRESTS", SETT.ROOMS().GUARD.iconBig());
	public final Extra prosecute = new Extra(json, "PROSECUTE", SPRITES.icons().m.descrimination) {

		@Override
		public int total(Race race, int daysBack) {
			return STATS.POP().POP.data(HCLASS.CITIZEN).get(race, daysBack);
		}
		
		
	};

	public final LIST<Punishment> punishments = new ArrayList<Punishment>(al);
	{al = null;};
	public final LIST<PunishmentDec> punishmentsdec = new ArrayList<PunishmentDec>(prison,execution,enslaved);
	public final LIST<Extra> extras = new ArrayList<Extra>(judgement,stocks);
	public final LIST<Extra> other = new ArrayList<Extra>(arrests,prosecute);
	
	private double upD = 0;
	
	
	public Processing() {
		saver.clear();
		json = null;
	}
	
	public HISTORY_INT punishTotal(Race race) {
		return punishAll.history(race);
	}
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			for (SAVABLE s : savers)
				s.save(file);
			file.d(upD);
			
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			for (SAVABLE s : savers)
				s.load(file);
			upD = file.d();
			if (VERSION.versionIsBefore(63, 6))
				clear();
		}
		
		@Override
		public void clear() {
			for (SAVABLE s : savers)
				s.clear();
			
			for (Extra e : extras) {
				e.allowedd.setAll(true);
			}
			arrests.allowedd.setAll(true);
			
			Arrays.fill(prison.dec, 0.5f);
			Arrays.fill(enslaved.dec, 0.25f);
			Arrays.fill(execution.dec, 0.25f);
			
			
			upD = 0;
		}
	};
	
	void update(double ds) {
		
		
		if (upD < 0)
			return;
		
		int ri = (int) upD;
		upD -= ds;
		
		if (ri == (int) upD)
			return;
		
		Race r = RACES.all().get(ri);
		
		prosecute.rate.setD(r, prate(prosecute, r));
		prosecute.rate.total().setD(prate(prosecute, null));
		
		double tot = 0;
		double ttot = 0;
		for (int i = 0; i < 8; i++) {
			tot += punishAll.history(r).get(i);
			ttot +=  punishAll.history(null).get(i);
		}
		
		if (tot == 0)
			return;
		
		for (Punishment p : punishments) {
			p.rate.setD(r, prate(p, r, tot));
			p.rate.total().setD(prate(p, null, ttot));
		}
		
		
		
	}
	
	private static double prate(PunishmentImp current, Race race) {
		double tot = 0;
		double am = 0;
		for (int i = 0; i < 8; i++) {
			am += current.history(race).get(i);
			tot += current.total(race, i);
		}
		if (tot == 0)
			am = CLAMP.d(am, 0, 1);
		else
			am = CLAMP.d(am/tot, 0, 1);
		return am;
	}
	
	private static double prate(PunishmentImp current, Race race, double tot) {
		double am = 0;
		for (int i = 0; i < 8; i++) {
			am += current.history(race).get(i);
		}
		if (tot == 0)
			am = CLAMP.d(am, 0, 1);
		else
			am = CLAMP.d(am/tot, 0, 1);
		return am;
	}
	
	public Punishment getPunishment(Humanoid h) {
		double r = RND.rFloat();
		for (PunishmentDec p : punishmentsdec) {
			r -= p.limit(h.race());
			if (r <= 0)
				return p;
		}
		return exile;
	}
	
	public abstract class PunishmentImp extends INFO{

		final HistoryRace history = new HistoryRace(STATS.DAYS_SAVED, TIME.days(), false);
		final HistoryRace rate = new HistoryRace(STATS.DAYS_SAVED, TIME.days(), true);
		public final CharSequence action;
		public final CharSequence verb;
		
		public final RoomBlueprintImp room;
		public final PUNISHMENT_SERVICE ser;
		public final SPRITE icon;
		public final double multiplier;
		
		private PunishmentImp(Json json, String key, PUNISHMENT_SERVICE room, SPRITE icon, double mul) {
			super(json.json(key));
			this.multiplier = mul;
			verb = json.json(key).text("VERB");
			action = json.json(key).text("ACTION");
			if (room != null) {
				this.room = (RoomBlueprintImp) room;
				this.ser = room;
			}else {
				this.room = null;
				this.ser = null;
			}
			this.icon = icon;
			savers.add(history);
			savers.add(rate);
		}
		
		public HISTORY_INT history(Race race) {
			return history.history(race);
		}
		
		public HISTORY rate(Race race) {
			return rate.history(race);
		}

		public abstract int total(Race race, int daysBack);

	}
	
	public abstract class Extra extends PunishmentImp{

		final Bitmap1D allowedd = new Bitmap1D(RACES.all().size(), false);
		
		private Extra(Json json, String key , PUNISHMENT_SERVICE room) {
			super(json, key, room, ((RoomBlueprintImp) room).iconBig(), 0);
			savers.add(allowedd);
		}
		
		private Extra(Json json, String key , SPRITE icon) {
			super(json, key, null, icon, 0);
			savers.add(allowedd);
		}
		
		public final BOOLEAN_OBJECTE<Race> allowed = new BOOLEAN_OBJECTE<Race>() {

			@Override
			public boolean is(Race t) {
				if (t == null) {
					for (int i = 0; i < allowedd.size(); i++)
						if (allowedd.get(i))
							return true;
					return false;
				}
				return allowedd.get(t.index());
			}

			@Override
			public BOOLEAN_OBJECTE<Race> set(Race t, boolean b) {
				if (t == null) {
					for (int i = 0; i < allowedd.size(); i++)
						allowedd.set(i, b);
				}else {
					allowedd.set(t.index(), b);
				}
				return this;
			}
			
			
			
		};
		
		public void inc(Race race, boolean success) {
			if (success)
				this.history.inc(race, 1);
			rate.setD(race, prate(this, race));
			rate.total().setD(prate(this, null));
		}
		
	}
	

	
	private final class ExtraImp extends Extra{

		final HistoryRace total = new HistoryRace(STATS.DAYS_SAVED, TIME.days(), false);
		
		private ExtraImp(Json json, String key , PUNISHMENT_SERVICE room) {
			super(json, key, room);
			savers.add(total);
		}
		
		private ExtraImp(Json json, String key , SPRITE icon) {
			super(json, key, icon);
			savers.add(total);
		}

		@Override
		public int total(Race race, int daysBack) {
			return total.history(race).get(daysBack);
		}
		
		@Override
		public void inc(Race race, boolean success) {
			total.inc(race, 1);
			super.inc(race, success);
		}
		
	}
	
	public class Punishment extends PunishmentImp implements INDEXED{

		private final int index;
		
		private Punishment(LISTE<Punishment> all, Json json, String key, PUNISHMENT_SERVICE room, SPRITE icon, double value) {
			super(json, key, room, icon, value);
			this.index = all.add(this);
		}
		
		
		public void inc(Race race) {
			
			history.inc(race, 1);
			punishAll.inc(race, 1);
			upD = RACES.all().size()-1;
				
		}
		
		public void dec(Race race) {
			
			history.inc(race, -1);
			punishAll.inc(race, -1);
			upD = RACES.all().size()-1;
		}

		@Override
		public int total(Race race, int daysBack) {
			return punishTotal(race).get(daysBack);
		}


		@Override
		public int index() {
			return index;
		}
		
	}
	
	public final class PunishmentDec extends Punishment {
		
		private final float[] dec = new float[RACES.all().size()];
		
		private PunishmentDec(LISTE<Punishment> all, Json json, String key, PUNISHMENT_SERVICE room, double mul) {
			super(all, json,key, room, ((RoomBlueprintImp) room).iconBig(), mul);
			savers.add(new SAVABLE() {
				
				@Override
				public void save(FilePutter f) {
					f.fsE(dec);
				}
				
				@Override
				public void load(FileGetter f) throws IOException {
					f.fsE(dec);
				}
				
				@Override
				public void clear() {
					Arrays.fill(dec, (float)0);
				}
			});
		}
		
		public double limit(Race race) {
			if (race == null) {
				double rr = 0;
				for (int i = 0; i < dec.length; i++)
					rr += dec[i];
				return rr / dec.length;
			}
			
			return dec[race.index];
		}
		
		public void limitSet(Race race, double lim) {
			if (race == null) {
				for (int i = 0; i < dec.length; i++)
					limitSet(RACES.all().get(i), lim);
				return;
			}
			
			lim = CLAMP.d(lim, 0, 1);
			dec[race.index] = (float) lim;
			double total = 0;
			for (PunishmentDec p : punishmentsdec) {
				total += p.dec[race.index];
			}
			
			
			total -= 1.0;
			if (total <= 0) {
				return;
			}
			
			int k = punishmentsdec.size();
			while(total > 0 && k > 0) {
				k--;
				double am = total/(punishmentsdec.size()-1);
				for (PunishmentDec p : punishmentsdec) {
					if (p != PunishmentDec.this) {
						double a = Math.min(am, p.dec[race.index]);
						p.dec[race.index] -= a;
						total -= a;
					}
				}
			}
			

		}
		
	}
	
}
