package world.entity;

import java.io.IOException;

import init.C;
import snake2d.Renderer;
import snake2d.util.datatypes.*;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.*;
import util.rendering.ShadowBatch;
import world.World;
import world.entity.army.WArmyConstructor;
import world.entity.caravan.Shipments;

public class WEntities extends World.WorldResource {

	private final ArrayListResize<WEntity> ents;
	private final _WEntityMap map;
	private final ArrayList<WEntity> tmp = new ArrayList<WEntity>(2056);
	private final ArrayList<WEntityConstructor<?>> constructors = new ArrayList<>(20);
	private final Rec rectmp = new Rec();

	public final Shipments caravans = new Shipments(constructors);
	public final WArmyConstructor armies = new WArmyConstructor(constructors);
	
	private final Tree<WEntity> renderables = new Tree<WEntity>(2056) {

		@Override
		protected boolean isGreaterThan(WEntity current, WEntity cmp) {
			return current.getZ() < cmp.getZ();
		}
		
	};

	public WEntities(World world) throws IOException{

		ents = new ArrayListResize<WEntity>(1024, 64000);
		map = new _WEntityMap(World.PWIDTH(), World.PHEIGHT());

	}

	@Override
	protected void load(FileGetter file) throws IOException {
		ents.clear();
		map.clear();
		

		int am = file.i();

		for (int i = 0; i < am; i++) {
			WEntityConstructor<?> c = constructors.get(file.i());
			WEntity e = c.create();
			e = e.load(file);
			e.hitBox.load(file);
			clear(e);
			e.index = ents.add(e);
			e.renderNext = null;
			map.add(e);
		}
	}
	
	@Override
	protected void clear() {
		for (WEntityConstructor<?> c : constructors)
			c.clear();
		ents.clear();
		map.clear();
	}

	private void clear(WEntity e) {
		e.index = -1;
		e.renderNext = null;
		e.gridX = -1;
		e.gridY = -1;
	}
	
	@Override
	public void save(FilePutter file) {
		file.i(ents.size());
		for (WEntity e : ents) {
			file.i(e.constructor().index);
			e.save(file);
			e.hitBox.save(file);
		}
	}
	
	public boolean canAdd() {
		return ents.hasRoom();
	}
	
	void add(WEntity e) {
		if (e.index != -1)
			throw new RuntimeException();
		int i = ents.add(e);
		clear(e);
		e.index = i;
		map.add(e);

	}

	void remove(WEntity e) {
		if (e.index == -1)
			throw new RuntimeException();
		map.remove(e);

		WEntity e2 = ents.remove(ents.size() - 1);

		if (e2 != e) {
			ents.replace(e.index, e2);
			e2.index = e.index;
		}
		clear(e);
	}


	@Override
	public void update(float ds) {
		for (int i = 0; i < ents.size(); i++) {
			WEntity e = ents.get(i);
			e.update(ds);
			if (!e.added())
				i--;
			else
				map.move(e);
		}
	}

	@Override
	protected void afterTick() {

	}

	public void fill(RECTANGLE area, LISTE<WEntity> result) {
		map.fill(area, result);
	}
	
	public LIST<WEntity> fill(RECTANGLE area) {
		tmp.clear();
		fill(area, tmp);
		return tmp;
	}
	
	public LIST<WEntity> fill(int x1, int x2, int y1, int y2) {
		tmp.clear();
		map.fill(x1, x2, y1, y2, tmp);
		return tmp;
	}
	
	public LIST<WEntity> fillTiles(int x1, int x2, int y1, int y2) {
		return fill(x1*C.TILE_SIZE, x2*C.TILE_SIZE, y1*C.TILE_SIZE, y2*C.TILE_SIZE);

	}
	
	public LIST<WEntity> fill(int x1, int y1) {
		tmp.clear();
		map.fill(x1, x1+1, y1, y1+1, tmp);
		return tmp;
	}
	
	public void fill(COORDINATE coo, LISTE<WEntity> result) {
		fill(coo.x(), coo.y(), result);
	}

	public void fill(int x, int y, LISTE<WEntity> result) {
		map.fill(x, y, result);
	}

	public WEntity getTallest(COORDINATE coo) {
		tmp.clear();
		fill(coo, tmp);
		WEntity tallest = null;
		double dist = Double.MAX_VALUE;
		for (WEntity e : tmp) {
			double d = coo.distance(e.body().cX(), e.body().cY());
			if (tallest == null || d < dist) {
				tallest = e;
				dist = d;
			}
		}
		return tallest;
	}

	public boolean areaIsClearOfEnts(RECTANGLE rec) {
		return fill(rec).isEmpty();
	}

	public LISTE<WEntity> getTempsAtTile(int tileX, int tileY, int tilesX, int tilesY) {

		tmp.clear();

		rectmp.set(tileX * C.TILE_SIZE, (tileX + tilesX) * C.TILE_SIZE, tileY * C.TILE_SIZE,
				(tileY + tilesY) * C.TILE_SIZE);

		fill(rectmp, tmp);

		return tmp;
	}

	public void renderBelowTerrain(Renderer r, ShadowBatch s, float ds, RECTANGLE renWindow, int offX, int offY) {
		
		
		
		offX = offX - renWindow.x1();
		offY = offY - renWindow.y1();

		

		for (WEntity e : tmp) {
			e.renderBelowTerrain(r, s, ds, e.body().x1() + offX, e.body().y1() + offY);
		}

	}

	public void renderAboveTerrain(Renderer r, ShadowBatch s, float ds, RECTANGLE renWindow, int offX, int offY) {

		renderables.clear();
		map.fill(renWindow, renderables);
		
		offX = offX - renWindow.x1();
		offY = offY - renWindow.y1();
		tmp.clear();
		
		WEntity e;
		while (renderables.hasMore()) {
			e = renderables.pollGreatest();
			tmp.add(e);
			e.renderAboveTerrain(r, s, ds, e.body().x1() + offX, e.body().y1() + offY);
		}

	}
	
	public LIST<WEntity> all(){
		return ents;
	}

}
