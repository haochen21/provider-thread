package com.beta.providerthread.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded=true)
@ToString
public abstract class Rule {

    @EqualsAndHashCode.Include
    private String id;

    private RuleType ruleType;

    private String metricsId;

    private String metricsName;

    private String moFilter;

    private Long sampleInterval;

    private Metrics metrics;

}
