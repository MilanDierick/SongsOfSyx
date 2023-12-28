package view.ui.battle;

import init.sprite.UI.UI;
import settlement.room.military.training.ROOM_M_TRAINER;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBattle.StatTraining;
import settlement.stats.equip.EquipBattle;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.data.INT.INTE;
import util.data.INT.IntImp;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.gui.slider.GAllocator;
import util.gui.table.GRows;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import world.army.util.DIV_SETTING;

public abstract class UIDivSpec implements DIV_SETTING{
	
	private final IntImp[] training = new IntImp[STATS.BATTLE().TRAINING_ALL.size()];
	private final Gear[] gear = new Gear[STATS.EQUIP().BATTLE_ALL().size()];
	
	private final static int width = 150;
	private final static int height = 42;

	public final GuiSection section = new GuiSection();
	
	public UIDivSpec(int xs, int ys) {
		this(xs, ys, 1.0);
	}
	
	public UIDivSpec(int xs, int ys, double maxTraining) {
		for (int i = 0; i < gear.length; i++) {
			gear[i] = new Gear(i, gear);
		}
		
		for (int i = 0; i < training.length; i++) {
			training[i] = new IntImp(0, 10) {
				@Override
				public int max() {
					int am = (int) (maxTraining*10);
					for(IntImp ii : training)
						if (ii != this)
							am -= ii.get();
					return CLAMP.i(am, 0, 10);
				};
			};
		}
		
		GRows rows = new GRows(xs);
		
		
		

		for (ROOM_M_TRAINER<?> t : ROOM_M_TRAINER.ALL()) {
			
			GStat s = new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.perc(text, training(t.training()));
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					
					b.title(t.tInfo.name);
					b.add(GFORMAT.perc(b.text(), training(t.training())));
					b.NL();
					b.text(t.tInfo.desc);
					b.sep();
					t.boosters.hover(b, 1.0, -1);
				};
				
			};
			
			rows.add(new Spec(t.icon, COLOR.RED100.makeSaturated(0.7), training[t.INDEX_TRAINING], s));
		
			
		}
		
		
	
		
		for (EquipBattle m : STATS.EQUIP().BATTLE_ALL()) {
			GStat s = new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.i(text, men()*gear[m.indexMilitary()].get());
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					m.hover(b);
				};
				
			};
				
			rows.add(new Spec(m.resource().icon(), COLOR.ORANGE100.makeSaturated(0.7), gear[m.indexMilitary()], s) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {

					
					super.render(r, ds);
					if (disabled()) {
						OPACITY.O50.bind();
						COLOR.BLACK.render(r, body(), -1);
						OPACITY.unbind();
					}
				}
				
				private boolean disabled() {
					if (gear[m.indexMilitary()].get() == 0) {
						for (EquipBattle e : STATS.EQUIP().BATTLE_ALL()) {
							if (e != m && gear[e.indexMilitary()].get() > 0 && !e.canCombineWith(m))
								return true;
							
						}
					}
					return false;
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					if (disabled()) {
						GBox b = (GBox) text;
						b.error(EquipBattle.¤¤combineProblem);
						b.NL(8);
					}else {
						super.hoverInfoGet(text);
					}
					
				}
				
			});
			
			
		
		}
		
		if (rows.rows().size() > ys) {
			section.addRelBody(8, DIR.S, new GScrollRows(rows.rows(), height*ys).view());
		}else {
			for (RENDEROBJ o : rows.rows()) {
				section.addDown(0, o);
			}
		}
		
		section.addRelBody(2, DIR.S, new GButt.ButtPanel(UI.FONT().S.getText(DicMisc.¤¤Clear)) {
			
			@Override
			protected void clickA() {
				for (IntImp ii : gear)
					ii.set(0);
				for (IntImp ii : training)
					ii.set(0);
				super.clickA();
			}
			
		});
		
		
	}
	
	private static class Gear extends IntImp {
		
		private final int gi;
		private final Gear[] other;
		
		Gear(int gi, Gear[] other){
			super(0, STATS.EQUIP().BATTLE_ALL().get(gi).max());
			this.gi = gi;
			this.other = other;
		}
		
		@Override
		public void set(int t) {
			if (t > 0) {
				EquipBattle s = STATS.EQUIP().BATTLE_ALL().get(gi);
				for (int oi = 0; oi < STATS.EQUIP().BATTLE_ALL().size(); oi++) {
					EquipBattle o = STATS.EQUIP().BATTLE_ALL().get(oi);
					if (s != o && !o.canCombineWith(s)) {
						other[o.indexMilitary()].set(0);
					}
				}
			}
			super.set(t);
		}
	}
	
	public abstract int men();

	
	public static class Spec extends GuiSection{
		

		private final GStat stat;
		
		public Spec(SPRITE icon, COLOR col, INTE ii, GStat stat){
			GAllocator g = new GAllocator(col, ii, 6, 12);
			
			body().setDim(width, height);
			
			addC(icon, 20, body().cY());
			
			add(stat, 48, body().cY()-stat.height());
			add(g, 40, body().cY() + 2);
			if (body().width() > width)
				body().incrW(4);
			
			this.stat = stat;
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			super.hoverInfoGet(text);
			if (text.emptyIs())
				stat.hoverInfoGet((GBox) text);
		}
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			GCOLOR.UI().bg().render(r, body());
			GButt.ButtPanel.renderFrame(r, body());
			super.render(r, ds);
		}
		
		
	}

	@Override
	public double training(StatTraining tr) {
		return (double)training[tr.tIndex].get()/10.0;
	}

//	public void trainingSet(StatTraining tr, double training) {
//		training[tr.tIndex] = training;
//	}
	
	public IntImp equipi(EquipBattle e) {
		return gear[e.indexMilitary()];
	}
	
	public IntImp traini(StatTraining tr) {
		return training[tr.tIndex];
	}
	
	@Override
	public double equip(EquipBattle e) {
		return gear[e.indexMilitary()].getD();
	}

}
