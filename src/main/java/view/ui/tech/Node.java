package view.ui.tech;

import game.faction.FACTIONS;
import game.faction.player.PTech;
import init.D;
import init.boostable.BBoost;
import init.boostable.BOOSTABLES;
import init.sprite.ICON;
import init.sprite.SPRITES;
import init.tech.TECH;
import init.tech.TECH.TechRequirement;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.Industry.IndustryResource;
import settlement.room.main.RoomBlueprintImp;
import settlement.tilemap.Floors.Floor;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.*;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.LIST;
import util.colors.GCOLOR;
import util.dic.DicMisc;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;
import view.keyboard.KEYS;
import view.main.VIEW;

final class Node extends GuiSection{

	public final static int WIDTH = 80;
	public final static int HEIGHT = 80;
	public static final COLOR Cdormant = COLOR.WHITE100.shade(0.3);
	public static final COLOR Cunlockable = COLOR.WHITE100.shade(1.0);
	private static final COLOR Cunlocked = COLOR.WHITE100.shade(0.5);
	private static final COLOR Callocated = new ColorImp(0, 70, 70);
	
	private static CharSequence ¤¤Relock = "¤Hold {0} and click to disable this technology. {1} Knowledge will be added to your frozen pool.";
	
	static {
		D.ts(Node.class);
	}
	
	public final TECH tech;
	Node(TECH tech){
		this.tech = tech;
		body().setDim(WIDTH, HEIGHT);
		addC(new Content(tech), body().cX(), body().cY());
		
	}
	
	private class Content extends ClickableAbs{
		


		
		Content(TECH tech){
			body.setDim(56, 56);
		}

		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			
			isHovered |= VIEW.UI().tech.tree.hoverededTechs[tech.index()] && FACTIONS.player().tech.costOfNextWithRequired(tech) <= FACTIONS.player().tech().available().get();
			for (BBoost b : tech.boosts()) {
				if (b.boost == VIEW.UI().tech.tree.hoveredBoost)
					isHovered = true;
			}
			
			col().render(r, body);
			//GCOLOR.UI().border().render(r, body);
			GCOLOR.UI().bg(isActive, isSelected, isHovered).render(r, body,-1);
			COLOR col = col();
			col.render(r, body,-4);
			
			GCOLOR.UI().bg(isActive, isSelected, isHovered).render(r, body,-7);
			
			{
			double levels = tech.levelMax;
			int level = FACTIONS.player().tech.level(tech);
			double d = level/levels;
			int y2 = body().y2()-8;
			int y1 = (int) (y2 - d*(body().height()-16));
				Callocated.render(r, body().x1()+8, body().x2()-8, y1, y2);
			}
			
			if (tech.roomsUnlocks().size() > 0) {
				tech.roomsUnlocks().get(0).iconBig().renderC(r, body().cX(), body().cY());
			}else if (tech.unlocksIndustry().size() > 0) {
				Industry in = tech.unlocksIndustry().get(0);
				in.blue.iconBig().renderC(r, body().cX(), body().cY());
				OPACITY.O50.bind();
				COLOR.BLACK.render(r, body().cX()-10, body().cX()-10+ICON.MEDIUM.SIZE+4, body().cY()-10, body().cY()-10+ICON.MEDIUM.SIZE+4);
				OPACITY.unbind();
				in.outs().get(0).resource.icon().render(r, body().cX()+2-10, body().cY()+2-10);
//				
//				int am = CLAMP.i(in.ins().size(), 0, 4);
//				int x1 = body().cX() - 16*am/2;
//				int y1 = body().cY()+8;
//				for (int i = 0; i < am; i++) {
//					in.ins().get(i).resource.icon().small.render(r, x1, y1);
//					x1 += 16;
//				}
			}else if (tech.unlocksRoads().size() > 0) {
				tech.unlocksRoads().get(0).getIcon().renderC(r, body().cX(), body().cY());
			}else if (tech.unlocksUpgrades().size() > 0) {
				RoomBlueprintImp in = tech.unlocksUpgrades().get(0);
				in.iconBig().nomal.renderC(r, body().cX(), body().cY());
				GCOLOR.UI().SOSO.hovered.bind();
				SPRITES.icons().m.arrow_up.renderC(r, body().cX()+10, body().cY()-10);
				COLOR.unbind();
			}else if(tech.boosts().size() == 1) {
				tech.boosts().get(0).boost.icon().renderC(r, body().cX(), body().cY());
				GCOLOR.UI().GOOD.hovered.bind();
				SPRITES.icons().s.plus.renderC(r, body().cX()+12, body().cY()-12);
				COLOR.unbind();
			}else {
				
				int am = CLAMP.i(tech.boosts().size(), 0, 4);
				int hi = (int) Math.ceil(am/2.0);
				int i = am;
				int y1 = body().cY()-16*hi/2;
				for (int y = 0; y < hi; y++) {
					int rr = CLAMP.i(i, 0, 2);
					
					int x1 = body().cX()-16*rr/2;
					for (int x = 0; x < rr; x++) {
						tech.boosts().get(i-1).boost.icon().render(r, x1, y1);
						x1 += 16;
						i--;
					}
					
					
					
					y1 += 16;
					
				}
				GCOLOR.UI().GOOD.hovered.bind();
				SPRITES.icons().s.plus.renderC(r, body().cX()+12, body().cY()-12);
				COLOR.unbind();
			}
			
			
			
			if (hoveredIs()) {
				
			}else {
				
			}
			
			
			
		}
		
		private COLOR col() {
			if (FACTIONS.player().tech.level(tech) >= tech.levelMax)
				return Cunlocked;
			if  (FACTIONS.player().tech.costOfNextWithRequired(tech) <= FACTIONS.player().tech().available().get())
				return Cunlockable;
			return Cdormant;
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			GBox b = (GBox) text;
			text.title(tech.info.name);
			
			PTech t = FACTIONS.player().tech();
			
			{
				b.textLL(DicMisc.¤¤Level);
				b.add(GFORMAT.iofkNoColor(b.text(), t.level(tech), tech.levelMax));
				b.tab(5);
				b.textLL(DicMisc.¤¤Allocated);
				b.add(SPRITES.icons().s.vial);
				b.add(GFORMAT.iBig(b.text(), t.costTotal(tech)));
				b.NL();
				
				
				
				if (t.level(tech) < tech.levelMax) {
					b.NL(4);
					b.textLL(DicMisc.¤¤Cost);
					b.add(SPRITES.icons().s.vial);
					int c = t.costLevelNext(tech);
					
					if (t.available().get() < c)
						b.add(GFORMAT.iBig(b.text(), c).errorify());
					else
						b.add(GFORMAT.iBig(b.text(), c));
					
					
					b.tab(5);
					b.textLL(DicMisc.¤¤TotalCost);
					b.add(SPRITES.icons().s.vial);
					int ct = t.costOfNextWithRequired(tech);
					if (t.available().get() < ct)
						b.add(GFORMAT.iBig(b.text(), ct).errorify());
					else
						b.add(GFORMAT.iBig(b.text(), ct));
					b.NL(4);
					
					LIST<TechRequirement> rr = tech.requires();
					
					int am = 0;
					for (TechRequirement r : rr)
						if (r.level > 0)
							am++;
					
					if (am > 0) {
						b.textLL(DicMisc.¤¤Requires);
						b.NL();
						for (TechRequirement r : rr) {
							if (r.level <= 0)
								continue;
							GText te = b.text();
							te.add(r.tech.category);
							te.add(':').s();
							te.add(r.tech.info.name);
							if (r.tech.levelMax > 1) {
								te.s().add(r.level);
							}
							if (t.level(r.tech) >= r.level)
								te.normalify2();
							else
								te.errorify();
							b.add(te);
							b.NL();
							
						}
						
					}
					b.NL();
					
				}
				
				
			}
			b.NL(8);
			
			
			
			if (tech.boosts().size() > 0) {
				b.textLL(BOOSTABLES.INFO().name);
				b.tab(6);
				b.textLL(DicMisc.¤¤Current);
				b.tab(9);
				b.textLL(DicMisc.¤¤Next);
				b.NL();
				
				for (BBoost bb : tech.boosts()) {
					b.add(bb.boost.icon());
					b.text(bb.boost.name);
					b.tab(6);
					b.add(GFORMAT.percGood(b.text(), t.level(tech)*bb.value()));
					b.tab(9);
					if (t.level(tech) < tech.levelMax)
						b.add(GFORMAT.percInc(b.text(), bb.value()));
					if (bb.isMul()) {
						b.text(DicMisc.¤¤Multiplies);
					}
					
					b.NL();
				}
				b.NL(8);
			}
			
			if (tech.roomsUnlocks().size() > 0 || tech.unlocksIndustry().size() > 0 || tech.unlocksRoads().size() > 0 || tech.unlocksUpgrades().size() > 0) {
				b.textLL(DicMisc.¤¤Unlocks);
				b.NL();
				for (RoomBlueprintImp bb : tech.roomsUnlocks()) {
					b.add(bb.iconBig().small);
					b.text(bb.info.name);
					b.NL();
				}
				for (Industry bb : tech.unlocksIndustry()) {
					b.add(bb.blue.iconBig().small);
					b.space();
					for (IndustryResource r : bb.ins()) {
						b.add(r.resource.icon());
						b.add(GFORMAT.f0(b.text(), -r.rate));
					}
					b.add(SPRITES.icons().m.arrow_right);
					for (IndustryResource r : bb.outs()) {
						b.add(r.resource.icon());
						b.add(GFORMAT.f0(b.text(), r.rate));
					}
					b.NL();
				}
				for (Floor f : tech.unlocksRoads()) {
					b.add(f.getIcon().nomal);
					b.text(f.name);
				}
				for (RoomBlueprintImp bb : tech.unlocksUpgrades()) {
					b.add(bb.iconBig().small);
					b.add(b.text().add(bb.info.name).s().add('(').add(DicMisc.¤¤Upgrade).add(')'));
					b.NL();
				}
				
				b.NL(8);
			}
			
			text.text(tech.info.desc);
			b.NL(8);
			
			if (t.level(tech) > 0) {
				GText te = b.text();
				te.add(¤¤Relock);
				te.insert(0, KEYS.MAIN().UNDO.repr());
				te.insert(1, t.costLevel(tech, t.level(tech)));
				b.error(te);
			}
			
		}
		
		@Override
		protected void clickA() {
			if (KEYS.MAIN().UNDO.isPressed())
				VIEW.UI().tech.tree.prompt.forget(tech);
			else
				VIEW.UI().tech.tree.prompt.unlock(tech);
			super.clickA();
		}
		
	}
	
}
