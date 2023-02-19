package view.world;

import static world.World.*;

import game.faction.FACTIONS;
import init.C;
import init.biomes.CLIMATE;
import init.biomes.CLIMATES;
import init.settings.S;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.MButt;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.COORDINATE;
import util.colors.GCOLORS_MAP;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import view.main.VIEW;
import view.subview.GameWindow;
import view.tool.*;
import view.world.ui.WorldHoverer;
import world.World;
import world.entity.WEntity;
import world.entity.army.WArmy;
import world.map.buildings.camp.WCampInstance;
import world.map.landmark.WorldLandmark;
import world.map.regions.REGIOND;
import world.map.regions.Region;

public class ToolDefault extends Tool{
	
	private boolean exploring = false;
	
	private final ToolConfig config = new ToolConfig() {};
	
	public ToolDefault(ToolManager manager) {
		super(manager);
	}
	
	@Override
	protected void updateHovered(float ds, GameWindow window) {
		
		exploring &= MButt.RIGHT.isDown();
		
		update(ds, window);
		if (exploring) {
			explore(window);
		}else {
			hover(window.pixel(), window);
		}
		
		
	}
	
	private void hover(COORDINATE coo, GameWindow window) {
		if (!PIXELS().holdsPoint(coo))
			return;
		WEntity e = ENTITIES().getTallest(coo);
		GBox box = VIEW.hoverBox();
		if (e != null) {
			World.OVERLAY().hover(e);
			WorldHoverer.hover(box, e);
		}else if(World.camps().map.get(window.tile()) != null) {
			WCampInstance w = World.camps().map.get(window.tile());
			World.OVERLAY().hover(w.coo().x()*C.TILE_SIZE+C.TILE_SIZEH-C.TILE_SIZE, w.coo().y()*C.TILE_SIZE+C.TILE_SIZEH-C.TILE_SIZE, C.TILE_SIZE*2, C.TILE_SIZE*2, GCOLORS_MAP.get(w.faction()), false);
			w.hoverInfo(box);
			
		}else {
			Region reg = World.REGIONS().getter.get(window.tile());
			if (reg != null && !reg.isWater()) {
				World.OVERLAY().hoverRegion(reg);
				if ( MButt.RIGHT.isDown() || ( Math.abs(window.tile().x()-reg.cx()) <= 1 && Math.abs(window.tile().x()-reg.cx()) <= 1))
					WorldHoverer.hover(box, reg);
			}
			
		}
	}
	
	@Override
	protected void update(float ds, GameWindow window) {
		
	}


	
	@Override
	protected void renderHovered(SPRITE_RENDERER r, float ds, GameWindow window, GBox box) {
		if (exploring) {
			SPRITES.cons().BIG.dashed.render(r, 0, window.tile().rel().x(), window.tile().rel().y());
			VIEW.mouse().setReplacement(SPRITES.icons().m.questionmark);
		}else {
			
			
		}
	}

	@Override
	protected boolean rightClick() {
		exploring = true;
		return false;
	}
	
	@Override
	protected void click(GameWindow window) {
		if (!PIXELS().holdsPoint(window.pixel()))
			return;
		
		WEntity e = ENTITIES().getTallest(window.pixel());
		if (e != null) {
			if (e instanceof WArmy) {
				WArmy a = (WArmy) e;
				if (a.faction() == FACTIONS.player() || S.get().developer)
					VIEW.world().UI.armies.open(a);
			}
		}else {
			Region reg = World.REGIONS().getter.get(window.tile());
			if (reg != null && !reg.isWater() && reg.faction() == FACTIONS.player()) {
				VIEW.world().UI.region.open(reg);
			}else if (reg != null && REGIOND.isCapitol(reg)) {
				VIEW.world().UI.faction.open(reg.faction());
			}
			
		}
		
	}
	
	@Override
	protected ToolConfig defaultConfig() {
		return config;
	}
	
	private static final CharSequence sFertility = "Fertility" + ":";
	private static final CharSequence sBonus = "+ WIP bonus.";
	private final static int tabs = 3;
	
	static void explore(GameWindow win) {
		if (!PIXELS().holdsPoint(win.pixel()))
			return;
		int tx = win.tile().x(); 
		int ty = win.tile().y();
		
		GBox b = VIEW.hoverBox();
		
		int inc = 1;
		
		Region reg = World.REGIONS().getter.get(tx, ty);
		if (reg != null && !reg.isWater())
			WorldHoverer.hover(b, World.REGIONS().getter.get(tx, ty));
		b.NL();
		
		{
			WorldLandmark a = LANDMARKS().setter.get(tx, ty);
			if (a != null) {
				b.add(b.text().lablify().add(a.name.toCamel()).add(a.index()));
				b.NL();
				b.add(b.text().normalify().add(a.description));
				
				for (int i = 0; i < 2; i++) {
					b.NL();
					b.tab(inc);
					b.add(b.text().normalify2().set(sBonus));
				}
				b.NL();
			}
			
			World.OVERLAY().hoverLandmark(World.LANDMARKS().setter.get(tx, ty));
			
			//AREAS().render(tx, ty);
		}
		
		{
			b.add(SPRITES.icons().m.crossair).add(b.text().add(tx).add(',').add(ty));
			b.NL();
		}
		
		{
			b.add(b.text().lablify().setFont(UI.FONT().H2).set(sFertility));
			b.tab(tabs);
			b.add(GFORMAT.perc(b.text(), FERTILITY().map.get(tx, ty)));
			b.NL();
		}
		
		{

		}
		
		{
			CLIMATE z = CLIMATE().getter.get(tx, ty);
			b.add(b.text().lablify().setFont(UI.FONT().H2).set(CLIMATES.INFO().name).add(':'));
			b.tab(tabs);
			b.add(b.text().lablifySub().setFont(UI.FONT().M).set(z.name));
			b.NL();
			b.text(z.desc);
			
			b.NL();
		}
		
		
		
		
//		
//		VIEW.world().inters.section.activate(tileInfo);
		
	}
	
}
