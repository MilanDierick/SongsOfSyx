package settlement.stats.standing;

import java.io.IOException;

import settlement.entity.humanoid.HCLASS;
import settlement.main.CapitolArea;
import settlement.main.SETT.SettResource;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;

public class STANDINGS extends SettResource{

	private static STANDINGS s;
	
	public static void create() {
		s = new STANDINGS();
	}
	
	private final StandingCitizen happiness = new StandingCitizen(); 
	private final StandingSlave submission = new StandingSlave();
	
	private STANDINGS(){
		
	}
	
	public static void initAll() {
		s.happiness.init();
		s.submission.init();
	}
	
	@Override
	protected void save(FilePutter file) {
		happiness.save(file);
		submission.save(file);
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		happiness.load(file);
		submission.load(file);
	}
	
	@Override
	protected void clearBeforeGeneration(CapitolArea area) {
		happiness.clear();
		submission.clear();
	}
	
	@Override
	protected void update(float ds) {
		happiness.update(ds);
		submission.update(ds);
	}
	
	public static Standing get(HCLASS c) {
		if (c == HCLASS.CITIZEN)
			return s.happiness;
		else if (c == HCLASS.SLAVE)
			return s.submission;
		return s.happiness;
	}
	
	public static StandingCitizen CITIZEN() {
		return s.happiness;
	}
	
	public static StandingSlave SLAVE() {
		return s.submission;
	}
	
	
	
	
}
