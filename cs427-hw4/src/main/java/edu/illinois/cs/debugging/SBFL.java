package edu.illinois.cs.debugging;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

/**
 * Implementation for Spectrum-based Fault Localization (SBFL).
 *
 */
public class SBFL
{

	/**
	 * Use Jsoup to parse the coverage file in the XML format.
	 * 
	 * @param file
	 * @return a map from each test to the set of lines that it covers
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	protected static Map<String, Set<String>> readXMLCov(File file)
			throws FileNotFoundException, IOException {
		FileInputStream fileInputStream = new FileInputStream(file);

		Map<String, Set<String>> res = new HashMap<String, Set<String>>();
		Document document = Jsoup.parse(fileInputStream, "UTF-8", "",
				Parser.xmlParser());

		Elements tests = document.select("test");
		for (Element test : tests) {
			Element name = test.child(0);
			Element covLines = test.child(1);

			Set<String> lines = new HashSet<String>();
			String[] items = covLines.text().split(", ");
			Collections.addAll(lines, items);
			res.put(name.text(), lines);
		}
		return res;
	}

	/**
	 * Compute the suspiciousness values for all covered statements based on
	 * Ochiai
	 * 
	 * @param cov
	 * @param failedTests
	 * @return a map from each line to its suspiciousness value
	 */
	public static Map<String, Double> Ochiai(Map<String, Set<String>> cov,
			Set<String> failedTests) {
		// using LinkedHashMap so that the statement list can be ranked
		Map<String, Double> susp = new LinkedHashMap<String, Double>();
		// TODO

		int numfail = failedTests.size();
		Map<String, Set<String>> mapped = new HashMap<>();

		for (Map.Entry<String, Set<String>> keys: cov.entrySet())
		{
			String str = keys.getKey();
			Set<String> test = keys.getValue();

			for (String temp: test)
			{
				if (mapped.containsKey(temp)!=true)
				{
					mapped.put(temp, new HashSet<>());
				}
				mapped.get(temp).add(str);
			}
		}

		for (Map.Entry<String, Set<String>> pairs: mapped.entrySet())
		{

			String pos = pairs.getKey();
			Set<String> tests = pairs.getValue();

			int fail = 0;

			for (String test: tests)
			{
				for (String failed: failedTests)
				{
					if (test.equals(failed)==true)
					{
						fail++;
					}
				}
			}
			int s = tests.size();
			double temp = Math.sqrt(numfail * s);
			double ochval = fail / temp;
			susp.put(pos, ochval);
		}

		return susp;
	}

	/**
	 * Get the suspiciousness value for the buggy line from the suspicious
	 * statement list
	 * 
	 * @param susp
	 * @param buggyLine
	 * @return the suspiciousness value for the buggy line
	 */
	protected static double getSusp(Map<String, Double> susp,
			String buggyLine) {
		// TODO
		double temp = susp.get(buggyLine);
		return temp;
	}

	/**
	 * Rank all statements based on the descending order of their suspiciousness
	 * values. Get the rank (print the lowest rank for the tied cases) for the
	 * buggy line from the suspicious statement list
	 * 
	 * @param susp
	 * @param buggyLine
	 * @return the rank of the buggy line
	 */
	protected static int getRank(Map<String, Double> susp, String buggyLine) {
		List<Map.Entry<String, Double>> list = new LinkedList<Map.Entry<String, Double>>(
				susp.entrySet());
		// TODO

		Collections.sort(list, new Comparator<Map.Entry<String, Double>>() 
		{
			public int compare(Map.Entry<String, Double> a, Map.Entry<String, Double> b)
			{
				return (b.getValue().compareTo(a.getValue())); 
			}
		});

		HashMap<String, Double> sort = new LinkedHashMap<String, Double>();

		for (Map.Entry<String, Double> temp: list)
		{
			sort.put(temp.getKey(), temp.getValue());
		}

		list = new LinkedList<Map.Entry<String, Double>>(sort.entrySet());

		double bugs = getSusp(susp, buggyLine);

		int temp = 0;

		for (int i = 0; i < list.size() - 1; i++)
		{
			while (list.get(i).getValue() >= bugs)
			{
				temp = temp+1;
				i=i+1;
			}
		}

		return temp;
	}

}
