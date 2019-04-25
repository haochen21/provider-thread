package com.beta.providerthread.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class SampleValue {

    @ToString.Include
    private LocalDateTime sampleTime;

    @ToString.Include
    private ValueType type;

    @ToString.Include
    private Object value;

    @ToString.Include
    private String metrics;

    @ToString.Include
    private String mo;

}
