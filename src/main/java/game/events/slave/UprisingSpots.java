package game.events.slave;

import java.io.IOException;

import init.config.Config;
import settlement.army.Div;
import settlement.entity.humanoid.HTYPE;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.main.throne.THRONE;
import settlement.stats.STATS;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;

public final class UprisingSpots{

	private ArrayList<UprisingSpot> spots = new ArrayList<>(Config.BATTLE.DIVISIONS_PER_ARMY);
	private int divI;
	private int amountTotal;
	private int signedUp;
	private int inposition;
	private final Coo position = new Coo();
	
	UprisingSpots(){
		
	}
	
	public int riot(int amount) { 
		
		
		
		//amount = STATS.POP().pop(HTYPE.SLAVE);
		clear();
		
		if (!UprisingSpot.setStart(position, THRONE.coo().x(), THRONE.coo().y(), 128))
			return 0;
		
		
		int sp = (int) Math.ceil((double)amount/(Config.BATTLE.MEN_PER_DIVISION));
		sp = CLAMP.i(sp, 0, spots.max());
		
		int menPerSpot = (int) Math.ceil((double)amount/sp);
		SETT.ARMY_AI().pause();
		for (int i = 0; i < sp; i++) {
			
			int a = CLAMP.i(menPerSpot, 0, amount);
			amount -= a;
			
			UprisingSpot s = UprisingSpot.make(position.x(), position.y(), a);
			
			if (s != null) {
				spots.add(s);
				amountTotal += a;
			}
			
		}
		SETT.ARMY_AI().unpause();
		
		
		return amountTotal;
		
	}
	
	boolean hasMore() {
		return spots.size() > 0;
	}
	
	void save(FilePutter file) {
		file.i(divI);
		file.i(amountTotal);
		file.i(signedUp);
		file.i(inposition);
		position.save(file);
		file.i(spots.size());
		for (UprisingSpot s : spots)
			s.save(file);
	}
	
	protected void load(FileGetter file) throws IOException {
		divI = file.i();
		amountTotal = file.i();
		signedUp = file.i();
		inposition = file.i();
		position.load(file);
		int am = file.i();
		spots.clear();
		for (int i = 0; i < am; i++) {
			spots.add(UprisingSpot.make(file));
		}
	}
	
	
	protected void clear() {
		spots.clear();
		divI = 0;
		amountTotal = 0;
		signedUp = 0;
		inposition = 0;
	}
	
	public boolean shouldSignUpUpriser(Humanoid h) {
		return signedUp < amountTotal;
	}
	
	public int signUpUpriserPositionByte(Humanoid h) {
		for (int i = 0; i < spots.size(); i++) {
			if (spots.get(i).signedUp < spots.get(i).amountTotal) {
				spots.get(i).signedUp++;
				signedUp ++;
				return i;
			}
		}
		return -1;
	}
	
	public boolean confirmUpriser(int positionByte) {
		if (positionByte < 0 || positionByte >= spots.size())
			return false;
		return spots.get(positionByte).signedUp <= spots.get(positionByte).amountTotal;
	}
	
	public void reportUpriserInPosition(int positionByte) {
		if (positionByte < 0 || positionByte >= spots.size())
			return;
		inposition ++;
	}
	
	public void cancelUpriser(Humanoid h, int positionByte, boolean inPosition) {
		if (positionByte < 0 || positionByte >= spots.size())
			return;
		spots.get(positionByte).signedUp --;
		signedUp --;
		if (inPosition) {
			inposition --;
		}
	}
	
	public COORDINATE getUpriserTile(int positionByte) {
		return spots.get(positionByte);
	}
	
	boolean update(double ds) {
		
		if (amountTotal > STATS.POP().pop(HTYPE.SLAVE)) {
			int d = amountTotal - STATS.POP().pop(HTYPE.SLAVE);
			for(int i = 0; i < spots.size(); i++) {
				int am = CLAMP.i(d, 0, spots.get(i).amountTotal);
				spots.get(i).amountTotal -= am;
				amountTotal -= am;
				if (am <= 0)
					break;
			}
		}
		
		if (inposition >= amountTotal) {
			while(nextDivision() && spots.size() > 0) {
				Div d = SETT.ARMIES().enemy().divisions().get(divI);
				spots.removeLast().makeDiv(d, spots.size());
			}
			clear();
			return true;
		}
		
		return false;
		
	}
	
	private boolean nextDivision() {
		
		if (divI >= Config.BATTLE.DIVISIONS_PER_ARMY) {
			return false;
		}
		while(SETT.ARMIES().enemy().divisions().get(divI).menNrOf() > 0) {
			divI ++;
			
			if (divI >= Config.BATTLE.DIVISIONS_PER_ARMY) {
				return false;
			}
		}
		return true;
	}
	



	


	
}
