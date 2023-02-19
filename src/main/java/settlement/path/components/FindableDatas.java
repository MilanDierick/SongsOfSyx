package settlement.path.components;

import static settlement.main.SETT.*;

import init.RES;
import init.resources.RESOURCE;
import settlement.entity.ENTITY;
import settlement.entity.animal.Animal;
import settlement.entity.humanoid.Humanoid;
import settlement.job.Job;
import settlement.main.SETT;
import settlement.misc.util.*;
import settlement.path.finder.SFinderFindable;
import settlement.room.home.HOME;
import settlement.room.main.Room;
import settlement.thing.THINGS.Thing;
import settlement.thing.ThingFindable;
import settlement.thing.ThingsResources.ScatteredResource;
import snake2d.util.datatypes.*;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.data.DataOL;
import util.data.INT_O.INT_OE;

public final class FindableDatas {
	
	
	public final FindableDataRes resScattered;
	public final FindableDataRes resCrate;
	public final FindableDataRes resCrateGet;
	public final FindableDataRes storage;
	public final FindableDataRes jobs;
	public final LIST<FindableDataRes> RESSES;
	
	private final FindableDataSingle[] findable;
	
	public final FindableDataSingle job;
	public final FindableDataSingle jobHarvest;
	private final FindableDataSingle[] people;
	public final FindableDataSingle reservableAnimals;
	public final FindableDataHome home;
	public final LIST<FindableDataSingle> SINGLES;
	
	final INT_OE<SComponent> checked;
	
	FindableDatas() {
		
		FindableData.datao = new DataOL<SComponent>() {

			@Override
			protected long[] data(SComponent t) {
				return t.fdata;
			}
		
		};
		FindableData.all.clear();
		FindableDataSingle.all.clear();
		FindableDataRes.all.clear();		
		checked = FindableData.datao.new DataBit();
		
		{
			findable = new FindableDataSingle[SFinderFindable.all().size()];
			for (int i = 0; i < findable.length; i++)
				findable[i] = new FindableDataSingle(SFinderFindable.all().get(i).name);
		}
		
		resScattered = new FindableDataRes("R");
		resCrate = new FindableDataRes("Crate");
		resCrateGet = new FindableDataRes("CGet");
		storage = new FindableDataRes("Store");
		jobs = new FindableDataRes("jobs");
		
		job = new FindableDataSingle("Job");
		jobHarvest = new FindableDataSingle("Job Harvest");
		people = new FindableDataSingle[] {
			new FindableDataSingle("Friendlies"),
			new FindableDataSingle("Enemies"),
		};
		reservableAnimals = new FindableDataSingle("Animals");
		home = new FindableDataHome();
		RESSES = new ArrayList<>(FindableDataRes.all);
		SINGLES = new ArrayList<>(FindableDataSingle.all);
	}
	
	public FindableDataSingle people(boolean friend) {
		return people[friend ? 0 : 1];
	}
	
	FindableDataSingle service(SFinderFindable f) {
		return findable[f.index];
	}
	
	
	void initComponent0(SComp0 c, RECTANGLE tiles) {

		c.clearData();
		
		
		int tx1 = tiles.x1() - (tiles.x1() > 0 ? 1 : 0);
		int tx2 = tiles.x2() + (tiles.x2() < TWIDTH ? 1 : 0);
		int ty1 = tiles.y1() - (tiles.y1() > 0 ? 1 : 0);
		int ty2 = tiles.y2() + (tiles.y2() < THEIGHT ? 1 : 0);
		
		
		for (int y = ty1; y < ty2; y++) {
			for (int x = tx1; x < tx2; x++) {
				if (!is(c, x, y))
					continue;
				
				for (Thing t : THINGS().get(x, y)) {
					
					if ((t instanceof ScatteredResource)) {
						ScatteredResource rw = (ScatteredResource) t;
						if (!rw.findableReservedCanBe())
							continue;
						resScattered.add(c, rw.resource());
					}else if (t instanceof ThingFindable) {
						ThingFindable ti = (ThingFindable) t;
						if (ti.findableReservedCanBe())
							findable[ti.finder().index].add(c);
						
					}
						
				}
				
				for (ENTITY ent : ENTITIES().getAtTile(x, y)) {
					if (ent instanceof Humanoid) {
						Humanoid a = (Humanoid) ent;
						people(!a.indu().hostile()).add(c);
					}else if (ent instanceof Animal) {
						if (((Animal)ent).reservable())
							reservableAnimals.add(c);
					}
				}
				
				if (TERRAIN().WATER.reservable(x, y))
					findable[SETT.PATH().finders().water.index].add(c);
				else if (PATH().finders.indoor.getReservable(x, y) != null)
					findable[SETT.PATH().finders().indoor.index].add(c);
				
				Job j = JOBS().getter.get(x, y);
				if (j != null && j.jobReserveCanBe()) {
					RESOURCE r = j.resourceCurrentlyNeeded();
					if (r != null) {
						jobs.add(c, j.resourceCurrentlyNeeded());
					}else if (j.needsRipe())
						jobHarvest.add(c);
					else
						job.add(c);
						
				}
				
				if (SETT.MAINTENANCE().finder().getReservable(x, y) != null)
					findable[SETT.MAINTENANCE().finder().index].add(c);
				
				Room i = ROOMS().map.get(x, y);
				if (i == null)
					continue;
				
				{
					SFinderFindable se = i.blueprint().service(x, y);
					
					if (se != null) {
						
						FINDABLE t = se.getReservable(x, y);
						
						if (t != null )
							findable[se.index].add(c);
					}
				}
				
				{
					RESOURCE_TILE r = i.resourceTile(x, y);
					if (r != null && r.findableReservedCanBe()) {
						if (r.isfetching()) {
							resCrateGet.add(c, r.resource());
						}else if (r.isStoring()) {
							resCrate.add(c, r.resource());
						}else {
							resScattered.add(c, r.resource());
						}
					}
				}
				
				{
					TILE_STORAGE s = i.storage(x, y);
					if (s != null && s.resource() != null && s.storageReservable() > 0) {
						storage.add(c, s.resource());
					}
				}
				
				{
					HOME h = HOME.getS(x, y, this);
					if (h != null) {
						if (h.availability() != null) {
							home.add(c, h.availability());
						}
						h.done();
					}
				}
				
			}
		}
		
	}
	
	void initComponentN(SCompN c, SComponentChecker checker, RECTANGLE boundsC) {

		c.clearData();
		c.edgeMask = 0;
		
		RES.filler().init(this);
		RES.filler().fill(c.centreX(), c.centreY());
		
		SComponentLevel l = SETT.PATH().comps.all.get(c.level().level()-1);

		while(RES.filler().hasMore()) {
			COORDINATE coo = RES.filler().poll();
			SComponent s = l.get(coo);
			checker.isSetAndSet(s);
			c.edgeMask |= s.edgeMask();
			for (FindableData d : FindableData.all) {
				if (d.get(s) > 0) {
					d.add(c);
				}
			}
			
			SComponentEdge e = s.edgefirst();
			while(e != null) {
				if (e.to().superComp() == c && boundsC.holdsPoint(e.to().centreX(), e.to().centreY())) {
					RES.filler().fill(e.to().centreX(), e.to().centreY());
				}
				e = e.next();
			}
			
		}
		
		RES.filler().done();
	}

	private boolean is(SComponent c, int tx, int ty) {
		
		for (DIR d : DIR.ORTHO)
			if (c.is(tx, ty, d))
				return true;
		return c.is(tx, ty);
	}

}
