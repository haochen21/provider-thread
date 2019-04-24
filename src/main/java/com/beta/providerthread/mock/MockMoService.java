package com.beta.providerthread.mock;

import com.beta.providerthread.model.Mo;
import com.beta.providerthread.model.MoType;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@NoArgsConstructor
public class MockMoService {


    public List<Mo> getWindows() {
        List<Mo> mos = new ArrayList<>();
        mos.add(createWindow("windows1","127.0.0.1"));

        return mos;
    }

    private Mo createWindow(String name, String ip) {
        Mo mo = new Mo();
        mo.setId(UUID.randomUUID().toString());
        mo.setMoType(MoType.WINDOWS);
        mo.setName(name);
        mo.setIp(ip);

        return mo;
    }

}
