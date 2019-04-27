package com.beta.providerthread.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class Mo {

    private String id;

    @EqualsAndHashCode.Exclude
    private String name;

    private String categoryName;

    private String moTypeName;

    @EqualsAndHashCode.Exclude
    private String ip;

    @EqualsAndHashCode.Exclude
    private MoType moType;

}
