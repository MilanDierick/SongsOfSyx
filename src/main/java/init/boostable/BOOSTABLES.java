package init.boostable;

import game.GAME;
import init.D;
import init.biomes.CLIMATES;
import init.boostable.BoostableCollection.BRooms;
import init.boostable.BoostableCollection.Collection;
import init.sprite.Icons;
import init.sprite.SPRITES;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.RoomsJson;
import snake2d.util.file.Json;
import snake2d.util.sets.*;
import util.info.INFO;
import util.keymap.KEY_COLLECTION;

public class BOOSTABLES {

	static BOOSTABLES self;
	public static final String key = "BONUS";
	private final KeyMap<KEY_COLLECTION<? extends BOOSTABLE>> collMap = new KeyMap<>();
	final ArrayList<BOOSTABLE> all = new ArrayList<>(512);
	private final INFO info;	
	private final Physics physics;
	private final Battle battle;
	private final Behaviour behaviour;
	private final Rates rates;
	private final Civic civics;
	private final Start start;
	private final LIST<BOOSTABLE> military;
	public final LIST<BoostableCollection> collections;
	public final BRooms ROOMS;
	
	private BOOSTABLES() {
		self = this;
		
		D.gInit(this);
		
		info = new INFO(D.g("Bonus"), D.g("Bonuses"), D.g("desc", "Bonuses boosts certain areas of the game."), null);
	
		physics = new Physics();
		battle = new Battle();
		behaviour = new Behaviour();
		rates = new Rates();
		civics = new Civic();
		ROOMS = new BRooms();
		start = new Start();
		collections = new ArrayList<>(physics, behaviour, battle, rates, civics, ROOMS, start);
		for (KEY_COLLECTION<? extends BOOSTABLE> c : collections)
			collMap.put(c.key(), c);
		military = battle.all.join(physics.MASS, physics.STAMINA, physics.SPEED);
		
	}
	
	public static void init() {
		new BOOSTABLES();
	}
	
	public static void finishUp(GAME game) {
		double[][] cb = new double[all().size()][];
		for (BOOSTABLE b : all()) {
			cb[b.index] = b.climates;
			b.climates = null;
		}
		CLIMATES.initBonuses(self, cb);
		
		
	}
	
	public static Physics PHYSICS() {
		return self.physics;
	}
	
	public static Battle BATTLE() {
		return self.battle;
	}
	
	public static Behaviour BEHAVIOUR() {
		return self.behaviour;
	}
	
	public static Rates RATES() {
		return self.rates;
	}
	
	public static Civic CIVICS() {
		return self.civics;
	}
	
	public static INFO INFO() {
		return self.info;
	}
	
	public static BRooms ROOMS() {
		return self.ROOMS;
	}
	
	public static Start START() {
		return self.start;
	}
	
	public static LIST<BOOSTABLE> all(){
		return self.all;
	}
	
	public static LIST<BOOSTABLE> military(){
		return self.military;
	}
	
	public static LIST<BoostableCollection> colls(){
		return self.collections;
	}
	
	public static LIST<BBoost> boosts(Json json){
		
		LIST<BBoost> res = new ArrayList<>(0);
		if (json.has("BONUS_ADD")) {
			res = res.join(boosts("BONUS_ADD", json, false));
		}
		
		if (json.has("BONUS_MUL")) {
			res = res.join(boosts("BONUS_MUL", json, true));
		};
		
		if (json.has("BONUS"))
			json.error("We don't use 'BONUS' anymore. We use either 'BONUS_ADD' or 'BONUS_MUL'", key);;
		
		return res;
		
	}
	
	private static LIST<BBoost> boosts(String key, Json json, boolean mul){
		if (!json.has(key))
			return new ArrayList<>(0);
		
		json = json.json(key);
		LinkedList<BBoost> res = new LinkedList<>();
		for (String kcoll : json.keys()) {
			if (kcoll.equals("ROOM_TYPE")) {
				GAME.Warn(json.errorGet("we doen't use this no more! Put it in the ROOM clause", key));
				continue;
			}
			
			if (kcoll.equals("ROOM")) {
				new RoomsJson("ROOM", json) {
					
					@Override
					public void doWithTheJson(RoomBlueprintImp room, Json j, String key) {
						LIST<BOOSTABLERoom> ls = ROOMS().room2Bonus.get(room.key);
						if (ls == null || ls.size() == 0) {
							if (!key.equals(KEY_COLLECTION.WILDCARD))
								GAME.Warn(j.errorGet(room.key + "does not have any boosts.", key));
						}else
							for (BOOSTABLERoom r : ls) {
								addBoost(j, res, r, key, true, mul);
							}
					}
				};
				continue;
			}
			
			KEY_COLLECTION<? extends BOOSTABLE> c = self.collMap.get(kcoll);
			if (c == null) {
				String e = "Invalid json block: " + kcoll + " available: ROOM_TYPE, ";
				for (KEY_COLLECTION<? extends BOOSTABLE> cc : self.collections)
					e += cc.key() + ", ";
				GAME.Warn(json.errorGet(e, kcoll));
			}else {
				Json j = json.json(kcoll);
				for (String k : j.keys()) {
					if (k.equals(KEY_COLLECTION.WILDCARD)) {
						double v = j.d(k);
						for (BOOSTABLE b : c.all())
							res.add(new BBoost(b, v, mul));
						continue;
					}
					BOOSTABLE b = c.tryGet(k);
					if (b == null) {
						String e = "Invalid " + c.key() + ": " + k + " available: ";
						for (String s : c.available())
							e += s + ", ";
						GAME.Warn(json.errorGet(e, kcoll));
					}else
						addBoost(j, res, b, k, true, mul);
				}
			}
			
		}
		return new ArrayList<BBoost>(res);
		
	}
	
//	public static double[] create(Json json, boolean isMul) {
//		double[] bb = new double[all().size()];
//		if (isMul)
//			Arrays.fill(bb, 1);
//		
//		if (json != null) {
//			for (BBoost b : boosts(json)) {
//				bb[b.boost.index] = b.max();
//			}
//		}
//		return bb;
//	}
	
	private static void addBoost(Json j, LinkedList<BBoost> res, BOOSTABLE b, String key, boolean dominant, boolean mul) {
		
		double value = j.d(key);
		value = (int)(value*10000)/10000.0;
		
		if (!dominant) {
			for (BBoost o : res) {
				if (o.boost == b) {
					return;
				}
			}
			res.add(new BBoost(b, value, mul));
			return;
		}
		
		boolean dup = true;
		while(dup) {
			for (BBoost o : res) {
				if (o.boost == b) {
					res.remove(o);
					break;
				}
			}
			dup = false;
		}
		res.add(new BBoost(b, value, mul));
		
	}
	
	public static final class Physics extends Collection{
		
		Physics(){
			super("PHYSICS", D.g("Physics"));
		}
		public final BOOSTABLE MASS = pushMisc(s().law, "MASS");
		public final BOOSTABLE STAMINA = pushMisc(s().heat, "STAMINA");
		public final BOOSTABLE SPEED = pushMisc(s().arrow_right,"SPEED");
		public final BOOSTABLE ACCELERATION = pushMisc(s().speed,"ACCELERATION");
		public final BOOSTABLE HEALTH = pushMisc(s().heart, "HEALTH");
		public final BOOSTABLE DEATH_AGE = pushMisc(s().death, "DEATH_AGE");
		public final BOOSTABLE RESISTANCE_HOT = pushMisc(s().heat,"RESISTANCE_HOT");
		public final BOOSTABLE RESISTANCE_COLD = pushMisc(s().ice,"RESISTANCE_COLD");
		
	}
	
	public static final class Battle extends Collection{
		
		Battle(){
			super("BATTLE", D.g("Battle"));
		}
		public final BOOSTABLE OFFENCE = pushMisc(s().sword,"OFFENCE_SKILL");
		public final BOOSTABLE DEFENCE = pushMisc(s().shield,"DEFENCE_SKILL");
		public final BOOSTABLE RANGED_SKILL = pushMisc(s().bow,"RANGED_SKILL");
		public final BOOSTABLE ARMOUR = pushMisc(s().armour,"ARMOUR");
		public final BOOSTABLE PIERCE_DAMAGE = pushMisc(s().pierce,"PIERCE_DAMAGE");
		public final BOOSTABLE BLUNT_DAMAGE = pushMisc(s().fist,"BLUNT_DAMAGE");
		public final BOOSTABLE MORALE = pushMisc(s().standard,"MORALE");

	}
	
	
	
	public static final class Behaviour extends Collection{
		
		Behaviour(){
			super("BEHAVIOUR", D.g("Behaviour"));
		}
		
		public final BOOSTABLE LAWFULNESS = pushMisc(s().law,"LAWFULNESS");
		public final BOOSTABLE SUBMISSION = pushMisc(s().slave,"SUBMISSION");
		public final BOOSTABLE HAPPINESS = pushMisc(s().column,"HAPPINESS");
		public final BOOSTABLE SANITY = pushMisc(s().crazy,"SANITY");
	}
	
	public static final class Rates extends Collection{
		
		Rates(){
			super("RATES", D.g("Rates"));
		}
		public final BOOSTABLE DEFECATION = pushMisc(s().squatter,"DEFECATE");
		public final BOOSTABLE SOILING = pushMisc(s().fly,"SOIL");
		public final BOOSTABLE PIETY = pushMisc(s().eye,"PIETY");
		public final BOOSTABLE HUNGER = pushMisc(s().plate,"HUNGER");
		public final BOOSTABLE THIRST = pushMisc(s().jug,"THIRST");
		public final BOOSTABLE LEARNING_SKILL = pushMisc(s().vial,"LEARNING");
		public final BOOSTABLE DOCTOR = pushMisc(s().heart,"DOCTOR");
		public final BOOSTABLE TRAINING = pushMisc(s().sword,"TRAINING");
		
	}
	
	public static final class Start extends Collection{
		
		Start(){
			super("START", D.g("Start"));
		}
		
		public final BOOSTABLE LANDING  = pushMisc(s().arrowUp,"LANDING");
		public final BOOSTABLE KNOWLEDGE = pushMisc(s().admin,"KNOWLEDGE");
		

	}
	
	public static final class Civic extends Collection{
		
		Civic(){
			super("CIVIC", D.g("Civic"));
		}
		
		public final BOOSTABLE MAINTENANCE = pushMisc(s().degrade,"MAINTENANCE");
		public final BOOSTABLE TRADE = pushMisc(s().money,"TRADE");
		public final BOOSTABLE SPOILAGE = pushMisc(s().fly,	"SPOILAGE");
		public final BOOSTABLE ACCIDENT = pushMisc(s().boom, "ACCIDENT");
		public final BOOSTABLE FURNITURE = pushMisc(s().bed, "FURNITURE");
		public final BOOSTABLE RAIDING = pushMisc(s().citizen, "RAIDING");
		
	}
	

	
	
	private static Icons.S s(){
		return  SPRITES.icons().s;
	}
	
	
}
