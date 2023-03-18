package integrations;

final class RPCHandler {

	private static final long interval = 15*1000;
	private final Rpcer[] rpcs;
	private long lastUpdate = 0;
	
	RPCHandler() {
		if (INTEGRATIONS.steamRunning()) {

			rpcs = new Rpcer[] {
				new RPCDiscord(),
				new SteamRPC(),
			};
		}else {
			rpcs = new Rpcer[] {
				new RPCDiscord(),
			};
		}
		
		
	}

	void dispose() {
		for (Rpcer rpc : rpcs) {
			rpc.dispose();
		}
	}

	
	public void update (INTER_RPC rpc) {
		long now = System.currentTimeMillis();
		if (now-lastUpdate > interval) {
			String state = rpc.rpcTitle();
			String[] ds = rpc.rpcDetails();
			String details = "";
			boolean first = true;
			for (String d : ds) {
				if (!first) {
					details += " | ";
				} else {
					first = false;
				}
				details += d;
			}
				
			for (Rpcer p : rpcs) {
				p.update(state, details);
			}
			lastUpdate = now;
		}
	}

}