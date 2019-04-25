package com.beta.providerthread.mock;

import com.beta.providerthread.model.Mo;
import com.beta.providerthread.model.MoType;
import com.beta.providerthread.service.MoService;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@NoArgsConstructor
public class MockMoServiceImpl implements MoService {

    @Override
    public List<Mo> findAll() {
        List<Mo> mos = new ArrayList<>();
        mos.add(createWindow("1", "windows-001", "127.0.0.1"));
        mos.add(createWindow("2", "windows-002", "21.0.0.1"));
        return mos;
    }

    private Mo createWindow(String id, String name, String ip) {
        Mo mo = new Mo();
        mo.setId(id);
        mo.setMoType(MoType.WINDOWS);
        mo.setName(name);
        mo.setIp(ip);

        return mo;
    }
}
