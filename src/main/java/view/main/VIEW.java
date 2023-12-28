package view.main;


import java.io.IOException;

import game.*;
import game.time.TIME;
import init.C;
import init.RES;
import snake2d.*;
import snake2d.KeyBoard.KeyEvent;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.*;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.dic.DicMisc;
import util.gui.misc.GBox;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;
import view.battle.BattleView;
import view.interrupter.InterManager;
import view.keyboard.KEYS;
import view.keyboard.KeyPoller;
import view.sett.SettView;
import view.ui.UIView;
import view.ui.message.Messages;
import view.world.WorldView;
import view.world.generator.WorldViewGenerator;
import world.WORLD;

public class VIEW extends CORE_STATE{
	
	private static VIEW i;

	{
		KEYS.init();
	}
	private KeyPoller keyPoller = KEYS.get();
	private final UIView ui;
	private final WorldView world;
	private final SettView sett;
	private final BattleView battle;
	private ViewSubSimple current;
	private ViewSub previous;
	
	private final Mouse mouse;
	
	private final Interrupters inters;
	private boolean hideUI = false;
	private static double renderSecond;
	public int renI;
	
	public VIEW(){
		
		i = this;
		ViewSub.all.clear();
		mouse = new Mouse();
		inters = new Interrupters();
		ui = new UIView();
		world = new WorldView();
		sett = new SettView();
		battle = new BattleView();
		world.activate();
		KEYS.get().readSettings();
		setFirstView(world);
		

	}
	
	public final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter saveFile) {
			saveFile.mark(this);
			inters.messages.saver.save(saveFile);
			
			if (current instanceof ViewSub)
				saveFile.i(((ViewSub)current).index);
			else
				saveFile.i(-1);
			
			
			for (ViewSub s : ViewSub.all) {
				saveFile.mark(s);
				s.save(saveFile);
			}
		}
		
		@Override
		public void load(FileGetter saveFile) throws IOException {
			saveFile.check(this);
			inters.messages.saver.load(saveFile);
			
			int si = saveFile.i();
			ViewSub v = null;
			if (si >= 0)
				v = ViewSub.all.get(si);
			
			
			for (ViewSub s : ViewSub.all) {
				saveFile.check(s);
				s.load(saveFile);
			}
			KEYS.get().readSettings();
			setFirstView(v);
			current.activate();
			
			
		}
		
		@Override
		public void clear() {
			// TODO Auto-generated method stub
			
		}
	};

	private void setFirstView(ViewSubSimple prefered) {
		if (prefered == null || !WORLD.GEN().isDone) {
			prefered = new WorldViewGenerator();
		}
		prefered.activate();
		previous = null;
		
	}
	

	@Override
	protected void keyPush(LIST<KeyEvent> keys, boolean hasCleared) {
		keyPoller.poll(keys);
		keyPoller = KEYS.get();
	}
	

	@Override
	protected void mouseClick(MButt button) {
		if (!inters.manager.click(button))
			return;
		
		if (inters.mouseMessage.close())
			return;
		GAME.script().mouseClick(button);
//		hoverTimer = 0;
		if (current.uiManager.click(button))
			current.mouseClick(button);
//		hoverClick = true;
		
	}

	
	private double hoverTimer = 0;

	private void hover() {
		
		COORDINATE mCoo = CORE.getInput().getMouse().getCoo();
		
		int dx = mCoo.x()-mouse.x();
		int dy = mCoo.y()-mouse.y();
		int d = dx*dx+dy*dy;
		boolean mouseHasMoved = d > 5;
		mouse.getCoo().set(mCoo);
		
		GAME.script().hover(mCoo, mouseHasMoved);
		
		
		
		if (mouseHasMoved){
			hoverTimer = 0;
//			hoverClick = false;
		}
		
		if (inters.manager.hover(mCoo, mouseHasMoved))
			if (current.uiManager.hover(mCoo, mouseHasMoved))
				current.hover(mCoo, mouseHasMoved);
		
		if (hoverTimer >= 0.4) {
			if (inters.manager.hoverTimer(hoverTimer, inters.mouseMessage.get())) 
				if (current.uiManager.hoverTimer(hoverTimer, inters.mouseMessage.get()))
					current.hoverTimer(hoverTimer, inters.mouseMessage.get());
		}
	}
	
	@Override
	protected void update(float ds, double slowDown) {
		

		
		GAME.afterTick();

		inters.manager.afterTick();
		current.uiManager.afterTick();
		current.afterTick();

		
		
		hover();
		
		//inters.mouseMessage.close();
		hoverTimer += ds;

		if (KEYS.MAIN().DEBUGGER.consumeClick())
			RES.debugger().toggle();
		if (hideUI) {
			if (KEYS.MAIN().ESCAPE.consumeClick() | MButt.RIGHT.consumeAllClick()) {
				hideUI = false;
				inters.mouseMessage.get().clear();
			}
		}
			

		inters.mouseMessage.update(mouse);
		if (inters.manager.update(ds) & current.uiManager.update(ds)) {
			current.update(ds, true);
		}else {
			current.update(ds, false);
			ds = 0;
			slowDown = 1.0;
		}

		if (KEYS.MAIN().ESCAPE.consumeClick()) {
			inters.menu.show(); 
		}
		
		if (KEYS.MAIN().QUICKSAVE.consumeClick() && canSave()) {
			RES.loader().minify(true, DicMisc.¤¤SAVING);
			RES.saver().quicksave();
			RES.loader().minify(false, DicMisc.¤¤SAVING);
		}
		
		if (KEYS.MAIN().QUICKLOAD.consumeClick() && GameLoader.quickload()) {
			RES.loader().minify(true, DicMisc.¤¤load);
			return;
		}
		
		GAME.update(ds, slowDown);

	}
	
	@Override
	protected void render(Renderer r, float ds) {
		
		renI++;
		renderSecond += ds;
		if (renderSecond > 10000)
			renderSecond -= 10000;
		
		RES.debugger().flush();
		
		if (hideUI) {
			current.render(r, ds, true);
			
			return;
		}
		

		
		
		
		TIME.light().applyGuiLight(ds, C.DIM());
		GAME.script().render(r, ds);
		
		mouse.render(r, ds);
		r.newLayer(true, 0);
		
		inters.mouseMessage.render(r, ds);
		
		
		
		
		if (!inters.manager.render(r, ds)) {
			return;
		}
		
		if (!current.uiManager.render(r, ds))
			return;
		
		current.render(r, ds, false);

	}
	
	public static void render() {
		i.render(CORE.renderer(), 0);
		i.inters.manager.afterTick();
		i.current.uiManager.afterTick();
		i.current.afterTick();
	}
	
	public static Mouse mouse(){
		return i.mouse;
	}
	
	public static WorldView world(){
		return i.world;
	}
	
	public static SettView s(){
		return i.sett;
	}
	
	public static BattleView b(){
		return i.battle;
	}
	
	public static ViewSubSimple current() {
		return i.current;
	}

	public static void setPrev() {
		if (i.previous == null)
			i.world.activate();
		else
			i.previous.activate();
	}
	
	public static Interrupters inters(){
		return i.inters;
	}
	
	public static Messages messages() {
		return i.inters.messages;
	}
	
	public static GBox hoverBox() {
		return i.inters.mouseMessage.get();
	}
	
	public static GBox timeBox() {
		return i.inters.mouseMessage.init(i.mouse, true);
	}
	
	public static void hoverError(CharSequence s) {
		GBox b = i.inters.mouseMessage.init(i.mouse, true);
		b.add(b.text().errorify().add(s));
	}
	
	public static void hoverBoxDistance(int max) {
		
		i.inters.mouseMessage.setDistance(max);
		
	}
	
	public static GBox mouseBox(boolean time) {
		return i.inters.mouseMessage.init(i.mouse, time);
	}
	
	public static SAVABLE saver() {
		return i.saver;
	}
	
	public static boolean hideUI() {
		return i.hideUI;
	}
	
	public static void hide() {
		i.hideUI = true;
	}
	
	public static double renderSecond() {
		return renderSecond;
	}
	
	public static boolean existTemp() {
		return i != null;
	}
	
	public static UIView UI() {
		return i.ui;
	}
	
	
	public static int RI() {
		return i.renI;
	}
	
	public static void setKeyPoller(KeyPoller poller) {
		i.keyPoller = poller;
	}
	
	public static boolean canSave() {
		return i.inters.manager.canSave() && VIEW.current().uiManager.canSave() && VIEW.current().canSave();
	}
	
	@Override
	protected void exit() {
		GAME.count().flush();
	}
	
	public static abstract class ViewSubSimple{
		
		protected abstract void hoverTimer(double mouseTimer, GBox text);
		
		protected boolean canSave() {
			return true;
		}

		protected abstract boolean update(float ds, boolean shouldUpdate);
		protected abstract void render(Renderer r, float ds, boolean hide);
		
		public void renderBelowTerrain(Renderer r, ShadowBatch s, RenderData data) {
			
		}
		
		protected abstract void mouseClick(MButt button);
		protected abstract void hover(COORDINATE mCoo, boolean mouseHasMoved);
		public final InterManager uiManager = new InterManager();
		
		public void activate(){
			
			if (i.current == this)
				return;
			
			i.inters.mouseMessage.close();
			if (i.current instanceof ViewSub)
				i.previous = (ViewSub) i.current;
			i.current = this;
			hover(CORE.getInput().getMouse().getCoo(), true);
		}
		
		public final boolean isActive(){
			return this == VIEW.i.current;
		}
		
		protected void afterTick() {
			
		}
		
	}
	
	public static abstract class ViewSub extends ViewSubSimple{

		private static final ArrayList<ViewSub> all = new ArrayList<>(20);
		private final int index = all.add(this);
		
		public int index() {
			return index;
		}
		
		protected abstract void save(FilePutter file);
		protected abstract void load(FileGetter file) throws IOException;
		
		
		
	}

	
}
