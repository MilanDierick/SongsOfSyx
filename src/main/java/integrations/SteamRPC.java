package integrations;

import com.codedisaster.steamworks.*;

final class SteamRPC extends Rpcer {

	private final SteamFriends friends;
	
	public SteamRPC() {
		friends = new SteamFriends(friendsCallback);
		
	}


	@Override
	protected void dispose() {
		friends.dispose();
	}

	@Override
	public void update(String state, String details) {
		if (INTEGRATIONS.steamRunning()) {
			try {
				if (details != null)
					friends.setRichPresence("text", state + " | " + details);
				else
					friends.setRichPresence("text", state);
				friends.setRichPresence("steam_display", "#StatusFull");
			}catch(Exception e) {
				e.printStackTrace();
				throw new RuntimeException("Something is wrong with RPC. Please uncheck RPC in the launcher and see if it helps.");
			}
			
		}
		
	}
	
	private SteamFriendsCallback friendsCallback = new SteamFriendsCallback() {
		@Override
		public void onSetPersonaNameResponse(boolean success, boolean localSuccess, SteamResult result) {

		}

		@Override
		public void onPersonaStateChange(SteamID steamID, SteamFriends.PersonaChange change) {

		}

		@Override
		public void onGameOverlayActivated(boolean active) {

		}

		@Override
		public void onGameLobbyJoinRequested(SteamID steamIDLobby, SteamID steamIDFriend) {

		}

		@Override
		public void onAvatarImageLoaded(SteamID steamID, int image, int width, int height) {

		}

		@Override
		public void onFriendRichPresenceUpdate(SteamID steamIDFriend, int appID) {

		}

		@Override
		public void onGameRichPresenceJoinRequested(SteamID steamIDFriend, String connect) {

		}

		@Override
		public void onGameServerChangeRequested(String server, String password) {

		}
	};


}
