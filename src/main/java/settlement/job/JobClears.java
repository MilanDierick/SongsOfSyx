package settlement.job;

import static settlement.main.SETT.*;

import game.GAME;
import game.faction.FResources.RTYPE;
import init.*;
import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCE;
import init.sound.SoundSettlement.Sound;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import settlement.entity.ENTITY;
import settlement.entity.animal.Animal;
import settlement.entity.humanoid.Humanoid;
import settlement.job.StateManager.State;
import settlement.main.SETT;
import settlement.room.industry.mine.ROOM_MINE;
import settlement.tilemap.terrain.TGrowable;
import settlement.tilemap.terrain.Terrain.TerrainTile;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.*;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.rnd.RND;
import snake2d.util.sets.*;
import snake2d.util.sprite.SPRITE;
import util.dic.DicMisc;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.info.GFORMAT;
import view.main.VIEW;
import view.tool.*;

public final class JobClears {


	JobClears() {
		
	}
	

	
	private static CharSequence ¤¤harvestDesc = "This job is dormant since there currently isn't enough growth to harvest anything. The job will become active once growth occurs.";
	private static CharSequence ¤¤hunt = "Hunt";
	private static CharSequence ¤¤huntD = "Manually hunt the wild animals on the map for resources. Note that hunting can be dangerous. A hunter might get mauled from time to time.";
	private static CharSequence ¤¤huntCancel = "Cancel Hunt";
	
	{
		D.t(this);
	}
	
	public final Job stone = new JobClear(
			D.g("Stone", "Clear Rock"),  
			D.g("StoneD", "Removes rocks on the map. Yields the resource stone"),
			D.g("StoneV", "Clearing rocks"),
			SETT.TERRAIN().ROCK.getIcon()) 
	{
		
		@Override
		protected CharSequence problem(int tx, int ty, boolean overwrite) {
			if (super.problem(tx, ty, overwrite) != null)
				return super.problem(tx, ty, overwrite);
			if (!TERRAIN().ROCK.is(tx, ty)) {
				return PlacableMessages.¤¤ROCK_MUST;
			}
			return null;
		}
		
		@Override
		public double jobPerformTime(Humanoid skill) {
			return 45;
			
		};
	};
	
	public final Job wood = new JobClear(
			D.g("Tree", "Fell Tree"),  
			D.g("TreeD", "Removes trees and yields wood. Trees will slowly grow back in time."),
			D.g("TreeV", "Chopping lumber"),
			SETT.TERRAIN().TREES.icon) 
	{

		@Override
		protected CharSequence problem(int tx, int ty, boolean overwrite) {
			if (super.problem(tx, ty, overwrite) != null)
				return super.problem(tx, ty, overwrite);
			if (!TERRAIN().TREES.isTree(tx, ty)) {
				return PlacableMessages.¤¤TREE_MUST;
			}
			return null;
		}
		
	};
	
	void HoverEdible(GBox box, int tx, int ty){
		
		
		

		TerrainTile t = TERRAIN().get(tx, ty);
		if (t instanceof TGrowable) {
			box.title(food.name);
			TGrowable g = (TGrowable)t;
			RESOURCE r = ((TGrowable)t).growable.resource;
			box.NL();
			box.add(r.icon());
			int am = g.resource.get(tx, ty);
			box.add(GFORMAT.iofk(box.text(), g.resource.get(tx, ty), g.size.get(tx, ty)));
			if (am == 0) {
				box.NL();
				box.error(¤¤harvestDesc);
			}
		}
		
		
	}
	
	public final Job food = new JobClear(
			D.g("Food", "Forage"),  
			D.g("FoodD", "Forage Wild Growing crops. These re-grow with each year."),
			D.g("FoodV", "Gathering"),
			SPRITES.icons().m.clear_food) 
	{
		
		
		@Override
		protected CharSequence problem(int tx, int ty, boolean overwrite) {
			if (super.problem(tx, ty, overwrite) != null)
				return super.problem(tx, ty, overwrite);
			if (SETT.FLOOR().getter.get(tx, ty) != null)
				return PlacableMessages.¤¤ROAD_ALREADY;
			
			TerrainTile t = TERRAIN().get(tx, ty);
			if (t instanceof TGrowable) {
				TGrowable g = (TGrowable) t;
				if (g.job.is(tx, ty)) {
					return PLACABLE.E;
				}
				return null;
			}
			
			return PlacableMessages.¤¤NOT_EDIBLE;
		}
		
		@Override
		public void doSomethingExtraRender() {
			
		};
		
		@Override
		public void hover(GBox box) {
			super.hover(box);
			
			if (!SETT.WEATHER().growthRipe.cropsAreRipe()) {
				box.NL();
				box.error(PlacableMessages.¤¤NOT_RIPE);
			}
			
			TerrainTile t = TERRAIN().get(coo);
			if (t instanceof TGrowable) {
				TGrowable g = (TGrowable)t;
				RESOURCE r = g.growable.resource;
				box.NL();
				box.add(r.icon());
				box.add(GFORMAT.iofk(box.text(), g.resource.get(coo), g.size.get(coo)));
			}
		}
		
		@Override
		void cancel(int tx, int ty) {
			TerrainTile t = TERRAIN().get(coo);
			if (t instanceof TGrowable) {
				((TGrowable)t).job.set(tx, ty, false);
			}
		};
		
		@Override
		public RESOURCE jobPerform(Humanoid skill, RESOURCE r, int ri) {
			TerrainTile t = TERRAIN().get(coo);
			if (t instanceof TGrowable) {
				TGrowable g = (TGrowable) t;
				boolean has = g.resource.get(coo) > 0;
				g.resource.increment(coo, -1);
				
				if (g.resource.get(coo) == 0) {
					
					PlacerDelete.place(coo.x(), coo.y());
					t = TERRAIN().get(coo);
					if (t instanceof TGrowable) {
						TGrowable b = (TGrowable) t;
						b.job.set(coo.x(), coo.y(), true);
					}
					
				}else
					JOBS().state.set(State.RESERVABLE, this);
				if (has) {
					GAME.player().res().inc(g.growable.resource, RTYPE.PRODUCED, 1);
					return g.growable.resource;
				}
				return null;
			}
			PlacerDelete.place(coo.x(), coo.y());
			return null;
		}
		
		@Override
		void renderAbove(SPRITE_RENDERER r, int x, int y, int mask, int tx, int ty) {
			SPRITES.cons().ICO.scratch.render(r, x, y);
		};
		
		@Override
		public boolean needsRipe() {
			return true;
		}
		
		@Override
		public PlacableMulti placer() {
			return foodPlacer;
		};
		
		public final PlacableMulti op = super.placer();
		
		
		public final PlacableMulti foodPlacer = new PlacableMulti(op.name(), op.desc, op.getIcon()) {
			
			int gi = -1;
			private final LinkedList<CLICKABLE> bb = new LinkedList<CLICKABLE>();
			private boolean overlay = true;
			
			{
				
				bb.add(new GButt.ButtPanel(SPRITES.icons().m.terrain) {
					
					@Override
					protected void clickA() {
						overlay = !overlay;
					};
					
					@Override
					protected void renAction() {
						selectedSet(overlay);
					};
					
				}.hoverTitleSet(DicMisc.¤¤Overlay));
				
				bb.add(new GButt.ButtPanel(SPRITES.icons().m.questionmark) {
					
					@Override
					protected void clickA() {
						gi = -1;
					};
					
					@Override
					protected void renAction() {
						selectedSet(gi == -1);
					};
					
				}.hoverTitleSet(DicMisc.¤¤All));
				
				int i = 1;
				for (TGrowable g : TERRAIN().GROWABLES) {
					final int k = i-1;
					i++;
					bb.add(new GButt.ButtPanel(g.getIcon()) {
						
						@Override
						protected void clickA() {
							gi = k;
						};
						
						@Override
						protected void renAction() {
							selectedSet(gi == k);
						};
						
					}.hoverTitleSet(g.name()));
				}
			}
			
			private final PlacableMulti undo = new PlacableMulti(DicMisc.¤¤delete) {
				
				@Override
				public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
					if (isPlacable(tx, ty, area, type) == null)
						SETT.JOBS().clearer.set(tx, ty);
					
					
				}
				
				@Override
				public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
					if (SETT.JOBS().getter.get(tx, ty) == food) {
						TerrainTile t = TERRAIN().get(tx, ty);
						if (t instanceof TGrowable) {
							TGrowable b = (TGrowable) t;
							if (gi < 0)
								return null;
							return SETT.JOBS().getter.get(tx, ty) == food && TERRAIN().GROWABLES.get(gi) == b ? null : E;	
						}
					}
					return E;
				}
				
				@Override
				public LIST<CLICKABLE> getAdditionalButt() {
					return bb;
				};
				
				@Override
				public void updateRegardless(view.subview.GameWindow window) {
					if (overlay)
						SETT.OVERLAY().EDIBLES.add();
				};
			};
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				TGrowable b = (TGrowable) TERRAIN().get(tx, ty);
				if (b.resource.get(tx, ty) <= 0)
					b.job.set(tx, ty, true);
				else
					op.place(tx, ty, area, type);
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				
				CharSequence s = op.isPlacable(tx, ty, area, type);
				if (s != null)
					return s;
				
				TerrainTile t = TERRAIN().get(tx, ty);
				if (t instanceof TGrowable) {
					TGrowable b = (TGrowable) t;
					if (gi < 0)
						return null;
					return TERRAIN().GROWABLES.get(gi) == b ? null : E;
				}
				return E;
				
			}
			
			@Override
			public void updateRegardless(view.subview.GameWindow window) {
				if (overlay)
					SETT.OVERLAY().EDIBLES.add();
			};
			
			@Override
			public CharSequence isPlacable(AREA area, PLACER_TYPE type) {
				return super.isPlacable(area, type);
			};

			@Override
			public LIST<CLICKABLE> getAdditionalButt() {
				return bb;
			};
			@Override
			public PLACABLE getUndo() {
				return undo;
			};
			
		};
	};
	

	
	public final Job water = new JobClear(
			D.g("Water", "Remove Water"),  
			D.g("WaterD", "Removes natural occurring water. Deep water can not be removed completely. But a passage-way can be created."),
			D.g("WaterV", "Removing water"),
			SETT.TERRAIN().WATER.SHALLOW.getIcon().twin(UI.icons().m.anti, DIR.C, 2)) 
	{
		@Override
		protected CharSequence problem(int tx, int ty, boolean overwrite) {
			if (super.problem(tx, ty, overwrite) != null)
				return super.problem(tx, ty, overwrite);
			
			if (!TERRAIN().WATER.is.is(tx, ty)) {
				return PlacableMessages.¤¤WATER_MUST;
			}
			return null;
		}
		
	};
	
	public final Job returnwater = new JobBuild(null, 1, false, 
			D.g("Canal", "Return water"),  
			D.g("CanalD", "Return water to where there is ground water."),
			SETT.TERRAIN().WATER.SHALLOW.getIcon()) {
		
		@Override
		void renderAbove(SPRITE_RENDERER r, int x, int y, int mask, int tx, int ty) {
			SPRITES.cons().ICO.unclear.render(r, x, y);
		}
		
		@Override
		protected double constructionTime(Humanoid skill) {
			return 45;
		}
		
		@Override
		protected Sound constructSound() {
			return RES.sound().settlement.action.dig;
		}
		
		@Override
		protected boolean construct(int tx, int ty) {
			if (TERRAIN().WATER.BRIDGE.is(tx, ty))
				TERRAIN().WATER.DEEP.placeFixed(coo.x(), coo.y());
			else
				TERRAIN().WATER.SHALLOW.placeFixed(coo.x(), coo.y());
			return false;
		}
		
		@Override
		protected CharSequence problem(int tx, int ty, boolean overwrite) {
			if (TERRAIN().WATER.BRIDGE.is(tx, ty))
				return null;
			if (super.problem(tx, ty, overwrite) != null)
				return super.problem(tx, ty, overwrite);

			if (!SETT.TERRAIN().WATER.groundWater.is(tx, ty) && !SETT.TERRAIN().WATER.groundWaterSalt.is(tx, ty))
				return PlacableMessages.¤¤WATER_RETURN;
			if (TERRAIN().WATER.is.is(tx, ty))
				return PlacableMessages.¤¤TERRAIN_BLOCK;
			return null;
		}

		private final Placer p = new Placer(this, placer.desc) {
			
			@Override
			public snake2d.util.sets.LIST<CLICKABLE> getAdditionalButt() {
				return butts;
			};
			
		};
		
		@Override
		public PlacableMulti placer() {
			return p;
		}

		@Override
		public TerrainTile becomes(int tx, int ty) {
			return TERRAIN().WATER.SHALLOW;
		};
	};
	
	private int raceSpeeds[];
	
	void initSpeeds() {
		raceSpeeds = new int[RACES.all().size()];
		for (Race r : RACES.all()) {
			double min = 0;
			for (ROOM_MINE m : SETT.ROOMS().MINES) {
				min = Math.max(min, m.industries().get(0).bonus().get(r));
			}
			
			raceSpeeds[r.index()] = (int) (60 / (1+min));
			
		}
	}

	
	public final Job tunnel = new JobClear(
			D.g("Tunnel", "Dig Into Mountain"),  
			D.g("TunnelD", "Digs a tunnel into the mountain"),
			D.g("TunnelV", "Tunnels into the mountain"),
			SETT.TERRAIN().MOUNTAIN.getIcon().twin(UI.icons().m.anti, DIR.C, 1)) 
	{

		double tunnelD = 0;
		
		@Override
		public RESOURCE jobPerform(Humanoid skill, RESOURCE r, int ri) {
			tunnelD += 1;
			
			while(tunnelD >= 1) {
				tunnelD --;
				RESOURCE res = tunnelPerform(coo);
				if (res != null) {
					GAME.player().res().inc(res, RTYPE.PRODUCED, 1);
				}
				if (!TERRAIN().MOUNTAIN.is(coo)) {
					PlacerDelete.place(coo.x(), coo.y());

					return res;
					
				}
				JOBS().state.set(State.RESERVABLE, this);
				return res;
				
			}
			JOBS().state.set(State.RESERVABLE, this);
			return null;
		}

		@Override
		protected CharSequence problem(int tx, int ty, boolean overwrite) {
			if (super.problem(tx, ty, overwrite) != null)
				return super.problem(tx, ty, overwrite);
			if (!TERRAIN().MOUNTAIN.is(tx, ty)) {
				return PlacableMessages.¤¤MOUNTAIN_MUST;
			}
			return null;
		};
		
		@Override
		public void doSomethingExtraRender() {
			SETT.OVERLAY().STONE.add();
		};
		
		@Override
		public boolean isConstruction() {
			return true;
		}
		
		@Override
		public double jobPerformTime(Humanoid skill) {
			return raceSpeeds[skill.race().index];
		};
		
		
	};
	
	public RESOURCE tunnelPerform(COORDINATE coo) {
		
		RESOURCE res = TERRAIN().get(coo.x(), coo.y()).clearing().clear1(coo.x(), coo.y());
		if (!TERRAIN().MOUNTAIN.is(coo)) {
			for (DIR d : DIR.ALLC) {
				if (TERRAIN().CAVE.canFix(coo.x()+d.x(), coo.y()+d.y())) {
					TERRAIN().CAVE.fix(coo.x()+d.x(), coo.y()+d.y());
				}
				GAME.count().TUNNELS.inc(1);
			}
		}
		
		return res;
	}
	
	public final Job caveFill = new JobBuildFillCave();
	
	
	public final PLACABLE woodAndRock = new PlacableMulti(
			D.g("Clear", "Clear All"), 
			D.g("ClearD", "Clears stone and wood"), SPRITES.icons().m.shovel) {
		
		@Override
		public void place(int tx, int ty, AREA a, PLACER_TYPE t) {
			if (wood.problem(tx, ty, false) == null)
				Placer.place(tx, ty, wood);
			else if (stone.problem(tx, ty, false) == null)
				Placer.place(tx, ty, stone);
		}
		
		@Override
		public CharSequence isPlacable(int tx, int ty, AREA a, PLACER_TYPE t) {
			
			
			if (wood.placer().isPlacable(tx, ty, a, t) == null)
				return null;
			
			if (stone.placer().isPlacable(tx, ty, a, t) == null)
				return null;
			
			if (wood.placer().isPlacable(tx, ty, a, t) != null)
				return wood.placer().isPlacable(tx, ty, a, t);
			
			if (stone.placer().isPlacable(tx, ty, a, t) != null)
				return stone.placer().isPlacable(tx, ty, a, t);
			
			return null;
		}
		
		@Override
		public PLACABLE getUndo() {
			return JOBS().tool_clear;
		}; 
		
		@Override
		public LIST<CLICKABLE> getAdditionalButt() {
			return butts;
		};
	};

	
	public final Job structure = new JobClear(
			D.g("Structure", "Dismantle Structure"),  
			D.g("StructureD", "Removes structures (fortifications, walls, roofs and roads)"),
			D.g("StructureV", "Demolishing structure"),
			SPRITES.icons().m.clear_structure) 
	{
		double demAmount;
		@Override
		public RESOURCE jobPerform(Humanoid skill, RESOURCE r, int ri) {
			if (problem(coo.x(), coo.y(), true) != null) {
				PlacerDelete.place(coo.x(), coo.y());
				return null;
			}
			
			RESOURCE res = null;
			if (FLOOR().getter.is(coo.x(), coo.y())) {
				res = FLOOR().getter.get(coo).resource;
				if (res != null) {
					demAmount += (1.0-FLOOR().degrade.get(coo))*FLOOR().getter.get(coo).resAmount*RND.rFloat();
				}
				FLOOR().clearer.clear(coo.x(), coo.y());
			}else {
				TerrainTile t = TERRAIN().get(coo.x(), coo.y());
				res = t.clearing().clear1(coo.x(), coo.y());
				demAmount += RND.rFloat();
			}
			
			if (res != null && demAmount > 1) {
				GAME.player().res().inc(res, RTYPE.CONSTRUCTION, (int)demAmount);
				demAmount -= 1;
				if (demAmount > 1) {
					THINGS().resources.create(coo, res, (int)demAmount);
					demAmount -= (int) demAmount;
				}
			}else {
				res = null;
			}

			if (problem(coo.x(), coo.y(), true) == null) {
				JOBS().state.set(State.RESERVABLE, this);
			}else {
				PlacerDelete.place(coo.x(), coo.y());
			}
			return res;
		}

		@Override
		protected CharSequence problem(int tx, int ty, boolean override) {
			if (super.problem(tx, ty, override) != null)
				return super.problem(tx, ty, override);
			if (TERRAIN().get(tx, ty).clearing().isStructure() && !TERRAIN().MOUNTAIN.isMountain(tx, ty))
				return null;
			if (SETT.FLOOR().getter.is(tx, ty))
				return null;
			if (TERRAIN().MOUNTAIN.isMountain(tx, ty))
				return PlacableMessages.¤¤BLOCKED;
			if (!TERRAIN().get(tx, ty).clearing().isStructure() && !SETT.FLOOR().getter.is(tx, ty))	
				return PlacableMessages.¤¤STRUCTURE_CLEAR;
			return null;
		};
		
		
	};
	
	public final PlacableMulti hunt = new PlacableMulti(¤¤hunt, ¤¤huntD, UI.icons().m.wildlife) {
		
		private final PlacableMulti undo = new PlacableMulti(¤¤huntCancel) {
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				for (ENTITY e : SETT.ENTITIES().getAtTile(tx, ty)) {
					if (e instanceof Animal) {
						Animal a = (Animal) e;
						a.huntMark(false);
					}
				}
			}
			
			@Override
			public void renderPlaceHolder(SPRITE_RENDERER r, int mask, int x, int y, int tx, int ty, AREA area, PLACER_TYPE type, boolean isPlacable, boolean areaIsPlacable) {
				SPRITES.cons().BIG.outline.render(r, mask, x, y);
				int dx = tx*C.TILE_SIZE;
				int dy = ty*C.TILE_SIZE;
				Job.CACTIVE.bind();
				for (ENTITY e : SETT.ENTITIES().getAtTile(tx, ty)) {
					if (e instanceof Animal) {
						Animal a = (Animal) e;
						if (a.huntMarkedIs()) {
							int ddx = a.body().cX()-dx;
							int ddy = a.body().cY()-dy;
							SPRITES.cons().ICO.crosshair.renderC(r, x+ddx, y+ddy);
						}
					}
				}
				COLOR.unbind();
			};
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				return null;
			}
		};
		
		@Override
		public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
			for (ENTITY e : SETT.ENTITIES().getAtTile(tx, ty)) {
				if (e instanceof Animal) {
					Animal a = (Animal) e;
					a.huntMark(true);
				}
			}
		}
		
		@Override
		public void renderPlaceHolder(SPRITE_RENDERER r, int mask, int x, int y, int tx, int ty, AREA area, PLACER_TYPE type, boolean isPlacable, boolean areaIsPlacable) {
			SPRITES.cons().BIG.outline.render(r, mask, x, y);
			int dx = tx*C.TILE_SIZE;
			int dy = ty*C.TILE_SIZE;
			Job.CACTIVE.bind();
			for (ENTITY e : SETT.ENTITIES().getAtTile(tx, ty)) {
				if (e instanceof Animal) {
					Animal a = (Animal) e;
					if (a.huntMarkedCan()) {
						int ddx = a.body().cX()-dx;
						int ddy = a.body().cY()-dy;
						SPRITES.cons().ICO.crosshair.renderC(r, x+ddx, y+ddy);
					}
					
				}
			}
			COLOR.unbind();
		};
		
		@Override
		public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
			return null;
		}
		
		@Override
		public PLACABLE getUndo() {
			return undo;
		};

	};
	
	public final PLACABLE[] placers = new PLACABLE[] {
		wood.placer(),
		stone.placer(),
		woodAndRock,
		water.placer(),
		returnwater.placer(),
		tunnel.placer(),
		caveFill.placer(),
		structure.placer(),
	};
	
	final ArrayList<CLICKABLE> butts = new ArrayList<>(placers.length);
	
	public PLACABLE lastActivated = woodAndRock;
	
	{
		for (PLACABLE p : placers) {
			CLICKABLE a = new GButt.ButtPanel(new SPRITE.Wrap(p.getIcon(), Icon.L, Icon.L)) {
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					b.title(p.name());
					p.hoverDesc(b);
				}
				
				@Override
				protected void clickA() {
					VIEW.inters().popup.close();
					VIEW.s().tools.place(p);
					lastActivated = p;
					super.clickA();
				}
				
				@Override
				protected void renAction() {
					selectedSet(VIEW.s().tools.placer.getCurrent() == p);
				}
				
			};
			butts.add(a);
		}
	}
	
	
}
