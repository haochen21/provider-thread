package com.beta.providerthread.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public abstract class HitLog {

    private String categoryName;

    private String moTypeName;

    private String moId;

    private String ruleId;

    private Mo mo;

    private Rule rule;

}
