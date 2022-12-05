insert into public.permission_groups_mapping (
	description,
	fabric_role,
	permission_group,
	created_by,
	updated_by,
	creation_date,
	update_date
) values ('Initial mapping for the Testers role', 'Testers', 'tester', 'admin', 'admin', NOW(), NOW());