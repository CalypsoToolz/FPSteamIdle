package ru.calypso.steam;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

public class Config {

	private static final Logger _log = Logger.getLogger(Config.class);

	public static String LOGIN, SESSION_ID;
	
	private static void loadOlympiadSettings()
	{
		ExProperties cfg = load("config/auth.properties");
		LOGIN = cfg.getProperty("SteamLogin", "");
		SESSION_ID = cfg.getProperty("SessionID", "");
	}
	
	public static void load()
	{
		loadOlympiadSettings();
	}
	
	private static ExProperties load(String filename)
	{
		return load(new File(filename));
	}

	private static ExProperties load(File file)
	{
		ExProperties result = new ExProperties();

		try
		{
			result.load(file);
		}
		catch(IOException e)
		{
			_log.error("Error loading config : " + file.getName() + "!");
		}

		return result;
	}
}
