package com.beta.providerthread.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class Metrics {

    @EqualsAndHashCode.Exclude
    private String id;

    private String name;

    @EqualsAndHashCode.Exclude
    private String categoryName;

    @EqualsAndHashCode.Exclude
    private String moTypeName;

    @EqualsAndHashCode.Exclude
    private String provider;

    @EqualsAndHashCode.Exclude
    private ProviderType providerType;

    @EqualsAndHashCode.Exclude
    private String serviceUrl;

    private MoType moType;
}
