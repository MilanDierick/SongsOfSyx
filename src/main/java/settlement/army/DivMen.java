package settlement.army;

import java.io.IOException;

import snake2d.util.file.*;

final class DivMen implements SAVABLE{

	private final short[] order;
	private final short[] orderR;
	private short orderI = 0;
	
	DivMen(int max){
		orderR = new short[max];
		order = new short[max];
		clear();
	}
	
	short getSpot(int i) {
		return orderR[i];
	}
	
	int freeSpots() {
		return order.length - orderI;
	}
	
	public int men() {
		return orderI;
	}
	
	public short getNewSpot() {
		if (freeSpots() <= 0)
			throw new RuntimeException();
		short spot = order[orderI];
		orderR[spot] = orderI;
		orderI++;
		
		return spot;
	}
	
	public void returnSpot(short spot) {
		if (orderI == 0)
			throw new RuntimeException();
		if (orderI == 1) {
			orderI = 0;
			return;
		}
		int spotLast = order[orderI-1];
		int orderIA = orderR[spot];
		if (orderIA >= orderI)
			throw new RuntimeException(orderIA + " " + orderI);
		
		order[orderIA] = (short) spotLast;
		order[orderI-1] = spot;
		
		orderR[spot] = -1;
		orderR[spotLast] = (short) orderIA;
		
		orderI--;
		
		
	}

	@Override
	public void save(FilePutter file) {
		file.ss(order);
		file.ss(orderR);
		file.i(orderI);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		file.ss(order);
		file.ss(orderR);
		orderI = (short) file.i();
	}

	@Override
	public void clear() {
		for (int i = 0; i < order.length; i++) {
			order[i] = (short)i;
			orderR[i] = -1;
		}
		orderI = 0;
	}
	
}
