package settlement.tilemap;

import snake2d.SPRITE_RENDERER;
import snake2d.util.color.*;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.HeightMap;
import util.data.DOUBLE.DOUBLE_MUTABLE;
import util.gui.slider.GGaugeMutable;
import view.interrupter.ISidePanel;
import view.main.VIEW;
import view.sett.IDebugPanelSett;

final class GeneratorTests {

	private static Debug debug;
	
	public GeneratorTests() {
		IDebugPanelSett.add("terrain", new ACTION() {
			
			@Override
			public void exe() {
				if (debug == null)
					debug = new Debug();
				VIEW.s().panels.add(debug, true);
			}
		});
	}
	
	private static class Debug extends ISidePanel {
		
		final int size = 128;
		final int scale = 4;
		HeightMap ferMap = new HeightMap(size, size, 32, 2);
		final HeightMap height = new HeightMap(size, size, 128, 4);
		double base2 = 1;
		double base = 0;
		
		private byte[][] res = new byte[size][size];
		private final OpacityImp o = new OpacityImp(0);
		Debug(){
			
			section.add(new RENDEROBJ.RenderImp(size*scale) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					
					for (int y = 0; y < size; y++) {
						for (int x = 0; x < size; x++) {
							int px = body.x1() + x*scale;
							int py = body.y1() + y*scale;
							o.set(res[y][x]&0x0FF);
							o.bind();
							COLOR.WHITE100.render(r, px, px+scale, py, py+scale);
							//CORE.renderer().renderParticle(px, py);
						}
					}
					OPACITY.unbind();
					
				}
			});
			
			GGaugeMutable g;
			
			g = new GGaugeMutable(new DOUBLE_MUTABLE() {
				
				@Override
				public double getD() {
					return base;
				}
				
				@Override
				public DOUBLE_MUTABLE setD(double d) {
					base = d;
					fixbase();
					return this;
				}
			}, 200);
			section.addDown(8, g);
			
			g = new GGaugeMutable(new DOUBLE_MUTABLE() {
				
				@Override
				public double getD() {
					return base2;
				}
				
				@Override
				public DOUBLE_MUTABLE setD(double d) {
					base2 = d;
					fixbase2(base2);
					return this;
				}
			}, 200);
			section.addDown(8, g);
			
			titleSet("");
			
			
		}
		
		private void fixbase() {
			for (int y = 0; y < size; y++) {
				for (int x = 0; x < size; x++) {
					double f = ferMap.get(x, y);
					double h = height.get(x, y);
					base = CLAMP.d(base, 0, 1);
					f = GeneratorFertilityInit.get(base, f, h);
					res[y][x] = (byte) (0x0FF*CLAMP.d(f, 0, 1));
				}
			}
		}
		
		private void fixbase2(double base) {
			base *= 0.8;
			
			for (int y = 0; y < size; y++) {
				for (int x = 0; x < size; x++) {
					double f = height.get(x, y);
					f = Math.pow(f, 1 + 8*(1-base));
					//f = Math.pow(f, 1 + (1-base));
					
					f = CLAMP.d(base + f, 0, 1);
					f -= 0.2*ferMap.get(x, y);
					res[y][x] = (byte) (0x0FF*CLAMP.d(f, 0, 1));
				}
			}
			
		}
	}
	
}
