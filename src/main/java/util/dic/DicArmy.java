package util.dic;

import init.D;

public final class DicArmy {

	private DicArmy() {
		
	}
	
	public static CharSequence ¤¤Balance = "¤Balance";
	public static CharSequence ¤¤Army = "¤Army";
	public static CharSequence ¤¤War = "¤War";
	public static CharSequence ¤¤Vassal = "¤Vassal";
	public static CharSequence ¤¤Peace = "¤Peace";
	public static CharSequence ¤¤PeaceDraft = "¤Draft a peace proposal";
	public static CharSequence ¤¤DeclareWar = "¤Declare War";
	public static CharSequence ¤¤WarD = "¤This faction is at war with you, and will send its armies after you until a peace is brokered.";
	public static CharSequence ¤¤DelareWarQ = "¤Are you sure you wish to declare war against {0}?";
	public static CharSequence ¤¤Armies = "¤Armies";
	public static CharSequence ¤¤Artillery = "¤Artillery";
	public static CharSequence ¤¤Soldiers = "¤Soldiers";
	public static CharSequence ¤¤Deployable = "¤Deployable";
	public static CharSequence ¤¤SoldiersD = "¤Soldiers are used to defend your settlement and instill public order. Soldiers can also be sent out to join world armies. The more soldiers you have, the less likely you are to get raided.";
	public static CharSequence ¤¤SoldiersTarget = "¤Soldiers Target";
	
	public static CharSequence ¤¤Campaigning = "¤Campaigning";
	public static CharSequence ¤¤CampaigningD = "¤Soldiers that are out campaigning.";
	
	public static CharSequence ¤¤Losses = "¤Losses";
	
	public static CharSequence ¤¤Recruit = "¤Recruit";
	public static CharSequence ¤¤Recruits = "¤Recruits";
	public static CharSequence ¤¤Conscripts = "¤Conscripts";
	public static CharSequence ¤¤Conscripted = "¤Conscripted";
	public static CharSequence ¤¤ConscriptsD = "¤Conscripts are the population available to draft into armies, minus the ones already drafted. The max amount is determined by your regions, and with time, your actual conscripts will increase towards this target. If no fresh conscripts exists no new divisions can be trained and existing ones will not replenish.";
	
	public static CharSequence ¤¤Casualties = "¤Casualties";
	public static CharSequence ¤¤Conscriptable = "¤Conscriptable";
	public static CharSequence ¤¤Formation = "¤Formation";
	
	public static CharSequence ¤¤Garrison = "¤Garrison";
	public static CharSequence ¤¤GarrisonD = "¤Garrisons will prolong an enemy siege and provide more attrition to enemies located in the region.";
	
	public static CharSequence ¤¤Move = "¤Move Army. Moving armies do not replenish or muster new men.";
	public static CharSequence ¤¤MoveCant = "¤Army can't move when there are no soldiers.";
	public static CharSequence ¤¤Stop = "¤Stop";
	public static CharSequence ¤¤Reassign = "¤Reassign";
	
	public static CharSequence ¤¤Disband = "¤Disband";
	
	public static CharSequence ¤¤Training = "¤Training";
	public static CharSequence ¤¤TrainingD = "¤Training improves soldiers skill, stamina and discipline on the battlefield.";
	
	public static CharSequence ¤¤Equipment = "¤Equipment";
	public static CharSequence ¤¤EquipmentD = "¤Equipment increases offensive and defensive capabilities of your troops.";
	
	public static CharSequence ¤¤Experience = "¤Experience";
	
//	public static CharSequence ¤¤RecruitmentTime = "¤Recruitment Time";
	
	public static CharSequence ¤¤Fortified = "¤Fortified";
	public static CharSequence ¤¤Fortifying = "¤Fortifying";
	
	public static CharSequence ¤¤Division = "¤Division";
	public static CharSequence ¤¤Divisions = "¤Divisions";
	
	public static CharSequence ¤¤SoldiersAreTraining = "¤{0} Conscripts are training.";

	public static CharSequence ¤¤NotRecruiting = "¤No soldiers are being recruited, since this army isn't fortified, or because it is situated outside of your controlled territory.";
	
	public static CharSequence ¤¤Battle = "¤Battle";
	public static CharSequence ¤¤BattleOf = "¤Battle of {0}";
	public static CharSequence ¤¤Engage = "¤Engage";
	public static CharSequence ¤¤AutoResolve = "¤Auto";
	public static CharSequence ¤¤Retreat = "¤Retreat";
	public static CharSequence ¤¤Intercepting = "¤Intercepting {0}";
	public static CharSequence ¤¤Intercept = "¤Intercept";
	public static CharSequence ¤¤MarchingTo = "¤Marching To {0}";
	public static CharSequence ¤¤Morale = "¤Morale";
	public static CharSequence ¤¤MoraleD = "¤Morale determines how long your troops will stand their ground for. Each division has an individual moral, that is also affected by a global one.";
	
	public static CharSequence ¤¤engageD = "¤Take personal command and fight this battle on the field.";
	public static CharSequence ¤¤autoD = "¤Auto resolve this battle. The result will be {0}. You will lose about {1} men and inflict about {2} casualties on the enemy.";
	public static CharSequence ¤¤RetreatD = "¤Make a tactical retreat. You will lose {0} men and some equipment.";
	public static CharSequence ¤¤RetreatCant = "¤Your forces are trapped, they can't retreat.";
	
	public static CharSequence ¤¤Victory = "Victory";
	public static CharSequence ¤¤Defeat = "¤Defeat";
	public static CharSequence ¤¤Annihilation = "¤Annihilation";
	
	public static CharSequence ¤¤Mercenaries = "¤Mercenaries";
	
	public static CharSequence ¤¤Besiege = "¤Besiege";
	public static CharSequence ¤¤Besieging = "¤Besieging";
	public static CharSequence ¤¤BesiegingSomething = "¤Besieging {0}";
	
	public static CharSequence ¤¤Supplies = "¤Supplies";
	public static CharSequence ¤¤SuppliesD = "¤Campaigning armies requires supplies to function. Some are essential, while others only provide morale boosts.";
	public static CharSequence ¤¤Spoils = "¤Spoils";
	public static CharSequence ¤¤Captives = "¤Captives";
	public static CharSequence ¤¤CaptivesD = "¤Captives from the battle. You can decide how many your want to accept into your city as prisoners. The rest will be disposed. Take heed that prisons are needed in order to process these captives.";
	public static CharSequence ¤¤Assault = "¤Assault";
	public static CharSequence ¤¤SiegeOf = "¤Siege of {0}";
	public static CharSequence ¤¤Siege = "¤Siege";
	public static CharSequence ¤¤Attack = "¤Attack";
	public static CharSequence ¤¤Attacking = "¤Attacking";
	public static CharSequence ¤¤Enemy = "¤Enemy";
	public static CharSequence ¤¤Enemies = "¤Enemies";
	public static CharSequence ¤¤Puppet = "¤Puppet";
	public static CharSequence ¤¤Neutral = "¤Neutral";
	public static CharSequence ¤¤Rebels = "¤Rebels";
	public static CharSequence ¤¤Rebel = "¤Rebel";
	
	public static CharSequence ¤¤Muster = "¤Muster";
	public static CharSequence ¤¤Musterd = "¤Mustered";
	public static CharSequence ¤¤MusterDesc = "¤Muster Men. Calls to arms and have your subjects form up in their division and man artillery, or un-muster men and have them return to their civil duties.";
	public static CharSequence ¤¤MusterOneProblem = "¤One or more divisions do not have a position. Set a position by clicking the division, then click and drag on the ground where you want them.";
	public static CharSequence ¤¤MusterProblem = "¤The division do not have a position. Set a position by clicking the division, then click and drag on the ground where you want them.";
	
	public static CharSequence ¤¤Range = "¤Range";
	public static CharSequence ¤¤Ammunition = "¤Ammunition";
	public static CharSequence ¤¤AmmoDesc = "¤Ammunition is used by range weapons.";
	public static CharSequence ¤¤ReloadingXX = "¤Unavailable. Reloading, ({0} seconds remaining)";
	public static CharSequence ¤¤Reloading = "¤Reloading";
	public static CharSequence ¤¤ReadyFire = "¤Ready to Fire.";
	public static CharSequence ¤¤Projectiles = "¤Projectiles";
	public static CharSequence ¤¤Projectile = "¤Projectile";
	public static CharSequence ¤¤Banner = "¤Banner";
	public static CharSequence ¤¤SplashDamage = "¤Splash Damage";
	public static CharSequence ¤¤ReloadTime= "¤Reload Time";
	public static CharSequence ¤¤Trespassing = "¤Trespassing Armies.";
	public static CharSequence ¤¤TrespassingD = "¤Trespassing enemy armies in your realm.";
	
	public static CharSequence ¤¤Power = "¤Power";
	public static CharSequence ¤¤PowerD = "¤Power of a unit is a mix of all its stats.";
	
	public static CharSequence ¤¤Fort = "¤Fortifications";
	public static CharSequence ¤¤FortD = "¤Fortifications gives a huge boost to the defence of the settlement. This boost will decrease with time until the settlement falls completely.";
	
	public static CharSequence ¤¤Unitinfo = "Click and drag a unit to rearrange it in the army. Hold {0} to select several units. Hold {1} and click to toggle units.";
	
	static {
		D.ts(DicArmy.class);
	}
	
}
