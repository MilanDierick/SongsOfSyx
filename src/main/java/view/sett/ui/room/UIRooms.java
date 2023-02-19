package view.sett.ui.room;

import static settlement.main.SETT.*;

import init.D;
import init.race.RACES;
import init.race.Race;
import settlement.entity.humanoid.HCLASS;
import settlement.main.SETT;
import settlement.room.main.*;
import settlement.stats.StatsEquippables.StatEquippableWork;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.ACTION;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import view.interrupter.ISidePanel;
import view.main.VIEW;

public final class UIRooms {


	final UIRoom[] rooms = new UIRoom[ROOMS().all().size()];
	private UIPanelMain main;
	
	private final GuiSection pop2 = new GuiSection();
	
	
	public UIRooms() {
		
		Init init = new Init();
		Modules mm = new Modules(init);
		
		D.t(UIRoom.class);
		
		for (RoomBlueprint p : ROOMS().all()) {
			rooms[p.index()] = new UIRoom(p, mm.get(p));
		}
		main = new UIPanelMain(rooms);
		
		SETT.addGeneratorHook(new ACTION() {
			
			@Override
			public void exe() {
				main = new UIPanelMain(rooms);
			}
		});
		
		for (int i = 0; i < RACES.all().size(); i++) {
			Race r = RACES.all().get(i);

			CLICKABLE c = new GButt.ButtPanel(r.appearance().iconBig.huge) {
				@Override
				protected void clickA() {
					VIEW.inters().popup.close();
					main.work.set(r, HCLASS.SLAVE);
					VIEW.s().panels.add(main.work, true);
				};
			}.hoverInfoSet(r.info.names);
			c.body().moveX1Y1((i % 5) * c.body().width(), (i / 5) * c.body().height());
			pop2.add(c);
		}

	}
	
	public ISidePanel main() {
		
		return main;
	}
	
	public void hover(GBox box, Room r, int rx, int ry) {
		rooms[r.blueprint().index()].hover(box, r, rx, ry);
	}
	
	public void open(StatEquippableWork w) {
		main.open(w);
	}
	
	public void open(RoomInstance r) {
		if (rooms[r.blueprint().index()].table == null)
			return;
		VIEW.s().panels.add(rooms[r.blueprint().index()].table.get(), true);
		VIEW.s().panels.add(rooms[r.blueprint().index()].detail(r), false);
	}
	
	public void open(RoomBlueprint r, boolean disturb) {
		VIEW.s().panels.add(rooms[r.index()].table.get(), disturb);
	}
	
	public void prio(HCLASS c, Race r, CLICKABLE trigger) {
		if (c == HCLASS.SLAVE) {
			VIEW.inters().popup.show(pop2, trigger);
		}else if (c == HCLASS.CITIZEN && r != null) {
			main.work.set(r, c);
			VIEW.s().panels.add(main.work, true);
		}else {
			VIEW.s().panels.add(main.work, true);
		}
		
	}

}
