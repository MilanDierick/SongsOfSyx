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

class UIDefeat extends GuiSection{


	private static CharSequence ¤¤DefeatD = "¤A dark day in the annals. The enemy has snatched victory from us.";
	private static CharSequence ¤¤RetreatD = "¤Our army has retreated to fight another day.";
	private static CharSequence ¤¤RetreatDefeat = "¤Our army attempted to retreat, but was destroyed in the process.";
	static {
		D.ts(UIDefeat.class);
	}

	
	UIDefeat(ACTION close, WBattleResult result, boolean retreat){
		
		
		add(new GHeader(DicArmy.¤¤Defeat));
		
		{
			CharSequence desc = ¤¤DefeatD;
			if (retreat) {
				desc = result.player.losses >= result.player.men ? ¤¤RetreatDefeat : ¤¤RetreatD;
			}
			GText t = new GText(UI.FONT().M, desc);
			t.setMaxWidth(Util.width);
			t.lablifySub();	
			addRelBody(4, DIR.S, t);
		}

		addRelBody(16, DIR.S, Util.result(result.player, result.enemy));
		
		addRelBody(16, DIR.S, new Util.Spoils(result.lostResources, new DOUBLE.DoubleImp().setD(1.0)));
		
		
		addRelBody(16, DIR.S, new Util.BButt(SPRITES.icons().m.ok, DicMisc.¤¤Close) {
			
			@Override
			protected void clickA() {
				close.exe();
				result.accept(null, null);
			}
			
		});
		
		
	}
	
	
}
