package settlement.weather;

import java.io.IOException;

import game.time.Seasons.Season;
import game.time.TIME;
import init.D;
import init.biomes.CLIMATE;
import settlement.main.SETT;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;

public final class WeatherTemp extends WeatherThing{

	private static final double speed = 1.0/TIME.secondsPerDay;
	private double target; 
	private int dayLast = -1;
	
	public static CharSequence ¤¤format = "¤~";
	public static CharSequence ¤¤name = "¤Temperature";
	public static CharSequence ¤¤desc = "¤Temperature is determined by climate, season and chance. Extreme temperatures on either end causes your subjects to be exposed and can lead to death. Hearths warms subjects. Skinny dipping in natural bodies of water cools subjects. Clothes help greatly with both warmth and cold.";
	public static double div = 4.0/60.0;
	
	static {
		D.ts(WeatherTemp.class);
	}
	
	
	WeatherTemp(){
		super(¤¤name, ¤¤desc);

	}
	
	@Override
	protected void update(double ds) {

		if (dayLast != TIME.days().bitsSinceStart()) {
			dayLast = TIME.days().bitsSinceStart();
			target = average(TIME.years().bitPartOf()) + RND.rFloat()*div*RND.rSign();		
		}
		
		double t = target * (TIME.light().nightIs() ? (1.0-div*TIME.light().partOfCircular()) : 1);
		double temperature = adjustTowards(getD(), ds*speed, t);
		temperature = CLAMP.d(temperature, 0, 1.0);
		setD(temperature);
		
		
		
	}
	
	public double average(double partOfYear) {
		
		return average(SETT.ENV().climate(), partOfYear);
	}
	
	public void setTarget(double target) {
		this.target = target;
		dayLast = TIME.days().bitsSinceStart();
	}
	
	private double average(CLIMATE c, double partOfYear) {
		if (partOfYear < 0)
			partOfYear += 1 - (int) partOfYear;
		
		double p = partOfYear*TIME.seasons().ALL.size();
		int si = (int) p;
		p -= si;
		
		Season s = TIME.seasons().ALL.getC(si);
		double startWV = 0;
		double endWV = 0;
		if (p < 0.5) {
			p+= 0.5;
			startWV = TIME.seasons().ALL.getC(si-1).winterValue;
			endWV = s.winterValue;
		}else {
			
			startWV = s.winterValue;
			endWV = TIME.seasons().ALL.getC(si+1).winterValue;
			p-= 0.5;
			
		}
		
		double wv = startWV + p*(endWV-startWV);
		double t = c.tempCold*wv + c.tempWarm*(1.0-wv);
		return t;
	}

	public double heat() {
		if (getD() > 0.5)
			return 2*(getD()-0.5);
		return 0;
	}
	
	public double cold() {
		if (getD() < 0.5) {
			return (0.5-getD())*2;
		}
		return 0;
	}
	
	public double target() {
		if (TIME.light().dayIs()) {
			return CLAMP.d((target + div*TIME.light().partOfCircular()), 0, 1);
		}else
			return CLAMP.d((target - div*TIME.light().partOfCircular()), 0, 1);
	}
	
	public void format(Str srt) {
		if (getD() >= 0.5) {
			srt.add('+');
			srt.add((int)(2*60*(getD()-0.5)));
		}else {
			srt.add('-');
			srt.add((int)(50*2*(0.5-getD())));
		}
		srt.add(¤¤format);
	}

	@Override
	protected void save(FilePutter file) {
		super.save(file);
		file.d(target);
		file.i(dayLast);
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		super.load(file);
		target = file.d();
		dayLast = file.i();
	}
	
	@Override
	protected void init() {
		dayLast = -1;
		update(0);
		
		
		setD(target());
		
	}
	
	private static final double eMin = 1.0/0.625;
	private static final double eMax = 1.0/0.375;
	
	public double getEntityTemp() {
		double d = getD();
		if (d < 0.625) {
			return -(0.625-d)*eMin;
		}else {
			return (d-0.625)/eMax;
		}
	}

	
}