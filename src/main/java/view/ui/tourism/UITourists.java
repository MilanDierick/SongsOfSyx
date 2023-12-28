package view.ui.tourism;

import util.dic.DicMisc;
import view.ui.manage.IFullView;

public final class UITourists extends IFullView {

	
	public UITourists() {
		super(DicMisc.¤¤Tourists);
		section.body().setWidth(WIDTH).setHeight(1);
		
		section.addDownC(0, new Tourism(HEIGHT));
		

	}




}
