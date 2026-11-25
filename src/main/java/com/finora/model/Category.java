package com.finora.model;

public class Category {

    private int id;
    private String name;
    private CategoryType type;
    private String icon;

    public Category() {
    }

    public Category(int id, String name, CategoryType type, String icon) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.icon = icon;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CategoryType getType() {
        return type;
    }

    public void setType(CategoryType type) {
        this.type = type;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
