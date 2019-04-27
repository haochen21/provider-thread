package com.beta.providerthread.mock;

import com.beta.providerthread.model.Category;
import com.beta.providerthread.model.MoType;
import com.beta.providerthread.service.MoTypeService;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@NoArgsConstructor
public class MockMoTypeService implements MoTypeService {

    private static final Logger logger = LoggerFactory.getLogger(MockMoTypeService.class);

    @Override
    public List<MoType> findAll() {
        List<MoType> moTypes = new ArrayList<>();
        try {
            int sleep = new Random().nextInt(2000) + 1000;
            Thread.sleep(sleep);
            Category host = new Category("host");
            Category database = new Category("database");
            Category middleware = new Category("middleware");
            moTypes.add(new MoType(host, "Windows"));
            moTypes.add(new MoType(host, "LinuxServer"));
            moTypes.add(new MoType(host, "IbmAixServer"));
            moTypes.add(new MoType(database, "Oracle"));
            moTypes.add(new MoType(database, "Db2"));
            moTypes.add(new MoType(database, "MySql"));
            moTypes.add(new MoType(middleware, "WebLogic"));
            moTypes.add(new MoType(middleware, "Apache"));
            logger.info("find moType time is: {}", sleep);
        } catch (Exception ex) {
            logger.error("find moType error!", ex);
        }
        return moTypes;
    }
}
