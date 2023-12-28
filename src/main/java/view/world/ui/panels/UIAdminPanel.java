package view.world.ui.panels;

import game.faction.FACTIONS;
import settlement.main.SETT;
import settlement.room.infra.admin.ROOM_ADMIN;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.data.GETTER;
import util.dic.DicMisc;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.info.GFORMAT;
import view.interrupter.ISidePanel;
import view.interrupter.ISidePanels;
import view.main.VIEW;
import world.regions.Region;
import world.regions.data.RD;

public final class UIAdminPanel extends ISidePanel{

	public UIAdminPanel(ISidePanels panels){
		titleSet(DicMisc.¤¤Admin);
		
		section.addRelBody(2, DIR.S, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.iofk(text, RD.ADMIN().factionSource.get(FACTIONS.player())-RD.ADMIN().consumed(FACTIONS.player()), RD.ADMIN().factionSource.get(FACTIONS.player()));
			}
		}.hv(DicMisc.¤¤Available));
		
		section.body().incrH(16);
		
		for (ROOM_ADMIN a : SETT.ROOMS().ADMINS) {
			section.addRelBody(2, DIR.S, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.iIncr(text, a.knowledge());
				}
			}.hh(a.iconBig()));
		}
		
		GTableBuilder bu = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				return FACTIONS.player().realm().regions()-1;
			}
						
		};
		
		bu.column(null, 250, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				GuiSection s = new GuiSection() {
					@Override
					protected void clickA() {
						Region r = FACTIONS.player().realm().region(ier.get()+1);
						if (r != null) {
							ISidePanel pp = VIEW.world().UI.regions.get(r);
							panels.clear();
							panels.add(UIAdminPanel.this, true);
							panels.add(pp, false);
						}
					}
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						Region r = FACTIONS.player().realm().region(ier.get()+1);
						if (r != null)
							VIEW.world().UI.regions.hover(r, text);
					}
					
				};
				s.add(new GStat() {
					
					@Override
					public void update(GText text) {
						Region r = FACTIONS.player().realm().region(ier.get()+1);
						if (r != null)
							text.add(r.info.name());
					}
					
				}.r(DIR.W));
				s.addRightC(180, new GStat() {
					
					@Override
					public void update(GText text) {
						Region r = FACTIONS.player().realm().region(ier.get()+1);
						if (r != null)
							GFORMAT.iIncr(text, (int)RD.ADMIN().consumed(r));
					}
				});
				
				s.body().setWidth(250);
				s.pad(0, 6);
				
				return s;
			}
		});
		
		section.addRelBody(16, DIR.S, bu.createHeight(HEIGHT-section.body().height()-32, true));
		
	}
	
}
