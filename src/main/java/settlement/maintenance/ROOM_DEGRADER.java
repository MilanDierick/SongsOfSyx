package settlement.maintenance;

import init.resources.RESOURCE;

public abstract class ROOM_DEGRADER{


	private final static double degI = 1.0/MRoom.degradeReal.mask;
	
	/**
	 * three bytes
	 * @return
	 */
	public abstract int getData();
	
	/**
	 * three bytes
	 * @param v
	 * @return
	 */
	protected abstract void setData(int v);
	
	public double get() {
		return get(getData());
	}
	
	public static double get(int data) {
		if (MRoom.negative.is(data)) {
			return 0;
		}
		return MRoom.degradeReal.get(data)*degI;
	}
	
	public abstract int resSize();
	public abstract int resAmount(int i);
	public abstract RESOURCE res(int i);
	public abstract int roomArea();
	public abstract double baseRate();
	public abstract double expenseRate();
	public abstract double degRate();
	
}
