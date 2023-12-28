package view.sett.ui.health;

import game.boosting.BOOSTABLES;
import game.boosting.Boostable;
import init.D;
import init.disease.DISEASE;
import init.disease.DISEASES;
import init.race.RACES;
import init.sprite.UI.UI;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.health.HEALTH;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LinkedList;
import util.colors.GCOLOR;
import util.dic.DicTime;
import util.gui.misc.*;
import util.gui.table.GScrollRows;
import util.gui.table.GStaples;
import util.info.GFORMAT;
import util.statistics.HISTORY_INT;
import view.interrupter.ISidePanel;

public class UIHealth extends ISidePanel{
	
	private static CharSequence ¤¤Diseases = "Known Diseases";
	
	static {
		D.ts(UIHealth.class);
	}
	
	public UIHealth() {
		titleSet(HEALTH.¤¤name);
		
		{
			GuiSection s = new GuiSection();
			s.addDown(2, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.i(text, STATS.NEEDS().disease.infected().data().get(null));
				}
			}.hh(STATS.NEEDS().disease.infected().info().name, STATS.NEEDS().disease.infected().info().desc, 200));
			
			s.addDown(2, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.iofk(text, SETT.ROOMS().HOSPITAL.service().available(), SETT.ROOMS().HOSPITAL.service().total());
				}
				
			}.hh(SETT.ROOMS().HOSPITAL.info.names, 200));
			
			section.add(s);
			
		}
		
		{
			GuiSection s = new GuiSection();
			
			GStaples chart = new GStaples(STATS.DAYS_SAVED) {

				@Override
				protected double getValue(int stapleI) {
					int i = STATS.DAYS_SAVED-stapleI-1;
					return HEALTH.rate().getD(i);
				}

				@Override
				protected void hover(GBox box, int stapleI) {
					int i = STATS.DAYS_SAVED-stapleI-1;
					box.title(DicTime.setDaysAgo(box.text(), i));
					
					box.textLL(HEALTH.rate().info().name);
					box.tab(6);
					box.add(GFORMAT.perc(box.text(), HEALTH.rate().get(i)/100.0));
					box.NL(8);
					
					box.NL(4);
					box.textL(HEALTH.hygine().info().name);
					box.tab(6);
					box.add(GFORMAT.f1(box.text(), HEALTH.hygine().get(i)/100.0));
					
					box.NL(4);
					box.textL(HEALTH.health().info().name);
					box.tab(6);
					box.add(GFORMAT.f1(box.text(), HEALTH.health().get(i)/100.0));
					
					box.NL(4);
					box.textL(HEALTH.pop().info().name);
					box.tab(6);
					box.add(GFORMAT.f1(box.text(), HEALTH.pop().get(i)/100.0));
					
				}
				
				@Override
				protected void setColor(ColorImp c, int stapleI, double value) {
					if (value < 0.5) {
						c.interpolate(GCOLOR.UI().SOSO.normal, GCOLOR.UI().BAD.normal, 1.0-value*2);
					}else {
						c.interpolate(GCOLOR.UI().SOSO.normal, GCOLOR.UI().GOOD.normal, (value-0.5)*2);
					}
				}
				
			};
			
			chart.body().setDim(400, 90);
			
			s.add(new GHeader(HEALTH.¤¤name).hoverInfoSet(HEALTH.¤¤desc));
			s.addRelBody(4, DIR.S, chart);
			
			section.addDown(16, s);
		}
		
		{
			GuiSection s = new GuiSection();
			s.addDownC(8, stat(HEALTH.hygine(), BOOSTABLES.CIVICS().HYGINE));
			s.addDownC(2, stat(HEALTH.health(), BOOSTABLES.PHYSICS().HEALTH));
			s.addDownC(2, stat(HEALTH.pop(), null));
			
			section.addDown(8, s);
		}
		
		{
			GuiSection s = new GuiSection();
			s.add(new GHeader(¤¤Diseases));
			
			LinkedList<RENDEROBJ> rows = new LinkedList<>();
			
			for (DISEASE d : DISEASES.all()) {
				rows.add(new HOVERABLE.Sprite(new GText(UI.FONT().M, d.info.name).normalify2()) {
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						d.hover(text);
					}
					
				});
			}
			
			s.addDown(2, new GScrollRows(rows, HEIGHT-section.body().height()-80, section.body().width()).view());
			
			section.addDown(16, s);
			
		}
		
		
		
		
	}
	
	private static RENDEROBJ stat(HISTORY_INT in, Boostable bo) {
		return new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.f1(text, in.get()/100.0);
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				b.title(in.info().name);
				b.text(in.info().desc);
				b.NL(8);
				if (bo != null)
					bo.hover(b, RACES.clP(null, null), true);
					
			};
			
		}.hh(in.info().name, 200);
	}
	
}
