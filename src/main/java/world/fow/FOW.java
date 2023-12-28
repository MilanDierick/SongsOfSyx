package world.fow;

import java.io.IOException;

import game.Profiler;
import game.faction.FACTIONS;
import game.faction.Faction;
import init.C;
import init.RES;
import settlement.main.SETT;
import snake2d.CORE;
import snake2d.PathTile;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.misc.IntChecker;
import snake2d.util.sets.Bitmap2D;
import util.data.BOOLEAN.BOOLEANImp;
import util.rendering.RenderData.RenderIterator;
import view.world.panel.IDebugPanelWorld;
import world.WORLD;
import world.WORLD.WorldResource;
import world.WRenContext;
import world.regions.Region;
import world.regions.WREGIONS;
import world.regions.data.RD;

public final class FOW extends WorldResource implements MAP_BOOLEAN{

	private final Bitmap2D tmp = new Bitmap2D(WORLD.TBOUNDS(), false);
	private final Bitmap2D visible = new Bitmap2D(WORLD.TBOUNDS(), false);

	private final IntChecker regCheck = new IntChecker(WREGIONS.MAX);
	public BOOLEANImp toggled = new BOOLEANImp(true);
	private boolean dirty = true;
	
	public FOW(){
		new RD.RDOwnerChanger() {
			
			@Override
			public void change(Region reg, Faction oldOwner, Faction newOwner) {
				if (oldOwner == FACTIONS.player() || newOwner == FACTIONS.player()) {
					dirty = true;
				}
			}
		};
		
		IDebugPanelWorld.add("toggle fow", toggled);
	}
	
	public void setDirty() {
		dirty = true;
	}
	
	void update() {
		
		
		visible.clear();
		
//		for (Region reg : WORLD.REGIONS().active()) {
//			for (DIR d : DIR.ALLC) {
//				visible.set(reg.cx(), reg.cy(), d, true);
//			}
//		}
		RES.flooder().init(this);
		for (int ri = 0; ri < FACTIONS.player().realm().regions(); ri++) {
			Region reg = FACTIONS.player().realm().region(ri);
			RES.flooder().pushSloppy(reg.cx(), reg.cy(), 0);
		}
		
		
		regCheck.init();
		while(RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			Region reg = WORLD.REGIONS().map.get(t);
			visible.set(t, true);
			if (reg != null && !regCheck.isSetAndSet(reg.index())) {
				visit(reg);
			}
			
			if (reg != null && reg.faction() != FACTIONS.player())
				continue;
			
			int dm = WORLD.PATH().dirMap().get(t);
			for (DIR d : DIR.ALL) {
				visible.set(t, d, true);
				if ((d.bit & dm) != 0) {
					
					RES.flooder().pushSmaller(t, d, t.getValue()+d.tileDistance());
				}
			}
			
		}
		RES.flooder().done();
		
	}
	
	private void visit(Region reg) {
		for (COORDINATE c : reg.info.bounds()) {
			if (reg.is(c))
				visible.set(c, true);
		}
	}
	
	public void render(WRenContext data) {
		
		if (!toggled.b)
			return;
		CORE.renderer().shadowDepthSet((byte)255);
		WORLD.CENTRE().sprite.renderAboveTerrain(data);
		RenderIterator it = data.data.onScreenTiles(0,0,0,0);
		
		while(it.has()) {
			render(data, it);
			it.next();
		}
		
		
	}
	
	public void render(WRenContext con, RenderIterator it) {
		
		if (!is(it.tile()))
			return;
		if (WORLD.REGIONS().centreTile().is(it.tile()) && is(it.tile())) {
			CORE.renderer().shadowDepthSet((byte)127);
		}else {
			CORE.renderer().shadowDepthSet((byte)255);
		}
		CORE.renderer().renderShadow(it.x(), it.x()+C.TILE_SIZE, it.y(), it.y()+C.TILE_SIZE, COLOR.WHITE100.texture(), (byte)0);
		tmp.set(it.tile(), false);
	}
	
	public void enlighten(int tx, int ty, int radius) {
		if (!toggled.b)
			return;
		for (int i = 0; RES.circle().radius(i) <= radius; i++) {
			tmp.set(tx+RES.circle().get(i).x(), ty+RES.circle().get(i).y(), true);
		}
		
	}

	@Override
	public boolean is(int tile) {
		return toggled.b && !(visible.is(tile) || tmp.is(tile));
	}

	@Override
	public boolean is(int tx, int ty) {
		return toggled.b && !(visible.is(tx, ty) || tmp.is(tx, ty));
	}

	@Override
	protected void update(float ds, Profiler prof) {
		prof.logStart(this);
		if (FACTIONS.player().capitolRegion() == null || !SETT.exists())
			return;
		if (dirty)
			update();
		dirty = false;
		super.update(ds, prof);
		prof.logEnd(this);
	}
	
	@Override
	protected void save(FilePutter file) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		dirty = true;
	}
	
	@Override
	protected void initBeforePlay() {
		dirty = true;
	}
	
}
