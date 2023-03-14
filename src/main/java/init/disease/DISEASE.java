package init.disease;

import init.biomes.CLIMATE;
import init.biomes.CLIMATES;
import settlement.main.SETT;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.Json;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.sets.INDEXED;
import snake2d.util.sets.LISTE;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import util.info.INFO;

public final class DISEASE implements INDEXED{

	private final int index;
	public final INFO info;
	public final String key;
	public final double fatalityRate;
	private final double[] occurence;
	public final int length;
	public final COLOR color;
	public final boolean epidemic;
	public final boolean regular;
	
	private static CharSequence ¤¤Lethality = "lethality";
	private static CharSequence ¤¤Length = "Length";
	private static CharSequence ¤¤Occurrence = "Occurrence";
	
	DISEASE(LISTE<DISEASE> all, String key, Json data, Json text) {
		index = all.add(this);
		this.key = key;
		info = new INFO(text);
		fatalityRate = data.d("FATALITY_RATE", 0, 1.0);
		occurence = new double[CLIMATES.ALL().size()];
		CLIMATES.MAP().fill("OCCURRENCE_CLIMATE", occurence, data, 0.00001, 1);
		length = data.i("INFECTION_DAYS", 1, 100);
		epidemic = data.bool("EPIDEMIC");
		regular = data.bool("REGULAR");
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
	
	public void hover(GUI_BOX text) {
		hover(text, SETT.ENV().climate());
	}
	
	public void hover(GUI_BOX text, CLIMATE climate) {
		GBox b = (GBox) text;
		info.hover(b);
		
		b.NL(8);
		b.textLL(¤¤Lethality);
		b.tab(6);
		b.add(GFORMAT.percInv(b.text(), fatalityRate));
		b.NL();
		
		b.textLL(¤¤Length);
		b.tab(6);
		b.add(GFORMAT.fRel(b.text(), length, 2));
		b.NL();
		
		b.textLL(¤¤Occurrence);
		b.tab(6);
		b.add(GFORMAT.percInv(b.text(), occurence[climate.index()]));
		b.NL();
	}
	
	public double occurence(CLIMATE c) {
		return occurence[c.index()];
	}

}
