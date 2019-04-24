package com.beta.providerthread.model;

import lombok.Getter;

@Getter
public enum Category {

    HOST("host"), DATABASE("database"),

    MIDDLEWARE("middleware"), LINE("line");

    private final String displayName;

    Category(final String displayName) {
        this.displayName = displayName;
    }

}
