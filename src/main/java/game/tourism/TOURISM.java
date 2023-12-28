package game.tourism;

import java.io.IOException;

import game.GAME.GameResource;
import game.Profiler;
import game.faction.FACTIONS;
import game.faction.FCredits.CTYPE;
import game.time.TIME;
import init.config.Config;
import init.paths.PATHS;
import init.race.RACES;
import init.race.Race;
import settlement.main.SETT;
import settlement.room.main.RoomBlueprintIns;
import settlement.stats.Induvidual;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.*;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.*;
import util.statistics.HISTORY_INT;
import util.statistics.HistoryInt;
import view.ui.wiki.WIKI;

public final class TOURISM extends GameResource{

	final static double CREDITS = Config.SETT.TOURIST_CRETIDS;
	final static int MIN_EMPLOYEES = 100;
	final static double MAX_EMPLOYEES = 1000;
	public final static int AMOUNT = Config.SETT.TOURIST_PER_YEAR_MAX;
	
	
	static TOURISM self;
	
	private final Review[] reviews = new Review[32];
	private final ArrayList<Review> list = new ArrayList<>(32);
//	final LIST<Tuple<StatNeed, LIST<ROOM_SERVICE_ACCESS_HASER>>> needs;
	private final LIST<Race> tourists;
	final HistoryInt history = new HistoryInt(24, TIME.seasons(), false);
	private final Updater updater;
	private final Bitmap1D permit = new Bitmap1D(RACES.all().size(), false);
	private double score = 0;
	private final ACTION wiki;
	
	public TOURISM(){
		super(false);
		self = this;
//		needs = new ArrayList<Tuple<StatNeed, LIST<ROOM_SERVICE_ACCESS_HASER>>>(
//				new Tuple.TupleImp<StatNeed, LIST<ROOM_SERVICE_ACCESS_HASER>>(
//						STATS.NEEDS().DIRTINESS, SETT.ROOMS().HYGINE),
//				new Tuple.TupleImp<StatNeed, LIST<ROOM_SERVICE_ACCESS_HASER>>(
//						STATS.NEEDS().HUNGER, SETT.ROOMS().EAT),
//				new Tuple.TupleImp<StatNeed, LIST<ROOM_SERVICE_ACCESS_HASER>>(
//						STATS.NEEDS().THIRST, SETT.ROOMS().DRINK),
//				new Tuple.TupleImp<StatNeed, LIST<ROOM_SERVICE_ACCESS_HASER>>(
//						STATS.NEEDS().CONSTIPATION, new ArrayList<ROOM_SERVICE_ACCESS_HASER>().join(SETT.ROOMS().LAVATORIES))
//				);
		for (int i = 0; i < reviews.length; i++)
			reviews[i] = new Review();
		
		LinkedList<Race> li = new LinkedList<>();
		for (Race r : RACES.all()) {
			if (r.tourism().occurence > 0) {
				li.add(r);
			}
		}
		tourists = new ArrayList<Race>(li);
		updater = new Updater();
		permit.setAll(true);
		wiki = WIKI.add(new Json(PATHS.RACE().text.getFolder("tourist").get("_WIKI")));
	}



	
	@Override
	protected void save(FilePutter file) {
		for (Review r : reviews)
			r.save(file);
		history.save(file);
		updater.save(file);
		permit.save(file);
		file.d(score);
		
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		for (Review r : reviews)
			r.load(file);
		history.load(file);
		updater.load(file);
		permit.load(file);
		score = file.d();
	}

	@Override
	protected void update(float ds, Profiler prof) {
		prof.logStart(TOURISM.class);
		updater.update(ds);
		prof.logEnd(TOURISM.class);
	}
	
	public static RoomBlueprintIns<?> attraction(Induvidual indu) {
		return Updater.attraction(indu);
	}
	
	public static LIST<Race> races(){
		return self.tourists;
	}
	
	public static HISTORY_INT history() {
		return self.history;
	}

	public static boolean permit(Race race) {
		return self.permit.get(race.index());
	}
	
	public static void permit(Race race, boolean perm) {
		self.permit.set(race.index(), perm);
	}
	
	public static double score() {
		return self.score;
	}
	
	public static void touristFinish(Induvidual tourist, COORDINATE inn) {
		if (SETT.ENTRY().beseiged()) {
			return;
		}
		
		Review v = self.reviews[self.reviews.length-1];
		for (int i = self.reviews.length-1; i > 0; i--) {
			self.reviews[i] = self.reviews[i-1];
		}
		self.reviews[0] = v;
		v.make(tourist, inn);
		
		self.score = (15*self.score + v.score)/16.0;
		self.score = CLAMP.d(self.score, 0, 1);
		
		if (SETT.ROOMS().INN.is(inn))
			SETT.ROOMS().INN.setReview(inn.x(), inn.y(), v);
		FACTIONS.player().credits().inc(v.credits, CTYPE.TOURISM);
	}
	
	public static LIST<Review> reviews(){
		self.list.clear();
		for (int i = 0; i < self.reviews.length; i++) {
			if (self.reviews[i].has())
				self.list.add(self.reviews[i]);
			else
				break;
		}
		return self.list;
	}
	
	public static ACTION wiki() {
		return self.wiki;
	}
	
}

