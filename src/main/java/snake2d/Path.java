package snake2d;

import java.io.IOException;

import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.*;
import snake2d.util.misc.CLAMP;

public interface Path extends COORDINATE, SAVABLE {
	
	/**
	 * 
	 * @return - the number of coordinates this path can store 
	 */
	public int getCapacity();
	
	/**
	 * 
	 * @return - the total cost of this path, calculated by costmethod.
	 */
	public double getTotalCost();
	
	/**
	 * 
	 * @return length of current path
	 */
	public int length();
	
	/**
	 * 
	 * @return - is the current coordinate the start of this path?
	 */
	public boolean isStart();
	
	/**
	 * set current coordinate to start coordinates
	 */
	public void setStart();

	/**
	 * 
	 * @return is current coordinate the destination?
	 */
	public boolean isDest();
	
	/**
	 * 
	 * @return is there a next coordinate on this path?
	 */
	public boolean hasNext();
	
	/**
	 * set the next coordinate
	 * @return there was another coordinate
	 */
	public boolean setNext();

	/**
	 * 
	 * @return count of this tile in this path
	 */
	public int getCurrentI();
	
	/**
	 * set current tile in path
	 */
	public void setCurrentI(int i);
	
	/**
	 * 
	 * @return is there a previous coordinate?
	 */
	public boolean hasPrev();
	
	/**
	 * set the previous coordinate
	 * @return there was previous coordinate
	 */
	public boolean setPrev();
	
	/**
	 * 
	 * @return did the actual path fit into this path's size? If not, you only got a partial
	 * path
	 */
	public boolean isCompleate();
	
	public int lengthTotal();

	public interface COST {

		public final static double BLOCKED = -1f;
		public final static double SKIP = -2f;
		
		/**
		 * 
		 * @param fromX
		 * @param fromY
		 * @param toX
		 * @param toY
		 * @return 
		 * -1 if tile not passible, 
		 * -2 if tile should be skipped,
		 *  else the cost multiplier
		 */
		double getCost(int fromX, int fromY, int toX, int toY);
		
	}
	
	public static abstract class DEST {

		protected abstract boolean isDest(int x, int y);
		protected abstract float getOptDistance(int x, int y);
		
		public static abstract class CLOSEST extends DEST{

			@Override
			protected final float getOptDistance(int x, int y) {
				return 0;
			}

		}
		
	}
	
	public static class Async extends PathSync {

		private volatile boolean undergoingProcessing = false;
		
		/**
		 * 
		 * @param size
		 */
		public Async(int size){
			super(size);
		}
		
		/**
		 * 
		 * @param success - path was found
		 */
		protected void pathCalculated(boolean success) {
			
		}
		
		final void lock(){
			undergoingProcessing = true;
		}
		
		final void unlock() {
			undergoingProcessing = false;
		}
		
		/**
		 * 
		 * @return is this path being processed by another thread? i.e. don't touch it?
		 */
		public final boolean isBusy(){
			return undergoingProcessing;
		}

	}
	
	public static class PathSync implements Path{

		private final static int bitA = 2;
		private final static int tilesPerInt = 8;
		private final static long mask = 0x0000000000000003;
		private final static long maskI = 0x00000000FFFFFFFC;
		
		private final int[] bits;
		private int length;
		private int tilesI = 0;
		private boolean compleate;
		private short currentX,currentY;
		private double totalCost;
		private int totalLength;
		
		/**
		 * 
		 * @param size
		 */
		public PathSync(int size){
			
			int ints = size/tilesPerInt;
			if (size % tilesPerInt > 0)
				ints++;
			this.bits = new int[ints];

		}
		
		@Override
		public void save(FilePutter file) {
			file.is(bits);
			file.i(length);
			file.i(tilesI);
			file.bool(compleate);
			file.i(currentX).i(currentY);
			file.d(totalCost);
			file.i(totalLength);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			file.is(bits);
			
			length = file.i();
			tilesI = file.i();
			compleate = file.bool();
			currentX = (short) file.i(); 
			currentY = (short) file.i();
			totalCost  = file.d();
			totalLength  = file.i();
		}
		
		@Override
		public void clear() {
			tilesI = 0;
			compleate = false;
			length = 0;
		}
		
		public void reverse() {
			while(hasPrev())
				setPrev();
		}
		
		
		public void copyTo(PathSync other) {
			if (other.getCapacity() < getCapacity())
				throw new RuntimeException();
			other.length = CLAMP.i(length, 0, other.getCapacity());
			other.tilesI = CLAMP.i(tilesI, 0, other.getCapacity());
			other.compleate = compleate;
			other.currentX = currentX;
			other.currentY = currentY;
			other.totalCost = totalCost;
			other.totalLength = totalLength;
			for (int i = 0; i < bits.length; i++) {
				other.bits[i] = bits[i];
			}
		}
		
		private final int get(int index){
			
			if (index < 0 || index >= bits.length*tilesPerInt*bitA)
				throw new RuntimeException("outof");
			
			index*= bitA;
			int i1 = index/32;
			
			index+= bitA;
			
			int i2 = index/32;
			int b2 = index%32;
			if (b2 == 0)
				b2 = 32;
			
			int res = bits[i1];
			
			if (i1 == i2 || b2 == 32){
				res = res >> (32-b2);
				res &= mask;
			}else{
				res = res << b2;
				res &= mask;
				int res2 = bits[i2];
				res2 = res2 >> (32-b2);
				res2 &= mask >> (b2);
				res |= res2;
				res &= mask;
			}
			
			return res;
			
		}

		private final void set(int index, int value){
			
			if (index < 0 || index >= bits.length*tilesPerInt*bitA)
				throw new RuntimeException("outof");
			
			value &= mask;
			
			index*= bitA;
			int i1 = index/32;
			
			index+= bitA;
			
			int i2 = index/32;
			int b2 = index%32;
			if (b2 == 0)
				b2 = 32;
			
			int res = bits[i1];
			
			if (i1 == i2 || b2 == 32){
				long m = (maskI << (32-b2));
				long m2 = (~m >> bitA);
				res &= (m | m2);
				res |= value << (32-b2);
				bits[i1] = res;

			}else{
				res = res >> (bitA - b2);
				res = res << (bitA - b2);
				res |= value >> (b2);
				bits[i1] = res;
				
				res = bits[i2];
				int m = -1;
				for (int j = 0; j < b2; j++){
					m = m >> 1;
					m &= 0x7FFFFFFF;
				}
				res &= m;
				res |= value << (32-b2);
				bits[i2] = res;
			}
			
		}
		
		@Override
		public final int getCapacity(){
			return bits.length*tilesPerInt;
		}
		
		protected final void cancel() {
			length = 0;
			totalCost = 0;
			tilesI = 0;
		}

		public final void set(PathTile dest) {
			if (dest == null) {
				cancel();
				return;
			}
			totalCost = dest.accCost;
			
			
			int i = 1;
			PathTile t = dest.pathParent;
			while(t != null){
				i++;
				t = t.pathParent;
			}
			
			this.length = i;
			this.totalLength = i;
			
			compleate = true;
			
			int skip = this.length - getCapacity();
			if (skip > 0) {
				this.length = getCapacity();
				compleate = false;
				while (skip > 0) {
					dest = dest.pathParent;
					skip--;
				}
			}
			
			tilesI = length-2;
			currentX = dest.x;
			currentY = dest.y;
			while(tilesI >= 0){
				dest = dest.pathParent;
				int dx = dest.x - currentX;
				int dy = dest.y - currentY;
				set(tilesI*2, dy);
				set(tilesI*2 + 1, dx);
				currentX = dest.x;
				currentY = dest.y;
				tilesI --;
			}
			tilesI++;
		}
		
		protected void setOne(int destX, int destY) {
			totalCost = 0;
			this.length = 1;
			compleate = true;
			
			currentX = (short) destX;
			currentY = (short) destY;
			tilesI = 0;
			totalLength = 1;
		}
		
		protected void setTwo(int x1, int y1, int x2, int y2) {
			totalCost = 0;
			this.length = 2;
			totalLength = 2;
			compleate = true;
			set(0, y1-y2);
			set(1, x1-x2);
			currentX = (short) x1;
			currentY = (short) y1;
			tilesI = 0;
		}
		
		
		@Override
		public final double getTotalCost() {
			return totalCost;
		}
		
		@Override
		public final int length() {
			return length;
		}
		
		@Override
		public int lengthTotal() {
			return totalLength;
		}
		
		@Override
		public final boolean isStart() {
			return tilesI == 0;
		}
		
		@Override
		public final void setStart() {
			while(hasPrev()){
				setPrev();
			}
		}

		@Override
		public boolean isDest() {
			return tilesI >= length - 1;
		}
		
		@Override
		public final boolean hasNext(){
			return tilesI < length - 1;
		}
		
		public final boolean nextIsLast(){
			return tilesI == length - 2;
		}
		
		@Override
		public boolean setNext() {
			if (hasNext()) {
				int x = get(tilesI*2 + 1);
				int y = get(tilesI*2);
				if (x == 3)
					x = -1;
				if (y == 3)
					y = -1;
				currentX -= x;
				currentY -= y;
				tilesI++;
				return true;
			}
			return false;
		}

		@Override
		public final boolean hasPrev(){
			return tilesI > 0;
		}
		
		@Override
		public boolean setPrev() {
			if (hasPrev()) {
				tilesI--;
				int y = get(tilesI*2);
				int x = get(tilesI*2+1);

				if (x == 3)
					x = -1;
				if (y == 3)
					y = -1;
				currentX += x;
				currentY += y;
				return true;
			}
			return false;
		}

		@Override
		public int x() {
			if (length > 0)
				return currentX;
			return-1;
			//throw new RuntimeException("path has length 0. There is no coordinates");
		}

		@Override
		public int y() {
			if (length > 0)
				return currentY;
			return -1;
			//throw new RuntimeException("path has length 0. There is no coordinates");
		}
		
		@Override
		public final boolean isCompleate(){
			return compleate;
		}
		
		@Override
		public int getCurrentI() {
			return tilesI;
		}
		
		@Override
		public void setCurrentI(int i) {
			if (i < 0 || i >= length)
				throw new RuntimeException(i + " " + length);
			while(tilesI < i)
				setNext();
			while(tilesI > i)
				setPrev();
		}


//		@Override
//		public void save(FilePutter file) {
//			file.i(length);
//			file.i(tilesI);
//			file.bool(compleate);
//			file.i(currentX).i(currentY);
//			file.d(totalCost);
//			file.is(bits);
//			
//		}
//
//		@Override
//		public void load(FileGetter file) throws IOException {
//			length = file.i();
//			tilesI = file.i();
//			compleate = file.bool();
//			currentX = (short) file.i();
//			currentY = (short) file.i();
//			totalCost = file.d();
//			file.is(bits);
//		}
//
//		@Override
//		public void clear() {
//			length = 0;
//			tilesI = 0;
//			compleate = false;
//			currentX = 0; currentY = 0;
//			totalCost = 0;
//			
//		}
		
		public void debug() {
			int iold = getCurrentI();
			setCurrentI(0);
			Printer.ln();
			Printer.ln("l:" + length());
			for (int i = 0; i < length; i++) {
				setCurrentI(i);
				Printer.ln("\t" + "("+x() + " " + y()+")");
			}
			setCurrentI(iold);
			Printer.ln();
		}
		
	}
	
}
