package view.world.ui.battle;

import init.D;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.misc.ACTION;
import util.data.DOUBLE;
import util.dic.DicMisc;
import util.gui.misc.GHeader;
import util.gui.misc.GText;
import world.battle.spec.WBattleResult;

class UIVictoryRetreat extends GuiSection{

	private static CharSequence ¤¤title = "¤Enemy Retreats";
	private static CharSequence ¤¤desc = "¤Enemy forces trembled before our might and ran before any engagement. We managed to hunt some down and plunder their baggage train.";
	static {
		D.ts(UIVictoryRetreat.class);
	}

	private final Util.Slaves slaves;
	private final Util.Spoils spoils;
	
	UIVictoryRetreat(ACTION close, WBattleResult result){
		
		add(new GHeader(¤¤title));
		
		{
			GText t = new GText(UI.FONT().M, ¤¤desc);
			t.setMaxWidth(Util.width);
			t.lablifySub();	
			addRelBody(4, DIR.S, t);
		}

		addRelBody(16, DIR.S, Util.result(result.player, result.enemy));
		
		spoils = new Util.Spoils(result.lostResources, new DOUBLE.DoubleImp().setD(1.0));
		addRelBody(16, DIR.S, spoils);
		slaves = new Util.Slaves(result.capturedRaces, new DOUBLE.DoubleImp().setD(1.0));
		
		addRelBody(16, DIR.S,  slaves);
		
		addRelBody(16, DIR.S, new Util.BButt(SPRITES.icons().m.ok, DicMisc.¤¤Accept) {
			
			@Override
			protected void clickA() {
				close.exe();
				result.accept(slaves.accepted(), spoils.accepted());
			}
			
		});
		
		
	}
	
	
}
