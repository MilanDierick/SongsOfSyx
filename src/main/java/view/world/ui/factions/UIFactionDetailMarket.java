package view.world.ui.factions;

import init.C;
import init.D;
import init.resources.RESOURCES;
import init.settings.S;
import init.sprite.ICON;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.data.GETTER;
import util.gui.misc.*;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.info.GFORMAT;
import util.statistics.HistoryResource;

final class UIFactionDetailMarket extends GuiSection{

	UIFactionDetailMarket(UIFactionDetail u, int height){

		D.gInit(this);
		
		GTableBuilder builder = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				return RESOURCES.ALL().size();
			}
			
			@Override
			public void hoverInfo(int index, GBox box) {
				if (index < 0)
					return;
				box.title(RESOURCES.ALL().get(index).name);
				
				for (HistoryResource r : u.faction.res().ins()) {
					box.textLL(r.info().name);
					box.tab(4);
					box.add(GFORMAT.iIncr(box.text(), r.get(RESOURCES.ALL().get(index))));
					box.NL();
				}
				box.NL(8);
				
				for (HistoryResource r : u.faction.res().outs()) {
					box.textLL(r.info().name);
					box.tab(4);
					box.add(GFORMAT.iIncr(box.text(), -r.get(RESOURCES.ALL().get(index))));
					box.NL();
				}
				
			}
		};
		
		builder.column(null, ICON.MEDIUM.SIZE, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				
				return new RENDEROBJ.Sprite(ICON.MEDIUM.SIZE) {
					@Override
					public void render(SPRITE_RENDERER r, float ds) {
						RESOURCES.ALL().get(ier.get()).icon().render(r, body());
					}
				};
			}
		});
		
		builder.column(D.g("sell"), ICON.MEDIUM.SIZE*3, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.i(text, (int) Math.ceil((u.faction.seller().priceSell(RESOURCES.ALL().get(ier.get()), 1))));
					}
				}.r(DIR.E);
			}
		}, DIR.E);
		
		builder.column(D.g("buy"), ICON.MEDIUM.SIZE*3, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.i(text, (int) (u.faction.buyer().buyPrice(RESOURCES.ALL().get(ier.get()), 1)));
					}
				}.r(DIR.E);
			}
		}, DIR.E);
		
		if (S.get().developer) {
			builder.column(D.g("stored"), ICON.MEDIUM.SIZE*4, new GRowBuilder() {
				
				@Override
				public RENDEROBJ build(GETTER<Integer> ier) {
					return new GStat() {
						
						@Override
						public void update(GText text) {
							GFORMAT.i(text, u.faction.seller().forSale(RESOURCES.ALL().get(ier.get())));
						}
					}.r(DIR.E);
				}
			}, DIR.E);
			
		}
		
		builder.column(null, C.SG*8, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new GStat() {
					
					@Override
					public void update(GText text) {
						
					}
				}.r(DIR.E);
			}
		}, DIR.E);
		
		add(builder.createHeight(height, true));
		

	}

}
