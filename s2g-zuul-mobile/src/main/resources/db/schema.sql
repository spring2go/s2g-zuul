CREATE TABLE `filters` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `filter_id` varchar(45) DEFAULT NULL COMMENT '过滤器id',
  `revision` int(11) DEFAULT NULL COMMENT '版本',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `is_active` tinyint(1) DEFAULT '0' COMMENT '是否是活跃',
  `is_canary` tinyint(1) DEFAULT '0' COMMENT '是否是灰度',
  `filter_code` longtext COMMENT 'filter代码',
  `filter_type` varchar(45) DEFAULT NULL COMMENT 'filter类型',
  `filter_name` varchar(45) DEFAULT NULL COMMENT '名称',
  `disable_property_name` varchar(45) DEFAULT NULL COMMENT '禁用属性',
  `filter_order` varchar(45) DEFAULT NULL COMMENT '顺序',
  `application_name` varchar(45) DEFAULT NULL COMMENT '应用名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8;

