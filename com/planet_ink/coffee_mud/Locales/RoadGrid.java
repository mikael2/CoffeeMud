package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class RoadGrid extends StdGrid
{
	public RoadGrid()
	{
		super();
		name="a road";
		baseEnvStats.setWeight(1);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_PLAINS;
		domainCondition=Room.CONDITION_NORMAL;
	}
	public Environmental newInstance()
	{
		return new RoadGrid();
	}
	public String getChildLocaleID(){return "Road";}
	public Vector resourceChoices(){return Road.roomResources;}
}
