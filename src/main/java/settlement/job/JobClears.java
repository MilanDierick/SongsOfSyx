package settlement.job;

import static settlement.main.SETT.*;

import game.GAME;
import init.D;
import init.RES;
import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCE;
import init.sound.SoundSettlement.Sound;
import init.sprite.SPRITES;
import settlement.entity.humanoid.Humanoid;
import settlement.job.StateManager.State;
import settlement.main.SETT;
import settlement.room.industry.mine.ROOM_MINE;
import settlement.tilemap.TGrowable;
import settlement.tilemap.Terrain.TerrainTile;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.*;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import util.dic.DicMisc;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.info.GFORMAT;
import view.tool.*;

public final class JobClears {


	JobClears() {
		
	}
	
	{
		D.gInit(this);
	}
	
	private CharSequence harvestDesc = D.g("HarvestDesc", "This job is dormant since there currently isn't enough growth to harvest anything. The job will become active once growth occurs.");
	
	public final Job stone = new JobClear(
			D.g("Stone", "Clear Rock"),  
			D.g("StoneD", "Removes rocks on the map. Yields the resource stone"),
			D.g("StoneV", "Clearing rocks"),
			SPRITES.icons().m.clearstone) 
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
	};
	
	public final Job wood = new JobClear(
			D.g("Tree", "Fell Tree"),  
			D.g("TreeD", "Removes trees and yields wood. Trees will slowly grow back in time."),
			D.g("TreeV", "Chopping lumber"),
			SPRITES.icons().m.axe) 
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
				box.error(harvestDesc);
			}
		}
		
		
	}
	
	public final Job food = new JobClear(
			D.g("Food", "Harvest wild edibles"),  
			D.g("FoodD", "Placed on edible vegetation. These grow with each year."),
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
				return null;
			}
			
			return PlacableMessages.¤¤NOT_EDIBLE;
		}
		
		@Override
		public void doSomethingExtraRender() {
			SETT.OVERLAY().EDIBLES.add();
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
				GAME.player().res().inProduced.inc(g.growable.resource, 1);
				return g.growable.resource;
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
			
			{
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
					if (gi > -1 && SETT.JOBS().getter.get(tx, ty) != null) {
						TerrainTile t = TERRAIN().get(tx, ty);
						if (t instanceof TGrowable) {
							
							TGrowable b = (TGrowable) t;
							return SETT.JOBS().getter.get(tx, ty) == food && TERRAIN().GROWABLES.get(gi) == b ? null : E;	
						}
					}
					return E;
				}
				
				@Override
				public LIST<CLICKABLE> getAdditionalButt() {
					return bb;
				};
			};
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				op.place(tx, ty, area, type);
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				
				CharSequence s = op.isPlacable(tx, ty, area, type);
				
				if (gi == -1 || s != null)
					return s;
				TerrainTile t = TERRAIN().get(tx, ty);
				if (t instanceof TGrowable) {
					TGrowable b = (TGrowable) t;
					return TERRAIN().GROWABLES.get(gi) == b ? null : E;
				}
				return E;
				
			}
			
			@Override
			public CharSequence isPlacable(AREA area, PLACER_TYPE type) {
				SETT.OVERLAY().EDIBLES.add();
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
			D.g("WaterD", "Removes shallow water..."),
			D.g("WaterV", "Removing water"),
			SPRITES.icons().m.fillWater) 
	{
		@Override
		protected CharSequence problem(int tx, int ty, boolean overwrite) {
			if (super.problem(tx, ty, overwrite) != null)
				return super.problem(tx, ty, overwrite);
			if (!TERRAIN().WATER.isWater(tx, ty)) {
				return PlacableMessages.¤¤WATER_MUST;
			}
			return null;
		}
		
		@Override
		public RESOURCE jobPerform(Humanoid skill, RESOURCE r, int ri) {
			int tx = jobCoo().x();
			int ty = jobCoo().y();
			r = super.jobPerform(skill, r, ri);
			if (!JOBS().getter.has(tx, ty)) {
				JOBS().waterTable++;
			}
			return r;
		};
	};
	
	public final Job returnwater = new JobBuild(null, 1, false, 
			D.g("Canal", "Dig Canal"),  
			D.g("CanalD", "Dig a canal. Can only be placed if you have water-table for it."),
			SPRITES.icons().m.digCanal) {
		
		private final CharSequence waterTable = D.g("Water-table", "Water-table: ");
		
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
			TERRAIN().WATER.placeFixed(coo.x(), coo.y());
			return false;
		}
		
		@Override
		void init(int tx, int ty) {
			JOBS().waterTable --;
			super.init(tx, ty);
		};
		
		@Override
		protected CharSequence problem(int tx, int ty, boolean overwrite) {
			if (super.problem(tx, ty, overwrite) != null)
				return super.problem(tx, ty, overwrite);
			if (JOBS().waterTable <= 0)
				return PlacableMessages.¤¤WATER_RETURN;
			if (TERRAIN().WATER.isWater(tx, ty))
				return PlacableMessages.¤¤TERRAIN_BLOCK;
			return null;
		}
		
		@Override
		void cancel(int tx, int ty) {
			JOBS().waterTable ++;
		};
		
		private final Placer p = new Placer(this, placer.desc) {
			
			@Override
			public void placeInfo(GBox b, int okTiles, AREA a) {
				super.placeInfo(b, okTiles, a);
				
				b.NL(2);
				b.text(b.text().add(waterTable).add(':').add(' ').add(JOBS().waterTable));
			}
		};
		
		@Override
		public PlacableMulti placer() {
			return p;
		}

		@Override
		public TerrainTile becomes(int tx, int ty) {
			return TERRAIN().WATER;
		};
	};
	
	private int raceSpeeds[];
	
	void initSpeeds() {
		raceSpeeds = new int[RACES.all().size()];
		for (Race r : RACES.all()) {
			double min = 0;
			for (ROOM_MINE m : SETT.ROOMS().MINES) {
				min = Math.max(min, m.industries().get(0).bonus().race(r));
			}
			
			raceSpeeds[r.index()] = (int) (60 / (1+min));
			
		}
	}

	
	public final Job tunnel = new JobClear(
			D.g("Tunnel", "Dig Into Mountain"),  
			D.g("TunnelD", "Digs a tunnel into the mountain"),
			D.g("TunnelV", "Tunnels into the mountain"),
			SPRITES.icons().m.clear_tunnel) 
	{

		double tunnelD = 0;
		
		@Override
		public RESOURCE jobPerform(Humanoid skill, RESOURCE r, int ri) {
			tunnelD += 1;
			
			while(tunnelD >= 1) {
				tunnelD --;
				RESOURCE res = tunnelPerform(coo);
				
				if (!TERRAIN().MOUNTAIN.is(coo)) {
					PlacerDelete.place(coo.x(), coo.y());

					return res;
					
				}
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
		public boolean isConstruction() {
			return true;
		}
		
		@Override
		public double jobPerformTime(Humanoid skill) {
			return raceSpeeds[skill.race().index];
		};
		
		
	};
	
	public RESOURCE tunnelPerform(COORDINATE coo) {
		RESOURCE res = TERRAIN().get(coo.x(), coo.y()).clearing().resource();
		TERRAIN().get(coo.x(), coo.y()).clearing().clear1(coo.x(), coo.y());
		if (!TERRAIN().MOUNTAIN.is(coo)) {
			for (DIR d : DIR.ALLC) {
				if (TERRAIN().CAVE.canFix(coo.x()+d.x(), coo.y()+d.y())) {
					TERRAIN().CAVE.fix(coo.x()+d.x(), coo.y()+d.y());
				}
				GAME.stats().TUNNELS.inc(1);
			}
			if (res != null && RND.oneIn(15)) {
				GAME.player().res().inProduced.inc(res, 1);
				return res;
			}
			return null;
			
		}else if (RND.oneIn(15)) {
			GAME.player().res().inProduced.inc(res, 1);
			return res;
		}
		return null;
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
				res = t.clearing().resource();
				t.clearing().clear1(coo.x(), coo.y());
				demAmount += RND.rFloat();
			}
			
			if (res != null && demAmount > 1) {
				GAME.player().res().inDemolition.inc(res, (int)demAmount);
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
	
	public final PLACABLE[] placers = new PLACABLE[] {
		wood.placer(),
		stone.placer(),
		woodAndRock,
		food.placer(),
		water.placer(),
		returnwater.placer(),
		tunnel.placer(),
		caveFill.placer(),
		structure.placer(),
	};
	
	
}
