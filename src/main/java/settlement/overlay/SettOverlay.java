package settlement.overlay;

import init.C;
import init.D;
import init.resources.RESOURCE;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.entity.ENTITY;
import settlement.environment.SettEnvMap.SettEnv;
import settlement.job.Job;
import settlement.main.SETT;
import settlement.path.finder.SFinderFindable;
import settlement.room.main.*;
import settlement.room.service.module.RoomServiceAccess.ROOM_SERVICE_ACCESS_HASER;
import settlement.thing.THINGS.Thing;
import settlement.thing.halfEntity.HalfEntity;
import settlement.tilemap.terrain.TGrowable;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.*;
import util.colors.GCOLORS_MAP;
import util.dic.DicMisc;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;

public final class SettOverlay {

	private final LinkedList<Addable> adders = new LinkedList<>();
	private final LIST<Env> envs;

	{
		D.gInit(this);
	}
	
	public Addable RESOURCES = new Addable(adders, "RESOURCES", D.g("Resources"), D.g("ResourcesD", "Highlights deposits and wild growth"), false, true) {
		
		@Override
		public boolean render(Renderer r, RenderIterator it) {
			
			if (SETT.MINERALS().getter.is(it.tile())) {
				MINERALS.renderBelow(r, it);
				
				MINERALS.render(r, it);
				return true;
			}
			if (SETT.TERRAIN().get(it.tile()) instanceof TGrowable) {
				EDIBLES.renderBelow(r, it);
				EDIBLES.render(r, it);
				return true;
			}
			return false;
		}
		
	};
	
	public Addable MINERALS = new Addable(adders, "MINERALS", D.g("Deposits"), D.g("DepositsD", "Highlights deposits that can be mined"), true, true) {
		
		@Override
		public boolean render(Renderer r, RenderIterator it) {
			if (SETT.ROOMS().map.is(it.tile()))
				return false;
			if (SETT.MINERALS().getter.is(it.tile())) {
				COLOR.unbind();
				double am = 0.25 + 0.75*SETT.MINERALS().amountD.get(it.tile());
				ColorImp.TMP.interpolate(COLOR.WHITE20, COLOR.WHITE100, am).bind();;
				int size = (int) (C.TILE_SIZE*(am));
				int off = (C.TILE_SIZE-size)/2;
				renderAbove(am, r, it, true);
				COLOR.unbind();
				SETT.MINERALS().getter.get(it.tile()).resource.icon().render(r, it.x()+off, it.x()+off+size, it.y()+off, it.y()+off+size);
				return true;
			}
			return false;
		}
		
		@Override
		public void renderBelow(Renderer r, RenderIterator it) {
			double v = SETT.MINERALS().amountD.get(it.tile());
			if (!SETT.TERRAIN().CAVE.is(it.tile()))
				renderUnder(v, r, it, false);
		};
	};
	
	
	public Addable WATER_SWEET = new Addable(adders, "GROUND_WATER", D.g("GroundW", "Ground Water"), D.g("GroundWD", "Ground water of either sweet (blue) or salt (yellow). Natural water can be added or removed here."), true, false) {
		
		@Override
		public void renderBelow(Renderer r, RenderIterator it) {
			COLOR c = COLOR.WHITE05;
			if (SETT.TERRAIN().WATER.groundWater.is(it.tile())) {
				c = GCOLORS_MAP.bestOverlay;
			}else if (SETT.TERRAIN().WATER.groundWaterSalt.is(it.tile())) {
				c = COLOR.YELLOW100;
			}
			renderUnder(c, r, it);
		};
	};
	
	public Addable EDIBLES = new Addable(adders, "GROWABLES", D.g("Harvestable"), D.g("HarverstableD", "Highlights wild growing things that can be harvested."), true, true) {
		
		@Override
		public boolean render(Renderer r, RenderIterator it) {
			if (SETT.JOBS().getter.get(it.tile()) == null && SETT.TERRAIN().get(it.tile()) instanceof TGrowable) {
				
				if (SETT.TERRAIN().get(it.tile()) instanceof TGrowable) {
					COLOR.unbind();
					double am =1.0;
					ColorImp.TMP.interpolate(COLOR.WHITE20, COLOR.WHITE100, am).bind();;
					int size = (int) (C.TILE_SIZE*(am));
					int off = (C.TILE_SIZE-size)/2;
					COLOR.unbind();
					((TGrowable)SETT.TERRAIN().get(it.tile())).growable.resource.icon().render(r, it.x()+off, it.x()+off+size, it.y()+off, it.y()+off+size);
					
					return true;
				}
			}
			return false;
		}
		
		@Override
		public void renderBelow(Renderer r, RenderIterator it) {
			
			double v = 0;
			if (SETT.TERRAIN().get(it.tile()) instanceof TGrowable) {
				TGrowable b= (TGrowable) SETT.TERRAIN().get(it.tile());
				v = 0.5 + 0.5*(double)b.size.DM.get(it.tile());
			}
			renderUnder(v, r, it, false);
		};
	};
	
	public Addable FERTILITY = new Addable(adders, "FERTILITY", D.g("Fertility"), D.g("FertilityD", "Highlights fertility, including that which is gained by sweet water"), true, true) {
		
		@Override
		public void renderBelow(Renderer r, RenderIterator it) {
			double d = SETT.FERTILITY().target.get(it.tile());
			d*=d;
			renderUnder(d, r, it, false);
			if (d > 0.75) {
				d = (d-0.75)*4;
				renderPluses(d, r, it);
			}
		};
		
		@Override
		public boolean render(Renderer r, RenderIterator it) {
			double d = SETT.FERTILITY().target.get(it.tile());
			d*=d;
			if (renderAbove(d, r, it, false)) {
				if (d > 0.75) {
					d = (d-0.75)*4;
					renderPluses(d, r, it);
				}
				return true;
			}
			return false;
		};
		
		
	};
	
	public Addable FERTILITY_BASE = new Addable(adders, "FERTILITY_base", D.g("Fertility-base"), D.g("FertilityBaseD", "Highlights base ground fertility"), true, true) {
		
		@Override
		public void renderBelow(Renderer r, RenderIterator it) {
			double d = SETT.FERTILITY().baseD.get(it.tile());
			d*= d;
			renderUnder(d, r, it, false);
			if (d > 0.75) {
				d = (d-0.75)*4;
				renderPluses(d, r, it);
			}
		};
		
		@Override
		public boolean render(Renderer r, RenderIterator it) {
			double d = SETT.FERTILITY().baseD.get(it.tile());
			d*=d;
			if (renderAbove(d, r, it, false)) {
				if (d > 0.75) {
					d = (d-0.75)*4;
					renderPluses(d, r, it);
				}
				return true;
			}
			return false;
		};
		
	};
	
	public final Addable FISH = new Addable(adders, "FISH", D.g("Fish"), D.g("FishD", "Found along the shorelines. Building fisheries on these spot allows for sending out boats, and allows for many more fishermen."), false, true) {
		
		@Override
		public boolean render(Renderer r, RenderIterator it) {
			if (SETT.TERRAIN().WATER.SHALLOW.is(it.tile()) && SETT.TERRAIN().WATER.deepSeaFishSpot.is(it.tile())) {
				GCOLORS_MAP.bestOverlay.bind();
				UI.icons().s.fish.renderScaled(r, it.x(), it.y(), C.SCALE);
			}
			return false;
		}
	};
	
	public final Addable STONE = new Addable(adders, "MOUNTAIN", D.g("Mountain"), D.g("MountainD", "Shows the strength of mountains"), false, true) {
		
		@Override
		public boolean render(Renderer r, RenderIterator it) {
			if (SETT.TERRAIN().MOUNTAIN.is(it.tile()) && !SETT.JOBS().getter.has(it.tile())) {
				
				renderUnder(0.25 + 0.5*SETT.TERRAIN().MOUNTAIN.strength(it.tile()), r, it, false);
				//renderPluses(SETT.TERRAIN().MOUNTAIN.strength(it.tile()), r, it);
				//renderUnder(SETT.TERRAIN().MOUNTAIN.strength(it.tile()), r, it, true);
			}
			return false;
		}
	};
	
	public Addable MAINTENANCE = new Addable(adders, "MAINTENANCE", D.g("Maintenance"), D.g("MaintenanceD", "Highlights what tiles need maintenance"), true, true) {
		

		@Override
		public boolean render(Renderer r, RenderIterator it) {
			if (SETT.MAINTENANCE().isser.is(it.tile())) {
				COLOR c = GCOLORS_MAP.BAD;
				if (SETT.MAINTENANCE().finder().getReserved(it.tx(), it.ty()) != null)
					c = GCOLORS_MAP.SOSO;
				if (SETT.MAINTENANCE().disabled.is(it.tile())) {
					COLOR.WHITE50.bind();
				}
				c.bind();
				SPRITES.cons().BIG.outline.render(r, 0, it.x(), it.y());
				COLOR.unbind();
				RESOURCE res = SETT.MAINTENANCE().resourceNeeded(it.tx(), it.ty());
				if (res != null) {
					res.icon().renderScaled(r, it.x()+C.TILE_SIZEH/4, it.y()+C.TILE_SIZEH/4, 2);
				}
				return true;
			}
			return false;
		}
		
		@Override
		public void renderBelow(Renderer r, RenderIterator it) {
			Room room = SETT.ROOMS().map.get(it.tile());
			COLOR c = COLOR.WHITE05;
			
			if (SETT.MAINTENANCE().disabled.is(it.tile())) {
				c = GCOLORS_MAP.map_not_ok;
				return;
			}else if (room != null) {
				if (room.degrader(it.tx(), it.ty()) != null) {
					if (room.getDegrade(it.tx(), it.ty()) > 0)
						c = GCOLORS_MAP.SOSO;
					else
						c = GCOLORS_MAP.bestOverlay;
				}
			}else if (SETT.FLOOR().getter.is(it.tile())) {
				if (SETT.FLOOR().degrade.get(it.tile()) > 0)
					c = GCOLORS_MAP.SOSO;
				else
					c = GCOLORS_MAP.bestOverlay;
			}
			Addable.renderUnder(c, r, it);
		};
		
	};
	
	public Addable ROADING = new Addable(adders, "ROADING", D.g("Path-Usage"), D.g("Path-UsageD", "Highlights the tiles your subjects use when moving."), true, false) {
		
		@Override
		public void renderBelow(Renderer r, RenderIterator it) {
			if (SETT.ROOMS().map.is(it.tile()))
				return;
			if (SETT.JOBS().getter.get(it.tile()) != null)
				return;
			
			double p = SETT.PATH().huristics.getter.get(it.tile())*16;
			p = CLAMP.d(p, 0, 1);
			if (SETT.FLOOR().getter.is(it.tile()) || SETT.JOBS().jobGetter.is(it.tile())) {
				ColorImp.TMP.interpolate(COLOR.WHITE05, GCOLORS_MAP.bestOverlay, p).bind();;
				renderPluses(p, r, it);
				return;
			}
			boolean b = Job.overwrite;
			Job.overwrite = false;
			if (SETT.JOBS().roads.get(0).placer().isPlacable(it.tx(), it.ty(), null, null) != null) {
				Job.overwrite = b;
				return;
			}
			Job.overwrite = b;

			
			double d = 0.25 + SETT.PATH().huristics.getter.get(it.tile())*8;
			d = CLAMP.d(d, 0, 1);
			renderUnder(d, r, it, false);
			ColorImp.TMP.interpolate(COLOR.WHITE05, GCOLORS_MAP.bestOverlay, p).bind();;
			renderPluses(p, r, it);
		};
	};
	
	public Addable WORKLOAD = new Addable(adders, "WORKLOAD", DicMisc.¤¤Workload, DicMisc.¤¤WorkloadD, true, false) {
		
		private final ColorImp c = new ColorImp();
		
		@Override
		public void renderBelow(Renderer r, RenderIterator it) {
			c.set(COLOR.WHITE05);
			Room ro = SETT.ROOMS().map.get(it.tx(), it.ty());
			if (ro != null && ro instanceof RoomInstance) {
				RoomInstance i = (RoomInstance) ro;
				if (i.blueprintI().employment() != null) {
					c.interpolate(GCOLORS_MAP.worstOverlay, GCOLORS_MAP.bestOverlay, i.employees().efficiency());
				}
				
			}
			renderUnder(c, r, it);
		}
	};
	
	private final ServiceRadius service = new ServiceRadius(adders);
	private final RadiusInter radius = new RadiusInter(adders);
	
	public void service(ROOM_SERVICE_ACCESS_HASER blue, RoomInstance ins) {
		service.add(blue, ins);
	}

	public void RadiusInter(RoomBlueprintIns<? extends RADIUS_INTER> blue, SFinderFindable fin) {
		radius.add(blue, fin);
	}

	public void RadiusInter(RoomBlueprintIns<? extends RADIUS_INTER> blue, SFinderFindable fin, int tx, int ty, double ra) {
		radius.add(blue, fin, tx, ty, ra);
	}

	
	
	public Addable HOMELESS = new Homeless(adders, "HOMELESS", D.g("Homeless"), D.g("HomelessD", "Highlights homeless workplaces and homeless oddjobbers"));
	
	public LIST<Addable> all(){
		return adders;
	}
	
	private final ArrayList<Addable> tmp = new ArrayList<>(adders.size());
	
	private final ArrayList<ON_TOP_TILE> tiles = new ArrayList<>(100);
	private final ArrayList<BODY_HOLDER> objects = new ArrayList<>(100);
	private final ArrayCooShort rooms = new ArrayCooShort(5);
	private final COLOR[] colors = new COLOR[100];
	

	
	public boolean renderOnGround(Renderer r, RenderData data, int zoomout) {
		Addable aa = getUnder();
		for (Addable a : adders) {
			a.added = false;
		}
		
		if (aa == null)
			return false;
		
		
		
		r.newLayer(true, zoomout);
		
		RenderIterator it = data.onScreenTiles();
		aa.initBelow(data);
		while(it.has()) {
			aa.renderBelow(r, it);
			it.next();
		}
		aa.finishBelow();
		COLOR.unbind();
		return true;
	}
	
	private Addable getUnder() {
		Addable aa = null;
		for (Addable a : adders) {
			if (a.added && a.under) {
				aa = a;
			}
		}
		return aa;
	}
	
	private void prune() {
		Addable aa = null;
		for (Addable a : adders) {
			if (a.added && a.exclusive) {
				if (aa != null)
					a.added = false;
				aa = a;
			}
		}
	}
	
	public void renderAbove(Renderer r, RenderData data, int zoomout) {
		tmp.clear();
		prune();
		for (Addable a : adders) {
			if (a.added && a.above) {
				a.initAbove(data);
				tmp.add(a);
			}
		}
		r.newLayer(true, zoomout);
		RenderIterator it = data.onScreenTiles();
		
		
		while(it.has()) {
			
			for (Addable a : tmp) {
				if (a.render(r, it))
					break;
			}
			
			it.next();
		}
		
		for (Addable a : tmp) {
			a.finishAbove();
		}
		ents(r, data);
	}
	
	private void ents(Renderer r, RenderData data) {
		if (objects.size() == 0 && rooms.getI() == 0 && tiles.size() == 0)
			return;
		
		for (int i = 0; i < objects.size(); i++) {
			colors[i].bind();
			BODY_HOLDER e = objects.get(i);
			SPRITES.cons().BIG.outline.renderBox(r, e.body().x1() - data.offX1(), e.body().y1() - data.offY1(), e.body().width(), e.body().height());
		}
		
		
		objects.clear();
		
		int rI = rooms.getI();
		for (int i = 0; i < rI; i++) {
			COORDINATE c = rooms.set(i);
			Room room = SETT.ROOMS().map.get(c);
			if (room != null) {
				

				if (SETT.ARMIES().map.army.get(room.mX(c.x(), c.y()), room.mY(c.x(), c.y())) == SETT.ARMIES().enemy())
					COLOR.RED2RED.bind();
				else
					COLOR.BLUE2BLUE.bind();
				int x1 = room.x1(c.x(), c.y());
				int x2 = x1 + room.width(c.x(), c.y());
				int y1 = room.y1(c.x(), c.y());
				int y2 = y1 + room.height(c.x(), c.y());
				int mx = room.mX(c.x(), c.y());
				int my = room.mY(c.x(), c.y());
				for (int ty =  y1-1; ty <= y2; ty++) {
					for (int tx =  x1-1; tx <= x2; tx++) {
						if (room.isSame(mx, my, tx, ty))
							continue;
						
						int m = 0;
						for (int di = 0; di < DIR.ORTHO.size(); di++) {
							DIR d = DIR.ORTHO.get(di);
							if (!room.isSame(mx, my, tx+d.x(), ty+d.y()))
								m |= d.mask();
						}
						if (m != 0x0F) {
							int x = tx*C.TILE_SIZE - data.offX1();
							int y = ty*C.TILE_SIZE - data.offY1();
							SPRITES.cons().BIG.outline.render(r, m, x, y);
						}
					}
						
				}
			}
		}
		rooms.set(0);
		
		COLOR.unbind();
		
		for (ON_TOP_TILE t : tiles) {
			t.render(r, null, data);
		}
		
		tiles.clearSloppy();
		
	}
	
	public Addable envThing(SettEnv t) {
		return envs.get(t.index());
	}
	
	public void add(int rx, int ry) {
		if (rooms.getI() < rooms.size()-1) {
			rooms.set(rooms.getI()).set(rx, ry);
			rooms.set(rooms.getI()+1);
		}
	}
	
	public void add(ENTITY e) {
		add(e, e.minimapColor());
	}
	
	public void add(Thing t) {
		add(t, COLOR.WHITE2WHITE);
	}
	
	public void add(HalfEntity t) {
		add(t, COLOR.WHITE2WHITE);
	}
	
	public void add(ON_TOP_TILE t) {
		if (tiles.contains(t))
			return;
		tiles.add(t);
	}
	
	public void add(BODY_HOLDER object, COLOR c) {
		if (!objects.hasRoom())
			return;
		objects.add(object);
		int i = objects.size()-1;
		colors[i] = c;
	}

	
	public SettOverlay(){
		LinkedList<Env> ee = new LinkedList<>();
		for (SettEnv s : SETT.ENV().environment.all()) {
			
			ee.add(new Env(adders, s));
		}
		this.envs = new ArrayList<Env>(ee);
	}
	
	

	
}
