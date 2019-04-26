package com.beta.providerthread.cache;

import com.beta.providerthread.model.Category;
import com.beta.providerthread.model.Mo;
import com.beta.providerthread.model.MoType;
import com.beta.providerthread.service.MoService;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Service
@NoArgsConstructor
public class MoCache implements Function<Map<String, List<MoType>>,
        Map<String, Mo>> {

    @Autowired
    private MoService moService;

    private ConcurrentHashMap<String, Mo> cache;

    private static final Logger logger = LoggerFactory.getLogger(MoCache.class);

    public MoCache(MoService moService) {
        this.moService = moService;
    }

    public Mo get(String key) {
        return cache.get(key);
    }

    @Override
    public Map<String, Mo> apply(Map<String, List<MoType>> moTypeMap) {
        logger.info("load mo cache start.......");
        moTypeMap.forEach((categoryName,moTypes) -> {
            moTypes.stream().forEach(moType->{
                logger.info("start find mo,type is: {}.",moType);
                CompletableFuture<List<Mo>> loadMosFuture = CompletableFuture
                        .supplyAsync(() -> moService.findByMoType(moType));
            });

        });
        logger.info("load mo cache end.......");
        return cache;

    }
}
