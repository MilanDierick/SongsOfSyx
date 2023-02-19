package world.entity.army;

import java.io.IOException;

import game.GAME;
import game.battle.BATTLE;
import game.faction.FACTIONS;
import game.faction.Faction;
import init.C;
import settlement.main.SETT;
import snake2d.Renderer;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.text.Str;
import util.rendering.ShadowBatch;
import world.World;
import world.army.*;
import world.army.WARMYD.WArmySupply;
import world.entity.*;
import world.entity.WPathing.WorldPathCost;
import world.entity.caravan.Shipment;
import world.map.regions.CapitolPlacablity;
import world.map.regions.Region;

public final class WArmy extends WEntity{

	short index = -1;
	public final Str name = new Str(24);
	
	private final WArmyDivs army = new WArmyDivs(this);
	private final WPath path = new WPath();
	static double speed = World.SPEED*32.0*C.TILE_SIZE/(SETT.TWIDTH/CapitolPlacablity.TILE_DIM);

	private byte state = 0;
	short stateShort;
	float stateFloat;
	
	public static int reinforceTiles = 4;
	private float upD = 1;
	
	public WorldPathCost cost() {
		return WArmyCost.get(this);
	}
	
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
		super(C.TILE_SIZE*2, C.TILE_SIZE*2);
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
	}

	@Override
	protected WEntity load(FileGetter file) throws IOException {
		index = file.s();
		name.load(file);
		army.load(file);
		path.load(file);
		World.ENTITIES().armies.add(this, index);
		stateFloat = file.f();
		stateShort = file.s();
		state = file.b();
		upD = file.f();
		return this;
	}

	@Override
	protected void clearP() {
		index = -1;
		army.clear();
		path.clear();
		state = 0;
		upD = 1;
	}

	@Override
	protected void renderBelowTerrain(Renderer r, ShadowBatch s, float ds, int x, int y) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void renderAboveTerrain(Renderer r, ShadowBatch s, float ds, int x, int y) {
		World.ENTITIES().armies.sprite.render(this, r, s, x, y, path.dir());		
	}
	
	@Override
	public Faction faction() {
		return WARMYD.faction().get(this);
	}
	
	public FactionArmies armies() {
		Faction f = faction();
		if (f != null)
			return f.kingdom().armies();
		return null;
	}

	@Override
	protected void update(float ds) {

		state = (byte) state().update(this, ds).index();
		
		upD += ds;
		if (upD > 2) {
			upD -= 2;
			updateLong();
		}
		
	}
	
	void updateLong() {
		int ctx = ctx();
		int cty = cty();
		
		Region r = World.REGIONS().getter.get(ctx, cty);
		if (WARMYD.men(null).get(this) == 0) {
			if (r == null || faction() == null || r.faction() == null || !FACTIONS.rel().ally(r.faction(), faction())) {
				remove();
				return;
			}
		}
		checkForResources();
		
		GAME.battle().reportArmyMovement(this);
		state = (byte) state().updateLong(this).index();
	}
	
	void checkForResources() {
		if (faction() == FACTIONS.player()) {
			for (WArmySupply s : WARMYD.supplies().all) {
				if (s.current().get(this) > s.max(this)) {
					returnResources();
					return;
				}
			}
		}
	}
	
	private void returnResources() {
		Shipment ship = World.ENTITIES().caravans.createSpoils(ctx(), cty(), FACTIONS.player().capitolRegion());
		if (ship != null) {
			for (WArmySupply ss : WARMYD.supplies().all) {
				int am = (int) (ss.current().get(this)-ss.max(this));
				am = CLAMP.i(am, 0, Short.MAX_VALUE);
				if (am > 0) {
					ship.load(ss.res, am);
					ss.current().inc(this, -am);
				}
			}
		}
	}

	@Override
	protected WEntityConstructor<? extends WEntity> constructor() {
		return World.ENTITIES().armies;
	}
	
	public short armyIndex() {
		return index;
	}

	
	public void setDestination(int tx, int ty) {
		if (WARMYD.men(null).get(this) == 0)
			return;
		stop();
		if (WPathing.path(ctx(), cty(), tx, ty, path, cost())) {
			state = (byte) WArmyState.moving.index();
		}
	}
	
	public void besiege(Region reg) {
		if (WARMYD.men(null).get(this) == 0)
			return;
		if (state() == WArmyState.besieging && reg.index() == stateShort) {
			if (!path.isValid()) {
				GAME.battle().besiegeFirst(this, reg, stateFloat);
			}
			return;
		}
		stop();
		if (WPathing.path(ctx(), cty(), reg.cx(), reg.cy(), path, cost())) {
			state = (byte) WArmyState.besieging.index();
			stateFloat = 0;
			stateShort = (short) reg.index();
		}
	}
	
	public void teleport(int tx, int ty) {
		body().moveCX(tx*C.TILE_SIZE + C.TILE_SIZEH);
		body().moveCY(ty*C.TILE_SIZE + C.TILE_SIZEH);
		stop();
	}
	
	public void intercept(WArmy other) {
		if (WARMYD.men(null).get(this) == 0)
			return;
		stop();
		if (WPathing.intercept(ctx(), cty(), other, path, cost())) {
			stateShort = other.armyIndex();
			state = (byte) WArmyState.intercepting.index();
		}
	}
	
	public void stop() {
		path.clear();
		if (state() != WArmyState.fortifying && state() != WArmyState.fortified) {
			stateFloat = 0;
			state = (byte) WArmyState.fortifying.index();
		}
		updateLong();
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
		WARMYD.removeOnlyTobeCalledFromAnArmy(this);
		World.ENTITIES().armies.ret(this);
	}
	
	@Override
	public WPath path() {
		return path;
	}

	public WArmyState state() {
		return WArmyState.all().get(state);
	}
	
	public Region region() {
		return World.REGIONS().getter.get(ctx(), cty());
	}
	
	public boolean acceptsSupplies() {
		return state() == WArmyState.fortified && World.REGIONS().getter.get(ctx(), cty()) != null && World.REGIONS().getter.get(ctx(), cty()).faction() != null && FACTIONS.rel().allies.get(World.REGIONS().getter.get(ctx(), cty()).faction(), faction()) == 1;
	}

	public boolean willConquer(Region f) {
		return BATTLE.regionCanHoldOut(f, this, stateFloat);
	}

}
