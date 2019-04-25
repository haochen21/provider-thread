package com.beta.providerthread.cache;

import com.beta.providerthread.model.Mo;
import com.beta.providerthread.service.MoService;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@NoArgsConstructor
public class MoCache  implements CacheInit {

    @Autowired
    private MoService moService;

    private ConcurrentHashMap<String, Mo> cache;

    public MoCache(MoService moService) {
        this.moService = moService;
    }

    public void init() {
        cache = moService.findAll().stream().collect(
                Collectors.toMap(Mo::getId, Function.identity(), (m1, m2) -> m1, ConcurrentHashMap::new));
    }

    public Mo get(String key) {
        return cache.get(key);
    }
}
