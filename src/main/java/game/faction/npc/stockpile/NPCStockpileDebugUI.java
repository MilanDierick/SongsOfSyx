package game.faction.npc.stockpile;

import game.faction.npc.FactionNPC;
import game.faction.npc.stockpile.Updater.SIns;
import game.time.TIME;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LIST;
import util.data.GETTER;
import util.gui.misc.*;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.info.GFORMAT;

public class NPCStockpileDebugUI extends GuiSection{

	
	public NPCStockpileDebugUI(FactionNPC faction){
		
		int HEIGHT = 800;
		
		add(sNew(faction, HEIGHT/2-32));
		add(sOld(faction, HEIGHT/2-32), 0, body().y2());
		
		addDownC(2, new GButt.ButtPanel("update") {
			@Override
			protected void clickA() {
				faction.stockpile.update(faction, TIME.secondsPerDay);
			}
		});
		
		addRightC(2, new GButt.ButtPanel("clear") {
			@Override
			protected void clickA() {
				faction.stockpile.saver().clear();
			}
		});
		
//		section.addRightC(2, new GButt.ButtPanel("r bonus") {
//			@Override
//			protected void clickA() {
//				faction.bonus().randomize();
//			}
//		});
		
	}
	
	private static GuiSection sNew(FactionNPC faction, int height) {
		
		final LIST<SIns> all = NPCStockpile.updater.allIns;
		
		
		GuiSection section = new GuiSection();
		
		GTableBuilder bu = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				return all.size();
			}
			
			@Override
			public void hoverInfo(int index, GBox box) {
				SIns p = all.get(index);
				box.text(p.ins.name);
			}
		};
		
		bu.column("", 32, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new RENDEROBJ.RenderImp(24) {
					
					@Override
					public void render(SPRITE_RENDERER r, float ds) {
						SIns p = all.get(ier.get());
						p.out.resource.icon().render(r, body);
					}
				};
			}
		});
		
		bu.column("ra", 60, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new GStat() {
					
					@Override
					public void update(GText text) {
						SIns p = all.get(ier.get());
						GFORMAT.f(text, p.out.rate);
					}
				}.r(DIR.NW);
			}
		});
		
		bu.column("bo", 60, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new GStat() {
					
					@Override
					public void update(GText text) {
						SIns p = all.get(ier.get());
						GFORMAT.f(text, p.ins.industry.bonus().get(faction));
					}
				}.r(DIR.NW);
			}
		});
		
		bu.column("bot", 60, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new GStat() {
					
					@Override
					public void update(GText text) {
						SIns p = all.get(ier.get());
						GFORMAT.f(text, p.out.rate*p.ins.industry.bonus().get(faction));
					}
				}.r(DIR.NW);
			}
		});
		
		bu.column("s1", 60, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new GStat() {
					
					@Override
					public void update(GText text) {
						SIns p = all.get(ier.get());
						GFORMAT.f(text, p.prodSpeed);
					}
				}.r(DIR.NW);
			}
		});

		bu.column("s2", 60, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new GStat() {
					
					@Override
					public void update(GText text) {
						SIns p = all.get(ier.get());
						GFORMAT.f(text, p.prodSpeedBonus);
					}
				}.r(DIR.NW);
			}
		});
		
		bu.column("s3", 60, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new GStat() {
					
					@Override
					public void update(GText text) {
						SIns p = all.get(ier.get());
						GFORMAT.f(text, p.prodSpeedTot);
					}
				}.r(DIR.NW);
			}
		});
		
		
		section.add(bu.createHeight(height, true));
		return section;
	}
	
	private static GuiSection sOld(FactionNPC faction, int height) {
		
		GuiSection section = new GuiSection();
		
		GTableBuilder bu = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				return RESOURCES.ALL().size();
			}
		};
		
		bu.column("", 32, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new RENDEROBJ.RenderImp(24) {
					
					@Override
					public void render(SPRITE_RENDERER r, float ds) {
						RESOURCE re = RESOURCES.ALL().get(ier.get());
						re.icon().render(r, body);
					}
				};
			}
		});


		bu.column("n1", 60, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.f(text, NPCStockpile.updater.o(ier.get()).needed);
					}
				}.r(DIR.NW);
			}
		});
		
		
		bu.column("p", 60, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.f(text, NPCStockpile.updater.o(ier.get()).prodSpeed);
					}
				}.r(DIR.NW);
			}
		});
		
		bu.column("a", 60, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.f(text, NPCStockpile.updater.o(ier.get()).amount);
						
					}
				}.r(DIR.NW);
			}
		});
		
		bu.column("pr", 60, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.f(text, NPCStockpile.updater.o(ier.get()).prio);
						
					}
				}.r(DIR.NW);
			}
		});
		
//		bu.column("target", 120, new GRowBuilder() {
//			
//			@Override
//			public RENDEROBJ build(GETTER<Integer> ier) {
//				return new GStat() {
//					
//					@Override
//					public void update(GText text) {
//						GFORMAT.f(text, NPCStockpile.updater.target.amountCurrent[ier.get()]);
//						text.s();
//						GFORMAT.f(text, NPCStockpile.updater.target.amountTarget[ier.get()]);
//					}
//				}.r(DIR.NW);
//			}
//		});
//		
//		bu.column("a prio", 120, new GRowBuilder() {
//			
//			@Override
//			public RENDEROBJ build(GETTER<Integer> ier) {
//				return new GStat() {
//					
//					@Override
//					public void update(GText text) {
//						GFORMAT.f(text, NPCStockpile.updater.target.amountPrio[ier.get()]);
//					}
//				}.r(DIR.NW);
//			}
//		});
//		
//		bu.column("p cap", 120, new GRowBuilder() {
//			
//			@Override
//			public RENDEROBJ build(GETTER<Integer> ier) {
//				return new GStat() {
//					
//					@Override
//					public void update(GText text) {
//						GFORMAT.f(text, NPCStockpile.updater.target.capability[ier.get()]);
//					}
//				}.r(DIR.NW);
//			}
//		});
		
		bu.column("add", 60, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.f(text, NPCStockpile.updater.o(ier.get()).produced);
					}
				}.r(DIR.NW);
			}
		});
		
		bu.column("consumed", 120, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.f(text, NPCStockpile.updater.civic.consumed[ier.get()]);
					}
				}.r(DIR.NW);
			}
		});
//		
		bu.column("stored", 120, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new GStat() {
					
					@Override
					public void update(GText text) {
						RESOURCE r = RESOURCES.ALL().get(ier.get());
						GFORMAT.f(text, faction.stockpile.amount(r));
					}
				}.r(DIR.NW);
			}
		});
//		
		bu.column("sell", 120, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.f(text, faction. stockpile.priceSell(ier.get(), 32));
					}
				}.r(DIR.NW);
			}
		});
		
		bu.column("buy", 120, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.f(text, faction.stockpile.priceBuy(ier.get(), 32));
					}
				}.r(DIR.NW);
			}
		});
		
		section.add(bu.createHeight(height, true));
		return section;
	}
	
	
}
