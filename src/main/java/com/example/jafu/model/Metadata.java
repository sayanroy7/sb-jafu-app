package com.example.jafu.model;

import java.util.Map;

/**
 * @author SAROY on 1/15/2020
 */
public class Metadata {

    private Integer id;

    private String name;

    private Map<String, String> values;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getValues() {
        return values;
    }

    public void setValues(Map<String, String> values) {
        this.values = values;
    }
}
