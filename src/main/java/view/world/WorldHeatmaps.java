package view.world;

import init.biomes.*;
import init.race.RACES;
import init.race.Race;
import init.resources.Minable;
import init.resources.RESOURCES;
import init.sprite.SPRITES;
import settlement.main.RenderData;
import settlement.main.RenderData.RenderIterator;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.gui.GuiSection;
import snake2d.util.misc.CLAMP;
import util.colors.GCOLORS_MAP;
import util.data.GETTER.GETTER_IMP;
import util.dic.DicMisc;
import util.dic.DicRes;
import util.gui.misc.GButt;
import util.rendering.ShadowBatch;
import view.main.VIEW;
import world.World;
import world.map.regions.REGIOND;
import world.map.regions.Region;
import world.map.terrain.WorldTerrainInfo;

final class WorldHeatmaps extends GButt.ButtPanel{

	final GETTER_IMP<HeatMap> thing = new GETTER_IMP<HeatMap>();
	private final WorldTerrainInfo info = new WorldTerrainInfo();
	private final GuiSection s;
	
	WorldHeatmaps(){
		super(SPRITES.icons().s.eye);
		s = new GuiSection();
		s.addDown(2, new HeatMap(DicMisc.¤¤Fertility) {

			@Override
			protected double getValue(int tx, int ty) {
				info.clear();
				info.add(tx, ty);
				return info.fertility().getD();
			}
			
		});
		
		{
			GuiSection pick = new GuiSection();
			for (Minable m : RESOURCES.minables()) {
				HeatMap min = new HeatMap(m.resource.name) {

					@Override
					protected double getValue(int tx, int ty) {
						info.clear();
						info.add(tx, ty);
						double d = 0;
						for (TERRAIN t : TERRAINS.ALL()) {
							d += m.terrain(t)*info.get(t).getD();
						}
						d /= m.occurance;
						
						return d;
					}
					
				};
				pick.addDown(2, min);
			}
			
			s.addDown(2, new Butt(DicRes.¤¤Deposits) {
				@Override
				protected void clickA() {
					VIEW.inters().popup.show(pick, WorldHeatmaps.this);
				}
			});
		}
		
		{
			GuiSection pick = new GuiSection();
			for (Race r : RACES.all()) {
				HeatMap min = new HeatMap(r.info.names) {

					@Override
					protected double getValue(int tx, int ty) {
						Region reg = World.REGIONS().getter.get(tx, ty);
						if (reg != null) {
							double am = 2.0*REGIOND.RACE(r).population.getD(reg)/r.population().rarity;
							return am;
						}
						return 0;
					}
					
				};
				pick.addDown(2, min);
			}
			
			s.addDown(2, new Butt(DicMisc.¤¤Population) {
				@Override
				protected void clickA() {
					VIEW.inters().popup.show(pick, WorldHeatmaps.this);
				}
			});
		}
		
//		s.addDown(2, new HeatMap(DicMisc.¤¤Population) {
//
//			@Override
//			protected double getValue(int tx, int ty) {
//				Region reg = World.REGIONS().getter.get(tx, ty);
//				if (reg != null) {
//					return REGIOND.POP().popValue(reg);
//				}
//				return 0;
//			}
//		});
		
		s.addDown(2, new HeatMap(CLIMATES.INFO().name) {

			@Override
			protected double getValue(int tx, int ty) {
				
				return 0;
			}
			
			@Override
			protected void render(Renderer r, ShadowBatch shadowBatch, RenderIterator it) {
				CLIMATE c =World.CLIMATE().getter.get(it.tile());
				COLOR.BLACK.bind();
				SPRITES.icons().s.dot.renderScaled(r, it.x()+6, it.y()+6, 2);
				c.color.bind();
				SPRITES.icons().s.dot.renderScaled(r, it.x(), it.y(), 2);
			}
		});
		
		hoverInfoSet(DicMisc.¤¤Overlays);
		
	}
	
	@Override
	protected void clickA() {
		VIEW.inters().popup.show(s, this);
	}
	
	@Override
	protected void renAction() {
		if (hoveredIs() && MButt.RIGHT.consumeClick()) {
			thing.set(null);
		}
		
		if (thing.get() != null) {
			World.addTopRender(thing.get());
		}
		
		selectedSet(thing.get() != null);
	};
	
	private abstract class Butt extends GButt.ButtPanel {
		public Butt(CharSequence label) {
			super(label);
			setDim(250, 26);
		}
	}
	
	private abstract class HeatMap extends Butt implements World.WorldTopRenderable {

		
		public HeatMap(CharSequence label) {
			super(label);
		}

		@Override
		public void render(Renderer r, ShadowBatch shadowBatch, RenderData data) {
			
			RenderIterator it = data.onScreenTiles();
			while(it.has()) {
				
				render(r, shadowBatch, it);
				it.next();
			}
			
		}
		
		@Override
		protected void clickA() {
			thing.set(this);
		}
		
		@Override
		protected void renAction() {
			selectedSet(thing.get() == this);
		}
		
		protected void render(Renderer r, ShadowBatch shadowBatch, RenderIterator it) {
			Region reg = World.REGIONS().getter.get(it.tile());
			
			if (reg == null || reg.isWater())
				return;
			double v = getValue(it.tx(), it.ty());
			v = CLAMP.d(v, 0, 1);
			if (v > 0) {
				ColorImp.TMP.interpolate(COLOR.BLACK, GCOLORS_MAP.bestOverlay, v).bind();
				SPRITES.cons().BIG.outline_thin.render(r, 0, it.x(), it.y());
				
			}
			if (r.getZoomout() <= 2) {
				int am = (int) Math.round(v*4.0);
				for (int i = 0; i < am; i++) {
					SPRITES.icons().s.plus.renderScaled(r, it.x()+16*(i%2), it.y()+16 + (i/2)*16, 2);
				}
			}
			COLOR.unbind();
		}
		
		protected abstract double getValue(int tx, int ty);
		
	}
	
}
