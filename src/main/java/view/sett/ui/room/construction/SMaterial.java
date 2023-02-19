package view.sett.ui.room.construction;

import init.D;
import init.sprite.SPRITES;
import settlement.main.SETT;
import settlement.tilemap.TBuilding;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.clickable.CLICKABLE;
import util.gui.misc.*;
import util.info.GFORMAT;
import view.main.VIEW;

final class SMaterial {
	
	private final GuiSection section = new GuiSection();
	private final GuiSection buttonsIndoor = new GuiSection();
	private final State s;
	
	{
		D.gInit(this);
	}
	
	private final CLICKABLE buttonIndoor = new GButt.ButtPanel(SPRITES.icons().m.cancel) {
		@Override
		protected void clickA() {
			VIEW.inters().popup.show(buttonsIndoor, this);
		}
		@Override
		protected void renAction() {
			replaceLabel(s.placement.placer.structure.get().iconCombo, DIR.C);
		}
	}.hoverInfoSet(D.g("indoor", "This room requires to be built indoors and you must pick a structure type."));
	
	private final CLICKABLE buttWalls = new GButt.ButtPanel(SPRITES.icons().m.wall) {
		@Override
		protected void clickA() {
			s.placement.placer.autoWalls.toggle();
		}
		
		@Override
		protected void renAction() {
			selectedSet(s.placement.placer.autoWalls.isOn());
		}
	}.hoverInfoSet(D.g("walls", "Auto build walls around room."));

	private final CLICKABLE buttDoor = new GButt.ButtPanel(SETT.ROOMS().placement.placer.placerDoor.getIcon()) {
		@Override
		protected void clickA() {
			if (s.placement.placer.autoWalls.isOn()) {
				VIEW.s().tools.place(s.placement.placer.placerDoor, s.config);
			}
		}
		
		@Override
		protected void renAction() {
			activeSet(s.placement.placer.autoWalls.isOn());
			selectedSet(VIEW.s().tools.placer.getCurrent() == s.placement.placer.placerDoor);
		}

		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			super.render(r, ds, isActive, isSelected, isHovered);
			if (s.problemneedDoor  && s.problemTimer > VIEW.renderSecond()) {
				COLOR.RED100.renderFrame(r, body, 2, 3);
				OPACITY.O25To50.bind();
				COLOR.RED100.render(r, body);
				OPACITY.unbind();
			}
		};
		
	}.hoverInfoSet(D.g("door", "places doorways on walls. Needed to make the room reachable. Doorways decrease isolation, but too little will make entering the room difficult"));
	private final CLICKABLE buttDoorRemove = new GButt.ButtPanel(SETT.ROOMS().placement.placer.placerDoor.getUndo().getIcon()) {
		@Override
		protected void clickA() {
			if (s.placement.placer.autoWalls.isOn()) {
				VIEW.s().tools.place(s.placement.placer.placerDoor.getUndo(), s.config);
			}
		}
		
		@Override
		protected void renAction() {
			activeSet(s.placement.placer.autoWalls.isOn());
				selectedSet(VIEW.s().tools.placer.getCurrent() == s.placement.placer.placerDoor.getUndo());
		}
	}.hoverInfoSet(SETT.ROOMS().placement.placer.placerDoor.getUndo().name());
	
	private final HOVERABLE isolation = new GStat() {
		@Override
		public void update(GText text) {
			GFORMAT.perc(text, s.placement.placer.isolation());
		}
	}.hh(SETT.ROOMS().isolation.info.name).hoverInfoSet(SETT.ROOMS().isolation.info.desc);
	
	SMaterial(State s){
		this.s = s;
		for (TBuilding t : SETT.TERRAIN().BUILDINGS.all()) {
			
			
			CLICKABLE c = new GButt.Panel(t.iconCombo, t.desc) {
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					b.title(t.name);
					buttonIndoor.hoverInfoGet(text);
					b.NL();
					b.text(t.desc);
					b.setResource(t.resource, t.resAmount);
				}
				
				@Override
				protected void clickA() {
					s.placement.placer.structure.set(t);
					VIEW.inters().popup.close();
				}
				
				@Override
				protected void renAction() {
					selectedSet(s.placement.placer.structure.get() == t);
				}
			};
			buttonsIndoor.addDownC(0, c);
		}
	}
	
	GuiSection get() {
		section.clear();
		
		section.addRightC(0, buttonIndoor);
		section.addRightC(0, buttWalls);
		if (s.b.constructor().mustBeIndoors() && s.b.constructor().usesArea()) {
			section.addRightC(0, buttDoor);
			section.addRightC(0, buttDoorRemove);
		}
		
		if (s.b.constructor().needsIsolation())
			section.addRelBody(4, DIR.N, isolation);
		return section;
		
	}
	
}
