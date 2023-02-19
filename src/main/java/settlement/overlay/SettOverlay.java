package settlement.overlay;

import init.C;
import init.D;
import init.resources.RESOURCE;
import init.sprite.SPRITES;
import settlement.entity.ENTITY;
import settlement.environment.SettEnvMap.SettEnv;
import settlement.main.RenderData;
import settlement.main.RenderData.RenderIterator;
import settlement.main.SETT;
import settlement.path.finder.SFinderFindable;
import settlement.room.main.*;
import settlement.room.service.module.RoomServiceDataAccess.ROOM_SERVICE_ACCESS_HASER;
import settlement.thing.THINGS.Thing;
import settlement.thing.halfEntity.HalfEntity;
import settlement.tilemap.TGrowable;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.*;
import util.colors.GCOLORS_MAP;
import util.dic.DicMisc;

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
			renderUnder(v, r, it, false);
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
				v = (double)b.size.DM.get(it.tile());
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
	
	public final Addable FISH = new Addable(adders, "FISH", D.g("Fish"), D.g("FishD", "Shows where fish is plentiful")) {
		

		@Override
		public boolean render(Renderer r, RenderIterator it) {
			if (SETT.TERRAIN().WATER.is(it.tile()) && !SETT.ROOMS().map.is(it.tile())) {
				
				double v = SETT.ENV().fish.get(it.tile());
				Addable.renderColor(v, r, it, true);
			}
			return false;
		}
	};
	
	public Addable MAINTENANCE = new Addable(adders, "MAINTENANCE", D.g("Maintenance"), D.g("MaintenanceD", "Highlights what tiles need maintenance"), true, true) {
		
		private final ColorImp c = new ColorImp();
		
		@Override
		public boolean render(Renderer r, RenderIterator it) {
			if (SETT.MAINTENANCE().isser.is(it.tile())) {
				COLOR c = GCOLORS_MAP.BAD;
				if (SETT.MAINTENANCE().finder().getReserved(it.tx(), it.ty()) != null)
					c = GCOLORS_MAP.SOSO;
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
			
			c.set(COLOR.WHITE05);
			if (room != null) {
				if (room.degrader(it.tx(), it.ty()) != null)
					c.interpolate(GCOLORS_MAP.SOSO, GCOLORS_MAP.bestOverlay, 1.0-room.getDegrade(it.tx(), it.ty()));
			}else if (SETT.FLOOR().getter.is(it.tile())) {
				c.interpolate(GCOLORS_MAP.SOSO, GCOLORS_MAP.bestOverlay, 1.0-SETT.FLOOR().degrade.get(it.tile()));
			}
			Addable.renderUnder(c, r, it);
		};
		
	};
	

	

	
	public Addable ROADING = new Addable(adders, "ROADING", D.g("Path-Usage"), D.g("Path-UsageD", "Highlights the tiles your subjects use when moving."), true, false) {
		
		@Override
		public void renderBelow(Renderer r, RenderIterator it) {
			renderUnder(CLAMP.d(SETT.PATH().huristics.getter.get(it.tile())*16, 0, 1), r, it);
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
	
	public Addable SHAPE = new Addable(adders, "SHAPE", D.g("Shape"), D.g("ShapeD", "Most rooms get either a square or round shape when built. This overlay shows roundness as good, and square as bad."), true, false) {
		
		@Override
		public void renderBelow(Renderer r, RenderIterator it) {
			double v = 0;
			RoomInstance ins = SETT.ROOMS().map.instance.get(it.tile());
			if (ins != null && ins.shape() != 0)
				v = ins.shape() < 0 ? 0 : 1;
			renderUnder(v, r, it);
		};

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
		rooms.set(rooms.getI()).set(rx, ry);
		rooms.set(rooms.getI()+1);
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
