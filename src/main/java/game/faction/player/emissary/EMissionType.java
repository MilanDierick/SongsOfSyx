package game.faction.player.emissary;

import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import game.faction.npc.ruler.ROpinions;
import game.faction.npc.ruler.Royalty;
import game.time.TIME;
import init.D;
import init.sprite.UI.UI;
import snake2d.util.color.ColorImp;
import snake2d.util.rnd.RND;
import snake2d.util.sets.*;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.StrInserter;
import world.WORLD;
import world.regions.Region;
import world.regions.data.RD;

public abstract class EMissionType implements INDEXED{

	private static ArrayListGrower<EMissionType> all = new ArrayListGrower<>();
	
	private final int index;
	public final SPRITE icon;
	
	static {
		D.gInit(EMissionType.class);
	}
	
	public static final EMissionType SUPPORT_R = new EMissionType(
			
			
			UI.icons().s.fist.createColored(new ColorImp(40, 40, 120)),
			D.g("Support", "Gather Support"), 
			D.g("SupportD", "Gathers support in a region, so that it will be more inclined to be ruled by you in the future.")) {

				
				final double ti = 1.0/(TIME.secondsPerDay*16*4);

				@Override
				protected boolean update(EMission e, double ds) {
					Region reg = WORLD.REGIONS().getByIndex((int)e.data1);
					if (reg != null && reg.faction() != FACTIONS.player()) {
						
						RD.OWNER().affiliation.incFraction(reg, ds*ti*RD.OWNER().affiliation.max(reg));
						return true;
					}
					return false;
				}

				@Override
				protected void set(EMission e, Region reg, Royalty roy) {
					e.data1 = reg.index();
				}

				@Override
				public boolean targetIs(EMission e, Region reg, Royalty roy) {
					return reg != null && e.data1 == reg.index();
				}

				final CharSequence dd = D.g("SupportV", "Gathering support in the region of {REGION}. Current support is: ");
				final StrInserter<EMission> i1 = new StrInserter<EMission>("REGION") {

					@Override
					protected void set(EMission e, Str str) {
						str.add(WORLD.REGIONS().all().get(e.data1).info.name());
					}
				
				};
				
				@Override
				public void edesc(Str str, EMission e) {
					str.add(dd);
					i1.insert(e, str);
					str.add((int)(RD.OWNER().affiliation.getD(WORLD.REGIONS().all().get(e.data1))*100));
					str.add('%');
				}
				
				@Override
				public Region reg(EMission e) {
					return WORLD.REGIONS().all().get(e.data1);
				}
				
				
				
	};
	
	public static final EMissionType FLATTER = new EMissionType(
			UI.icons().s.heart.createColored(new ColorImp(120, 40, 120)),
			D.g("Flatter"), 
			D.g("FlatterD", "Flattering a royalty will increase their opinion of you and your faction.")) {

				final double ti = 1.0/(TIME.secondsPerDay*16*2);
		
				@Override
				protected boolean update(EMission e, double ds) {
					
					Royalty r = getRoy(e.data1, e.data2);
					if (r != null) {
						ROpinions.flatter(r, ds*ti);
						return true;
					}
					return false;
				}

				@Override
				protected void set(EMission e, Region reg, Royalty roy) {
					e.data1 = roy.court.faction.index();
					e.data2 = roy.ID;
				}

				@Override
				public boolean targetIs(EMission e, Region reg, Royalty roy) {
					return roy == getRoy(e.data1, e.data2); 
				}
				
				final CharSequence dd = D.g("FlatterV", "Flattering {ROYALTY}. Current flattery value is: ");
				final StrInserter<EMission> i1 = new StrInserter<EMission>("ROYALTY") {

					@Override
					protected void set(EMission e, Str str) {
						Royalty r = getRoy(e.data1, e.data2);
						str.add(r.name());
					}
				
				};
				
				@Override
				public void edesc(Str str, EMission e) {
					Royalty r = getRoy(e.data1, e.data2);
					if (r == null)
						return;
					str.add(dd);
					i1.insert(e, str);
					str.add((int)(ROpinions.flattery(r)*100));
					str.add('%');
				}
				
				@Override
				public Faction faction(EMission e) {
					Royalty r = getRoy(e.data1, e.data2);
					if (r == null)
						return null;
					return r.court.faction;
				}
		
	};
	
	public static final EMissionType FAVOUR = new EMissionType(
			UI.icons().s.star.createColored(new ColorImp(20, 120, 20)),
			D.g("Favour"), 
			D.g("FavourD", "Favouring an heir increases their chances of becoming the next ruler of their faction.")) {

				@Override
				protected boolean update(EMission e, double ds) {

					Royalty r = getRoy(e.data1, e.data2);
					if (r != null && r.successionI() > 1) {
						e.dataD -= ds;
						if (e.dataD < 0) {
							r.court.promote(r, true);
							if (r.successionI() <= 1)
								return false;
						}
						return true;
					}

					return false;
				}

				@Override
				protected void set(EMission e, Region reg, Royalty roy) {
					e.data1 = roy.court.faction.index();
					e.data2 = roy.ID;
					e.dataD = TIME.secondsPerDay*4 * RND.rFloat()*TIME.secondsPerDay*16*8;
				}
				
				@Override
				public boolean targetIs(EMission e, Region reg, Royalty roy) {
					return roy == getRoy(e.data1, e.data2); 
				}
				
				final CharSequence dd = D.g("FavourV", "Favouring {ROYALTY}. A promotion might come any day now.");
				final StrInserter<EMission> i1 = new StrInserter<EMission>("ROYALTY") {

					@Override
					protected void set(EMission e, Str str) {
						Royalty r = getRoy(e.data1, e.data2);
						str.add(r.name());
					}
				
				};
				
				@Override
				public void edesc(Str str, EMission e) {
					Royalty r = getRoy(e.data1, e.data2);
					if (r == null)
						return;
					str.add(dd);
					i1.insert(e, str);
				}
				
				@Override
				public Faction faction(EMission e) {
					Royalty r = getRoy(e.data1, e.data2);
					if (r == null)
						return null;
					return r.court.faction;
				}
		
	};
	
	public static final EMissionType ASSASINATE = new EMissionType(
			UI.icons().s.death.createColored(new ColorImp(120, 20, 20)),
			D.g("Assassinate"), 
			D.g("AssassinateD", "Assassinate a royalty. Has a small chance of succeeding and failed attempts will decrease the royalty's opinion of you severely.")) {

				@Override
				protected boolean update(EMission e, double ds) {
					Royalty r = getRoy(e.data1, e.data2);
					if (r != null) {
						e.dataD -= ds;
						if (e.dataD < 0) {
							ROpinions.assasinate(r);
							if (getRoy(e.data1, e.data2) == null)
								return false;
						}
						return true;
					}
					return false;
				}

				@Override
				protected void set(EMission e, Region reg, Royalty roy) {
					e.data1 = roy.court.faction.index();
					e.data2 = roy.ID;
					e.dataD = TIME.secondsPerDay*2 * RND.rFloat()*TIME.secondsPerDay*16*8;
				}
				
				@Override
				public boolean targetIs(EMission e, Region reg, Royalty roy) {
					return roy == getRoy(e.data1, e.data2); 
				}
				
				final CharSequence dd = D.g("AssassinateV", "Arranging for an accident to befall on {ROYALTY}.");
				final StrInserter<EMission> i1 = new StrInserter<EMission>("ROYALTY") {

					@Override
					protected void set(EMission e, Str str) {
						Royalty r = getRoy(e.data1, e.data2);
						str.add(r.name());
					}
				
				};
				
				@Override
				public void edesc(Str str, EMission e) {
					Royalty r = getRoy(e.data1, e.data2);
					if (r == null)
						return;
					str.add(dd);
					i1.insert(e, str);
				}
				
				@Override
				public Faction faction(EMission e) {
					Royalty r = getRoy(e.data1, e.data2);
					if (r == null)
						return null;
					return r.court.faction;
				}
		
	};
	
	public static LIST<EMissionType> ALL(){
		return all;
	}
	
	private static Royalty getRoy(int fi, int ri) {
		Faction f = FACTIONS.getByIndex(fi);
		if (f instanceof FactionNPC && f.isActive()) {
			FactionNPC ff = (FactionNPC) f;
			Royalty r = ff.court().getByID(ri);
			return r;
		}
		return null;
	}
	
	public final CharSequence name;
	public final CharSequence desc;
	
	private EMissionType(SPRITE icon, CharSequence name, CharSequence desc) {
		this.name = name;
		this.desc = desc;
		this.icon = icon;
		index = all.add(this);
	}

	@Override
	public int index() {
		return index;
	}
	
	protected abstract void set(EMission e, Region reg, Royalty roy);
	
	public abstract void edesc(Str str, EMission e);
	
	protected abstract boolean update(EMission e, double ds);
	
	public abstract boolean targetIs(EMission e, Region reg, Royalty roy);
	
	public Faction faction(EMission e) {
		return null;
	}
	
	public Region reg(EMission e) {
		return null;
	}
	
}
