
package launcher;

import static launcher.Resources.*;

import init.C;
import launcher.LSettings.LSetting;
import launcher.Resources.GUI;
import snake2d.*;
import snake2d.Displays.DisplayMode;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.ACTION;
import snake2d.util.process.Proccesser;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.Text;

class ScreenSetting extends GuiSection{

	private final Launcher l;
	
	private final GuiSection fullScreen = new GuiSection() {
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			visableSet(l.s.screenMode.get() == LSettings.screenModeFull);
			if (visableIs())
				super.render(r, ds);
		}
	};
	private final GuiSection windowed  = new GuiSection() {
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			visableSet(l.s.screenMode.get() == LSettings.screenModeWindowed);
			if (visableIs())
				super.render(r, ds);
		}
	};
	
	private final ScrollBox content;
	
	private GuiSection message;
	private final GuiSection mFullScreens;
	
	private Str hoverInfo = new Str(200);
	
	ScreenSetting(Launcher l){
		
		this.l = l;
		final int m = 5;
		final int x1 = 40;
		
		{
			SPRITE[] panel = Sprites.smallPanel;
			add(panel[0], 0, 0);
			for (int i = 0; i <= 6; i++)
				addDown(0, panel[1]);
			addDown(0, panel[2]);
			
			
		}
		
		{
			CLICKABLE c = new CheckBox(l.s.debug, "Debug", "Starts the game in debug mode and will print extra information and diagnostics when enabled at the cost of performance. Helpful when modding.");
			c.body().moveY1(70);
			c.body().moveX1(x1);
			add(c);
			
			c = new CheckBox(l.s.developer, "Developer", "Enables powerful tools in game that can be used to test things, or cheat.");
			c.body().moveY1(getLastY1());
			c.body().moveX1(x1 + 200);
			add(c);
			

			c = new CheckBox(l.s.linear, "Linear", "Enables linear filtering when the game is scaled.");
			c.body().moveY1(getLastY1());
			c.body().moveX1(x1 + 2*200);
			add(c);
			
			if (!Proccesser.isMac()) {
				c = new CheckBox(l.s.rpc, "RPC", "Enables rich presense, that will send out information to other apps such as discord about your game for others to see.");
				c.body().moveY1(getLastY1());
				c.body().moveX1(x1 + 3*200);
				add(c);
			}
			
			c = new CheckBox(l.s.shading, "Shading", "Use normal maps and dynamic lighning");
			c.body().moveY1(getLastY2());
			c.body().moveX1(x1 + 0);
			add(c);
			
			c = new CheckBox(l.s.vsync, "VSync", "Enables vsync to reduce screen tearing. Can cause conflicts with NVidia GSync in which case it's recommended to turn that off.");
			c.body().moveY1(getLastY1());
			c.body().moveX1(x1 + 200);
			add(c);
			
			c = new CheckBox(l.s.easy, "UI-Easy", "Only works with the default english language. Replaces the fonts with open sans, and tweeks UI colors to make things more clear.");
			c.body().moveY1(getLastY1());
			c.body().moveX1(x1 + 2*200);
			add(c);
		}
		
		
		{
			CLICKABLE c;
			
			c = new Clicker("Audio", "What audio device to use. If empty, the game can not find a device with openal support. Try fiddling with headphones and jacks if you have a problem.") {
				private final String none = "none";
				private final String def = "default";
				@Override
				void set(Text t) {
					t.set(get());
					t.setMaxChars(40);
					
				}
				private double timer = 5;
				private String get() {
					String a = l.s.audiodevice;
					if (a == null) {
						return none;
					}else if(a.isEmpty()) {
						return def;
					}else {
						for (String s : SoundDevices.get()) {
							if (s.equalsIgnoreCase(a)) {
								return s;
							}
						}

						if (SoundDevices.get().size() > 0)
							return SoundDevices.get().get(0);
						
						return none;
					}

					
				}
				
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
						boolean isHovered) {
					super.render(r, ds, isActive, isSelected, isHovered);
					timer-= ds;
					if (timer < 0) {
						timer = 5;
						SoundDevices.refresh();
					}
				}
				
				@Override
				protected void clickA() {
					GuiSection mFullScreens = new GuiSection();
					ScrollBox content = new ScrollBox(Sett.HEIGHT-100);
					
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
					
					content.add(new GUI.Button.Text("none").clickActionSet(new ACTION() {
						@Override
						public void exe() {
							l.s.audiodevice = null;
							message = null;
						}
					}));
					
					content.add(new GUI.Button.Text("default").clickActionSet(new ACTION() {
						@Override
						public void exe() {
							l.s.audiodevice = "";
							message = null;
						}
					}));
					
					for (String s : SoundDevices.get()) {
						content.add(new GUI.Button.Text(s).clickActionSet(new ACTION() {
							@Override
							public void exe() {
								l.s.audiodevice = s;
								message = null;
							}
						}));
					}
					
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
					
					message = mFullScreens;
				}
				
			};
			
			
			add(c, x1+280, getLastY2()+m);
			
			
//			c = new Multi(l.s.audioDevice, "AUDIO", "What audio device to use. If empty, the game can not find a device with openal support. Try fiddling with headphones and jacks if you have a problem.", 400) {
//				private double timer = 5;
//				private String none = "none";
//				@Override
//				public void setValue(Text v, LSetting s) {
//					if (s.get() == 0) {
//						v.set(none);
//					}else {
//						int i = s.get()-1;
//						if (i < 0 || i >= SoundDevices.get().size())
//							v.set(none);
//						else
//							v.set(SoundDevices.get().get(i));
//						
//					}
//				}
//				
//				@Override
//				public void render(SPRITE_RENDERER r, float ds) {
//					timer-= ds;
//					if (timer < 0) {
//						timer = 5;
//						SoundDevices.refresh();
//					}
//					super.render(r, ds);
//				}
//			};
//			c.body().moveY1(getLastY1());
//			c.body().moveX1(x1+280);
//			add(c);
			

		}
		
		{
			
			CLICKABLE c = new Multi(l.s.screenMode, "Screen", "The type of display to be created for the game.", 200) {
				int i = -1;
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					if (i != l.s.monitor.get()) {
						i = l.s.monitor.get();
						settResolutions();
					}
					super.render(r, ds);
				}
				
				private String[] vs = new String[] {
					"Borderless",
					"Full",
					"Windowed"
				};
				
				@Override
				public void setValue(Text v, LSetting s) {
					v.add(vs[s.get()]);
				}
				
			};
			c.body().moveY1(getLastY2()+m*3);
			c.body().moveX1(x1);
			add(c);
			
			c = new Multi(l.s.monitor, "Monitor", "Which monitor to start the game in.") {
				int i = -1;
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					if (i != l.s.monitor.get()) {
						i = l.s.monitor.get();
						settResolutions();
					}
					super.render(r, ds);
				}
				
			};
			c.body().moveY1(getLastY1());
			c.body().moveX1(x1 + 450);
			add(c);
			
			
		}
		
		{
		
			CLICKABLE b = new Clicker("Resolution", "What resolution to use in full screen mode") {
				
				@Override
				void set(Text t) {
					t.clear();
					DisplayMode d = Displays.available(l.s.monitor.get()).get(l.s.fullScreenDisplay.get());
					t.add(d.width).add(' ').add('x').add(' ').add(d.height).add(' ').add('@').add(d.refresh).add('H').add('z');
				}
				
				@Override
				protected void clickA() {
					message = mFullScreens;
				}
			};
			

			fullScreen.addRightC(32, b);
			CLICKABLE d = new CheckBox(l.s.fill, "Fill", "Stretches the defined window over the whole screen.");
			d.body().moveX1(3*200);
			fullScreen.add(d, 3*200, 0);
			
			
			fullScreen.body().moveY1(getLastY2()+m);
			fullScreen.body().moveX1(x1);
			
			add(fullScreen);
			
			mFullScreens = new GuiSection();
			content = new ScrollBox(Sett.HEIGHT-100);
			
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
		
		
		
		{
			CLICKABLE c = new Multi(l.s.windowWidth, "Width", "The width of the window") {
				
				@Override
				public void setValue(Text v, LSetting s) {
					v.add((int)(s.getD()*100));
					v.add('%');
				}
			};
			windowed.add(c);
			c = new Multi(
					l.s.windowHeight, "Height", "The height of the window") {
				@Override
				public void setValue(Text v, LSetting s) {
					v.add((int)(s.getD()*100));
					v.add('%');
				}
			};
			windowed.addDown(0, c);
			
			
			CLICKABLE d = new CheckBox(l.s.fill, "Fill", "Stretches the defined window over the whole screen.");
			d.body().moveX1(3*200);
			windowed.add(d);
			windowed.body().moveY1(getLastY1());
			windowed.body().moveX1(x1);
			
			d = new CheckBox(l.s.decorated, "Borders", "Use borders and system decoration on the window");
			windowed.addDown(0, d);
			
			add(windowed);
		}

		
		CLICKABLE b = new GUI.Button.Text("RESET").clickActionSet(new ACTION() {
			@Override
			public void exe() {
				l.s.setDefault();
			}
		});
		b.body().moveX1(550).moveY2(body().y2()-30);
		add(b);
		
		b = new GUI.Button.Text("BACK").clickActionSet(new ACTION() {
			@Override
			public void exe() {
				exit();
			}
		});
		b.body().moveX1(getLastX2() + 40).moveY2(getLastY2());
		add(b);
		
		body().centerIn(0, Sett.WIDTH, 0, Sett.HEIGHT);
		settResolutions();
		
	}

	
	private CLICKABLE resButt(int i) {
		
		
		
		DisplayMode d = Displays.available(l.s.monitor.get()).get(i);
		
		COLOR c = COLOR.WHITE100;
		if (d.width < C.MIN_WIDTH) {
			c = COLOR.RED100;
		}else if (d.height < C.MIN_HEIGHT) {
			c = COLOR.RED100;
		}else {
			double a = d.width*d.height;
			if (a > C.MAX_SCREEN_AREA) {
				c = COLOR.ORANGE100;
			}
		}
		final COLOR c2 = c;
		
		CLICKABLE b = new GUI.Button.Text(Displays.available(l.s.monitor.get()).get(i).toString()) {
			
			@Override
			protected void clickA() {
				l.s.fullScreenDisplay.set(i);
				message = null;
			}
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
					boolean isHovered) {
				c2.bind();
				super.render(r, ds, isActive, isSelected, isHovered);
			}
		};
		
		return b;
		
	}
	
	private void settResolutions() {
		content.clear();
		for (int i = 0; i < Displays.available(l.s.monitor.get()).size(); i++) {
			CLICKABLE bb = resButt(i);
			content.add(bb);
		}
	}
	
	private void exit(){
		message = null;
		
		l.setMain();
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		super.render(r, ds);
		if (hoverInfo.length() != 0) {
			Gui.c_label.bind();
			Sprites.font.render(r, hoverInfo, 40, 300, 450, 1);
			hoverInfo.clear();
		}
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
	
	private abstract class Clicker extends CLICKABLE.ClickableAbs{

		private final SPRITE s;
		private Text t = new Text(Sprites.font, 64).setScale(1.5);

		Clicker(String name, String desc) {
			this.s = new Text(Sprites.font, name).setScale(1.5);
			hoverInfoSet(desc);
			body.setHeight(s.height());
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive,
				boolean isSelected, boolean isHovered) {
			
			set(t);
			t.adjustWidth();
			body.setWidth(s.width() + 10 + t.width());
			
			
			if (!isActive)
				Gui.c_inactive.bind();
			else if (isHovered && isSelected)
				Gui.c_hover_selected.bind();
			else if (isHovered)
				Gui.c_hover.bind();
			else if (isSelected)
				Gui.c_selected.bind();
			s.render(r, body.x1(), body.y1());
			t.renderCY(r, body().x1()+s.width()+10, body().cY());
			COLOR.unbind();

		}
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			if (super.hover(mCoo) && this.hoverInfo != null) {
				ScreenSetting.this.hoverInfo.add(this.hoverInfo);
				return true;
			}
			return false;
		}
		
		abstract void set(Text t);
	}
	
	private class CheckBox extends CLICKABLE.ClickableAbs{

		private final SPRITE s;
		private final LSetting b;

		CheckBox(LSetting b, String name, String desc) {
			this.b = b;
			this.s = new Text(Sprites.font, name).setScale(1.5);
			body.setWidth(Sprites.checkBox1.width() + 10 + s.width()).setHeight(s.height());
			hoverInfoSet(desc);
		}
		
		@Override
		protected void clickA() {
			b.set((b.get()+1)&0b01);
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive,
				boolean isSelected, boolean isHovered) {
			
			isSelected = b.get() == 1;
			
			SPRITE box = isSelected ? Sprites.checkBox2 : Sprites.checkBox1;
			int y1 = (s.height()-box.height())/2;
			box.render(r, body.x1(), body.y1()+y1);
			
			
			if (!isActive)
				Gui.c_inactive.bind();
			else if (isHovered && isSelected)
				Gui.c_hover_selected.bind();
			else if (isHovered)
				Gui.c_hover.bind();
			else if (isSelected)
				Gui.c_selected.bind();
			s.render(r, body.x1()+box.width()+10, body.y1());
			COLOR.unbind();

		}
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			if (super.hover(mCoo) && this.hoverInfo != null) {
				ScreenSetting.this.hoverInfo.add(this.hoverInfo);
				return true;
			}
			return false;
		}

	}
	
	private class Multi extends GuiSection{

		private final SPRITE s;
		private final Text value = new Text(Sprites.font, 100).setScale(1.5);
		private final CLICKABLE left = new GUI.Button.Sprite(Sprites.arrowLR[0]) {
			@Override
			protected void clickA() {
				b.inc(-1);
			};
			
			@Override
			protected void renAction() {
				activeSet(b.get() > b.min());
			};
		};
		private final CLICKABLE right = new GUI.Button.Sprite(Sprites.arrowLR[1]) {
			@Override
			protected void clickA() {
				b.inc(1);
			};
			
			@Override
			protected void renAction() {
				activeSet(b.get() < b.max());
			};
		};
		
		private final LSetting b;
		private int width;
		private String desc;
		
		Multi(LSetting b, String name, String desc) {
			this(b, name, desc, 100);
		}
		
		Multi(LSetting b, String name, String desc, int w) {
			this.b = b;
			s = new snake2d.util.sprite.text.Text(Sprites.font, name).setScale(1.5);
			add(s, 0, 0);
			hoverInfoSet(desc);
			this.desc = desc;
			width = w;
			addRightC(25,left);
			addRightC(width, right);
		}
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			value.clear();
			setValue(value, b);
			value.adjustWidth();
			int dx = (width-value.width())/2;
			value.render(r, left.body().x2()+dx, body().y1());
			super.render(r, ds);
		}
		
		public void setValue(Text v, LSetting s) {
			v.add(s.get());
		}
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			if (super.hover(mCoo)) {
				ScreenSetting.this.hoverInfo.add(desc);
				return true;
			}
			return false;
		}

	}
	
	
	
	
}
