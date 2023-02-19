package settlement.room.main.construction;

import static settlement.room.main.construction.ConstructionData.*;

import init.D;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.settings.S;
import settlement.job.Job;
import settlement.main.SETT;
import settlement.room.main.Room;
import settlement.room.main.furnisher.FurnisherItem;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.misc.CLAMP;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import view.main.VIEW;
import view.sett.ui.room.UIRoomModule;

final class ConstructionHoverer extends UIRoomModule {

	{
		D.gInit(this.getClass());
	}
	
	private final CharSequence stringprog = D.g("progress", "Construction Progress");
	private final CharSequence stringclear = D.g("cleared", "cleared: ");
	private final CharSequence stringMat = D.g("Materials");
	private final CharSequence stringAct = D.g("Dormant", "(Dormant. Activate to commence work)");
	private final int[] resNeeded = new int[RESOURCES.ALL().size()];
	private final int[] resAllocated = new int[RESOURCES.ALL().size()];

	@Override
	public void hover(GBox box, Room r, int rx, int ry) {
		
		ConstructionInstance k = (ConstructionInstance) r;
		
		if (!k.active)
			box.add(box.text().errorify().add(stringAct));
		box.NL();
		box.add(box.text().add(stringMat));
		box.NL();

		for (int i = 0; i < k.blueprint.resources(); i++) {
			resNeeded[i] = 0;
			resAllocated[i] = 0;
		}
		
		int clearNeeded = 0;
		int floorNeeded = 0;
		int structuresNeeded = 0;
		int structureResources = 0;
		int itemNeeded = 0;
		int itemTotal = 0;
		
		for (COORDINATE c : k.body()) {
			if (!k.is(c))
				continue;
			if (k.needsClear(c))
				clearNeeded ++;
			if (k.structureI >= 0 && !SETT.TERRAIN().CAVE.is(c) && !SETT.TERRAIN().MOUNTAIN.isMountain(c.x(), c.y()) && !SETT.TERRAIN().BUILDINGS.all().get(k.structureI).roof.is(c)) {
				structuresNeeded++;
				structureResources += dWorkAmount.get(c);
			}
			if (dFloored.is(c, 0))
				floorNeeded ++;
			FurnisherItem it = SETT.ROOMS().fData.item.get(c);
			if (it != null)
				itemTotal ++;
			if (it != null && (dConstructed.is(c, 0) || dBroken.is(c, 1))) {
				itemNeeded ++;
//				for (int i = 0; i < k.blueprint.resources(); i++) {
//					resNeeded[i] += it.cost(i);
//				}
			}
			if (SETT.ROOMS().fData.isMaster.is(c)) {
				
//				for (int i = 0; i < k.blueprint.resources(); i++) {
//					resNeeded[i] += it.cost(i);
//					if (dConstructed.is(c, 1) && dBroken.is(c, 0)) {
//						resAllocated[i] += it.cost(i);
//					}
//				}
				
				
			}
			
			
			int am = dResAllocated.get(c);
			for (int i = 0; i < k.blueprint.resources(); i++) {
				int b =  dResourceNeeded[i].get(c);
				resNeeded[i] += b;
				if (am > 0) {
					int a = CLAMP.i(am, 0, b);
					resAllocated[i] += a;
					am -= a;
				}
			}
		}

		if (k.structureI >= 0) {
			RESOURCE sRes = SETT.TERRAIN().BUILDINGS.getAt(k.structureI).resource;
			int sResA = SETT.TERRAIN().BUILDINGS.getAt(k.structureI).resAmount;
			int kkkk = -1;
			for (int i = 0; i < k.blueprint.resources(); i++) {
				if (k.blueprint.resource(i) == sRes) {
					kkkk = i;
					resAllocated[i] += structureResources; 
					resAllocated[i] += (k.area()-structuresNeeded)*sResA;
					resNeeded[i] += k.area()*sResA;
				}
				
			}
			if (kkkk == -1 && sRes != null && structuresNeeded > 0) {
				box.setResource(sRes,structureResources + (k.area()-structuresNeeded)*sResA, k.area()*sResA);
			}
		}

		for (int i = 0; i < k.blueprint.resources(); i++) {
			if (resNeeded[i] > 0) {
				RESOURCE res = k.blueprint.resource(i);
				box.setResource(res, resAllocated[i], resNeeded[i]);
			}
			
		}

		box.NL();
		box.add(box.text().set(stringclear));
		box.add(GFORMAT.perc(box.text(), (k.area()-clearNeeded)/(double)k.area()));
		
		
		box.NL();
		box.add(box.text().set(stringprog));
		double total = k.area() + itemTotal;
		double p = floorNeeded + k.builtNeeded;
		double t = (total-p)/total;
		box.add(GFORMAT.perc(box.text(), t));

		Job j = SETT.JOBS().getter.get(VIEW.s().getWindow().tile());
		if (j != null) {
			box.NL(8);
			j.hover(box);
		}
		
		if (S.get().developer) {
			box.NL();
			k.debug(box);
			box.NL();box.NL();
			box.add(box.text().add("nClear: ").add(clearNeeded));
			box.add(box.text().add("nFloor: ").add(floorNeeded));
			box.add(box.text().add("nStruc: ").add(structuresNeeded));
			box.add(box.text().add("nItem: ").add(itemNeeded).add('/').add(itemTotal));
		}
	}
	
}
