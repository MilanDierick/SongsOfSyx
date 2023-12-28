package game.faction.trade;

import game.faction.FResources.RTYPE;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import util.dic.DicGeo;
import util.dic.DicRes;

public final class ITYPE {
	
	private final static ArrayListGrower<ITYPE> pall = new ArrayListGrower<ITYPE>();
	
	public final static LIST<ITYPE> all = pall;
	
	public static final ITYPE tax = new ITYPE(DicGeo.¤¤Tribute, RTYPE.TAX);
	public static final ITYPE trade = new ITYPE(DicGeo.¤¤Trade, RTYPE.TRADE);
	public static final ITYPE spoils = new ITYPE(DicRes.¤¤Spoils, RTYPE.SPOILS);
	public static final ITYPE diplomacy = new ITYPE(DicRes.¤¤Spoils, RTYPE.DIPLOMACY);
	
	public final CharSequence name;
	public final int index;
	public final RTYPE rtype;
	
	private ITYPE(CharSequence name, RTYPE rtype) {
		this.name = name;
		this.index = pall.add(this);
		this.rtype = rtype;
	}

}