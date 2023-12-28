package view.world.ui.battle;

import init.D;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.misc.ACTION;
import util.data.DOUBLE;
import util.dic.DicArmy;
import util.dic.DicMisc;
import util.gui.misc.GHeader;
import util.gui.misc.GText;
import world.battle.spec.WBattleResult;

class UIVictory extends GuiSection{


	private static CharSequence ¤¤victoryD = "¤The gods have smiled upon your name. Victory is ours and our foe has been beaten.";
	static {
		D.ts(UIVictory.class);
	}

	private final Util.Slaves slaves;
	
	UIVictory(ACTION close, WBattleResult result){
		
		add(new GHeader(DicArmy.¤¤Victory));
		
		{
			GText t = new GText(UI.FONT().M, ¤¤victoryD);
			t.setMaxWidth(Util.width);
			t.lablifySub();	
			addRelBody(4, DIR.S, t);
		}

		addRelBody(16, DIR.S, Util.result(result.player, result.enemy));
		addRelBody(16, DIR.S, new Util.Spoils(result.lostResources, new DOUBLE.DoubleImp().setD(1.0)));
		slaves = new Util.Slaves(result.capturedRaces, new DOUBLE.DoubleImp().setD(1.0));
		
		addRelBody(16, DIR.S,  slaves);
		
		addRelBody(16, DIR.S, new Util.BButt(SPRITES.icons().m.ok, DicMisc.¤¤Accept) {
			
			@Override
			protected void clickA() {
				close.exe();
				result.accept(slaves.accepted(), result.lostResources);
			}
			
		});
		
		
	}
	
	
}
