package settlement.room.main;

import static settlement.main.SETT.*;

import java.io.IOException;

import game.boosting.BOOSTING;
import game.boosting.Boostable;
import game.time.TIME;
import settlement.main.SETT;
import settlement.misc.util.FSERVICE;
import settlement.path.AVAILABILITY;
import settlement.path.finder.SFinderRoomService;
import settlement.room.food.cannibal.ROOM_CANNIBAL;
import settlement.room.food.farm.ROOM_FARM;
import settlement.room.food.fish.ROOM_FISHERY;
import settlement.room.food.hunter2.ROOM_HUNTER;
import settlement.room.food.orchard.ROOM_ORCHARD;
import settlement.room.food.pasture.ROOM_PASTURE;
import settlement.room.health.asylum.ROOM_ASYLUM;
import settlement.room.health.hospital.ROOM_HOSPITAL;
import settlement.room.health.physician.ROOM_PHYSICIAN;
import settlement.room.home.HOMES;
import settlement.room.industry.mine.ROOM_MINE;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry;
import settlement.room.industry.refiner.ROOM_REFINER;
import settlement.room.industry.woodcutter.ROOM_WOODCUTTER;
import settlement.room.industry.workshop.ROOM_WORKSHOP;
import settlement.room.infra.admin.ROOM_ADMIN;
import settlement.room.infra.bench.ROOM_BENCH;
import settlement.room.infra.builder.ROOM_BUILDER;
import settlement.room.infra.elderly.ROOM_RESTHOME;
import settlement.room.infra.embassy.ROOM_EMBASSY;
import settlement.room.infra.export.ROOM_EXPORT;
import settlement.room.infra.gate.ROOM_GATE;
import settlement.room.infra.hauler.ROOM_HAULER;
import settlement.room.infra.importt.ROOM_IMPORT;
import settlement.room.infra.inn.ROOM_INN;
import settlement.room.infra.janitor.ROOM_JANITOR;
import settlement.room.infra.monument.ROOM_MONUMENT;
import settlement.room.infra.stockpile.ROOM_STOCKPILE;
import settlement.room.infra.torch.ROOM_TORCH;
import settlement.room.infra.transport.ROOM_TRANSPORT;
import settlement.room.knowledge.laboratory.ROOM_LABORATORY;
import settlement.room.knowledge.library.ROOM_LIBRARY;
import settlement.room.knowledge.school.ROOM_SCHOOL;
import settlement.room.knowledge.university.ROOM_UNIVERSITY;
import settlement.room.law.court.ROOM_COURT;
import settlement.room.law.execution.ROOM_EXECTUTION;
import settlement.room.law.guard.ROOM_GUARD;
import settlement.room.law.prison.ROOM_PRISON;
import settlement.room.law.slaver.ROOM_SLAVER;
import settlement.room.law.stocks.ROOM_STOCKS;
import settlement.room.main.category.RoomCategories;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.construction.CONSTRUCTION;
import settlement.room.main.copy.ROOM_COPY;
import settlement.room.main.employment.RoomEmployments;
import settlement.room.main.placement.PLACEMENT;
import settlement.room.main.throne.THRONE;
import settlement.room.main.util.*;
import settlement.room.military.artillery.ROOM_ARTILLERY;
import settlement.room.military.supply.ROOM_SUPPLY;
import settlement.room.military.training.archery.ROOM_ARCHERY;
import settlement.room.military.training.barracks.ROOM_BARRACKS;
import settlement.room.service.arena.grand.ROOM_ARENA;
import settlement.room.service.arena.pit.ROOM_FIGHTPIT;
import settlement.room.service.barber.ROOM_BARBER;
import settlement.room.service.food.canteen.ROOM_CANTEEN;
import settlement.room.service.food.eatery.ROOM_EATERY;
import settlement.room.service.food.tavern.ROOM_TAVERN;
import settlement.room.service.hearth.ROOM_HEARTH;
import settlement.room.service.hygine.bath.ROOM_BATH;
import settlement.room.service.hygine.well.ROOM_WELL;
import settlement.room.service.lavatory.ROOM_LAVATORY;
import settlement.room.service.market.ROOM_MARKET;
import settlement.room.service.module.RoomServiceAccess.ROOM_SERVICE_ACCESS_HASER;
import settlement.room.service.module.RoomServices;
import settlement.room.service.nursery.ROOM_NURSERY;
import settlement.room.service.speaker.ROOM_SPEAKER;
import settlement.room.service.stage.ROOM_STAGE;
import settlement.room.spirit.dump.ROOM_DUMP;
import settlement.room.spirit.grave.*;
import settlement.room.spirit.temple.ROOM_TEMPLES;
import settlement.room.tests.RoomTests;
import settlement.room.water.ROOM_WATER;
import settlement.room.water.pool.ROOM_POOL;
import settlement.tilemap.TileMap;
import settlement.tilemap.TileMap.SMinimapGetter;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.*;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;
import util.updating.IUpdater;

public final class ROOMS extends TileMap.Resource {

	public final static String KEY = "ROOM";
	public final static short ROOM_MAX = Short.MAX_VALUE;
	public final MapRoom map = new MapRoom();
	final MapRoomData.Data pData = new MapRoomData.Data();
	public final MapRoomData data = pData;
	public final Bitmap2D extraBit = new Bitmap2D(SETT.TILE_BOUNDS, false);
	
	public static final double UPDATE_INTERVAL = 32;
	
	private final IUpdater updater = new IUpdater(ROOM_MAX, UPDATE_INTERVAL) {

		int dayCurrent = TIME.days().bitsSinceStart();
		int dayCount = 0;
		int lastday;
		boolean day;
		
		@Override
		protected void update(int i, double timeSinceLast) {
			
			if (dayCurrent != TIME.days().bitsSinceStart()) {
				dayCurrent = TIME.days().bitsSinceStart();
				dayCount ++;
				lastday = i;
				day = true;
			}else if (lastday == i){
				day = false;
			}
			
			if (map.getByIndex(i) != null)
				map.getByIndex(i).update(timeSinceLast, day, dayCount);
		}
		
		@Override
		public void save(FilePutter file) {
			super.save(file);
			file.i(dayCurrent);
			file.i(dayCount);
			file.i(lastday);
			file.bool(day);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			super.load(file);
			dayCurrent = file.i();
			dayCount = file.i();
			lastday = file.i();
			day = file.bool();
		}
	};

	public final Deleter DELETE = new Deleter(this);
	public final RoomIsolation isolation = new RoomIsolation(this);
	final TmpArea tmpArea = new TmpArea(this);
	public final PLACEMENT placement = new PLACEMENT(this);
	public final CONSTRUCTION construction = new CONSTRUCTION(this);
	public final ROOM_COPY copy;
	public final MapDataF fData = new MapDataF(this);
	public final RoomCategories CATS = new RoomCategories(this);
	public final RoomStats stats = new RoomStats();
	
	private RoomInitData init = new RoomInitData(this).setType(null);
	
	
	public final ROOM_STOCKPILE STOCKPILE = new ROOM_STOCKPILE(init, CATS.LOGISTICS);
	public final ROOM_EXPORT EXPORT = new ROOM_EXPORT(init, CATS.LOGISTICS);
	public final ROOM_IMPORT IMPORT = new ROOM_IMPORT(init, CATS.LOGISTICS);
	public final ROOM_SUPPLY SUPPLY = new ROOM_SUPPLY(init, CATS.MILITARY);
	public final ROOM_HAULER HAULER = new ROOM_HAULER(init, CATS.LOGISTICS);
	public final ROOM_TRANSPORT TRANSPORT = new ROOM_TRANSPORT(init, CATS.LOGISTICS);
	public final ROOM_JANITOR JANITOR = new ROOM_JANITOR(init, CATS.MAIN_MISC.misc);
	public final ROOM_EMBASSY EMBASSY = new ROOM_EMBASSY(init, CATS.ADMIN);
	public final ROOM_DUMP DUMP = new ROOM_DUMP(init, CATS.SER_DEATH);
	public final ROOM_WOODCUTTER WOOD_CUTTER = new ROOM_WOODCUTTER(init, CATS.MAIN_INDUSTRY.misc);

	public final ROOM_CANNIBAL CANNIBAL = new ROOM_CANNIBAL(init, CATS.MAIN_AGRIULTURE.misc);
	public final ROOM_WATER WATER = new ROOM_WATER(init, CATS.WATER);
	
	public final ROOM_GUARD GUARD = new ROOM_GUARD(init, CATS.LAW);
	public final ROOM_PRISON PRISON = new ROOM_PRISON(init, CATS.LAW);
	public final ROOM_EXECTUTION EXECUTION = new ROOM_EXECTUTION(init, CATS.LAW);
	public final ROOM_SLAVER SLAVER = new ROOM_SLAVER(init, CATS.LAW);
	public final ROOM_STOCKS STOCKS = new ROOM_STOCKS(init, CATS.LAW);
	public final ROOM_COURT COURT = new ROOM_COURT(init, CATS.LAW);
	public final THRONE THRONE = new THRONE(init, CATS.MAIN_MISC.misc);
	public final ROOM_BUILDER BUILDER = new ROOM_BUILDER(init, CATS.MAIN_MISC.misc);
	public final ROOM_HEARTH HEARTH = new ROOM_HEARTH("_HEARTH", 0, init, CATS.SER_OTHER);
	public final HOMES HOMES = new HOMES(init, CATS.SER_OTHER);
	public final ROOM_ASYLUM ASYLUM = new ROOM_ASYLUM(init, CATS.SER_HEALTH);

	public final ROOM_HOSPITAL HOSPITAL = new ROOM_HOSPITAL(init, CATS.SER_HEALTH);
	public final ROOM_INN INN = new ROOM_INN(init, CATS.ADMIN);
	
	public final ROOM_TORCH TORCH = new ROOM_TORCH(init, CATS.DECOR);
	public final ROOM_BENCH BENCH = new ROOM_BENCH(init, CATS.DECOR);
	
	public final LIST<ROOM_POOL> POOLS = new RoomsCreator<ROOM_POOL>(init, "POOL",
			CATS.WATER) {
		
		@Override
		public ROOM_POOL create(String key, RoomInitData data, RoomCategorySub cat, int index)
				throws IOException {
			return new ROOM_POOL(index, data, key, cat);
		}
	}.all();
	
	public final LIST<ROOM_BARRACKS> BARRACKS = new RoomsCreator<ROOM_BARRACKS>(init, "BARRACKS",
			CATS.MILITARY) {
		
		@Override
		public ROOM_BARRACKS create(String key, RoomInitData data, RoomCategorySub cat, int index)
				throws IOException {
			return new ROOM_BARRACKS(index, data, key);
		}
	}.all();
	
	public final LIST<ROOM_ARCHERY> ARCHERIES = new RoomsCreator<ROOM_ARCHERY>(init, "ARCHERY",
			CATS.MILITARY) {
		
		@Override
		public ROOM_ARCHERY create(String key, RoomInitData data, RoomCategorySub cat, int index)
				throws IOException {
			return new ROOM_ARCHERY(index, data, key);
		}
	}.all();

	
	public final LIST<ROOM_MONUMENT> MONUMENTS = new RoomsCreator<ROOM_MONUMENT>(init, "MONUMENT",
			CATS.DECOR) {
		
		@Override
		public ROOM_MONUMENT create(String key, RoomInitData data, RoomCategorySub cat, int index)
				throws IOException {
			return new ROOM_MONUMENT(data, index, key, cat);
		}
	}.all();

	public final LIST<ROOM_GATE> GATES = new RoomsCreator<ROOM_GATE>(init, ROOM_GATE.type,
			CATS.MILITARY) {
		
		@Override
		public ROOM_GATE create(String key, RoomInitData data, RoomCategorySub cat, int index)
				throws IOException {
			return new ROOM_GATE(data, index, key, cat);
		}
	}.all();

	public final LIST<ROOM_ARTILLERY> ARTILLERY = new RoomsCreator<ROOM_ARTILLERY>(init, ROOM_ARTILLERY.type,
			CATS.MILITARY) {
		
		@Override
		public ROOM_ARTILLERY create(String key, RoomInitData data, RoomCategorySub cat, int index)
				throws IOException {
			return new ROOM_ARTILLERY(index, data, key, cat);
		}
	}.all();
	


	
	public final LIST<ROOM_FISHERY> FISHERIES = new RoomsCreator<ROOM_FISHERY>(init, ROOM_FISHERY.type,
			CATS.FISH) {
		
		@Override
		public ROOM_FISHERY create(String key, RoomInitData data, RoomCategorySub cat, int index)
				throws IOException {
			return new ROOM_FISHERY(data, key, index, cat);
		}
	}.all();
	
	public final LIST<ROOM_MINE> MINES = new RoomsCreator<ROOM_MINE>(init, ROOM_MINE.type,
			CATS.MINES) {
		@Override
		public ROOM_MINE create(String key, RoomInitData data, RoomCategorySub cat, int index)
				throws IOException {
			return new ROOM_MINE(data, key, index, cat);
		}
	}.all();
	

	public final LIST<ROOM_FARM> FARMS = new RoomsCreator<ROOM_FARM>(init, ROOM_FARM.type, CATS.FARMS) {

		@Override
		public ROOM_FARM create(String key, RoomInitData data, RoomCategorySub cat, int index) throws IOException {
			return new ROOM_FARM(data, key, cat, index);
		}

	}.all();
	
	public final LIST<ROOM_ORCHARD> ORCHARDS = new RoomsCreator<ROOM_ORCHARD>(init, ROOM_ORCHARD.type, CATS.FARMS) {

		@Override
		public ROOM_ORCHARD create(String key, RoomInitData data, RoomCategorySub cat, int index) throws IOException {
			return new ROOM_ORCHARD(data, key, cat, index);
		}

	}.all();

	public final LIST<ROOM_PASTURE> PASTURES = new RoomsCreator<ROOM_PASTURE>(init, ROOM_PASTURE.type, CATS.HUSBANDRY) {

		@Override
		public ROOM_PASTURE create(String key, RoomInitData data, RoomCategorySub cat, int index) throws IOException {
			return new ROOM_PASTURE(data, key, cat, index);
		}

	}.all();
	
	public final LIST<ROOM_HUNTER> HUNTERS = new RoomsCreator<ROOM_HUNTER>(init, ROOM_HUNTER.type, CATS.MAIN_AGRIULTURE.misc) {

		@Override
		public ROOM_HUNTER create(String key, RoomInitData data, RoomCategorySub cat, int index) throws IOException {
			return new ROOM_HUNTER(index, data, key, cat);
		}

	}.all();

	public final LIST<ROOM_REFINER> REFINERS = new RoomsCreator<ROOM_REFINER>(init, ROOM_REFINER.type,
			CATS.REFINERS) {

		@Override
		public ROOM_REFINER create(String key, RoomInitData data, RoomCategorySub cat, int index) throws IOException {
			return new ROOM_REFINER(data, key, index, cat);
		}

	}.all();

	public final LIST<ROOM_WORKSHOP> WORKSHOPS = new RoomsCreator<ROOM_WORKSHOP>(init, ROOM_WORKSHOP.type,
			CATS.CRAFTING) {

		@Override
		public ROOM_WORKSHOP create(String key, RoomInitData data, RoomCategorySub cat, int index) throws IOException {
			return new ROOM_WORKSHOP(index, data, key, cat);
		}

	}.all();


	public final LIST<ROOM_SPEAKER> SPEAKERS = new RoomsCreator<ROOM_SPEAKER>(init, "SPEAKER",
			CATS.SER_ENTERTAIN) {
		@Override
		public ROOM_SPEAKER create(String key, RoomInitData data, RoomCategorySub cat, int index) throws IOException {
			return new ROOM_SPEAKER(key, index, data, cat);
		}

	}.all();
	public final LIST<ROOM_STAGE> STAGES = new RoomsCreator<ROOM_STAGE>(init, "STAGE",
			CATS.SER_ENTERTAIN) {
		@Override
		public ROOM_STAGE create(String key, RoomInitData data, RoomCategorySub cat, int index) throws IOException {
			return new ROOM_STAGE(key, index, data, cat);
		}

	}.all();
	public final LIST<ROOM_FIGHTPIT> ARENAS = new RoomsCreator<ROOM_FIGHTPIT>(init, "ARENA",
			CATS.SER_ENTERTAIN) {
		@Override
		public ROOM_FIGHTPIT create(String key, RoomInitData data, RoomCategorySub cat, int index) throws IOException {
			return new ROOM_FIGHTPIT(key, index, data, cat);
		}

	}.all();
	public final LIST<ROOM_ARENA> GARENAS = new RoomsCreator<ROOM_ARENA>(init, "ARENAG",
			CATS.SER_ENTERTAIN) {
		@Override
		public ROOM_ARENA create(String key, RoomInitData data, RoomCategorySub cat, int index) throws IOException {
			return new ROOM_ARENA(key, index, data, cat);
		}

	}.all();
	

	public final SFinderRoomService graveServiceSpots = new SFinderRoomService("mourn") {
		
		@Override
		public FSERVICE get(int tx, int ty) {
			RoomBlueprint p = map.blueprint.get(tx, ty);
			if (p != null && p instanceof GraveData.GRAVE_DATA_HOLDER)
				return ((GraveData.GRAVE_DATA_HOLDER) p).graveData().burrialService(tx, ty);
			return null;
		}
	};
	public final LIST<ROOM_GRAVEYARD> GRAVEYARDS = new RoomsCreator<ROOM_GRAVEYARD>(init, "GRAVEYARD",
			CATS.SER_DEATH) {


		@Override
		public ROOM_GRAVEYARD create(String key, RoomInitData data, RoomCategorySub cat, int index) throws IOException {
			return new ROOM_GRAVEYARD(index, key, data, cat, graveServiceSpots);
		}

	}.all();
	
	public final LIST<ROOM_TOMB> TOMBS = new RoomsCreator<ROOM_TOMB>(init, "TOMB",
			CATS.SER_DEATH) {

		@Override
		public ROOM_TOMB create(String key, RoomInitData data, RoomCategorySub cat, int index) throws IOException {
			return new ROOM_TOMB(index, key, data, cat, graveServiceSpots);
		}

	}.all();
	
	public final LIST<GraveData.GRAVE_DATA_HOLDER> GRAVES = new ArrayList<GraveData.GRAVE_DATA_HOLDER>(0).join(GRAVEYARDS).join(TOMBS);

	public final ROOM_TEMPLES TEMPLES = new ROOM_TEMPLES(this, init);

	public final LIST<ROOM_CANTEEN> CANTEENS = new RoomsCreator<ROOM_CANTEEN>(init, "CANTEEN", CATS.SER_CONSUMPTION) {

		@Override
		public ROOM_CANTEEN create(String key, RoomInitData data, RoomCategorySub cat, int index) throws IOException {
			return new ROOM_CANTEEN(key, index, data, cat);
		}

	}.all();
	
	public final LIST<ROOM_EATERY> EATERIES = new RoomsCreator<ROOM_EATERY>(init, "EATERY", CATS.SER_CONSUMPTION) {

		@Override
		public ROOM_EATERY create(String key, RoomInitData data, RoomCategorySub cat, int index) throws IOException {
			return new ROOM_EATERY(key, index, data, cat);
		}

	}.all();
	
	public final LIST<ROOM_TAVERN> TAVERNS = new RoomsCreator<ROOM_TAVERN>(init, "TAVERN", CATS.SER_CONSUMPTION) {

		@Override
		public ROOM_TAVERN create(String key, RoomInitData data, RoomCategorySub cat, int index) throws IOException {
			return new ROOM_TAVERN(key, index, data, cat);
		}

	}.all();
	
	public final LIST<ROOM_BATH> BATHS = new RoomsCreator<ROOM_BATH>(init, "BATH", CATS.SER_HEALTH) {

		@Override
		public ROOM_BATH create(String key, RoomInitData data, RoomCategorySub cat, int index) throws IOException {
			return new ROOM_BATH(key, index, data, cat);
		}

	}.all();
	
	public final LIST<ROOM_WELL> WELLS = new RoomsCreator<ROOM_WELL>(init, "WELL", CATS.SER_HEALTH) {

		@Override
		public ROOM_WELL create(String key, RoomInitData data, RoomCategorySub cat, int index) throws IOException {
			return new ROOM_WELL(key, index, data, cat);
		}

	}.all();

	public final LIST<ROOM_BARBER> BARBERS = new RoomsCreator<ROOM_BARBER>(init, ROOM_BARBER.TYPE, CATS.SER_OTHER) {

		@Override
		public ROOM_BARBER create(String key, RoomInitData data, RoomCategorySub cat, int index) throws IOException {
			return new ROOM_BARBER(data, index, key, cat);
		}

	}.all();
	
	public final LIST<ROOM_MARKET> MARKET = new RoomsCreator<ROOM_MARKET>(init, ROOM_MARKET.TYPE, CATS.SER_CONSUMPTION) {

		@Override
		public ROOM_MARKET create(String key, RoomInitData data, RoomCategorySub cat, int index) throws IOException {
			return new ROOM_MARKET(key, index, data, cat);
		}

	}.all();
	
	public final LIST<ROOM_LAVATORY> LAVATORIES = new RoomsCreator<ROOM_LAVATORY>(init, "LAVATORY", CATS.SER_HEALTH) {

		@Override
		public ROOM_LAVATORY create(String key, RoomInitData data, RoomCategorySub cat, int index) throws IOException {
			return new ROOM_LAVATORY(data, index, key, cat);
		}

	}.all();
	
	public final LIST<ROOM_PHYSICIAN> PHYSICIANS = new RoomsCreator<ROOM_PHYSICIAN>(init, "PHYSICIAN", CATS.SER_HEALTH) {

		@Override
		public ROOM_PHYSICIAN create(String key, RoomInitData data, RoomCategorySub cat, int index) throws IOException {
			return new ROOM_PHYSICIAN(key, index, data, cat);
		}

	}.all();
	
//	public final LIST<ROOM_SERVICE_ACCESS_HASER> HYGINE = new ArrayList<ROOM_SERVICE_ACCESS_HASER>().join(BATHS).join(WELLS);
//	public final LIST<ROOM_SERVICE_ACCESS_HASER> EAT = new ArrayList<ROOM_SERVICE_ACCESS_HASER>().join(EATERIES).join(CANTEENS);
//	public final LIST<ROOM_SERVICE_ACCESS_HASER> DRINK = new ArrayList<ROOM_SERVICE_ACCESS_HASER>().join(TAVERNS);
//	public final LIST<ROOM_SERVICE_ACCESS_HASER> ENTERTAINMENT = new ArrayList<ROOM_SERVICE_ACCESS_HASER>(0)
//			.join(SPEAKERS).join(STAGES).join(ARENAS).join(GARENAS);

	
	public final LIST<ROOM_LABORATORY> LABORATORIES = new RoomsCreator<ROOM_LABORATORY>(init, ROOM_LABORATORY.type, CATS.ADMIN) {

		@Override
		public ROOM_LABORATORY create(String key, RoomInitData data, RoomCategorySub cat, int index) throws IOException {
			return new ROOM_LABORATORY(key, index, data, cat);
		}

	}.all();
	
	public final LIST<ROOM_LIBRARY> LIBRARIES = new RoomsCreator<ROOM_LIBRARY>(init, ROOM_LIBRARY.type, CATS.ADMIN) {

		@Override
		public ROOM_LIBRARY create(String key, RoomInitData data, RoomCategorySub cat, int index) throws IOException {
			return new ROOM_LIBRARY(key, index, data, cat);
		}

	}.all();
	
	public final LIST<ROOM_SCHOOL> SCHOOLS = new RoomsCreator<ROOM_SCHOOL>(init, "SCHOOL", CATS.ADMIN) {

		@Override
		public ROOM_SCHOOL create(String key, RoomInitData data, RoomCategorySub cat, int index) throws IOException {
			return new ROOM_SCHOOL(key, index, data, cat);
		}

	}.all();
	public final LIST<ROOM_UNIVERSITY> UNIVERSITIES = new RoomsCreator<ROOM_UNIVERSITY>(init, "UNIVERSITY", CATS.ADMIN) {

		@Override
		public ROOM_UNIVERSITY create(String key, RoomInitData data, RoomCategorySub cat, int index) throws IOException {
			return new ROOM_UNIVERSITY(key, index, data, cat);
		}

	}.all();
	public final LIST<ROOM_RESTHOME> RESTHOMES = new RoomsCreator<ROOM_RESTHOME>(init, "RESTHOME", CATS.SER_OTHER) {

		@Override
		public ROOM_RESTHOME create(String key, RoomInitData data, RoomCategorySub cat, int index) throws IOException {
			return new ROOM_RESTHOME(key, index, data, cat);
		}

	}.all();
	
	public final LIST<ROOM_SERVICE_ACCESS_HASER> EDUCATION = new ArrayList<ROOM_SERVICE_ACCESS_HASER>().join(SCHOOLS);
	
	public final LIST<ROOM_ADMIN> ADMINS = new RoomsCreator<ROOM_ADMIN>(init, ROOM_ADMIN.type, CATS.ADMIN) {

		@Override
		public ROOM_ADMIN create(String key, RoomInitData data, RoomCategorySub cat, int index) throws IOException {
			return new ROOM_ADMIN(key, index, data, cat);
		}

	}.all();
	
	public final LIST<ROOM_NURSERY> NURSERIES = new RoomsCreator<ROOM_NURSERY>(init, "NURSERY", CATS.BREEDING) {

		@Override
		public ROOM_NURSERY create(String key, RoomInitData data, RoomCategorySub cat, int index) throws IOException {
			return new ROOM_NURSERY(index, data, cat, key);
		}

	}.all();

	{
		for (RoomCreator c : RoomCreator.scriptRooms) {
			c.createBlueprint(init);
		}
		RoomBlueprint.ALL.trim();
	}

	public final RoomEmployments employment;

	public final LIST<Industry> INDUSTRIES;
	
	public final int AMOUNT_OF_BLUEPRINTS = RoomBlueprint.ALL.size();
	
	public final FlatIndustries FIndustries;
	
	private final BonusExp exp;
	
	static ROOMSLookup lookup;
	
	public final RBonus bonus;
	
	public final RoomServices SERVICE;
	
	public final RoomProduction PROD;
	
	public ROOMS() throws IOException {
		
		
		
		
		
		int am = 0;
		for (RoomBlueprint b : all()) {
			if (b instanceof INDUSTRY_HASER) {
				INDUSTRY_HASER h = (INDUSTRY_HASER) b;
				am += h.industries().size();
			}
				
		}
		
		ArrayList<Industry> hh = new ArrayList<>(am);
		for (RoomBlueprint b : all()) {
			if (b instanceof INDUSTRY_HASER) {
				INDUSTRY_HASER h = (INDUSTRY_HASER) b;
				for (Industry i : h.industries())
					hh.add(i);
			}
				
		}
		lookup = new ROOMSLookup(this);
		employment = new RoomEmployments(this);
		INDUSTRIES = new ArrayList<>(hh);
		FIndustries = new FlatIndustries(this);
		exp = new BonusExp();
		copy = new ROOM_COPY(this);
		bonus = new RBonus(all());
		SERVICE = init.service;
		PROD = new RoomProduction(this);
		new RoomTests(this);
	}

	public boolean thereCanBeMoreRooms() {
		return map.thereCanBeMoreRooms();
	}

	public Room getByIndex(int index) {
		return map.getByIndex(index);
	}

	@Override
	protected void save(FilePutter file) {

		pData.save(file);
		fData.saver.save(file);
		map.saver.save(file);
		extraBit.save(file);
		updater.save(file);

		employment.saver.save(file);
		for (RoomBlueprint p : RoomBlueprint.ALL)
			p.save(file);
		((RoomResource)stats).save(file);
		HOMES.saver.save(file);
		exp.save(file);
		placement.saver.save(file);
	}

	@Override
	protected void load(FileGetter file) throws IOException {

		pData.load(file);
		fData.saver.load(file);
		map.saver.load(file);
		extraBit.load(file);
		updater.load(file);

		employment.saver.load(file);
		for (RoomBlueprint p : RoomBlueprint.ALL) {
			p.load(file);
		}

		((RoomResource)stats).load(file);
		
		HOMES.saver.load(file);
		exp.load(file);
		// for (COORDINATE c : Settlement.TILE_BOUNDS) {
		// Room r = get(c);
		// if (r instanceof JOBMANAGER_HASER && r instanceof FeastInstance) {
		// JOBMANAGER_HASER jh = (JOBMANAGER_HASER) r;
		// SETT_JOB j = jh.getWork().getJob(c);
		// if (j != null)
		// j.jobReserveCancel(null);
		// }
		// }

		// for (COORDINATE c : Settlement.TILE_BOUNDS) {
		// FSERVICE s = SERVICE.KITCHEN.service.service(c.x(), c.y());
		// if (s == null)
		// continue;
		// while(s.findableReservedIs()) {
		// s.findableReserveCancel();
		// }
		// }
		placement.saver.load(file);
	}

	@Override
	protected void clearAll() {
		
		pData.clear();
		fData.saver.clear();
		map.saver.clear();
		extraBit.clear();
		// fix singletons

		employment.saver.clear();

		for (RoomBlueprint p : RoomBlueprint.ALL)
			p.clear();
		((RoomResource)stats).clear();
		HOMES.saver.clear();
		exp.clear();
	}

	public void render(Renderer r, ShadowBatch shadowBatch, RenderData data, int zoom) {

		shadowBatch.setHard();
		RenderData.RenderIterator i = data.onScreenTiles();

		while (i.has()) {
			Room room = this.map.get(i.tx(), i.ty());

			if (room != null) {

				if (room.render(r, shadowBatch, i)) {
					i.hiddenSet();
				}
			}

			i.next();
		}

		shadowBatch.setSoft();

	}

	public void renderAbove(Renderer r, ShadowBatch shadowBatch, RenderData data, int zoom) {

		shadowBatch.setHard();
		RenderData.RenderIterator i = data.onScreenTiles();

		while (i.has()) {

			Room room = this.map.get(i.tile());

			if (room != null) {

				if (room.renderAbove(r, shadowBatch, i)) {
					i.hiddenSet();
				}
			}

			i.next();
		}

		shadowBatch.setSoft();

	}

	public void renderAfterGround(Renderer r, ShadowBatch shadowBatch, RenderData.RenderIterator it) {

		Room room = this.map.get(it.tile());
		if (room != null) {

			if (room.renderBelow(r, shadowBatch, it)) {
				it.hiddenSet();
			}
		}
	}

	@Override
	protected void update(float ds) {
		for (RoomBlueprint b : RoomBlueprint.ALL)
			b.update(ds);

		updater.update(ds);
		employment.update(ds);
		exp.update(ds);
	}

	public AVAILABILITY getAvailability(int tx, int ty) {
		int t = tx + ty * TWIDTH;
		if (map.is(t))
			return map.get(t).getAvailability(t);
		return null;
	}
	
	public SMinimapGetter miniC = new SMinimapGetter() {
		
		@Override
		public COLOR miniColorPimped(ColorImp origional, int tx, int ty, boolean northern, boolean southern) {
			Room r = map.getRaw(tx, ty);
			if (r != null)
				return r.blueprint().miniCPimped(origional, tx, ty, northern, southern);
			return origional;
		}
		
		@Override
		public COLOR miniC(int tx, int ty) {
			Room r = map.getRaw(tx, ty);
			if (r != null)
				return r.blueprint().miniC(tx, ty);
			return null;
		}
	};

	public LIST<RoomBlueprint> all() {
		return RoomBlueprint.ALL;
	}
	
	public LIST<RoomBlueprintImp> imps() {
		return RoomBlueprintImp.IMPS;
	}

	public int nrOfRooms() {
		return map.nrOFRooms() - 7;
	}
	
	@Override
	protected void afterTick() {
		isolation.update();
	}
	
	public TmpArea tmpArea(Object user) {
		tmpArea.init(user);
		return tmpArea;
	}
	
	final static class ROOMSLookup {

		final KeyMap<RoomBlueprintImp> look = new KeyMap<>();
		final KeyMap<LinkedList<RoomBlueprintImp>> cats = new KeyMap<>();
		
		ROOMSLookup(ROOMS rooms) {
			
			for (RoomBlueprintImp bi : rooms.imps()) {
				look.put(bi.key, bi);
				if (bi.type == null)
					continue;
				if (!cats.containsKey(bi.type))
					cats.put(bi.type, new LinkedList<>());
				cats.get(bi.type).add(bi);
			}
			
		}
	}
	
	public static abstract class RoomResource{

		protected abstract void save(FilePutter file);

		protected abstract void load(FileGetter file) throws IOException;

		protected abstract void clear();
		
		protected abstract void update(float ds);
		
		
	}
	
	public static final class RBonus {

		
		public final LIST<RoomBlueprintImp> all;
		private final RoomBlueprintImp[] map = new RoomBlueprintImp[BOOSTING.ALL().size()];
		{
			
		}
		
		public RoomBlueprintImp get(Boostable bo) {
			if (bo.index() >= map.length)
				return null;
			return map[bo.index()];
			
		}
		
		RBonus(LIST<RoomBlueprint> rooms){
			LinkedList<RoomBlueprintImp> all = new LinkedList<>();
			
			for (RoomBlueprint p : rooms) {
				if (p instanceof RoomBlueprintImp) {
					RoomBlueprintImp b = (RoomBlueprintImp) p;
					if (b.bonus() != null) {
						map[b.bonus().index()] = b;
						all.add(b);
					}
				}
			}
			this.all = new ArrayList<RoomBlueprintImp>(all);
		}
		
	}
	
}
