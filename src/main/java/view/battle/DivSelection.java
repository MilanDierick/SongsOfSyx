package view.battle;

import settlement.army.ArmyManager;
import settlement.army.Div;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

public final class DivSelection {

	private final boolean[] selected = new boolean[ArmyManager.DIVISIONS];
	private final ArrayList<Div> selection = new ArrayList<>(ArmyManager.DIVISIONS);
	private final boolean[] hovered = new boolean[ArmyManager.DIVISIONS];
	
	public void select(Div f) {
		if (selected[f.index()])
			return;
		selection.add(f);
		selected[f.index()] = true;
		
	}
	
	public void deSelect(Div f) {
		if (!selected[f.index()])
			return;
		selection.remove(f);
		selected[f.index()] = false;
	}
	
	public boolean selected(Div f) {
		return selected[f.index()];
	}
	
	public void clear() {
		selection.clear();
		for (int i = 0; i < selected.length; i++) {
			selected[i] = false;
		}
		artillery.clear();
	}
	
	public LIST<Div> selection(){
		return selection;
	}
	
	public int allSelected() {
		return selection.size() + artillery.selection().size();
	}
	
	public boolean isClear() {
		return selection.size() == 0 && artillery.isClear();
	}

	public void toggle(Div f) {
		if (selected(f)){
			deSelect(f);
		}else
			select(f);
	}
	
	public boolean hovered(Div d) {
		return hovered[d.index()];
	}
	
	public void hover(Div d) {
		hovered[d.index()] = true;
	}
	
	public void clearHover() {
		for (int i = 0; i < hovered.length; i++) {
			hovered[i] = false;
		}
		artillery.clearHover();
	}
	
	public final CatSelection artillery = new CatSelection();

	
}
