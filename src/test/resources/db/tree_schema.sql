CREATE TABLE `tree` (
  `TREE_ID` varchar(32) NOT NULL COMMENT '树id',
  `REMARK` varchar(300) NOT NULL COMMENT '描述',
  `CREATOR` varchar(32) DEFAULT NULL COMMENT '创建人',
  `CDATE` datetime DEFAULT NULL COMMENT '创建时间',
  `UPDATOR` varchar(32) DEFAULT NULL COMMENT '最后修改人',
  `UDATE` datetime DEFAULT NULL COMMENT '最后修改时间',
  `VALID` tinyint(4) DEFAULT NULL COMMENT '是否有效',
  PRIMARY KEY (`TREE_ID`)
);

CREATE TABLE `tree_node` (
  `TN_ID` int(11) NOT NULL COMMENT '节点id',
  `TREE_ID` varchar(32) NOT NULL COMMENT '树id',
  `NODE_NAME` varchar(32) DEFAULT NULL COMMENT '节点名',
  `NODE_TYPE` varchar(32) DEFAULT NULL COMMENT '节点类型',
  `SOURE_TABLE` varchar(32) DEFAULT NULL COMMENT '数据来源表名',
  `SOURCE_PK_VAL` varchar(32) DEFAULT NULL COMMENT '数据来源主键(结合SOURE_TABLE可以找到源数据重新定义节点名)',
  `CDATE` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`TN_ID`),
);


CREATE TABLE `tree_path` (
  `ANCESTOR` int(11) NOT NULL COMMENT '祖先节点',
  `DESCENDANT` int(11) NOT NULL COMMENT '后代节点',
  `DISTANCE` int(11) DEFAULT NULL COMMENT '祖先距离后代的距离',
  `TREE_ID` varchar(32) NOT NULL COMMENT '树id',
  `SEQ` int(11) DEFAULT NULL COMMENT '排序号(在同一层中数字越大越优先)',
  `CDATE` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`TREE_ID`,`ANCESTOR`,`DESCENDANT`)
);