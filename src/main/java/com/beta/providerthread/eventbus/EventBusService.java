package com.beta.providerthread.eventbus;

import com.google.common.eventbus.EventBus;
import org.springframework.stereotype.Service;

@Service
public class EventBusService {

    private EventBus eventBus;

    public EventBusService(){
        eventBus = new EventBus();
    }

    public EventBus getEventBus() {
        return eventBus;
    }
}
