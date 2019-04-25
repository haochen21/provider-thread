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

    private MoType moType;

    private String ip;
}
