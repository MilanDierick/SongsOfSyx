package settlement.job;

import static settlement.main.SETT.*;

import game.GAME;
import game.faction.FACTIONS;
import init.D;
import init.sound.SOUND;
import init.sound.SoundSettlement.Sound;
import init.sprite.SPRITES;
import settlement.entity.humanoid.Humanoid;
import settlement.environment.SettEnvMap.SettEnv;
import settlement.main.RenderData.RenderIterator;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.tilemap.Floors.Floor;
import settlement.tilemap.Terrain.TerrainTile;
import snake2d.Renderer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.sets.*;
import util.dic.DicMisc;
import util.gui.misc.GBox;
import util.gui.misc.GButt.Panel;
import util.info.GFORMAT;
import util.rendering.ShadowBatch;
import view.tool.*;

public final class JobBuildRoad extends JobBuild{

	private final Floor floor;
	private final Placer placer;
	private boolean showRoads = true;
	private static CharSequence ¤¤Convert = "Convert";
	private static CharSequence ¤¤ConvertD = "Convert existing roads into this type.";
	private static CharSequence ¤¤roadvalue = "Road Access";
	private static CharSequence ¤¤durability = "Durability";
	static {
		D.ts(JobBuildRoad.class);
	}
	
	private static boolean convert = false;
	
	static LIST<JobBuildRoad> make(){
		ArrayList<JobBuildRoad> all = new ArrayList<>(SETT.FLOOR().roads.size());
		for (Floor f : SETT.FLOOR().roads) {
			all.add(new JobBuildRoad(f));
		}
		return all;
		
	}
	
	private JobBuildRoad(Floor floor) {
		super(
				floor.resource, 
				floor.resAmount, 
				false, 
				floor.name, 
				floor.desc, 
				floor.getIcon());
		this.floor = floor;

		
		LinkedList<CLICKABLE> bs = new LinkedList<CLICKABLE>();
		bs.add(new Panel(SPRITES.icons().m.cog) {
			@Override
			protected void clickA() {
				showRoads = !showRoads;
			}
			
			@Override
			protected void renAction() {
				selectedSet(showRoads);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.title(SETT.OVERLAY().ROADING.name);
				text.text(SETT.OVERLAY().ROADING.desc);
			}
		});
		
		bs.add(new Panel(SPRITES.icons().m.arrow_right) {
			@Override
			protected void clickA() {
				convert = !convert;
			}
			
			@Override
			protected void renAction() {
				selectedSet(convert);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				
				text.title(¤¤Convert);
				text.text(¤¤ConvertD);
			}
		});
		

		
		placer = new Placer(this, floor.resource, floor.resAmount, floor.desc) {
			@Override
			public void hoverDesc(GBox box) {
				super.hoverDesc(box);
				box.NL(4);
				
				box.textL(¤¤roadvalue);
				box.tab(5);
				box.add(GFORMAT.perc(box.text(), floor.walkValue));
				box.NL();
				
				box.textL(DicMisc.¤¤Speed);
				box.tab(5);
				box.add(GFORMAT.percInc(box.text(), (floor.speed.movementSpeed-AVAILABILITY.NORMAL.movementSpeed)));
				box.NL();
				
				for (SettEnv e : SETT.ENV().environment.all()) {
					if (floor.envValue(e) != 0) {
						box.textL(e.name);
						box.tab(5);
						box.add(GFORMAT.perc(box.text(), floor.envValue(e)));
						box.NL();
					}
				}
				box.NL();
				box.textL(¤¤durability);
				box.tab(5);
				box.add(GFORMAT.perc(box.text(), floor.durability));
				
			}
			
			@Override
			public LIST<CLICKABLE> getAdditionalButt() {
				return bs;
			}
		};
	}
	
	@Override
	public PlacableMulti placer() {
		return placer;
	}

	@Override
	void renderAbove(SPRITE_RENDERER r, int x, int y, int mask, int tx, int ty) {
		for (DIR d: DIR.ORTHO) {
			if (FLOOR().getter.is(tx, ty, d) || JOBS().getter.get(tx, ty, d) == this)
				mask |= d.mask();
		}
		SPRITES.cons().BIG.dashed.render(r, mask, x, y);
		
	}
	
	@Override
	protected CharSequence problem(int tx, int ty, boolean overwrite) {
		
		if (convert) {
			if (SETT.FLOOR().getter.get(tx, ty) == floor)
				return PLACABLE.E;
			if (SETT.FLOOR().getter.get(tx, ty) == null)
				return PLACABLE.E;
		}
		
		if (ROOMS().map.is(tx, ty))
			return PlacableMessages.¤¤ROOM_BLOCK;
		
		if (SETT.PATH().solidity.is(tx, ty))
			return PlacableMessages.¤¤BLOCKED;
		
		if (!SETT.TERRAIN().get(tx, ty).roofIs() && !SETT.TERRAIN().get(tx, ty).clearing().can())
			return PlacableMessages.¤¤BLOCKED;
		
		if (SETT.FLOOR().getter.get(tx, ty) == floor){
			return PlacableMessages.¤¤ROAD_ALREADY;
		}
		
		return lockText();
		
	}
	
	@Override
	public CharSequence lockText() {
		return FACTIONS.player().locks.unlockText(floor);
	}
	
	
	@Override
	boolean terrainNeedsClear(int tx, int ty) {
		if (TERRAIN().get(tx, ty).roofIs())
			return false;
		return super.terrainNeedsClear(tx, ty);
	}
	
	@Override
	protected double constructionTime(Humanoid skill) {
		return 25;
	}
	
	@Override
	protected Sound constructSound() {
		return SOUND.sett().action.dig;
	}
	
	@Override
	protected void renderBelow(Renderer r, ShadowBatch shadowBatch, RenderIterator i, int state) {
		if (!FLOOR().getter.is(i.tile()))
			super.renderBelow(r, shadowBatch, i, state);
	}
	
	@Override
	public void doSomethingExtraRender() {
		if (showRoads)
			SETT.OVERLAY().ROADING.add();
	}
	
	@Override
	protected boolean construct(int tx, int ty) {
		if (FLOOR().getter.get(tx, ty) != floor) {
			if (floor.resource != null)
				GAME.player().res().outConstruction.inc(floor.resource,  floor.resAmount);
			floor.placeFixed(tx, ty);
			FLOOR().degrade.set(tx, ty, 0);
		}else
			FLOOR().degrade.set(tx, ty, FLOOR().degrade.get(tx, ty)-0.25);
		return FLOOR().degrade.get(tx, ty) != 0;
	}
	
	@Override
	public boolean isConstruction() {
		return true;
	}
	
	@Override
	public TerrainTile becomes(int tx, int ty) {
		return TERRAIN().NADA;
	}

}
