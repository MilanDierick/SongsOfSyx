package settlement.stats.law;

import java.io.IOException;

import game.GAME;
import init.boostable.BOOSTABLES;
import settlement.entity.humanoid.Humanoid;
import settlement.main.CapitolArea;
import settlement.main.SETT.SettResource;
import settlement.stats.STATS;
import snake2d.LOG;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import view.sett.IDebugPanelSett;

public final class LAW extends SettResource{

	private static LAW self;
	
	{PRISONER_TYPE.init();}
	private final Crimes crimes = new Crimes();
	private final Prisoners prisoners = new Prisoners();
	private final Processing processing = new Processing();
	private final LawRate law = new LawRate();
	private final Curfew curfew = new Curfew();
	public static final double HI_RATE = 1.0/200;
	private double rate = 0;
	private int upI = -1;
	private boolean debug = false;

	public LAW(){
		self = this;
		IDebugPanelSett.add("DEBUG CRIMES", new ACTION() {
			
			@Override
			public void exe() {
				debug = true;
			}
		});
	}
	
	@Override
	protected void save(FilePutter file) {
		crimes.saver.save(file);
		processing.saver.save(file);
		law.saver.save(file);
		curfew.saver.save(file);
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		crimes.saver.load(file);
		processing.saver.load(file);
		law.saver.load(file);
		upI = -1;
		curfew.saver.load(file);
	}
	
	@Override
	protected void clearBeforeGeneration(CapitolArea area) {
		crimes.saver.clear();
		processing.saver.clear();
		law.saver.clear();
		upI = -1;
		curfew.saver.clear();
	}
	
	@Override
	protected void update(float ds) {
		processing.update(ds);
		crimes.update(ds);
		law.update(ds);
		curfew.update(ds);
	}
	
	public static Prisoners prisoners() {
		return self.prisoners;
	}
	
	public static Crimes crimes() {
		return self.crimes;
	}
	
	public static Processing process() {
		return self.processing;
	}

	public static LawRate law() {
		return self.law;
	}
	
	public static Curfew curfew() {
		return self.curfew;
	}
	
	private static double POPI = 1.0/8000;
	private static double RANI = 1.0/0x0FFFFFF;
	
	public static double getCrimeRate(Humanoid a) {
		
		if (self.upI != GAME.updateI()) {
			double pop = ((STATS.POP().POP.data().get(null)-150)*POPI);
			pop = CLAMP.d(pop, 0, 1);
			double rate = HI_RATE*Math.pow(pop, 0.5);
			double law = Math.pow(law().rate().getD(), 0.75);
			
			rate /= 1 + law*20;		
			self.rate = rate;
			
			self.upI = GAME.updateI();
		}
		
		double b = BOOSTABLES.BEHAVIOUR().LAWFULNESS.get(a);
		if (b == 0)
			return HI_RATE;
		
		double rate = self.rate / b;
		double boo = 0.1 + 1.8*(a.indu().randomness2()&0x0FFFFFF)*RANI;
		rate *= boo;
		
		if (self.debug) {
			LOG.ln(a.race() + " " + 10000*rate + " " + 10000*self.rate + " " + BOOSTABLES.BEHAVIOUR().LAWFULNESS.get(a));
			return 1;
		}
		return CLAMP.d(rate, 0, HI_RATE);
		
		
		
	}
	
	
}
