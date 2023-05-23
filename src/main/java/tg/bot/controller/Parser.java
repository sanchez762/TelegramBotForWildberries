package tg.bot.controller;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.Objects;

public class Parser {
    public static String parse(String str) throws IOException, ParseException {
        boolean isExist = false;
        String template = "https://card.wb.ru/cards/detail?&dest=-1257786&nm=";
        String idOfItem = str.substring(str.indexOf("catalog/")+8, str.indexOf("/detail"));
        Long idOfSize;
        try {
            idOfSize = Long.valueOf(str.substring(str.indexOf("size=")+5));
        } catch (NumberFormatException e) {
            idOfSize = null;
        }
        Connection.Response response= Jsoup.connect(template+idOfItem)
                .ignoreContentType(true)
                .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                .referrer("https://www.google.com")
                .timeout(12000)
                .followRedirects(true)
                .execute();
        String res = response.body();
        JSONParser parser = new JSONParser();
        JSONObject jsonResponse = (JSONObject) parser.parse(res);
        JSONObject data = (JSONObject) jsonResponse.get("data");
        JSONArray products = (JSONArray) data.get("products");
        JSONObject products2 = (JSONObject) products.get(0);
        JSONArray sizes = (JSONArray) products2.get("sizes");
        String name = (String) products2.get("name");
        Long price = (Long) products2.get("salePriceU");

        for (Object o : sizes) {
            JSONObject size = (JSONObject) o;
            if (sizes.size() == 1) {
                JSONArray stocks = (JSONArray) size.get("stocks");
                if (!stocks.isEmpty()) {
                    isExist = true;
                }
                break;
            }
            if (Objects.equals(size.get("optionId"), idOfSize)) {
                JSONArray stocks = (JSONArray) size.get("stocks");
                if (!stocks.isEmpty()) {
                    isExist = true;
                }
                break;
            }
        }
        String result;
        if (isExist) {
            result = "Товар: " + name + "\nЕсть в наличии, цена: " + price/100;
        } else result = "Товар: " + name + "\nОтсутствует в продаже";

        return result;
    }
}
