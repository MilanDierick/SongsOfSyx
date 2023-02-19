package launcher;


import static launcher.Resources.*;

import java.io.IOException;

import game.VERSION;
import init.paths.PATH;
import init.paths.PATHS.PATHS_BASE;
import launcher.Resources.GUI;
import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.Json;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.ACTION;
import snake2d.util.sprite.text.Text;

class ScreenMain extends GuiSection{

	private final Launcher l;
	private GuiSection message;
	private final PATH plang = PATHS_BASE.langs();
	
	ScreenMain(Launcher l){
		
		this.l = l;
		
		
		add(Sprites.smallPanel[0], 0, 0);
		for (int i = 0; i <= 3; i++)
			addDown(0, Sprites.smallPanel[1]);
		addDown(0, Sprites.smallPanel[2]);
		
		//BUTTONS
		
		GuiSection buttons = new GuiSection();
		
		CLICKABLE b;
		
		b = new GUI.Button.Text("LAUNCH");
		b.clickActionSet(new ACTION() {
			@Override
			public void exe() {
				l.setMods();
			}
		});
		buttons.add(b);
		
		
		b = new GUI.Button.Text("SETTINGS").clickActionSet(new ACTION() {
			@Override
			public void exe() {
				l.setSetts();
			}
		});
		buttons.addRightC(20, b);
		
		b = new GUI.Button.Text("INFO").clickActionSet(new ACTION() {
			
			@Override
			public void exe() {
				l.setInfo();
			}
		});
		buttons.addRightC(20, b);
		
		b = new GUI.Button.Text("EXIT").clickActionSet(new ACTION() {
			@Override
			public void exe() {
				Launcher.startGame = false;
				CORE.annihilate();
			}
		});
		buttons.addRightC(20, b);
		
		b = new GUI.Button.Sprite(Resources.Sprites.social[3]).clickActionSet(new ACTION() {
			
			@Override
			public void exe() {
				try {
					openBrowser("https://songsofsyx.mod.io/");
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		buttons.addRightC(20, b);
		b = new GUI.Button.Sprite(Resources.Sprites.social[2]).clickActionSet(new ACTION() {
			
			@Override
			public void exe() {
				try {
					openBrowser("https://discord.gg/eacfCuE");
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		buttons.addRightC(20, b);
		b = new GUI.Button.Sprite(Resources.Sprites.social[1]).clickActionSet(new ACTION() {
			
			@Override
			public void exe() {
				try {
					openBrowser("https://twitter.com/songsofsyx");
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		buttons.addRightC(5, b);
		b = new GUI.Button.Sprite(Resources.Sprites.social[0]).clickActionSet(new ACTION() {
			
			@Override
			public void exe() {
				try {
					openBrowser("https://www.youtube.com/channel/UCuWzoe8gnqI1brHv-k3oVyA");
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		buttons.addRightC(5, b);
		
		body().centerY(0, Sett.HEIGHT);
		body().centerX(0, Sett.WIDTH);
		
		add(Sprites.logo, (Sett.WIDTH-Sprites.logo.width())/2, body().y1()+80);
		
		buttons.body().centerX(0, Sett.WIDTH);
		buttons.body().moveY1(getLastY2()+10);
		

		
		add(buttons);
		
		b = new GUI.Button.Text("> " + VERSION.VERSION_STRING + " <").clickActionSet(new ACTION() {
			
			@Override
			public void exe() {
				l.setLog();
			}
		});
		add(b, 90,125);
		
		add(new LangButtMain(), 500, 125);
		
//		b = new GUI.Button.Sprite(Resources.Sprites.kickstarter) {
//			@Override
//			protected void clickA() {
//				
//			};
//		};
//		
//		b.body().moveX2(Sett.WIDTH-20);
//		b.body().moveY1(20);
//		add(b);
		

	}
	
	public static void openBrowser(String url) throws IOException {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.indexOf("win") >= 0) {
			Runtime rt = Runtime.getRuntime();
			rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
		}else if(os.indexOf("mac") >= 0) {
			Runtime rt = Runtime.getRuntime();
			rt.exec("open " + url);
		}else if(os.indexOf("nix") >=0 || os.indexOf("nux") >=0) {
			Runtime rt = Runtime.getRuntime();
			String[] browsers = { "epiphany", "firefox", "mozilla", "konqueror",
			                                 "netscape", "opera", "links", "lynx" };

			StringBuffer cmd = new StringBuffer();
			for (int i = 0; i < browsers.length; i++)
			    if(i == 0)
			        cmd.append(String.format(    "%s \"%s\"", browsers[i], url));
			    else
			        cmd.append(String.format(" || %s \"%s\"", browsers[i], url)); 

			rt.exec(new String[] { "sh", "-c", cmd.toString() });
		}
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		super.render(r, ds);
		if (message != null){
			OPACITY.O75.bind();
			COLOR.BLACK.render(r, 0, Sett.WIDTH, 0, Sett.HEIGHT);
			OPACITY.unbind();
			message.render(r, ds);
		}
		COLOR.unbind();
	};
	
	@Override
	public boolean hover(COORDINATE mCoo) {
		if (message != null)
			return message.hover(mCoo);
		else {
			return super.hover(mCoo);
		}
	};
	
	@Override
	public boolean click() {
		if (message != null)
			return message.click();
		else
			return super.click();
		
	};
	
	private class LangButtMain extends CLICKABLE.ClickableAbs{

		private final Text value = new Text(Sprites.font, 100).setScale(2);
		private final String ll = "Language: ";
		private final LangButt[] butts;
		private final GuiSection mFullScreens; 
		
		public LangButtMain() {
			body.setDim(300, value.height());
			String[] langs2 = plang.folders();
			butts = new LangButt[langs2.length+1];
			for (int i = 0; i < langs2.length; i++) {
				butts[i+1] = new LangButt(langs2[i]);
			}
			butts[0] = new LangButt(null);
			
			
			mFullScreens = new GuiSection();
			ScrollBox content = new ScrollBox(Sett.HEIGHT-100);
			for (LangButt b : butts) {
				content.add(b);
			}
			
			CLICKABLE up = new GUI.Button.Sprite(Sprites.arrowUpDown[0]).clickActionSet(new ACTION() {
				@Override
				public void exe() {
					content.scrollUp();
				}
			});
			mFullScreens.add(up);
			
			
			CLICKABLE down = new GUI.Button.Sprite(Sprites.arrowUpDown[1]).clickActionSet(new ACTION() {
				@Override
				public void exe() {
					content.scrollDown();
				}
			});
			down.body().moveY2(Sett.HEIGHT-120);
			
			mFullScreens.add(down);
			content.addNavButts(up, down);
			content.body().moveX1Y1(30, 0);
			mFullScreens.add(content);
			
			CLICKABLE can = new GUI.Button.Text("CANCEL") {
				@Override
				protected void clickA() {
					message = null;
				};
			};
			
			
			can.body().moveX1(80);
			can.body().moveY1(mFullScreens.getLastY2()+10);
			mFullScreens.add(can);
			mFullScreens.body().centerX(0, Sett.WIDTH);
			mFullScreens.body().centerY(0, Sett.HEIGHT);
			
			
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			if (isHovered)
				Gui.c_hover_selected.bind();
			value.clear();
			value.set(ll);
			LangButt s = selected();
			value.render(r, body.x1(), body.y1());
			s.value.render(r, body.x1()+value.width(), body.cY()-s.value.height()/2);
			COLOR.unbind();
			
		}
		
		@Override
		protected void clickA() {
			message = mFullScreens;
			super.clickA();
		}
		
		private LangButt selected() {

			for (int i = 1; i < butts.length; i++) {
				if (butts[i].code.equals(l.s.lang))
					return butts[i];
			}
			return butts[0];
		}
		
		
		private class LangButt extends CLICKABLE.ClickableAbs{

			private final Text value;
			private final String code;
			private final String name;
			
			public LangButt(String lang) {
				if (lang == null) {
					code = "";
					name = "English";
				}else {
					code = lang;
					Json j = new Json(plang.getFolder(lang).get("_Info"));
					name = j.text("NAME") + " " + (int) (100 * j.d("COVERAGE")) + "%";
				}
				value = new Text(Sprites.font, name).setScale(1);
				body.setDim(150, value.height());
				
			}
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
				if (isHovered)
					Gui.c_hover_selected.bind();
				value.render(r, body.x1(), body.y1());
				COLOR.unbind();
			}
			
			@Override
			protected void clickA() {
				l.s.lang = code;
				l.s.save();
				message = null;
			}
			
			
			
		}
	}
	


	
}
