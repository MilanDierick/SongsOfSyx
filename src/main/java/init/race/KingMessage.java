package init.race;

import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import game.faction.npc.ruler.RTraits.Title;
import game.faction.player.PTitles.PTitle;
import init.paths.PATHS;
import snake2d.LOG;
import snake2d.util.file.Json;
import snake2d.util.misc.ACTION;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.StrInserter;
import view.interrupter.IDebugPanel;

public class KingMessage {

	public final Message tTrade;
	public final Message tTradeCancel;
	public final Message tPeaceA;
	public final Message tPeaceB;
	public final Message tGift;
	public final Message tRequest;
	public final Message tDemand;
	public final Message tTradeStop;
	public final Message tGangbang;
	public final Message tGreeting;
	
	public static KingMessage make(Json data, ExpandInit init) {
		String key = data.value("KING_FILE");
		if (!init.kmessagess.containsKey(key)) {
			KingMessage m =  new KingMessage(new Json(PATHS.RACE().text.getFolder("king").get(key)));
			init.kmessagess.put(key,m);
			IDebugPanel.add("King message test: " + key, new ACTION() {
				
				@Override
				public void exe() {
					FactionNPC f = FACTIONS.NPCs().rnd();
					FactionNPC o = FACTIONS.NPCs().rnd();
					KingMessage m = f.court().king().roy().induvidual.race().kingMessage();
					
					
					log(m.tTrade, f, o);
					log(m.tPeaceA, f, o);
					log(m.tPeaceB, f, o);
					log( m.tGift, f, o);
					log(m.tRequest, f, o);
					log(m.tDemand, f, o);
					log(m.tTradeStop, f, o);
					log(m.tGangbang, f, o);
				}
				
				private void log(Message m, FactionNPC f, FactionNPC o) {
					LOG.ln(m.key);
					for (int i = 0; i < m.all.length; i++) {
						LOG.ln(m.get(f, o, i));
					}
					LOG.ln();
					
				}
				
			});
		}
		
		return init.kmessagess.get(key);
	}
	
	public KingMessage(Json j) {
		tTrade = new Message(j, "TRADE");
		tTradeCancel = new Message(j, "TRADE_CANCEL");
		tPeaceA = new Message(j, "PEACE_GOOD");
		tPeaceB = new Message(j, "PEACE_BAD");
		tGift = new Message(j, "GIFT");
		tRequest = new Message(j, "REQUEST");
		tDemand = new Message(j, "DEMAND");
		tTradeStop = new Message(j, "STOP_TRADE");
		tGangbang = new Message(j, "GANGBANG");
		tGreeting = new Message(j, "GREETING");
	}
	
	private static final IF third = new IF("NAME_3RD_FACTION") {

		@Override
		protected void set(FactionNPC t, Str str) {
			str.add(t.name);
		}
		
	};
	private static final IF[] in = new IF[] {
		new IF("RACE_PLAYER") {

			@Override
			protected void set(FactionNPC t, Str str) {
				str.add(FACTIONS.player().race().info.namePosessive);
			}
			
		},
		new IF("RACE_FACTION") {

			@Override
			protected void set(FactionNPC t, Str str) {
				str.add(t.race().info.namePosessive);
			}
			
		},
		new IF("NAME_FACTION") {

			@Override
			protected void set(FactionNPC t, Str str) {
				str.add(t.name);
			}
			
		},
		new IF("NAME_PLAYER") {

			@Override
			protected void set(FactionNPC t, Str str) {
				str.add(FACTIONS.player().name);
			}
			
		},
		
		new IF("NAME_RULER_PLAYER") {

			@Override
			protected void set(FactionNPC t, Str str) {
				str.add(FACTIONS.player().ruler().name);
			}
			
		},
		new IF("INTRO_FACTION") {

			@Override
			protected void set(FactionNPC t, Str str) {
				str.add(t.nameIntro);
			}
			
		},
		new IF("NAME_RULER_FACTION") {

			@Override
			protected void set(FactionNPC t, Str str) {
				str.add(t.court().king().name);
			}
			
		},
		new IF("NAME_RULER_PLAYER") {

			@Override
			protected void set(FactionNPC t, Str str) {
				str.add(FACTIONS.player().ruler().name);
			}
			
		},
		new IF("INTRO_RULER_FACTION") {

			@Override
			protected void set(FactionNPC t, Str str) {
				t.court().king().intro(str);
			}
			
		},
		new IF("INTRO_RULER_PLAYER") {

			@Override
			protected void set(FactionNPC t, Str str) {
				str.add(FACTIONS.player().level().current().name());
			}
			
		},
		new IF("TITLES_FACTION") {

			@Override
			protected void set(FactionNPC t, Str str) {
				LIST<Title> tt = t.court().king().roy().traits();
				for (int i = 0; i < tt.size(); i++) {
					str.add(tt.get(i).title);
					if (i < tt.size()-1)
						str.add(',').s();
				}
			}
			
		},
		new IF("TITLES_PLAYER") {

			@Override
			protected void set(FactionNPC ff, Str str) {
				for (PTitle t : FACTIONS.player().titles.all()) {
					if (t.selected()) {
						str.add(t.name);
						str.add(',').s();
					}

				}
			}
			
		},
		
	};
	
	private static final Str  TMP = new Str(250);
	
	public static class Message {
		
		private final String[] all;
		public final String key;
		
		Message(Json j, String key){
			all = j.texts(key);
			this.key = key;
		}
		
		public CharSequence get(FactionNPC f, FactionNPC third) {
			return get(f, third, RND.rInt(all.length));
		}
		
		private CharSequence get(FactionNPC f, FactionNPC third, int mi) {
			TMP.clear();
			TMP.add(all[mi]);
			for (IF ff : in) {
				ff.insert(f, TMP);
			}
			if (third != null)
				KingMessage.third.insert(third, TMP);
			return TMP;
		}
		
		

		
	}
	
	private static abstract class IF extends StrInserter<FactionNPC> {

		public IF(String key) {
			super(key);
		}
		
	}
}
