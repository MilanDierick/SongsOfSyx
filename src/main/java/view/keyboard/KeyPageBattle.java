package view.keyboard;

import init.D;
import snake2d.KEYCODES;
import util.dic.DicArmy;

public class KeyPageBattle extends KeyPage{
	
	KeyPageBattle(){
		super("BATTLE");
	}

	{
		D.gInit(this);
	}
	

	public final Key UP = new Key("UP", D.g("Up"),D.g("UpD", "Move selected Divisions."),this,  KEYCODES.KEY_UP); 
	public final Key DOWN = new Key("DOWN", D.g("Down"), UP.desc,this,  KEYCODES.KEY_DOWN);
	public final Key LEFT = new Key("LEFT", D.g("Left"),UP.desc,this,  KEYCODES.KEY_LEFT); 
	public final Key RIGHT = new Key("RIGHT", D.g("Right"),UP.desc,this, KEYCODES.KEY_RIGHT);
	public final Key SELECT_ALL = new Key("SELECT_ALL", D.g("Select"), D.g("SelectD", "Selects all divisions"),this, KEYCODES.KEY_LEFT_CONTROL, KEYCODES.KEY_SPACE);
	public final Key SHOW_DIVISIONS = new Key("SHOW_DIVISION", D.g("Show"), D.g("ShowD", "Shows all division positions."),this, KEYCODES.KEY_SPACE);
	
	@Override
	public CharSequence name() {
		return DicArmy.¤¤Battle;
	}
	

	
	
}
