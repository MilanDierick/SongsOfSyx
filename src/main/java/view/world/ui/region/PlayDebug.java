package view.world.ui.region;

import game.boosting.BSourceInfo;
import game.faction.FACTIONS;
import init.sprite.UI.UI;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.data.INT.INTE;
import util.gui.misc.GButt;
import util.gui.misc.GText;
import util.gui.slider.GSliderInt;
import world.regions.Region;
import world.regions.data.RBooster;
import world.regions.data.RD;
import world.regions.data.pop.RDRace;

class PlayDebug extends GuiSection{

	Region reg;
	
	PlayDebug(){
		padd(new GButt.ButtPanel("admin+") {
			
			@Override
			protected void clickA() {
				
				new RBooster(new BSourceInfo("cheat", null), 0, 2500, false) {
					final Region rr = reg;

					@Override
					protected double get(Region reg) {
						return reg == rr ? 1 : 0;
					}

					
				}.add(RD.ADMIN().boost);
			}
			
		});
		
		padd(new GButt.Checkbox("claim") {
			
			@Override
			protected void renAction() {
				selectedSet(RD.REALM(reg) == RD.REALM(FACTIONS.player()));
			}
			
			@Override
			protected void clickA() {
				RD.setFaction(reg, selectedIs() ? null : FACTIONS.player());
			}
		});
		
		padd(new GButt.ButtPanel("affiliate") {
			@Override
			protected void clickA() {
				RD.OWNER().affiliation.setD(reg, 1.0);
			}
		});
		
		{
			GuiSection s = new GuiSection();
			s.add(new GText(UI.FONT().S, "devastation"), 0, 0);
			INTE ii = new INTE() {
				
				@Override
				public int min() {
					return 0;
				}
				
				@Override
				public int max() {
					return RD.DEVASTATION().current.max(null);
				}
				
				@Override
				public int get() {
					return RD.DEVASTATION().current.get(reg);
				}
				
				@Override
				public void set(int t) {
					RD.DEVASTATION().current.set(reg, t);
				}
			};
			
			s.addRightC(8, new GSliderInt(ii, 100, false));
			padd(s);
		}
		
		padd(new GButt.ButtPanel("garrison") {
			@Override
			protected void clickA() {
				RD.MILITARY().garrison.inc(reg, 50);
			}
		});
		
//		padd(new GButt.ButtPanel("finish b.") {
//			@Override
//			protected void clickA() {
//				for (RDBuilding b : RD.BUILDINGS().all) {
//					b.level.set(reg, b.targetLevel.get(reg));
//				}
//			}
//		});
		
		padd(new GButt.ButtPanel("build") {
			@Override
			protected void clickA() {
				RD.UPDATER().BUILD(reg);
//				for (RDBuilding b : RD.BUILDINGS().all) {
//					b.level.set(reg, b.targetLevel.get(reg));
//				}
			}
		});
		
		padd(new GButt.ButtPanel("pop ini") {
			
			@Override
			protected void clickA() {
				
				RD.RACES().initPopulation(reg);
			}
			
		});
		
		padd(new GButt.ButtPanel("pop clear") {
			
			@Override
			protected void clickA() {
				
				for (RDRace r : RD.RACES().all) {
					r.pop.set(reg, 0);
				}
			}
			
		});
		
		
		for (RDRace r : RD.RACES().all) {
			padd(new GButt.ButtPanel(r.race + "++") {
				
				@Override
				protected void clickA() {
					r.pop.inc(reg, 100);
				}
				
			});
		}
	}
	
	void padd(RENDEROBJ o) {
		if (getLastX2() > 600) {
			add(o, 0, body().y2());
		}else
			addRightC(0, o);
		
	}
	
}
