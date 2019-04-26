package com.beta.providerthread.cache;

import com.beta.providerthread.model.MoType;
import com.beta.providerthread.service.MoTypeService;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;


@Service
@NoArgsConstructor
public class MoTypeCache implements Supplier<Map<String, List<MoType>>> {

    private MoTypeService moTypeService;

    private ConcurrentHashMap<String, List<MoType>> cache;

    public MoTypeCache(MoTypeService moTypeService) {
        this.moTypeService = moTypeService;
    }

    @Override
    public Map<String, List<MoType>> get() {
        cache = moTypeService.findAll().stream().collect(
                Collectors.groupingBy(moType -> moType.getCategory().getName(),
                        ConcurrentHashMap::new,
                        toList()));
        return cache;
    }
}
