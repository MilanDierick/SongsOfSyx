package settlement.job;

import static settlement.main.SETT.*;

import game.GAME;
import init.D;
import init.sound.SoundSettlement.Sound;
import init.sprite.SPRITES;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.tilemap.TBuilding;
import settlement.tilemap.Terrain.TerrainTile;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.DIR;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import view.tool.*;

public class JobBuildStructure {

	public final TBuilding building;
	public final Job wall;
	public final Job ceiling;
	public final PlacableMulti combo;
	public final PlacableMulti convert;
	
	private static CharSequence ¤¤WallD = "¤Walls can be used to fence off areas.";
	private static CharSequence ¤¤CeilingD = "¤Ceilings can house rooms inside them, and protects subjects from the elements.";
	private static CharSequence ¤¤Structure = "¤{0} Room.";
	private static CharSequence ¤¤StructureD = "A combination tool that makes ceilings surrounded by walls of chosen material.";
	
	private static CharSequence ¤¤Convert = "Convert";
	private static CharSequence ¤¤ConvertD = "Convert existing structures into this type.";
	
	private static CharSequence ¤¤SameProblem = "Must be placed on a structure of a different type.";
	private static CharSequence ¤¤Blocked = "Area is blocked and can't be worked.";
	private static CharSequence ¤¤Rooms = "The ceiling of rooms must be changed by refurnishing the room.";
	
	static {
		D.ts(JobBuildStructure.class);
	}
	
	public JobBuildStructure(TBuilding building) {
		this.building = building;
		this.wall = new Wall();
		this.ceiling = new Roof();
		this.combo = new Combo();
		this.convert = new Convert();
	}
	
	static LIST<JobBuildStructure> make(){
		ArrayList<JobBuildStructure> all = new ArrayList<>(TERRAIN().BUILDINGS.all().size());
		for (TBuilding s : TERRAIN().BUILDINGS.all()) {
			all.add(new JobBuildStructure(s));
		}
		return all;
	}

	private final class Wall extends JobBuild {

		Wall() {
			super(building.resource, building.resAmount+1, true, building.nameWall,
					¤¤WallD, building.wall.getIcon());
		}

		@Override
		void renderAbove(SPRITE_RENDERER r, int x, int y, int mask, int tx, int ty) {
			for (DIR d : DIR.ORTHO) {
				Job j = JOBS().getter.get(tx, ty, d);
				if (j instanceof Wall || building.wall.is(tx, ty))
					mask |= d.mask();
			}
			SPRITES.cons().BIG.dashedThick.render(r, mask, x, y);
		}
		
		@Override
		protected CharSequence problem(int tx, int ty, boolean overwrite) {
			if (ROOMS().map.is(tx, ty))
				return PlacableMessages.¤¤ROOM_BLOCK;
			
			if (TERRAIN().MOUNTAIN.isMountain(tx, ty)) {
				return PlacableMessages.¤¤MOUNTAIN_NOT;
			}
			TerrainTile t = TERRAIN().get(tx, ty);
			if (t.clearing().needs() && !t.clearing().can())
				return PlacableMessages.¤¤MISC;
			if (JOBS().getter.get(tx, ty) == this)
				return PLACABLE.E;
			if (t == building.wall)
				return PLACABLE.E;
			
			return null;
		}

		@Override
		boolean terrainNeedsClear(int tx, int ty) {
			if (building.roof.is(tx, ty))
				return false;
			return super.terrainNeedsClear(tx, ty);
		}

		@Override
		boolean resNeeds(int tx, int ty) {
			if (building.roof.is(tx, ty))
				return JOBS().progress.get(tx + ty * TWIDTH) == 0;
			return super.resNeeds(tx, ty);
		}

		@Override
		protected double constructionTime(Humanoid skill) {
			return 14;
		}

		@Override
		protected Sound constructSound() {
			return building.sound;
		}

		@Override
		protected boolean construct(int tx, int ty) {
			if (building.resource != null)
				GAME.player().res().outConstruction.inc(building.resource,  building.resAmount+1);
			building.wall.placeFixed(tx, ty);
			return false;
		}
		
		@Override
		public boolean becomesSolid() {
			return true;
		}
		
		@Override
		public boolean isConstruction() {
			return true;
		}

		@Override
		public TerrainTile becomes(int tx, int ty) {
			return building.wall;
		}

	}

	private final class Roof extends JobBuild {

		Roof() {
			super(building.resource, building.resAmount, false, building.nameCeiling,
					¤¤CeilingD, building.roof.getIcon());
		}

		@Override
		void renderAbove(SPRITE_RENDERER r, int x, int y, int mask, int tx, int ty) {
			for (DIR d : DIR.ORTHO) {
				Job j = JOBS().getter.get(tx, ty, d);
				if (j instanceof Wall || j instanceof Roof || building.roof.is(tx, ty))
					mask |= d.mask();
			}
			SPRITES.cons().BIG.dashed.render(r, mask, x, y);

		}
		
		@Override
		protected CharSequence problem(int tx, int ty, boolean overwrite) {
			if (building.wall.is(tx, ty))
				return null;
			return super.problem(tx, ty, overwrite);
		}

		@Override
		protected double constructionTime(Humanoid skill) {
			return 14;
		}

		@Override
		protected Sound constructSound() {
			return building.sound;
		}

		@Override
		boolean terrainNeedsClear(int tx, int ty) {
			if (building.wall.is(tx, ty))
				return false;
			return super.terrainNeedsClear(tx, ty);
		}
		
		@Override
		boolean resNeeds(int tx, int ty) {
			if (building.wall.is(tx, ty))
				return false;
			return super.resNeeds(tx, ty);
		}

		@Override
		protected boolean construct(int tx, int ty) {
			if (building.resource != null)
				GAME.player().res().outConstruction.inc(building.resource,  building.resAmount);
			building.roof.placeFixed(tx, ty);
			return false;
		}
		
		@Override
		public boolean isConstruction() {
			return true;
		}

		@Override
		public TerrainTile becomes(int tx, int ty) {
			return building.roof;
		}

	}

	private final class Combo extends PlacableMulti {

		public Combo() {
			super(new Str(¤¤Structure).insert(0, building.name), ¤¤StructureD, building.iconCombo);
			// TODO Auto-generated constructor stub
		}

		@Override
		public CharSequence isPlacable(int tx, int ty, AREA a, PLACER_TYPE t) {
			if (isWall(tx, ty, a))
				return wall.placer().isPlacable(tx,  ty, a, t);
			return ceiling.placer().isPlacable(tx,  ty, a, t);
		}

		@Override
		public void place(int tx, int ty, AREA a, PLACER_TYPE t) {
			if (isWall(tx, ty, a))
				wall.placer().place(tx, ty, a, t);
			else
				ceiling.placer().place(tx, ty, a, t);
		}
		
		@Override
		public void renderPlaceHolder(SPRITE_RENDERER r, int mask, int x, int y, int tx, int ty, AREA a,
				PLACER_TYPE t, boolean isPlacable, boolean areaIsPlacable) {
			if (isWall(tx, ty, a))
				wall.renderAbove(r, x, y, mask, tx, ty);
			else
				ceiling.renderAbove(r, x, y, mask, tx, ty);
		}
		
		private boolean isWall(int tx, int ty, AREA a) {
			for (DIR d : DIR.ALL) {
				if (!a.is(tx, ty, d)) {
					int y1 = a.body().y1();
					int y2 = a.body().y2();
					int x1 = a.body().x1();
					int x2 = a.body().x2();
					if ((a.body().height() & 1) == 1) {
						y1 = a.body().cY();
						y2 = a.body().cY();
					}else {
						y1 = a.body().cY()-1;
						y2 = a.body().cY();
					}
					
					if ((a.body().width() & 1) == 1) {
						x1 = a.body().cX();
						x2 = a.body().cX();
					}else {
						x1 = a.body().cX()-1;
						x2 = a.body().cX();
					}
					
					return (tx < x1 || tx > x2) && (ty < y1 || ty > y2);
				}
			}
			return false;
		}
		
		@Override
		public boolean canBePlacedAs(PLACER_TYPE t) {
			return t != PLACER_TYPE.LINE;
		}
		
		@Override
		public PLACABLE getUndo() {
			return wall.placer().getUndo();
		}
	}
	
	private final class Convert extends PlacableMulti {

		public Convert() {
			super(¤¤Convert, ¤¤ConvertD, new SPRITE.Twin(building.wall.getIcon(), SPRITES.icons().m.arrow_right));
		}

		@Override
		public CharSequence isPlacable(int tx, int ty, AREA a, PLACER_TYPE t) {
			if (!PATH().reachability.is(tx, ty))
				return ¤¤Blocked;
			TerrainTile te = SETT.TERRAIN().get(tx, ty);
			if (te == null || !(te instanceof TBuilding.BuildingComponent))
				return ¤¤Blocked;
			if (building.isser.is(tx, ty))
				return ¤¤SameProblem;
			if (SETT.ROOMS().map.is(tx, ty))
				return ¤¤Rooms;
			return null;
		}

		@Override
		public void place(int tx, int ty, AREA a, PLACER_TYPE t) {
			TerrainTile te = SETT.TERRAIN().get(tx, ty);
			if (te instanceof TBuilding.Wall)
				wall.placer().place(tx, ty, a, t);
			else if (te instanceof TBuilding.Ceiling || te instanceof TBuilding.Ceiling.Opening)
				ceiling.placer().place(tx, ty, a, t);
		}
		
		@Override
		public PLACABLE getUndo() {
			return JOBS().tool_clear;
		}
	}
	


}
