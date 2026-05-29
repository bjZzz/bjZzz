package com.nanda.common.security.context;

import lombok.Data;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Data
public class AuthContext {

    private Long userId;
    private String username;
    private Long orgId;
    private Set<Long> orgIds = new HashSet<Long>();
    private Set<String> permissions = new HashSet<String>();

    public Set<Long> getOrgIds() {
        return orgIds == null ? Collections.<Long>emptySet() : orgIds;
    }

    public Set<String> getPermissions() {
        return permissions == null ? Collections.<String>emptySet() : permissions;
    }
}
