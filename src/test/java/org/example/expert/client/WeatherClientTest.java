package org.example.expert.client;

import org.example.expert.client.dto.WeatherDto;
import org.example.expert.domain.common.exception.ServerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

    @Test
    @DisplayName("상태코드가 OK가 아니면 예외")
    void statusCode가_OK가_아니면_예외() {

        when(restTemplate.getForEntity(any(), any()))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.BAD_REQUEST));

        assertThrows(ServerException.class,
                () -> weatherClient.getTodayWeather());
    }

    @Test
    @DisplayName("바디가 null이면 예외")
    void body가_null이면_예외() {

        when(restTemplate.getForEntity(any(), any()))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        assertThrows(ServerException.class,
                () -> weatherClient.getTodayWeather());
    }

    @Test
    @DisplayName("바디가 비어있으면 예외")
    void body가_비어있으면_예외() {

        when(restTemplate.getForEntity(any(), any()))
                .thenReturn(new ResponseEntity<>(new WeatherDto[]{}, HttpStatus.OK));

        assertThrows(ServerException.class,
                () -> weatherClient.getTodayWeather());
    }

    @Test
    @DisplayName("오늘 날짜가 일치하면 성공")
    void 오늘_날짜가_있으면_정상반환() {

        WeatherDto dto = new WeatherDto(today(), "맑음");

        when(restTemplate.getForEntity(any(), any()))
                .thenReturn(new ResponseEntity<>(new WeatherDto[]{dto}, HttpStatus.OK));

        String result = weatherClient.getTodayWeather();

        assertEquals("맑음", result);
    }

    @Test
    @DisplayName("오늘 날짜가 없으면 예외 발생")
    void 오늘_날짜가_없으면_예외() {

        WeatherDto dto = new WeatherDto("01-01", "눈");

        when(restTemplate.getForEntity(any(), any()))
                .thenReturn(new ResponseEntity<>(new WeatherDto[]{dto}, HttpStatus.OK));

        assertThrows(ServerException.class,
                () -> weatherClient.getTodayWeather());
    }
}