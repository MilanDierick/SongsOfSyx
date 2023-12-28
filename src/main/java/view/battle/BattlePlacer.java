package view.battle;

import static settlement.main.SETT.*;

import init.C;
import settlement.army.Div;
import settlement.army.order.DivTDataTask;
import settlement.main.SETT;
import settlement.room.military.artillery.ArtilleryInstance;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.Coo;
import util.gui.misc.GBox;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;
import view.keyboard.KEYS;
import view.subview.GameWindow;

public final class BattlePlacer {

	private final GameWindow w;
	final DivSelection s;
	private final BattlePlacerRenderer ren;
	
	
	Mode current;
	private final Mode selectMore;
	private final Mode position;
	private final Mode spin;
	private final BattlePlacerAttack attack;

	private final Action action = new Action();
	
	public BattlePlacer(GameWindow w, DivSelection s) {
		this.w = w;
		this.s = s;
		selectMore = new BattlePlacerSelect(w, s, action);
		position = new BattlePlacerPlace(w, s, action);
		spin = new BattlePlacerSpin(w, s, action);
		attack = new BattlePlacerAttack(w, s, action);
		current = selectMore;
		ren = new BattlePlacerRenderer(this);
	}
	
	public void click(MButt butt) {
		
		if (butt == MButt.LEFT) {
			action.clicked = true;
			action.start.set(w.pixel());
		}else if (butt == MButt.RIGHT){
			if (action.clicked) {
				action.clicked = false;
			}else {
				s.clear();
			}
		}
	}
	
	
	void keypush() {
		
		int dx = 0;
		int dy = 0;
		
		if (KEYS.BATTLE().SELECT_ALL.consumeClick()) {
			action.clicked = false;
			s.clear();
			boolean allSelected = true;
			for (Div d : SETT.ARMIES().player().divisions()) {
				if (d.menNrOf() > 0) {
					allSelected &= s.selected(d);
					s.select(d);
				}
			}
			if (allSelected) {
				for (ArtilleryInstance ins : s.artillery.all()) {
					s.artillery.select(ins);
				}
			}
		}
		
		if (KEYS.MAIN().BACKSPACE.consumeClick()) {
			action.clicked = false;
			for (Div d : s.selection()) {
				DivTDataTask.TMP.stop();
				d.order().task.set(DivTDataTask.TMP);
			}
			for (ArtilleryInstance ins : s.artillery.all()) {
				ins.clearTarget();
			}
		}
		
		if (KEYS.BATTLE().UP.consumeClick()) {
			dy = -C.TILE_SIZE;
		}if (KEYS.BATTLE().DOWN.consumeClick()) {
			dy = C.TILE_SIZE;
		}
		
		if (KEYS.BATTLE().LEFT.consumeClick())
			dx = -C.TILE_SIZE;
		if (KEYS.BATTLE().RIGHT.consumeClick()) {
			dx = C.TILE_SIZE;
		}
		
		if (dx == 0 && dy == 0)
			return;
		action.clicked = false;
		for (Div d : s.selection()) {
			ARMIES().placer.deploy(d, dx, dy);
		}
		
	}
	
	private Mode getState() {
		if (s.allSelected() <= 0)
			return selectMore;
		if (attack.init()) {
			return attack;
		}
		if (s.allSelected() <= 0 || KEYS.MAIN().UNDO.isPressed()) {
			return selectMore;
		}else if(KEYS.MAIN().MOD.isPressed()) {
			return spin;
		}else {
			return position;
		}
	}
	
	public void update(boolean hovered) {
		
		current = getState();
		
		keypush();
		action.clickReleased = false;
		
		if (action.clicked && !MButt.LEFT.isDown()) {
			action.clicked = false;
			action.clickReleased = true;
		}
		

		ren.add(hovered);
		current.update(hovered);
		
	}
	
	public void hoverTimer(GBox text) {
		current.hoverTimer(text);
		
	}
	
	static abstract class Mode {
		
		abstract void update(boolean hovered);
		abstract void hoverTimer(GBox text);
		abstract void render(Renderer r, ShadowBatch shadowBatch, RenderData data);
		
	}
	
	static final class Action {
		
		public Coo start = new Coo();
		public boolean clicked;
		public boolean clickReleased;
		
	}
	




	
}
