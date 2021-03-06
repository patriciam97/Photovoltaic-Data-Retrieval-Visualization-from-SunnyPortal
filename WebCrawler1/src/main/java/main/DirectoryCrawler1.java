package main;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

/**
 * 
 * @author Patricia M.
 *
 */
 public class DirectoryCrawler1 implements Callable<DirectoryCrawler1> {
 	private String url;
 	private String Conn;
 	private int maxPgs,fromPg,toPg;
 	private ArrayList<String> urls= new ArrayList<String>();
 	private ArrayList<String> fullurls = new ArrayList<String>();
 	private ArrayList<String> powerList = new ArrayList<String>();
	private ArrayList<String> cityList = new ArrayList<String>();
	private ArrayList<String> countryList = new ArrayList<String>();
	private ArrayList<String> zipcodeList = new ArrayList<String>();
	private ArrayList<String> systemList = new ArrayList<String>();
	private ArrayList<String>  coordinates;
 	private static int counter=0;
 	private static int ids;
 	private int id;
 	private static Document doc;
 	private String threadName;
 	

 	/**
 	 * 
 	 * @param url the url of the website
 	 */
 	public DirectoryCrawler1(String url) {
 		this.url = url;
 		this.id = ids++;
 	}
 	/**
 	 * this constructor is used for multi-threading
 	 * @param threadname  	name of thread
 	 * @param Conn			database connection
 	 * @param url 			url of website
 	 * @param toPg			to page
 	 * @param frompg		from page
 	 * @throws IOException
 	 */
 	public DirectoryCrawler1(String threadname,String Conn,String url,int toPg,int frompg) throws IOException {
 		this.url = url;
 		this.Conn=Conn;
 		this.toPg=toPg;
 		this.fromPg=frompg;
 		this.threadName=threadname;
 		this.id = ids++;

 	}

 	/**
 	 * 
 	 * @return an integer of the maximum number of pages of the directory
 	 * @throws IOException
 	 */
 	
	public int getMaximumPages() throws IOException {
		
		int pg = 0; //initialise variable to 0.
		doc= Jsoup.connect(url).timeout(20000000).get(); //connect to the website
		Element table = doc.getElementById("ctl00_ContentPlaceHolder1__dataGridPagerDown_PagerTable");
		Elements pages = table.select("tr td a");
		// the last element is the total number of the pages
		pg = Integer.parseInt(pages.last().text());
		System.out.println("Total num of pages: "+pg);
		return pg+1;
	}

	/**
	 * extracts the full urls of all systems from page fromPg to page toPg
	 */
 	public void GetUrls() {
 		for (int i = fromPg; i < toPg; i++) {
 			counter=0;
 			ConnectToEachPage(i);
 			ExtractUrls();
 			System.out.println("["+id+"] "+"Number of urls extraced from page "+ (i+1)+": "+counter);
 		}
 		ManipulateUrls(); // extracts only the path to each PV System Profile
 	}

 	/**
 	 * 
 	 * @param pg connects to that page
 	 */
 	private void ConnectToEachPage(int pg) {
 		String link = url + "PageIndex=" + pg;
 		System.out.println("Crawling page: " + (pg+1));
 		try {
				//connects to the link
			doc = Jsoup.connect(link).timeout(20000000).get();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}

	/**
	 *  this function extracts the full url for each PV System Profile
	 */
 	private void ExtractUrls() {
 		String system,country,city,zipcode,power;
 		Elements reportContent;		
 		// Select all elements with an href tag
 		reportContent = doc.select("table[class=base-grid] tr[class^= base-grid-item]"); 
 			for (Element el : reportContent) {
 				String link=el.select("td a[href]").get(0).attr("href").toString();
 				if(fullurls.contains(link)==false){
	 				counter++;
	 				country=el.select("td").get(2).text().toString().toUpperCase();
	 				zipcode=el.select("td").get(3).text().toString();
	 				city=el.select("td").get(4).text().toString();
	 				power=el.select("td").get(5).text().toString();
	 				system=el.select("td").get(1).text().toString();
	 				powerList.add(power);
					cityList.add(city);
					countryList.add(country);
					zipcodeList.add(zipcode);
					systemList.add(system);
	 				fullurls.add(link);
	 				//System.out.printf("%s \t %s \t %s \t %s \t %s \n",system,country,city,zipcode,power);
 				}
 			}
 		this.urls = fullurls;
 	}
	/**
	 * extracts only the path from the full url.
	 */
 	private void ManipulateUrls() {
	// extracts the path for each PV System Profile
 		ArrayList<String> updatedUrls = new ArrayList<String>();
 		String substrings[] = null;
		
 		for (int i = 0; i < urls.size(); i++) {
			//for all urls available
 			if (urls.get(i).toString().contains("OpenPlant")) {
 				// splits the original url we have extracted at these characters
 				substrings = urls.get(i).split("[=&]");
 				// substring[2] corresponds to the one we want
				updatedUrls.add(substrings[1]);
 			}
 		}
		this.urls= updatedUrls;
}

/**
 * 
 * @return the power list from the Directory page
 */
	public ArrayList<String> getPowerList(){
		return powerList;
		
	}
/**
 * 
 * @return the city list from the Directory page
 */
public ArrayList<String> getCityList(){
	return cityList;
	
}
/**
 * 
 * @return the zip code list from the Directory page
 */
public ArrayList<String> getZipCodeList(){
	return zipcodeList;
	
}
public ArrayList<String> getUrls() {
	return urls;
}
/**
 * 
 * @return the country list from the Directory page
 */
public ArrayList<String> getCountryList(){
	return countryList;
		
}
	/**
	 * 
	 * @return the system list from the Directory page
	 */
	public ArrayList<String> getSystemList(){
		return systemList;
		
	}
	/**
	 * this function saves the path,system name,country,city,zipcode and power of each system along with the timestamp of the current date and time.
	 */
	public void SaveDirectory() {
		MongoClientURI uri = new MongoClientURI(Conn);
		MongoClient mongoClient = new MongoClient(uri);
		DB db = mongoClient.getDB("sunnyportal");
		Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
		mongoLogger.setLevel(Level.SEVERE);
		DateFormat formatter = new SimpleDateFormat("dd/MM/yy HH:mm");
		Date date = new Date();
		Long timestamp=date.getTime();
		DBCollection collection = db.getCollection("DirectoryCollection");
		// creating a document for the PV System
		for(int i=0;i<urls.size();i++) {
				getCoordinates(cityList.get(i),countryList.get(i));
				DBObject prof = new BasicDBObject("_id", urls.get(i))
						.append("Timestamp", timestamp)
						.append("System", systemList.get(i))
						.append("Country", countryList.get(i))
						.append("City", cityList.get(i))
						.append("ZipCode",zipcodeList.get(i))
						.append("SystemPower", powerList.get(i))
						.append("Coordinates", coordinates);
				//check if this document already exists
				DBObject exists=collection.findOne(urls.get(i));
				if (exists!=null){ 
					//if it exists
					if (exists.equals(prof)){
						//if it is the same
						System.out.println("["+id+"]"+formatter.format(date)+": "+systemList.get(i)+" is up to date.");
					}else{
						//needs to be updated
						collection.update(exists,prof);
						System.out.println("["+id+"]"+formatter.format(date)+": "+systemList.get(i)+ " updated.");
					}
				}else{
					//if it doesnt exist, insert it
					collection.insert(prof);
					System.out.println("["+id+"]"+formatter.format(date)+": "+systemList.get(i)+" saved.");
				}
		}
	}
	public void getCoordinates(String city,String country){
		String link;
		String username1="patriciam97";
		String username2="s1616316";
		String username3="patriciam1997";
		String username = username1;
		boolean retrieved=false;
		while(retrieved==false) {
			if (username.equals(username1)){
				username=username2;
			}else if (username.equals(username2)){
				username=username3;
			}else {
				username=username1;
			}
			link="http://api.geonames.org/search?q="+city+"+"+country+"&username="+username;
			try {
				if(Jsoup.connect(link).timeout(20000000).get().selectFirst("totalResultsCount").text().equals("0")){
					link="http://api.geonames.org/search?q="+country+"&username="+username;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch (NullPointerException e) {
				e.printStackTrace();
			}
			String lat = null;
			try {
				lat = Jsoup.connect(link).timeout(2000000).get().selectFirst("geonames geoname lat").text();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch (NullPointerException e) {
				e.printStackTrace();
			}
			String lng = null;
			try {
				lng = Jsoup.connect(link).timeout(2000000).get().selectFirst("geonames geoname lng").text();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch (NullPointerException e) {
				e.printStackTrace();
			}
		   try {
				if(lat.equals(null)==false && (lng).equals(null)==false) {
			
				coordinates= new ArrayList<String>();
				coordinates.add(lat);
				coordinates.add(lng);
				retrieved=true;
			}
		   }catch (NullPointerException e) {
				e.printStackTrace();
		   }
		}
	}
	/**
	 * this method is used for multi-threading 
	 */
	public DirectoryCrawler1 call() throws Exception {
		this.GetUrls();
	  	this.SaveDirectory();
	  	id--;
		return this;
	}

 }
