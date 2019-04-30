package com.beta.providerthread.service;

import lombok.Getter;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * 1. connectTimeOut：指建立连接的超时时间
 * 2. connectionRequestTimeOut：指从连接池获取到连接的超时时间，如果是非连接池的话，该参数暂时没有发现有什么用处
 * 3. socketTimeOut：指客户端和服务进行数据交互的时间，是指两者之间如果两个数据包之间的时间大于该时间则认为超时，而不是整个交互的整体时间，
 *    比如如果设置1秒超时，如果每隔0.8秒传输一次数据，传输10次，总共8秒，这样是不超时的。
 *    而如果任意两个数据包之间的时间超过了1秒，则超时。
 */
@Getter
public class RestTemplateService {

    private RestTemplate restTemplate;

    public RestTemplateService(int connectTimeout, int connectionRequestTimeout, int readTimeout) {

        PoolingHttpClientConnectionManager poolingConnectionManager = new PoolingHttpClientConnectionManager();
        poolingConnectionManager.setMaxTotal(1000); // 连接池最大连接数
        poolingConnectionManager.setDefaultMaxPerRoute(100); // 每个主机的并发

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        //设置HTTP连接管理器
        httpClientBuilder.setConnectionManager(poolingConnectionManager);
        HttpClient httpClient = httpClientBuilder.build();

        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        httpRequestFactory.setConnectTimeout(connectTimeout);
        httpRequestFactory.setConnectionRequestTimeout(connectionRequestTimeout);
        httpRequestFactory.setReadTimeout(readTimeout);
        restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(httpRequestFactory);
    }


}
