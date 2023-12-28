package world.army;

import java.io.IOException;
import java.util.Arrays;

import init.config.Config;
import snake2d.util.bit.BitsLong;
import snake2d.util.file.*;
import snake2d.util.sprite.text.Str;
import util.dic.DicArmy;
import world.WORLD;
import world.entity.army.WArmy;

public final class WArmyDivs implements SAVABLE{

	private final long[] divs = new long[Config.BATTLE.DIVISIONS_PER_ARMY];
	private int divI;
	public final Str name = new Str(24);
	final long[] data;
	
	static final BitsLong BType = new BitsLong(0xFF_00_00_00_00_00_00_00l);
	
	
	public WArmyDivs(WArmy e) {
		data = new long[AD.self.dataArmyCount];
		clear();
		
	}
	
	@Override
	public void save(FilePutter file) {
		file.ls(divs);
		file.i(divI);
		file.ls(data);
		name.save(file);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		file.ls(divs);
		divI = file.i();
		file.ls(data);
//		if (!VERSION.versionIsBefore(58, 4))
//			file.is(data2);
		name.load(file);
	}

	@Override
	public void clear() {
		divI = 0;
		name.clear().add(DicArmy.¤¤Army);
		Arrays.fill(data, 0);
	}
	
	public boolean canAdd() {
		return divI < divs.length;
	}
	
	public int size() {
		return divI;
	}
	
	public ADDiv get(int i) {
		if (i < 0 || i >= size()) {
			return null;
		}
		
		switch(BType.get(divs[i])) {
			case WDivRegional.type: return WORLD.ARMIES().regional().get((int)divs[i] & 0x0FFFFFFFF);
			case WDivStored.type: return WORLD.ARMIES().cityDivs().get(divs[i]);
			case WDivMercenary.type: return WORLD.ARMIES().mercenaries().get(divs[i]);
			default: throw new RuntimeException();
		}
	}
	
	public void insert(int after, int insert) {
		if (after < 0 || after >= size() || insert < 0 || insert >= size())
			throw new RuntimeException(after + " " + insert);
		if (after == insert)
			return;
		long di = divs[insert];
		for (int i = insert; i < size()-1; i++) {
			divs[i] = divs[i+1];
		}
		if (after > insert)
			after--;
		
		for (int i = size()-1; i > after; i--) {
			divs[i] = divs[i-1];
		}
		divs[after] = di;
	}
	
	void setData(int i, long data) {
		divs[i] = data;
	}
	
	void remove(int i) {
		for (int ii = i; ii < divI-1; ii++)
			divs[ii] = divs[ii+1];
		divI --;
	}
	
	int add() {
		divI ++;
		return divI-1;
	}
	
}
