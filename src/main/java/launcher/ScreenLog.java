
package launcher;

import static launcher.Resources.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import launcher.Resources.GUI;
import snake2d.MButt;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.*;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.ACTION;

class ScreenLog extends GuiSection{

	private final Lines lines = new Lines();
	
	ScreenLog(Launcher l){
	
		
		body().setDim(Sett.WIDTH, Sett.HEIGHT);
		add(new GUI.RText.Header("Changelog"), 20, 16);
		
		
		lines.body().moveX1Y1(10, 60);
		add(lines);
		
		
		CLICKABLE up = new GUI.Button.Sprite(Sprites.arrowUpDown[0]).clickActionSet(new ACTION() {
			@Override
			public void exe() {
				lines.top -=5;
			}
		});
		up.body().moveY1(60+25).moveX2(Sett.WIDTH-20);
		add(up);
		
		CLICKABLE down = new GUI.Button.Sprite(Sprites.arrowUpDown[1]).clickActionSet(new ACTION() {
			@Override
			public void exe() {
				lines.top +=5;
			}
		});
		down.body().moveY2(Sett.HEIGHT-125).moveX2(Sett.WIDTH-20);
		add(down);
		
		
		
		CLICKABLE b = new GUI.Button.Text("BACK").clickActionSet(new ACTION() {
			@Override
			public void exe() {
				l.setMain();
			}
		});
		b.body().moveX2(Sett.WIDTH-20).moveY2(Sett.HEIGHT-25);
		add(b);
		
		//body().centerIn(0, Sett.WIDTH, 0, Sett.HEIGHT);
		
		
	}


	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		OPACITY.O75.bind();
		COLOR.BLACK.render(r, 0, Sett.WIDTH, 0, Sett.HEIGHT);
		OPACITY.unbind();
		COLOR.unbind();
		
		super.render(r, ds);
		
	};

	
	private static class Lines extends RenderImp {
		
		private CharSequence[] lines = lines();
		int top = 0;
		private final String sep = "-";
		private final COLOR[] cols = new COLOR[] {
			COLOR.WHITE100,
			new ColorImp(127, 110, 100),
		};

		Lines(){
			body().setWidth(Sett.WIDTH-150).setHeight(Sett.HEIGHT-80);
		}
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			int i = (int) MButt.clearWheelSpin();
			top -= i;
			
			if (top >= lines.length-1)
				top = lines.length-1;
			if (top < 0)
				top = 0;
			
			int y1 = body().y1();
			int line = top;
			while(line < lines.length && y1 < body().y2()) {
				y1 = render(y1, lines[line], r, cols[line&1]);
				line++;
			}
			
			
		}
		
		private int render(int y1, CharSequence s, SPRITE_RENDERER r, COLOR color) {
			int start = 0;
			color.bind();
			while(true) {
				int end = Sprites.font.getEndIndex(s, start, body().width());
				int y2 = y1 + Sprites.font.height();
				if (y2 < body().y2()) {
					if (start == 0) {
						if (s.length() > 0 && s.charAt(0) == '!') {
							start = 1;
							COLOR.BLUEISH.bind();
						}else if (s.length() > 0){
							Sprites.font.render(r, sep, body().x1(), y1);
						}
					}
					
					Sprites.font.render(r, s, body().x1()+20, y1, start, end, 1);
					
				}
				y1 = y2;
				start = end;
				if (start == s.length())
					break;
			}
			COLOR.unbind();
			return y1+2;
		}
		
		private CharSequence[] lines() {
			String[] lines = new String[0];
			try {
				InputStream fis = getClass().getResourceAsStream("Patchnotes.txt");
				byte[] bs = new byte[100000];
				int size = 0;
				while(true) {
					int r = fis.read();
					if (r == -1)
						break;
					bs[size] = (byte) r;
					size++;
				}
				byte[] res = new byte[size];
				for (int i = 0; i < res.length; i++)
					res[i] = bs[i];
				
				
				lines = new String(res,StandardCharsets.UTF_8).split("\\R");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return lines;
			
		}
		
	}
	
	
	
	
}
