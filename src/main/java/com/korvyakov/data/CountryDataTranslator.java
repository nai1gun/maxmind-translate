package com.korvyakov.data;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:alexander@korvyakov.ru">Korvyakov</a>
 * @since 18.05.2015
 */
public class CountryDataTranslator {

	private static final String INPUT_FILTE_NAME = "iso3166.csv";

	private static final int BUNCH_SIZE = 100;

	private static TranslateService translateService = new TranslateService();

	public static void main(String[] args) {
		List<String> fileLines;
		File file = new File(INPUT_FILTE_NAME);
		try {
			fileLines = FileUtils.readLines(file);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		List<CountryItem> items = new ArrayList<>(fileLines.size());
		for (String line: fileLines) {
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
			CountryItem item = new CountryItem();
			item.countryCode = parts[0];
			item.countryName = parts[1].replace("\"", "");
			items.add(item);
		}
		CountryItem[][] splittedItems = new CountryItem[(items.size() / BUNCH_SIZE) + 1][BUNCH_SIZE];
		{
			int i = 0;
			for (CountryItem item : items) {
				int i1 = i / BUNCH_SIZE;
				int i2 = i % BUNCH_SIZE;
				splittedItems[i1][i2] = item;
				i++;
			}
		}

		try {
			int i = 0;
			for(CountryItem[] itemsChunk: splittedItems) {
				List<String> textsToTranslate = new ArrayList<>();
				for (CountryItem item : itemsChunk) {
					if (item != null) {
						textsToTranslate.add(item.countryName);
					}
				}
				List<String> translated = translateService.translate(textsToTranslate);
				int j = 0;
				for (String translation : translated) {
					splittedItems[i][j++].countryNameTranslated = translation;
				}
				i++;
			}
			StringBuilder resultBuilder = new StringBuilder();
			items.stream().filter(item -> item != null).forEach(item -> resultBuilder.append(
					String.format("%s,\"%s\",\"%s\"\n",
							item.countryCode, item.countryName, item.countryNameTranslated)));
			File outputFile = new File(INPUT_FILTE_NAME.replace(".csv", String.format("_%s.csv", Config.LANGUAGE_TO_TRANSLATE)));
			FileUtils.write(outputFile, resultBuilder.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static class CountryItem {

		String countryCode;

		String countryName;

		String countryNameTranslated;

	}

}
