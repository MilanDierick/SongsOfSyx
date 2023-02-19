package init.sprite;

import java.io.IOException;

import init.paths.PATHS;
import util.spritecomposer.*;
import util.spritecomposer.ComposerThings.*;

public class Icons{
	
	public final M m = new M();
	public final S s = new S();
	public final L l = new L();
	
	public Icons() throws IOException{

		
	}
	
	public static class M {
		
		int i = 0;
		
		private ISpriteData ge = new ISpriteData() {
			@Override
			protected SpriteData init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.singles.setSkip(i++, 1).paste(true);
				return d.s24.saveSprite();
			}
		};
		
		{
			new IInit(PATHS.SPRITE_UI().get("IconMedium"), 492, 612) {
				
				@Override
				protected void init(ComposerUtil c, ComposerSources s, ComposerDests d) throws IOException {
					s.singles.init(0, 0, 1, 1, 8, 32, d.s24);
				}
			};
		}
		public final ICON.MEDIUM clear_structure = get();
		public final ICON.MEDIUM clear_tunnel = get();
		public final ICON.MEDIUM job_sleep = get();
		public final ICON.MEDIUM job_awake = get();
		public final ICON.MEDIUM agriculture = get();
		public final ICON.MEDIUM axe = get();
		public final ICON.MEDIUM cancel = get();
		public final ICON.MEDIUM terrain = get();
		public final ICON.MEDIUM clear_room = get();
		public final ICON.MEDIUM crossair = get();
		public final ICON.MEDIUM shield_big = get();
		public final ICON.MEDIUM wall = get();
		public final ICON.MEDIUM anti = get();
		public final ICON.MEDIUM copy = get();
		public final ICON.MEDIUM noble = get();
		public final ICON.MEDIUM cat_food = get();
		public final ICON.MEDIUM wildlife = get();
		public final ICON.MEDIUM clearstone = get();
		public final ICON.MEDIUM tunnel_fill = get();
		public final ICON.MEDIUM religion = get();
		public final ICON.MEDIUM clear_all = get();
		public final ICON.MEDIUM furniture = get();
		public final ICON.MEDIUM skull = get();
		public final ICON.MEDIUM descrimination = get();
		public final ICON.MEDIUM admin = get();
		public final ICON.MEDIUM polishCave = get();
		public final ICON.MEDIUM ok = get();
		public final ICON.MEDIUM questionmark = get();
		public final ICON.MEDIUM arrow_up = get();
		public final ICON.MEDIUM arrow_right = get();
		public final ICON.MEDIUM arrow_down = get();
		public final ICON.MEDIUM arrow_left = get();
		public final ICON.MEDIUM book = get();
		public final ICON.MEDIUM expand = get();
		public final ICON.MEDIUM shrink = get();
		public final ICON.MEDIUM citizen = get();
		public final ICON.MEDIUM rebellion = get();
		public final ICON.MEDIUM shovel = get();
		public final ICON.MEDIUM urn = get();
		public final ICON.MEDIUM fillWater = get();
		public final ICON.MEDIUM digCanal = get();
		public final ICON.MEDIUM stength = get();
		public final ICON.MEDIUM plus = get();
		public final ICON.MEDIUM minus = get();
		public final ICON.MEDIUM rotate = get();
		public final ICON.MEDIUM exit = get();
		public final ICON.MEDIUM repair = get();
		public final ICON.MEDIUM time = get();
		public final ICON.MEDIUM menu = get();
		public final ICON.MEDIUM camera = get();
		public final ICON.MEDIUM city = get();
		public final ICON.MEDIUM map = get();
		public final ICON.MEDIUM flag = get();
		public final ICON.MEDIUM cog = get();
		public final ICON.MEDIUM openscroll = get();
		public final ICON.MEDIUM raw_materials = get();
		public final ICON.MEDIUM refiner = get();
		public final ICON.MEDIUM service = get();
		public final ICON.MEDIUM infra = get();
		public final ICON.MEDIUM building = get();
		public final ICON.MEDIUM pickaxe = get();
		public final ICON.MEDIUM place_fill = get();
		public final ICON.MEDIUM shield = get();
		public final ICON.MEDIUM horn = get();
		public final ICON.MEDIUM clear_food = get();
		public final ICON.MEDIUM for_loose = get();
		public final ICON.MEDIUM for_tight = get();
		public final ICON.MEDIUM fast_forw = get();
		public final ICON.MEDIUM for_muster = get();
		public final ICON.MEDIUM circle_frame = get();
		public final ICON.MEDIUM circle_inner = get();
		{get();}
		public final ICON.MEDIUM cog_big = get();
		public final ICON.MEDIUM place_brush = get();
		public final ICON.MEDIUM place_rec = get();
		public final ICON.MEDIUM place_line = get();
		public final ICON.MEDIUM place_ellispse = get();
		public final ICON.MEDIUM place_rec_hollow = get();
		public final ICON.MEDIUM trash = get();
		public final ICON.MEDIUM menu2 = get();
		public final ICON.MEDIUM law = get();
		public final ICON.MEDIUM overwrite = get();
		public final ICON.MEDIUM workshop = get();
		public final ICON.MEDIUM slave = get();
		public final ICON.MEDIUM entertainment = get();
		public final ICON.MEDIUM sanitation = get();
		public final ICON.MEDIUM sword = get();
		public final ICON.MEDIUM fish = get();
		public final ICON.MEDIUM pasture = get();
		public final ICON.MEDIUM heart = get();
		public final ICON.MEDIUM lock = get();
		public final ICON.MEDIUM search = get();
		public final ICON.MEDIUM bow = get();
		public final ICON.MEDIUM fortification = get();
		public final ICON.MEDIUM disease = get();
		public final ICON.MEDIUM ceiling = get();
		public final ICON.MEDIUM wallceiling = get();
		public final ICON.MEDIUM chainsFree = get();
		
		private M() throws IOException{
			ge = null;
		}
		
		private ICON.MEDIUM get() throws IOException {
			return IIcon.MEDIUM.get(ge.get());
		}


		
	}
	
	public static class S {
		
		int i = 0;
		
		private ISpriteData ge = new ISpriteData() {
			@Override
			protected SpriteData init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.singles.setSkip(i++, 1).paste(true);
				return d.s16.saveSprite();
			}
		};
		
		{
			new IInit(PATHS.SPRITE_UI().get("IconSmall"), 188, 500) {
				
				@Override
				protected void init(ComposerUtil c, ComposerSources s, ComposerDests d) throws IOException {
					s.singles.init(0, 0, 1, 1, 4, 32, d.s16);
				}
			};
		}
		
		public final ICON.SMALL magnifier = get();
		public final ICON.SMALL minifier = get();
		public final ICON.SMALL minimap = get();
		public final ICON.SMALL arrowUp = get();
		public final ICON.SMALL arrowDown = get();
		public final ICON.SMALL cancel = get();
		public final ICON.SMALL camera = get();
		public final ICON.SMALL crazy = get();
		public final ICON.SMALL menu = get();
		public final ICON.SMALL cog = get();
		public final ICON.SMALL question = get();
		public final ICON.SMALL urn = get();
		public final ICON.SMALL magnifierBig = get();
		public final ICON.SMALL minifierBig = get();
		public final ICON.SMALL human = get();
		public final ICON.SMALL hammer = get();
		public final ICON.SMALL column = get();
		public final ICON.SMALL vial = get();
		public final ICON.SMALL crate = get();
		public final ICON.SMALL plate = get();
		public final ICON.SMALL sword = get();
		public final ICON.SMALL money = get();
		public final ICON.SMALL crossheir = get();
		public final ICON.SMALL standard = get();
		public final ICON.SMALL temperature = get();
		public final ICON.SMALL eye = get();
		public final ICON.SMALL law = get();
		
		public final ICON.SMALL pickaxe = get();
		public final ICON.SMALL shield = get();
		public final ICON.SMALL hammer_lying = get();
		public final ICON.SMALL sprout = get();
		public final ICON.SMALL fence = get();
		public final ICON.SMALL bow = get();
		public final ICON.SMALL fish = get();
		public final ICON.SMALL heart = get();
		public final ICON.SMALL citizen = get();
		public final ICON.SMALL slave = get();
		public final ICON.SMALL noble = get();
		public final ICON.SMALL world = get();
		public final ICON.SMALL admin = get();
		public final ICON.SMALL muster = get();
		public final ICON.SMALL time = get();
		public final ICON.SMALL ice = get();
		public final ICON.SMALL heat = get();
		public final ICON.SMALL pluses = get();
		public final ICON.SMALL squatter = get();
		public final ICON.SMALL fly = get();
		public final ICON.SMALL jug = get();
		public final ICON.SMALL bed = get();
		public final ICON.SMALL alert = get();
		public final ICON.SMALL arrow_right = get();
		public final ICON.SMALL arrow_left = get();
		public final ICON.SMALL plus = get();
		public final ICON.SMALL minus = get();
		public final ICON.SMALL allRight = get();
		public final ICON.SMALL circle = get();
		public final ICON.SMALL clock = get();
		public final ICON.SMALL death = get();
		public final ICON.SMALL dot = get();
		public final ICON.SMALL house = get();
		public final ICON.SMALL degrade = get();
		public final ICON.SMALL fist = get();
		public final ICON.SMALL armour = get();
		public final ICON.SMALL pierce = get();
		public final ICON.SMALL speed = get();
		public final ICON.SMALL boom = get();
		public final ICON.SMALL drop = get();
		public final ICON.SMALL star = get();
		{
			get();
		}

		private S() throws IOException{
			ge = null;
		}
		
		private ICON.SMALL get() throws IOException {
			return IIcon.SMALL.get(ge.get());
		}

		
	}
	
	public static class L{
		
		private int i = 0;

		
		private ISpriteData ge = new ISpriteData() {
			@Override
			protected SpriteData init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.singles.setSkip(i++, 1).paste(true);
				return d.s32.saveSprite();
			}
		};
		
		{
			new IInit(PATHS.SPRITE_UI().get("IconLarge"), 620, 400) {
				
				@Override
				protected void init(ComposerUtil c, ComposerSources s, ComposerDests d) throws IOException {
					s.singles.init(0, 0, 1, 1, 8, 32, d.s32);
				}
			};
		}
		
		{get();}
		{get();}
		{get();}
		{get();}
		public final ICON.BIG dop_realms = get();
		public final ICON.BIG view_military = get();
		{get();}
		{get();}
		public final ICON.BIG season_spring = get();
		public final ICON.BIG season_summer = get();
		public final ICON.BIG season_autumn = get();
		public final ICON.BIG season_winter = get();
		{get();}
		{get();}
		{get();}
		{get();}
		{get();}
		{get();}
		public final ICON.BIG mysteryman = get();
		
	 
		
		public final ICON.BIG bannerPole = get();
		{i = 5*8;}
		public final ICON.BIG[] banners = new ICON.BIG[] {
			get(),get(),get(),get(),
			get(),get(),get(),get()
		};
		
		private L() throws IOException{
			ge = null;
		}
		
		private ICON.BIG get() throws IOException {
			return IIcon.LARGE.get(ge.get());
		}
		
	}
	
}
