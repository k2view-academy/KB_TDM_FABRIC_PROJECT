


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
-- TOC entry 200 (class 1259 OID 16410)
-- Name: balance; Type: TABLE; Schema: public; Owner: BILLING_USER
--

CREATE TABLE public.balance (
    subscriber_id bigint,
    balance_id bigint NOT NULL,
    balance_ref_id bigint,
    available_amount bigint,
    reset_amount bigint,
    reset_date timestamp without time zone
);


ALTER TABLE public.balance OWNER TO "BILLING_USER";

--
-- TOC entry 202 (class 1259 OID 16417)
-- Name: invoice; Type: TABLE; Schema: public; Owner: BILLING_USER
--

CREATE TABLE public.invoice (
    subscriber_id bigint,
    invoice_id bigint NOT NULL,
    issued_date timestamp without time zone,
    due_date timestamp without time zone,
    status character varying(2000) DEFAULT NULL::character varying,
    balance bigint,
    invoice_image bytea
);


ALTER TABLE public.invoice OWNER TO "BILLING_USER";

--
-- TOC entry 203 (class 1259 OID 16424)
-- Name: offer; Type: TABLE; Schema: public; Owner: BILLING_USER
--

CREATE TABLE public.offer (
    subscriber_id bigint,
    offer_id bigint NOT NULL,
    offer_ref_id bigint,
    offer_description character varying(200) DEFAULT NULL::character varying,
    from_date timestamp without time zone,
    to_date timestamp without time zone
);


ALTER TABLE public.offer OWNER TO "BILLING_USER";

--
-- TOC entry 204 (class 1259 OID 16428)
-- Name: payment; Type: TABLE; Schema: public; Owner: BILLING_USER
--

CREATE TABLE public.payment (
    invoice_id bigint,
    payment_id bigint NOT NULL,
    issued_date timestamp without time zone,
    status character varying(200) DEFAULT NULL::character varying,
    amount character varying(200) DEFAULT NULL::character varying
);


ALTER TABLE public.payment OWNER TO "BILLING_USER";

--
-- TOC entry 206 (class 1259 OID 16439)
-- Name: subscriber; Type: TABLE; Schema: public; Owner: BILLING_USER
--

CREATE TABLE public.subscriber (
    subscriber_id bigint NOT NULL,
    msisdn character varying(200) DEFAULT NULL::character varying,
    imsi character varying(200) DEFAULT NULL::character varying,
    sim character varying(200) DEFAULT NULL::character varying,
    first_name character varying(200) DEFAULT NULL::character varying,
    last_name character varying(200) DEFAULT NULL::character varying,
    subscriber_type character varying(200) DEFAULT NULL::character varying,
    vip_status character varying(200) DEFAULT NULL::character varying
);


ALTER TABLE public.subscriber OWNER TO "BILLING_USER";

--
-- TOC entry 2888 (class 2606 OID 16453)
-- Name: balance balance_pkey; Type: CONSTRAINT; Schema: public; Owner: BILLING_USER
--

ALTER TABLE ONLY public.balance
    ADD CONSTRAINT balance_pkey PRIMARY KEY (balance_id);


--
-- TOC entry 2890 (class 2606 OID 16455)
-- Name: invoice invoice_pkey; Type: CONSTRAINT; Schema: public; Owner: BILLING_USER
--

ALTER TABLE ONLY public.invoice
    ADD CONSTRAINT invoice_pkey PRIMARY KEY (invoice_id);


--
-- TOC entry 2892 (class 2606 OID 16457)
-- Name: offer offer_pkey; Type: CONSTRAINT; Schema: public; Owner: BILLING_USER
--

ALTER TABLE ONLY public.offer
    ADD CONSTRAINT offer_pkey PRIMARY KEY (offer_id);


--
-- TOC entry 2894 (class 2606 OID 16459)
-- Name: subscriber subscriber_pkey; Type: CONSTRAINT; Schema: public; Owner: BILLING_USER
--

ALTER TABLE ONLY public.subscriber
    ADD CONSTRAINT subscriber_pkey PRIMARY KEY (subscriber_id);


--
-- TOC entry 2895 (class 2606 OID 16460)
-- Name: balance balance_fk1; Type: FK CONSTRAINT; Schema: public; Owner: BILLING_USER
--

ALTER TABLE ONLY public.balance
    ADD CONSTRAINT balance_fk1 FOREIGN KEY (subscriber_id) REFERENCES public.subscriber(subscriber_id);


--
-- TOC entry 2896 (class 2606 OID 16465)
-- Name: invoice invoice_fk1; Type: FK CONSTRAINT; Schema: public; Owner: BILLING_USER
--

ALTER TABLE ONLY public.invoice
    ADD CONSTRAINT invoice_fk1 FOREIGN KEY (subscriber_id) REFERENCES public.subscriber(subscriber_id);


--
-- TOC entry 2897 (class 2606 OID 16470)
-- Name: offer offer_fk1; Type: FK CONSTRAINT; Schema: public; Owner: BILLING_USER
--

ALTER TABLE ONLY public.offer
    ADD CONSTRAINT offer_fk1 FOREIGN KEY (subscriber_id) REFERENCES public.subscriber(subscriber_id);


--
-- TOC entry 2898 (class 2606 OID 16475)
-- Name: payment payment_fk1; Type: FK CONSTRAINT; Schema: public; Owner: BILLING_USER
--

ALTER TABLE ONLY public.payment
    ADD CONSTRAINT payment_fk1 FOREIGN KEY (invoice_id) REFERENCES public.invoice(invoice_id);


-- Completed on 2022-12-08 17:41:58

--
-- PostgreSQL database dump complete
--

