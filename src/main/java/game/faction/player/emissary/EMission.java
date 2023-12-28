package game.faction.player.emissary;

import game.faction.FACTIONS;
import game.faction.npc.ruler.Royalty;
import world.regions.Region;

public final class EMission {

	int data1;
	int data2;
	double dataD;
	byte type = -1;
	
	public void assign(Region reg, Royalty roy, EMissionType mission) {
		this.type = (byte) mission.index();
		mission.set(this, reg, roy);
	}
	
	public EMissionType mission() {
		return EMissionType.ALL().get(type);
	}
	
	public void recall() {
		FACTIONS.player().emissaries.remove(this);
	}
	
}
