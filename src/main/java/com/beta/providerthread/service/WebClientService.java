package com.beta.providerthread.service;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.Getter;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Service
@Getter
public class WebClientService {

    private WebClient pdmWebclient;

    public WebClientService() {
        HttpClient httpClient = HttpClient.create()
                .tcpConfiguration(client ->
                        client.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                                .doOnConnected(conn -> conn
                                        .addHandlerLast(new ReadTimeoutHandler(10))
                                        .addHandlerLast(new WriteTimeoutHandler(10))));
        //if you use WebClient.Builder to create WebClient instances spring boot uses the same ClientHttpConnector for each one
        //https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-webclient.html
        pdmWebclient = WebClient.builder()
                .baseUrl("http://127.0.0.1:9200")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }


}
