package init.resources;

import java.io.Serializable;

public interface STOCKPILE extends Serializable{

	public int get(RESOURCE res);
	
	public class StockpileImp implements STOCKPILE{
		
		private static final long serialVersionUID = 1L;
		private int[] amounts = new int[RESOURCES.ALL().size()];

		@Override
		public int get(RESOURCE res) {
			return amounts[res.bIndex()];
		}
		
		public void set(RESOURCE res, int amount) {
			amounts[res.bIndex()] = amount;
		}
		
		public void add(RESOURCE res, int inc) {
			amounts[res.bIndex()] += inc;
		}
		
		
	}
	
}
