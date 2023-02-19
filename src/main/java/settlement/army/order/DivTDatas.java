package settlement.army.order;

import java.io.IOException;

import init.RES;
import settlement.army.Div;
import snake2d.util.file.*;

public final class DivTDatas implements SAVABLE{

	private final DivTData[] orders = new DivTData[RES.config().BATTLE.DIVISIONS_PER_ARMY*2];
	
	public DivTDatas() {
		for (int i = 0; i < orders.length; i++)
			orders[i] = new DivTData(i);
	}

	@Override
	public void save(FilePutter file) {
		for (int i = 0; i < orders.length; i++)
			orders[i].save(file);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		for (int i = 0; i < orders.length; i++)
			orders[i].load(file);
	}

	@Override
	public void clear() {
		for (int i = 0; i < orders.length; i++)
			orders[i].clear();;
		
	}
	
	public DivTData get(Div div) {
		return orders[div.index()];
	}
	
	public DivTData get(int di) {
		return orders[di];
	}
	
}
