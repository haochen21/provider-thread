package com.beta.providerthread.cache;

import com.beta.providerthread.model.MoType;
import com.beta.providerthread.service.MoTypeService;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;


@Service
@NoArgsConstructor
public class MoTypeCache implements Supplier<Map<String, MoType>> {

    private MoTypeService moTypeService;

    // key: category
    private ConcurrentHashMap<String, List<MoType>> categoryCache;

    // key: category.motype
    private ConcurrentHashMap<String, MoType> moTypeCache;

    private static final Logger logger = LoggerFactory.getLogger(MoTypeCache.class);

    public MoTypeCache(MoTypeService moTypeService) {
        this.moTypeService = moTypeService;
    }

    @Override
    public Map<String, MoType> get() {
        logger.info("load moType cache start.......");
        List<MoType> moTypes = moTypeService.findAll();
        categoryCache = moTypes.stream().collect(
                Collectors.groupingBy(moType -> moType.getCategory().getName(),
                        ConcurrentHashMap::new,
                        toList()));
        moTypeCache = moTypes.stream().collect(Collectors.toMap(moType -> moType.toString(),
                Function.identity(),
                (m1, m2) -> m1,
                ConcurrentHashMap::new));
        logger.info("load moType cache end.......");
        return moTypeCache;
    }
}
