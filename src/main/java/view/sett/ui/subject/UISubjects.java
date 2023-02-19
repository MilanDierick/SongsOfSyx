package view.sett.ui.subject;

import init.settings.S;
import settlement.entity.humanoid.*;
import settlement.room.main.RoomInstance;
import util.gui.misc.GBox;
import view.interrupter.ISidePanel;
import view.main.VIEW;

public class UISubjects {
	
	private final UIList list = new UIList();
	private final Panel subject = new UISubject();
	final UIHoverer hoverer = new UIHoverer();
	private final Panel prisoner = new UIPrisoner();
	
	public UISubjects() {
		
		
	}
	
	Humanoid current() {
		Humanoid a = subject.showing();
		if (a != null)
			return a;
		return null;
	}
	
	public void hoverInfo(Humanoid h, GBox text) {
		hoverer.hover(h, text);
	}
	
	public void show() {
		list.show();
	}
	
	public boolean listActive() {
		return VIEW.s().panels.added(list);
	}
	
	public boolean shows(Humanoid h) {
		return current() == h;
	}
	
	public void show(Humanoid h) {
		list.show(h);
		if (get(h) != null)
			get(h).activate(h, list);
	}
	
	public void showSingle(Humanoid h) {
		if (get(h) != null)
			get(h).activate(h, list);
	}
	
	public void showProfession(RoomInstance work) {
		list.showProfession(work);
	}
	
	public boolean canShow(Humanoid a) {
		return get(a) != null;
	}
	
	public Panel get(Humanoid a) {
		if (a.indu().hType() == HTYPE.PRISONER)
			return prisoner;
		if (a.indu().clas() == HCLASS.CHILD)
			return null;
		if (a.indu().clas().player)
			return subject;
		return S.get().developer ? subject : null;
	}
	
	interface Panel {
		
		public void activate(Humanoid a, ISidePanel list);
		public Humanoid showing();
		
	}

}
