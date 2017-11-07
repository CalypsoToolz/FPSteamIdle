package ru.calypso.steam;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Consts;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.codedisaster.steamworks.SteamAPIWarningMessageHook;
import com.codedisaster.steamworks.SteamUtilsCallback;

/**
 * 
 * @author CalypsoToolz
 *
 */

public class IdleManager {

	public static List<IdleGame> games = new ArrayList<>();
	private static final Logger _log = Logger.getLogger(IdleManager.class);
	@SuppressWarnings("deprecation")
	protected static DefaultHttpClient httpClient = new DefaultHttpClient();
	private static String myProfileURL;

	@SuppressWarnings("deprecation")
	public static void addCookie(Cookie cookie)
    {
        httpClient.getCookieStore().addCookie(cookie);
    }
	
	public static void addCookie(String name, String value, boolean secure)
    {
        BasicClientCookie cookie = new BasicClientCookie(name, value);
        cookie.setVersion(0);
        cookie.setDomain("steamcommunity.com");
        cookie.setPath("/");
        cookie.setSecure(secure);
        addCookie(cookie);
    }
	
	private static void removeFile() {
		try {
			new File("steam_appid.txt").delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void check()
	{
		games.clear();
		HttpResponse resp = null;
		try {
			resp = request(myProfileURL + "/badges/", "GET", null, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		java.util.Scanner s = null;
		try {
			s = new java.util.Scanner(resp.getEntity().getContent(), "UTF-8").useDelimiter("\\A");
		} catch (UnsupportedOperationException | IOException e) {
			e.printStackTrace();
		}
        String respText = s.hasNext() ? s.next() : "";
        s.close();
        //toFile(respText, "response.txt");

		Document doc = Jsoup.parse(respText);
		Elements userinfo = doc.getElementsByAttributeValueStarting("class", "user_avatar");
		if(userinfo.size() == 0)
		{
			_log.info("Invalid auth data! Exit...");
			System.exit(0);
		}
		
		Elements badgesMap = doc.getElementsByAttributeValue("class", "badge_title_stats");
		Elements badgesPages = doc.getElementsByAttributeValue("class", "pagelink");
		int pagesMax = 0;
		if(badgesPages.size() > 0)
		{
			pagesMax = Integer.parseInt(badgesPages.get(badgesPages.size() - 1).text());
			_log.info("TODO: " + pagesMax + " badge pages found. This is NOT DONE!");
		}
		
		if(badgesMap.size() == 0)
		{
			_log.info("No badges, exit...");
			System.exit(0);
		}
		
		int needIdle = 0;
		for(Element badge : badgesMap)
		{
			int toDrop = 0;
			try {
				String t = badge.getElementsByAttributeValue("class", "progress_info_bold").get(0).text();
				toDrop = Integer.parseInt(t.substring(t.length() - 1));
			}
			catch (Exception e) {}
			
			if(toDrop == 0)
				continue;
			String tmp = badge.getElementsByAttributeValue("class", "btn_green_white_innerfade btn_small_thin").outerHtml().split("//run/")[1];
			int appId = Integer.parseInt(tmp.substring(0, tmp.indexOf("\">")));
			tmp = badge.getElementsByAttributeValue("class", "whiteLink how_to_get_card_drops").outerHtml();
			tmp = tmp.substring(tmp.indexOf("( &quot;") + 8, tmp.indexOf("&quot;,"));
			games.add(new IdleGame(tmp, appId, toDrop));
			needIdle += toDrop;
		}
		if(games.size() == 0)
		{
			_log.info("Games is over! Done... exit");
			System.exit(0);
		}
		_log.info("We can get " + needIdle + " card(s) from " + games.size() + " game(s)");
		
		games.get(0).run();
	}
	
	public static void main(String... args)
	{
		Config.load();
		removeFile();
		myProfileURL = "http://steamcommunity.com/profiles/" + Config.LOGIN.substring(0, 17);
		addCookie("sessionid", Config.SESSION_ID, false);
		addCookie("steamLogin", Config.LOGIN, false);
		addCookie("steamparental", "", false);
		check();
	}
	
	public static SteamUtilsCallback clUtilsCallback = new SteamUtilsCallback() {
		
		@Override
		public void onSteamShutdown() {
			System.err.println("Steam client requested to shut down!");
		}
	};
	
	public static SteamAPIWarningMessageHook clMessageHook = new SteamAPIWarningMessageHook() {
		@Override
		public void onWarningMessage(int severity, String message) {
			System.err.println("[client debug message] (" + severity + ") " + message);
		}
	};
	
	public static HttpResponse request(String url, String method, List<NameValuePair> data, boolean ajax) throws IOException
    {
		_log.info("Sending " + method + " request to " + url);
        HttpRequest request;
        if(method.equals("POST"))
        {
            request = new HttpPost(url);
        } else //(method.equals("POST"))
        {
            request = new HttpGet(url);
        }

        request.setHeader("Accept", "text/javascript, text/html, application/xml, text/xml, */*");
        request.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        request.setHeader("Host", "steamcommunity.com");

        if (ajax)
        {
            request.setHeader("X-Requested-With", "XMLHttpRequest");
            request.setHeader("X-Prototype-Version", "1.7");
        }

        if(data != null && !method.equals("GET"))
        {
            ((HttpPost)request).setEntity(new UrlEncodedFormEntity(data, Consts.UTF_8));
        }
        return httpClient.execute((HttpUriRequest)request);
    }
}
