
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



CREATE TABLE public.balance (
    subscriber_id bigint,
    balance_id bigint NOT NULL,
    balance_ref_id bigint,
    available_amount bigint,
    reset_amount bigint,
    reset_date timestamp without time zone
);


ALTER TABLE public.balance OWNER TO "TAR_BILLING_USER";



CREATE TABLE public.contract_offer_mapping (
    contract_ref_id bigint,
    offer_ref_id bigint,
    offer_contract_description character varying(200) DEFAULT NULL::character varying
);


ALTER TABLE public.contract_offer_mapping OWNER TO "TAR_BILLING_USER";

--
-- TOC entry 202 (class 1259 OID 16574)
-- Name: invoice; Type: TABLE; Schema: public; Owner: TAR_BILLING_USER
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


ALTER TABLE public.invoice OWNER TO "TAR_BILLING_USER";

--
-- TOC entry 203 (class 1259 OID 16581)
-- Name: offer; Type: TABLE; Schema: public; Owner: TAR_BILLING_USER
--

CREATE TABLE public.offer (
    subscriber_id bigint,
    offer_id bigint NOT NULL,
    offer_ref_id bigint,
    offer_description character varying(200) DEFAULT NULL::character varying,
    from_date timestamp without time zone,
    to_date timestamp without time zone
);


ALTER TABLE public.offer OWNER TO "TAR_BILLING_USER";

--
-- TOC entry 204 (class 1259 OID 16585)
-- Name: payment; Type: TABLE; Schema: public; Owner: TAR_BILLING_USER
--

CREATE TABLE public.payment (
    invoice_id bigint,
    payment_id bigint NOT NULL,
    issued_date timestamp without time zone,
    status character varying(200) DEFAULT NULL::character varying,
    amount character varying(200) DEFAULT NULL::character varying,
    payment_method character varying(200)
);


ALTER TABLE public.payment OWNER TO "TAR_BILLING_USER";

--
-- TOC entry 205 (class 1259 OID 16593)
-- Name: simtodevice; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.simtodevice (
    msisdn text,
    imei1 text,
    tac1 text,
    reminder1 text,
    imei2 text,
    tac2 text,
    reminder2 text,
    imei3 text,
    tac3 text,
    reminder3 text,
    imei4 text,
    tac4 text,
    reminder4 text
);


ALTER TABLE public.simtodevice OWNER TO postgres;

--
-- TOC entry 206 (class 1259 OID 16599)
-- Name: subscriber; Type: TABLE; Schema: public; Owner: TAR_BILLING_USER
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


ALTER TABLE public.subscriber OWNER TO "TAR_BILLING_USER";

--
-- TOC entry 2889 (class 2606 OID 16613)
-- Name: balance balance_pkey; Type: CONSTRAINT; Schema: public; Owner: TAR_BILLING_USER
--

ALTER TABLE ONLY public.balance
    ADD CONSTRAINT balance_pkey PRIMARY KEY (balance_id);


--
-- TOC entry 2891 (class 2606 OID 16615)
-- Name: invoice invoice_pkey; Type: CONSTRAINT; Schema: public; Owner: TAR_BILLING_USER
--

ALTER TABLE ONLY public.invoice
    ADD CONSTRAINT invoice_pkey PRIMARY KEY (invoice_id);


--
-- TOC entry 2893 (class 2606 OID 16617)
-- Name: offer offer_pkey; Type: CONSTRAINT; Schema: public; Owner: TAR_BILLING_USER
--

ALTER TABLE ONLY public.offer
    ADD CONSTRAINT offer_pkey PRIMARY KEY (offer_id);


--
-- TOC entry 2895 (class 2606 OID 16619)
-- Name: subscriber subscriber_pkey; Type: CONSTRAINT; Schema: public; Owner: TAR_BILLING_USER
--

ALTER TABLE ONLY public.subscriber
    ADD CONSTRAINT subscriber_pkey PRIMARY KEY (subscriber_id);


--
-- TOC entry 2896 (class 2606 OID 16620)
-- Name: balance balance_fk1; Type: FK CONSTRAINT; Schema: public; Owner: TAR_BILLING_USER
--

ALTER TABLE ONLY public.balance
    ADD CONSTRAINT balance_fk1 FOREIGN KEY (subscriber_id) REFERENCES public.subscriber(subscriber_id);


--
-- TOC entry 2897 (class 2606 OID 16625)
-- Name: invoice invoice_fk1; Type: FK CONSTRAINT; Schema: public; Owner: TAR_BILLING_USER
--

ALTER TABLE ONLY public.invoice
    ADD CONSTRAINT invoice_fk1 FOREIGN KEY (subscriber_id) REFERENCES public.subscriber(subscriber_id);


--
-- TOC entry 2898 (class 2606 OID 16630)
-- Name: offer offer_fk1; Type: FK CONSTRAINT; Schema: public; Owner: TAR_BILLING_USER
--

ALTER TABLE ONLY public.offer
    ADD CONSTRAINT offer_fk1 FOREIGN KEY (subscriber_id) REFERENCES public.subscriber(subscriber_id);


--
-- TOC entry 2899 (class 2606 OID 16635)
-- Name: payment payment_fk1; Type: FK CONSTRAINT; Schema: public; Owner: TAR_BILLING_USER
--

ALTER TABLE ONLY public.payment
    ADD CONSTRAINT payment_fk1 FOREIGN KEY (invoice_id) REFERENCES public.invoice(invoice_id);


-- Completed on 2022-12-08 17:43:42

--
-- PostgreSQL database dump complete
--

