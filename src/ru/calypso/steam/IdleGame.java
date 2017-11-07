package ru.calypso.steam;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.log4j.Logger;

import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamException;

/**
 * 
 * @author CalypsoToolz
 *
 */

public class IdleGame {

	private static final Logger _log = Logger.getLogger(IdleGame.class);

	public int cardsLeft, appId;
	public String gameName;
	
	public IdleGame(String gameName, int appId, int left)
	{
		this.gameName = gameName;
		this.appId = appId;
		this.cardsLeft = left;
	}
	
	public static void main(String[] args)
	{
		_log.info("Start init Steam API...");
		try {
		    if (!SteamAPI.init("./")) {
				_log.info("Can't init Steam API, You client is runned?");
		        System.exit(0);
		    }
		} catch (SteamException e) {
		    e.printStackTrace();
		}
		try {
			_log.info("Sleeping for 5 min...");
			Thread.sleep(5 * 60000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.exit(2);
	}
	
	public void run() {
		IdleManager.games.remove(this);
		_log.info("Running game " + gameName + " (" + cardsLeft + " card(s) left)");
		makeFile();
		ProcessBuilder pb = new ProcessBuilder("java", "-cp", "FPSteamIdle.jar", "ru.calypso.steam.IdleGame",
				"" + appId);
		pb.redirectErrorStream(true);
		try {
			Process proc = pb.start();
			try {
				InputStream stdout = proc.getInputStream();
				InputStreamReader isrStdout = new InputStreamReader(stdout);
				BufferedReader brStdout = new BufferedReader(isrStdout);

				String line = null;
				while ((line = brStdout.readLine()) != null) {
					_log.info(line);
				}

				// int exitVal =
				proc.waitFor();
				IdleManager.check();
				/*
				if (IdleManager.games.size() > 0)
					IdleManager.games.get(0).run();
				else {
					_log.infon("Games is over! Done... exit");
					removeFile();
					System.exit(0);
				}
				*/
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void makeFile()
	{
		Writer writer = null;

		try {
		    writer = new BufferedWriter(new OutputStreamWriter(
		          new FileOutputStream("steam_appid.txt"), "utf-8"));
		    writer.write(""+appId);
		} catch (IOException ex) {
		} finally {
		   try {writer.close();} catch (Exception ex) {}
		}
	}
}
