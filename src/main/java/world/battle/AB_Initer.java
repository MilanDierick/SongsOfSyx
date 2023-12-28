package world.battle;

import game.Profiler;
import game.faction.FACTIONS;
import game.time.TIME;
import init.config.Config;
import snake2d.util.misc.CLAMP;
import view.main.VIEW;
import world.army.util.Power.PowerSpec;
import world.battle.spec.*;
import world.regions.Region;
import world.regions.data.RD;

final class AB_Initer {

	private final ISide sa = new ISide();
	private final ISide sb = new ISide();
	private final AC_Resolver resolver;
	private final PBattle pbattle;
	private final PSiege pSiege;
	
	AB_Initer(PUnitFactory uf){
		resolver = new AC_Resolver(uf);
		pbattle = new PBattle(uf, resolver);
		pSiege = new PSiege(uf, resolver);
	}
	
	public boolean handle(Side A, Side B) {
		
		
		
		init(A, B);

		double vi = 1.0/(sa.attackV+sb.attackV);
		sa.attackV *= vi;
		sb.attackV *= vi;

		
		boolean ret = handle();
		return ret;
	}
	
	public void handle(Side A, Side B, Region besigedB, double besigeTimer) {
		
		if (B.men <= 0) {
			sb.side = B;
			sa.side = A;
			setNoLosses(sb.losses, sb);
			setNoLosses(sa.losses, sa);
			resolver.conquer(A, sa.losses, B, sb.losses, besigedB);
			return;
		}
			
		
		init(A, B);
		double fort = TIME.years().bitSeconds()/(2*besigeTimer+1)-1;
		fort = CLAMP.d(fort, 0, 1);
		fort *= besigedB == FACTIONS.player().capitolRegion() ? 10 : RD.MILITARY().defences.get(besigedB);
		if (fort < 0)
			fort = 0;
		if (sa.attackV < sb.attackV) {
			if (retreatWill(sa, sb)) {
				handle();
				return;
			}
			
			if (!A.isPlayer && !B.isPlayer) {
				handle();
				return;
			}
		}
		
		sb.attackV *= fort;
		
		double vi = 1.0/(sa.attackV+sb.attackV);
		sa.attackV *= vi;
		sb.attackV *= vi;

		if (A.isPlayer) {
			pSiege.init(sa, sb, besigedB, fort);
			return;
		}
		
		if(sa.attackV < sb.attackV) {
			return;
		}else {
			if (besigedB == FACTIONS.player().capitolRegion())
				resolver.conquer(A, sa.losses, B, sb.losses, besigedB);
			else
				handle();
		}
		
	}
	

	
	private void init(Side A, Side B) {
		sa.side = A;
		sa.spec.clear();
		sb.side = B;
		sb.spec.clear();
		
		Profiler.LIVE.logStart(A);
		
		for (int i = 0; i < A.divs(); i++) {
			sa.spec.add(A.div(i));
		}
		for (int i = 0; i < B.divs(); i++) {
			sb.spec.add(B.div(i));
		}
		
		Profiler.LIVE.logEnd(A);

		sa.attackV = 1.0;
		sb.attackV = 1.0;
		for (int i = 0; i < sa.spec.attack.length; i++) {
			sa.attackV += (sa.spec.attack[i] + (sa.spec.range*sa.spec.ranged[i]/(sb.spec.speed+1)))/(sb.spec.defence[i]+1);
			sb.attackV += (sb.spec.attack[i] + (sb.spec.range*sb.spec.ranged[i]/(sa.spec.speed+1)))/(sa.spec.defence[i]+1);
		}
		
		sa.attackV *= mul(A);
		sb.attackV *= mul(B);

	}
	
	public boolean handle() {
		
		if (retreatWill(sa, sb)) {
			return retreat(sa, sb);
		}
		else if (retreatWill(sb, sa)) {
			return retreat(sb, sa);
		}

		if (handlePlayer(sa, sb))
			return true;
		if (handlePlayer(sb, sa))
			return true;
		
		fight(sa, sb);
		return true;
	}
	
	private boolean retreatWill(ISide retreater, ISide other) {
		
		if (retreater.side.isPlayer)
			return false;
		
		if (retreater.attackV > 0.5)
			return false;
		
		if (!retreater.side.mustFight)
			return true;
		
		if (!resolver.canRetreat(retreater.side)) {
			return false;
		}
		
		
		double v = retreater.attackV;
		
		double dd = (retreater.side.div(0).men()%16) / 16.0;
		
		v -= dd;
		
		if (v <= 0)
			return true;
		
		return false;
		
	}
	
	private boolean retreat(ISide retreater, ISide other) {
		if (setRetreatLosses(retreater.losses, retreater, other)) {
			setNoLosses(other.losses, other);
			resolver.retreat(retreater.side, retreater.losses, other.side, other.losses);
			return true;
		}
		return false;
	}
	
	private boolean handlePlayer(ISide p, ISide e) {
		if (!p.side.isPlayer)
			return false;
		pbattle.init(p, e);
		return true;
	}

	
	private static boolean setRetreatLosses(int[] lossess, ISide retreater, ISide other) {
		boolean ret = false;
		setLooseLosses(lossess, retreater, other);
		for (int i = 0; i < retreater.side.divs(); i++) {
			int losses = 0;
			if(retreater.side.unit(i).mustFight) {
				lossess[i] = (int) Math.ceil(lossess[i]*WBattles.retreatPenalty);
				ret = true;
			}else
				lossess[i] = losses;
		}
		return ret;
	}
	
	private static void setLooseLosses(int[] lossess, ISide looser, ISide other) {
		for (int i = 0; i < looser.side.divs(); i++) {
			int m =  looser.side.div(i).men();
			int losses = (int) (Config.BATTLE.MEN_PER_DIVISION*(other.attackV + Rnd.f()*0.5));;
			losses = CLAMP.i(losses, 0, m);
			lossess[i] = losses;
			
		}
	}
	
	private static void setWinLosses(int[] lossess, ISide winner, ISide other) {
		for (int i = 0; i < winner.side.divs(); i++) {
			
			int m =  winner.side.div(i).men();
			int losses = (int) (Rnd.f()*m*(other.attackV));
			losses = CLAMP.i(losses, 0, m);
			lossess[i] = losses;
		}
	}

	
	private static void setNoLosses(int[] lossess, ISide s) {
		for (int i = 0; i < s.side.divs(); i++) {
			lossess[i] = 0;
		}
	}

	
	private void fight(ISide a, ISide b) {
		if (a.attackV >= b.attackV) {
			setWinLosses(a.losses, a, b);
			setLooseLosses(b.losses, b, a);
			resolver.battle(a.side, a.losses, b.side, b.losses);
		}else {
			setWinLosses(b.losses, b, a);
			setLooseLosses(a.losses, a, b);
			resolver.battle(b.side, b.losses, a.side, a.losses);
		}
	}
	
	
	private double mul(Side a) {
		double t = 0;
		int menTot = 1;
		for (SideUnit u : a.us) {
			menTot += u.men;
			t += u.baseMul*u.men;
		}
		t /= menTot;
		return t;
	}
	


	
	private static class ISide {
		
		public Side side;
		public final PowerSpec spec = new PowerSpec();
		public double attackV;
		public final int[] losses = new int[Config.BATTLE.DIVISIONS_PER_ARMY];
		
		public final int[] pLosses = new int[Config.BATTLE.DIVISIONS_PER_ARMY];
		public final int[] pLossesRet = new int[Config.BATTLE.DIVISIONS_PER_ARMY];
		
	}
	
	private static class PBattle extends WBattleSpec {
		
		private final PUnitFactory uf;
		private ISide sPlayer;
		private ISide sEnemy;
		private final AC_Resolver resolver;
		
		PBattle(PUnitFactory uf, AC_Resolver resolver) {
			this.player = new WBattleSide();
			this.enemy = new WBattleSide();
			this.uf = uf;
			this.resolver = resolver;
		}
		
		public void init(ISide player, ISide enemy) {
			this.sPlayer = player;
			this.sEnemy = enemy;
			
			uf.clear();
			victory = player.attackV >= enemy.attackV;
			pInit(player, this.player, victory, enemy, uf);
			pInit(enemy, this.enemy, !victory, player, uf);
			
			VIEW.world().UI.battle.prompt(this);
		}
		
		@Override
		public void retreat() {
			setNoLosses(sEnemy.losses, sEnemy);
			resolver.retreat(sPlayer.side, sPlayer.pLossesRet, sEnemy.side, sEnemy.losses);
		}

		@Override
		public void auto() {
			if (victory) {
				resolver.battle(sPlayer.side, sPlayer.pLosses, sEnemy.side, sEnemy.pLosses);
			}else {
				resolver.battle(sEnemy.side, sEnemy.pLosses, sPlayer.side, sPlayer.pLosses);
			}
		}

		@Override
		public void engage() {
			new AB_UPlayer(sPlayer.side, sEnemy.side, resolver);			
		}
		
		
	}
	
	private static class PSiege extends WBattleSiege {
		
		private final PUnitFactory uf;
		private ISide sPlayer;
		private ISide sEnemy;
		private final AC_Resolver resolver;
		
		PSiege(PUnitFactory uf, AC_Resolver resolver) {
			this.player = new WBattleSide();
			this.enemy = new WBattleSide();
			this.uf = uf;
			this.resolver = resolver;
		}
		
		public void init(ISide player, ISide enemy, Region besigedB, double fort) {
			this.sPlayer = player;
			this.sEnemy = enemy;
			this.besiged = besigedB;
			this.fortifications = fort;
			uf.clear();
			victory = player.attackV >= enemy.attackV;
			pInit(player, this.player, victory, enemy, uf);
			pInit(enemy, this.enemy, !victory, player, uf);
			
			VIEW.world().UI.battle.prompt(this);
		}


		@Override
		public void lift() {
			for (SideUnit u : sPlayer.side.us)
				if (u.a() != null && u.a().faction() == FACTIONS.player())
					u.a().stop();
			
		}

		@Override
		public void auto() {

			if (victory) {
				resolver.conquer(sPlayer.side, sPlayer.pLosses,sEnemy.side, sEnemy.pLosses, besiged);
			}else {
				resolver.battle(sEnemy.side, sEnemy.pLosses, sPlayer.side, sPlayer.pLosses);
			}
			
		}
		
		
	}
	
	private static void pInit(ISide s, WBattleSide bs, boolean victory, ISide other, PUnitFactory uf) {
		bs.artilleryPieces = 0;
		bs.coo.set(s.side.us.get(0).coo);
		bs.losses = 0;
		bs.lossesRetreat = 0;
		bs.men = 0;
		bs.powerBalance = s.attackV;
		bs.units.clear();
		
		if (victory) {
			setWinLosses(s.pLosses, s, other);
		}else {
			setLooseLosses(s.pLosses, s, other);
		}
		setRetreatLosses(s.pLossesRet, s, other);
		
		for (SideUnit su : s.side.us) {
			WBattleUnit u = uf.next(su);
			bs.units.add(u);
			bs.men += u.men;
		}
		
		for (int i = 0; i < s.side.divs(); i++) {
			bs.losses += s.pLosses[i];
			bs.units.get(s.side.ui(i)).losses += s.pLosses[i];
			bs.lossesRetreat += s.pLossesRet[i];
			bs.units.get(s.side.ui(i)).lossesRetreat += s.pLossesRet[i];
		}
	}
	
}
