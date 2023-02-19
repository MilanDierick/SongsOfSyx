package settlement.stats;

import init.D;
import snake2d.util.sets.*;
import util.info.INFO;

public final class CAUSE_ARRIVE extends INFO implements INDEXED{

	private static LISTE<CAUSE_ARRIVE> all = new LinkedList<CAUSE_ARRIVE>();
	
	static {
		D.spush(CAUSE_ARRIVE.class);
	}
	
	public static final CAUSE_ARRIVE BORN = new CAUSE_ARRIVE(
			D.g("Born"),
			D.g("BornD", "Subjects that have been born in your city."),
			false
			); 
	
	public static final CAUSE_ARRIVE IMMIGRATED = new CAUSE_ARRIVE(
			D.g("Immigrated"),
			D.g("ImmigratedD", "Subjects that have immigrated to your city."),
			true
			); 
	
	public static final CAUSE_ARRIVE EMANCIPATED = new CAUSE_ARRIVE(
			D.g("Emancipated"),
			D.g("EmancipatedD", "Subjects that are freed slaves."),
			false
			); 
	
	public static final CAUSE_ARRIVE PAROLE = new CAUSE_ARRIVE(
			D.g("Parole"),
			D.g("ParoleD", "Subjects that have been prisoners and are now pardoned and free citizens."),
			false
			); 
	
	public static final CAUSE_ARRIVE SOLDIER_RETURN = new CAUSE_ARRIVE(
			D.g("Soldiers"),
			D.g("SoldiersD", "Soldiers that have returned from campaigning."),
			true
			); 
	
	public static final CAUSE_ARRIVE CURED = new CAUSE_ARRIVE(
			D.g("Readjusted"),
			D.g("ReadjustedD", "Subjects that have been readjusted in the asylum and cured of insanity."),
			false
			); 

	static {
		all = new ArrayList<>(all);
		D.spop();
	}
	
	public static LIST<CAUSE_ARRIVE> ALL(){
		return all;
	}
	
	private final int index;
	public boolean fromoutside;
	
	private CAUSE_ARRIVE(CharSequence name, CharSequence desc, boolean fromOutside) {
		super(name, desc);
		index = all.add(this);
		this.fromoutside = fromOutside;
	}

	@Override
	public int index() {
		return index;
	}
	
	
}
