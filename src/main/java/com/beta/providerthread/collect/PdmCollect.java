package com.beta.providerthread.collect;

import com.beta.providerthread.model.SampleValue;
import com.beta.providerthread.pdm.PdmSample;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class PdmCollect extends Collector {

    private PdmSample pdmSample;

    @Override
    public SampleValue execute() {
        return null;
    }
}
