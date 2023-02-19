package util.statistics;

import java.io.IOException;

import init.resources.RESOURCE;
import init.resources.RESOURCES;
import snake2d.util.file.*;
import util.data.INT_O.INT_OE;
import util.data.IntObject;

public class IntResource extends IntObject<RESOURCE> implements INT_OE<RESOURCE>, SAVABLE{

	private final int[] is = new int[RESOURCES.ALL().size()];
	
	@Override
	public int get(RESOURCE t) {
		if (t == null) {
			int am = 0;
			for (int i = 0; i < RESOURCES.ALL().size(); i++) {
				am += is[i];
			}
			return am;
		}
		return is[t.bIndex()];
	}

	@Override
	public void save(FilePutter file) {
		file.is(is);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		file.is(is);
	}

	@Override
	public void clear() {
		for (int i = 0; i < is.length; i++)
			is[i] = 0;
	}

	@Override
	protected void setP(RESOURCE t, int i) {
		is[t.bIndex()] = i;
	}

	@Override
	public int min(RESOURCE t) {
		return 0;
	}

	@Override
	public int max(RESOURCE t) {
		return Integer.MAX_VALUE;
	}

}
