package view.world.ui.region;

import game.faction.FACTIONS;
import game.faction.FCredits;
import game.faction.FResources.RTYPE;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.data.GETTER;
import util.dic.DicGeo;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.info.GFORMAT;
import view.interrupter.ISidePanel;
import view.interrupter.ISidePanels;
import view.main.VIEW;
import world.regions.Region;
import world.regions.data.RD;
import world.regions.data.RDOutput.RDResource;

final class ListPlayer extends ISidePanel{

	
	ListPlayer(ISidePanels panels) {
		
		
		GTableBuilder bu = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				return FACTIONS.player().realm().regions()-1;
			}

			@Override
			public void hoverInfo(int index, GBox box) {
				Region reg = FACTIONS.player().realm().region(index+1);
				VIEW.world().UI.regions.hover(reg, box);
			}
			
			@Override
			public void click(int index) {
				Region reg = FACTIONS.player().realm().region(index+1);
				VIEW.world().window.centererTile.set(reg.cx(), reg.cy());
				ISidePanel p = VIEW.world().UI.regions.get(reg);
				panels.add(ListPlayer.this, true);
				panels.add(p, false);
				super.click(index);
			}
			
			@Override
			public boolean selectedIs(int index) {
				Region reg = FACTIONS.player().realm().region(index+1);
				return VIEW.world().UI.regions.active(reg);
			}
			
		};
		
		HOVERABLE title;
		
		bu.column(DicMisc.¤¤name, 180, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new GStat(UI.FONT().H2) {
					
					@Override
					public void update(GText text) {
						text.lablify().add(reg(ier).info.name());
					}
				}.r(DIR.NW);
			}
		});
		
		title = new HOVERABLE.Sprite(UI.icons().s.human).hoverTitleSet(DicMisc.¤¤Population);
		bu.column(title, 80, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.i(text, RD.RACES().population.get(reg(ier)));
					}
				}.r(DIR.E);
			}
		}, DIR.E);
		
		title = new HOVERABLE.Sprite(UI.icons().s.heart).hoverTitleSet(RD.RACES().loyaltyAll.info().name);
		bu.column(title, 64, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.perc(text, RD.RACES().loyaltyAll.getD(reg(ier)));
					}
				}.r(DIR.E);
			}
		}, DIR.E);
		
		title = new HOVERABLE.Sprite(UI.icons().s.money).hoverTitleSet(FCredits.CTYPE.TAX.name);
		bu.column(title, 80, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.iIncr(text, (int)RD.TAX().boost.get(reg(ier)));
					}
				}.r(DIR.E);
			}
		}, DIR.E);
		
		title = new HOVERABLE.Sprite(UI.icons().s.arrow_left).hoverTitleSet(RTYPE.TAX.name);
		bu.column(title, Icon.S*10, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				
				return new RENDEROBJ.RenderImp(120, Icon.S) {
					
					@Override
					public void render(SPRITE_RENDERER r, float ds) {
						int i = 0;
						for (RDResource res : RD.OUTPUT().all) {
							if (res.getDelivery(reg(ier)) > 0)
								res.res.icon().small.render(r, body.x1()+i*Icon.S, body.y1());
							i++;
							if (i >= 10)
								break;
						}
					}
				};
			}
		}, DIR.E);
		
		
		section.add(bu.createHeight(HEIGHT-64, true));
		
		section.addDownC(8, new GButt.ButtPanel(DicMisc.¤¤All) {
			
			@Override
			protected void clickA() {
				VIEW.world().UI.regions.openOtherList();
			}
			
		});
		
		
		
		
		titleSet(DicGeo.¤¤Realm);
	}
	
	private static Region reg(GETTER<Integer> ier) {
		return FACTIONS.player().realm().region(ier.get()+1);
	}
	
	

	
	
}
