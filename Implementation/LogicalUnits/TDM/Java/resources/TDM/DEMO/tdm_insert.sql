
INSERT INTO public.business_entities (be_name, be_description, be_id, be_created_by, be_creation_date, be_last_updated_date, be_last_updated_by, be_status) VALUES ('Customer360', 'This is a Business Entity created for the TDM GUI for testers course.', 1, 'admin', '2022-12-01 13:27:20.522', '2022-12-01 13:38:25.987', 'admin', 'Active') ON CONFLICT DO NOTHING;

-- Source Env

INSERT INTO public.environment_products (environment_product_id, environment_id, product_id, product_version, created_by, creation_date, last_updated_date, last_updated_by, status, data_center_name) VALUES (1, 1, 1, '1', 'admin', now(), now(), 'admin', 'Active', 'DC1') ON CONFLICT DO NOTHING;
INSERT INTO public.environment_products (environment_product_id, environment_id, product_id, product_version, created_by, creation_date, last_updated_date, last_updated_by, status, data_center_name) VALUES (2, 1, 2, 'PROD', 'admin', now(), now(), 'admin', 'Active', 'DC1') ON CONFLICT DO NOTHING;
INSERT INTO public.environment_products (environment_product_id, environment_id, product_id, product_version, created_by, creation_date, last_updated_date, last_updated_by, status, data_center_name) VALUES (3, 1, 3, '1', 'admin', now(), now(), 'admin', 'Active', 'DC1') ON CONFLICT DO NOTHING;
INSERT INTO public.environment_products (environment_product_id, environment_id, product_id, product_version, created_by, creation_date, last_updated_date, last_updated_by, status, data_center_name) VALUES (4, 1, 4, '1', 'admin', now(), now(), 'admin', 'Active', 'DC1') ON CONFLICT DO NOTHING;

-- Target Env

INSERT INTO public.environment_products (environment_product_id, environment_id, product_id, product_version, created_by, creation_date, last_updated_date, last_updated_by, status, data_center_name) VALUES (5, 2, 1, '1', 'admin', now(), now(), 'admin', 'Active', 'DC1') ON CONFLICT DO NOTHING;
INSERT INTO public.environment_products (environment_product_id, environment_id, product_id, product_version, created_by, creation_date, last_updated_date, last_updated_by, status, data_center_name) VALUES (6, 2, 2, 'DEV', 'admin', now(), now(), 'admin', 'Active', 'DC1') ON CONFLICT DO NOTHING;
INSERT INTO public.environment_products (environment_product_id, environment_id, product_id, product_version, created_by, creation_date, last_updated_date, last_updated_by, status, data_center_name) VALUES (7, 2, 3, '1', 'admin', now(), now(), 'admin', 'Active', 'DC1') ON CONFLICT DO NOTHING;
INSERT INTO public.environment_products (environment_product_id, environment_id, product_id, product_version, created_by, creation_date, last_updated_date, last_updated_by, status, data_center_name) VALUES (8, 2, 4, '1', 'admin', now(), now(), 'admin', 'Active', 'DC1') ON CONFLICT DO NOTHING;

--Synthetic Env
INSERT INTO public.environment_products (environment_product_id, environment_id, product_id, product_version, created_by, creation_date, last_updated_date, last_updated_by, status, data_center_name) VALUES (9, -1,  1, 'Synthetic', 'admin', now(), now(), 'admin', 'Active', 'DC1') ON CONFLICT DO NOTHING;
INSERT INTO public.environment_products (environment_product_id, environment_id, product_id, product_version, created_by, creation_date, last_updated_date, last_updated_by, status, data_center_name) VALUES (10, -1, 2, 'Synthetic', 'admin', now(), now(), 'admin', 'Active', 'DC1') ON CONFLICT DO NOTHING;
INSERT INTO public.environment_products (environment_product_id, environment_id, product_id, product_version, created_by, creation_date, last_updated_date, last_updated_by, status, data_center_name) VALUES (11, -1, 3, 'Synthetic', 'admin', now(), now(), 'admin', 'Active', 'DC1') ON CONFLICT DO NOTHING;
INSERT INTO public.environment_products (environment_product_id, environment_id, product_id, product_version, created_by, creation_date, last_updated_date, last_updated_by, status, data_center_name) VALUES (12, -1, 4, 'Synthetic', 'admin', now(), now(), 'admin', 'Active', 'DC1') ON CONFLICT DO NOTHING;

INSERT INTO public.environment_role_users (environment_id, role_id, user_type, username, user_id) VALUES (1, 1, 'ID', 'ALL', '-1') ON CONFLICT DO NOTHING;
INSERT INTO public.environment_role_users (environment_id, role_id, user_type, username, user_id) VALUES (1, 1, 'GROUP', 'Testersgrp1', 'Testersgrp1') ON CONFLICT DO NOTHING;
INSERT INTO public.environment_role_users (environment_id, role_id, user_type, username, user_id) VALUES (2, 2, 'ID', 'ALL', '-1') ON CONFLICT DO NOTHING;
INSERT INTO public.environment_role_users (environment_id, role_id, user_type, username, user_id) VALUES (2, 2, 'GROUP', 'Testersgrp1', 'Testersgrp1') ON CONFLICT DO NOTHING;

INSERT INTO public.environments (environment_name, environment_description, environment_expiration_date, environment_point_of_contact_first_name, environment_point_of_contact_last_name, environment_point_of_contact_phone1, environment_point_of_contact_phone2, environment_point_of_contact_email, environment_id, environment_created_by, environment_creation_date, environment_last_updated_date, environment_last_updated_by, environment_status, allow_write, allow_read, sync_mode) VALUES ('SRC', 'This is the Source environment.', NULL, NULL, NULL, NULL, NULL, NULL, 1, 'admin', '2022-12-01 13:40:14.578', '2022-12-01 13:41:58.95', 'admin', 'Active', false, true, 'ON') ON CONFLICT DO NOTHING;
INSERT INTO public.environments (environment_name, environment_description, environment_expiration_date, environment_point_of_contact_first_name, environment_point_of_contact_last_name, environment_point_of_contact_phone1, environment_point_of_contact_phone2, environment_point_of_contact_email, environment_id, environment_created_by, environment_creation_date, environment_last_updated_date, environment_last_updated_by, environment_status, allow_write, allow_read, sync_mode) VALUES ('TAR', 'This is the Target environment.', NULL, NULL, NULL, NULL, NULL, NULL, 2, 'admin', '2022-12-01 13:42:33.739', '2022-12-01 13:43:23.54', 'admin', 'Active', true, true, 'ON') ON CONFLICT DO NOTHING;

INSERT INTO public.permission_groups_mapping (description, fabric_role, permission_group, created_by, updated_by, creation_date, update_date) VALUES ('Maps Tester Permission Group to Testersgrp1 Fabric Role.', 'Testersgrp1', 'tester', 'admin', 'admin', '2022-12-01 13:25:30.980407', '2022-12-01 13:25:30.980407') ON CONFLICT DO NOTHING;

INSERT INTO public.product_logical_units (lu_name, lu_description, be_id, lu_parent_id, lu_id, product_name, lu_parent_name, product_id) VALUES ('Customer', 'This is the Parent LU.', 1, NULL, 1, 'CRM', NULL, 1) ON CONFLICT DO NOTHING;
INSERT INTO public.product_logical_units (lu_name, lu_description, be_id, lu_parent_id, lu_id, product_name, lu_parent_name, product_id) VALUES ('Billing', 'This is a Child LU.', 1, 1, 4, 'Billing', 'Customer', 2) ON CONFLICT DO NOTHING;
INSERT INTO public.product_logical_units (lu_name, lu_description, be_id, lu_parent_id, lu_id, product_name, lu_parent_name, product_id) VALUES ('Collection', 'This is a Child LU.', 1, 1, 2, 'Collection', 'Customer', 3) ON CONFLICT DO NOTHING;
INSERT INTO public.product_logical_units (lu_name, lu_description, be_id, lu_parent_id, lu_id, product_name, lu_parent_name, product_id) VALUES ('Orders', 'This is a Child LU.', 1, 1, 3, 'Ordering', 'Customer', 4) ON CONFLICT DO NOTHING;

INSERT INTO public.products (product_name, product_description,  product_versions, product_id, product_created_by, product_creation_date, product_last_updated_date, product_last_updated_by, product_status) VALUES ('CRM', 'CRM Application', '1', 1, 'admin', now(), now(), 'admin', 'Active') ON CONFLICT DO NOTHING;
INSERT INTO public.products (product_name, product_description,  product_versions, product_id, product_created_by, product_creation_date, product_last_updated_date, product_last_updated_by, product_status) VALUES ('Billing', 'Billing Application', 'DEV, PROD', 2, 'admin', now(), now(), 'admin', 'Active') ON CONFLICT DO NOTHING;
INSERT INTO public.products (product_name, product_description,  product_versions, product_id, product_created_by, product_creation_date, product_last_updated_date, product_last_updated_by, product_status) VALUES ('Collection', 'Collection Application', '1', 3, 'admin', now(), now(), 'admin', 'Active') ON CONFLICT DO NOTHING;
INSERT INTO public.products (product_name, product_description,  product_versions, product_id, product_created_by, product_creation_date, product_last_updated_date, product_last_updated_by, product_status) VALUES ('Ordering', 'Ordering Application', '1', 4, 'admin', now(), now(), 'admin', 'Active') ON CONFLICT DO NOTHING;

INSERT INTO public.tdm_be_post_exe_process (process_id, process_name, process_description, be_id, execution_order) VALUES (1, 'postTaskExePrintToLog', 'This is a Post Execution Process Flow.', 1, 1) ON CONFLICT DO NOTHING;
SELECT pg_catalog.setval('public.business_entities_be_id_seq', 1, true);
SELECT pg_catalog.setval('public.environment_product_id_seq', 8, true);
SELECT pg_catalog.setval('public.environment_roles_role_id_seq', 3, true);
SELECT pg_catalog.setval('public.environments_environment_id_seq', 3, true);
SELECT pg_catalog.setval('public.post_exe_process_id_seq', 1, true);
SELECT pg_catalog.setval('public.product_logical_units_lu_id_seq', 3, true);
SELECT pg_catalog.setval('public.products_product_id_seq', 4, true);
SELECT pg_catalog.setval('public.tasks_ref_table_id_seq', 1, false);
SELECT pg_catalog.setval('public.tasks_task_execution_id_seq', 1, false);
SELECT pg_catalog.setval('public.tasks_task_id_seq', 1, false);
SELECT pg_catalog.setval('public.tdm_be_env_exclusion_list_be_env_exclusion_list_id_seq', 1, false);