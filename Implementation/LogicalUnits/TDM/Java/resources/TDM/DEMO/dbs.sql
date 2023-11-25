
--
-- PostgreSQL database dump
--

-- Dumped from database version 13.6
-- Dumped by pg_dump version 13.6

-- Started on 2022-12-08 17:43:41

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

CREATE DATABASE "BILLING_DB" WITH TEMPLATE = template0 ENCODING = 'UTF8' CONNECTION LIMIT = -1;
ALTER DATABASE "BILLING_DB" OWNER TO "billing_prod";

CREATE DATABASE "TAR_BILLING_DB" WITH TEMPLATE = template0 ENCODING = 'UTF8' CONNECTION LIMIT = -1;
ALTER DATABASE "TAR_BILLING_DB" OWNER TO "tar_billing_prod";