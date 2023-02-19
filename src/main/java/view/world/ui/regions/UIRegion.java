package view.world.ui.regions;

import init.D;
import init.sprite.ICON;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.sprite.text.StringInputSprite;
import util.colors.GCOLOR;
import util.dic.DicGeo;
import util.gui.misc.*;
import util.info.GFORMAT;
import view.interrupter.ISidePanel;
import view.main.VIEW;
import world.World;
import world.map.regions.*;

final class UIRegion extends ISidePanel {

	private static CharSequence ¤¤Allocated = "¤Allocated Points"; 
	static CharSequence ¤¤cost = "¤Administration points cost:";
	private final ToolAttack tool = new ToolAttack();
	
	static {
		D.ts(UIRegion.class);
	}
	
	static Region reg;
	
	{D.gInit(this);}
	private final StringInputSprite name = new StringInputSprite(Region.nameSize, UI.FONT().H2) {
		@Override
		protected void change() {
			reg.name().clear().add(text());
		};
	};

	UIRegion() {
		titleSet(DicGeo.¤¤Region);
		
		section.add(new GInput(name));
		
		section.addDown(4, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, REGIOND.OWNER().adminCostAll(reg));
				text.s();
				text.add('(');
				GFORMAT.i(text, REGIOND.OWNER().adminPenalty.get(reg));
				text.add(')');
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				b.title(REGIOND.OWNER().adminPoints().info().name);
				b.add(GFORMAT.i(b.text(), REGIOND.OWNER().adminCostAll(reg)));
				b.text(REGIOND.OWNER().adminPoints().info().desc);
				b.NL(12);
				b.error(REGIOND.OWNER().adminPenalty.info().name);
				b.add(GFORMAT.i(b.text(), REGIOND.OWNER().adminPenalty.get(reg)));
				b.NL();
				b.text(REGIOND.OWNER().adminPenalty.info().desc);

			};
			
		}.hh(SPRITES.icons().m.admin));
		
		section.addRightC(90, new HOVERABLE.HoverableAbs(48, ICON.MEDIUM.SIZE) {
			
			GText t = new GText(UI.FONT().S, 6);
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				if (REGIOND.OWNER().deficiency.getD(reg) < 1) {
					GCOLOR.T().IBAD.bind();
					SPRITES.icons().s.alert.renderCY(r, body().x1(), body().cY());
					t.clear();
					GFORMAT.perc(t, -1.0 + REGIOND.OWNER().deficiency.getD(reg));
					t.renderCY(r, body().x1()+18, body().cY());
				}
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				if (REGIOND.OWNER().deficiency.getD(reg) < 1) {
					b.title(REGIOND.OWNER().deficiency.info().name);
					b.text(REGIOND.OWNER().deficiency.info().desc);
				}
			}
		});
		
		
		section.addRelBody(10, DIR.S, new Population());
		
		section.addRelBody(8, DIR.S, new Civic());
		
		section.addRelBody(8, DIR.S, new Military());	
		
		section.addRelBody(8, DIR.S, new Resources());
		

	}
	


	@Override
	protected void update(float ds) {
		World.OVERLAY().hoverRegion(reg);
	}

	ISidePanel get(Region faction) {
		UIRegion.reg = faction;

		name.text().clear().add(reg.name());
		VIEW.world().tools.place(tool, tool.config, false);
		return this;
		// list.open(f, m);
	}

	
	static void hover(GBox b, RegionFactors stat) {
		
		boolean bb = false;
		
		for (RegionFactor f : stat.factors()) {
			double d = f.next(reg);
			
			COLOR c = GCOLOR.T().INACTIVE;
			if (d < 1)
				c = GCOLOR.T().IBAD;
			else if (d > 1)
				c = GCOLOR.T().IGOOD;
			
			if (bb) {
				b.tab(8);
				b.add(b.text().color(c).add(f.info().name));
				b.tab(13);
				b.add(GFORMAT.f(b.text(), d).color(c));
				b.NL(2);
				bb = false;
			}else {
				b.add(b.text().color(c).add(f.info().name));
				b.tab(5);
				b.add(GFORMAT.f(b.text(), d).color(c));
				bb = true;
			}
		}
		b.NL(2);
	}
	
	static void hover(GBox b, RegionDecree stat) {
		
		b.title(stat.info().name);
		b.text(stat.info().desc);
		b.NL(8);
		
		b.text(¤¤cost);
		b.add(b.text().lablify().add(REGIOND.OWNER().nextPointCost(reg, stat.cost(reg))));
		
		b.NL(16);
		
		boolean bb = false;
		
		for (RegionFactor f : stat.affects()) {
			

			if (bb) {
				b.tab(8);
				b.textL(f.stat().info().name);
				b.tab(13);
				b.add(GFORMAT.f0(b.text(), f.factorPerPoint()));
				GText t = b.text();
				t.add('(');
				GFORMAT.f1(t, f.getD(reg));
				t.add(')');
				b.add(t);
				b.NL(2);
				bb = false;
			}else {
				b.textL(f.stat().info().name);
				b.tab(5);
				b.add(GFORMAT.f0(b.text(), f.factorPerPoint()));
				GText t = b.text();
				t.add('(');
				GFORMAT.f1(t, f.getD(reg));
				t.add(')');
				b.add(t);
				bb = true;
			}
		}
	}
	
	static void hoverLight(GBox b, RegionDecree stat) {
		
		b.text(¤¤Allocated);
		b.add(b.text().lablify().add(stat.get(reg)));
		b.NL();
		if (stat.max(reg) != 1 || stat.get(reg) == 0) {
			b.text(¤¤cost);
			b.add(b.text().lablify().add(REGIOND.OWNER().nextPointCost(reg, stat.cost(reg))));
			b.NL();
		}
		
	}
	
	
	
}
