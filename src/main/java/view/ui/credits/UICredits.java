package view.ui.credits;

import static util.dic.DicRes.*;

import view.ui.manage.IFullView;

public final class UICredits extends IFullView {

	
	public UICredits() {
		super(¤¤Treasury);
		
		section.body().setWidth(WIDTH).setHeight(1);
		
		section.addDownC(0, new COverview(HEIGHT));

	}


}
