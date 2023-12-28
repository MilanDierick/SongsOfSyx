package util.gui.common;

import java.io.File;

import game.time.TIME;
import init.D;
import init.RES;
import init.paths.PATH;
import init.paths.PATHS;
import init.sprite.UI.UI;
import snake2d.*;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileManager;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.sprite.text.Str;
import util.data.INT.IntImp;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.gui.slider.GSliderInt;
import view.interrupter.Interrupter;
import view.main.VIEW;

public final class SuperSc extends GuiSection{


	public static CharSequence ¤¤name = "Super screenshot";
	private static CharSequence ¤¤desc = "A super screenshot generates a large image of the current view. It takes some time. Screenshots are saved in your local files. You can find them through the game launcher.";
	private static CharSequence ¤¤time = "Generate a screenshot every {0} in-game day. Older screenshots will be overwritten by new based on how many screenshots you keep";
	private static CharSequence ¤¤keep = "Save and keep {0} screenshots files. If file amount exceeded, they fill be overwritten.";
	private static CharSequence ¤¤sc = "¤Super Screenshot";
	
	private final String fn;
	private final SUPER_SCREENSHOT shot;
	
	static {
		D.ts(SuperSc.class);
	}
	
	private final double[] day = new double[] {
		-1,
		8,
		4,
		2,
		1,
		0.5,
	};
	
	private final IntImp iday;
	private final IntImp saved;
	
	public SuperSc(String fn, SUPER_SCREENSHOT shot) {
		
		this.fn = fn;
		this.shot = shot;
		add(new GHeader(¤¤name));
		addRelBody(4, DIR.S, new GText(UI.FONT().M, ¤¤desc).setMaxWidth(400).r(DIR.N));
		
		addRelBody(4, DIR.S, new GButt.ButtPanel(DicMisc.¤¤Generate + " 1") {
			
			@Override
			protected void clickA() {
				take();
			}
			
		});
		addRelBody(16, DIR.S, new GHeader(DicMisc.¤¤Timer));
		
		iday = new IntImp(0, day.length-1);
		
		GuiSection sl = new GuiSection() {
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				
				if (iday.get() > 0) {
					Str.TMP.clear().add(¤¤time).insert(0, day[iday.get()], 1);
					b.text(Str.TMP);
				}else {
					b.text(DicMisc.¤¤Deactivated);
				}
			}
			
		};
		sl.add(UI.icons().s.clock, 0, 0);
		sl.addRightC(8, new GSliderInt(iday, 200, true));
		addRelBody(4, DIR.S, sl);
		
		saved = new IntImp() {
			
			@Override
			public int max() {
				return 400;
			};
		};
		saved.set(100);
		
		sl = new GuiSection() {
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				Str.TMP.clear().add(¤¤keep).insert(0, saved.get());
				b.text(Str.TMP);
			}
			
		};
		sl.add(UI.icons().s.urn, 0, 0);
		sl.addRightC(8, new GSliderInt(saved, 200, false));
		addDown(2, sl);
		
		Interrupter in = new Interrupter(true, true) {
			
			double old = 0;
			
			@Override
			protected boolean update(float ds) {
				double dday = day[iday.get()];
				if (dday < 0)
					return true;
				
				double d = TIME.secondsPerDay *dday;
				
				double day = (TIME.currentSecond()/d)%1.0;
				if (old < 0.5 && day >= 0.5) {
					take();
				}
				old = day;
				
				return true;
			}
			
			@Override
			protected boolean render(Renderer r, float ds) {
				return true;
			}
			
			@Override
			protected void mouseClick(MButt button) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			protected void hoverTimer(GBox text) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
				return false;
			}
		};
		
		VIEW.inters().manager.add(in);
		
	}
	
	
	private void take() {
		RES.loader().init();
		RES.loader().print(¤¤sc);
		
		String smallest = null;
		int am = 0;
		long lastM = Long.MAX_VALUE;
		PATH p = PATHS.local().SCREENSHOT_S;
		for (String s : p.getFiles()) {
			if (s.startsWith(fn)) {
				am++;
				long m = p.get(s).toFile().lastModified();
				if (m < lastM) {
					lastM = m;
					smallest = s;
				}
			}
		}
		if (am >= saved.get()) {
			p.delete(smallest);
		}
		
		String f = ""+p.get().toAbsolutePath() + File.separator + fn;
		f = FileManager.NAME.timeStampString(f) + ".jpg";
		
		shot.perform(f);
	}
	
	
}
