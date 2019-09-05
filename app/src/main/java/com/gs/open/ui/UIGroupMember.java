package com.gs.open.ui;

public class UIGroupMember{
    String id;
    String name;
    String portrait;

    public UIGroupMember(String id, String name, String portrait) {
        this.id = id;
        this.name = name;
        this.portrait = portrait;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPortrait() {
        return portrait;
    }

    public void setPortrait(String portrait) {
        this.portrait = portrait;
    }
}
