package settlement.stats.standing;

import java.io.IOException;

import game.time.TIME;
import init.D;
import settlement.entity.humanoid.HCLASS;
import settlement.main.SETT;
import settlement.stats.*;
import settlement.stats.StatsMultipliers.StatMultiplier;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.data.DOUBLE;
import util.info.INFO;
import util.statistics.HistoryInt;
import view.sett.IDebugPanelSett;

public final class StandingSlave extends Standing{

	{D.t(this);}

	private double tar;
	private double cur;
	private double pow;
	private double timer;
	private final static double upI = 4;
	
	public final DOUBLE targetSubmission = new DOUBLE() {
		
		private final INFO info = new INFO(
				D.g("Target", "Target Submission"),
				D.g("TargetD", "What value your slave submission is moving towards.")
				);

		
		@Override
		public double getD() {
			return tar;
		}
		
		@Override
		public INFO info() {
			return info;
		}
	};
	

	
	public final HistoryInt submission = new SlaveThing( 
			D.g("Submission", "Submission"),
			D.g("SubmissionD", "The submission of your slaves. Low submission can lead to uprisings. Submission is gained by providing fulfillment for your slaves and keeping their numbers down relative to your citizen population. Different species also have different submission properties.")
			);
	
	private final SlaveFactor army = new SlaveFactor( 
			1,
			D.g("Army", "Army"),
			D.g("ArmyD", "The size of your army. This size greatly deters any potential uprisings.")
			);
	
	private final SlaveFactor fullfillment = new SlaveFactor( 
			2, 
			D.g("Fulfillment", "Fulfillment"),
			D.g("FulfillmentD", "Slaves wants some comfort, same as the next person. In order to keep your slaves submissive, you must provide some basic services for them.")
			);
	
	public final SlaveFactor advantage = new SlaveFactor(
			1,
			D.g("Numbers"),
			D.g("NumbersD", "The amount of slaves contra your citizen population. The more slaves, the more hope for a successful uprising.")
			);
	
	public final LIST<SlaveFactor> factors = new ArrayList<>(advantage, army, fullfillment);
	
	private final static HCLASS cl = HCLASS.SLAVE;
	
	StandingSlave(){

		IDebugPanelSett.add("submission++", new ACTION() {
			
			@Override
			public void exe() {
				submission.inc(10);
			}
		});
		
	}
	
	void init() {
		timer = -upI;
		update(0);
		cur = tar;
		submission.setD(cur);
	}
	
	void update(double ds) {
		
		timer -= ds;
		if (timer < 0){
			timer += upI;
		}else{
			return;
		}
		
		
		update();
		
		int t = (int) (tar*100);
		int c = (int) (cur*100);
		double d = t-c;
		
		d *= upI/(100.0*TIME.secondsPerDay);

		cur = cur+d;
		if (d < 0 && cur < tar)
			cur = tar;
		else if(d > 0 && cur > tar)
			cur = tar;
		
		cur = CLAMP.d(cur, 0, 1);
		submission.set((int)(100*cur));
		
		pow = Math.pow(cur, 0.5);
	}
	
	public void update() {
		{
			double ful = 0;
			double max = 0;
			double def = 0;
			
			for (STAT s : STATS.all()) {
				ful += s.standing().get(cl, null);
				max += s.standing().max(cl, null);
				def +=  s.standing().def(cl, null);
			}
			if (ful < def) {
				ful = ful/def;
			}else {
				ful-= def;
				max-= def;
				ful /= max;
				ful = Math.pow(ful, 1.5);
				ful*= 3;
				ful += 1;
				
			}
			
			fullfillment.setD(ful);
			
		}
		
		{
			double num = numbersMul(STATS.POP().POP.data(cl).get(null), STATS.POP().POP.data(HCLASS.CITIZEN).get(null));
			advantage.setD(num);
		}
		
		army.setD(1.0 + SETT.ARMIES().player().men()/(STATS.POP().POP.data(cl).get(null)+1.0));
		
		{
			tar = 1.0;
			for (SlaveFactor f : factors) {
				tar *= f.getD();
			}
			for (StatMultiplier m : STATS.MULTIPLIERS().get(HCLASS.SLAVE))
				tar *= m.multiplier(cl, null, 0);
			tar = CLAMP.d(tar, 0, 1);
			
		}
	}
	
	public double numbersMul(double slaves, double citizens) {
		double po = slaves + (citizens+1);
		slaves /= po;
		slaves = 1.0-slaves;
		return slaves;
	}
	
	@Override
	void save(FilePutter file) {
		file.d(cur);
		file.d(tar);
		submission.save(file);
		for (SlaveFactor f : factors)
			f.save(file);
		
	}
	
	@Override
	void load(FileGetter file) throws IOException {
		cur = file.d();
		tar = file.d();
		submission.load(file);
		for (SlaveFactor f : factors)
			f.load(file);
		
	}
	
	@Override
	void clear() {
		cur = 0;
		tar = 0;
		submission.clear();
		for (SlaveFactor f : factors)
			f.clear();;
	}

	@Override
	public double current(Induvidual a) {
		
		double ful = 0;
		{
			double max = 0;
			double def = 0;
			
			for (STAT s : STATS.all()) {
				ful +=  s.standing().get(a);
				max += s.standing().max(cl, a.race());
				def +=  s.standing().def(cl, a.race());
			}
			
			if (ful < def) {
				ful = ful/def;
			}else {
				ful-= def;
				max-= def;
				ful /= max;
				ful = Math.pow(ful, 1.5);
				ful += 1;
			}
		}
		
		{
		
			for (int k = 1; k < factors.size(); k++)
				ful *= factors.get(k).getD();
			
			
		}
		
		for (StatMultiplier m : STATS.MULTIPLIERS().get(HCLASS.SLAVE))
			ful *= m.multiplier(cl, null, 0);
		ful = CLAMP.d(ful, 0, 1);
		return ful;
	}

	@Override
	public double current() {
		return submission.getD();
	}
	
	public double currentPow() {
		return pow;
	}

	@Override
	public double target() {
		return tar;
	}
	
	@Override
	public INFO info() {
		return submission.info();
	}
	
	private class SlaveThing extends HistoryInt {

		public SlaveThing(CharSequence name, CharSequence desc) {
			super(name, desc, STATS.DAYS_SAVED, TIME.days(), true);
		}
		
		@Override
		public int min() {
			return 0;
		}
		
		@Override
		public int max() {
			return 100;
		}
		
	}
	
	public class SlaveFactor extends HistoryInt {
		
		private SlaveFactor(double weight, CharSequence name, CharSequence desc) {
			super(name, desc, STATS.DAYS_SAVED, TIME.days(), true);
		}
		
		@Override
		public double getD(int fromZero) {
			return get(fromZero)/1000.0;
		}
		
		@Override
		public DOUBLE_MUTABLE setD(double d) {
			super.set((int)(d*1000));
			return this;
		}
		
	}




	
}
