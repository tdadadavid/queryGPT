package org.example;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebScanner {
    public List<SearchResult> startSearch(@NotNull String query) throws UnsupportedEncodingException {
        List<SearchResult> results = new ArrayList<>();

        String apiKey = "AIzaSyDmr2A1paeuxokxWCk-vBJHkDsSMUWlenA";
        String cx = "754a086891c634ba2";

        if (query.isEmpty()) {
            return results;
        }

        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
        String url = "https://www.googleapis.com/customsearch/v1?key=" + apiKey +"&cx="+ cx + "&q=" + encodedQuery;

        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpGet request = new HttpGet(url);

            HttpResponse response = client.execute(request);
            String jsonResponse = EntityUtils.toString(response.getEntity());

            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray itemsArray = jsonObject.getJSONArray("items");

            System.out.println("google results: " + itemsArray.length());
            for (int i = 0; i < itemsArray.length(); i++) {
                JSONObject item = itemsArray.getJSONObject(i);
                String link = item.getString("link");
                SearchResult result = processUrl(link, query, 0);
                System.out.println("full url: " + result.getFullUrl());
                System.out.println("Current (i): " + i);
                results.add(result);

            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return results;
    }

    private SearchResult processUrl(String link, String query) {
        String relevantParagraph = findRelevantParagraph(link, query);

        if (relevantParagraph != null && !relevantParagraph.isEmpty()) {
            return new SearchResult(relevantParagraph, link, calculateRelevanceScore(query, relevantParagraph));
        } else {
            return null;
        }
    }

    private SearchResult processUrl(String link, String query, int o) {

        RequestConfig config = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.STANDARD)
                .build();

        CloseableHttpClient client =HttpClients.custom()
                .setDefaultRequestConfig(config)
                .build();

        int bestScore = 1;
        String bestParagraph = "";
        HttpGet linkRequest = new HttpGet(link);

        try (CloseableHttpResponse linkResponse = client.execute(linkRequest)) {
            String htmlContent = EntityUtils.toString(linkResponse.getEntity());

            // Extract paragraphs without Jsoup
            String[] paragraphs = htmlContent.split("</p>");

            for (String paragraph : paragraphs) {
                int score = calculateBagOfWordsScore(query, paragraph);
                if (score > bestScore) {
                    bestScore = score;
                    bestParagraph = paragraph;

                    return new SearchResult(bestParagraph, link, bestScore);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    private String findRelevantParagraph(String url, String query) {
        try {
            Document doc = Jsoup.connect(url).get();
            Elements paragraphs = doc.select("p");

            String bestParagraph = "";
            int bestScore = 0;

            for (Element paragraph : paragraphs) {
                int score = calculateBagOfWordsScore(query, paragraph.text());
                if (score > bestScore) {
                    bestScore = score;
                    bestParagraph = paragraph.text();
                }
            }

            return bestParagraph;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private int calculateBagOfWordsScore(String query, String text) {
        Map<String, Integer> queryFreqMap = new HashMap<>();
        Map<String, Integer> textFreqMap = new HashMap<>();

        // Tokenize query and text
        String[] queryWords = query.split("\\s+");
        String[] textWords = text.split("\\s+");

        // Count word frequencies in query
        for (String word : queryWords) {
            queryFreqMap.put(word, queryFreqMap.getOrDefault(word, 0) + 1);
        }

        // Count word frequencies in text
        for (String word : textWords) {
            textFreqMap.put(word, textFreqMap.getOrDefault(word, 0) + 1);
        }

        // Calculate intersection score
        int score = 0;
        for (Map.Entry<String, Integer> entry : queryFreqMap.entrySet()) {
            String word = entry.getKey();
            if (textFreqMap.containsKey(word)) {
                score += entry.getValue() * textFreqMap.get(word);
            }
        }

        System.out.println("score: " + score);

        return score;
    }

    private int calculateRelevanceScore(String query, String relevantParagraph) {
        // Your implementation of relevance score calculation goes here
        // This is a simplified example, you may need to refine it based on your specific requirements
        return relevantParagraph.split("\\s+").length; // Simple score based on paragraph length
    }
}
