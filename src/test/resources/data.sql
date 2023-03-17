-- <------------------------- DataPowerServiceTest begin ------------------------->
-- => hasAllPower(): check has all power
INSERT INTO sys_data_power(SDP_ID, BTYPE, POWER_TYPE, OWER, POBJ, PVAL) VALUES
-- zhangsan has all power in btype1 useing "user_org"
('sdpa001', 'btype1', 'user_org', 'zhangsan', 'all', 3),
-- zhangsan has all power in btype2 useing "role_org"
('sdpa002', 'btype2', 'role_org', 'role001', 'all', 3),
-- zhangsan has all power in btype3 useing "user_user"
('sdpa003', 'btype3', 'org_org', 'org001', 'all', 3),
-- no belong to zhangsan
('sdpa004', 'btype4', 'user_org', 'lisi', 'all', 3),
-- zhangsan has all power in btype5 useing "user_user"
('sdpa005', 'btype5', 'user_region', 'zhangsan', 'all', 3);

-- => getRegionScope(): check region power
INSERT INTO sys_data_power(SDP_ID, BTYPE, POWER_TYPE, OWER, POBJ, PVAL) VALUES
-- check user_region
('sdpb001', 'b_btyp1', 'user_region', 'zhangsan', 'b_region01', 3),
('sdpb002', 'b_btyp2', 'user_region', 'lisi', 'b_region01', 3),
-- check org_region
('sdpb003', 'b_btyp3', 'org_region', 'org001', 'b_region03', 3),
('sdpb004', 'b_btyp4', 'org_region', 'org002', 'b_region04', 3),
-- check role_region
('sdpb005', 'b_btyp5', 'role_region', 'role001', 'b_region05', 3),
('sdpb006', 'b_btyp6', 'role_region', 'roleTest', 'b_region06', 3);

-- => getOrgScope(): check org power
INSERT INTO sys_dept (id, name,area,sort, mark) VALUES
('a_idorg_01', 'orgname001','region01',1 , 1),
('a_idorg_02', 'orgname002','region01',1 , 1),
('a_idorg_03', 'orgname003','region01',1 , 1),
('a_idorg_04', 'orgname004','region01',1 , 1),

('a_idorg_05', 'orgname005','region02',1 , 1),
('a_idorg_06', 'orgname006','region02',1 , 1),

('a_idorg_07', 'orgname007','region03',1 , 1),
('a_idorg_08', 'orgname008','region03',1 , 1),

('a_idorg_09', 'orgname009','region04',1 , 1);

INSERT INTO sys_data_power(SDP_ID, BTYPE, POWER_TYPE, OWER, POBJ, PVAL) VALUES
-- check user_org
('sdpc001', 'c_btyp1', 'user_org', 'zhangsan', 'a_idorg_01', 3),
('sdpc00_xx1', 'c_btyp1', 'user_org', 'zhangsan', 'a_idorg_xx1', 3),
('sdpc002', 'c_btyp1', 'user_org', 'zhangsan', 'a_idorg_02', 3),
('sdpc003', 'c_btyp1', 'user_org', 'lisi', 'a_idorg_03', 3),

-- check role_org
('sdpc004', 'c_btyp2', 'role_org', 'role001', 'a_idorg_01', 3),
('sdpc005', 'c_btyp2', 'role_org', 'role002', 'a_idorg_02', 3),
('sdpc006', 'c_btyp2', 'role_org', 'role003', 'a_idorg_03', 3),
('sdpc007', 'c_btyp2', 'role_org', 'role001', 'a_idorg_04', 3),

-- check user_region to org
('sdpc008', 'd_btyp1', 'user_region', 'zhangsan', 'region02', 3),
-- check role_region to org
('sdpc009', 'e_btyp1', 'role_region', 'role001', 'region01', 3),
('sdpc010', 'e_btyp1', 'role_region', 'role002', 'region02', 3),

-- mutil
('sdpc011', 'f_btyp1', 'user_org', 'zhangsan', 'a_idorg_01', 3), -- 1
('sdpc012', 'f_btyp1', 'user_region', 'zhangsan', 'region02', 3),-- 2
('sdpc013', 'f_btyp1', 'role_region', 'role001', 'region03', 3), -- 2
('sdpc014', 'f_btyp1', 'role_org', 'role002', 'a_idorg_09', 3); -- 1
-- <------------------------- DataPowerServiceTest end ------------------------->