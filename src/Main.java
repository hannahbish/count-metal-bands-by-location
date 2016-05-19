
public class Main {

	public static void main(String[] args) {

		// Get time when program starts running
		long startTime = System.currentTimeMillis();

		// URL parts of metal-archives api GET call
		String urlPartOne = "http://www.metal-archives.com/browse/ajax-country/c/US/json/1?sEcho=1&iColumns=4&sColumns=&iDisplayStart=";
		String urlPartTwo = "&iDisplayLength=500&mDataProp_0=0&mDataProp_1=1&mDataProp_2=2&mDataProp_3=3&iSortCol_0=0&sSortDir_0=asc&iSortingCols=1&bSortable_0=true&bSortable_1=true&bSortable_2=true&bSortable_3=false&_=1463503972375";
		
		// Run the counter
		MetalBandCounter counter = new MetalBandCounter();
		counter.run(urlPartOne, urlPartTwo);
		
		// Print the map, total number of bands, and time taken to run
		System.out.println(counter.getMap());
		System.out.println("Number of bands inserted into map: " + counter.getCount());
		long endTime   = System.currentTimeMillis();
		long totalTime = (endTime - startTime) / 1000;
		System.out.println("Total time to run: " + totalTime + " seconds");
	}

}
