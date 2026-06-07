CREATE ROLE 'TDM Environment Owner & Task Creator' description "Owner privileges";
CREATE ROLE 'TDM Task Executor' description "Tester privileges";
GRANT ALL ON * TO 'TDM Environment Owner & Task Creator';
GRANT ALL_WS ON * TO 'TDM Environment Owner & Task Creator';
GRANT ALL ON * TO 'TDM Task Executor';
GRANT ALL_WS ON * TO 'TDM Task Executor';