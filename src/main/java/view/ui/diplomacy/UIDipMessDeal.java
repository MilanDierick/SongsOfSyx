package view.ui.diplomacy;

import game.faction.FACTIONS;
import game.faction.diplomacy.Deal;
import game.faction.diplomacy.DealSave;
import game.faction.npc.ruler.ROpinions;
import game.time.TIME;
import init.D;
import init.sprite.UI.UI;
import snake2d.LOG;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import util.dic.DicMisc;
import util.gui.misc.*;
import view.main.VIEW;
import view.ui.diplomacy.UIDipMess.MessIntro;
import view.ui.message.MessageSection;

public final class UIDipMessDeal extends MessageSection{
	
	static CharSequence ¤¤noLonger = "¤This offer is no longer valid.";
	static CharSequence ¤¤accepted = "¤You have accepted this offer.";
	static CharSequence ¤¤declined = "¤You have declined this offer.";
	static CharSequence ¤¤Time = "¤Inform us of you decision within a day.";
	static CharSequence ¤¤DeclineBad = "¤Declining this offer will anger the offering faction.";
	static CharSequence ¤¤DeclineGood = "¤Declining this offer will make the offering faction think more highly of you.";
	private static CharSequence ¤¤Inspect = "¤Inspect Faction.";

	static {
		D.ts(UIDipMessDeal.class);
	}
	private static final long serialVersionUID = 1L;
	private final double power;
	private final double time;
	private final DealSave save;
	private final String desc;
	private boolean accepted;
	private byte aa = 0;
	private final double happiness;
	private final double decline;
	private final MessIntro intro;
	
	public UIDipMessDeal(CharSequence title, CharSequence desc, Deal deal, double happiness, double declineP) {
		super(title);
		power = deal.faction().powerW()/FACTIONS.player().powerW();
		time = TIME.currentSecond();
		save = new DealSave(deal);
		this.desc = ""+desc;
		this.happiness = happiness;
		this.decline = declineP;
		deal.faction().request.set(declineP);
		intro = new MessIntro(deal.faction());
	}
	
	@Override
	protected void make(GuiSection section) {
		
		

		paragraph(desc);

		section.addRelBody(16, DIR.S, new UIDealListSaved(save, 250));
		
		
		
		section.addRelBody(16, DIR.S, new GStat() {

			@Override
			public void update(GText text) {
				if (!pactive()) {
					text.warnify();
					if (aa == -1)
						text.add(¤¤declined);
					else if (aa == 1)
						text.add(¤¤accepted);
					else
						text.add(¤¤noLonger);
					text.setMaxWidth(WIDTH);
				}else {
					text.color(COLOR.WHITE85);
					text.add(¤¤Time);
				}
			}
			
		}.r(DIR.N));
		
		GuiSection s = new GuiSection();
		s.addRightC(0, new GButt.ButtPanel(UI.icons().m.crossair) {
			
			@Override
			protected void renAction() {
				activeSet(save.f() != null);
			}
			
			@Override
			protected void clickA() {
				VIEW.UI().factions.open(save.f());
			}
			
		}.hoverTitleSet(¤¤Inspect));
		s.addRightC(0, new GButt.ButtPanel(DicMisc.¤¤Accept) {
			
			@Override
			protected void renAction() {
				activeSet(pactive());
			}
			
			@Override
			protected void clickA() {
				Deal d = Deal.TMP();
				if (!save.set(d))
					return;
				
				double dv = d.execute();
				ROpinions.makeDeal(d.faction(), -dv);
				save.f().request.clear();
				ROpinions.favour(d.faction(), happiness);
				accepted = true;
				aa = 1;
				
			}
			
		});
		
		s.addRightC(0, new GButt.ButtPanel(DicMisc.¤¤Decline) {
			
			@Override
			protected void renAction() {
				activeSet(pactive());
			}
			
			@Override
			protected void clickA() {
				accepted = true;
				aa = -1;
				save.f().request.expire();
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				if (decline < 0) {
					text.text(¤¤DeclineBad);
				}else if (decline > 0)
					text.text(¤¤DeclineGood);
			}
			
		});
		
		section.addRelBody(8, DIR.S, s);
		
		{
			section.addRelBody(8, DIR.N, intro.make());
		}
	}
	
	private boolean pactive() {
//		LOG.ln(Math.abs(TIME.currentSecond()-time) + " " + Math.abs(power - save.f().powerW()/FACTIONS.player().powerW()));
		if (accepted || aa != 0)
			return false;
		if (Math.abs(TIME.currentSecond()-time) > TIME.secondsPerDay)
			return false;
		if (!save.set(Deal.TMP()))
			return false;
		if (Math.abs(TIME.currentSecond()-time) < 5)
			return true;
		if (Math.abs(power - save.f().powerW()/FACTIONS.player().powerW()) > 0.2)
			return false;
		return true;
	}
	
	
}