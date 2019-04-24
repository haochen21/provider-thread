package com.beta.providerthread.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class Rule {

    @EqualsAndHashCode.Exclude
    private String id;

    private Enum moType;

    private Metrics metrics;

    private String moFilter;

    private Long sampleInterval;

}
