package init.resources;

public final class Meal {

	private Meal() {
		
	}
	
	public static short make(Edible e, int amount) {
		return (short) (e.index() << 8 | amount);
	}
	
	public static Edible get(short data) {
		return RESOURCES.EDI().all().get((data>>8)&0x0FF);
	}
	
	public static int amount(short data) {
		return data & 0x0FF;
	}
	
}
