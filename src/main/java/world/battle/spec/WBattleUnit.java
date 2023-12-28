package world.battle.spec;

import snake2d.util.gui.GUI_BOX;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;

public abstract class WBattleUnit {


	public final Str name = new Str(24);
	public int men;
	public int losses;
	public int lossesRetreat;
	public SPRITE icon;
	
	public abstract void hover(GUI_BOX box);

}