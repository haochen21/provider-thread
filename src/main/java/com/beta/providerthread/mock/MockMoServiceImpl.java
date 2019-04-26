package com.beta.providerthread.mock;

import com.beta.providerthread.model.Category;
import com.beta.providerthread.model.Mo;
import com.beta.providerthread.model.MoType;
import com.beta.providerthread.service.MoService;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@NoArgsConstructor
public class MockMoServiceImpl implements MoService {

    private static final Logger logger = LoggerFactory.getLogger(MockMoServiceImpl.class);


    @Override
    public List<Mo> findByMoType(MoType moType) {
        List<Mo> mos = new ArrayList<>();
        try {
            int sleep = new Random().nextInt(2000) + 1000;
            Thread.sleep(sleep);
            switch (moType.getName()) {
                case "Windows":
                    mos.addAll(findWindows());
                    break;
                case "LinuxServer":
                    mos.addAll(findLinuxServer());
                    break;
                default:
                    logger.info("can't match motype: {}", moType.toString());
                    break;
            }
            logger.info("find mo by moType: {},time is: {}", moType, sleep);
        } catch (Exception ex) {
            logger.error("find mo error!", ex);
        }
        return mos;
    }

    private List<Mo> findWindows() {
        List<Mo> mos = new ArrayList<>();
        mos.add(createWindow("1", "windows-001", "127.0.0.1"));
        mos.add(createWindow("2", "windows-002", "21.0.0.1"));
        return mos;
    }

    private Mo createWindow(String id, String name, String ip) {
        Mo mo = new Mo();
        mo.setId(id);
        mo.setCategoryName("host");
        mo.setMoTypeName("Windows");
        mo.setName(name);
        mo.setIp(ip);

        return mo;
    }

    private List<Mo> findLinuxServer() {
        List<Mo> mos = new ArrayList<>();
        mos.add(createLinuxServer("1", "centos-001", "127.0.0.3"));
        mos.add(createLinuxServer("2", "centos-002", "21.0.0.4"));
        return mos;
    }

    private Mo createLinuxServer(String id, String name, String ip) {
        Mo mo = new Mo();
        mo.setId(id);
        mo.setCategoryName("host");
        mo.setMoTypeName("LinuxServer");
        mo.setName(name);
        mo.setIp(ip);

        return mo;
    }
}
