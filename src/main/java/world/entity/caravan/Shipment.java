package world.entity.caravan;

import java.io.IOException;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.trade.FACTION_IMPORTER;
import game.time.TIME;
import init.C;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.main.SETT;
import snake2d.*;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.dic.DicGeo;
import util.dic.DicRes;
import util.rendering.ShadowBatch;
import view.main.VIEW;
import world.World;
import world.entity.*;
import world.entity.WPathing.WorldPathCost;
import world.map.regions.CapitolPlacablity;
import world.map.regions.Region;


public final class Shipment extends WEntity{

	public static final int MAX_DISTANCE = 550;
	private static double speed = World.SPEED*24.0*C.TILE_SIZE/(SETT.TWIDTH/CapitolPlacablity.TILE_DIM);
	
	private final WPath path = new WPath();
	private int destination;
	private final int[] payload = new int[RESOURCES.ALL().size()];
	
	private static final WorldPathCost cost =new WorldPathCost() {
		
		@Override
		public boolean canMove(Region a, Region b) {
			return true;
		}
	};
	
	private byte type;

	public enum Type {
		tax(DicGeo.¤¤Tribute),
		trade(DicGeo.¤¤Trade),
		spoils(DicRes.¤¤Spoils);
		
		public static final LIST<Type> all = new ArrayList<>(values());
		public final CharSequence name;
		
		private Type(CharSequence name) {
			this.name = name;
		}
	}

	
	
	Shipment(){
		super(C.TILE_SIZE, C.TILE_SIZE);

	}
	
	@Override
	protected void save(FilePutter file) {
		file.i(destination);
		file.b(type);
		file.is(payload);
		path.save(file);
	}
	
	@Override
	protected WEntity load(FileGetter file) throws IOException {
		destination = file.i();
		type = file.b();
		file.is(payload);
		path.load(file);
		return this;
	}
	
	@Override
	protected void clearP() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void renderAboveTerrain(Renderer r, ShadowBatch s, float ds, int x, int y) {
		
		int i;
		
		if (World.WATER().has.is(body().cX()>>C.T_SCROLL, body().cY()>>C.T_SCROLL)) {
			i = 8*3;
		}else {
			i = (int) GAME.intervals().get05()%3;
			i*= 8;
		}
		if (type != 0)
			i+= 8*4;
		World.ENTITIES().caravans.caravan.render(r, i+path.dir().id(), x, y);
		s.setDistance2Ground(0).setHeight(2);
		World.ENTITIES().caravans.caravan.render(s, i+path.dir().id(), x, y);
		if (TIME.light().nightIs() && (TIME.light().partOfCircular()*16 > (destination&0x07))) {
			x += C.TILE_SIZEH/2+(GAME.intervals().get05()+destination & 0b11);
			y += C.TILE_SIZEH/2+(GAME.intervals().get05()+(destination>>4) & 0b11);
			CORE.renderer().renderUniLight(x, y, 2, 128);
		}
	}
	
	static void render(SPRITE_RENDERER r, float ds, int x, int y) {
		int i = (int) (VIEW.renderSecond()*2)%3;
		i*= 8;
		i+= 8*4;
		World.ENTITIES().caravans.caravan.render(r, i+DIR.SW.id(), x, y);
	}
	
	@Override
	protected void renderBelowTerrain(Renderer r, ShadowBatch s, float ds, int x, int y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void update(float ds) {

		path.move(this, speed*ds, cost);
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
		//GAME.Notify("oh no!");
		remove();
	}
	
	private void arrive() {
		Faction f = destination().faction();
		FACTION_IMPORTER s = f.buyer();
		
		
		for (RESOURCE r : RESOURCES.ALL()) {
			int am = payload[r.bIndex()];
			if (am != 0) {
				s.reserveSpace(r, -am);
				payload[r.bIndex()] = 0;
				if (type() == Type.tax) {
					s.addTaxes(r, am);
				}else if (type() == Type.trade)
					s.addImport(r, am);
				else {
					s.addSpoils(r, am);
				}
			}
		}
		remove();
	}
	
//	public void delay(double seconds) {
//		this.delaySeconds = (float) seconds;
//	}
	

	
	void add(int tx, int ty, Faction destination, Type type) {
		
		for (int i = 0; i < payload.length; i++)
			payload[i] = 0;
		body().moveX1Y1(tx*C.TILE_SIZE, ty*C.TILE_SIZE);
		path.clear();
		this.destination = destination.index();
		
		WPathing.path(tx, ty, destination.capitolRegion().cx(), destination.capitolRegion().cy(), path, cost);

		this.type = (byte) type.ordinal();
		
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
			if (payload[r.bIndex()] > 0)
				destination().faction().buyer().reserveSpace(r, -payload[r.bIndex()]);
			payload[r.bIndex()] = 0;
		}
		
	}

	@Override
	protected Shipments constructor() {
		return World.ENTITIES().caravans;
	}
	
	public Type type() {
		return Type.all.get(type);
	}
	
	@Override
	public int getZ() {
		return -1;
	}
	
	public Shipment load(RESOURCE r, int amount) {
		destination().faction().buyer().reserveSpace(r, -payload[r.bIndex()]);
		payload[r.bIndex()] = CLAMP.i(amount+payload[r.bIndex()], 0, Integer.MAX_VALUE);
		destination().faction().buyer().reserveSpace(r, payload[r.bIndex()]);
		return this;
	}
	
	public int loadGet(RESOURCE r) {
		return payload[r.bIndex()];
	}
	
	public Region destination() {
		Faction f = FACTIONS.getByIndex(destination);
		if (!f.isActive())
			return null;
		
		return f.capitolRegion();
	}
	
	@Override
	public WPath path() {
		return path;
	}
	
	
}
