package com.beta.providerthread.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class Metrics {

    private String name;

    private Enum moType;

    @EqualsAndHashCode.Exclude
    private String provider;

    @EqualsAndHashCode.Exclude
    private Enum providerType;

    @EqualsAndHashCode.Exclude
    private String serviceUrl;
}
