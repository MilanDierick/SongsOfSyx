package view.world;

import static world.WORLD.*;

import game.faction.FACTIONS;
import init.C;
import init.biomes.*;
import init.resources.Minable;
import init.settings.S;
import init.sprite.SPRITES;
import snake2d.MButt;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.COORDINATE;
import util.colors.GCOLORS_MAP;
import util.dic.DicMisc;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import view.main.VIEW;
import view.subview.GameWindow;
import view.tool.*;
import view.world.ui.WorldHoverer;
import world.WORLD;
import world.entity.WEntity;
import world.entity.army.WArmy;
import world.map.buildings.camp.WCampInstance;
import world.map.landmark.WorldLandmark;
import world.regions.Region;

final class ToolDefault extends Tool{
	
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
		if (e != null && (e.faction() == FACTIONS.player() || !WORLD.FOW().is(window.tile()))) {
			WORLD.OVERLAY().hoverEntity(e);
			WorldHoverer.hover(box, e);
		}else if(WORLD.camps().map.get(window.tile()) != null) {
			WCampInstance w = WORLD.camps().map.get(window.tile());
			WORLD.OVERLAY().things.hover(w.coo().x()*C.TILE_SIZE+C.TILE_SIZEH-C.TILE_SIZE, w.coo().y()*C.TILE_SIZE+C.TILE_SIZEH-C.TILE_SIZE, C.TILE_SIZE*2, C.TILE_SIZE*2, GCOLORS_MAP.get(w.regionFaction()), false);
			VIEW.world().UI.camps.hover(box, w);
			
		}else {
			
			Region reg = WORLD.REGIONS().map.centre.get(window.tile());
			if (reg != null) {
				WORLD.OVERLAY().hover(reg);
				VIEW.world().UI.regions.hover(reg, box);
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
		
		for (WEntity e : ENTITIES().fill(window.pixel().x(), window.pixel().y())) {
			if (e != null && (e.faction() == FACTIONS.player() || !WORLD.FOW().is(window.tile()))) {
				if (e instanceof WArmy) {
					WArmy a = (WArmy) e;
					if (a.faction() == FACTIONS.player() || S.get().developer) {
						VIEW.world().UI.armies.open(a);
						return;
					}
				}
			}
		}
		{
			
			Region reg = WORLD.REGIONS().map.centre.get(window.tile());
			if (reg != null) {
				VIEW.world().UI.regions.open(reg, true);
			}
			
		}
		
	}
	
	@Override
	protected ToolConfig defaultConfig() {
		return config;
	}
	
	private final static int tabs = 5;
	
	static void explore(GameWindow win) {
		if (!PIXELS().holdsPoint(win.pixel()))
			return;
		int tx = win.tile().x(); 
		int ty = win.tile().y();
		
		GBox b = VIEW.hoverBox();
		
		WORLD.OVERLAY().landmarks.add();
		
		{
			b.title(TERRAINS.world.get(tx, ty).name);
			b.add(SPRITES.icons().m.crossair);
			b.tab(1);
			b.add(b.text().add(tx));
			b.tab(2);
			b.add(b.text().add(ty));
			b.NL();
		}
		
		{
			b.textLL(DicMisc.¤¤Fertility);
			b.tab(tabs);
			b.add(GFORMAT.perc(b.text(), FERTILITY().map.get(tx, ty)));
			b.NL();
			
			b.textLL(DicMisc.¤¤Minerals);
			b.tab(tabs);
			Minable m = WORLD.MINERALS().get(tx, ty);
			if (m != null) {
				b.text(m.name);
			}
			b.NL();
			
			CLIMATE z = CLIMATE().getter.get(tx, ty);
			b.textLL(CLIMATES.INFO().name);
			b.tab(tabs);
			b.text(z.name);
			b.NL();
			
		}
		
		{
			
			WorldLandmark a = LANDMARKS().setter.get(tx, ty);
			if (a != null) {
				b.NL(8);
				WORLD.OVERLAY().landmarks.hover(a);
				b.textLL(a.name);
				b.NL();
				b.text(a.description);
				b.NL();
			}
		}
		
		if (S.get().developer) {
			b.add(b.text().add(WORLD.FOW().is(tx, ty)));
			b.NL();
		}
		
	}
	
}
