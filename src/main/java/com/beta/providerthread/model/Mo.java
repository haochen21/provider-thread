package com.beta.providerthread.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class Mo {

    private String id;

    private String name;

    private String categoryName;

    private String moTypeName;

    private String ip;

    @ToString.Exclude
    private MoType moType;

}
