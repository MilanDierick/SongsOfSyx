package world.entity.caravan;

import java.io.IOException;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.trade.FACTION_IMPORTER;
import game.faction.trade.ITYPE;
import game.time.TIME;
import init.C;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import snake2d.*;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import util.rendering.ShadowBatch;
import view.main.VIEW;
import world.WORLD;
import world.entity.WEntity;
import world.map.pathing.WPath;
import world.map.pathing.WTREATY;
import world.regions.Region;


public final class Shipment extends WEntity{

	public static final int MAX_DISTANCE = 550;
	private static double speed = C.TILE_SIZE*0.1;
	
	private final WPath path = new P();
	private short destReg;
	private short destFaction;
	private final int[] payload = new int[RESOURCES.ALL().size()];
	
	private byte type;
	private byte size = -1;	
	
	Shipment(){
		super(C.TILE_SIZE, C.TILE_SIZE);

	}
	
	@Override
	protected void save(FilePutter file) {
		file.s(destReg);
		file.s(destFaction);
		file.b(type);
		file.is(payload);
		path.save(file);
	}
	
	@Override
	protected WEntity load(FileGetter file) throws IOException {
		destReg = file.s();
		destFaction = file.s();
		type = file.b();
		file.is(payload);
		path.load(file);
		size = -1;
		return this;
	}
	
	@Override
	protected void clearP() {
		// TODO Auto-generated method stub
		
	}

	
	@Override
	protected void renderAboveTerrain(Renderer r, ShadowBatch s, float ds, int x, int y) {
		
		if (WORLD.FOW().is(ctx(), cty()))
			return;
		
		int i;
		
		if (WORLD.WATER().isBig.is(body().cX()>>C.T_SCROLL, body().cY()>>C.T_SCROLL)) {
			i = 8*3;
		}else {
			i = (int) GAME.intervals().get05()%3;
			i*= 8;
		}
//		if (type != 0)
//			i+= 8*4;
		
		if (size == -1) {
			int size = 0;
			for (int py : payload)
				size += py;
			size /= 256;
			this.size = (byte) CLAMP.i(size, 0, 2);
		}
		
		i += size*8*4;
		
		s.setDistance2Ground(0).setHeight(2);
		WORLD.ENTITIES().caravans.caravan.render(r, i+path.dir().id(), x, y);
		WORLD.ENTITIES().caravans.caravan.render(s, i+path.dir().id(), x, y);
		
		int am = 2;
		if (am > 1) {
			
		}
		
		if (TIME.light().nightIs() && (TIME.light().partOfCircular()*16 > (destReg&0x07))) {
			DIR d = path.dir();
			x += 8*d.x();
			y += 8*d.y();
			x += C.TILE_SIZEH/2+(4-(GAME.intervals().get05()%8));
			y += C.TILE_SIZEH/2+(4-(GAME.intervals().get04()%8));
			CORE.renderer().renderUniLight(x, y, 2, 128);
		}
	}
	
	static void render(SPRITE_RENDERER r, float ds, int x, int y) {
		int i = (int) (VIEW.renderSecond()*2)%3;
		i*= 8;
		i+= 8*4;
		WORLD.ENTITIES().caravans.caravan.render(r, i+DIR.SW.id(), x, y);
	}
	
	@Override
	protected void renderBelowTerrain(Renderer r, ShadowBatch s, float ds, int x, int y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void update(float ds) {

		path.move(this, speed*ds);
		if (!path.isValid()) {
			cancel();
			return;
		}
			
		
		if (path.arrived()) {
			Region c = destination();
			if (c != null && Math.abs(path.x() - c.cx()) * Math.abs(path.y() - c.cy()) <= 1) {
				arrive();
				return;
			}else {
				cancel();
				return;
			}
		}
		
	}

	
	private void cancel() {
		remove();
	}
	
	private void arrive() {
		Faction f = faction();
		if (f == null || f.capitolRegion() != destination()) {
			cancel();
			return;
		}
		FACTION_IMPORTER s = f.buyer();
		
		
		for (RESOURCE r : RESOURCES.ALL()) {
			int am = payload[r.bIndex()];
			if (am != 0) {
				ITYPE t = ITYPE.all.get(type);
				s.deliverAndUnreserve(r, am, t);
				payload[r.bIndex()] = 0;
			}
		}
		remove();
	}
	
//	public void delay(double seconds) {
//		this.delaySeconds = (float) seconds;
//	}
	

	
	void add(int tx, int ty, Faction destination, ITYPE type) {
		
		for (int i = 0; i < payload.length; i++)
			payload[i] = 0;
		body().moveX1Y1(tx*C.TILE_SIZE, ty*C.TILE_SIZE);
		path.clear();
		this.destFaction = (short) destination.index();
		this.destReg = (short) destination.capitolRegion().index();
		
		path.find(tx, ty, destination.capitolRegion().cx(), destination.capitolRegion().cy());

		this.type = (byte) type.index;
		
		add();
	}

	@Override
	protected void addAction() {
		
	}
	
	@Override
	protected void removeAction() {
		if (!constructor().free.isFull())
			constructor().free.push(this);
		if (destination() == null) {
			return;
		}
		for (RESOURCE r : RESOURCES.ALL()) {
			if (payload[r.bIndex()] > 0) {
				faction().buyer().reserve(r, -payload[r.bIndex()], type());
			}
			payload[r.bIndex()] = 0;
		}
		
	}

	@Override
	protected Shipments constructor() {
		return WORLD.ENTITIES().caravans;
	}
	
	public ITYPE type() {
		return ITYPE.all.get(type);
	}
	
	@Override
	public int getZ() {
		return -1;
	}
	
	public Shipment load(RESOURCE r, int amount) {
		payload[r.bIndex()] = CLAMP.i(amount+payload[r.bIndex()], 0, Integer.MAX_VALUE);
		size = -1;
		return this;
	}
	
	public Shipment loadAndReserve(RESOURCE r, int amount) {
		if (amount == 0)
			return this;
		faction().buyer().reserve(r, amount, type());
		payload[r.bIndex()] = CLAMP.i(amount+payload[r.bIndex()], 0, Integer.MAX_VALUE);
		size = -1;
		return this;
	}
	
	public int loadGet(RESOURCE r) {
		return payload[r.bIndex()];
	}
	
	public Region destination() {
		return WORLD.REGIONS().getByIndex(destReg);
	}
	
	@Override
	public WPath path() {
		return path;
	}
	
	private static class P extends WPath {

		@Override
		public WTREATY treaty() {
			return WTREATY.DUMMY();
		}
		
	};
	
	@Override
	public Faction faction() {
		return FACTIONS.getByIndex(destFaction);
	}
	
}
