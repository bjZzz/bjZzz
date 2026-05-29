package com.nanda.platform.user.domain.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PermissionTreeNode {

    private Long id;
    private String permCode;
    private String permName;
    private String module;
    private List<PermissionTreeNode> children = new ArrayList<PermissionTreeNode>();
}
