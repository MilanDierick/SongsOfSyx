package settlement.army.formation;



import init.C;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

public enum DIV_FORMATION {

	LOOSE(C.TILE_SIZE),
	TIGHT(12*C.SCALE),
	;
	
	public final static LIST<DIV_FORMATION> all = new ArrayList<>(values());
	
	public final int size;
	public final int sizeH;
	
	private DIV_FORMATION(int size) {
		this.size = size;
		this.sizeH = size/2;
	}
}
