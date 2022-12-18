


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

--
-- TOC entry 200 (class 1259 OID 16562)
-- Name: orders; Type: TABLE; Schema: public; Owner: ORDERS_USER
--

CREATE TABLE public.orders (
    contract_id bigint,
    order_id bigint NOT NULL,
    order_type character varying(200),
    order_date date,
    order_status character varying(200)
);


--ALTER TABLE public.orders OWNER TO "ORDERING_USER";
ALTER TABLE public.orders OWNER TO "ORDERS_USER";

--
-- TOC entry 2849 (class 2606 OID 16566)
-- Name: orders orders_pk; Type: CONSTRAINT; Schema: public; Owner: ORDERS_USER
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_pk PRIMARY KEY (order_id);


-- Completed on 2022-12-08 17:43:19

--
-- PostgreSQL database dump complete
--

