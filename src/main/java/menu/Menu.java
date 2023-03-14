package menu;


import init.C;
import init.D;
import init.settings.S;
import init.sprite.UI.UI;
import integrations.INTEGRATIONS;
import integrations.INTER_RPC;
import snake2d.*;
import snake2d.CORE.GlJob;
import snake2d.KeyBoard.KeyEvent;
import snake2d.util.datatypes.*;
import snake2d.util.gui.GuiSection;
import snake2d.util.light.AmbientLight;
import snake2d.util.light.PointLight;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.TextureCoords;

public class Menu extends CORE_STATE{
	
	final ScMain main;
	final ScOptions options;
	final ScCampaign campaigns;
	final ScLoad load;
	final ScExamples examples;
	final ScRandom sandbox;
	final ScCredits credits;
	final ScHallLegends hallOfLegends;
	final ScHallHeroes hallOfHeroes;
	final ScHallFame hallOfFame;
	private SCREEN current;

	private Coo mCoo = new Coo();
	
	
	private final Background bg;
	
	private final PointLight mouseLight;
	
	private final Logo logo;
	private static boolean hasLogo = true;
	
	private final Intro intro;
	private static boolean hasIntro = true;
	private float fadeLight = 0;
	
	private final RPC rpc = new RPC();
	
	private final CharSequence ¤¤loading = "¤loading...";
	
	public static void start(){
		
		CORE.create(S.get().make());
		CORE.getInput().getMouse().showCusor(false);
		CORE.start(new CORE_STATE.Constructor() {
			@Override
			public CORE_STATE getState() {
				return make();
			}
		});
		
	}
	
	public static Menu make() {
		
		new GlJob() {
			@Override
			public void doJob() {
				RESOURCES.make();
				
				
			}
		}.perform();
		Menu menu = new Menu();
		PreLoader.exit();
		return menu;
	}
	
	private Menu(){
		
		D.t(this);
		
		final Rec bounds = new Rec(0, C.MIN_WIDTH, 0, 2*256);
		bounds.centerIn(C.DIM());
		
		
		GUI.init(bounds);

		bg = new Background(bounds);
		
		
		options = new ScOptions(this);
		sandbox = new ScRandom(this);
		campaigns = new ScCampaign(this);
		load = new ScLoad(this);
		hallOfLegends = new ScHallLegends(this);
		hallOfHeroes = new ScHallHeroes(this);
		hallOfFame = new ScHallFame(this);
		examples = new ScExamples(this);
		credits = new ScCredits(this);
		main = new ScMain(this);
		current = main;
		
		mouseLight = new PointLight(1.0f, 1.0f, 1.3f, 0, 0, 15);
		mouseLight.setFalloff(1);
		
		intro = new Intro(main, bg);
		
		logo = new Logo();

		S.get().applyRuntimeConfigs();
		
		
	}


	private void hover(COORDINATE mCoo, boolean mouseHasMoved) {
		
		this.mCoo.set(mCoo);
		if (hasIntro || hasLogo)
			return;
		
		
		mouseLight.set(mCoo);
		current.hover(mCoo);
	}

	@Override
	public void update(float ds, double slow) {
		hover(CORE.getInput().getMouse().getCoo(), false);
		INTEGRATIONS.updateRPC(rpc);
		if (hasLogo) {
			hasLogo = logo.update(ds);
			return;
		}
		
		RESOURCES.sound().play();
		
		hasIntro = hasIntro && intro.update(ds);
		if (!hasIntro && fadeLight < 1) {
			fadeLight += ds;
			if (fadeLight > 1)
				fadeLight = 1;
		}
	}
	
	
	@Override
	public void render(Renderer r, float ds) {
		CORE.renderer().shadeLight(true);
		CORE.renderer().shadowDepthDefault();
		if (hasLogo) {
			logo.render(r, ds);
			return;
		}
		
		if (hasIntro) {
			intro.render(r, ds);
			return;
		}
		
		AmbientLight.Strongmoonlight.register(C.DIM());
		UI.decor().mouse.render(r, mCoo.x(), mCoo.y());
		r.newLayer(true, 0);
		
		mouseLight.setRed(fadeLight);
		mouseLight.setGreen(fadeLight);
		mouseLight.setBlue(fadeLight*1.3);
		
		mouseLight.register();
		current.render(rr, ds);
		
		r.newLayer(false, 0);
		
		current.renderBackground(bg, ds, mCoo);
		
	}

	@Override
	public void mouseClick(MButt button) {
		
		if (hasLogo) {
			hasLogo = false;
			return;
		}
		
		if (hasIntro) {
			hasIntro = false;
			return;
		}
		
		
		if (button == MButt.LEFT){
			current.click();
		}
		if (button == MButt.RIGHT)
			current.back(this);
		
		
		
	}

	void switchScreen(SCREEN screen){
		current = screen;
		current.hover(mCoo);
	}
	
	SCREEN screen() {
		return current;
	}
	
	Coo getMCoo(){
		return mCoo;
	}
	
	void start(Constructor state){
		CORE.renderer().clear();
		
		
		GuiSection s = new GuiSection();
		GUI.addTitleText(s, ¤¤loading);
		s.body().centerIn(C.DIM());
		s.render(rr, 0);
		AmbientLight.Strongmoonlight.register(C.DIM());
		CORE.renderer().newLayer(false, 0);
		bg.render(CORE.renderer(), 0);
		
		CORE.swapAndPoll();
		CORE.setCurrentState(state);
	}

	@Override
	protected void keyPush(LIST<KeyEvent> keys, boolean hasCleared) {
		for (int i = 0; i < keys.size(); i++) {
			KeyEvent key = keys.get(i);
			if (hasLogo) {
				hasLogo = false;
				return;
			}
			
			if (hasIntro) {
				hasIntro = false;
				return;
			}
			if (key.code() == KEYCODES.KEY_ESCAPE) {
				if (!current.back(this)) {
					return;
				}
				break;
			}else {
				current.poll(key);
			}
			
		}
		
	}
	
	private final static class RPC implements INTER_RPC{

		private final String title = "Exploring the menu";
		private final String[] details = new String[0];
		
		@Override
		public String rpcTitle() {
			return title;
		}

		@Override
		public String[] rpcDetails() {
			return details;
		}
		
	}
	
	private final SPRITE_RENDERER rr = new SPRITE_RENDERER() {
		
		private final int ss = 4;
		private final int si = 8;

		@Override
		public void renderSprite(int x1, int x2, int y1, int y2, TextureCoords texture) {
			CORE.renderer().renderSprite(x1, x2, y1, y2, texture);
			for (int i = 0; i < si; i++)
				CORE.renderer().renderShadow(x1+ss+i, x2+ss+i, y1-ss-i, y2-ss-i, texture, (byte)0);
		}
	};
	
}
