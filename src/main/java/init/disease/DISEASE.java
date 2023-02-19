package init.disease;

import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.Json;
import snake2d.util.sets.INDEXED;
import snake2d.util.sets.LISTE;
import util.info.INFO;

public final class DISEASE implements INDEXED{

	private final int index;
	public final INFO info;
	public final String key;
	public final double fatalityRate;
	public final double spread;
	public final long spreadI;
	public final double occurence;
	public final COLOR color;
	
	DISEASE(LISTE<DISEASE> all, String key, Json data, Json text) {
		index = all.add(this);
		this.key = key;
		info = new INFO(text);
		fatalityRate = data.d("FATALITY_RATE", 0, 1.0);
		spread = data.d("SPREAD", 0, 10);
		spreadI = (long) (spread*Integer.MAX_VALUE);
		occurence = data.d("OCCURENCE", 0, 1);
		color = new ColorImp(data);
	}
	
	@Override
	public int index() {
		return index;
	}
	
	@Override
	public String toString() {
		return key;
	}

}
