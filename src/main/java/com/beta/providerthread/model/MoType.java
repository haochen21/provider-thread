package com.beta.providerthread.model;

import lombok.Getter;

import java.util.EnumSet;
import java.util.Set;

@Getter
public enum MoType {

    WINDOWS(Category.HOST, "Windows"),

    LINUXSERVER(Category.HOST, "LinuxServer"),

    IBMAIXSERVER(Category.HOST, "IbmAixServer"),

    ORACLE(Category.DATABASE, "Oracle"),

    DB2(Category.DATABASE, "Db2"),

    MYSQL(Category.DATABASE, "MySql"),

    WEBLOGIC(Category.MIDDLEWARE, "WebLogic"),

    APACHE(Category.MIDDLEWARE, "Apache");

    public static Set<MoType> hosts = EnumSet.of(WINDOWS, LINUXSERVER, IBMAIXSERVER);

    public static Set<MoType> databases = EnumSet.of(ORACLE, DB2, MYSQL);

    public static Set<MoType> middlewares = EnumSet.of(WEBLOGIC, APACHE);

    private String categoryName;

    private String moTypeName;

    MoType(Category category, String moTypeName) {
        this.categoryName = category.getDisplayName();
        this.moTypeName = moTypeName;
    }
}
