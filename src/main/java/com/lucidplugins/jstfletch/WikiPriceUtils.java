package com.lucidplugins.jstfletch;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class WikiPriceUtils {
    private static final String WIKI_API_URL = "https://prices.runescape.wiki/api/v1/osrs/latest";
    private static final Map<Integer, Integer> itemPriceCache = new HashMap<>();

    public static int getItemPrice(int itemId) {
        if (itemPriceCache.containsKey(itemId)) {
            return itemPriceCache.get(itemId);
        }

        try {
            URL url = new URL(WIKI_API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "LucidPlugins/JstFletch");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                JsonReader reader = new JsonReader(new InputStreamReader(connection.getInputStream()));
                JsonParser parser = new JsonParser();
                JsonObject jsonResponse = parser.parse(reader).getAsJsonObject();
                JsonArray data = jsonResponse.getAsJsonArray("data");

                for (int i = 0; i < data.size(); i++) {
                    JsonObject item = data.get(i).getAsJsonObject();
                    int id = item.get("id").getAsInt();
                    int price = item.get("overall_average").getAsInt();
                    itemPriceCache.put(id, price);
                }
            } else {
                System.err.println("Failed to fetch item prices from wiki API. Response code: " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return itemPriceCache.getOrDefault(itemId, -1);
    }
}
