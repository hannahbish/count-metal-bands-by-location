import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;


public class MetalBandCounter {

	private int count;
	private int totalRecords;
	private HashMap<String, Integer> map;

	public void run(String urlPartOne, String urlPartTwo) {
		// Init the count of the number of bands and total records
		this.count = 0;
		this.totalRecords = 0;

		// Init HashMap to contain the states and number of bands per state
		this.map = new HashMap<String, Integer>();

		// Call first page of api to get total records
		try {
			httpGet(urlPartOne + 0 + urlPartTwo);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Round up to the nearest 500th record (the GET call paginates every 500 bands)
		this.totalRecords = this.totalRecords + (500-(this.totalRecords%500));

		// Iterate through all pages  
		for (int i = 500; i < this.totalRecords; i = i + 500) {
			try {
				httpGet(urlPartOne + Integer.toString(i) + urlPartTwo);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void putInMap(String state) {
		this.count++;
		// If state already exists in map, increment
		if (this.map.containsKey(state)) {
			this.map.put(state, this.map.get(state) + 1);
		} else {
			this.map.put(state, 1);
		}
	}

	public void httpGet(String urlStr) throws IOException {
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		if (conn.getResponseCode() != 200) {
			throw new IOException(conn.getResponseMessage());
		}

		// Buffer the result into a string
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

		// Parse the result
		read(rd);

		conn.disconnect();
	}

	public void read(BufferedReader reader) {
		String currentLine;
		try {
			while((currentLine = reader.readLine()) != null) {

				// If this is the first call, get the total number of records
				if (this.count == 0 && currentLine.indexOf("iTotalRecords") > 0) {
					this.totalRecords = Integer.parseInt(currentLine.substring(currentLine.indexOf("iTotalRecords") + 15, currentLine.length()-1).trim());
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
								putInMap(fSlashParts[i].trim());
							}
						}
					} 
					// Split by back slash and take all values
					else if (state.indexOf("\\") > 0) {
						String[] bSlashParts = state.split("\\");
						for (int i = 0; i < bSlashParts.length; i++) {
							if (state != null && !"".equals(state)) {
								putInMap(bSlashParts[i].trim());
							}
						}
					} 
					// No splitting required
					else if (state != null && !"".equals(state)){
						putInMap(state);
					}
				};
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public HashMap<String, Integer> getMap() {
		return this.map;
	}

	public void setMap(HashMap<String, Integer> map) {
		this.map = map;
	}

	public int getCount() {
		return this.count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getTotalRecords() {
		return this.totalRecords;
	}

	public void setTotalRecords(int totalRecords) {
		this.totalRecords = totalRecords;
	}
}
