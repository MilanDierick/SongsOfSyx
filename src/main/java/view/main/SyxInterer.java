//package view.main;
//
//import game.faction.FACTIONS;
//import game.time.TIME;
//import integrations.INTEGRATIONS;
//import integrations.INTER_RPC;
//import settlement.entity.humanoid.HCLASS;
//import settlement.stats.STATS;
//import settlement.stats.standing.STANDINGS;
//
//final class SyxInterer implements INTER_RPC {
//
//	public SyxInterer() {
//		//INTEGRATIONS.achivementsReset();
//	}
//
//	@Override
//	public String rpcTitle() {
//		CharSequence faction = FACTIONS.player().appearence().name();
//		return "Ruling over " + faction;
//	}
//
//	@Override
//	public String[] rpcDetails() {
//		int pop = (int) STATS.POP().POP.data(HCLASS.CITIZEN).get(null, 0);
//		int rep = (int) (100*STANDINGS.CITIZEN().happiness.getD(null));
//		int year = (int) (TIME.currentSecond() / TIME.years().bitSeconds());
//		return new String[] { "pop: " + pop, "hap: " + rep + "%", "year: " + year, };
//	}
//
//	void update() {
//		INTEGRATIONS.updateRPC(this);
//	}
//
//}
