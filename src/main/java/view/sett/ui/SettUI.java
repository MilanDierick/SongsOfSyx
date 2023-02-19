package view.sett.ui;

import settlement.main.SETT;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.RoomInstance;
import view.interrupter.InterManager;
import view.sett.ui.army.UIArmy;
import view.sett.ui.bottom.UIBuildPanel;
import view.sett.ui.home.UIHomes;
import view.sett.ui.law.UILaw;
import view.sett.ui.noble.UINobles;
import view.sett.ui.room.construction.UIRoomPlacer;
import view.sett.ui.room.copy.UICopier;
import view.sett.ui.standing.UICitizens;
import view.sett.ui.standing.UISlaves;
import view.sett.ui.subject.UISubjects;

public final class SettUI {

	public final UIRoomPlacer placer = new UIRoomPlacer();
	public final view.sett.ui.room.UIRooms rooms = new view.sett.ui.room.UIRooms();
	public final UISubjects subjects = new UISubjects();
	public final UIArmy army = new UIArmy(SETT.ARMIES());
	public final UICitizens standing = new UICitizens();
	public final UISlaves slaves = new UISlaves();
	public final UINobles nobles = new UINobles();
	public final UILaw law = new UILaw();
	public final UICopier copier = new UICopier();
	public final UIHomes home = new UIHomes();
	
	public SettUI(InterManager m){
		
		new UIBuildPanel(placer, m);
	}
	
	public void reconstruct(RoomInstance r) {
		placer.init(r);
	}
	
	public void reconstruct(int tx, int ty) {
		placer.init(tx, ty);
	}
	
	public void reconstruct(RoomBlueprintImp b) {
		placer.init(b, -1,-1);
	}
	
}
