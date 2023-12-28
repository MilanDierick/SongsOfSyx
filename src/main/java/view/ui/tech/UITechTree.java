package view.ui.tech;

import game.faction.FACTIONS;
import init.tech.TECH;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.renderable.RENDEROBJ;
import view.ui.manage.IFullView;

public class UITechTree extends IFullView{

	final Tree tree;

	public UITechTree(){
		super(FACTIONS.player().tech().info.name);

		Info info = new Info(this);
		
		
		
		
		RENDEROBJ oo = new InfoBonuses(HEIGHT-(info.body().height()+16));
		
		tree = new Tree(HEIGHT-(info.body().height()+16), WIDTH-oo.body().width()-48);
		section.add(tree, 0, 0);
		
		
		oo.body().moveX1(section.body().x2()+8);
		oo.body().moveY1(section.body().y1());
		section.add(oo);
		
		section.addRelBody(16, DIR.N, info);
		
//		section.add(oo);
//		
	}
	
	void filter(TECH tech) {
		tree.filter(tech);
	}
	
//	public CLICKABLE butt() {
//		return new UIPanelTopButtL( SPRITES.icons().s.vial) {
//			
//			@Override
//			protected double valueNext() {
//				return value();
//			}
//			
//			@Override
//			protected double value() {
//				return GAME.player().tech.penalty().getD();
//			}
//			
//			@Override
//			protected int getNumber() {
//				return (int) GAME.player().tech.available().get();
//			}
//			
//			@Override
//			public void hoverInfoGet(GUI_BOX text) {
//				FACTIONS.player().tech.info.hover(text);
//			};
//
//			@Override
//			protected boolean isActive() {
//				return GAME.player().tech.available().get() != 0 || GAME.player().tech.allocated().get() != 0;
//			};
//			
//			@Override
//			protected void renAction() {
//				
//				//selectedSet(isActivated());
//			}
//			
//			@Override
//			protected void clickA() {
//				activate();
//			}
//			
//		};
//	}
	
}
