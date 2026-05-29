package com.nanda.platform.org.domain.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OrgTreeNode {

    private Long id;
    private String orgCode;
    private String orgName;
    private Long parentId;
    private List<OrgTreeNode> children = new ArrayList<OrgTreeNode>();
}
