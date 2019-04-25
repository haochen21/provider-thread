package com.beta.providerthread.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public abstract class Rule {

    private String id;

    private RuleType ruleType;

    private MoType moType;

    private String metricsName;

    private String moFilter;

    private Long sampleInterval;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Metrics metrics;

}
