package org.example.expert.client;

import org.example.expert.client.dto.WeatherDto;
import org.example.expert.domain.common.exception.ServerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeatherClientTest {

    @Mock
    private RestTemplateBuilder builder;

    @Mock
    private RestTemplate restTemplate;

    private WeatherClient weatherClient;

    @BeforeEach
    void setUp() {
        when(builder.build()).thenReturn(restTemplate);
        weatherClient = new WeatherClient(builder);
    }

    private String today() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("MM-dd"));
    }

    // 1️⃣ 상태코드 실패
    @Test
    void 상태코드가_OK가_아니면_예외() {

        when(restTemplate.getForEntity(any(), any()))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.BAD_REQUEST));

        assertThrows(ServerException.class,
                () -> weatherClient.getTodayWeather());
    }

    // 2️⃣ body null
    @Test
    void 바디가_null이면_예외() {

        when(restTemplate.getForEntity(any(), any()))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        assertThrows(ServerException.class,
                () -> weatherClient.getTodayWeather());
    }

    // 3️⃣ body empty
    @Test
    void 바디가_비어있으면_예외() {

        when(restTemplate.getForEntity(any(), any()))
                .thenReturn(new ResponseEntity<>(new WeatherDto[]{}, HttpStatus.OK));

        assertThrows(ServerException.class,
                () -> weatherClient.getTodayWeather());
    }

    // 4️⃣ 오늘 날짜 일치 → 성공
    @Test
    void 오늘날짜가_있으면_정상반환() {

        WeatherDto dto = new WeatherDto(today(), "맑음");

        when(restTemplate.getForEntity(any(), any()))
                .thenReturn(new ResponseEntity<>(new WeatherDto[]{dto}, HttpStatus.OK));

        String result = weatherClient.getTodayWeather();

        assertEquals("맑음", result);
    }

    // 5️⃣ 오늘 날짜 없음 → 예외
    @Test
    void 오늘날짜가_없으면_예외() {

        WeatherDto dto = new WeatherDto("01-01", "눈");

        when(restTemplate.getForEntity(any(), any()))
                .thenReturn(new ResponseEntity<>(new WeatherDto[]{dto}, HttpStatus.OK));

        assertThrows(ServerException.class,
                () -> weatherClient.getTodayWeather());
    }
}