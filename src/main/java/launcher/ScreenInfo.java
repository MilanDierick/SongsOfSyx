
package launcher;

import static launcher.Resources.*;

import java.io.IOException;
import java.util.Locale;

import game.VERSION;
import init.paths.PATHS;
import launcher.Resources.GUI;
import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.file.FileManager;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.Text;

class ScreenInfo extends GuiSection{
	
	private Str hoverInfo = new Str(200);
	
	ScreenInfo(Launcher l){
		
		SPRITE[] panel = Sprites.smallPanel;
		add(panel[0], 0, 0);
		for (int i = 0; i <= 6; i++)
			addDown(0, panel[1]);
		addDown(0, panel[2]);
		
		String[] keys = new String[] {
			"Game Version",
			"Platform",
			"JRE",
			"Graphics Card",
			"Graphics Driver"
		};
		String[] values = new String[]{
			VERSION.VERSION_STRING,
			System.getProperty("os.name", "generic").toLowerCase(Locale.ROOT),
			System.getProperty("java.version") + " bits:" + System.getProperty("sun.arch.data.model"),
			CORE.getGraphics().render(),
			CORE.getGraphics().renderV(),
		};
		
		int y1 = 60;
		for (int i = 0; i < keys.length; i++){
			RENDEROBJ r = new RENDEROBJ.Sprite(new Text(Sprites.font, keys[i]).setScale(1)).setColor(COLOR.BLUE100);
			r.body().moveY1(y1+=r.body().height());
			r.body().moveX1(40);
			add(r);
			r = new RENDEROBJ.Sprite(new Text(Sprites.font, values[i]).setScale(1));
			addRight(10, r);
		}
		
		{
			RENDEROBJ r = new RENDEROBJ.Sprite(new Text(Sprites.font, "local Files").setScale(1)).setColor(COLOR.BLUE100);
			r.body().moveY1(y1+=r.body().height());
			r.body().moveX1(40);
			add(r);
			CLICKABLE c = new ClickableAbs() {
				Text t = new Text(Sprites.font, ""+PATHS.local().ROOT.get()).setScale(1);
				{
					body.setDim(t);
				}
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
					if (isHovered)
						COLOR.GREEN100.bind();
					else
						COLOR.YELLOW100.bind();
					t.render(r, body);
					COLOR.unbind();
				}
				
				@Override
				protected void clickA() {
					FileManager.openDesctop(""+PATHS.local().ROOT.get());
				}
			};
			addRight(10, c);
			r = new RENDEROBJ.Sprite(new Text(Sprites.font, "saves").setScale(1)).setColor(COLOR.BLUE100);
			r.body().moveY1(y1+=r.body().height());
			r.body().moveX1(40);
			add(r);
			c = new ClickableAbs() {
				Text t = new Text(Sprites.font, ""+PATHS.local().SAVE.get()).setScale(1);
				{
					body.setDim(t);
				}
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
					if (isHovered)
						COLOR.GREEN100.bind();
					else
						COLOR.YELLOW100.bind();
					t.render(r, body);
					COLOR.unbind();
				}
				
				@Override
				protected void clickA() {
					FileManager.openDesctop(""+PATHS.local().SAVE.get());
				}
			};
			addRight(10, c);
			r = new RENDEROBJ.Sprite(new Text(Sprites.font, "screenshots").setScale(1)).setColor(COLOR.BLUE100);
			r.body().moveY1(y1+=r.body().height());
			r.body().moveX1(40);
			add(r);
			c = new ClickableAbs() {
				Text t = new Text(Sprites.font, ""+PATHS.local().SCREENSHOT.get()).setScale(1);
				{
					body.setDim(t);
				}
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
					if (isHovered)
						COLOR.GREEN100.bind();
					else
						COLOR.YELLOW100.bind();
					t.render(r, body);
					COLOR.unbind();
				}
				
				@Override
				protected void clickA() {
					FileManager.openDesctop(""+PATHS.local().SCREENSHOT.get());
				}
			};
			addRight(10, c);
			
			r = new RENDEROBJ.Sprite(new Text(Sprites.font, "mods").setScale(1)).setColor(COLOR.BLUE100);
			r.body().moveY1(y1+=r.body().height());
			r.body().moveX1(40);
			add(r);
			c = new ClickableAbs() {
				Text t = new Text(Sprites.font, ""+PATHS.local().MODS.get()).setScale(1);
				{
					body.setDim(t);
				}
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
					if (isHovered)
						COLOR.GREEN100.bind();
					else
						COLOR.YELLOW100.bind();
					t.render(r, body);
					COLOR.unbind();
				}
				
				@Override
				protected void clickA() {
					FileManager.openDesctop(""+PATHS.local().MODS.get());
				}
			};
			addRight(10, c);
			
			r = new RENDEROBJ.Sprite(new Text(Sprites.font, "contact").setScale(1)).setColor(COLOR.BLUE100);
			r.body().moveY1(y1+=r.body().height());
			r.body().moveX1(40);
			add(r);
			c = new ClickableAbs() {
				Text t = new Text(Sprites.font, "info@songsofsyx.com").setScale(1);
				{
					body.setDim(t);
				}
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
					if (isHovered)
						COLOR.GREEN100.bind();
					else
						COLOR.YELLOW100.bind();
					t.render(r, body);
					COLOR.unbind();
				}
				
				@Override
				protected void clickA() {
					FileManager.sendEmail("info@songsofsyx.com", "Greetings, oh great dev", "Inquiry");
				}
			};
			addRight(10, c);
			
			r = new ClickableAbs() {
				Text t = new Text(Sprites.font, "patch-notes").setScale(1);
				{
					body.setDim(t);
				}
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
					if (isHovered)
						COLOR.GREEN100.bind();
					else
						COLOR.YELLOW100.bind();
					t.render(r, body);
					COLOR.unbind();
				}
				
				@Override
				protected void clickA() {
					l.setLog();
				}
			};
			r.body().moveY1(y1+=r.body().height()+16);
			r.body().moveX1(40);
			add(r);
			
			r = new ClickableAbs() {
				Text t = new Text(Sprites.font, "roadmap").setScale(1);
				{
					body.setDim(t);
				}
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
					if (isHovered)
						COLOR.GREEN100.bind();
					else
						COLOR.YELLOW100.bind();
					t.render(r, body);
					COLOR.unbind();
				}
				
				@Override
				protected void clickA() {
					try {
						ScreenMain.openBrowser("https://trello.com/b/wF5RYqdF/songs-of-syx");
						
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
			r.body().moveY1(y1+=r.body().height());
			r.body().moveX1(40);
			add(r);
		}
		
		RENDEROBJ b = new GUI.Button.Text("CANCEL").clickActionSet(new ACTION() {
			@Override
			public void exe() {
				l.setMain();
			}
		});
		b.body().moveX2(Sett.WIDTH - 40).moveY2(Sett.HEIGHT-100);
		add(b);
		
		
		body().centerIn(0, Sett.WIDTH, 0, Sett.HEIGHT);
		
	
		
	}

	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		super.render(r, ds);
		if (hoverInfo.length() != 0) {
			Gui.c_label.bind();
			Sprites.font.render(r, hoverInfo, 40, 315, 450, 1);
			hoverInfo.clear();
		}
		COLOR.unbind();
	};
	

	
	
}
