package launcher;

import static launcher.Resources.*;

import game.VERSION;
import init.error.ErrorHandler;
import init.paths.ModInfo;
import init.paths.ModInfo.ModInfoException;
import init.paths.PATHS;
import launcher.Resources.GUI;
import snake2d.CORE;
import snake2d.Errors.GameError;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileManager;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.ACTION;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.Text;

class ScreenMods extends GuiSection{

	private ModInfo hoveredMod = null;
	private String errorMod = null;
	private String errorMessage = null;
	private Text hs = new Text(Sprites.font, 200).setScale(1);
	private final GuiSection mods = new GuiSection();
	private boolean fetching = false;
	private SteamAchieve steam = null;
	private final Launcher l;
	
	ScreenMods(Launcher l) {
		
		this.l = l;
		SPRITE[] panel = Sprites.smallPanel;
		add(panel[0], 0, 0);
		for (int i = 0; i <= 6; i++)
			addDown(0, panel[1]);
		addDown(0, panel[2]);
		
		GUI.RText rs = new GUI.RText.Header("PICK MODS");
		rs.body().moveX1Y1(100, 60);
		add(rs);
		
		mods.body().setHeight(body().height()-200);
		mods.body().incr(50, 110);
		add(mods);
		
		update(0);
		
		CLICKABLE b;
		
		
		
		
		
		b = new GUI.Button.Text("PLAY"){
			
			@Override
			protected void clickA() {
				
				l.s.save();
				Launcher.startGame = true;
				CORE.annihilate();
			}
			
			@Override
			protected void renAction() {
				activeSet(!fetching);
			}

		};
		b.body().moveX1(100);
		b.body().moveY1(315);
		add(b);
		
		rs = new GUI.RText.Normal("or");
		rs.body().moveX1Y1(getLastX2() + 15, 315);
		add(rs);
		
		b = new GUI.Button.Text("CANCEL").clickActionSet(new ACTION() {
			@Override
			public void exe() {
				l.setMain();
			}
		});
		b.body().moveX1Y1(getLastX2() + 15, 315);
		add(b);
		
		if (PATHS.isSteam() || PATHS.isDevelop()) {
			steam = new SteamAchieve();
			
			b = new GUI.Button.Text("sync steam") {
				boolean sentError = false;
				@Override
				protected void clickA() {
					fetching = true;
					steam.work();
				}
				
				@Override
				protected void renAction() {
					
					activeSet(steam.isDone() && !steam.isError());
					if (steam.exception() != null && ! sentError) {
						sentError = true;
						steam.exception().printStackTrace(System.err);
						new ErrorHandler().handle(new GameError(steam.exception().getMessage()), "error syncing steam");
					}
				}
				
				
			};
			
			b.body().moveX1Y1(getLastX2() + 100, 315);
			add(b);
			addRightC(8, Resources.Sprites.social[3]);
		}
		
		body().centerIn(0, Sett.WIDTH, 0, Sett.HEIGHT);
		
		int am = 0;
		for (String s : l.s.mods()) {
			if (PATHS.local().MODS.exists(s))
				am++;
		}
		String[] mods = new String[am];
		am = 0;
		for (String s : l.s.mods()) {
			if (PATHS.local().MODS.exists(s))
				mods[am++] = s;
		}
		l.s.setMods(mods);
		
		update(0);
	}
	
	private final String sOutdated = " (outdated!)";
	private final String sBy = "  by: ";
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		
			
		if (fetching && steam != null && (steam.isDone() || steam.isError())) {
			//update(ds);
			fetching = false;
		}
		
		super.render(r, ds);
		int sx = body().x1()+550;
		int y1 = body().y1()+70;
		if (hoveredMod != null) {
			
			hs.setMaxWidth(300);
			
			hs.clear().add(hoveredMod.name).add(' ').add(hoveredMod.version);
			COLOR.GREEN100.bind();
			if (hoveredMod.majorVersion != VERSION.VERSION_MAJOR) {
				COLOR.ORANGE100.bind();
				hs.add(sOutdated);
			}
			hs.adjustWidth();
			hs.render(r, sx, y1);
			y1 += hs.height();
			COLOR.unbind();
			
			hs.clear().add(hoveredMod.desc);
			hs.adjustWidth();
			hs.render(r, sx, y1);
			y1 += hs.height();
			
			COLOR.BLUEISH.bind();
			hs.clear().add(sBy).add(hoveredMod.author);
			hs.adjustWidth();
			hs.render(r, sx, y1);
			y1 += hs.height();
			COLOR.GREENISH.bind();
			hs.clear().add(hoveredMod.info);
			hs.adjustWidth();
			hs.render(r, sx, y1);
			y1 += hs.height();
			COLOR.unbind();
			
			hs.clear();
			hs.add(hoveredMod.absolutePath);
			hs.adjustWidth();
			hs.render(r, sx, y1);
			y1 += hs.height();
			
			hoveredMod = null;
		}else if (errorMod != null) {
			hs.clear();
			hs.add(errorMessage);
			hs.adjustWidth();
			hs.render(r, sx, y1);
			y1 += hs.height();
			
			hs.clear();
			hs.add(errorMod);
			hs.adjustWidth();
			hs.render(r, sx, y1);
		}
		errorMod = null;
	}
	
//	private boolean error = true;
	
	private void update(double ds) {
		
		
		String[] paths = PATHS.local().MODS.folders();
		
		ScrollBox labels = new ScrollBox(body().height()-200);
		
		for (String s : paths){
			ModInfo i;
			try {
				i = new ModInfo(s);
				labels.add(new ModButt(i));
			} catch (ModInfoException e) {
//				if (error)
//					e.printStackTrace(System.out);
				labels.add(new Borked(""+PATHS.local().MODS.getFolder(s).get().toAbsolutePath(), e.getMessage()));
			}
			
		}

//		error = false;
		
		labels.body().incr(120, 110);
		
		CLICKABLE up = new GUI.Button.Sprite(Sprites.arrowUpDown[0]).clickActionSet(new ACTION() {
			
			@Override
			public void exe() {
				labels.scrollUp();
			}
		}); 

		CLICKABLE down = new GUI.Button.Sprite(Sprites.arrowUpDown[1]).clickActionSet(new ACTION() {
			
			@Override
			public void exe() {
				labels.scrollDown();
			}
		});
		labels.addNavButts(up, down);
		
		int x = this.mods.body().x1();
		int y = this.mods.body().y1();
		this.mods.clear();
		this.mods.add(up);
		this.mods.add(down, 0, 140);
		
		this.mods.addRelBody(48, DIR.E, labels);
		this.mods.body().moveX1(x).moveY1(y);
		
	}
	
	private void toggle(ModButt butt) {
		
	}
	
	private final Str str = new Str(5);
	
	private class ModButt extends GUI.Button.Text{

		final ModInfo i;
		
		ModButt(ModInfo info) {
			super(info.name);
			i = info;
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			
			int selectedIndex = getS();
			isSelected = selectedIndex != -1;
			if (i.majorVersion != VERSION.VERSION_MAJOR)
				isActive = false;
				
			super.render(r, ds, isActive, isSelected, isHovered);
			if (isSelected) {
				Gui.c_hover_selected.bind();
				str.clear();
				str.add(selectedIndex);
				
				COLOR.unbind();	
				Resources.Sprites.font.render(r, str, body().x1()-32, body().y1(), 2);
			}
		}
		
		@Override
		protected void clickA() {
			
			int selectedIndex = getS();
			if (selectedIndex == -1) {
				String[] mods = new String[l.s.mods().length + 1];
				for (int i = 0; i < l.s.mods().length; i++)
					mods[i] = l.s.mods()[i];
				mods[mods.length-1] = i.path;
				l.s.setMods(mods);
			}else {
				
				String[] mods = new String[l.s.mods().length - 1];
				int k = 0;
				for (String s : l.s.mods()) {
					if (!s.equals(i.path)) {
						mods[k] = s;
						k++;
					}
				}
				l.s.setMods(mods);
			}
			
			
			toggle(this);
			super.clickA();
		}
		
		private int getS() {
			if (l.s.mods() == null)
				return -1;
			int k = 0;
			for (String s : l.s.mods()) {
				if (i.path.equals(s))
					return k;
				k++;
			}
			return -1;
		}
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			if (super.hover(mCoo)) {
				hoveredMod = i;
				return true;
			}
			return false;
		}
		
	}
	
	private final String sBroken = "Unsupported Mod";
	
	private class Borked extends GUI.Button.Text{

		final String path;
		final String message;
		
		Borked(String path, String message) {
			super(sBroken);
			this.path = path;
			this.message = message;
		}
		
		
		@Override
		protected void clickA() {
			FileManager.openDesctop(path);
		}
		
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			if (super.hover(mCoo)) {
				errorMod = path;
				errorMessage = message;
				return true;
			}
			return false;
		}
		
	}
	
	
//	static GUI_OBJE getModButt(String string){
//		
//		Text t = new Text(font).setScale(2).setText(string);
//		return Butt.getGreen(t);
//		
//	}
	
}
