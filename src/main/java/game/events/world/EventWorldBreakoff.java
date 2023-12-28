package game.events.world;

import java.io.IOException;

import game.events.EVENTS.EventResource;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import game.time.TIME;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sprite.text.Str;
import view.ui.message.MessageText;
import world.WORLD;
import world.entity.army.WArmy;
import world.map.pathing.WRegSel;
import world.map.pathing.WRegs.RDist;
import world.map.pathing.WTREATY;
import world.regions.Region;
import world.regions.data.RD;

public class EventWorldBreakoff extends EventResource{

	private static final double dTime = TIME.secondsPerDay*32;
	private double timer = dTime;
	private final ArrayList<Region> tmp = new ArrayList<Region>(32);
	
	private static CharSequence ¤¤title = "Realm Collapses";
	private static CharSequence ¤¤desc = "Due to internal strife, the realm of {0} has divided.";
	
	private int nextFaction;
	private double nextAm;
	
	EventWorldBreakoff(){
		clear();
	}
	
	@Override
	protected void update(double ds) {
		
		timer -= ds;
		if (timer < 0) {
			
			Faction f = FACTIONS.getByIndex(nextFaction);

			if (f.isActive() && f instanceof FactionNPC && FACTIONS.DIP().war.getEnemies(f).size() == 0) {
				shatter((FactionNPC) f);
			}
			clear();
		}
		
		
	}

	void shatter(FactionNPC f){
		if (f.realm().regions() <= 1)
			return;
		
		int am = (int) (f.realm().regions()*nextAm);
		am = CLAMP.i(am, 0, 32);
		Region ff = null;
		for (RDist d : WORLD.PATH().tmpRegs.all(f.capitolRegion(), WTREATY.SAME(f), WRegSel.DUMMY())){
			if (d.reg != f.capitolRegion())
				ff = d.reg;
		}
		if (ff != null)
			shatter(f, ff, am);
		
	}
	
	void shatter(FactionNPC f, Region start, int am){
		
		RD.setFaction(start, null);
		
		tmp.clear();
		
		for (RDist d : WORLD.PATH().tmpRegs.all(f.capitolRegion(), WTREATY.NEIGHBOURS(start), WRegSel.DUMMY())){
			
			if (d.reg.faction() == f && d.reg != f.capitolRegion()) {
				tmp.add(d.reg);
				am--;
				if (am <= 0)
					break;
			}
			
		}
		
		for (Region reg : tmp) {
			RD.setFaction(reg, null);
			for (WArmy a : WORLD.ENTITIES().armies.fill(reg)) {
				if (a.faction() == f)
					a.disband();
			}
		}
		
		if (RD.DIST().factionBordersPlayer(f))
			new MessageText(¤¤title).paragraph(Str.TMP.clear().add(¤¤desc).insert(0, f.name)).send();
		
	}
	
	@Override
	protected void save(FilePutter file) {
		file.d(timer);
		file.i(nextFaction);
		file.d(nextAm);
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		timer = file.d();
		nextFaction = file.i();
		nextAm = file.d();
	}

	@Override
	protected void clear() {
		timer = RND.rFloat()*dTime;
		nextFaction = RND.rInt(FACTIONS.MAX);
		nextAm = RND.rFloat();
	}	

}
