package view.world.ui.region;

import java.util.LinkedList;

import game.boosting.BoostSpec;
import game.boosting.Boostable;
import game.faction.FACTIONS;
import game.faction.FCredits.CTYPE;
import game.faction.Faction;
import init.C;
import init.D;
import init.settings.S;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.*;
import snake2d.util.datatypes.*;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.data.GETTER.GETTER_IMP;
import util.dic.*;
import util.gui.misc.*;
import util.gui.table.GScrollRows;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.info.GFORMAT;
import view.main.VIEW;
import world.regions.Region;
import world.regions.data.RD;
import world.regions.data.building.*;

class PlayBuildings extends GuiSection{

	private final GETTER_IMP<Region> g;
	
	private final ArrayList<RENDEROBJ> activeButts = new ArrayList<RENDEROBJ>(RD.BUILDINGS().all.size()+1);
	private final RENDEROBJ[] butts = new RENDEROBJ[RD.BUILDINGS().all.size()+1];
	
	private static CharSequence ¤¤AlreadyConstructed = "This building has already been constructed";
	private static CharSequence ¤¤RemoveAll = "Remove all constructed buildings?";

	static {
		D.ts(PlayBuildings.class);
	}
	
	private final COLOR[] buCols = COLOR.interpolate(new ColorImp(40, 40, 30), new ColorImp(127, 110, 10), 16);
	private final GText num = new GText(UI.FONT().S, 8);
	
	public int width = 64+8;
	public static final int height = 64+24;
	private final int amX;
	
	PlayBuildings(GETTER_IMP<Region> g, int width, int height){
		this.g = g;
		this.width = ((width-7*4)/7)&~0b01;
		for (int i = 0; i < RD.BUILDINGS().sorted.size(); i++) {
			butts[i] = new BuildingButt(RD.BUILDINGS().sorted.get(i));
		}
		butts[RD.BUILDINGS().all.size()] = new AddButt();
		
		amX = (width-24)/(this.width);
		
		
		
		
		GTableBuilder builder = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				return (int) Math.ceil(activeButts.size()/(double)amX);
			}
		};
		
		builder.column(null, amX*this.width, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new Row(ier);
			}
		});
		
		add(new Info(g, width));
		
		int hi = height-body().height()-16;
		int h = hi/(PlayBuildings.height+16);
		if (h < 1)
			addRelBody(12, DIR.S, builder.createHeight(PlayBuildings.height, false));
		else
			addRelBody(12, DIR.S, builder.createHeight((PlayBuildings.height+16)*h, false));
		
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		activeButts.clearSloppy();
		activeButts.add(butts[butts.length-1]);
		for (int i = 0; i <RD.BUILDINGS().sorted.size(); i++) {
			RDBuilding b = RD.BUILDINGS().sorted.get(i);
			if (b.level.get(g.get()) != 0)
				activeButts.add(butts[i]);
		}
		
		super.render(r, ds);
	}
	
	private class Row extends GuiSection{
		
		private final GETTER<Integer> ier;
		
		Row(GETTER<Integer> ier){
			this.ier = ier;
			body().setHeight(height+16);
		}
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			int x1 = body().x1();
			int y1 = body().y1();
			clear();
			int s = ier.get()*amX;
			for (int i = 0; i < amX && i+s < activeButts.size(); i++) {
				addRightC(0, activeButts.get(i+s));
			}
			body().setHeight(height+16);
			body().moveX1(x1);
			body().moveY1(y1);
			super.render(r, ds);
		}
		
	}
	
	
	private class AddButt extends GButt.ButtPanel {

		private final GuiSection pop = new GuiSection();
		
		public AddButt() {
			super(SPRITES.icons().m.plus);
			body.setDim(width, height);
			LinkedList<RENDEROBJ> rows = new LinkedList<>();
			
			int wam = 6;
			int hi = 0;
			
			for (RDBuildingCat cat : RD.BUILDINGS().cats) {
				int i = 0;
				GuiSection row = new GuiSection();
				rows.add(row);
				for (RDBuilding b : cat.all()) {
					if (i >= wam) {
						row = new GuiSection();
						rows.add(row);
						i = 0;
					}
					ConstructButt bb = new ConstructButt(b);
					hi = bb.body.height();
					row.addRightC(4, bb);
					i++;
				}
				rows.add(new RENDEROBJ.RenderDummy(1, 12));
			}
			
			int hh = Math.min(rows.size()*(hi+12), C.HEIGHT()-64);
			
			GScrollRows sc = new GScrollRows(rows, hh);
			pop.add(sc.view());
			
		}
		
		@Override
		protected void clickA() {
			VIEW.inters().popup.show(pop, this);
		}
		
	}
	
	private class ConstructButt extends ClickableAbs{
		
		private final RDBuilding bu;
		
		
		ConstructButt(RDBuilding b){
			body.setDim(64+24, 64+24);
			this.bu = b;
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			
			isSelected = bu.level.get(g.get()) != 0;
			isActive = !isSelected && canAfford();
			
			ColorImp cc = ColorImp.TMP;
			cc.set(bu.cat.color);
			cc.render(r, body);
			cc.shadeSelf(0.5);
			cc.renderFrame(r, body, 0, 1);
			cc.renderFrame(r, body, -3, 1);
			
			Rec.TEMP.setDim(body.width()-4, body.height()-4);
			Rec.TEMP.moveC(body.cX(), body.cY());
			GButt.ButtPanel.renderBG(r, isActive, isSelected, isHovered, Rec.TEMP);

			
			
			
			
			bu.levels().get(1).icon.huge.renderC(r, body.cX(), body.cY());
			if(!isActive) {
				OPACITY.O50.bind();
				COLOR.BLACK.render(r, Rec.TEMP);
				OPACITY.unbind();
			}
			renderEfficiency(bu, body, r, efficiency(bu));
//			
			GButt.ButtPanel.renderFrame(r, isActive, isSelected, isHovered, body);
			

			
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			GBox b = (GBox) text;
			b.title(bu.info.name);
			b.text(bu.info.desc);
			
			b.sep();
			
			if (bu.level.get(g.get()) > 0) {
				b.text(¤¤AlreadyConstructed);
				return;
			}
			
			if (bu.baseFactors.size() > 0) {
				b.textSLL(DicMisc.¤¤Efficiency);
				b.NL();
				for (BoostSpec bo : bu.baseFactors) {
					bo.booster.hover(b, bo.get(g.get()));
					b.NL();
				}
			}
			
			bu.levels().get(1).reqs.hover(text, g.get());
			
			b.NL(8);
			hoverCosts(g.get(), bu, 0, 1, text);
			
			b.NL(8);
			hoverNonCosts(g.get(), bu, 0, 1, text);
			
			
		}
		
		@Override
		protected void clickA() {
			if (!active())
				return;
			if (S.get().developer || canAfford()) {
				bu.level.set(g.get(), 1);
				//VIEW.inters().popup.close();
				if (g.get().faction() == FACTIONS.player())
					FACTIONS.player().credits().inc(-credits(bu, 0, 1), CTYPE.CONSTRUCTION);
			}
			
		}
		
		private boolean active() {
			return bu.level.get(g.get()) == 0;
		}
		
		private boolean canAfford() {
			return bu.canAfford(g.get(), 1);
		}
		
	}
	
	
	public void hoverNonCosts(Region reg, RDBuilding bu, int fromL, int toL, GUI_BOX text) {
		GBox b = (GBox) text;
		
		boolean global = false;
		boolean local = false;
		for (BoostSpec s : bu.boosters().all()) {
			if (!s.booster.isMul && RD.DEFS().get(s.boostable, s.booster) != null && !s.booster.has(Faction.class)) {
				continue;
			}
			global |= s.booster.has(Faction.class);
			local |= !global;
		}
		
		if (local) {
			b.add(b.text().lablify().add(DicMisc.¤¤Effects).add(':').s().add(DicGeo.¤¤Region));
			b.NL();
			for (BoostSpec s : bu.boosters().all()) {
				if (!s.booster.isMul && RD.DEFS().get(s.boostable, s.booster) != null && !s.booster.has(Faction.class)) {
					continue;
				}
				if (!s.booster.has(Faction.class)) {
					bu.boosters().hover(b, s, getB(bu, fromL, toL, s), 0);
					b.NL();
				}
			}
		}
		
		
		if (global) {
			b.add(b.text().lablify().add(DicMisc.¤¤Effects).add(':').s().add(DicGeo.¤¤Realm));
			b.NL();
			for (BoostSpec s : bu.boosters().all()) {
				if (!s.booster.isMul && RD.DEFS().get(s.boostable, s.booster) != null && !s.booster.has(Faction.class)) {
					continue;
				}
				if (s.booster.has(Faction.class)) {
					bu.boosters().hover(b, s, getB(bu, fromL, toL, s), 0);
					b.NL();
				}
			}
		}
	}
	
	private static void hoverCosts(Region reg, RDBuilding bu, int fromL, int toL, GUI_BOX text) {
		GBox b = (GBox) text;
		
		b.NL(8);
		b.textLL(DicMisc.¤¤Cost);
		b.NL();
		
		int cr =  credits(bu, fromL, toL);
		if (cr > 0) {
			hoverCost(text, UI.icons().s.money, DicRes.¤¤Curr, -cr, (int)FACTIONS.player().credits().getD());
			b.NL();
		}
		
		for (BoostSpec s : bu.boosters().all()) {
			
			if (!s.booster.isMul && RD.DEFS().get(s.boostable, s.booster) != null && !s.booster.has(Faction.class)) {
				double value = getB(bu, fromL, toL, s);
				
				
				hoverCost(text, s.boostable.icon, s.boostable.name, value, s.boostable.get(reg));
				b.NL();
			}
		}
	}
	
	private static void hoverCost(GUI_BOX text, SPRITE icon, CharSequence name, double value, double current) {
	
		if (value == 0)
			return;
		GBox b = (GBox) text;
	
		b.add(icon);
		GText nn = b.text();
		GText vv = b.text();
		nn.normalify2();
		vv.normalify2();
		nn.add(name);
		GFORMAT.iOrF(vv, value);
		if (value > 0) {
			nn.normalify();
			vv.normalify();
		}else if (current  < -value) {
			nn.errorify();
			vv.errorify();
		}else {
			nn.normalify2();
			vv.normalify2();
		}
		
		b.add(nn);
		b.tab(7);
		b.add(vv);
		b.tab(9);
		GText cc = b.text();
		cc.add('(');
		GFORMAT.iOrF(cc, current).add(')');
		b.add(cc);
		b.NL();
	}
	
	private static double getB(RDBuilding bu, int fromL, int toL, BoostSpec spec) {
		double am = 0;
		RDBuildingLevel l = bu.levels.get(toL);
		boolean mm = spec.booster.isMul;
		for (BoostSpec boo : l.local.all()) {
			if (spec.isSameAs(boo)) {
				am += boo.booster.to();
				if (mm)
					am -= 1;
			}
		}
		l = bu.levels.get(fromL);
		for (BoostSpec boo : l.local.all()) {
			
			if (spec.isSameAs(boo)) {
				am -= boo.booster.to();
				if (mm)
					am += 1;
			}
		}
		if (mm)
			am += 1;
		return am;
	}
	
	private double efficiency(RDBuilding bu) {
		double mul = 1;
		for (BoostSpec f : bu.baseFactors) {
			mul*= f.get(g.get());
		}
		return mul;
	}
	
	private static int credits(RDBuilding bu, int fromL, int toL) {
		int cost = bu.levels.get(toL).cost-bu.levels.get(fromL).cost;
		return cost;
	}
	
	private class BuildingButt extends ClickableAbs{
		
		private final RDBuilding bu;
		private final GuiSection lPop = new GuiSection();
		
		BuildingButt(RDBuilding b){
			body.setDim(width, height);
			this.bu = b;
			
			for (int i = bu.levels().size()-1; i >= 0; i--) {
				lPop.addDown(0, new LevelButt(b, i));
			}
			
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			

			Region reg = g.get();
			ColorImp cc = ColorImp.TMP;
			cc.set(bu.cat.color);
			cc.render(r, body);
			cc.shadeSelf(0.5);
			cc.renderFrame(r, body, 0, 1);
			cc.renderFrame(r, body, -3, 1);
			
			Rec.TEMP.setDim(body.width()-4, body.height()-4);
			Rec.TEMP.moveC(body.cX(), body.cY());
			GButt.ButtPanel.renderBG(r, isActive, isSelected, isHovered, Rec.TEMP);
			
			int tl = bu.level.get(reg);
			
			if (tl < 1)
				tl = 1;
			bu.levels().get(tl).icon.huge.renderC(r, body.cX(), body.cY()+2);

			num.clear();
			GFORMAT.toNumeral(num, tl);
			num.adjustWidth();
			
			OPACITY.O75.bind();
			num.color(COLOR.BLACK);
			num.renderC(r, body.cX()+1, body.y1()+14+1);
			COLOR col = buCols[(int) ((double)(buCols.length-1)*bu.level.get(reg)/((bu.levels().size()-1)))];
			num.color(col);
			OPACITY.unbind();
			num.renderC(r, body.cX(), body.y1()+14);
//			
			
			if (!bu.level.isMax(reg)) {
				if (bu.canAfford(reg, bu.level.get(reg)+1))
					COLOR.YELLOW100.bind();
				else
					COLOR.WHITE50.bind();
				UI.icons().s.chevron(DIR.N).renderC(r, body().cX(), body().y1()+4);
				COLOR.unbind();
			}
			
			renderEfficiency(bu, body, r, efficiency(bu));
			GButt.ButtPanel.renderFrame(r, isActive, isSelected, isHovered, body);
			

			
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			Region reg = g.get();
			GBox b = (GBox) text;
			RDBuildingLevel l = bu.levels().get(bu.level.get(reg));
			b.title(l.name);
			b.text(bu.info.desc);
			b.sep();
			
			double mul = bu.efficiency.get(reg);
			
			if (mul != 1) {
				bu.efficiency.hover(b, reg, DicMisc.¤¤Efficiency, true);
				b.sep();
			}
			
			bu.boosters().hover(text, reg);
			
			
			
		}
		
		@Override
		protected void clickA() {
			VIEW.inters().popup.show(lPop, this);
//			
//			if (bu.level.isMax(reg))
//				bu.level.set(reg, 0);
//			else
//				bu.level.inc(reg, 1);
		}
		
	}
	
	private class LevelButt extends ClickableAbs{
		
		private final RDBuilding bu;
		private final int level;
		
		LevelButt(RDBuilding b, int level){
			body.setDim(128, 40);
			this.bu = b;
			this.level = level;
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			Region reg = g.get();
			GCOLOR.UI().border().render(r, body,-1);
			
			if (bu.level.get(reg) == level) {
				COLOR.WHITE100.render(r, body,-2);
				GCOLOR.UI().bg(isActive, isSelected, isHovered).render(r, body,-4);
			}else {
				GCOLOR.UI().bg(isActive, isSelected, isHovered).render(r, body,-2);
			}
			
			bu.levels().get(level).icon.big.renderCY(r, body().x1()+8, body().cY());
			num.clear();
			num.color(COLOR.WHITE100);
			GFORMAT.toNumeral(num, level);
			num.renderCY(r, body().x1()+48, body.cY());
			
			if (level > bu.level.get(reg)) {
				if (!bu.canAfford(reg, level)) {
					OPACITY.O50.bind();
					COLOR.BLACK.render(r, body, -1);
					OPACITY.unbind();
				}
			}
			
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {

			Region reg = g.get();
			GBox b = (GBox) text;
			b.title(bu.levels().get(level).name);
			if (level == 0)
				return;
			b.text(bu.info.desc);
			
			b.sep();
			if (bu.levels().get(level).reqs.hover(text, reg))
				b.sep();
			
			
			hoverCosts(reg, bu, bu.level.get(reg), level, text);
			hoverNonCosts(reg, bu, bu.level.get(reg), level, text);
			
		}
		
		@Override
		protected void clickA() {
			
			if (S.get().developer || bu.canAfford(g.get(), level)) {
				if (g.get().faction() == FACTIONS.player())
					FACTIONS.player().credits().inc(-credits(bu, bu.level.get(g.get()), level), CTYPE.CONSTRUCTION);
				bu.level.set(g.get(), level);
//				if (level < bu.level.get(g.get()))
//					bu.level.set(g.get(), level);
				VIEW.inters().popup.close();
				
			}
			
			
//			if (bu.level.isMax(reg))
//				bu.level.set(reg, 0);
//			else
//				bu.level.inc(reg, 1);
		}
		
		
	}
	
	private void renderEfficiency(RDBuilding bu, RECTANGLE body, SPRITE_RENDERER r, double d) {
		d -= 1;
		int am = (int) (d*8);
		am = CLAMP.i(am, -7, 7);
		if (am != 0) {
			am = Math.abs(am);
			SPRITE s = UI.icons().s.chevron(DIR.N);
			if (d<0) {
				COLOR.RED100.bind();
				s = UI.icons().s.chevron(DIR.S);
			}else {
				COLOR.GREEN100.bind();
			}
			for (int i = 0; i < am; i++) {
				s.render(r, body.x2()-18, body.y1()+i*8);
			}
			
		}
	}
	
	static final class Info extends GuiSection {
		
		private final GETTER_IMP<Region> g;
		
		Info(GETTER_IMP<Region> g, int WIDTH) {
			this.g = g;
			int i = 0;
			int cols = 5;
			int width = 110;
			int height = 28;
			DIR align = DIR.W;
			
			addGridD(boost(RD.BUILDINGS().points, UI.icons().m.workshop), i++, cols, width, height, align);
			addGridD(boost(RD.RACES().workforce, UI.icons().m.stength), i++, cols, width, height, align);
			addGridD(boost(RD.ADMIN().boost, UI.icons().m.admin), i++, cols, width, height, align);
			
			ACTION a = new ACTION() {

				@Override
				public void exe() {
					for (RDBuilding b : RD.BUILDINGS().all) {
						b.level.set(g.get(), 0);
						//<b.targetLevel.set(g.get(), 0);
					}
				}
				
			};
			
			CLICKABLE b = new GButt.ButtPanel(UI.icons().s.cancel) {
				
				@Override
				protected void clickA() {
					VIEW.inters().yesNo.activate(¤¤RemoveAll, a, ACTION.NOP, true);
					super.clickA();
				}
				
			}.hoverInfoSet(¤¤RemoveAll);
			
			addGridD(b, i++, cols, width, height, align);
			
			pad((WIDTH-body().width())/2, 0);
			
		
		}
		
		private RENDEROBJ boost(Boostable bo, SPRITE icon) {
			return new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.iIncr(text, (int)bo.get(g.get()));
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					b.title(bo.name);
					b.text(bo.desc);
					b.sep();
					bo.hover(b, g.get(), null, true);
					
					
				};
				
			}.hh(icon);
		}
		
	}
	
}
