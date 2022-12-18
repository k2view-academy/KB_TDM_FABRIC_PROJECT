
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
SET default_tablespace = '';
SET default_table_access_method = heap;



CREATE TABLE public.collection (
    customer_id bigint,
    collection_id bigint NOT NULL,
    last_update date,
    collection_status character varying(200)
);


--ALTER TABLE public.collection OWNER TO "TAR_COLLECTION_USER";
ALTER TABLE public.collection OWNER TO "TAR_COLLECTION_USER";


ALTER TABLE ONLY public.collection
    ADD CONSTRAINT collection_pk PRIMARY KEY (collection_id);

