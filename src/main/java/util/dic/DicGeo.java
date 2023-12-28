package util.dic;

import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import init.D;
import snake2d.util.datatypes.DIR;
import util.gui.misc.GText;
import world.regions.data.RD;

public class DicGeo {

	public static CharSequence ¤¤Destination = "¤Destination";
	public static CharSequence ¤¤Regions = "¤Regions";
	public static CharSequence ¤¤RegionDesc = "¤The world is made up of regions. Regions have people living in them, which lives you can control if you manage to capture it. A region has an urban center, that can either be a town of a faction capital. You upgrade regions with administration points.";
	public static CharSequence ¤¤MustBeOwnRegion = "¤Must be in a controlled region";
	public static CharSequence ¤¤NoRuler = "¤Free Lands";
	public static CharSequence ¤¤BoundFor = "¤Bound For {0}";
	public static CharSequence ¤¤Regional = "Regional {0}";
	public static CharSequence ¤¤Region = "Region";
	public static CharSequence ¤¤Realm = "Realm";
	public static CharSequence ¤¤Tribute = "¤Tribute";
	public static CharSequence ¤¤Trade = "¤Trade";
	public static CharSequence ¤¤Traded = "¤Traded";
	public static CharSequence ¤¤Capitol = "¤Capital";
	public static CharSequence ¤¤CanTrade = "¤Can Trade";
	public static CharSequence ¤¤TradeD = "¤Trade you to purchase and sell goods with this faction. Factions can be traded with if you border them.";
	public static CharSequence ¤¤World = "¤World";
	public static CharSequence ¤¤Caravan = "¤Caravan";
	public static CharSequence ¤¤Moving = "¤Moving";
	public static CharSequence ¤¤Location = "¤Location";
	public static CharSequence ¤¤Global = "¤Global";
	public static CharSequence ¤¤AtWar = "¤At War";
	public static CharSequence ¤¤AtWarD = "¤Warring factions do not trade, and will try to destroy one another.";
	public static CharSequence ¤¤TradePartner = "¤Trade Partner";
	public static CharSequence ¤¤TradePartners = "¤Trade Partners";
	public static CharSequence ¤¤TradePartnerD = "¤Trade partners are able to trade goods with one another.";
	public static CharSequence ¤¤Neutral = "¤Neutral Faction";
	public static CharSequence ¤¤Distant = "¤Distant Faction";
	public static CharSequence ¤¤RegionOf = "¤Region of";
	public static CharSequence ¤¤CapitolOf = "¤Capital of";
	public static CharSequence ¤¤CapitolYou = "¤Your Capital";
	public static CharSequence ¤¤peace = "Peace Treaty";
	public static CharSequence ¤¤peaceD = "End hostilities.";
	public static CharSequence ¤¤Opinion = "Opinion";
	public static CharSequence ¤¤OpinionD = "The opinion of the current regent of a faction regarding you. High opinions help diplomacy and decreases trade prices. Low opinion might lead to war.";
	
	private static CharSequence ¤¤North = "North";
	private static CharSequence ¤¤NorthEast = "North-East";
	private static CharSequence ¤¤East = "East";
	private static CharSequence ¤¤SouthEast = "South-East";
	private static CharSequence ¤¤South = "South";
	private static CharSequence ¤¤SouthWest = "South-West";
	private static CharSequence ¤¤West = "West";
	private static CharSequence ¤¤NorthWest = "North-West";
	public static CharSequence ¤¤Central = "Central";
	
	public static CharSequence get(DIR d) {
		if (d == DIR.N)
			return ¤¤North;
		if (d == DIR.NE)
			return ¤¤NorthEast;
		if (d == DIR.E)
			return ¤¤East;
		if (d == DIR.SE)
			return ¤¤SouthEast;
		if (d == DIR.S)
			return ¤¤South;
		if (d == DIR.SW)
			return ¤¤SouthWest;
		if (d == DIR.W)
			return ¤¤West;
		if (d == DIR.NW)
			return ¤¤NorthWest;
		return ¤¤Central;
	}
	
	public static void fType(FactionNPC f, GText text) {
		if (FACTIONS.DIP().war.is(FACTIONS.player(), f))
			text.errorify().add(DicGeo.¤¤AtWar);
		else if (FACTIONS.DIP().trades(FACTIONS.player(), f))
			text.normalify2().add(DicGeo.¤¤TradePartner);
		else if (!RD.DIST().factionBordersPlayer(f))
			text.normalify().add(DicGeo.¤¤Distant);
		else
			text.warnify().add(DicGeo.¤¤Neutral);
	}
	
	static {
		D.ts(DicGeo.class);
	}
}
