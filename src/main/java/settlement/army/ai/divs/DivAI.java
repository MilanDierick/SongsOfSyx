package settlement.army.ai.divs;


import settlement.army.Div;
import settlement.army.order.DivTData;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.INDEXED;
import util.data.DataOL;

class DivAI implements INDEXED{

	protected final Tools t;
	private final int index;
	
	public DivAI(Tools tools, ArrayList<DivAI> all, DataOL<Div> data) {
		this.t = tools;
		index = all.add(this);
	}
	
	@Override
	public int index() {
		return index;
	}
	
	public void update(DivTData order) {
		
	}
	
}
