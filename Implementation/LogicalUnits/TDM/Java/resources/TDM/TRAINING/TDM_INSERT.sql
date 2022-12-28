--
-- PostgreSQL database dump
--

-- Dumped from database version 13.3 (Debian 13.3-1.pgdg100+1)
-- Dumped by pg_dump version 13.6

-- Started on 2022-12-13 14:12:22

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 3246 (class 0 OID 32777)
-- Dependencies: 200
-- Data for Name: activities; Type: TABLE DATA; Schema: public; Owner: tdm
--

-- INSERT INTO public.activities (date, action, entity, username, description, user_id) VALUES ('2022-12-01 13:27:20.695', 'create', 'Business entities', 'admin', 'Business entity Customer360 was created', 'admin');
--INSERT INTO public.activities (date, action, entity, username, description, user_id) VALUES ('2022-12-01 13:29:18.692', 'update', 'Business entities', 'admin', 'Logical unit Customer was added to business entity Customer360', 'admin');
--INSERT INTO public.activities (date, action, entity, username, description, user_id) VALUES ('2022-12-01 13:29:18.695', 'update', 'Business entities', 'admin', 'Logical unit Collection was added to business entity Customer360', 'admin');
--INSERT INTO public.activities (date, action, entity, username, description, user_id) VALUES ('2022-12-01 13:29:18.697', 'update', 'Business entities', 'admin', 'Logical unit Orders was added to business entity Customer360', 'admin');
--INSERT INTO public.activities (date, action, entity, username, description, user_id) VALUES ('2022-12-01 13:31:05.926', 'update', 'Business entities', 'admin', 'Post Execution Process postTaskExePrintToLog was added to business entity Customer360', 'admin');
--INSERT INTO public.activities (date, action, entity, username, description, user_id) VALUES ('2022-12-01 13:38:25.99', 'update', 'Business entities', 'admin', 'Business entity Customer360 was updated', 'admin');
--INSERT INTO public.activities (date, action, entity, username, description, user_id) VALUES ('2022-12-01 13:39:24.84', 'create', 'Product', 'admin', 'Product CRM System was created', 'admin');
--INSERT INTO public.activities (date, action, entity, username, description, user_id) VALUES ('2022-12-01 13:39:28.675', 'update', 'Product', 'admin', 'Product CRM System was updated', 'admin');
--INSERT INTO public.activities (date, action, entity, username, description, user_id) VALUES ('2022-12-01 13:40:14.631', 'delete', 'Environment', 'admin', 'Environment SRCLocalDebug was created', 'admin');
--INSERT INTO public.activities (date, action, entity, username, description, user_id) VALUES ('2022-12-01 13:40:59.787', 'update', 'Business entities', 'admin', 'Logical unit Collection was updated', 'admin');
--INSERT INTO public.activities (date, action, entity, username, description, user_id) VALUES ('2022-12-01 13:40:59.791', 'update', 'Business entities', 'admin', 'Logical unit Customer was updated', 'admin');
--INSERT INTO public.activities (date, action, entity, username, description, user_id) VALUES ('2022-12-01 13:40:59.795', 'update', 'Business entities', 'admin', 'Logical unit Orders was updated', 'admin');
--INSERT INTO public.activities (date, action, entity, username, description, user_id) VALUES ('2022-12-01 13:41:02.997', 'update', 'Product', 'admin', 'Product CRM System was updated', 'admin');
--INSERT INTO public.activities (date, action, entity, username, description, user_id) VALUES ('2022-12-01 13:41:16.237', 'update', 'Environments', 'admin', 'Products of environment SRCLocalDebug were updated', 'admin');
--INSERT INTO public.activities (date, action, entity, username, description, user_id) VALUES ('2022-12-01 13:41:54.059', 'update', 'Environment', 'admin', 'Role set1 was added to environment SRCLocalDebug', 'admin');
--INSERT INTO public.activities (date, action, entity, username, description, user_id) VALUES ('2022-12-01 13:41:54.093', 'update', 'Environments', 'admin', 'Testers of role set1 of environment SRCLocalDebug were updated', 'admin');
--INSERT INTO public.activities (date, action, entity, username, description, user_id) VALUES ('2022-12-01 13:41:58.952', 'update', 'Environment', 'admin', 'Environment SRCLocalDebug was updated', 'admin');
--INSERT INTO public.activities (date, action, entity, username, description, user_id) VALUES ('2022-12-01 13:42:33.742', 'delete', 'Environment', 'admin', 'Environment TARLocalDebug was created', 'admin');
--INSERT INTO public.activities (date, action, entity, username, description, user_id) VALUES ('2022-12-01 13:42:43.85', 'update', 'Environments', 'admin', 'Products of environment TARLocalDebug were updated', 'admin');
--INSERT INTO public.activities (date, action, entity, username, description, user_id) VALUES ('2022-12-01 13:43:19.186', 'update', 'Environment', 'admin', 'Role set2 was added to environment TARLocalDebug', 'admin');
--INSERT INTO public.activities (date, action, entity, username, description, user_id) VALUES ('2022-12-01 13:43:19.254', 'update', 'Environments', 'admin', 'Testers of role set2 of environment TARLocalDebug were updated', 'admin');
--INSERT INTO public.activities (date, action, entity, username, description, user_id) VALUES ('2022-12-01 13:43:23.543', 'update', 'Environment', 'admin', 'Environment TARLocalDebug was updated', 'admin');


--
-- TOC entry 3248 (class 0 OID 32785)
-- Dependencies: 202
-- Data for Name: business_entities; Type: TABLE DATA; Schema: public; Owner: tdm
--

INSERT INTO public.business_entities (be_name, be_description, be_id, be_created_by, be_creation_date, be_last_updated_date, be_last_updated_by, be_status) VALUES ('Customer360', 'This is a Business Entity created for the TDM GUI for testers course.', 1, 'admin', '2022-12-01 13:27:20.522', '2022-12-01 13:38:25.987', 'admin', 'Active');


--
-- TOC entry 3250 (class 0 OID 32794)
-- Dependencies: 204
-- Data for Name: environment_owners; Type: TABLE DATA; Schema: public; Owner: tdm
--



--
-- TOC entry 3252 (class 0 OID 32800)
-- Dependencies: 206
-- Data for Name: environment_products; Type: TABLE DATA; Schema: public; Owner: tdm
--

-- Source Env

INSERT INTO public.environment_products (environment_product_id, environment_id, product_id, product_version, created_by, creation_date, last_updated_date, last_updated_by, status) VALUES (1, 1, 1, '1', 'admin', now(), now(), 'admin', 'Active');

INSERT INTO public.environment_products (environment_product_id, environment_id, product_id, product_version, created_by, creation_date, last_updated_date, last_updated_by, status) VALUES (2, 1, 2, 'PROD', 'admin', now(), now(), 'admin', 'Active');

INSERT INTO public.environment_products (environment_product_id, environment_id, product_id, product_version, created_by, creation_date, last_updated_date, last_updated_by, status) VALUES (3, 1, 3, '1', 'admin', now(), now(), 'admin', 'Active');

INSERT INTO public.environment_products (environment_product_id, environment_id, product_id, product_version, created_by, creation_date, last_updated_date, last_updated_by, status) VALUES (4, 1, 4, '1', 'admin', now(), now(), 'admin', 'Active');

-- Target Env

INSERT INTO public.environment_products (environment_product_id, environment_id, product_id, product_version, created_by, creation_date, last_updated_date, last_updated_by, status) VALUES (5, 2, 1, '1', 'admin', now(), now(), 'admin', 'Active');

INSERT INTO public.environment_products (environment_product_id, environment_id, product_id, product_version, created_by, creation_date, last_updated_date, last_updated_by, status) VALUES (6, 2, 2, 'DEV', 'admin', now(), now(), 'admin', 'Active');

INSERT INTO public.environment_products (environment_product_id, environment_id, product_id, product_version, created_by, creation_date, last_updated_date, last_updated_by, status) VALUES (7, 2, 3, '1', 'admin', now(), now(), 'admin', 'Active');

INSERT INTO public.environment_products (environment_product_id, environment_id, product_id, product_version, created_by, creation_date, last_updated_date, last_updated_by, status) VALUES (8, 2, 4, '1', 'admin', now(), now(), 'admin', 'Active');


--
-- TOC entry 3253 (class 0 OID 32807)
-- Dependencies: 207
-- Data for Name: environment_role_users; Type: TABLE DATA; Schema: public; Owner: tdm
--

INSERT INTO public.environment_role_users (environment_id, role_id, user_type, username, user_id) VALUES (1, 1, 'ID', 'ALL', '-1');
INSERT INTO public.environment_role_users (environment_id, role_id, user_type, username, user_id) VALUES (1, 1, 'GROUP', 'Testersgrp1', 'Testersgrp1');
INSERT INTO public.environment_role_users (environment_id, role_id, user_type, username, user_id) VALUES (2, 2, 'ID', 'ALL', '-1');
INSERT INTO public.environment_role_users (environment_id, role_id, user_type, username, user_id) VALUES (2, 2, 'GROUP', 'Testersgrp1', 'Testersgrp1');


--
-- TOC entry 3255 (class 0 OID 32816)
-- Dependencies: 209
-- Data for Name: environment_roles; Type: TABLE DATA; Schema: public; Owner: tdm
--

INSERT INTO public.environment_roles (environment_id, role_name, role_description, allowed_delete_before_load, allowed_creation_of_synthetic_data, allowed_random_entity_selection, allowed_request_of_fresh_data, allowed_task_scheduling, allowed_number_of_entities_to_copy, role_id, role_created_by, role_creation_date, role_last_updated_date, role_expiration_date, role_last_updated_by, role_status, allowed_refresh_reference_data, allowed_replace_sequences, allow_read, allow_write, allowed_number_of_entities_to_read, allowed_entity_versioning, allowed_test_conn_failure, allowed_number_of_reserved_entities) VALUES (1, 'set1', '', false, false, false, true, true, 0, 1, 'admin', '2022-12-01 13:41:54.008', '2022-12-01 13:41:54.008', NULL, 'admin', 'Active', true, false, true, false, 150, true, true, 0);
INSERT INTO public.environment_roles (environment_id, role_name, role_description, allowed_delete_before_load, allowed_creation_of_synthetic_data, allowed_random_entity_selection, allowed_request_of_fresh_data, allowed_task_scheduling, allowed_number_of_entities_to_copy, role_id, role_created_by, role_creation_date, role_last_updated_date, role_expiration_date, role_last_updated_by, role_status, allowed_refresh_reference_data, allowed_replace_sequences, allow_read, allow_write, allowed_number_of_entities_to_read, allowed_entity_versioning, allowed_test_conn_failure, allowed_number_of_reserved_entities) VALUES (2, 'set2', '', true, true, true, true, true, 1000, 2, 'admin', '2022-12-01 13:43:19.183', '2022-12-01 13:43:19.183', NULL, 'admin', 'Active', true, true, true, true, 5, true, true, 1000);


--
-- TOC entry 3257 (class 0 OID 32837)
-- Dependencies: 211
-- Data for Name: environments; Type: TABLE DATA; Schema: public; Owner: tdm
--

INSERT INTO public.environments (environment_name, environment_description, environment_expiration_date, environment_point_of_contact_first_name, environment_point_of_contact_last_name, environment_point_of_contact_phone1, environment_point_of_contact_phone2, environment_point_of_contact_email, environment_id, environment_created_by, environment_creation_date, environment_last_updated_date, environment_last_updated_by, environment_status, allow_write, allow_read, sync_mode) VALUES ('SRCLocalDebug', 'This is the Source environment.', NULL, NULL, NULL, NULL, NULL, NULL, 1, 'admin', '2022-12-01 13:40:14.578', '2022-12-01 13:41:58.95', 'admin', 'Active', false, true, 'ON');
INSERT INTO public.environments (environment_name, environment_description, environment_expiration_date, environment_point_of_contact_first_name, environment_point_of_contact_last_name, environment_point_of_contact_phone1, environment_point_of_contact_phone2, environment_point_of_contact_email, environment_id, environment_created_by, environment_creation_date, environment_last_updated_date, environment_last_updated_by, environment_status, allow_write, allow_read, sync_mode) VALUES ('TARLocalDebug', 'This is the Target environment.', NULL, NULL, NULL, NULL, NULL, NULL, 2, 'admin', '2022-12-01 13:42:33.739', '2022-12-01 13:43:23.54', 'admin', 'Active', true, true, 'ON');


--
-- TOC entry 3258 (class 0 OID 32847)
-- Dependencies: 212
-- Data for Name: instance_table_count; Type: TABLE DATA; Schema: public; Owner: tdm
--



--
-- TOC entry 3259 (class 0 OID 32853)
-- Dependencies: 213
-- Data for Name: parameters; Type: TABLE DATA; Schema: public; Owner: tdm
--



--
-- TOC entry 3260 (class 0 OID 32859)
-- Dependencies: 214
-- Data for Name: permission_groups_mapping; Type: TABLE DATA; Schema: public; Owner: tdm
--

-- 28-12-22 - comment the initial setting since it is included in the create TDM DB scripts

-- INSERT INTO public.permission_groups_mapping (description, fabric_role, permission_group, created_by, updated_by, creation_date, update_date) VALUES ('Initial mapping for admin user', 'admin', 'admin', 'admin', 'admin', '2022-03-31 18:28:28.773873', '2022-03-31 18:28:28.773873');
INSERT INTO public.permission_groups_mapping (description, fabric_role, permission_group, created_by, updated_by, creation_date, update_date) VALUES ('Maps Tester Permission Group to Testersgrp1 Fabric Role.', 'Testersgrp1', 'tester', 'admin', 'admin', '2022-12-01 13:25:30.980407', '2022-12-01 13:25:30.980407');


--
-- TOC entry 3264 (class 0 OID 32871)
-- Dependencies: 218
-- Data for Name: product_logical_units; Type: TABLE DATA; Schema: public; Owner: tdm
--

INSERT INTO public.product_logical_units (lu_name, lu_description, be_id, lu_parent_id, lu_id, product_name, lu_parent_name, product_id) VALUES ('Customer', 'This is the Parent LU.', 1, NULL, 1, 'CRM', NULL, 1);
INSERT INTO public.product_logical_units (lu_name, lu_description, be_id, lu_parent_id, lu_id, product_name, lu_parent_name, product_id) VALUES ('Billing', 'This is a Child LU.', 1, 1, 4, 'Billing', 'Customer', 2);
INSERT INTO public.product_logical_units (lu_name, lu_description, be_id, lu_parent_id, lu_id, product_name, lu_parent_name, product_id) VALUES ('Collection', 'This is a Child LU.', 1, 1, 2, 'Collection', 'Customer', 3);
INSERT INTO public.product_logical_units (lu_name, lu_description, be_id, lu_parent_id, lu_id, product_name, lu_parent_name, product_id) VALUES ('Orders', 'This is a Child LU.', 1, 1, 3, 'Ordering', 'Customer', 4);



--
-- TOC entry 3266 (class 0 OID 32880)
-- Dependencies: 220
-- Data for Name: products; Type: TABLE DATA; Schema: public; Owner: tdm
--


INSERT INTO public.products (product_name, product_description,  product_versions, product_id, product_created_by, product_creation_date, product_last_updated_date, product_last_updated_by, product_status) VALUES ('CRM', 'CRM Application', '1', 1, 'admin', now(), now(), 'admin', 'Active');
INSERT INTO public.products (product_name, product_description,  product_versions, product_id, product_created_by, product_creation_date, product_last_updated_date, product_last_updated_by, product_status) VALUES ('Billing', 'Billing Application', 'DEV, PROD', 2, 'admin', now(), now(), 'admin', 'Active');
INSERT INTO public.products (product_name, product_description,  product_versions, product_id, product_created_by, product_creation_date, product_last_updated_date, product_last_updated_by, product_status) VALUES ('Collection', 'Collection Application', '1', 3, 'admin', now(), now(), 'admin', 'Active');
INSERT INTO public.products (product_name, product_description,  product_versions, product_id, product_created_by, product_creation_date, product_last_updated_date, product_last_updated_by, product_status) VALUES ('Ordering', 'Ordering Application', '1', 4, 'admin', now(), now(), 'admin', 'Active');


INSERT INTO public.tdm_be_post_exe_process (process_id, process_name, process_description, be_id, execution_order) VALUES (1, 'postTaskExePrintToLog', 'This is a Post Execution Process Flow.', 1, 1);



-- 28-12-22- Comment the insert to the tdm_general_parameters. This insert is included in the creation scripts of the TDMDB 

-- INSERT INTO public.tdm_general_parameters (param_name, param_value) VALUES ('cleanup_retention_period', '2');
-- INSERT INTO public.tdm_general_parameters (param_name, param_value) VALUES ('tdm_gui_params', '{"maxRetentionPeriod":90,"defaultPeriod":{"unit":"Days","value":5},"permissionGroups":["admin","owner","tester"],"availableOptions":[{"name":"Minutes","units":0.00069444444},{"name":"Hours","units":0.04166666666},{"name":"Days","units":1},{"name":"Weeks","units":7},{"name":"Years","units":365}]}');
-- INSERT INTO public.tdm_general_parameters (param_name, param_value) VALUES ('MAX_RESERVATION_DAYS_FOR_TESTER', '10');
-- INSERT INTO public.tdm_general_parameters (param_name, param_value) VALUES ('TDM_VERSION', '7.5.2');




--
-- TOC entry 3300 (class 0 OID 0)
-- Dependencies: 201
-- Name: business_entities_be_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tdm
--

SELECT pg_catalog.setval('public.business_entities_be_id_seq', 1, true);


--
-- TOC entry 3301 (class 0 OID 0)
-- Dependencies: 203
-- Name: data_centers_data_center_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tdm
--

SELECT pg_catalog.setval('public.data_centers_data_center_id_seq', 1, false);


--
-- TOC entry 3302 (class 0 OID 0)
-- Dependencies: 205
-- Name: environment_product_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tdm
--

SELECT pg_catalog.setval('public.environment_product_id_seq', 2, true);


--
-- TOC entry 3303 (class 0 OID 0)
-- Dependencies: 208
-- Name: environment_roles_role_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tdm
--

SELECT pg_catalog.setval('public.environment_roles_role_id_seq', 2, true);


--
-- TOC entry 3304 (class 0 OID 0)
-- Dependencies: 210
-- Name: environments_environment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tdm
--

SELECT pg_catalog.setval('public.environments_environment_id_seq', 2, true);


--
-- TOC entry 3305 (class 0 OID 0)
-- Dependencies: 215
-- Name: post_exe_process_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tdm
--

SELECT pg_catalog.setval('public.post_exe_process_id_seq', 1, true);


--
-- TOC entry 3306 (class 0 OID 0)
-- Dependencies: 216
-- Name: product_interfaces_interface_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tdm
--

SELECT pg_catalog.setval('public.product_interfaces_interface_id_seq', 1, false);


--
-- TOC entry 3307 (class 0 OID 0)
-- Dependencies: 217
-- Name: product_logical_units_lu_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tdm
--

SELECT pg_catalog.setval('public.product_logical_units_lu_id_seq', 3, true);


--
-- TOC entry 3308 (class 0 OID 0)
-- Dependencies: 219
-- Name: products_product_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tdm
--

SELECT pg_catalog.setval('public.products_product_id_seq', 1, true);


--
-- TOC entry 3309 (class 0 OID 0)
-- Dependencies: 221
-- Name: source_environment_roles_role_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tdm
--

SELECT pg_catalog.setval('public.source_environment_roles_role_id_seq', 21, false);


--
-- TOC entry 3310 (class 0 OID 0)
-- Dependencies: 222
-- Name: source_environments_source_environment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tdm
--

SELECT pg_catalog.setval('public.source_environments_source_environment_id_seq', 1, false);


--
-- TOC entry 3311 (class 0 OID 0)
-- Dependencies: 233
-- Name: tasks_ref_table_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tdm
--

SELECT pg_catalog.setval('public.tasks_ref_table_id_seq', 1, false);


--
-- TOC entry 3312 (class 0 OID 0)
-- Dependencies: 239
-- Name: tasks_task_execution_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tdm
--

SELECT pg_catalog.setval('public.tasks_task_execution_id_seq', 1, false);


--
-- TOC entry 3313 (class 0 OID 0)
-- Dependencies: 235
-- Name: tasks_task_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tdm
--

SELECT pg_catalog.setval('public.tasks_task_id_seq', 1, false);


--
-- TOC entry 3314 (class 0 OID 0)
-- Dependencies: 240
-- Name: tdm_be_env_exclusion_list_be_env_exclusion_list_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tdm
--

SELECT pg_catalog.setval('public.tdm_be_env_exclusion_list_be_env_exclusion_list_id_seq', 1, false);


-- Completed on 2022-12-13 14:12:22

--
-- PostgreSQL database dump complete
--

