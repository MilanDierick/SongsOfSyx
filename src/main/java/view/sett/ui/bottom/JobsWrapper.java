package view.sett.ui.bottom;

import init.C;
import init.D;
import init.biomes.BUILDING_PREFS;
import init.race.RACES;
import init.race.Race;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.job.Job;
import settlement.job.JobBuildStructure;
import settlement.main.SETT;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.*;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.*;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.dic.DicMisc;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.panel.GPanelS;
import view.keyboard.KEYS;
import view.keyboard.KeyButt;
import view.main.VIEW;
import view.tool.*;

final class JobsWrapper {



	private static CharSequence ¤¤Fences = "¤Fences";
	private static CharSequence ¤¤Roads = "¤Roads";
	private static CharSequence ¤¤Place = "¤Place: ";
	
	
	static {
		D.ts(JobsWrapper.class);
	}
	
	private JobsWrapper() {
	
	}
	
	public static CLICKABLE forts() {
		return normal(SETT.JOBS().build_fort, DicMisc.¤¤Fortifications, ""+¤¤Place + DicMisc.¤¤Fortifications, SPRITES.icons().m.fortification, "FORTIFICATION");
	}
	
	public static CLICKABLE fences() {
		return normal(SETT.JOBS().fences, ¤¤Fences, ""+¤¤Place + ¤¤Fences, SETT.JOBS().fences.get(0).placer().getIcon(), "PLACE_FENCES");
	}
	
	public static CLICKABLE roads() {
		return normal(SETT.JOBS().roads, ¤¤Roads, ""+¤¤Place + ¤¤Roads, SETT.JOBS().roads.get(0).placer().getIcon(), "PLACE_ROADS");
	}
	
	public static CLICKABLE struct() {

		PlaceStruct st = new PlaceStruct(SETT.JOBS().build_structure.get(0), DicMisc.¤¤Structures, ""+¤¤Place +  DicMisc.¤¤Structures);
		
		
		GuiSection section = new GuiSection();
		
		for (JobBuildStructure j : SETT.JOBS().build_structure) {
			
			
			GButt.ButtPanel b = new GButt.ButtPanel(j.wall.placer().getIcon()) {
				
				@Override
				protected void clickA() {
					st.setStruc(j);
				};
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					text.title(j.building.name);
					text.text(j.building.desc);
					GBox b = (GBox) text;
					b.NL();
					b.setResource(j.building.resource, j.building.resAmount);
					
					b.NL(8);
					
					for (Race r : RACES.all()) {
						double d = r.pref().structure(BUILDING_PREFS.get(j.building));
						int k = 1 + (int) (5*d);
						if ((r.index & 0b0011) == 0)
							b.NL();
						b.tab((r.index&0b011)*3);
						b.add(r.appearance().icon);
						ColorImp.TMP.interpolate(GCOLOR.UI().BAD.hovered, GCOLOR.UI().GOOD.hovered, d);
						for (int i = 0; i < k; i++) {
							b.add(SPRITES.icons().s.heart, ColorImp.TMP);
							b.rewind(8);
						}
						b.space();
						
					}
					
				}
				
				@Override
				protected void renAction() {
					selectedSet(st.struc == j);
				}
				
			};
			
			section.addRightC(0, b);
		}
		
		Config c = new Config(section, st);
		return wrap(st, c,  DicMisc.¤¤Structures, ""+¤¤Place +  DicMisc.¤¤Structures, SETT.JOBS().build_structure.get(0).combo.getIcon(), "PLACE_STRUCTURES");

	}
	
	private static class PlaceStruct extends Place {

		LinkedList<CLICKABLE> butts = new LinkedList<>();
		private final ArrayList<CLICKABLE> ad = new ArrayList<>(10);
		private JobBuildStructure struc;
		private int type = 0;
		
		PlaceStruct(JobBuildStructure str, CharSequence name, CharSequence desc) {
			super(str.combo, name, desc);
			struc = str;
			
			
			butts.add(new GButt.ButtPanel(SPRITES.icons().m.wallceiling) {
				
				@Override
				protected void clickA() {
					job = struc.combo;
					type = 0;
				};
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					struc.combo.hoverDesc((GBox) text);
				}
				
				@Override
				protected void renAction() {
					selectedSet(type == 0);
				}
				
			});
			
			butts.add(new GButt.ButtPanel(SPRITES.icons().m.wall) {
				
				@Override
				protected void clickA() {
					job = struc.wall.placer();
					type = 1;
				};
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					struc.wall.placer().hoverDesc((GBox) text);
				}
				
				@Override
				protected void renAction() {
					selectedSet(type == 1);
				}
				
			});
			
			butts.add(new GButt.ButtPanel(SPRITES.icons().m.ceiling) {
				
				@Override
				protected void clickA() {
					job = struc.ceiling.placer();
					type = 2;
				};
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					struc.ceiling.placer().hoverDesc((GBox) text);
				}
				
				@Override
				protected void renAction() {
					selectedSet(type == 2);
				}
				
			});
			
			butts.add(new GButt.ButtPanel(SPRITES.icons().m.arrow_right) {
				
				@Override
				protected void clickA() {
					job = struc.convert;
					type = 3;
				};
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					struc.convert.hoverDesc((GBox) text);
				}
				
				@Override
				protected void renAction() {
					selectedSet(type == 3);
				}
				
			});
			

			
		}
		
		void setStruc(JobBuildStructure struc) {
			this.struc = struc;
			switch(type) {
			case 0: job = struc.combo; break;
			case 1: job = struc.wall.placer(); break;
			case 2: job = struc.ceiling.placer(); break;
			case 3: job = struc.convert; break;
			}
		}
		
		
		@Override
		public LIST<CLICKABLE> getAdditionalButt() {
			ad.clear();
			if (super.getAdditionalButt() != null)
				ad.add(super.getAdditionalButt());
			ad.add(butts);
			return ad;
		}
		
	}
	
	public static CLICKABLE normal(LIST<? extends Job> jobs, CharSequence name, CharSequence desc, SPRITE icon, String key) {
		
		Job jjj = jobs.get(0);
		for (Job jj : jobs)
			if (jj.lockText() == null)
				jjj = jj;
		
		Place place = new Place(jjj.placer(), name, desc); 
		GuiSection section = new GuiSection();
		
		for (Job j : jobs) {
			
			
			GButt.ButtPanel b = new GButt.ButtPanel(j.placer().getIcon()) {
				
				@Override
				protected void clickA() {
					if (j.lockText() == null)
						place.job = j.placer();
				};
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					j.placer().hoverDesc(b);
					if (j.lockText() != null) {
						b.NL(8);
						b.error(j.lockText());
					}
				}
				
				@Override
				protected void renAction() {
					
					selectedSet(place.job == j.placer());
				}
				
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
						boolean isHovered) {
					super.render(r, ds, isActive, isSelected, isHovered);
					if (j.lockText() != null) {
						OPACITY.O50.bind();
						COLOR.BLACK.render(r, body);
						OPACITY.unbind();
					}
				}
				
			};
			
			section.addRightC(0, b);
		}
		Config config = new Config(section, place);
		return wrap(place, config, name, desc, icon, key);
	}
	
	private static CLICKABLE wrap(Place place, Config config, CharSequence name, CharSequence desc, SPRITE icon, String key) {
		
		ACTION a = new ACTION() {
			
			@Override
			public void exe() {
				VIEW.inters().popup.close();
				VIEW.s().tools.place(place, config);
			}
		};
		GButt.ButtPanel b = new GButt.ButtPanel(name) {
			
			@Override
			protected void clickA() {
				super.clickA();
				a.exe();
			};
			
		}.icon(icon);
		b.setDim(Popup.width, Popup.bh);
		CLICKABLE c = KeyButt.wrap(a, b, KEYS.SETT(), key, name, desc);
		SearchToolPanel.add(b, name);
		return c;
	}
	
	private static class Place extends PlacableMulti {
		PlacableMulti job;
		
		Place(PlacableMulti j, CharSequence name, CharSequence desc) {
			super(name, "", null, SETT.JOBS().tool_clear);
			job = j;
		}

		@Override
		public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
			return job.isPlacable(tx, ty, area, type);
		}

		@Override
		public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
			job.place(tx, ty, area, type);
		}
		
		@Override
		public boolean expandsTo(int fromX, int fromY, int toX, int toY) {
			return job.expandsTo(fromX, fromY, toX, toY);
		}
		
		@Override
		public void finishPlacing(AREA placedArea) {
			job.finishPlacing(placedArea);
		}
		
		@Override
		public LIST<CLICKABLE> getAdditionalButt() {
			return job.getAdditionalButt();
		}

		@Override
		public boolean canBePlacedAs(PLACER_TYPE t) {
			return job.canBePlacedAs(t);
		}

		@Override
		public PLACABLE getUndo() {
			return job.getUndo();
		}
		
		@Override
		public void hoverDesc(GBox box) {
			job.hoverDesc(box);
		}
		
		@Override
		public CharSequence isPlacable(AREA area, PLACER_TYPE type) {
			return job.isPlacable(area, type);
		}
		
		@Override
		public void placeInfo(GBox b, int oktiles, AREA a) {
			job.placeInfo(b, oktiles, a);
		}
		
		@Override
		public void renderPlaceHolder(SPRITE_RENDERER r, int mask, int x, int y, int tx, int ty, AREA area,
				PLACER_TYPE type, boolean isPlacable, boolean areaIsPlacable) {
			job.renderPlaceHolder(r, mask, x, y, tx, ty, area, type, isPlacable, areaIsPlacable);
		}
	}
	
	
	private static class Config implements ToolConfig {
		
		protected GuiSection section;
		private final GPanelS panel = new GPanelS();
		private GuiSection full = new GuiSection();
		private final Place place;
		CLICKABLE exit = new GButt.Glow(SPRITES.icons().s.cancel, UI.PANEL().panelM.get(0)) {
			@Override
			protected void clickA() {
				VIEW.s().tools.placer.deactivate();
			};
		};
		
		Config(GuiSection section, Place place){
			this.section = section;
			this.place = place;
		}
		
		@Override
		public void addUI(LISTE<RENDEROBJ> uis) {
			full.clear();
			
			VIEW.s().tools.placer.stealButtons(full);
			if (place.getAdditionalButt() != null)
				for (CLICKABLE p : place.getAdditionalButt())
					full.addRightC(0, p);
			full.body().centerX(C.DIM());
			full.addRelBody(C.SG*8, DIR.N, section);
			
			panel.setButtBg();
			panel.inner().set(full);
			panel.moveExit(exit);
			full.add(exit);
			full.add(panel);
			full.moveLastToBack();
			full.body().moveY1(75);
			uis.add(full);
		}
	}
	
	
	
	
}
