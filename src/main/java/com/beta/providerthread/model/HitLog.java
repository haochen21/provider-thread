package com.beta.providerthread.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class HitLog {

    @EqualsAndHashCode.Exclude
    private String id;

    private Mo mo;

    private Rule rule;

}
