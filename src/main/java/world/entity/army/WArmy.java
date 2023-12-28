package world.entity.army;

import java.io.IOException;

import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.trade.ITYPE;
import init.C;
import snake2d.PathTile;
import snake2d.Renderer;
import snake2d.util.datatypes.*;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.text.Str;
import util.rendering.ShadowBatch;
import world.WORLD;
import world.army.*;
import world.entity.WEntity;
import world.entity.WEntityConstructor;
import world.entity.caravan.Shipment;
import world.map.pathing.WPath;
import world.map.pathing.WTREATY;
import world.regions.Region;
import world.regions.centre.WCentre;

public final class WArmy extends WEntity{

	short index = -1;
	public final Str name = new Str(24);
	
	private final WArmyDivs army = new WArmyDivs(this);
	private final WTREATY treaty = new WTREATY() {
		
		@Override
		public boolean can(int fx, int fy, int tx, int ty, double dist) {

			Region reg = WORLD.REGIONS().map.centre.get(fx, fy);
			if (reg == null)
				return true;
			
			if (FACTIONS.DIP().war.is(reg.faction(), WArmy.this.faction())) {
				return reg != WORLD.REGIONS().map.centre.get(tx, ty);
				//return movesAwayFrom(fx, fy, reg.cx(), reg.cy(), tx, ty);
			}
			
			return true;
		}
	};
	private final WPath path = new P(treaty);
	static double speed = C.TILE_SIZE*0.1;
	public static final int size = C.TILE_SIZE*2;
	private byte state = 0;
	short stateShort;
	float stateFloat;
	
	public static int reinforceTiles = 4;
	private float upD = 0;
	public boolean hasBeenAskedforRegionAssistance = false;
	
	void init(int tx, int ty) {
		body().moveCX(tx*C.TILE_SIZE + C.TILE_SIZEH);
		body().moveCY(ty*C.TILE_SIZE + C.TILE_SIZEH);
		state = 0;
		army.clear();
		path.clear();
		add();
		if (!added())
			throw new RuntimeException();
	}
	
	public WArmy() {
		super(size, size);
	}
	
	@Override
	protected void save(FilePutter file) {
		
		file.s(index);
		name.save(file);
		army.save(file);
		path.save(file);
		file.f(stateFloat);
		file.s(stateShort);
		file.b(state);
		file.f(upD);
		file.bool(hasBeenAskedforRegionAssistance);
	}

	@Override
	protected WEntity load(FileGetter file) throws IOException {
		index = file.s();
		name.load(file);
		army.load(file);
		path.load(file);
		WORLD.ENTITIES().armies.add(this, index);
		stateFloat = file.f();
		stateShort = file.s();
		state = file.b();
		upD = file.f();
		hasBeenAskedforRegionAssistance = file.bool();
		return this;
	}

	@Override
	protected void clearP() {
		index = -1;
		army.clear();
		path.clear();
		state = 0;
		upD = 1;
		hasBeenAskedforRegionAssistance = false;
	}

	@Override
	protected void renderBelowTerrain(Renderer r, ShadowBatch s, float ds, int x, int y) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void renderAboveTerrain(Renderer r, ShadowBatch s, float ds, int x, int y) {
		if (faction() != FACTIONS.player()) {
			if (WORLD.FOW().is(ctx(), cty()))
				return;
		}
		WORLD.ENTITIES().armies.sprite.render(this, r, s, x, y, path.dir());		
	}
	
	@Override
	protected void handleFow() {
		if (faction()== FACTIONS.player()) {
			WORLD.FOW().enlighten(ctx(), cty(), 5);
		}
	}
	
	@Override
	public Faction faction() {
		return AD.faction().get(this);
	}
	
	public FactionArmies armies() {
		Faction f = faction();
		if (f != null)
			return f.armies();
		return null;
	}

	@Override
	protected void update(float ds) {

		if (upD <= 0) {
			upD = 16;
			if (AD.men(null).get(this) == 0) {
				if (!acceptsSupplies()) {
					remove();
					return;
				}
			}
			checkForResources();
			
			WORLD.BATTLES().reportActivity(this);
			
		}
		
		upD -= ds;
		
		int ox = ctx();
		int oy = cty();
		Region reg = region();
		state = (byte) state().update(this, ds).index();
		
		if (ox != ctx() || oy != cty()) {
			upD = -1;
			if (reg != region())
				hasBeenAskedforRegionAssistance = false;
		}
		
	}
	
	void checkForResources() {
		if (faction() == FACTIONS.player()) {
			for (ADSupply s : AD.supplies().all) {
				if (s.current().get(this) > s.max(this)) {
					returnResources();
					return;
				}
			}
		}
	}
	
	private void returnResources() {
		Shipment ship = WORLD.ENTITIES().caravans.create(ctx(), cty(), FACTIONS.player().capitolRegion(), ITYPE.spoils);
		if (ship != null) {
			for (ADSupply ss : AD.supplies().all) {
				int am = (int) (ss.current().get(this)-ss.max(this));
				am = CLAMP.i(am, 0, Short.MAX_VALUE);
				if (am > 0) {
					ship.loadAndReserve(ss.res, am);
					ss.current().inc(this, -am);
				}
			}
		}
	}

	@Override
	protected WEntityConstructor<? extends WEntity> constructor() {
		return WORLD.ENTITIES().armies;
	}
	
	public short armyIndex() {
		return index;
	}

	
	public void setDestination(int tx, int ty) {
		if (AD.men(null).get(this) == 0)
			return;
		stop();
		if (path.find(ctx(), cty(), tx, ty))
			state = (byte) WArmyState.moving.index();
	}
	
	public void besiege(Region reg) {
		if (AD.men(null).get(this) == 0)
			return;
		if (besieging(reg)) {
			WORLD.BATTLES().besige(this, reg);
		}else {
			stop();
			COORDINATE c = besigeTile(reg);
			if (c != null) {
				if (path.find(ctx(), cty(),c.x(), c.y())) {
					state = (byte) WArmyState.besieging.index();
					stateFloat = 0;
					stateShort = (short) reg.index();
					return;
				}
			}
		}
		
	}
	
	public COORDINATE besigeTile(Region reg) {
		
		
		if (WORLD.REGIONS().map.centre.get(ctx(), cty()) == reg) {
			Rec.TEMP.setDim(WCentre.TILE_DIM+2, WCentre.TILE_DIM+2);
			Rec.TEMP.moveC(reg.cx(), reg.cy());
			
			for (COORDINATE c : Rec.TEMP) {
				if (reg.isBesigeTile(c.x(), c.y())) {
					return c;
				}
			}
		}
		
		if (reg.isBesigeTile(ctx(), cty())) {
			Coo.TMP.set(ctx(), cty());
			return Coo.TMP; 
		}
		
		PathTile t = WORLD.PATH().path(ctx(), cty(), reg.cx(), reg.cy(), WTREATY.DUMMY());
		while(t != null) {
			if (reg.isBesigeTile(t.x(), t.y())) {
				return t;
			}
			t = t.getParent();
		}
		return t;
	}
	
	
	public void teleport(int tx, int ty) {
		body().moveCX(tx*C.TILE_SIZE + C.TILE_SIZEH);
		body().moveCY(ty*C.TILE_SIZE + C.TILE_SIZEH);
		stop();
	}
	
	public void intercept(WArmy other) {
		if (AD.men(null).get(this) == 0)
			return;
		stop();
		if (path.find(ctx(), cty(), other.ctx(), other.cty())) {
			stateShort = other.armyIndex();
			state = (byte) WArmyState.intercepting.index();
		}
	}
	
	public WArmy intercepting() {
		if (state() == WArmyState.intercepting) {
			if (stateShort != -1) {
				WArmy aa = WORLD.ENTITIES().armies.get(stateShort);
				if (aa == null || !aa.added()) {
					return null;
				}
				return aa;
			}

		}
		return null;
	}
	
	public void stop() {

		path.clear();
		if (state() != WArmyState.fortifying && state() != WArmyState.fortified) {
			stateFloat = 0;
			state = (byte) WArmyState.fortifying.index();
			upD = -1;
		}
		
	}

	public WArmyDivs divs() {
		return army;
	}
	
	public void disband() {
		if(added())
			super.remove();
	}
	
	@Override
	protected void removeAction() {
		for (int i = 0; i < divs().size(); i++) {
			divs().get(i).disband();
			i--;
		}
		checkForResources();
		AD.removeOnlyTobeCalledFromAnArmy(this);
		WORLD.ENTITIES().armies.ret(this);
	}
	
	@Override
	public WPath path() {
		return path;
	}

	public WArmyState state() {
		return WArmyState.all().get(state);
	}
	
	public Region region() {
		return WORLD.REGIONS().map.get(ctx(), cty());
	}
	
	public boolean acceptsSupplies() {

		return state() == WArmyState.fortified && WORLD.REGIONS().map.get(ctx(), cty()) != null && WORLD.REGIONS().map.get(ctx(), cty()).faction() == faction();
	}
	
	public double besigeTimer() {
		if (state() == WArmyState.besieging)
			return stateFloat;
		return 0;
	}
	
	public boolean besieging(Region reg) {
		return reg != null && reg == besieging();
	}
	
	public Region besieging() {
		if (state() != WArmyState.besieging)
			return null;
		Region reg = WORLD.REGIONS().getByIndex(stateShort);
		if (reg == null)
			return null;
		
		if (!FACTIONS.DIP().war.is(faction(), reg.faction()))
			return null;
		if (AD.men(null).get(this) <= 0)
			return null;
		
		return reg.isBesigeTile(ctx(), cty()) ? reg : null;
	}
	
	public WArmy getHostileArmy() {
		
		return getHostileArmy(ctx(), cty());
	}
	
	public WArmy getHostileArmy(int tx, int ty) {
		
		if (WORLD.PATH().route.is(tx, ty)) {
			int dm = WORLD.PATH().dirMap().get(tx, ty);
			
			for (int di = 0; di < DIR.ALLC.size(); di++) {
				DIR d = DIR.ALLC.get(di);
				if (d == DIR.C || (dm & d.bit) != 0) {
					WArmy ao = getHostileArmyTile(tx+d.x(), ty+d.y());
					if (ao != null)
						return ao;
				}
			}
		}
		
		
		return null;
	}
	
	public WArmy getHostileArmyTile(int dx, int dy) {
		
		if (!WORLD.IN_BOUNDS(dx, dy))
			return null;
		
		for (WArmy ao : WORLD.ENTITIES().armies.fillTile(dx, dy)) {
			if (ao.ctx() == dx && ao.cty() == dy) {
				if (AD.men(null).get(ao) > 0 && FACTIONS.DIP().war.is(faction(), ao.faction())) {
					return ao;
				}
			}
			
		}
		
		return null;
	}

	
	@Override
	public String toString() {
		return "[" + index + "]" + name + " (" + ctx() + "," + cty() + ")";
	}

	
	private static final class P extends WPath {
		
		private final WTREATY t;
		
		P(WTREATY t){
			this.t = t;
		}

		@Override
		public WTREATY treaty() {
			return t;
		}
		
		
	};
	
}
