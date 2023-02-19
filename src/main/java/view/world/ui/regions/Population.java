package view.world.ui.regions;

import static view.world.ui.regions.UIRegion.*;

import init.race.*;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayList;
import util.colors.GCOLOR;
import util.gui.misc.*;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import world.map.regions.*;
import world.map.regions.RegionTaxes.RegionIndustry;

final class Population extends GuiSection{

	
	Population(){

		int[] cxs = new int[] {
			80,
			223,
			311,
			351,
			391,
			431,
			471
		};
		int xi = 0;
		
		GuiSection hs = new GuiSection();
		RegionRace r = REGIOND.RACE(RACES.all().get(0));
		hs.addCentredX(new GHeader(r.population.info().name), cxs[xi++]);
		hs.addCentredX(new GHeader(r.loyalty_target.info().name), cxs[xi++]);
		hs.addCentredX(new HOVERABLE.Sprite(SPRITES.icons().m.noble).hoverInfoSet(r.elevation.info().name), cxs[xi++]);
		hs.addCentredX(new HOVERABLE.Sprite(SPRITES.icons().m.descrimination).hoverInfoSet(r.prosecute.info().name), cxs[xi++]);
		hs.addCentredX(new HOVERABLE.Sprite(SPRITES.icons().m.cancel).hoverInfoSet(r.exile.info().name), cxs[xi++]);
		hs.addCentredX(new HOVERABLE.Sprite(SPRITES.icons().m.skull).hoverInfoSet(r.massacre.info().name), cxs[xi++]);
		add(hs);
		
		
		
		
		
		ArrayList<RENDEROBJ> rows = new ArrayList<>(RACES.all().size());
		for (Race ra : RACES.all())
			if (ra.population().rarity > 0)
				rows.add(row(ra));
		int am = (int) Math.round(CORE.getGraphics().nativeHeight*5/800.0);
		add(new GScrollRows(rows, rows.get(0).body().height()*Math.min(am, RACES.all().size())).view(), 0, getLastY2());
		
		xi = 0;
		hs = new GuiSection();
		
		hs.addCentredX(new RENDEROBJ.RenderImp(120, 16) {
			
			GText text = new GText(UI.FONT().S, 8);
			
			@Override
			public void render(SPRITE_RENDERER ren, float ds) {
				int n = 0;
				int t = 0;
				for (Race r : RACES.all()) {
					n += REGIOND.RACE(r).population.get(reg);
					t += REGIOND.RACE(r).targetPop(reg);
				}
				
				
				text.clear();
				GFORMAT.i(text, n);
				text.renderCY(ren, body().x1(), body().cY());
				SPRITES.icons().s.arrow_right.renderC(ren, body);
				text.clear();
				GFORMAT.i(text, t);
				text.color(n <= t ? GCOLOR.T().IGOOD : GCOLOR.T().IBAD);
				text.renderCY(ren, body().cX()+10, body().cY());
			}
		}, cxs[xi++]);
		
		RENDEROBJ rr = new RENDEROBJ.RenderImp(120, 16) {
			
			GText text = new GText(UI.FONT().S, 8);
			
			@Override
			public void render(SPRITE_RENDERER ren, float ds) {
				double n = REGIOND.OWNER().loyalty_current.getD(reg);
				double t = REGIOND.OWNER().loyalty_target.getD(reg);

				text.clear();
				GFORMAT.perc(text, n);
				text.renderCY(ren, body().x1(), body().cY());
				SPRITES.icons().s.arrow_right.renderC(ren, body);
				text.clear();
				GFORMAT.perc(text, t);
				text.renderCY(ren, body().cX()+10, body().cY());
			}
		};
		
		hs.addCentredX(rr, cxs[xi++]);
		hs.body().moveY1(getLastY2()+4);
		add(hs);
		
	}
	
	GuiSection row(Race race) {
		GuiSection s = new GuiSection() {
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				
				super.render(r, ds);
				GCOLOR.UI().border().render(r, body().x1(), body().x2(), body().y2()-1, body().y2());
			}
		};
		RegionRace r = REGIOND.RACE(race);
		
		s.add(new HOVERABLE.Sprite(race.appearance().icon){
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				b.title(race.info.names);
				b.text(race.info.desc);
				b.NL(4);
				
				b.textL(RacePopulation.¤¤rarity);
				b.add(GFORMAT.percBig(b.text(), race.population().rarity));
				
				b.NL(4);
				
				b.textL(RacePopulation.¤¤reproductionRate);
				b.add(GFORMAT.percBig(b.text(), race.population().reproductionRate));
				
				b.NL(4);
				
				{
					int t = 0;
					for (RegionIndustry i : REGIOND.RES().all) {
						if (t++ > 8) {
							b.NL();
							t = 0;
						}
						b.add(i.industry.blue.iconBig());
						b.add(GFORMAT.f0(b.text(), i.industry.bonus().race(race)));
						b.space();
					}
				}

			}
		}, 0, 0);
		s.addRightC(8, pop(race));
		s.addRightC(4, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.perc(text, REGIOND.RACE(race).population_growth.getD(reg));
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				b.title(REGIOND.RACE(race).population_growth.info().name);
				b.text(REGIOND.RACE(race).population_growth.info().desc);
				b.NL(8);
				
				UIRegion.hover(b, REGIOND.RACE(race).population_growth);
			}
		}.r());
		s.addRightC(56, loyalty(race));

		s.addRightC(24, check(race, r.elevation));
		s.addRightC(24, check(race, r.prosecute));
		s.addRightC(24, check(race, r.exile));
		s.addRightC(24, check(race, r.massacre));
		
		s.pad(0, 3);
		
		return s;
		
	}
	
	private RENDEROBJ pop(Race race) {
		RegionRace ra = REGIOND.RACE(race);
		
		HOVERABLE.HoverableAbs h = new HoverableAbs(80, 12) {
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				double now = ra.population.getD(reg);
				double next = ra.targetPop(reg);
				next /= REGIOND.POP().capacity.getD(reg);
				GMeter.renderDelta(r, now, next, body());
				
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				
				GBox b = (GBox) text;
				
				b.title( ra.population.info().name);
				b.add(GFORMAT.iBig(b.text(), ra.population.get(reg)));
				b.add(SPRITES.icons().s.arrow_right);
				b.add(GFORMAT.iBig(b.text(), ra.targetPop(reg)));
				b.NL();
				

				b.text(ra.population.info().desc);
				b.NL(8);
				
				
				
				b.textLL(ra.population_target.info().name);
				b.NL();
				
				UIRegion.hover(b, ra.population_target);
				
				
				b.NL(2);
				b.textL(ra.crowding.info().name);
				b.tab(5);
				b.add(GFORMAT.f1(b.text(), ra.crowding.getD(reg)));
				b.NL(2);
				
			}
		};
		
		return h;
	}
	
	private RENDEROBJ loyalty(Race race) {
		RegionRace ra = REGIOND.RACE(race);
		
		HOVERABLE.HoverableAbs h = new HoverableAbs(100, 12) {
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				double now = ra.loyalty.getD(reg);
				double next = ra.loyalty_target.next(reg);
				GMeter.renderDelta(r, now, next, body());
				
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				
				GBox b = (GBox) text;
				
				b.title(ra.loyalty_target.info().name);
				b.add(GFORMAT.perc(b.text(), ra.loyalty.getD(reg)));
				b.add(SPRITES.icons().s.arrow_right);
				b.add(GFORMAT.perc(b.text(), ra.loyalty_target.next(reg)));
				b.NL(4);
				b.text(ra.loyalty_target.info().desc);
				b.NL(8);
				
				b.textLL(ra.loyalty_target.info().name);
				b.NL();
				
				UIRegion.hover(b, ra.loyalty_target);
				
			}
		};
		
		return h;
	}
	
	RENDEROBJ check(Race r, RegionDecree d) {
		return new GButt.Checkbox() {
			
			@Override
			protected void clickA() {
				int i = (d.get(reg)+1)&1;
				for (RegionDecree d : REGIOND.RACE(r).decs)
					d.set(reg, 0);
				d.set(reg, i);
			}
			
			@Override
			protected void renAction() {
				selectedSet(d.get(reg) == 1);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				UIRegion.hover(((GBox)text), d);
			}
			
		};
	}
	
	RENDEROBJ stat(RegionFactors stat) {
		
		return new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.f0(text, stat.getD(reg), 0.5);
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				UIRegion.hover((GBox) b, stat);
			}
						
		}.r();

		
	}

	
}
