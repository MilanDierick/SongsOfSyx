package util.dic;

import init.D;
import snake2d.util.datatypes.DIR;

public class DicGeo {

	public static CharSequence ¤¤Destination = "¤Destination";
	public static CharSequence ¤¤Regions = "¤Regions";
	public static CharSequence ¤¤RegionDesc = "¤The world is made up of regions. Regions have people living in them, which lives you can control if you manage to capture it. A region has an urban center, that can either be a town of a faction capitol. You upgrade regions with administration points.";
	public static CharSequence ¤¤MustBeOwnRegion = "¤Must be in a controlled region";
	public static CharSequence ¤¤MustNotBeWater = "¤Must not be placed on water.";
	public static CharSequence ¤¤NoRuler = "¤Free Lands";
	public static CharSequence ¤¤BoundFor = "¤Bound For {0}";
	public static CharSequence ¤¤Regional = "Regional {0}";
	public static CharSequence ¤¤Region = "Region";
	public static CharSequence ¤¤Tribute = "¤Tribute";
	public static CharSequence ¤¤Trade = "¤Trade";
	public static CharSequence ¤¤Capitol = "¤Capitol";
	public static CharSequence ¤¤World = "¤World";
	public static CharSequence ¤¤Caravan = "¤Caravan";
	public static CharSequence ¤¤Moving = "¤Moving";
	public static CharSequence ¤¤Location = "¤Location";
	public static CharSequence ¤¤Global = "¤Global";
	public static CharSequence ¤¤AtWar = "¤At War";
	public static CharSequence ¤¤AtWarD = "¤Warring factions do not trade, and will try to destroy one another.";
	public static CharSequence ¤¤Vassal = "¤Vassal";
	public static CharSequence ¤¤VassalD = "¤A vassal has an protector that it pays taxes to. They trade like normal.";
	public static CharSequence ¤¤Protector = "¤Protector";
	public static CharSequence ¤¤ProtectorD = "¤A protector is a vassals overlord and will receive taxes from the vassal.";
	public static CharSequence ¤¤TradePartner = "¤Trade Partner";
	public static CharSequence ¤¤TradePartnerD = "¤Trade partners are able to trade goods with one another.";

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
	
	static {
		D.ts(DicGeo.class);
	}
}
