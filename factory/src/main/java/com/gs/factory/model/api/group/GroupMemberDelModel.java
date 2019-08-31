package com.gs.factory.model.api.group;

import java.util.HashSet;
import java.util.Set;

public class GroupMemberDelModel {
    private Set<String> users = new HashSet<>();

    public GroupMemberDelModel(Set<String> users) {
        this.users = users;
    }

    public Set<String> getUsers() {
        return users;
    }

    public void setUsers(Set<String> users) {
        this.users = users;
    }
}
