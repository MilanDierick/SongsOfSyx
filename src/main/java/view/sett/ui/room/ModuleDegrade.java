package view.sett.ui.room;

import game.faction.FACTIONS;
import init.C;
import init.D;
import init.boostable.BOOSTABLES;
import init.sprite.SPRITES;
import settlement.main.SETT;
import settlement.room.main.*;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.LISTE;
import util.data.GETTER;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.gui.table.GTableSorter.GTFilter;
import util.gui.table.GTableSorter.GTSort;
import util.info.GFORMAT;
import view.sett.ui.room.Modules.ModuleMaker;

final class ModuleDegrade implements ModuleMaker {

	private final CharSequence ¤¤DEGRADE = "¤Degradation";
	private final CharSequence ¤¤DEGRADE_AVE = "¤Average degradation amongst these room. Degradation affects a room negatively.";
	private final CharSequence ¤¤DEGRADE_DESC = "¤Degradation affects rooms negatively. Janitors and idle people will maintain rooms. Some maintenance requires materials.";

	private final CharSequence ¤¤Rate = "¤Rate";
	private final CharSequence ¤¤Lock = "¤The technology for the room is locked, and it can't be maintained.";
	private final CharSequence ¤¤RateDesc = "¤All rooms degrade. The degradation can increase further if large rooms are constructed without adequate ceiling support in the center. Lack of isolation also increases degradation.";
	private final CharSequence ¤¤badIsolation = "¤Room poorly isolated";
	private final CharSequence ¤¤Support = "¤Support";
	private final CharSequence ¤¤SupportD = "¤When a room is built it may require extra support if the shape is very large. As a result, more maintenance is needed.";
	
	public ModuleDegrade(Init init) {
		D.t(this);
//		sDEGRADE = init.d("DEGRADE");
//		sDEGRADE_AVE = init.d("DEGRADE_AVE");
//		sDEGRADE_DESC = init.d("DEGRADE_DESC");
	}
	

	@Override
	public void make(RoomBlueprint p, LISTE<UIRoomModule> l) {
		l.add(new Hover());
		if (p instanceof RoomBlueprintIns<?>) {
			RoomBlueprintIns<?> pp = (RoomBlueprintIns<?>) p;
			if (pp.degrades())
				l.add(new I(pp));
		}
		
	}
	
	private final class Hover extends UIRoomModule {
		
		@Override
		public void hover(GBox box, Room room, int rx, int ry) {
			if (room.degrader(rx, ry) != null) {
				box.text(¤¤DEGRADE);
				box.add(GFORMAT.percInv(box.text(), room.getDegrade(rx, ry)));
				box.NL(2);
				if (FACTIONS.player().locks.unlockText(room.blueprint()) != null)
					box.error(¤¤Lock);
				box.NL(2);
			}
			if(room.constructor() != null && room.constructor().needsIsolation()) {
				box.text(SETT.ROOMS().isolation.info.name);
				box.add(GFORMAT.perc(box.text(), room.isolation(rx, ry)));
				box.NL(2);
			}
		}

		@Override
		public void problem(GBox box, Room room, int rx, int ry) {
			if(room.getDegrade(rx, ry) > 0.5) {
				box.add(box.text().errorify().add(¤¤DEGRADE)).NL(C.SG*2);
				box.NL();
			}
			if(room.constructor() != null && room.constructor().mustBeIndoors() && room.isolation(rx, ry) < 0.0) {
				box.add(box.text().errorify().add(¤¤badIsolation)).NL(C.SG*2);
				box.NL();
			}
		}
	}
	
	private final class I extends UIRoomModule {
		
		private final RoomBlueprintIns<?> blue;
		private final boolean iso;
		
		I(RoomBlueprintIns<?> blue){
			this.blue = blue;
			iso = blue.constructor() != null && blue.constructor().needsIsolation();
		}
		
		@Override
		public void appendManageScr(GGrid grid, GGrid text, GuiSection extra) {
			grid.add(new GStat() {

				@Override
				public void update(GText text) {
					GFORMAT.percInv(text, (blue.degradeAverage()));
					
				}
			}.hh(SPRITES.icons().s.degrade).hoverInfoSet(¤¤DEGRADE_AVE));
		}

		@Override
		public void appendTableFilters(LISTE<GTFilter<RoomInstance>> filters, LISTE<GTSort<RoomInstance>> sorts,
				LISTE<UIRoomBulkApplier> appliers) {
			sorts.add(new GTSort<RoomInstance>(¤¤DEGRADE) {
				
				@Override
				public int cmp(RoomInstance current, RoomInstance cmp) {
					return Double.compare(current.getDegrade(), cmp.getDegrade());
				}

				@Override
				public void format(RoomInstance h, GText text) {
					GFORMAT.perc(text, h.getDegrade());
				}
				
			});
			if (iso) {
				sorts.add(new GTSort<RoomInstance>(SETT.ROOMS().isolation.info.name) {
					
					@Override
					public int cmp(RoomInstance current, RoomInstance cmp) {
						return Double.compare(current.isolation(current.mX(), current.mY()), cmp.isolation(cmp.mX(), cmp.mY()));
					}

					@Override
					public void format(RoomInstance h, GText text) {
						GFORMAT.perc(text, h.isolation(h.mX(), h.mY()));
					}
					
				});
			}
			
		}
		
		@Override
		public void appendPanelIcon(LISTE<RENDEROBJ> section, GETTER<RoomInstance> get) {
			
			GStat s = new GStat() {
				@Override
				public void update(GText text) {
					GFORMAT.percInv(text, get.get().getDegrade());
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					
					b.title(¤¤DEGRADE);
					b.text(¤¤DEGRADE_DESC);
					
					b.NL(8);
					
					
					b.textLL(¤¤Rate);
					b.NL();
					b.text(¤¤RateDesc);
					b.NL(8);
					b.textL(DicMisc.¤¤Base);
					b.tab(6);
					b.add(GFORMAT.f(b.text(), get.get().degrader(get.get().mX(), get.get().mY()).baseRate()));
					b.NL();
					{
						double ex = get.get().degrader(get.get().mX(), get.get().mY()).expenseRate();
						b.textL(¤¤Support);
						b.tab(6);
						GText t = b.text();
						t.add('*');
						b.add(GFORMAT.f(t, ex));
						if (ex > 1) {
							t.errorify();
						}else {
							t.normalify2();
						}
						b.NL();
						b.text(¤¤SupportD);
						b.NL(4);
					}
					
					
					
					if (iso) {
						double is = (1.0 + (1.0 - get.get().isolation(get.get().mX(), get.get().mY())) * 2);
						b.textL(SETT.ROOMS().isolation.info.name);
						b.tab(6);
						GText t = b.text();
						t.add('*');
						b.add(GFORMAT.f(t, is));
						if (is > 1) {
							t.errorify();
						}else {
							t.normalify2();
						}
						b.NL();
						b.text(SETT.ROOMS().isolation.info.desc);
						b.NL(4);
					}
					b.textL(BOOSTABLES.INFO().name);
					b.tab(6);
					GText t = b.text();
					t.add('/');
					b.add(GFORMAT.f(t, CLAMP.d(1.0/BOOSTABLES.CIVICS().MAINTENANCE.get(null, null), 0, 1000)));
					t.normalify2();
					b.NL();
				}
			};
			

			
			section.add(new GHeader.HeaderHorizontal(SPRITES.icons().s.degrade, s)); 
		}
		
	}

	





	
}
