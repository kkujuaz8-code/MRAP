package com.marketrisk.service.stock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Slf4j
@Service
public class StockService {

    @Value("${finnhub.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public double getCurrentPriceValue(String ticker) {
        String normalizedTicker = normalizeTicker(ticker);

        try {
            String url = "https://finnhub.io/api/v1/quote"
                    + "?symbol=" + normalizedTicker
                    + "&token=" + apiKey;

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<?, ?> body = response.getBody();

            if (body == null || body.get("c") == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "해당 종목의 현재가를 찾을 수 없습니다."
                );
            }

            double currentPrice = Double.parseDouble(body.get("c").toString());

            if (currentPrice <= 0) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "해당 종목의 현재가를 찾을 수 없습니다."
                );
            }

            return currentPrice;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Finnhub 현재가 조회 실패. ticker={}", normalizedTicker, e);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Finnhub 주가 서버와 통신하지 못했습니다.",
                    e
            );
        }
    }

    public String getRealTimePrice(String ticker) {
        double price = getCurrentPriceValue(ticker);
        return normalizeTicker(ticker) + " 종목의 실시간 가격: $" + price;
    }

    private String normalizeTicker(String ticker) {
        if (ticker == null || ticker.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "티커를 입력해주세요."
            );
        }

        String normalizedTicker = ticker.trim().toUpperCase();

        if (!normalizedTicker.matches("[A-Z0-9.:-]{1,20}")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "티커 형식이 올바르지 않습니다."
            );
        }

        return normalizedTicker;
    }
}
