-- W8: ingestion webhook/dicom + analytics sandbox permissions and seed data
INSERT INTO sys_permission (id, perm_code, perm_name, module, deleted) VALUES
(80, 'ingestion:webhook:manage', 'Webhook订阅管理', 'ingestion', 0),
(81, 'ingestion:dicom:write', 'DICOM采集', 'ingestion', 0),
(82, 'analytics:sandbox:execute', '沙箱分析执行', 'analytics', 0),
(83, 'analytics:sandbox:manage', '沙箱算法与模板管理', 'analytics', 0);

INSERT INTO sys_role_permission (id, role_id, perm_id, deleted) VALUES
(80, 1, 80, 0), (81, 1, 81, 0), (82, 1, 82, 0), (83, 1, 83, 0);

INSERT INTO ana_algorithm_registry (id, algorithm_code, algorithm_name, version, package_ref, status) VALUES
(1, 'survival', '生存分析', '1.0.0', 'nanda-algo-survival', 'ACTIVE'),
(2, 'regression', '回归分析', '1.0.0', 'nanda-algo-regression', 'ACTIVE'),
(3, 'network', '共病网络', '1.0.0', 'nanda-algo-network', 'ACTIVE');

INSERT INTO ana_script_template (id, template_code, template_name, script_content, org_id, created_at) VALUES
(1, 'kaplan_meier', 'Kaplan-Meier 生存曲线', 'import nanda_sandbox as ns\n# TODO: load dataset and plot KM curve', NULL, NOW()),
(2, 'cox_regression', 'Cox 比例风险回归', 'import nanda_sandbox as ns\n# TODO: fit Cox model', NULL, NOW());
