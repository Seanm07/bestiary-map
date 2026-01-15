package com.bestiarymap;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class BestiaryMapPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(BestiaryMapPlugin.class);
		RuneLite.main(args);
	}
}