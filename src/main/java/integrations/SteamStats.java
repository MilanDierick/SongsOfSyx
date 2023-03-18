package integrations;

import com.codedisaster.steamworks.*;

final class SteamStats {

	private SteamUserStats stats;
	@SuppressWarnings("unused")
	private SteamResult statResult;
	private volatile boolean isInit=false;
	private int numAchievements;

	private SteamUserStatsCallback userStatsCallback = new SteamUserStatsCallback() {

		@Override
		public void onUserStatsReceived(long gameId, SteamID steamIDUser, SteamResult result) {
			INTEGRATIONS.log("UserStats received!");
			INTEGRATIONS.log("GameID: "+gameId);
			INTEGRATIONS.log("UserID: "+steamIDUser);
			INTEGRATIONS.log("Result: "+result);
			statResult=result;
			numAchievements=stats.getNumAchievements();
			isInit=true;
		}

		@Override
		public void onUserStatsStored(long gameId, SteamResult result) {
			INTEGRATIONS.log("UserStats stored!");
			INTEGRATIONS.log("Result: "+result);			
		}

		@Override
		public void onUserStatsUnloaded(SteamID steamIDUser) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onUserAchievementStored(long gameId, boolean isGroupAchievement, String achievementName,
				int curProgress, int maxProgress) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onLeaderboardFindResult(SteamLeaderboardHandle leaderboard, boolean found) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onLeaderboardScoresDownloaded(SteamLeaderboardHandle leaderboard,
				SteamLeaderboardEntriesHandle entries, int numEntries) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onLeaderboardScoreUploaded(boolean success, SteamLeaderboardHandle leaderboard, int score,
				boolean scoreChanged, int globalRankNew, int globalRankPrevious) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onGlobalStatsReceived(long gameId, SteamResult result) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onNumberOfCurrentPlayersReceived(boolean arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}

	};


	public SteamStats() {
		init();
	}

	private void init() {

		INTEGRATIONS.log("Register Userstats ...");
		stats= new SteamUserStats(userStatsCallback);
		stats.requestCurrentStats();
		for (int i = 0; i < 5000; i++) {
			if (isInit)
				return;
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unused")
	private void listAchievements() {
		INTEGRATIONS.log("Songs of Syx has "+numAchievements+" Achievements:");
		for(int i = 0; i<numAchievements; i++)
			INTEGRATIONS.log("Achievement "+i+": "+stats.getAchievementName(i));
	}

	public void getStat(String name) {
		if(isInit) {
			for(int i = 0; i<34; i++)
				INTEGRATIONS.log("Achievement Name: "+stats.getAchievementName(i));
			int value= 100;
			value=stats.getStatI(name, value);
			INTEGRATIONS.log("Stat "+name+" has the value of "+value);
		}else
			INTEGRATIONS.log("SteamStats not properly initialized");
	}

	//Incrementing a stat
	public boolean incStat(String name, int value) {
		if(isInit) {
			int oldValue = stats.getStatI(name, value);
			int newValue = oldValue+value;
			INTEGRATIONS.log("Increasing "+name+" by "+value+" to "+newValue);
			return stats.setStatI(name, oldValue+value);
		}else {
			INTEGRATIONS.log("SteamStats not properly initialized");
			return false;
		}
	}
	
	public boolean setStat(String name, int value) {
		if(isInit) {
			return stats.setStatI(name, value);
		}else {
			INTEGRATIONS.log("SteamStats not properly initialized");
			return false;
		}
	}

	//Setting a highscore for a stat
	public boolean setMaxStat(String name, int value) {
		if(isInit) {
			int oldValue = stats.getStatI(name, value);
			if(value>oldValue)
				return stats.setStatI(name, value);
			else {
				INTEGRATIONS.log("Trying to set a new highscore ("+value+") for "+name+" but the old one is higher ("+oldValue+")");
				return false;
			}
		} else {
			INTEGRATIONS.log("SteamStats not properly initialized");
			return false;
		}
	}

	public boolean getAchieved(String name) {
		boolean isAchieved = false;
		if(isInit) {
			isAchieved=stats.isAchieved(name, isAchieved);
			INTEGRATIONS.log("Achievement "+name+" achieved?  "+isAchieved);
			return isAchieved;
		}else {
			INTEGRATIONS.log("SteamStats not properly initialized");
			return false;
		}
	}

	//Show an progress popup in Steamoverlay
	public boolean indicateProgress(String name) {
		int curProgress=0;
		int maxProgress=0;
		boolean result = stats.indicateAchievementProgress(name, curProgress, maxProgress);
		INTEGRATIONS.log("Indicating progress for "+name+" ("+curProgress+"/"+maxProgress+")");
		return result;
	}

	//Unlocks an achievement
	public void setAchieved(String name) {
		boolean isAchieved = false;
		if(isInit) {
			isAchieved=stats.isAchieved(name, isAchieved);
			if(!isAchieved) {
				stats.setAchievement(name);
				stats.storeStats();
				INTEGRATIONS.log("Unlocking Achievement "+name);
			}else
				INTEGRATIONS.log("Trying to unlock Achievement "+name+" but it is already unlocked");
		}else
			INTEGRATIONS.log("SteamStats not properly initialized");
	}

	//Publish and sync changes to Steam server
	public void storeStats() {
		INTEGRATIONS.log("Storing UserStats...");
		stats.storeStats();
	}

	//deletes ALL stats and optionally Achievements
	//ONLY for testing!!
	public void Reset(boolean achievementsToo) {
		stats.resetAllStats(achievementsToo);
	}

	public void dispose() {
		stats.dispose();
	}
}
