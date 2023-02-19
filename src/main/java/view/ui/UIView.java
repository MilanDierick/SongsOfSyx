package view.ui;

import view.ui.credits.UICredits;
import view.ui.profile.UIProfile;
import view.ui.tech.UITechTree;

public class UIView {

	public final UICredits trade;
	public final UIGoods goods;
	public final UITechTree tech;
	public final UIProfile level;
	
	public UIView() {
		trade = new UICredits();
		goods = new UIGoods();
		tech = new UITechTree();
		level = new UIProfile();
	}
	
}
