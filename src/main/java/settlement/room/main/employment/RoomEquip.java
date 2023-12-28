package settlement.room.main.employment;

import java.io.IOException;

import game.GAME;
import game.boosting.*;
import game.faction.Faction;
import game.faction.npc.NPCBonus;
import init.D;
import init.race.POP_CL;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.room.main.*;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import snake2d.util.file.*;
import snake2d.util.sets.*;
import snake2d.util.sprite.text.Str;
import util.data.INT.INTE;
import util.data.INT.IntImp;
import util.info.INFO;

public class RoomEquip implements INDEXED{

	private final ArrayListGrower<RoomEmploymentSimple> rooms = new ArrayListGrower<>();
	private final ArrayList<IntImp> targets;
	private final int[] currents;
	private int total;
	private final int index;
	public final double degradePerDay;
	private final int defaultTarget;
	public final RESOURCE resource;
	public final BoostSpecs boosts;
	public final int maxAm;
	private static CharSequence ¤¤equipment = "Equipment";
	private static CharSequence ¤¤equipmentD = "Equipment boosts the efficiency of rooms. When equipped, each {0} degrades with a rate of {1} % per day.";
	
	private BoostSpec[] boostMap;
	
	public final INFO info;
	
	static {
		D.ts(RoomEquip.class);
	}
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			file.i(targets.size());
			file.i(total);
			for (int i = 0; i < targets.size(); i++) {
				file.i(targets.get(i).get());
				file.i(currents[i]);
			}
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			int am = file.i();
			total = file.i();
			
			if (am != targets.size()) {
				for (int i = 0; i < am; i++) {
					file.i();
					file.i();
				}
				clear();
			}else {
				for (int i = 0; i < targets.size(); i++) {
					targets.get(i).set(file.i());
					currents[i] = file.i();
				}
			}
			
			
		}
		
		@Override
		public void clear() {
			total = 0;
			for (int i = 0; i < targets.size(); i++) {
				targets.get(i).set(defaultTarget);
				currents[i] = 0;
			}
		}
	};
	
	RoomEquip(LISTE<RoomEquip> all, RoomEmployments emps, Json data) {
		targets = new ArrayList<>(emps.ALLS().size());
		while(targets.hasRoom())
			targets.add(new IntImp(0,0));
		currents = new int[emps.ALLS().size()];
		boostMap = new BoostSpec[emps.ALLS().size()];
		
		
		index = all.add(this);
		resource = RESOURCES.map().get(data);
		degradePerDay = data.d("WEAR_PER_DAY", 0, 1);
		defaultTarget = data.i("DEFAULT_TARGET");
		info = new INFO(¤¤equipment + ": " + resource.names, ""+Str.TMP.clear().add(¤¤equipmentD).insert(0, resource.name).insert(1, degradePerDay*100, 2));
		boosts = new BoostSpecs(resource.names, resource.icon(), true);
		double add = data.d("BOOST_MAX_VALUE");
		
		
		
		new RoomsJson("EQUIP_AMOUNTS", data) {
			
			@Override
			public void doWithTheJson(RoomBlueprintImp room, Json j, String key) {
				
				if (room.bonus() == null || room.employment() == null) {
					GAME.WarnLight(data.errorGet("Not a valid boostable room " + key, key));
					return;
				}
				rooms.add(room.employment());
				int am = j.i(key, 0, 100);
				targets.get(room.employment().eindex()).max = am;
				targets.get(room.employment().eindex()).set(defaultTarget);
			}
		};
		
		int m = 0;
		
		for (RoomEmploymentSimple e : rooms) {
			m = Math.max(m, targets.get(e.eindex()).max());
		}
		
		maxAm = m;
		
		for (RoomEmploymentSimple e : rooms) {
			m = targets.get(e.eindex()).max();
			final double to = Math.ceil(add*100*targets.get(e.eindex()).max())/(maxAm*100.0);
			
			BoosterSimple bo = new BoosterSimple(new BSourceInfo(resource.names, resource.icon()), false) {
				
				@Override
				public double vGet(Faction f) {
					return 0;
				}
				
				@Override
				public double vGet(NPCBonus bonus) {
					return bonus.get(index);
				}
				
				@Override
				public double vGet(POP_CL reg) {
					return RoomEquip.this.value(e);
				}
				
				@Override
				public double vGet(Induvidual indu) {
					RoomInstance ins = STATS.WORK().EMPLOYED.get(indu);
					if (ins == null || ins.blueprint() != e.blueprint())
						return 0;
					return ins.employees().toolD(RoomEquip.this);
				}
				
				@Override
				public double to() {
					return to;
				}
				
				@Override
				public double from() {
					return 0;
				}
				
				@Override
				public boolean has(Class<? extends BOOSTABLE_O> b) {
					return b == Induvidual.class || b == NPCBonus.class || b == POP_CL.class;
				}
				
			};
			
			BoostSpec s = boosts.push(bo, e.blueprint().bonus());
			boostMap[e.eindex()] = s;
		}
	}
	
	public INTE target(RoomEmploymentSimple e) {
		return targets.get(e.eindex());
	}
	
	public int targetI(RoomEmploymentSimple e) {
		return targets.get(e.eindex()).get()*e.employed();
	}

	public int current(RoomEmploymentSimple e) {
		return currents[e.eindex()];
	}
	
	public double value(RoomEmploymentSimple e) {
		double tt = targetI(e);
		if (tt == 0)
			return 0;
		return (double)currents[e.eindex()]/tt;
	}
	
	public int currentTotal() {
		return total;
	}
	
	public int neededTotal() {
		int am = 0;
		for (int i = 0; i < rooms.size(); i++) {
			am += targetI(rooms.get(i));
		}
		return am;
	}
	
	public LIST<RoomEmploymentSimple> rooms(){
		return rooms;
	}
	
	public BoostSpec boost(RoomEmploymentSimple e) {
		return boostMap[e.eindex()];
	}
	
	public boolean has(RoomEmploymentSimple e) {
		return targets.get(e.eindex()).max() > 0;
	}
	
	void count(RoomEmploymentSimple e, int am) {
		currents[e.eindex()] += am;
		total += am;
	}

	@Override
	public int index() {
		return index;
	}
	
	
	
}
