package game.tourism;

import game.faction.FACTIONS;
import init.race.RACES;
import settlement.entity.humanoid.HTYPE;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.RoomInstance;
import settlement.room.service.module.RoomServiceAccess;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import snake2d.util.MATH;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.Json;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;

final class Text {
	
	public final Entry rating;
	public final Entry attraction;
	public final Entry service;
	public final Entry inn;
	
	private static final Str str = new Str(128);
	
	public Text(Json json){
		rating = new Entry(0, json, "RATING") {
			@Override
			public Str get(double rating, Induvidual i, COORDINATE inn, RoomServiceAccess service) {
				Str str = super.get(rating, i, inn, null);
				str.insert("CITY_NAME", FACTIONS.player().name);
				str.insert("RULER_NAME", FACTIONS.player().ruler().name);
				return str;
			}
		};
		attraction = new Entry(1, json, "ATTRACTION") {
			@Override
			public Str get(double rating, Induvidual i, COORDINATE inn, RoomServiceAccess service) {
				Str str = super.get(rating, i, inn, null);
				RoomBlueprintImp att = TOURISM.attraction(i);
				str.insert("ROOMS", att.info.names);
				str.insert("ROOM", att.info.name);
				str.insert("EMP_TITLE", att.employment().title);
				str.insert("RACE", i.race().info.name);
				str.insert("RACES", i.race().info.names);
				str.insert("RACE_POSSESIVE", i.race().info.namePosessive);
				return str;
			}
		};
		service = new Entry(2, json, "SERVICE") {
			@Override
			public Str get(double rating, Induvidual i, COORDINATE inn, RoomServiceAccess service) {
				
				Str str = super.get(rating, i, inn, null);

				RoomBlueprintImp s = service.room();
				
				str.insert("SERVICE", s.info.name);
				str.insert("SERVICES", s.info.names);
				
				
				
				return str;
			}
		};
		inn = new Entry(3, json, "INN") {
			@Override
			public Str get(double rating, Induvidual i, COORDINATE inn, RoomServiceAccess service) {

				Str str = super.get(rating, i, inn, null);
				
				RoomInstance ins = SETT.ROOMS().INN.getter.get(inn);
				if (ins == null)
					return str;
				
				str.insert("NAME_INN", ins.name());
				if (ins.employees().employed() > 0){
					int e = (int) (ins.employees().employed()*RND.rFloat());
					for (Humanoid a : ins.employees().employees()) {
						if (e-- <= 0) {
							str.insert("HOST_NAME", STATS.APPEARANCE().name(a.indu()));
							break;
						}
					}
				}else {
					str.insert("HOST_NAME", STATS.APPEARANCE().name(RACES.all().rnd(), HTYPE.SUBJECT, 0, RND.rInt(), 0));
				}
				
				
				
				return str;
			}
			
		};
	}
	
	static class Entry {
		
		private CharSequence[][] chars = new CharSequence[3][];
		private final int scroll;
		
		Entry(int index, Json json, String key){
			scroll = index*8;
			if (json != null) {
				json = json.json(key);
				chars[0] = json.texts("BAD");
				chars[1] = json.texts("OK");
				chars[2] = json.texts("GOOD");
			}else {
				chars[0] = new CharSequence[] {""};
				chars[1] = new CharSequence[] {""};
				chars[2] = new CharSequence[] {""};
			}
		}
		
		public Str get(double rating, Induvidual i, COORDINATE inn, RoomServiceAccess service) {
			int ri = (int) Math.round((rating*2 - 0.25));
			ri = CLAMP.i(ri, 0, 2);
			int r = (int) (STATS.RAN().get(i, scroll));
			str.clear().add(chars[ri][MATH.mod(r, chars[ri].length)]);
			return str;	
		}
		
	}
	
	
	
}