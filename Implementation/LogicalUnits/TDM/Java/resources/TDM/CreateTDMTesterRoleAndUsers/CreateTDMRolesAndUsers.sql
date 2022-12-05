create role 'Testersgrp1' description "This is a Tester Role";
grant ALL_WS ON * to Testersgrp1;
grant READ ON * to Testersgrp1;
grant WRITE ON * to Testersgrp1;
create USER IF NOT EXISTS 'tester1' WITH PASSWORD 't1';
create USER IF NOT EXISTS 'tester2' WITH PASSWORD 't2';
assign role Testersgrp1 to user tester1;
assign role Testersgrp1 to user tester2;
