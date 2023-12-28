package world.log;

import game.faction.Faction;
import init.D;
import init.sprite.UI.UI;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.StrInserter;
import util.dic.DicArmy;
import world.WORLD;
import world.entity.army.WArmy;
import world.map.pathing.WRegSel;
import world.map.pathing.WRegs.RDist;
import world.map.pathing.WTREATY;
import world.regions.Region;

public final class WLogger {
	
	private final static Str tmp = new Str(256);
	private static CharSequence ¤¤besiege = "An army of {FACTION_A} besieges {REG_NAME}, controlled by {FACTION_B}.";
	private static CharSequence ¤¤besiegeC = "An army of {FACTION_A} besieges the capital of {FACTION_B}.";
	private static CharSequence ¤¤siegeVictory = "{FACTION_A} forces have taken control of {REG_NAME}.";
	private static CharSequence ¤¤siegeDefeat = "{FACTION_A} forces have retreated from {REG_NAME}.";
	private static CharSequence ¤¤warDeclared = "The realm of {FACTION_A} has declared war on {FACTION_B}";
	private static CharSequence ¤¤warPeace = "{FACTION_A} and {FACTION_B} have agreed to a truce.";
	private static CharSequence ¤¤battle = "An army of {FACTION_A} defeated an army of {FACTION_B} near {REG_NAME}.";
	private static CharSequence ¤¤trade = "{FACTION_A} and {FACTION_B} are now trade partners.";
	private static CharSequence ¤¤sucession = "{ROYALTY_A} ascends the throne of {FACTION_A}, succeeding the old leader, {ROYALTY_B}.";
	private static CharSequence ¤¤regChange = "{REG_NAME} changes master from {FACTION_A} to {FACTION_B}.";
	private static CharSequence ¤¤newFaction = "A new faction has emerged. They call themselves {FACTION_A}.";
	private static CharSequence ¤¤factionDestroyed = "The faction of {FACTION_A} has been completely destroyed.";
	static {
		D.ts(WLogger.class);
	}
	
	private static final StrInserter.Simple iFacA = new StrInserter.Simple("FACTION_A");
	private static final StrInserter.Simple iFacB = new StrInserter.Simple("FACTION_B");
	private static final StrInserter.Simple iReg = new StrInserter.Simple("REG_NAME");
	private static final StrInserter.Simple iRoyA = new StrInserter.Simple("ROYALTY_A");
	private static final StrInserter.Simple iRoyB = new StrInserter.Simple("ROYALTY_B");
	
	
	

	
	
	
	private WLogger() {
		
	}
	
	public static void besiege(WArmy b, Region reg) {
		tmp.clear().add(reg.capitol() ? ¤¤besiegeC : ¤¤besiege);
		iFacA.insert(faction(b.faction()), tmp);
		iFacB.insert(faction(reg.faction()), tmp);
		iReg.insert(reg.info.name(), tmp);
		WORLD.LOG().log(b.faction(), reg.faction(), UI.icons().s.degrade, tmp, reg.cx(), reg.cy());
	}
	
	public static void siege(Faction f, Region reg, boolean victory) {
		tmp.clear().add(victory ? ¤¤siegeVictory : ¤¤siegeDefeat);
		iFacA.insert(faction(f), tmp);
		iFacB.insert(faction(reg.faction()), tmp);
		iReg.insert(reg.info.name(), tmp);
		WORLD.LOG().log(f, reg.faction(), UI.icons().s.degrade, tmp, reg.cx(), reg.cy());
	}
	
	public static void war(Faction a, Faction b, boolean war) {
		tmp.clear().add(war ? ¤¤warDeclared : ¤¤warPeace);
		iFacA.insert(faction(a), tmp);
		iFacB.insert(faction(b), tmp);
		if (b.capitolRegion() != null)
			WORLD.LOG().log(a, b, UI.icons().s.sword, tmp, b.capitolRegion().cx(), b.capitolRegion().cy());
	}
	
	public static void battle(Faction victor, Faction looser, int tx, int ty) {
		tmp.clear().add(¤¤battle);
		iFacA.insert(faction(victor), tmp);
		iFacB.insert(faction(looser), tmp);
		Region reg = WORLD.REGIONS().map.get(tx, ty);
		if (reg == null) {
			RDist r = WORLD.PATH().tmpRegs.single(tx, ty, WTREATY.DUMMY(), WRegSel.DUMMY(null));
			if (r != null) {
				reg = r.reg;
			}
		}
		
		if (reg != null) {
			iReg.insert(reg.info.name(), tmp);
		}
		WORLD.LOG().log(victor, looser, UI.icons().s.sword, tmp, tx, ty);
	}
	
	public static void trade(Faction a, Faction b) {
		tmp.clear().add(¤¤trade);
		iFacA.insert(faction(a), tmp);
		iFacB.insert(faction(b), tmp);
		if (b.capitolRegion() != null)
			WORLD.LOG().log(a, b, UI.icons().s.urn, tmp, b.capitolRegion().cx(), b.capitolRegion().cy());
	}
	
	public static void newLeader(Faction a, CharSequence prev, CharSequence neww) {
		tmp.clear().add(¤¤sucession);
		iRoyA.insert(neww, tmp);
		iRoyB.insert(prev, tmp);
		iFacA.insert(a.name, tmp);
		if (a.capitolRegion() != null)
			WORLD.LOG().log(null, a, UI.icons().s.crown, tmp, a.capitolRegion().cx(), a.capitolRegion().cy());
	}
	
	public static void newOwner(Faction old, Faction newO, Region reg) {
		tmp.clear().add(¤¤regChange);
		iFacA.insert(faction(old), tmp);
		iFacB.insert(faction(newO), tmp);
		iReg.insert(reg.info.name(), tmp);
		WORLD.LOG().log(old, newO, UI.icons().s.crown, tmp, reg.cx(), reg.cy());
	}
	
	public static void newFaction(Faction f) {
		tmp.clear().add(¤¤newFaction);
		iFacA.insert(faction(f), tmp);
		WORLD.LOG().log(null, f, UI.icons().s.crown, tmp, f.capitolRegion().cx(), f.capitolRegion().cy());
	}
	
	public static void factionDestroyed(Faction f) {
		tmp.clear().add(¤¤factionDestroyed);
		iFacA.insert(faction(f), tmp);
		if (f.capitolRegion() != null)
		WORLD.LOG().log(null, f, UI.icons().s.crown, tmp, f.capitolRegion().cx(), f.capitolRegion().cy());
	}
	
	public static CharSequence faction(Faction f) {
		if (f == null)
			return DicArmy.¤¤Rebels;
		return f.name;
	}

	
}
