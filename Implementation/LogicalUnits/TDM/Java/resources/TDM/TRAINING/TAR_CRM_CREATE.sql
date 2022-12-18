--
-- PostgreSQL database dump
--


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
-- TOC entry 200 (class 1259 OID 16651)
-- Name: activity; Type: TABLE; Schema: public; Owner: TAR_CRM_USER
--

CREATE TABLE public.activity (
    customer_id bigint,
    activity_id bigint NOT NULL,
    activity_date timestamp without time zone,
    activity_note character varying(2000)
);


ALTER TABLE public.activity OWNER TO "TAR_CRM_USER";

--
-- TOC entry 201 (class 1259 OID 16657)
-- Name: address; Type: TABLE; Schema: public; Owner: TAR_CRM_USER
--

CREATE TABLE public.address (
    customer_id bigint,
    address_id bigint NOT NULL,
    street_address_1 character varying(200),
    street_address_2 character varying(200),
    city character varying(200),
    zip character varying(200),
    state character varying(200),
    country character varying(200)
);


ALTER TABLE public.address OWNER TO "TAR_CRM_USER";

--
-- TOC entry 202 (class 1259 OID 16663)
-- Name: case_note; Type: TABLE; Schema: public; Owner: TAR_CRM_USER
--

CREATE TABLE public.case_note (
    case_id bigint,
    note_id bigint NOT NULL,
    note_date timestamp without time zone,
    note_text character varying(3000)
);


ALTER TABLE public.case_note OWNER TO "TAR_CRM_USER";

--
-- TOC entry 203 (class 1259 OID 16669)
-- Name: cases; Type: TABLE; Schema: public; Owner: TAR_CRM_USER
--

CREATE TABLE public.cases (
    activity_id bigint,
    case_id bigint NOT NULL,
    case_date timestamp without time zone,
    case_type character varying(200),
    status character varying(200)
);


ALTER TABLE public.cases OWNER TO "TAR_CRM_USER";

--
-- TOC entry 204 (class 1259 OID 16672)
-- Name: contract; Type: TABLE; Schema: public; Owner: TAR_CRM_USER
--

CREATE TABLE public.contract (
    customer_id bigint,
    contract_id bigint NOT NULL,
    contract_ref_id bigint,
    associated_line character varying(200),
    contract_description character varying(200),
    from_date timestamp without time zone,
    to_date timestamp without time zone,
    associated_line_fmt character varying(200)
);


ALTER TABLE public.contract OWNER TO "TAR_CRM_USER";

--
-- TOC entry 205 (class 1259 OID 16678)
-- Name: customer; Type: TABLE; Schema: public; Owner: TAR_CRM_USER
--

CREATE TABLE public.customer (
    customer_id bigint NOT NULL,
    ssn character varying(20),
    first_name character varying(200),
    last_name character varying(200)
);


ALTER TABLE public.customer OWNER TO "TAR_CRM_USER";

--
-- TOC entry 206 (class 1259 OID 16681)
-- Name: devicestable2017; Type: TABLE; Schema: public; Owner: TAR_CRM_USER
--

CREATE TABLE public.devicestable2017 (
    tac numeric,
    brandmodel character varying(300),
    manufacturer character varying(300),
    frequencies_band character varying(4000),
    radiointerface character varying(300),
    brandname character varying(200),
    modelname character varying(300),
    os character varying(200),
    chipnfc character varying(200),
    chipbluetooth character varying(200),
    chipwlan character varying(200),
    devicetype character varying(300),
    removableuicc character varying(200),
    removableeuicc character varying(200),
    nonremovableuicc character varying(200),
    nonremovableeuicc character varying(200),
    deviceatlasid character varying(50),
    standardisedfull character varying(300),
    standardised_device_model character varying(200),
    standardisedmarketing character varying(300),
    interlmodel character varying(300),
    allocationdate timestamp without time zone,
    countrycode numeric,
    fixedcode character varying(200),
    manufacturercode character varying(50),
    year_released character varying(50),
    mobile_device numeric,
    hardwaretype character varying(200),
    touchscreen numeric,
    screenwidth numeric,
    screenheight numeric,
    diagolscreensize numeric,
    displayppi numeric,
    devicepixelratio numeric,
    screencolordepth numeric,
    nfc numeric,
    camera numeric,
    ismobilephone numeric,
    istablet numeric,
    isereader numeric,
    isgamesconsole numeric,
    istv numeric,
    issettupbox numeric,
    ismediaplayer numeric,
    chipsetvendor character varying(200),
    chipset character varying(200),
    chipsetmodel character varying(200),
    cpu character varying(200),
    cpu_cores character varying(200),
    cpu_maximum_frequency character varying(200),
    gpu character varying(200),
    sim_slots numeric,
    sim_size character varying(200),
    interl_storage_capacity character varying(200),
    expandable_storage numeric,
    total_ram character varying(200),
    os_me character varying(200),
    os_version character varying(200),
    os_android numeric,
    os_bada numeric,
    os_ios numeric,
    os_rim numeric,
    os_symbian numeric,
    os_windows_mobile numeric,
    os_windows_phone numeric,
    os_windows_rt numeric,
    os_web_os numeric,
    browser_me character varying(200),
    browser_version character varying(200),
    browser_rendering_engine character varying(200),
    markup_xhtml_basic_1v0 numeric,
    markup_xhtml_mp_1v0 numeric,
    markup_xhtml_mp_1v1 numeric,
    markup_xhtml_mp_1v2 numeric,
    markup_wml1 numeric,
    vcard_download numeric,
    image__gif87 numeric,
    image__gif89a numeric,
    image__jpg numeric,
    image__png numeric,
    uri_scheme_tel numeric,
    uri_scheme_sms numeric,
    uri_scheme_sms_to numeric,
    cookie numeric,
    https numeric,
    memory_limit_markup numeric,
    memory_limit_embedded_media numeric,
    memory_limit_download numeric,
    flash_capable numeric,
    js_support_basic_java_script numeric,
    js_modify_dom numeric,
    js_modify_css numeric,
    js_support_events numeric,
    js_support_event_listener numeric,
    js_xhr numeric,
    js_support_console_log numeric,
    js_json numeric,
    supports_client_side numeric,
    csd numeric,
    hscsd numeric,
    gprs numeric,
    edge numeric,
    hsdpa numeric,
    umts numeric,
    hspa numeric,
    lte numeric,
    lte_category numeric,
    lte_advanced numeric,
    volte numeric,
    wifi numeric,
    vowifi numeric,
    html_audio numeric,
    html_canvas numeric,
    html_inline_svg numeric,
    html_svg numeric,
    html_video numeric,
    css_animations numeric,
    css_columns numeric,
    css_transforms numeric,
    css_transitions numeric,
    js_application_cache numeric,
    js_geo_location numeric,
    js_indexeddb numeric,
    js_local_storage numeric,
    js_session_storage numeric,
    js_web_gl numeric,
    js_web_sockets numeric,
    js_web_sql_database numeric,
    js_web_workers numeric,
    js_device_orientation numeric,
    js_device_motion numeric,
    js_touch_events numeric,
    js_query_selector numeric,
    wmv numeric,
    midi_monophonic numeric,
    midi_polyphonic numeric,
    amr numeric,
    mp3 numeric,
    aac numeric
);


ALTER TABLE public.devicestable2017 OWNER TO "TAR_CRM_USER";

--
-- TOC entry 2878 (class 2606 OID 16688)
-- Name: activity activity_pk; Type: CONSTRAINT; Schema: public; Owner: TAR_CRM_USER
--

ALTER TABLE ONLY public.activity
    ADD CONSTRAINT activity_pk PRIMARY KEY (activity_id);


--
-- TOC entry 2880 (class 2606 OID 16690)
-- Name: address address_pk; Type: CONSTRAINT; Schema: public; Owner: TAR_CRM_USER
--

ALTER TABLE ONLY public.address
    ADD CONSTRAINT address_pk PRIMARY KEY (address_id);


--
-- TOC entry 2884 (class 2606 OID 16692)
-- Name: cases case_pk; Type: CONSTRAINT; Schema: public; Owner: TAR_CRM_USER
--

ALTER TABLE ONLY public.cases
    ADD CONSTRAINT case_pk PRIMARY KEY (case_id);


--
-- TOC entry 2886 (class 2606 OID 16694)
-- Name: contract contract_pk; Type: CONSTRAINT; Schema: public; Owner: TAR_CRM_USER
--

ALTER TABLE ONLY public.contract
    ADD CONSTRAINT contract_pk PRIMARY KEY (contract_id);


--
-- TOC entry 2888 (class 2606 OID 16696)
-- Name: customer customer_pk; Type: CONSTRAINT; Schema: public; Owner: TAR_CRM_USER
--

ALTER TABLE ONLY public.customer
    ADD CONSTRAINT customer_pk PRIMARY KEY (customer_id);


--
-- TOC entry 2882 (class 2606 OID 16698)
-- Name: case_note note_pk; Type: CONSTRAINT; Schema: public; Owner: TAR_CRM_USER
--

ALTER TABLE ONLY public.case_note
    ADD CONSTRAINT note_pk PRIMARY KEY (note_id);


--
-- TOC entry 2889 (class 2606 OID 16699)
-- Name: activity activity_fk1; Type: FK CONSTRAINT; Schema: public; Owner: TAR_CRM_USER
--

ALTER TABLE ONLY public.activity
    ADD CONSTRAINT activity_fk1 FOREIGN KEY (customer_id) REFERENCES public.customer(customer_id);


--
-- TOC entry 2890 (class 2606 OID 16704)
-- Name: address address_fk1; Type: FK CONSTRAINT; Schema: public; Owner: TAR_CRM_USER
--

ALTER TABLE ONLY public.address
    ADD CONSTRAINT address_fk1 FOREIGN KEY (customer_id) REFERENCES public.customer(customer_id);


--
-- TOC entry 2892 (class 2606 OID 16709)
-- Name: cases case_fk1; Type: FK CONSTRAINT; Schema: public; Owner: TAR_CRM_USER
--

ALTER TABLE ONLY public.cases
    ADD CONSTRAINT case_fk1 FOREIGN KEY (activity_id) REFERENCES public.activity(activity_id);


--
-- TOC entry 2891 (class 2606 OID 16714)
-- Name: case_note case_note_fk1; Type: FK CONSTRAINT; Schema: public; Owner: TAR_CRM_USER
--

ALTER TABLE ONLY public.case_note
    ADD CONSTRAINT case_note_fk1 FOREIGN KEY (case_id) REFERENCES public.cases(case_id);


--
-- TOC entry 2893 (class 2606 OID 16719)
-- Name: contract contract_fk1; Type: FK CONSTRAINT; Schema: public; Owner: TAR_CRM_USER
--

ALTER TABLE ONLY public.contract
    ADD CONSTRAINT contract_fk1 FOREIGN KEY (customer_id) REFERENCES public.customer(customer_id);


-- Completed on 2022-12-08 17:44:43

--
-- PostgreSQL database dump complete
--

