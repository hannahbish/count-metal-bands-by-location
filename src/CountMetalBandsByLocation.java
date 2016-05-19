import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;


public class CountMetalBandsByLocation {

	private static int count;
	private static int totalRecords;

	public static void main(String[] args) throws IOException, URISyntaxException {

		// Get time when program starts running
		long startTime = System.currentTimeMillis();

		// URL parts of metal-archives api GET call
		String urlPartOne = "http://www.metal-archives.com/browse/ajax-country/c/US/json/1?sEcho=1&iColumns=4&sColumns=&iDisplayStart=";
		String urlPartTwo = "&iDisplayLength=500&mDataProp_0=0&mDataProp_1=1&mDataProp_2=2&mDataProp_3=3&iSortCol_0=0&sSortDir_0=asc&iSortingCols=1&bSortable_0=true&bSortable_1=true&bSortable_2=true&bSortable_3=false&_=1463503972375";

		// Init the count of the number of bands and total records
		count = 0;
		totalRecords = 0;

		// HashMap to contain the states and number of bands per state
		HashMap<String, Integer> map = new HashMap<String, Integer>();

		// Call first page of api to get total records
		httpGet(map, urlPartOne + 0 + urlPartTwo);

		// Round up to the nearest 500th record (the GET call paginates every 500 bands)
		totalRecords = totalRecords + (500-(totalRecords%500));

		// Iterate through all pages  
		for (int i = 500; i < totalRecords; i = i + 500) {
			httpGet(map, urlPartOne + Integer.toString(i) + urlPartTwo);
		}

		// Print the map, total number of bands, and time taken to run
		System.out.println(map);
		System.out.println("Number of bands inserted into map: " + count);
		long endTime   = System.currentTimeMillis();
		long totalTime = (endTime - startTime) / 1000;
		System.out.println("Total time to run: " + totalTime + " seconds");
	}

	public static void putInMap(HashMap<String, Integer> map, String state) {
		count++;
		// If state already exists in map, increment
		if (map.containsKey(state)) {
			map.put(state, map.get(state) + 1);
		} else {
			map.put(state, 1);
		}
	}

	public static void httpGet(HashMap<String, Integer> map, String urlStr) throws IOException {
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		if (conn.getResponseCode() != 200) {
			throw new IOException(conn.getResponseMessage());
		}

		// Buffer the result into a string
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

		// Parse the result
		read(map, rd);

		conn.disconnect();
	}

	public static void read(HashMap<String,Integer> map, BufferedReader reader) {
		String currentLine;
		try {
			while((currentLine = reader.readLine()) != null) {

				// If this is the first call, get the total number of records
				if (count == 0 && currentLine.indexOf("iTotalRecords") > 0) {
					totalRecords = Integer.parseInt(currentLine.substring(currentLine.indexOf("iTotalRecords") + 15, currentLine.length()-1).trim());
				}

				// The band's location is listed two lines below its link
				if (currentLine.indexOf("href") > 0) {
					
					reader.readLine();
					String state = reader.readLine();
					
					// Remove trailing whitespace and quotation marks
					state = state.trim();
					state = state.substring(state.indexOf('"')+1, state.lastIndexOf('"'));

					// Split by comma and only take final value
					if (state.indexOf(",") > 0) {
						String[] commaParts = state.split(",");
						state = commaParts[commaParts.length - 1].trim();
					} 

					// Split by semicolon and only take final value
					if (state.indexOf(";") > 0) {
						String[] semiColonParts = state.split(";");
						state = semiColonParts[semiColonParts.length - 1].trim();
					} 

					// Remove any endings like '(now)' or '(later)'
					if (state.indexOf("(") > 0) {
						state = state.substring(0, state.indexOf("(")).trim();
					}
					
					// Split by forward slash and take all values
					if (state.indexOf("/") > 0) {
						String[] fSlashParts = state.split("/");
						for (int i = 0; i < fSlashParts.length; i++) {
							if (state != null && !"".equals(state)) {
								putInMap(map, fSlashParts[i].trim());
							}
						}
					} 
					// Split by back slash and take all values
					else if (state.indexOf("\\") > 0) {
						String[] bSlashParts = state.split("\\");
						for (int i = 0; i < bSlashParts.length; i++) {
							if (state != null && !"".equals(state)) {
								putInMap(map, bSlashParts[i].trim());
							}
						}
					} 
					// No splitting required
					else if (state != null && !"".equals(state)){
						putInMap(map, state);
					}
				};
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
