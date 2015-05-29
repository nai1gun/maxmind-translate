package com.korvyakov.data;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:alexander@korvyakov.ru">Korvyakov</a>
 * @since 18.05.2015
 */
public class RegionDataTranslator {

	private static final String INPUT_FILTE_NAME = "region_codes.csv";

	private static final int BUNCH_SIZE = 100;

	private static TranslateService translateService = new TranslateService();

	public static void main(String[] args) {
		List<String> fileLines;
		File file = new File("region_codes.csv");
		try {
			fileLines = FileUtils.readLines(file);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		List<RegionItem> items = new ArrayList<>(fileLines.size());
		for (String line: fileLines) {
			//String[] parts = line.split(",");
			String[] parts = line.split("(?x)" +
	             ",          " +   // Split on comma
	             "(?=        " +   // Followed by
	             "  (?:      " +   // Start a non-capture group
	             "    [^\"]* " +   // 0 or more non-quote characters
	             "    \"     " +   // 1 quote
	             "    [^\"]* " +   // 0 or more non-quote characters
	             "    \"     " +   // 1 quote
	             "  )*       " +   // 0 or more repetition of non-capture group (multiple of 2 quotes will be even)
	             "  [^\"]*   " +   // Finally 0 or more non-quotes
	             "  $        " +   // Till the end  (This is necessary, else every comma will satisfy the condition)
	             ")          "     // End look-ahead
                 );
			RegionItem item = new RegionItem();
			item.countryCode = parts[0];
			item.regionCode = parts[1];
			item.region = parts[2].replace("\"", "");
			items.add(item);
		}
		RegionItem[][] splittedItems = new RegionItem[(items.size() / BUNCH_SIZE) + 1][BUNCH_SIZE];
		{
			int i = 0;
			for (RegionItem item : items) {
				int i1 = i / BUNCH_SIZE;
				int i2 = i % BUNCH_SIZE;
				splittedItems[i1][i2] = item;
				i++;
			}
		}
		try {
			int i = 0;
			for(RegionItem[] itemsChunk: splittedItems) {
				List<String> textsToTranslate = new ArrayList<>();
				for (RegionItem item : itemsChunk) {
					if (item != null) {
						textsToTranslate.add(item.region);
					}
				}
				List<String> translated = translateService.translate(textsToTranslate);
				int j = 0;
				for (String translation : translated) {
					splittedItems[i][j++].regionTranslated = translation;
				}
				i++;
			}
			StringBuilder resultBuilder = new StringBuilder();
			items.stream().filter(item -> item != null).forEach(item -> resultBuilder.append(
					String.format("%s,%s,\"%s\",\"%s\"\n",
							item.countryCode, item.regionCode, item.region, item.regionTranslated)));
			File outputFile = new File(INPUT_FILTE_NAME.replace(".csv", String.format("_%s.csv", Config.LANGUAGE_TO_TRANSLATE)));
			FileUtils.write(outputFile, resultBuilder.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static class RegionItem {

		String countryCode;

		String regionCode;

		String region;

		String regionTranslated;

	}

}
