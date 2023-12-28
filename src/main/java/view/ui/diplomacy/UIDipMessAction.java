package view.ui.diplomacy;

import game.faction.npc.FactionNPC;
import game.faction.npc.ruler.ROpinions;
import game.time.TIME;
import init.sprite.UI.UI;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import util.dic.DicMisc;
import util.gui.misc.*;
import view.ui.diplomacy.UIDipMess.MessFaction;
import view.ui.diplomacy.UIDipMess.MessIntro;
import view.ui.message.MessageSection;

public abstract class UIDipMessAction extends MessageSection{

	private static final long serialVersionUID = 1L;
	private final double time;
	private final String desc;
	private final String req;
	private boolean accepted;
	private byte aa;
	private final double declinePenalty;
	private final double happiness;
	private final MessIntro intro;	
	private final MessFaction of;
	
	public UIDipMessAction(CharSequence title, CharSequence desc, CharSequence req, FactionNPC f, FactionNPC o, double happiness, double decline) {
		super(title);
		time = TIME.currentSecond();
		this.desc = ""+desc;
		this.req = ""+req;
		of = new MessFaction(o);
		this.declinePenalty = decline;
		this.happiness = happiness;
		f.request.set(decline);
		intro = new MessIntro(f);
	}
	
	@Override
	protected void make(GuiSection section) {
		
		
		paragraph(desc);
		
		section.addRelBody(8, DIR.S, new GText(UI.FONT().M, req).lablifySub().setMaxWidth(WIDTH));
		
		section.addRelBody(8, DIR.S, new GText(UI.FONT().S, UIDipMessDeal.¤¤Time).color(COLOR.WHITE85).setMaxWidth(WIDTH));
		


		
		GuiSection s = new GuiSection();
		s.addRightC(0, new GButt.ButtPanel(DicMisc.¤¤Accept) {
			
			@Override
			protected void renAction() {
				activeSet(pactive());
			}
			
			@Override
			protected void clickA() {
				accepted = true;
				ROpinions.favour(intro.faction(), happiness);
				accept(intro.faction(), of.faction());
				close();
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
				intro.faction().request.expire();
				close();
				aa = -1;
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				if (declinePenalty < 0) {
					text.text(UIDipMessDeal.¤¤DeclineBad);
				}else if (declinePenalty > 0)
					text.text(UIDipMessDeal.¤¤DeclineGood);
			}
			
		});
		
		section.addRelBody(8, DIR.S, s);
		
		section.addRelBody(16, DIR.S, new GStat() {

			@Override
			public void update(GText text) {
				if (!pactive()) {
					text.warnify();
					if (aa == -1)
						text.add(UIDipMessDeal.¤¤declined);
					else if (aa == 1)
						text.add(UIDipMessDeal.¤¤accepted);
					else
						text.add(UIDipMessDeal.¤¤noLonger);
					text.setMaxWidth(WIDTH);
				}
			}
			
		}.r(DIR.N));
		
		section.addRelBody(8, DIR.N, intro.make());
	}
	
	private boolean pactive() {
		if (accepted)
			return false;
		if (Math.abs(TIME.currentSecond()-time) > TIME.secondsPerDay)
			return false;
		if (intro.faction() == null || of.faction() == null)
			return false;
		return valid(intro.faction(), of.faction());
	}
	
	protected abstract void accept(FactionNPC f, FactionNPC o);
	protected abstract boolean valid(FactionNPC f, FactionNPC o);
}