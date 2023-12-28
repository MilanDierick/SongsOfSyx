package view.ui.faction;

import game.faction.diplomacy.Deal;
import game.faction.diplomacy.DealDrawfter;
import game.faction.npc.FactionNPC;
import game.faction.npc.ruler.ROpinions;
import init.settings.S;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.dic.DicGeo;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.info.GFORMAT;
import view.main.VIEW;
import view.ui.diplomacy.UIDealConfig;
import view.ui.diplomacy.UIDealList;

final class Diplomacy extends GuiSection{

	private static CharSequence ¤¤What = "What do you wish to offer us?";
	private static CharSequence ¤¤Barter = "Barter";
	private static CharSequence ¤¤BarterD = "Allow the Faction to compose a deal that they feel comfortable with based on you demands.";
	private static CharSequence ¤¤desc = "The value of a deal is weighed by the faction's perception of the value of its component. A deal needs to have a possible value in order to go through. A high positive value indicate generosity on your part, and will increase the factions opinion of you.";
	private static CharSequence ¤¤Accept = "The deal will be accepted";
	private static CharSequence ¤¤AcceptNo = "The deal will not be accepted";
	private static CharSequence ¤¤OpinionD = "The change of opinion of the factions ruler if this deal is accepted.";
	private static CharSequence ¤¤No = "You have nothing of worth to offer the faction.";
	
	private final Deal deal;
	public final GuiSection section = new GuiSection();
	private double timer = 0;
	
	public Diplomacy(GETTER<FactionNPC> g, Deal deal, int height){
		
		this.deal = new Deal();
		
		addRelBody(0, DIR.S,  new GText(UI.FONT().M, ¤¤What).lablifySub());
		
		GuiSection op = new GuiSection();
		
		{
			op.addRightC(0, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.iIncr(text, (long) (deal.valueCredits()));
				}
			}.hh(DicMisc.¤¤Value).hoverInfoSet(¤¤desc));
			
			op.addRightC(100, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.f0(text, deal.opinionChange());
				}
			}.hh(DicGeo.¤¤Opinion).hoverInfoSet(¤¤OpinionD));
			
			op.addRightC(100, new GButt.ButtPanel(DicMisc.¤¤Accept) {
				
				@Override
				protected void renAction() {
					activeSet(deal.canBeAccepted() || S.get().developer);
				}
				
				@Override
				protected void clickA() {
					if (deal.canBeAccepted() || S.get().developer)
						deal.execute();
					deal.setFactionAndClear(g.get());
					super.clickA();
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					
					GBox b = (GBox) text;
					b.text(¤¤desc);
					
					{
						b.NL(8);
						b.textLL(DicMisc.¤¤Value);
						b.tab(6);
						b.add(UI.icons().s.money);
						b.add(GFORMAT.i(b.text(), (long) (deal.valueCredits())));
					}

					{
						b.NL();
						b.textLL(DicGeo.¤¤Opinion);
						b.tab(6);
						b.add(GFORMAT.f0(b.text(), deal.opinionChange()));
						GText t = b.text();
						t.add('(');
						GFORMAT.f(t, ROpinions.current(deal.faction()));
						t.add(')');
						b.add(t);
					}
					
					
					b.NL(8);
					if (deal.canBeAccepted())
						b.textL(¤¤Accept);
					else
						b.error(¤¤AcceptNo);
				}
				
				
			}).hoverInfoSet(¤¤desc);
			
			op.addRightC(16, new GButt.ButtPanel(¤¤Barter) {
				
				
				private GTextR t = new GText(UI.FONT().M, ¤¤No).warnify().r(DIR.N);
				
				@Override
				protected void clickA() {
					DealDrawfter.draft(deal);
					if (deal.hasDeal() && !deal.canBeAccepted()) {
						timer = 5;
						VIEW.inters().popup.show(t, this);
					}
					super.clickA();
				}
				
				@Override
				protected void renAction() {
					activeSet(deal.hasDeal());
				}
				
			}).hoverInfoSet(¤¤BarterD);
		}
		
		int h = height-body().height()-op.body().height()-16;
		
		
		
		GuiSection s = new GuiSection();
		
		s.add(new UIDealConfig(deal));
		s.addRelBody(16, DIR.E, new SPRITE.Imp(1, h) {
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				GCOLOR.UI().border().render(r, X1, X2, Y1, Y2);
			}
		});
		s.addRelBody(16, DIR.E, new UIDealList(deal, h));
		
		addRelBody(8, DIR.S, s);
		
		addRelBody(8, DIR.S, op);
			
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds){
		
		
		if (timer > 0) {
			timer -= ds;
			if (timer <= 0)
				VIEW.inters().popup.close();
		}
		super.render(r, ds);
	}
	

	public void openPeace(FactionNPC other) {
		deal.setFactionAndClear(other);
		DealDrawfter.draftPeace(deal, other);
	}
	
}
