package com.beta.providerthread.collect;

import com.beta.providerthread.model.SampleValue;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.web.client.RestTemplate;

@NoArgsConstructor
@AllArgsConstructor
public class RpcCollect extends Collector {

    private RestTemplate restTemplate;

    @Override
    public SampleValue execute() {
        return null;
    }

}
