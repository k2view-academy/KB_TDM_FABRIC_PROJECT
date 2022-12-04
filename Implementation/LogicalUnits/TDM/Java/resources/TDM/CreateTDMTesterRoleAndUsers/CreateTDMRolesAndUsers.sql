create role 'Testers' description "The testers role";
grant ALL_WS to Testers;
create USER IF NOT EXISTS 'tester1' WITH PASSWORD '123';
create USER IF NOT EXISTS 'tester2' WITH PASSWORD '123';
assign role Testers to user tester1;
assign role Testers to user tester2;
