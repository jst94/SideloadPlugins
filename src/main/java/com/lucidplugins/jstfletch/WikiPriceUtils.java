package com.lucidplugins.jstfletch;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import net.runelite.client.RuneLite;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WikiPriceUtils {
    private static final String WIKI_API_URL = "https://prices.runescape.wiki/api/v1/osrs/latest";
    private static final Map<Integer, CachedPrice> itemPriceCache = new HashMap<>();
    private static final long CACHE_DURATION = TimeUnit.MINUTES.toMillis(5); // Cache prices for 5 minutes
    private static final OkHttpClient HTTP_CLIENT = RuneLite.getInjector().getInstance(OkHttpClient.class);

    private static class CachedPrice {
        final int price;
        final long timestamp;

        CachedPrice(int price) {
            this.price = price;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_DURATION;
        }
    }

    public static int getItemPrice(int itemId) {
        // Check cache first
        CachedPrice cachedPrice = itemPriceCache.get(itemId);
        if (cachedPrice != null && !cachedPrice.isExpired()) {
            return cachedPrice.price;
        }

        try {
            Request request = new Request.Builder()
                    .url(WIKI_API_URL)
                    .header("User-Agent", "LucidPlugins/JstFletch")
                    .build();

            try (Response response = HTTP_CLIENT.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected response code: " + response);
                }

                String responseBody = response.body().string();
                JsonReader reader = new JsonReader(new StringReader(responseBody));
                JsonParser parser = new JsonParser();
                JsonObject jsonResponse = parser.parse(reader).getAsJsonObject();
                JsonObject data = jsonResponse.getAsJsonObject("data");

                if (data.has(String.valueOf(itemId))) {
                    JsonObject item = data.getAsJsonObject(String.valueOf(itemId));
                    int price = item.get("high").getAsInt();
                    itemPriceCache.put(itemId, new CachedPrice(price));
                    return price;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Return cached price even if expired, or -1 if no cache exists
        return cachedPrice != null ? cachedPrice.price : -1;
    }

    public static void clearCache() {
        itemPriceCache.clear();
    }
}
