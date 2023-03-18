package integrations;

import club.minnced.discord.rpc.*;

class RPCDiscord extends Rpcer {

	private DiscordRPC rpcLib;
	// to set time elapsed to total gametime since start
	private long startTime;

	public RPCDiscord() {
		rpcLib = DiscordRPC.INSTANCE;
		startTime = System.currentTimeMillis() / 1000;
		String applicationId = "618471189722955807";
		String steamId = "";
		DiscordEventHandlers handlers = new DiscordEventHandlers();
		// handlers.ready = new OnReady() {
		//
		// @Override
		// public void accept(DiscordUser arg0) {
		// System.out.println("Ready!");
		//
		// }
		// };
		rpcLib.Discord_Initialize(applicationId, handlers, true, steamId);
	}

	@Override
	public void update(String state, String details) {
		DiscordRichPresence discordPresence = new DiscordRichPresence();
		discordPresence.state = state;
		discordPresence.details = details;
		discordPresence.startTimestamp = this.startTime;
		discordPresence.smallImageKey = "city4";
		rpcLib.Discord_UpdatePresence(discordPresence);
	}

	// call this whenever the game is closed
	@Override
	public void dispose() {
		rpcLib.Discord_Shutdown();
	}
}