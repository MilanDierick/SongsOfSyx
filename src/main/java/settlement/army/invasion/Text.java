package settlement.army.invasion;

import init.D;

final class Text {

	static CharSequence ¤¤invasion = "¤Invasion!";
	static CharSequence ¤¤invasionD = "¤An enemy host of {0} men have been seen gathering {1} of the city, preparing an assault. Muster your forces and fight them. If they reach the throne, it is all over.";
	static CharSequence ¤¤Bombardment = "¤Bombardment";
	static CharSequence ¤¤BombardmentD = "¤The enemy has started bombarding us to clear a path. There is nothing we can do but take cover until they are done.";
	
	static CharSequence ¤¤Deployment = "¤Deployment";
	static CharSequence ¤¤DeploymentD = "¤The enemy are deploying their troops. May the gods help us!";
	
	static CharSequence ¤¤Retreat = "¤Retreat!";
	static CharSequence ¤¤RetreatD = "¤Enemy forces are weary of fighting and have retreated!";

	static CharSequence ¤¤LooseD = "¤It's over! The enemy forces have reached the throne, and have taken control of the city. They have sacked your treasury for {0} {1}, and collected {2}% of your warehouse stock.";
	static CharSequence ¤¤LooseDFaction = "¤The faction of {0} sends its regards. Now that you've bent the knee, they hope you've learn your lesson, and you are now at peace.";
	static CharSequence ¤¤Victory = "¤Our men have prevailed and our foe is beaten. Rejoice! The {0} survivors can be turned into prisoners, and needs a prison to stay in. Do you accept them? Declining will have the remaining enemies chased down and killed.";
	
	static {
		D.ts(Text.class);
	}
	
}
