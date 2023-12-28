package game.battle;

import java.io.IOException;

import game.GAME;
import game.GameLoader;
import init.C;
import init.RES;
import init.paths.PATHS;
import init.sprite.UI.UI;
import settlement.army.Div;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.main.throne.THRONE;
import snake2d.*;
import snake2d.util.datatypes.*;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LIST;
import util.dic.DicArmy;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import view.interrupter.Interrupter;
import view.main.VIEW;
import view.subview.GameWindow;
import world.battle.WBattles;

public class BattleState {

	private boolean deploying = true;
	private boolean concluded = false;
	private final Rec deploymentTiles = new Rec();
	private double throneTimer = 0;
	public static final int throneMax = 60*5;
	private final PlayerBattleSpec resolve;
	private final int ekilled;
	
	public BattleState(PlayerBattleSpec resolve){
		save("__beforeBattle");

		this.resolve = resolve;
		new BattleStateGenerator().generate(this, resolve, deploymentTiles);
		
		VIEW.messages().hideAll();
		deploying = true;
		throneTimer = 60*5;
		GAME.SPEED.speedSet(0);
		save("__battle");
		SETT.ARMY_AI().pause();
		
		VIEW.b().activate(this);
		VIEW.b().getWindow().centererTile.set(THRONE.coo());
		VIEW.b().getWindow().zoomoutmax();
		ekilled = GAME.count().ENEMIES_KILLED.current();
	}
	
	public void save(String name) {
		if (PATHS.local().SAVE.exists(name))
			PATHS.local().SAVE.delete(name);
		try {
			RES.saver().save(name);
		} catch (IOException e) {
			e.printStackTrace();
			throw new Errors.DataError(name, PATHS.local().SAVE.get(name));
		}
		
	}
	
	
	public void reloadBattle() {
		new GameLoader(PATHS.local().SAVE.get("__battle")){
			
			@Override
			public void doAfterSet() {
				concluded = false;
				deploying = true;
				throneTimer = 0;

				SETT.ARMY_AI().pause();
				VIEW.b().activate(BattleState.this);
				VIEW.b().getWindow().centererTile.set(THRONE.coo());
				VIEW.b().getWindow().zoomoutmax();
				GAME.SPEED.speedSet(0);
				
			}
			
		}.set();
	}
	
	public double throneTimer() {
		return throneTimer;
	}
	
	public boolean deploying() {
		return deploying;
	}
	
	public void deploy() {
		deploying = false;
		SETT.ARMY_AI().unpause();
	}
	
	public RECTANGLE deploymentBounds() {
		return deploymentTiles;
	}
	
	
	void liveResolve(boolean retreat, boolean win) {
		resolve.conclude(throneTimer<= 0, retreat);
		int killed = GAME.count().ENEMIES_KILLED.current()-ekilled;
		new GameLoader(PATHS.local().SAVE.get("__beforeBattle")){
			
			@Override
			public void doAfterSet() {
				
				resolve.finish();
				if (!win) {
					GAME.count().BATTLES_WON.inc(1);
					SETT.INVADOR().decreaseWins();
				}else {
					GAME.count().BATTLES_LOST.inc(1);
				}
				GAME.count().ENEMIES_KILLED.inc(killed);
			}
			
		}.set();
	}
	
	
	public void liveRetreat() {
		liveResolve(true, false);
	}
	
	public int liveRetreatLosses() {
		int am = (int) Math.ceil(SETT.ARMIES().enemy().men()*WBattles.retreatPenalty);
		for (Div d : SETT.ARMIES().player().divisions()) {
			if (d.settings.isFighting()) {
				am += d.menNrOf()*0.5;
			}
		}
		if (am > SETT.ARMIES().player().men()) {
			am = SETT.ARMIES().player().men();
		}
		return am;
	}
	
	public void update(double ds) {
		
		if (concluded)
			return;
		
		if (deploying)
			return;
		
		if (SETT.ARMIES().player().men() == 0 || SETT.ARMIES().enemy().men() == 0) {
			if (SETT.ARMIES().player().men() == 0)
				new ILiveConclude(DicArmy.¤¤Defeat, false, false);
			else if (SETT.ARMIES().enemy().men() == 0)
				new ILiveConclude(DicArmy.¤¤Victory, false, true);
			concluded = true;
			return;
		}
		
		LIST<ENTITY> es = SETT.ENTITIES().fillTiles(THRONE.coo().x()-4, THRONE.coo().y()-4, 8, 8);
		
		double tt = throneTimer - ds;
		throneTimer = throneMax;
		for (ENTITY e : es) {
			if (e instanceof Humanoid) {
				Humanoid h = (Humanoid) e;
				if (h.indu().hType().hostile && h.division() != null) {
					throneTimer = tt;
					break;
				}
			}
		}
		
		if (throneTimer <= 0) {
			new ILiveConclude(DicArmy.¤¤Defeat, false, false);
			concluded = true;
			return;
		}
		
		
	}
	
	final class ILiveConclude extends Interrupter{

		
		private RENDEROBJ thing; 
		private double timer = 0;
		private final boolean retreat;
		private final boolean win;
		private final GameWindow window = new GameWindow(C.DIM(), SETT.PIXEL_BOUNDS, 0);
		
		ILiveConclude(CharSequence title, boolean retreat, boolean win){
			pin();
			persistantSet();
			this.win = win;
			
			GText tt = new GText(UI.FONT().H1, title).lablify();
			thing = new RENDEROBJ.RenderImp(tt.width(), 1) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					UI.PANEL().titleBoxes[UI.PANEL().titleBoxes.length-1].renderCY(r, body.x1(), body.cY(), body().width());
					tt.renderC(r, body);
				}
			};
			thing.body().centerIn(C.DIM());
			this.retreat = retreat;
			window.copy(VIEW.b().getWindow());
			SETT.ARMY_AI().pause();
			VIEW.inters().manager.add(this);
		}

		@Override
		protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
			return true;
		}

		@Override
		protected void mouseClick(MButt button) {
			
		}

		@Override
		protected void hoverTimer(GBox text) {
			
		}

		@Override
		protected boolean render(Renderer r, float ds) {
			
			thing.render(r, ds);
			GAME.s().render(r, ds, window);
			return false;
		}

		@Override
		protected boolean update(float ds) {
			GAME.SPEED.speedSet(1);
			timer += ds;
			if (timer >= 4) {
				liveResolve(retreat, win);
			}
			return true;
		}
		
	}
	
	
}
