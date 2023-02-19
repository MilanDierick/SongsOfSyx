package view.sett.ui.room.construction;

import init.C;
import init.resources.RESOURCES;
import init.sprite.ICON;
import settlement.main.SETT;
import snake2d.SPRITE_RENDERER;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;

final class SStats {

	private final HOVERABLE[] statStats = new HOVERABLE[8];
	private final HOVERABLE statResourcesStructure;
	private final HOVERABLE statResourcesCave;
	private final HOVERABLE[] statResources = new HOVERABLE[8];
	private final GuiSection stats = new GuiSection();
	private final State s;
	
	SStats(State s){
		this.s = s;
		statResourcesStructure = new HOVERABLE.HoverableAbs((int) (ICON.MEDIUM.SIZE*2.5), ICON.MEDIUM.SIZE) {
			final GStat stat = new GStat() {
				
				@Override
				public void update(GText text) {
					int am = s.placement.placer.structure.roofs()*s.placement.placer.structure.get().resAmount;
					am += s.placement.placer.structure.walls()*s.placement.placer.structure.get().resAmount;
					GFORMAT.i(text, am);
				}
			};
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {

				s.placement.placer.structure.get().resource.icon().render(r, body().x1(), body().y1());
				stat.render(r, body().x1() + ICON.MEDIUM.SIZE+C.SG*2, body().y1()+(body().height()-stat.height())/2);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.text(s.placement.placer.structure.get().resource.name);
				text.NL();
				text.text(s.placement.placer.structure.get().nameCeiling);
			}
		};
		statResourcesCave = new HOVERABLE.HoverableAbs((int) (ICON.MEDIUM.SIZE*2.5), ICON.MEDIUM.SIZE) {
			final GStat stat = new GStat() {
				
				@Override
				public void update(GText text) {
					int am = s.placement.placer.structure.mountainWalls()*SETT.JOBS().clearss.caveFill.resAmount();
					GFORMAT.i(text, am);
				}
			};
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				if (s.placement.placer.structure.mountainWalls() == 0)
					return;
				RESOURCES.STONE().icon().render(r, body().x1(), body().y1());
				stat.render(r, body().x1() + ICON.MEDIUM.SIZE+C.SG*2, body().y1()+(body().height()-stat.height())/2);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.text(RESOURCES.STONE().name);
				text.NL();
				text.text(SETT.JOBS().clearss.caveFill.placer().name());
			}
		};
		

		for (int i = 0; i < 8; i++) {
			final int k = i;
			statResources[i] = new HOVERABLE.HoverableAbs((int) (ICON.MEDIUM.SIZE*2.5), ICON.MEDIUM.SIZE) {
				final GStat stat = new GStat() {
					
					@Override
					public void update(GText text) {
						double am = SETT.ROOMS().placement.placer.resNeeded(k);
						GFORMAT.i(text, (int)Math.ceil(am));
						
					}
				};
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
					if (SETT.ROOMS().placement.placer.resNeeded(k) <= 0)
						return;
					s.b.constructor().resource(k).icon().render(r, body().x1(), body().y1());
					stat.render(r, body().x1() + ICON.MEDIUM.SIZE+C.SG*2, body().y1()+(body().height()-stat.height())/2);
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					if ( SETT.ROOMS().placement.placer.resNeeded(k) <= 0)
						return;
					text.text(s.b.constructor().resource(k).name);
				}
			};

			statStats[i] = new HOVERABLE.HoverableAbs(C.SG*250, C.SG*16) {
				final GStat stat = new GStat() {
					@Override
					public void update(GText text) {
						s.b.constructor().stats().get(k).format(text, s.placement.placer.itemStats(k));
					}
				};
				final GStat title = new GStat() {
					@Override
					public void update(GText text) {
						text.lablify().add(s.b.constructor().stats().get(k).name());
					}
				};
				
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
					stat.render(r, body().x1()+190, body().y1());
					title.render(r, body().x1(), body().y1());
					
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					text.text(s.b.constructor().stats().get(k).desc());
				}
			};
			
		}
	}
	
	GuiSection get() {
		stats.clear();
		int k = 0;
		
		for (int i = 0; i <s. b.constructor().stats().size(); i++) {
			stats.addDown(0, statStats[i]);
		}
		
		int w = statResources[0].body().width();
		int h = statResources[0].body().height();
		int y1 = 84+18;
		
		for (int i = 0; i < s.b.constructor().resources(); i++) {
			stats.add(statResources[i], (k%3)*w, y1 + (k/3)*h);
			k++;
		}
		if (s.b.constructor().mustBeIndoors()) {
			k++;
			stats.add(statResourcesStructure, (k%3)*w, y1+ (k/3)*h);
			k++;
			stats.add(statResourcesCave, (k%3)*w, y1 + (k/3)*h);
		}
		stats.body().incrH(16);
		return stats;
	}
	
}
