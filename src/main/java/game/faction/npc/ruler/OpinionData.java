package game.faction.npc.ruler;

import java.io.IOException;
import java.util.Arrays;

import game.VERSION;
import snake2d.util.file.*;

public final class OpinionData implements SAVABLE{

	final double[] data;
	
	OpinionData(int size){
		data = new double[size];
	}

	@Override
	public void save(FilePutter file) {
		file.dsE(data);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		if (VERSION.versionIsBefore(64, 34)) {
			double[] dd = new double[data.length-2];
			file.dsE(dd);
			for (int i = 0; i < dd.length; i++)
				data[i] = dd[i];
		}else
			file.dsE(data);
	}

	@Override
	public void clear() {
		Arrays.fill(data, 0);
	}
	
}
