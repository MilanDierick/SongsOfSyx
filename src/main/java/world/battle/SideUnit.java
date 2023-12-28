package world.battle;

import game.faction.Faction;
import snake2d.util.datatypes.Coo;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.text.Str;
import world.WORLD;
import world.army.AD;
import world.army.WDIV;
import world.entity.army.WArmy;
import world.regions.Region;
import world.regions.data.RD;

final class SideUnit {

	private int type;
	private final static int T_ARMY = 0;
	private final static int T_GARRISON = 1;
	
	private int regionI;
	private int armyI;
	private int maxDivs;
	
	private Str name = new Str(24);
	public int men;
	public int losses;
	public final Coo coo = new Coo();
	public boolean mustFight;
	public double baseMul = 0;

	
	public SideUnit() {
		
	}
	
	void copy(SideUnit o) {
		this.type = o.type;
		this.regionI = o.regionI;
		this.armyI = o.armyI;
		this.maxDivs = o.maxDivs;
		this.baseMul = o.baseMul;
		this.mustFight = o.mustFight;
		this.men = o.men;
		this.losses = o.losses;
		this.coo.set(o.coo);
		this.name.clear().add(o.name);
	}
	
	public SideUnit set(Region reg, int maxDivs, boolean mustFight) {
		type = T_GARRISON;
		regionI = reg.index();
		this.maxDivs = maxDivs;
		this.mustFight = mustFight;
		this.baseMul = 1.0;
		coo.set(reg.cx(), reg.cy());
		name.clear().add(reg.info.name());
		men = RD.MILITARY().garrison.get(reg);
		return this;
	}
	
	public SideUnit set(WArmy a, int maxDivs, boolean mustFight) {
		type = T_ARMY;
		armyI = a.armyIndex();
		this.maxDivs = maxDivs;
		this.mustFight = mustFight;
		this.baseMul = 0.25 + 0.75*AD.supplies().morale(a);
		coo.set(a.ctx(), a.cty());
		name.clear().add(a.name);
		men = AD.men(null).get(a);
		return this;
	}
	
	public int divs() {
		switch(type) {
		case T_ARMY : return CLAMP.i(a().divs().size(), 0, maxDivs);
		case T_GARRISON: return CLAMP.i(RD.MILITARY().divisions(r()).size(), 0, maxDivs);
		default: throw new RuntimeException();
		}
	}
	
	public WDIV div(int index) {
		switch(type) {
		case T_ARMY : return a().divs().get(index);
		case T_GARRISON: return RD.MILITARY().divisions(r()).get(index);
		default: throw new RuntimeException();
		}
	}
	
	public Faction faction() {
		switch(type) {
		case T_ARMY : return a().faction();
		case T_GARRISON: return r().faction();
		default: throw new RuntimeException();
		}
	}
	
	public WArmy a() {
		if (type == T_ARMY)
			return WORLD.ENTITIES().armies.get(armyI);
		return null;
	}
	
	public Region r() {
		if (type == T_GARRISON)
			return WORLD.REGIONS().getByIndex(regionI);
		return null;
	}
	
	public boolean isSameAs(SideUnit o) {
		if (type == o.type)
			return (type == T_GARRISON && regionI == o.regionI) || (type == T_ARMY && armyI == o.armyI);
		return false;
	}

	
}