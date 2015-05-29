package com.korvyakov.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:korvyakov@redhelper.ru">Korvyakov</a>
 * @since 29.05.2015
 */
public class TranslateService {

	private String apiKey = Config.YANDEX_TRANSLATE_API_KEY;

	private String languageCode = Config.LANGUAGE_TO_TRANSLATE;

	private String commonUrlPart() {
		return String.format(
			"https://translate.yandex.net/api/v1.5/tr.json/translate?key=%s&lang=en-%s",
			apiKey, languageCode);
	}

	public List<String> translate(String[] texts) throws IOException {
		List<String> ret = new ArrayList<>();
		if (texts == null || texts.length == 0) {
			return ret;
		}
		String url = commonUrlPart();
		StringBuilder sb = new StringBuilder();
		for(String text: texts) {
			sb.append("&text=").append(text != null ? text.replace(" ", "+"): "");
		}
		url += sb.toString();
		Content content = Request.Get(url).execute().returnContent();
		JsonParser parser = new JsonParser();
		JsonObject json = parser.parse(content.asString()).getAsJsonObject();
		int code = json.get("code").getAsInt();
		if (code != 200) {
			String message = json.get("message").getAsString();
			throw new RuntimeException(String.format("Got error response! Code: %s. Message: %s",
					code, message));
		}
		JsonArray translations = json.getAsJsonArray("text");
		int j = 0;
		for (JsonElement translation : translations) {
			ret.add(translation.getAsString());
		}
		return ret;
	}

	public List<String> translate(List<String> texts) throws IOException {
		if (texts == null || texts.size() == 0) {
			return new ArrayList<>();
		}
		return translate(texts.toArray(new String[texts.size()]));
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getLanguageCode() {
		return languageCode;
	}

	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}
}
