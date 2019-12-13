package org.redisson.test;

import java.io.Serializable;

class ClassOnlyInAppB implements Serializable {

    private static final long serialVersionUID = 1L;

    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
