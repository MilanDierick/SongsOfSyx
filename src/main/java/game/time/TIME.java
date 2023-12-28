package game.time;

import java.io.IOException;
import java.io.Serializable;

import game.GAME.GameResource;
import game.Profiler;
import game.time.Seasons.Season;
import game.time.TIMECYCLE.*;
import init.paths.PATHS;
import snake2d.util.file.*;

public class TIME extends GameResource implements Serializable{

	private static final long serialVersionUID = 1L;

	public final static int secondsPerHour = 48;
	public final static int hoursPerDay = 24;
	public final static int secondsPerDay = secondsPerHour*hoursPerDay;
	public final static double secondsPerDayI = 1.0/secondsPerDay;
	public final static int workHours = 14;
	public final static double workValue = (double)hoursPerDay/workHours;
	
	/**
	 * Approximately how long it will take to walk between adjacent jobs.
	 */
	public final static double workSecondsWalkNext = 3;
	public final static double workSeconds = workHours*secondsPerHour;
//	private final Light light = new Light();
	
	private double currentSecond;
	private double playedGame;
	private double offsetSecond = 0;
	
	private final Hours hours;
	private final Days days;
	private final Seasons seasons;
	private final Years years;
	private final Ages ages;
	private final Light light;
	
	private static TIME t;
	
	public TIME(){
		TIME.t = this;
		
		Json jData = new Json(PATHS.CONFIG().get("Time"));
		Json jText = new Json(PATHS.TEXT_MISC().get("Time"));
		

		hours = new Hours(secondsPerHour, hoursPerDay);
		

		days = new Days((int) hours.cycleSeconds(), jData.i("DAYS_PER_SEASON", 2, 8));

		seasons = new Seasons(days.cycleSeconds(), jData, jText);
		
		
		years = new Years((int) seasons.cycleSeconds(), jData.i("YEARS_PER_AGE"));

		ages = new Ages((int) years.cycleSeconds(), jData, jText);
		
		
		currentSecond += days.bitSeconds()*0.5;
		light = new Light();
		update(0, Profiler.DUMMY);
	}
	
	public void set(double currentSecond){
		this.currentSecond = currentSecond;
		update(0, Profiler.DUMMY);
	}
	
	public static double currentSecond() {
		return t.currentSecond;
	}
	
	public static Hours hours() {
		return t.hours;
	}
	
	public static Days days(){
		return t.days;
	}
	
	public static Season season() {
		return t.seasons.current();
	}
	
	public static Seasons seasons() {
		return t.seasons;
	}
	
	public static Years years(){
		return t.years;
	}
	
	public static Ages age() {
		return t.ages;
	}
	
	public static Light light() {
		return t.light;
	}
	

	
	static double getIncrementedTime(double time) {
		double currentSecond = t.currentSecond;
		currentSecond += time;
		if (currentSecond >= t.ages.cycleSeconds())
			currentSecond -= t.ages.cycleSeconds();
		else if (currentSecond < 0)
			currentSecond += t.ages.cycleSeconds();
		return currentSecond;
		
	}
	
	public double getFertility() {
		return 1.0;
	}
	
	public static int getWorkPerDay(double workSeconds) {
		double walkSpeed = 1.5;
		double toFrom = 150/walkSpeed;
		double workNet = TIME.workSeconds - toFrom;
		
		return (int) Math.ceil(workNet / (workSeconds + 3)); 
		
	}

	@Override
	protected void save(FilePutter file) {
		file.d(currentSecond);
		file.d(offsetSecond);
		file.d(playedGame);
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		currentSecond = file.d();
		offsetSecond = file.d();
		playedGame = file.d();
		update(0, Profiler.DUMMY);
	}

	public static double playedGame() {
		return t.playedGame;
	}
	
	@Override
	protected void update(float ds, Profiler prof) {
		currentSecond += ds;
		playedGame += ds;
		double current = currentSecond + offsetSecond;
		
		while (currentSecond >= ages.cycleSeconds())
			currentSecond -= ages.cycleSeconds();
		while (current >= ages.cycleSeconds())
			current -= ages.cycleSeconds();
		hours.update(current);
		days.update(current);
		seasons.update(current);
		years.update(current);
		ages.update(current);
//		light.calc(sun, ds);
		light.update(ds);
	}
	
}
