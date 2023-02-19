package view.world;


import game.GAME;
import game.faction.FACTIONS;
import init.D;
import init.settings.S;
import init.sprite.SPRITES;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import util.dic.DicArmy;
import util.dic.DicGeo;
import util.gui.misc.*;
import util.info.GFORMAT;
import view.main.VIEW;
import view.ui.UIPanelTop;
import view.world.ui.UICaravanList;
import world.World;

public class WorldViewPanel{
	
	public WorldViewPanel(UIPanelTop top){
	
		D.gInit(this);
		CLICKABLE b;
		
		
		
		GuiSection butts = new GuiSection();
		
		b = new GButt.BStat2(SPRITES.icons().s.admin, new GStat() {

			@Override
			public void update(GText text) {
				GFORMAT.iIncr(text, GAME.player().admin().available());
				
			}
		}.decrease()) {
			@Override
			protected void clickA() {
				if (VIEW.world().UI.region.listIsOpen(VIEW.world().panels))
					VIEW.world().UI.region.close(VIEW.world().panels);
				VIEW.world().UI.region.open(null, VIEW.world().panels);
			};
			@Override
			protected void renAction() {
				selectedSet(VIEW.world().UI.region.listIsOpen(VIEW.world().panels));
			}
		}.hoverTitleSet(DicGeo.¤¤Regions).hoverInfoSet(DicGeo.¤¤RegionDesc);
		butts.addRightC(0, b);
		
		b = new GButt.BStat2(SPRITES.icons().s.sword, new GStat() {

			@Override
			public void update(GText text) {
				GFORMAT.i(text, GAME.player().kingdom().armies().all().size());
				
			}
		}.decrease()) {
			@Override
			protected void clickA() {
				if (VIEW.world().UI.armies.listIsOpen(VIEW.world().panels))
					VIEW.world().UI.armies.close(VIEW.world().panels);
				VIEW.world().UI.armies.openList(null, VIEW.world().panels);
			};
			@Override
			protected void renAction() {
				selectedSet(VIEW.world().UI.armies.listIsOpen(VIEW.world().panels));
			}
		}.hoverInfoSet(DicArmy.¤¤Armies);
		butts.addDown(0, b);
		
		
		b = new GButt.ButtPanel(SPRITES.icons().m.map) {
			@Override
			protected void clickA() {
				if (VIEW.world().UI.faction.listIsOpen())
					VIEW.world().UI.faction.close();
				VIEW.world().UI.faction.openList(null);
			};
			@Override
			protected void renAction() {
				selectedSet(VIEW.world().UI.faction.listIsOpen());
			}
		};
		b.hoverInfoSet(D.g("Factions"));
		butts.addRelBody(8, DIR.E, b);
		
		b = new GButt.ButtPanel(SPRITES.icons().m.building) {
			@Override
			protected void clickA() {
				if (VIEW.world().panels.added(VIEW.world().UI.camps))
					VIEW.world().panels.remove(VIEW.world().UI.camps);
				else
					VIEW.world().panels.add(VIEW.world().UI.camps, true);
			};
			@Override
			protected void renAction() {
				selectedSet(VIEW.world().panels.added(VIEW.world().UI.camps));
			}
		}.hoverSet(World.BUILDINGS().camp.info);
		butts.addRelBody(8, DIR.E, b);

		b = new UICaravanList();
		butts.addRelBody(8, DIR.E, b);

		{

			
			

			
			butts.addRight(0, new GButt.ButtPanel(SPRITES.icons().m.crossair) {
				@Override
				protected void clickA() {
					VIEW.world().window.centererTile.set(FACTIONS.player().capitolRegion().cx(), FACTIONS.player().capitolRegion().cy());
				};
			}.hoverInfoSet(D.g("go to capitol")));
			
			if (S.get().developer) {
				butts.addRight(0, new GButt.ButtPanel(SPRITES.icons().s.cog) {
					@Override
					protected void clickA() {
						VIEW.world().debug.show();
					}
					
					@Override
					protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
							boolean isHovered) {
						selectedSet(VIEW.world().debug.isActivated());
						super.render(r, ds, isActive, isSelected, isHovered);
					}
				}.hoverInfoSet(D.g("developer tools")));
			}
		}

		top.addLeft(butts);
	}

	
}
