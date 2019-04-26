package com.beta.providerthread.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
public class MoType {

    private Category category;

    private String name;

    @Override
    public String toString() {
        return category.getName() + "." + name;
    }
}
