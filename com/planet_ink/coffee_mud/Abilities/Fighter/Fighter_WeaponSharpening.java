package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/* 
   Copyright 2000-2014 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

@SuppressWarnings("rawtypes")
public class Fighter_WeaponSharpening extends FighterSkill
{
	public String ID() { return "Fighter_WeaponSharpening"; }
	public String name(){ return "Weapon Sharpening";}
	private static final String[] triggerStrings = {"WEAPONSHARPENING","SHARPEN"};
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}
	public int maxRange(){return adjustedMaxInvokerRange(0);}
	public int classificationCode(){ return Ability.ACODE_SKILL|Ability.DOMAIN_WEAPON_USE;}
	public int usageType(){return USAGE_MANA;}
	private int damageBonus = 1;
	
	public void executeMsg(Environmental host, CMMsg msg)
	{
		super.executeMsg(host,msg);
		if(affected instanceof Item)
		{
			if(((Item)affected).subjectToWearAndTear()&&(((Item)affected).usesRemaining()<95))
				unInvoke();
		}
	}

	public void setMiscText(String newMiscText) 
	{
		super.setMiscText(newMiscText);
		if(newMiscText.length()>0)
			damageBonus=CMath.s_int(newMiscText);
	}
	
	public void unInvoke()
	{
		if((affected instanceof Item)
		&&(!((Item)affected).amDestroyed())
		&&(((Item)affected).owner() instanceof MOB))
		{
			MOB M=(MOB)((Item)affected).owner();
			if((!M.amDead())&&(CMLib.flags().isInTheGame(M,true))&&(!((Item)affected).amWearingAt(Wearable.IN_INVENTORY)))
				M.tell(M,affected,null,_("<T-NAME> no longer seem(s) quite as sharp."));
		}
		super.unInvoke();
	}
	
	public void affectPhyStats(Physical affected, PhyStats stats)
	{
		if((affected instanceof Item)&&(damageBonus>0)&&(((Item)affected).owner() instanceof MOB))
		{
			stats.setDamage(stats.damage()+damageBonus);
			stats.addAmbiance("^w*^?");
		}
	}
	
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(mob.isInCombat())
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}
	
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Item weapon=super.getTarget(mob,null,givenTarget,null,commands,Wearable.FILTER_WORNONLY);
		if(weapon==null) return false;
		if(!(weapon instanceof Weapon))
		{
			mob.tell(mob,weapon,null,"<T-NAME> is not a weapon.");
			return false;
		}
		boolean isSharpenable;
		switch(((Weapon)weapon).weaponClassification())
		{
		case Weapon.CLASS_AXE:
		case Weapon.CLASS_DAGGER:
		case Weapon.CLASS_EDGED:
		case Weapon.CLASS_SWORD:
		case Weapon.CLASS_POLEARM:
			isSharpenable=true;
			break;
		case Weapon.CLASS_FLAILED:
		case Weapon.CLASS_RANGED:
		case Weapon.CLASS_THROWN:
			switch(((Weapon)weapon).weaponType())
			{
			case Weapon.TYPE_PIERCING:
			case Weapon.TYPE_SLASHING:
				isSharpenable=true;
				break;
			default:
				isSharpenable=false;
				break;
			}
			break;
		default:
			isSharpenable=false;
			break;
		}
		if(!isSharpenable)
		{
			mob.tell(mob,weapon,null,"<T-NAME> can not be sharpened with this skill.");
			return false;
		}
		if((weapon.subjectToWearAndTear())&&(weapon.usesRemaining()<95))
		{
			mob.tell(mob,weapon,null,"<T-NAME> needs repairing first.");
			return false;
		}
		if((!auto)&&(mob.isInCombat()))
		{
			mob.tell("You are a bit too busy to do that right now.");
			return false;
		}
		int bonus=(int)Math.round(CMath.mul(0.10+(0.10*getXLEVELLevel(mob)),weapon.phyStats().damage()));
		if(bonus<1)
		{
			mob.tell(mob,weapon,null,"<T-NAME> is too weak of a weapon to provide any more benefit from sharpening.");
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			String str=auto?"<T-NAME> looks sharper!":"<S-NAME> sharpen(s) <T-NAMESELF>.";
			CMMsg msg=CMClass.getMsg(mob,weapon,this,CMMsg.MSG_NOISYMOVEMENT,str);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,weapon,asLevel,0);
				Ability A=weapon.fetchEffect(ID());
				if(A!=null){ A.setMiscText(""+bonus); A.makeLongLasting();}
				weapon.recoverPhyStats();
				mob.location().recoverRoomStats();
			}
		}
		else
			return beneficialVisualFizzle(mob,weapon,"<S-NAME> attempt(s) to tweak <T-NAME>, but just can't get it quite right.");
		return success;
	}

}