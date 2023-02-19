package game.faction.player;

import game.faction.FResources;
import game.time.TIME;
import init.D;
import util.statistics.HistoryResource;

public final class PResources extends FResources{
	
	{D.gInit(this);}
	
	public final HistoryResource inTribute = new InOut(
			D.g("Tribute"), 
			D.g("TributeD", "How much gained through payment of other factions."), in);
	public final HistoryResource inDemolition = new InOut(
			D.g("demolition"), 
			D.g("demoDesc", "The amount spent gained by demolishing structures."), in);
	public final HistoryResource inProduced = new InOut(
			D.g("Produced"), 
			D.g("ProducedD", "How much gained through production, mining and extraction."), in);
	public final HistoryResource inMilitary = new InOut(
			D.g("Military"), 
			D.g("MilitaryD", "Resources gained from conquest, or army supplies sent back."), in);
	
	public final HistoryResource outMaintenance = new InOut(
			D.g("maintenance"), 
			D.g("maintDesc", "The amount spent on maintenance."), out);
	public final HistoryResource outHousehold = new InOut(
			D.g("household"), 
			D.g("householdDesc", "The amount spent on housing and housing upkeep."), out);
	public final HistoryResource outConstruction = new InOut(
			D.g("construction"), 
			D.g("constructionDesc", "The amount spent on construction."), out);
	public final HistoryResource outSpoilt = new InOut(
			D.g("spoilage"), 
			D.g("spoilageDesc", "The amount lost due to spoilage."), out);
	public final HistoryResource outTheft = new InOut(
			D.g("Theft"), 
			D.g("TheftDesc", "The amount lost due to theft by your criminals."), out);
	public final HistoryResource outConsumed = new InOut(
			D.g("Consumed"), 
			D.g("ConsumedD", "How much loss through consumption."), out);
	public final HistoryResource outMilitary = new InOut(
			inMilitary.info().name, 
			D.g("OutMilitaryD", "Resources spent on supplying the army."), out);
	public final HistoryResource outSacrificed = new InOut(
			D.g("Sacrificed"), 
			D.g("SacrificedD", "Resources sacrificed to the gods."), out);
	
	
	public PResources() {
		super(48, TIME.seasons());
		ins = ins.join(inTribute, inProduced, inDemolition, inMilitary);
		outs = outs.join(outMaintenance, outHousehold, outConstruction, outSpoilt, outTheft, outConsumed, outMilitary, outSacrificed);
	}
	
}