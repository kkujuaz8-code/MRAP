package com.marketrisk.service.portfolio;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RankingService {

    private static final int RANKING_LIMIT = 20;

    @Value("${alphavantage.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private List<Map<String, Object>> fetchAlphaVantageRanking(String categoryKey) {
        String url = "https://www.alphavantage.co/query"
                + "?function=TOP_GAINERS_LOSERS"
                + "&apikey=" + apiKey;

        final String response;

        try {
            response = restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            log.error("Alpha Vantage 랭킹 API 통신 실패", e);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "주식 랭킹 서버와 통신하지 못했습니다.",
                    e
            );
        }

        try {
            JsonNode rootNode = objectMapper.readTree(response);
            validateAlphaVantageResponse(rootNode);

            JsonNode targetArray = rootNode.path(categoryKey);
            if (!targetArray.isArray()) {
                log.error("Alpha Vantage 응답에 {} 배열이 없습니다. response={}", categoryKey, response);
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "주식 랭킹 데이터 형식이 올바르지 않습니다."
                );
            }

            List<Map<String, Object>> resultList = new ArrayList<>();

            for (JsonNode node : targetArray) {
                String ticker = node.path("ticker").asText(null);
                String priceText = node.path("price").asText(null);
                String changePercent = node.path("change_percentage").asText(null);

                if (ticker == null || priceText == null || changePercent == null) {
                    log.warn("필수 필드가 없는 랭킹 항목을 건너뜁니다. item={}", node);
                    continue;
                }

                try {
                    double price = Double.parseDouble(priceText);
                    double rate = parsePercentage(changePercent);

                    Map<String, Object> item = new HashMap<>();
                    item.put("ticker", ticker);
                    item.put("name", ticker);
                    item.put("price", price);
                    item.put("rate", rate);
                    resultList.add(item);
                } catch (NumberFormatException e) {
                    log.warn("가격 또는 등락률 변환 실패로 항목을 건너뜁니다. item={}", node);
                }

                if (resultList.size() >= RANKING_LIMIT) {
                    break;
                }
            }

            return resultList;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Alpha Vantage 랭킹 JSON 처리 실패. response={}", response, e);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "주식 랭킹 응답을 처리하지 못했습니다.",
                    e
            );
        }
    }

    private void validateAlphaVantageResponse(JsonNode rootNode) {
        if (rootNode.has("Note")) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Alpha Vantage API 호출 제한에 도달했습니다. 잠시 후 다시 시도해주세요."
            );
        }

        if (rootNode.has("Information")) {
            log.error("Alpha Vantage API 오류: {}", rootNode.path("Information").asText());
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Alpha Vantage API가 요청을 거부했습니다."
            );
        }

        if (rootNode.has("Error Message")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Alpha Vantage API 요청이 올바르지 않습니다."
            );
        }
    }

    private double parsePercentage(String value) {
        return Double.parseDouble(
                value.replace("%", "")
                        .replace("+", "")
                        .trim()
        );
    }

    public List<Map<String, Object>> getRealActiveRanking() {
        return fetchAlphaVantageRanking("most_actively_traded");
    }

    public List<Map<String, Object>> getRealRiseRanking() {
        return fetchAlphaVantageRanking("top_gainers");
    }

    public List<Map<String, Object>> getRealFallRanking() {
        return fetchAlphaVantageRanking("top_losers");
    }
}
