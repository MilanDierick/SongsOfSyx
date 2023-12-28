package util.data;

import java.io.IOException;

import snake2d.util.file.*;
import util.info.INFO;

public interface DOUBLE {

	public double getD();

	public interface DOUBLE_MUTABLE extends DOUBLE{
		
		public default DOUBLE_MUTABLE incD(double d) {
			setD(getD()+d);
			return this;
		}
		public DOUBLE_MUTABLE setD(double d);
	}
	
	public default INFO info() {
		return null;
	}
	
	public static class DoubleImp implements DOUBLE_MUTABLE, SAVABLE {
		
		private double d;
		
		@Override
		public double getD() {
			return d;
		}

		@Override
		public void save(FilePutter file) {
			file.d(d);
		}

		@Override
		public void load(FileGetter file) throws IOException {
			d = file.d();
		}

		@Override
		public void clear() {
			d = 0;
		}

		@Override
		public DOUBLE_MUTABLE setD(double d) {
			this.d = d;
			return this;
		}
		
		
	}
	
	
	
}
