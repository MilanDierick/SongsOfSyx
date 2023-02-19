package launcher;

import game.VERSION;
import init.D;
import init.error.ErrorHandler;
import init.paths.PATHS;
import snake2d.*;
import snake2d.CORE.GlJob;
import snake2d.KeyBoard.KeyEvent;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.gui.GuiSection;
import snake2d.util.sets.LIST;

public class Launcher extends CORE_STATE{
	
	//button related
	private GuiSection current;
	private final ScreenMain main;
	private final ScreenSetting setts;
	private final ScreenMods mods;
	private final ScreenInfo info;
	private final ScreenLog log;
	private final BG bg;
	private COORDINATE mCoo = new Coo();
	final Sett settings = new Sett();
	final LSettings s = new LSettings();
	
	static boolean startGame = false;
	
	public static void start(){
		startGame = false;
		CORE.create(new Sett());
		CORE.start(new Constructor() {
			@Override
			public CORE_STATE getState() {
				
				PreLoader.exit();
				return new Launcher();
			}
		});
		Resources.nullify();
		
	}
	
	public static void main(String[] args) {
    	CORE.init(new ErrorHandler());
    	PATHS.init(new String[0], null, false);
    	D.init();
		start();
		if (!startGame)
			System.exit(1);
		System.exit(0);
	}
	
	private Launcher(){
		
		
	
		new GlJob() {
			@Override
			public void doJob() {
				
				new Resources();
			}
		}.perform();

		
		main = new ScreenMain(this);
		setts = new ScreenSetting(this);
		mods = new ScreenMods(this);
		info = new ScreenInfo(this);
		log = new ScreenLog(this);
		

		if (s.version.get() != VERSION.VERSION) {
			s.save();
			current = log;
		}
		else
			current = main;
		
		bg = new BG();
		
	}

	@Override
	public void mouseClick(MButt button) {
		if (button == MButt.LEFT){
			current.click();
		}else if (button == MButt.RIGHT)
			setMain();
		
	}

	@Override
	public void update(float ds, double slow) {
		bg.update(ds);
		hover(CORE.getInput().getMouse().getCoo(), false);
	}

	@Override
	public void render(Renderer r, float ds) {
		
		bg.render(r, ds);
		
		current.render(r, ds);
		bg.renderClouds(r, ds);
		
	}


	//functions for the buttons
	
	void setInfo() {
		current = info;
		current.hover(mCoo);
	}
	
	void setMain(){
		current = main;
		current.hover(mCoo);
	}
	
	void setSetts(){
		current = setts;
		current.hover(mCoo);
	}
	
	void setMods(){
		current = mods;
		current.hover(mCoo);
	}
	
	void setLog(){
		current = log;
		current.hover(mCoo);
	}

	public void hover(COORDINATE mCoo, boolean mouseHasMoved) {
		this.mCoo = mCoo;
		current.hover(mCoo);
	}

	@Override
	protected void keyPush(LIST<KeyEvent> keys, boolean hasCleared) {
		
		for (KeyEvent c : keys) {
			if (CORE.getInput().getKeyboard().isPressed(KEYCODES.KEY_LEFT_CONTROL) && c.code() == KEYCODES.KEY_X)
				throw new RuntimeException("creating debugging info. Please send this to the developer if you're having issues");
			if (c.code() == KEYCODES.KEY_ESCAPE) {
				startGame = false;
				CORE.annihilate();
				return;
			}
		}
	}
	
}
