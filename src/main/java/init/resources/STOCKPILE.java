package init.resources;

import java.io.IOException;
import java.util.Arrays;

import snake2d.util.file.*;

public interface STOCKPILE {

	public int get(RESOURCE res);
	public int get(int ri);
	
	public class StockpileImp implements STOCKPILE, SAVABLE{
		
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

		@Override
		public void save(FilePutter file) {
			file.isE(amounts);
		}

		@Override
		public void load(FileGetter file) throws IOException {
			file.isE(amounts);
		}

		@Override
		public void clear() {
			Arrays.fill(amounts, 0);
		}

		@Override
		public int get(int ri) {
			return amounts[ri];
		}
		
		
	}
	
}
