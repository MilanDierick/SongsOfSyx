package settlement.stats.equip;

import java.io.IOException;

import game.boosting.BOOSTABLE_O;
import game.boosting.BoostSpecs;
import game.faction.npc.NPCBonus;
import init.D;
import init.config.Config;
import init.paths.PATH;
import init.race.POP_CL;
import init.race.Race;
import settlement.army.ArmyManager;
import settlement.army.Div;
import settlement.entity.humanoid.HCLASS;
import settlement.stats.*;
import settlement.stats.util.StatBooster;
import snake2d.util.file.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.LISTE;

public class EquipBattle extends Equip{
	
	private final int[] tars = new int[ArmyManager.ARMIES*Config.BATTLE.DIVISIONS_PER_ARMY];
	private final int iMil;
	public final int amountInGarrison;
	public final int amountGuard;
	public final double[] slotUse;
	public static CharSequence ¤¤combineProblem = "Can not be combined with current equipment.";
	
	public static boolean fixifixshit = false;
	
	static {
		D.ts(EquipBattle.class);
	}
	
	EquipBattle(String coll, String key, PATH path, LISTE<Equip> all, LISTE<EquipBattle> mil, StatsInit init) {
		super(coll, key, path, all, init);
		iMil = mil.add(this);
		
		SAVABLE s = new SAVABLE() {

			@Override
			public void save(FilePutter file) {
				file.isE(tars);
			}

			@Override
			public void load(FileGetter file) throws IOException {
				file.isE(tars);
				if (fixifixshit)
					file.isE(tars);
			}

			@Override
			public void clear() {
				for (int i = 0; i < tars.length; i++)
					tars[i] = targetDefault;
			}
		};
			
		
		s.clear();
		Json j = new Json(path.get(key));
		amountInGarrison = j.i("AMOUNT_IN_GARRISON", 0, equipMax);
		amountGuard = j.i("EQUIP_GUARDS", 0, equipMax);
		init.savables.add(s);
		slotUse = j.ds("SLOT_USAGE");
		stat.info().setMatters(false, true);
	}
	
	@Override
	protected void push(BoostSpecs boosters, Json data) {
		boosters.push(data, new StatBooster() {

			@Override
			public double vGet(Induvidual indu) {
				return stat.indu().getD(indu);
			}

			@Override
			public double vGet(Div div) {
				return stat.div().getD(div);
			}

			@Override
			public double vGet(NPCBonus bonus) {
				return 0;
			}
			
			@Override
			public boolean has(Class<? extends BOOSTABLE_O> b) {
				return StatBooster.super.has(b) && b != NPCBonus.class && b != POP_CL.class;
			}

			@Override
			public double vGet(POP_CL reg, int daysBack) {
				return 0;
			}
			
		});
	}

	@Override
	public int target(Induvidual h) {
		Div i = STATS.BATTLE().DIV.get(h);
		if (i == null || !i.settings.mustering())
			return 0;
		return target(i);
	}
	
	@Override
	public double value(HCLASS c, Race type) {
		return stat.data(c).getD(type);
	}

	@Override
	protected double value(Induvidual v) {
		return stat.indu().getD(v);
	}

	@Override
	protected double value(Div v) {
		return stat.div().getD(v);
	}

	@Override
	public int max(Induvidual i) {
		return equipMax;
	}


	public int target(Div d) {
		return CLAMP.i(tars[d.index()], 0, equipMax);
	}
	
	public void targetSet(Div d, int t) {
		tars[d.index()] = CLAMP.i(t, 0, equipMax);
	}
	
	public int max() {
		return equipMax;
	}

	public int indexMilitary() {
		return iMil;
	}


	public int garrisonAmount() {
		return amountInGarrison;
	}

	public int guardAmount() {
		return amountGuard;
	}
	
	public boolean canCombineWith(EquipBattle other) {
		for (int i = 0; i < slotUse.length; i++) {
			if (slotUse[i] + other.slotUse[i] > 1)
				return false;
		}

		return true;
	}
	
}