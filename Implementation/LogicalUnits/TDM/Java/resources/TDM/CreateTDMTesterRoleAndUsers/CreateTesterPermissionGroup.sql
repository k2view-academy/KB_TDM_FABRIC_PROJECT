insert into public.permission_groups_mapping (
	description,
	fabric_role,
	permission_group,
	created_by,
	updated_by,
	creation_date,
	update_date
) values ('Initial mapping for tester users', 'Testersgrp1', 'tester', 'admin', 'admin', NOW(), NOW());