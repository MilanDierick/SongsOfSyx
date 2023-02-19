package settlement.entity.animal;

import static settlement.main.SETT.*;

import java.io.IOException;

import init.C;
import init.paths.PATHS;
import init.resources.RESOURCE;
import settlement.entity.ENTITY;
import settlement.entity.animal.spawning.AnimalSpawning;
import settlement.main.CapitolArea;
import settlement.main.SETT;
import settlement.main.SETT.SettResource;
import settlement.room.food.pasture.ROOM_PASTURE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.TILE_SHEET;
import util.keymap.RCollection;
import util.rendering.ShadowBatch;
import util.spritecomposer.*;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerThings.ITileSheetL;
import view.sett.IDebugPanelSett;
import view.tool.*;

public final class ANIMALS extends SettResource{

	public final RCollection<AnimalSpecies> species = AnimalSpecies.create();
	public final AnimalSpawning spawn = new AnimalSpawning();
	
	private final ArrayList<AnimalSpecies> caravans  = new ArrayList<>(species.all().size());
	private LIST<AnimalSpecies> sett;
	final TILE_SHEET texture_blood;
	final LIST<TILE_SHEET> texture_water;
	final TILE_SHEET crate;
	
	
	public ANIMALS() throws IOException{
		
		PLACABLE death = new PlacableSimple("kill animals") {
			
			@Override
			public void place(int x, int y) {
				for (ENTITY e : ENTITIES().getAtPointL(x, y)) {
					if (e instanceof Animal) {
						((Animal) e).kill(false, false);
						return;
					}
				}
			}
			
			@Override
			public CharSequence isPlacable(int x, int y) {
				for (ENTITY e : ENTITIES().getAtPointL(x, y)) {
					if (e instanceof Animal) {
						return null;
					}
				}
				return E;
			}
		};
		
		IDebugPanelSett.add(death);
		
		texture_blood = (new ITileSheet(PATHS.SPRITE().getFolder("animal").get("_Texture"), 264,156) {

			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources ss, ComposerDests d) {
				final ComposerSources.Singles s = ss.singles;
				
				ComposerDests.Tile t = d.s24;
				s.init(0, 0, 3, 1, 2, 10, t);
				s.setVar(0);
				for (int i = 0; i < 5; i++) {
					s.setSkip(i * 2, 2).paste(3, true);
				}
				return t.saveGame();

			}
		}).get();

		texture_water = (new ITileSheetL() {

			@Override
			protected int init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.singles.setVar(1);
				return 4;
			}

			@Override
			protected TILE_SHEET next(int i, ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.singles.setSkip(i * 2, 2).paste(3, true);
				return d.s24.saveGame();
			}
		}).get();
		
		crate = (new ITileSheet() {

			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources ss, ComposerDests d) {
				
				final ComposerSources.Singles s = ss.singles;
				s.setSkip(8, 2).paste(3, true);
				return d.s24.saveGame();

			}
		}).get();
		
		for (AnimalSpecies s : species.all()) {
			PlacableSimple p = new PlacableSimple(s.name) {
				
				@Override
				public void place(int x, int y) {
					new Animal(x, y, s, null);
					
				}
				
				@Override
				public CharSequence isPlacable(int x, int y) {
					return ANIMALS.this.isPlacable(s, x, y) ? null : E;
				}
			};
			IDebugPanelSett.add("animal", p);
			if (s.caravanable)
				caravans.add(s);
		}
		PlacableSimpleTile p = new PlacableSimpleTile("control animal") {
			
			@Override
			public void place(int tx, int ty) {
				for (ENTITY e : SETT.ENTITIES().getAtTile(tx, ty)) {
					if (e instanceof Animal) {
						((Animal) e).setState(State.CONTROLLED, 1);
						e.physics.setMass(500);
					}
				}
				
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty) {
				for (ENTITY e : SETT.ENTITIES().getAtTile(tx, ty)) {
					if (e instanceof Animal)
						return null;
				}
				return E;
			}
		};
		
		IDebugPanelSett.add("animal", p);
	}
	
	public LIST<AnimalSpecies> caravans(){
		return caravans;
	}

	@Override
	protected void save(FilePutter saveFile) {
		spawn.saver.save(saveFile);
		
	}
	
	@Override
	protected void update(float ds) {
		spawn.update(ds);
	}

	@Override
	protected void load(FileGetter saveFile) throws IOException {
		sett = exists(SETT.WORLD_AREA());
		spawn.saver.load(saveFile);
	}
	
	@Override
	protected void generate(CapitolArea area) {
		sett = exists(area);
		//this.max = new AnimalGenerator(area, this).generate();
		spawn.saver.clear();
		spawn.generate(this, area);
	}
	
	public LIST<AnimalSpecies> sett(){
		return sett;
	}
	
	private LIST<AnimalSpecies> exists(CapitolArea area) {

		ArrayList<AnimalSpecies> res = new ArrayList<>(SETT.ANIMALS().species.all().size());
		for (AnimalSpecies s : SETT.ANIMALS().species.all()) {
			if (exists(s, area))
				res.add(s);
		}
		return res;
	}
	
	private boolean exists(AnimalSpecies s, CapitolArea area) {
		for (ROOM_PASTURE p : SETT.ROOMS().PASTURES) {
			if (p.species == s && !p.isAvailable(area.climate()))
				return false;
		}
		return true;
	}
	
	public void renderCaravan(SPRITE_RENDERER r, ShadowBatch s, double movement, int cx, int cy, RESOURCE res, int resAmount, boolean inWater, int dir, int ran) {
		Sprite.renderCaravan(r, s, movement, cx, cy, res, resAmount, inWater, dir, ran);
	}
	
//	public Animal place(AnimalSpecies s, int x, int y) {
//		if (isPlacable(s, x, y)){
//			return new Animal(x,y,s);
//		}
//		return null;
//	}
	
	public boolean isPlacable(AnimalSpecies s, int x, int y) {
		int x1 = (x-s.hitboxSize/2);
		int x2 = (x+s.hitboxSize/2);
		int y1 = (y-s.hitboxSize/2);
		int y2 = (y+s.hitboxSize/2);
		if (x1 < 0 || x2 >= PWIDTH || y1 < 0 || y2 >= PHEIGHT)
			return false;
		x1 /= C.TILE_SIZE;
		x2 /= C.TILE_SIZE;
		y1 /= C.TILE_SIZE;
		y2 /= C.TILE_SIZE;
		return !PATH().solidity.is(x1, y1) && !PATH().solidity.is(x2, y1) &&
				!PATH().solidity.is(x1, y2) && !PATH().solidity.is(x2, y2) &&
				ENTITIES().getAtPoint(x,y) == null;
	}
	
}
