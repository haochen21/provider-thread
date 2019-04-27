package com.beta.providerthread.cache;

import com.beta.providerthread.mock.MockMoServiceImpl;
import com.beta.providerthread.mock.MockMoTypeService;
import com.beta.providerthread.model.Mo;
import com.beta.providerthread.model.MoType;
import com.beta.providerthread.service.MoService;
import com.beta.providerthread.service.MoTypeService;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@NoArgsConstructor
public class MoCache implements Function<Map<String, MoType>,
        Map<String, Mo>> {

    @Autowired
    private MoService moService;

    // key: moType+"."+id
    private ConcurrentHashMap<String, Mo> cache;

    private static final Logger logger = LoggerFactory.getLogger(MoCache.class);

    public MoCache(MoService moService) {
        this.moService = moService;
    }

    @Override
    public Map<String, Mo> apply(Map<String, MoType> moTypeMap) {
        logger.info("load mo cache start.......");
        List<CompletableFuture<List<Mo>>> moFutures = new ArrayList<>();
        moTypeMap.forEach((key, moType) -> {
                    CompletableFuture<List<Mo>> moFuture = CompletableFuture
                            .supplyAsync(() -> moService.findByMoType(moType));
                    moFutures.add(moFuture);
                }
        );
        CompletableFuture<Void> allFuturesResult =
                CompletableFuture.allOf(moFutures.toArray(new CompletableFuture[moFutures.size()]));
        allFuturesResult.whenComplete((v, throwable) -> {
            List<Mo> mos = moFutures.stream()
                    .map(future -> future.join())
                    .flatMap(List::stream)
                    .map(mo -> {
                        MoType moType = moTypeMap.get(mo.getCategoryName() + "." + mo.getMoTypeName());
                        mo.setMoType(moType);
                        return mo;
                    })
                    .collect(Collectors.toList());

            cache = mos.stream().collect(
                    Collectors.toMap(mo -> mo.getMoType() + "." + mo.getId(),
                            Function.identity(), (m1, m2) -> m1, ConcurrentHashMap::new));
        }).join();

        logger.info("load mo cache end.......");
        return cache;
    }

    public Mo get(String key) {
        return cache.get(key);
    }

    public static void main(String[] args) {
        MoTypeService moTypeService = new MockMoTypeService();
        MoTypeCache moTypeCache = new MoTypeCache(moTypeService);

        MoService moService = new MockMoServiceImpl();
        MoCache moCache = new MoCache(moService);

        CompletableFuture<Map<String, MoType>> moTypeFuture = CompletableFuture
                .supplyAsync(moTypeCache);
        CompletableFuture<Map<String, Mo>> moFuture = moTypeFuture.thenApplyAsync(moCache);
        moFuture.thenAcceptAsync(moMap ->
                moMap.forEach((k, v) -> logger.info(v.toString()))).join();
    }
}
