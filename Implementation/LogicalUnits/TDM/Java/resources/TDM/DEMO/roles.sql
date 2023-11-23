do
$$
begin
  if not exists (select * from pg_user where usename = 'billing_prod') then
     CREATE ROLE "billing_prod" LOGIN PASSWORD 'billing_prod' SUPERUSER INHERIT CREATEDB CREATEROLE REPLICATION;
  end if;
  if not exists (select * from pg_user where usename = 'tar_billing_prod') then
     CREATE ROLE "tar_billing_prod" LOGIN PASSWORD 'tar_billing_prod' SUPERUSER INHERIT CREATEDB CREATEROLE REPLICATION;
  end if;
end
$$
;