package settlement.job;

import static settlement.main.SETT.*;

import init.resources.RESOURCE;
import init.resources.RESOURCES;
import snake2d.util.datatypes.AREA;
import snake2d.util.misc.TOGGLEBLE;
import view.sett.IDebugPanelSett;
import view.tool.*;

class Debug {


	static boolean showRoom = false;
	
	Debug(){
		
		PLACABLE p;
		
		p = new PlacableMulti("reservePerform") {
			@Override
			public void place(int tx, int ty, AREA a, PLACER_TYPE t) {
				Job j =  JOBS().getter.get(tx, ty);
				if (j == null)
					return;
				RESOURCE res = j.jobResourceBitToFetch() != 0 ? RESOURCES.ALL().get(Long.numberOfTrailingZeros(j.jobResourceBitToFetch())) : null;
				
				if (j.jobReserveCanBe()) {
					j.jobReserve(res);
				}else if (j.jobReservedIs(res)) {
					j.jobPerform(null, res, 1);
				}
				
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA a, PLACER_TYPE t) {
				Job j =  JOBS().getter.get(tx, ty);
				if (j != null) {
					RESOURCE res = j.jobResourceBitToFetch() != 0 ? RESOURCES.ALL().get(Long.numberOfTrailingZeros(j.jobResourceBitToFetch())) : null;
					if (j.jobReserveCanBe() || j.jobReservedIs(res)) {
						return null;
					}
				}
				return "";
			}
		};
		

		

		
		IDebugPanelSett.add("job", p);
		
		TOGGLEBLE roomJobs = new TOGGLEBLE() {
			
			@Override
			public boolean isOn() {
				// TODO Auto-generated method stub
				return showRoom;
			}

			@Override
			public void set(boolean bool) {
				showRoom = bool;
			}
		};
		
		IDebugPanelSett.add("Show roomjobs", roomJobs);
		
	}
	
}
