package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class Deer extends StdMOB
{

	public Deer()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());

		Username="a deer";
		setDescription("A nervous, but beautifully graceful creation.");
		setDisplayText("A deer looks up as you happen along.");
		setAlignment(500);
		setMoney(0);
		setWimpHitPoint(0);

		baseEnvStats().setDamage(1);
		baseEnvStats().setSpeed(2.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(1);
		baseEnvStats().setArmor(50);
		baseCharStats().setMyRace(CMClass.getRace("Deer"));
		baseCharStats().getMyRace().startRacing(this,false);

		baseState.setHitPoints((Math.abs(randomizer.nextInt() % 4)*baseEnvStats().level()) + 1);

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new Deer();
	}
}