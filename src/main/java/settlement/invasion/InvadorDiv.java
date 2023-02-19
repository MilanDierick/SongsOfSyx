package settlement.invasion;

import java.io.IOException;

import init.race.RACES;
import init.race.Race;
import settlement.stats.STATS;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;

public class InvadorDiv {
	
	public int men;
	public Race race;
	public double experience;
	public int[] equipment = new int[STATS.EQUIP().military_all().size()];
	public double trainingM;
	public double trainingR;
	
	public InvadorDiv() {
		
	}
	
	InvadorDiv(FileGetter file) throws IOException{
		men = file.i();
		race = RACES.all().get(file.i());
		experience = file.d();
		file.is(equipment);
		trainingR = file.d();
	}
	
	public void save(FilePutter file) {
		file.i(men);
		file.i(race.index);
		file.d(experience);
		file.is(equipment);
		file.d(trainingM);
	}

	
}