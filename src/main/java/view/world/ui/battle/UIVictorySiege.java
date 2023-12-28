package view.world.ui.battle;

import game.faction.FACTIONS;
import init.D;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import util.colors.GCOLOR;
import util.data.DOUBLE;
import util.data.DOUBLE.DoubleImp;
import util.dic.DicArmy;
import util.gui.misc.*;
import world.battle.spec.WBattleSiege;

class UIVictorySiege extends GuiSection{


	private static CharSequence ¤¤victoryD = "¤The gods have smiled upon your name. Victory is ours and our foe has been beaten.";
	private static CharSequence ¤¤Mercy = "¤Mercy";
	private static CharSequence ¤¤mercyD = "¤Show mercy. Neither plunder, nor slaves will be had, but the region will have a high opinion of you.";
	private static CharSequence ¤¤Sack = "¤Sack";
	private static CharSequence ¤¤sackD = "¤Let your men have some fun in the conquered settlement by sacking and killing those who get in the way.";
	private static CharSequence ¤¤Raze = "¤Raze";
	private static CharSequence ¤¤RazeD = "¤Spare none, leave no stone unturned and teach this settlement a lesson that will be remembered for generations.";
	
	private static CharSequence ¤¤Occupy = "¤Occupy";
	private static CharSequence ¤¤Abandon = "¤Abandon";
	private static CharSequence ¤¤Puppet = "¤Puppet";
	
	private static CharSequence ¤¤OccupyD = "¤Take full control of this region.";
	private static CharSequence ¤¤AbandonD = "¤Let this settlement find its future on its own.";
	private static CharSequence ¤¤PuppetP = "¤Currently, there are no nobles available that can take on the job of governing the region.";
	private static CharSequence ¤¤PuppetD = "¤Install a puppet regime. A new faction will be created, which will be long indebted to you.";
	
	static {
		D.ts(UIVictorySiege.class);
	}

	private final Util.Slaves slaves;
	private final Util.Spoils spoils;
	private final DOUBLE.DoubleImp mul = new DoubleImp();
	
	
	UIVictorySiege(ACTION close, WBattleSiege.Result result){
		
		
		add(new GHeader(DicArmy.¤¤Victory));
		
		{
			GText t = new GText(UI.FONT().M, ¤¤victoryD);
			t.setMaxWidth(Util.width);
			t.lablifySub();	
			addRelBody(4, DIR.S, t);
		}

		addRelBody(16, DIR.S, Util.result(result.player, result.enemy));
		mul.setD(0.4);
		spoils = new Util.Spoils(result.lostResources, mul);
		slaves = new Util.Slaves(result.capturedRaces, mul);
		addRelBody(16, DIR.S, spoils);
		addRelBody(16, DIR.S,  slaves);
		
		{
			GuiSection ss = new GuiSection();
			
			ss.add(new GButt.ButtPanel(¤¤Mercy) {
				
				double m = 0;
				
				@Override
				protected void clickA() {
					mul.setD(m);
				};
				
				@Override
				protected void renAction() {
					selectedSet(mul.getD() == m);
				};
				
			}.setDim(150, 30).hoverInfoSet(¤¤mercyD));
			
			ss.addRightC(2, new GButt.ButtPanel(¤¤Sack) {
				
				double m = 0.4;
				
				@Override
				protected void clickA() {
					mul.setD(m);
				};
				
				@Override
				protected void renAction() {
					selectedSet(mul.getD() == m);
				};
				
			}.setDim(150, 30).hoverInfoSet(¤¤sackD));
			
			ss.addRightC(2, new GButt.ButtPanel(¤¤Raze) {
	
				double m = 0.9;
				
				@Override
				protected void clickA() {
					mul.setD(m);
				};
				
				@Override
				protected void renAction() {
					selectedSet(mul.getD() == m);
				};
			}.setDim(150, 30).hoverInfoSet(¤¤RazeD));
			
			addRelBody(16, DIR.S, ss);
			
			addRelBody(16, DIR.S, new RENDEROBJ.RenderImp(400, 2) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					GCOLOR.UI().border().render(r, body);
				}
			});
			
			{
				GuiSection butts = new GuiSection();
				butts.add(new Util.BButt(FACTIONS.player().banner().MEDIUM, ¤¤Occupy) {
					
					@Override
					protected void clickA() {
						close.exe();
						result.occupy(mul.getD(), slaves.accepted(), spoils.accepted());
					}
					
				}.hoverInfoSet(¤¤OccupyD));
				
				butts.addRightC(0, (new Util.BButt(SPRITES.icons().m.rebellion, ¤¤Abandon) {
					
					@Override
					protected void clickA() {
						close.exe();
						result.abandon(mul.getD(), slaves.accepted(), result.lostResources);
					}
					
				}.hoverInfoSet(¤¤AbandonD)));
				
				butts.addRightC(0, new Util.BButt(SPRITES.icons().m.flag, ¤¤Puppet) {
					
					@Override
					protected void clickA() {
						close.exe();
						result.puppet(mul.getD(), slaves.accepted(), spoils.accepted());
					}
					
					
					
					@Override
					protected void renAction() {
						activeSet(result.canPuppet());
					};
					@Override
					public void hoverInfoGet(snake2d.util.gui.GUI_BOX text) {
						if (!result.canPuppet()) {
							text.text(¤¤PuppetP);
						}else
							super.hoverInfoGet(text);
					};
					
				}.hoverInfoSet(¤¤PuppetD));
				
				addRelBody(8, DIR.S, butts);
			}
			
			
		}
		
	}
	
	
}
