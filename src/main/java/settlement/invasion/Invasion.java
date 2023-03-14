package settlement.invasion;

import static settlement.main.SETT.*;

import java.io.IOException;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.time.TIME;
import init.config.Config;
import settlement.army.Div;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.HTYPE;
import settlement.entity.humanoid.Humanoid;
import settlement.invasion.SpotMaker.InvasionSpot;
import settlement.main.SETT;
import settlement.room.main.throne.THRONE;
import settlement.stats.STATS;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.IntChecker;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListShort;
import snake2d.util.sprite.text.Str;
import util.dic.DicArmy;
import util.dic.DicRes;
import view.main.MessageText;
import view.main.VIEW;

final class Invasion {

	private final ArrayList<InvadorDiv> divs;
	private int artillery = 0;
	private STATE state = null;
	private double timer = 0;
	private final InvasionSpot spot;
	private ArrayListShort activeDivs = new ArrayListShort(Config.BATTLE.DIVISIONS_PER_ARMY);
	private int invadingFacion;
	public boolean victory;
	private static final IntChecker check = new IntChecker(Config.BATTLE.DIVISIONS_PER_BATTLE);
	
	private enum STATE {
		WARNING,
		BOMBARD_TEST,
		BOMBARD,
		PLACEART,
		DEPLOYING,
		FIGHTING,
		WAIT_FOR_REPLY,
		DONE,
	}

	Invasion(FileGetter f) throws IOException{
		divs = new ArrayList<>(f.i());
		while(divs.hasRoom())
			divs.add(new InvadorDiv(f));
		artillery = f.i();
		state = STATE.values()[f.i()];
		timer = f.d();
		spot = new InvasionSpot(f);
		activeDivs.load(f);
		invadingFacion = f.i();
		victory = f.bool();
	}
	
	public void save(FilePutter file) {
		file.i(divs.size());
		for (InvadorDiv d : divs)
			d.save(file);
		file.i(artillery);
		file.i(state.ordinal());
		file.d(timer);
		spot.save(file);
		activeDivs.save(file);
		file.i(invadingFacion);
		file.bool(victory);
	}
	
	Invasion(InvadorDiv[] divs, DIR dir, Faction f){
		
		if (f == null)
			invadingFacion = -1;
		else
			invadingFacion = f.index();
		
		int men = 0;
		
		for (InvadorDiv d : divs)
			men += d.men;
		
		artillery = (int) Math.ceil(men/200.0);
		
		this.divs = new ArrayList<>(divs);
		
		if (dir == DIR.C || dir == null) {
			dir = DIR.ORTHO.rnd();
		}
		
		spot = SpotMaker.get(men, dir);
		
		state = STATE.WARNING;
		timer = 0;
		
		GAME.stats().INVASIONS.inc(1);
		
		CharSequence title = Text.¤¤invasion;
		CharSequence text = Str.TMP.clear().add(Text.¤¤invasionD).insert(0, men).insert(1, spot.dir.perpendicular().getName());
		
		new MessageText(title, text).send();
		
		
	}
	

	
	public boolean update(double ds) {
		timer += ds;
		
		if (spot.body.width()*spot.body.height() < 8) {
			GAME.Warn(""+spot.body);
			return false;
		}
		switch(state) {
		case WARNING:
			if (timer > TIME.secondsPerDay/8 && spot.launchProj()) {
				CharSequence title = Text.¤¤Bombardment;
				CharSequence text = Text.¤¤BombardmentD;
				new MessageText(title, text).send();
				VIEW.s().getWindow().centererTile.set(spot.body.cX(), spot.body.cY());
				state = STATE.BOMBARD;
				timer = 0;
			}else if (timer > TIME.secondsPerDay/2) {
				launch();
			}
			break;
		case BOMBARD:
			timer += ds*artillery*0.25;
			while(timer >= 1) {
				timer -= RND.rFloat();
				if (!spot.launchProj()) {
					launch();
					break;
				}
			}
			break;
		case PLACEART:
			if (timer < 40)
				return true;
			state = STATE.DEPLOYING;
			break;
		case DEPLOYING:
			if (divs.size() == 0) {
				timer = 0;
				state = STATE.FIGHTING;
			}else {
				while(timer > 1) {

					Div d = DivDeployer.deploy(divs, spot);
					if (d != null)
						activeDivs.add(d.index());
					timer -= 1;
					if (spot.size < 8) {
						activeDivs.clear();
					}
				}
			}
			
			
			
			break;
		case FIGHTING:
			if (!fight()) {
				state = victory ? STATE.WAIT_FOR_REPLY : STATE.DONE;
				return true;
			}
			if (timer > TIME.secondsPerDay*5) {
				state = STATE.DONE;
				remove();
				CharSequence title = Text.¤¤Retreat;
				CharSequence text = Text.¤¤RetreatD;
				new MessageText(title, text).send();
				return false;
			}
			break;
		case WAIT_FOR_REPLY:
			return STATS.POP().pop(HTYPE.ENEMY) > 0;
		default:
			return false;
			
		
		}
		return true;
	}
	
	private void launch() {
		state = STATE.DEPLOYING;
		timer = 0;
		if (ArtilleryPlacer.placeArt(divs, spot, artillery)) {
			state = STATE.PLACEART;
		}
		
		CharSequence title = Text.¤¤Deployment;
		CharSequence text = Text.¤¤DeploymentD;
		VIEW.s().getWindow().centererTile.set(spot.body.cX(), spot.body.cY());
		new MessageText(title, text).send();
	}
	
	boolean fight() {
		
		
		
		for (int i = 0; i < activeDivs.size(); i++) {
			Div d = SETT.ARMIES().division((short) activeDivs.get(i));
			if (!d.order().active() && d.menNrOf() == 0) {
				activeDivs.remove(i);
				i--;
			}
		}
		if (activeDivs.size() == 0) {
			
			int am = 0;
			for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
				if (e instanceof Humanoid) {
					Humanoid h = (Humanoid) e;
					if (SETT.PATH().reachability.is(h.tc()) && h.indu().hType() == HTYPE.ENEMY && STATS.BATTLE().ROUTING.indu().get(h.indu()) !=0) {
						am++;
					}
				}
			}
			
			String m = "" + Str.TMP.clear().add(Text.¤¤Victory).insert(0, am);
			
			ACTION yes = new ACTION() {
				
				@Override
				public void exe() {
					for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
						if (e instanceof Humanoid) {
							Humanoid h = (Humanoid) e;
							if (SETT.PATH().reachability.is(h.tc()) && h.indu().hType() == HTYPE.ENEMY && STATS.BATTLE().ROUTING.indu().get(h.indu()) !=0) {
								
								h.HTypeSet(HTYPE.PRISONER, null, null);
								STATS.BATTLE().ROUTING.indu().set(h.indu(), 0);
							}
						}
					}
					state = STATE.DONE;
				}
			};
			
			ACTION no = new ACTION() {
				
				@Override
				public void exe() {
					
					state = STATE.DONE;
				}
			};
			VIEW.inters().yesNo.activate(m, yes, no, false);
			victory = true;
			return false;
		}else if (ARMIES().enemy().men() > 0){
			COORDINATE c = THRONE.coo();
			for (int x = c.x()-1; x < c.x()+2; x++) {
				for (int y = c.y()-1; y < c.y()+2; y++) {
					for (ENTITY e : SETT.ENTITIES().getAtTile(x, y)){
						if (e instanceof Humanoid) {
							if (((Humanoid)e).indu().hType() == HTYPE.ENEMY  && ((Humanoid)e).division() != null) {
								victory = false;
								loose(Text.¤¤LooseD);
								return false;
							}
						}
					}
				}
			}
		}
		
		return true;
		
	}
	
	private void remove() {
		
		check.init();
		for (int i = 0; i < activeDivs.size(); i++) {
			int di = activeDivs.get(i);
			check.isSetAndSet(di);
		}
		
		for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
			if (e instanceof Humanoid) {
				Humanoid h = (Humanoid) e;
				if (h.division() != null && check.isSet(h.division().index())) {
					h.helloMyNameIsInigoMontoyaYouKilledMyFatherPrepareToDie();
				}
			}
		}
	}

	
	private void loose(CharSequence loose) {
		
		double am = 0.25 + 0.05*RND.rInt(6);
		
		SETT.ROOMS().STOCKPILE.removeFromEverywhere(am, -1l, FACTIONS.player().res().outTribute);
		
		int creds = FACTIONS.player().credits().credits() > 0 ? (int) (FACTIONS.player().credits().credits()*0.75) : 0;
		FACTIONS.player().credits().tribute.OUT.inc(creds);
		
		CharSequence t1 = "" + Str.TMP.clear().add(loose).insert(0, creds).insert(1, DicRes.¤¤Currs).insert(2, (int)(100*am));
		
		MessageText m = new MessageText(DicArmy.¤¤Defeat, t1);
		
		if (invadingFacion != -1) {
			
			FACTIONS.rel().overlord.set(FACTIONS.getByIndex(invadingFacion), FACTIONS.player(), 1);
			
			m.paragraph(Str.TMP.clear().add(Text.¤¤LooseDFaction).insert(0, FACTIONS.getByIndex(invadingFacion).appearence().name()));
			
			
			
		}
		
		remove();

		m.send();
	}



	

	
	
	
}
