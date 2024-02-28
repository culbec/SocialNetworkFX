--
-- PostgreSQL database dump
--

-- Dumped from database version 16.1 (Ubuntu 16.1-1.pgdg22.04+1)
-- Dumped by pg_dump version 16.1 (Ubuntu 16.1-1.pgdg22.04+1)

-- Started on 2024-01-23 11:57:01 EET

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

--
-- TOC entry 2 (class 3079 OID 16491)
-- Name: uuid-ossp; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA public;


--
-- TOC entry 3541 (class 0 OID 0)
-- Dependencies: 2
-- Name: EXTENSION "uuid-ossp"; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION "uuid-ossp" IS 'generate universally unique identifiers (UUIDs)';


--
-- TOC entry 231 (class 1255 OID 16502)
-- Name: generate_friendship_id(uuid, uuid); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.generate_friendship_id(user1 uuid, user2 uuid) RETURNS uuid
    LANGUAGE plpgsql
    AS $$
BEGIN
  -- You can generate a UUID based on user1 and user2 here
  -- For example:
  RETURN MD5(user1::text || user2::text)::UUID;
END;
$$;


ALTER FUNCTION public.generate_friendship_id(user1 uuid, user2 uuid) OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 218 (class 1259 OID 16514)
-- Name: friendrequests; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.friendrequests (
    id_user1 uuid NOT NULL,
    id_user2 uuid NOT NULL,
    status character varying NOT NULL,
    date timestamp without time zone NOT NULL
);


ALTER TABLE public.friendrequests OWNER TO postgres;

--
-- TOC entry 217 (class 1259 OID 16476)
-- Name: friendships; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.friendships (
    id_user1 uuid NOT NULL,
    id_user2 uuid NOT NULL,
    date timestamp without time zone NOT NULL
);


ALTER TABLE public.friendships OWNER TO postgres;

--
-- TOC entry 219 (class 1259 OID 16580)
-- Name: messages; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.messages (
    id uuid NOT NULL,
    message character varying NOT NULL,
    date timestamp without time zone NOT NULL,
    reply_id uuid
);


ALTER TABLE public.messages OWNER TO postgres;

--
-- TOC entry 220 (class 1259 OID 16597)
-- Name: messagesUsers; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public."messagesUsers" (
    id_message uuid NOT NULL,
    id_sender uuid NOT NULL,
    id_receiver uuid NOT NULL
);


ALTER TABLE public."messagesUsers" OWNER TO postgres;

--
-- TOC entry 216 (class 1259 OID 16449)
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    id uuid NOT NULL,
    first_name character varying NOT NULL,
    last_name character varying NOT NULL,
    email character varying NOT NULL,
    password character varying
);


ALTER TABLE public.users OWNER TO postgres;

--
-- TOC entry 3533 (class 0 OID 16514)
-- Dependencies: 218
-- Data for Name: friendrequests; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.friendrequests (id_user1, id_user2, status, date) FROM stdin;
5d37c7bf-1901-4a8b-8cec-660b3064e325	915eab85-7100-462c-9961-90eb87c3f3d9	rejected	2023-12-16 17:18:45.968
5d37c7bf-1901-4a8b-8cec-660b3064e325	915eab85-7100-462c-9961-90eb87c3f3d9	rejected	2023-12-16 17:19:13.386
5d37c7bf-1901-4a8b-8cec-660b3064e325	915eab85-7100-462c-9961-90eb87c3f3d9	rejected	2023-12-16 17:38:20.286
5d37c7bf-1901-4a8b-8cec-660b3064e325	915eab85-7100-462c-9961-90eb87c3f3d9	rejected	2023-12-16 17:40:51.701
5d37c7bf-1901-4a8b-8cec-660b3064e325	915eab85-7100-462c-9961-90eb87c3f3d9	rejected	2023-12-16 17:41:41.042
5d37c7bf-1901-4a8b-8cec-660b3064e325	915eab85-7100-462c-9961-90eb87c3f3d9	rejected	2023-12-16 17:42:29.916
5d37c7bf-1901-4a8b-8cec-660b3064e325	915eab85-7100-462c-9961-90eb87c3f3d9	rejected	2023-12-16 17:43:13.545
5d37c7bf-1901-4a8b-8cec-660b3064e325	915eab85-7100-462c-9961-90eb87c3f3d9	accepted	2023-12-16 17:51:50.732
5d37c7bf-1901-4a8b-8cec-660b3064e325	4c4d5ea5-8d5f-46a6-a1db-5baa6f2ea586	accepted	2023-12-16 17:55:42.998
5d37c7bf-1901-4a8b-8cec-660b3064e325	b68daa0d-e351-4312-9069-e15e8566cc91	accepted	2023-12-16 17:57:20.905
4a3ee63c-802d-48c3-ac22-68d087928a80	915eab85-7100-462c-9961-90eb87c3f3d9	accepted	2024-01-05 19:17:04.064
4a3ee63c-802d-48c3-ac22-68d087928a80	915eab85-7100-462c-9961-90eb87c3f3d9	accepted	2024-01-05 19:19:30.647
4a3ee63c-802d-48c3-ac22-68d087928a80	2c9fdccf-45f4-4d1d-b2e3-33e924cb3e5a	accepted	2024-01-05 19:23:40.243
4a3ee63c-802d-48c3-ac22-68d087928a80	eec69f0e-d8d8-4e5c-8606-489f51f5166e	accepted	2024-01-05 19:26:58.309
4a3ee63c-802d-48c3-ac22-68d087928a80	eec69f0e-d8d8-4e5c-8606-489f51f5166e	accepted	2024-01-05 19:29:03.504
4a3ee63c-802d-48c3-ac22-68d087928a80	eec69f0e-d8d8-4e5c-8606-489f51f5166e	accepted	2024-01-05 19:35:28.231
4a3ee63c-802d-48c3-ac22-68d087928a80	eec69f0e-d8d8-4e5c-8606-489f51f5166e	accepted	2024-01-05 19:37:46.551
4a3ee63c-802d-48c3-ac22-68d087928a80	eec69f0e-d8d8-4e5c-8606-489f51f5166e	accepted	2024-01-05 19:41:38.548
4a3ee63c-802d-48c3-ac22-68d087928a80	eec69f0e-d8d8-4e5c-8606-489f51f5166e	accepted	2024-01-05 19:43:22.594
\.


--
-- TOC entry 3532 (class 0 OID 16476)
-- Dependencies: 217
-- Data for Name: friendships; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.friendships (id_user1, id_user2, date) FROM stdin;
915eab85-7100-462c-9961-90eb87c3f3d9	4c4d5ea5-8d5f-46a6-a1db-5baa6f2ea586	2023-12-16 16:04:29.030905
915eab85-7100-462c-9961-90eb87c3f3d9	b68daa0d-e351-4312-9069-e15e8566cc91	2023-12-16 16:04:37.294935
915eab85-7100-462c-9961-90eb87c3f3d9	2c9fdccf-45f4-4d1d-b2e3-33e924cb3e5a	2023-12-16 16:04:41.483472
915eab85-7100-462c-9961-90eb87c3f3d9	99280656-fe59-4748-969a-3d4c31fcc260	2023-12-16 16:04:45.641767
4c4d5ea5-8d5f-46a6-a1db-5baa6f2ea586	99280656-fe59-4748-969a-3d4c31fcc260	2023-12-16 16:04:49.810798
4c4d5ea5-8d5f-46a6-a1db-5baa6f2ea586	8b8bc550-a708-4024-8f05-d549c7a91c0a	2023-12-16 16:04:53.947153
4c4d5ea5-8d5f-46a6-a1db-5baa6f2ea586	241f6a9e-d3fc-4ce7-9a79-b5869e9a0a60	2023-12-16 16:04:58.122979
2c9fdccf-45f4-4d1d-b2e3-33e924cb3e5a	b68daa0d-e351-4312-9069-e15e8566cc91	2023-12-16 16:05:06.409916
5d37c7bf-1901-4a8b-8cec-660b3064e325	b68daa0d-e351-4312-9069-e15e8566cc91	2023-12-16 17:57:24.222427
4a3ee63c-802d-48c3-ac22-68d087928a80	915eab85-7100-462c-9961-90eb87c3f3d9	2024-01-05 19:19:32.77789
4a3ee63c-802d-48c3-ac22-68d087928a80	2c9fdccf-45f4-4d1d-b2e3-33e924cb3e5a	2024-01-05 19:23:42.605217
\.


--
-- TOC entry 3534 (class 0 OID 16580)
-- Dependencies: 219
-- Data for Name: messages; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.messages (id, message, date, reply_id) FROM stdin;
35e4cf7d-9379-421d-b458-29885cc81bde	adasda	2023-12-05 14:39:45.514	\N
4ce3e6c3-0f11-47bd-9f2c-d13fa8884d34	samp	2023-12-05 14:40:01.996	35e4cf7d-9379-421d-b458-29885cc81bde
14b99412-30e1-470c-9b6d-f17276c36e6f	aa	2023-12-05 14:40:14.7	4ce3e6c3-0f11-47bd-9f2c-d13fa8884d34
82b1370b-cca6-4d09-9d6f-de355fda308d	asdasda	2023-12-05 16:00:19.035	\N
0924c873-67cf-45c8-a333-1450241dd91d	sal.	2023-12-05 16:00:50.425	35e4cf7d-9379-421d-b458-29885cc81bde
4678d13a-5c8a-44d2-9e25-f7eb566c4565	yahoo	2023-12-05 16:14:37.337	\N
708b500c-9d5c-430b-a461-bb4984f2089d	kaak	2023-12-05 16:17:08.365	4ce3e6c3-0f11-47bd-9f2c-d13fa8884d34
2a769b58-9840-4d2c-a6f3-c9e85a20385f	kaak	2023-12-05 16:17:11.316	4ce3e6c3-0f11-47bd-9f2c-d13fa8884d34
112b8c21-250a-419a-bf24-c1febe8cb231	kaak	2023-12-05 16:17:14.18	82b1370b-cca6-4d09-9d6f-de355fda308d
9e66dceb-b2a8-4384-8a6c-4dbd3f6885f3	ada	2023-12-05 16:18:06.317	112b8c21-250a-419a-bf24-c1febe8cb231
8152f1b5-4e68-43c2-9491-9a5e6a837f90	ada	2023-12-05 16:18:10.852	14b99412-30e1-470c-9b6d-f17276c36e6f
8c55ac3c-d983-40e7-a08a-d494ad3a04f2	reply la mesaj	2023-12-05 16:19:24.745	35e4cf7d-9379-421d-b458-29885cc81bde
06b17e05-e605-4e00-86cb-666f9ea18bf0	mesaj	2023-12-05 16:20:23.886	\N
4dccd28c-d9d6-4a8d-8082-abc204d5b0ca	salut	2023-12-05 17:08:27.282	\N
e61d2d12-ab96-43af-9d4e-047181d026b6	salut x2	2023-12-05 17:09:27.275	4dccd28c-d9d6-4a8d-8082-abc204d5b0ca
d91c953b-0c33-463e-8190-e8d1d6ab3d5a	salut la mai multi	2023-12-05 17:10:12.568	\N
9f82a81f-f3a2-4653-98fe-0e88a40210e0	samp	2023-12-16 14:34:45.011	\N
72b94aed-aa95-49d2-9c7b-1641a7de32d0	Salut, de la Eusebiu!	2023-12-16 18:20:17.455	\N
03626907-f6eb-410d-803c-9d780e5d51f8	Salut, iar!	2023-12-16 18:58:09.56	\N
2621e748-b164-4f4b-abdb-3028973b7660	Salut, din nou!	2023-12-16 19:07:15.707	72b94aed-aa95-49d2-9c7b-1641a7de32d0
38e38e28-86be-45d6-84ae-5f72508ffec9	Buna!	2023-12-16 19:08:18.466	03626907-f6eb-410d-803c-9d780e5d51f8
976f43ec-540f-4008-b691-3e555eadbc6d	test	2023-12-16 19:15:52.117	\N
178ef05e-b33c-4606-8b23-43ab56d061db	salut	2023-12-18 16:01:23.205	\N
f91d25b2-8b49-4893-b966-97d996bb5846	salut iar	2023-12-18 16:01:28.76	\N
3a0c3fc2-76f8-452c-a31a-0e1c75d5d4c5	salut	2024-01-05 19:16:11.832	\N
3346adc4-2557-43df-93dd-5af1074dbe16	salut, eusebiu	2024-01-05 19:16:19.733	\N
55709637-eecd-40f1-97fb-39676a4ebfde	salut	2024-01-08 16:50:25.849	\N
f2bc133e-5339-4428-9f19-0cccfede1e25	salut	2024-01-08 16:50:31.204	55709637-eecd-40f1-97fb-39676a4ebfde
4733544b-a657-435f-9b67-7c783497c0d2	salut	2024-01-09 15:53:59.943	3346adc4-2557-43df-93dd-5af1074dbe16
eaa216e5-0782-4a73-8c74-731e467d9f39	reply la test	2024-01-09 16:03:18.988	976f43ec-540f-4008-b691-3e555eadbc6d
4ba7eeca-6cf1-460e-8167-c6802fe9bfc4	salut la salut	2024-01-09 16:03:33.012	3346adc4-2557-43df-93dd-5af1074dbe16
a3fe6cef-5e00-4cd7-8c0e-89ef6ecddf02	1234	2024-01-09 16:06:49.052	4733544b-a657-435f-9b67-7c783497c0d2
7c4bf5f5-bf02-4899-a389-c66dde498bf9	salut la salut propriu	2024-01-09 16:15:29.324	4733544b-a657-435f-9b67-7c783497c0d2
78ba5bd0-caf1-4930-9c8f-7a85f96e2126	reply la test	2024-01-09 16:15:42.032	976f43ec-540f-4008-b691-3e555eadbc6d
\.


--
-- TOC entry 3535 (class 0 OID 16597)
-- Dependencies: 220
-- Data for Name: messagesUsers; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public."messagesUsers" (id_message, id_sender, id_receiver) FROM stdin;
72b94aed-aa95-49d2-9c7b-1641a7de32d0	915eab85-7100-462c-9961-90eb87c3f3d9	b68daa0d-e351-4312-9069-e15e8566cc91
72b94aed-aa95-49d2-9c7b-1641a7de32d0	915eab85-7100-462c-9961-90eb87c3f3d9	2c9fdccf-45f4-4d1d-b2e3-33e924cb3e5a
72b94aed-aa95-49d2-9c7b-1641a7de32d0	915eab85-7100-462c-9961-90eb87c3f3d9	99280656-fe59-4748-969a-3d4c31fcc260
72b94aed-aa95-49d2-9c7b-1641a7de32d0	915eab85-7100-462c-9961-90eb87c3f3d9	8b8bc550-a708-4024-8f05-d549c7a91c0a
03626907-f6eb-410d-803c-9d780e5d51f8	915eab85-7100-462c-9961-90eb87c3f3d9	99280656-fe59-4748-969a-3d4c31fcc260
2621e748-b164-4f4b-abdb-3028973b7660	915eab85-7100-462c-9961-90eb87c3f3d9	915eab85-7100-462c-9961-90eb87c3f3d9
38e38e28-86be-45d6-84ae-5f72508ffec9	915eab85-7100-462c-9961-90eb87c3f3d9	915eab85-7100-462c-9961-90eb87c3f3d9
976f43ec-540f-4008-b691-3e555eadbc6d	915eab85-7100-462c-9961-90eb87c3f3d9	99280656-fe59-4748-969a-3d4c31fcc260
178ef05e-b33c-4606-8b23-43ab56d061db	b68daa0d-e351-4312-9069-e15e8566cc91	915eab85-7100-462c-9961-90eb87c3f3d9
f91d25b2-8b49-4893-b966-97d996bb5846	b68daa0d-e351-4312-9069-e15e8566cc91	915eab85-7100-462c-9961-90eb87c3f3d9
3a0c3fc2-76f8-452c-a31a-0e1c75d5d4c5	915eab85-7100-462c-9961-90eb87c3f3d9	2c9fdccf-45f4-4d1d-b2e3-33e924cb3e5a
3346adc4-2557-43df-93dd-5af1074dbe16	2c9fdccf-45f4-4d1d-b2e3-33e924cb3e5a	915eab85-7100-462c-9961-90eb87c3f3d9
55709637-eecd-40f1-97fb-39676a4ebfde	915eab85-7100-462c-9961-90eb87c3f3d9	b68daa0d-e351-4312-9069-e15e8566cc91
f2bc133e-5339-4428-9f19-0cccfede1e25	915eab85-7100-462c-9961-90eb87c3f3d9	915eab85-7100-462c-9961-90eb87c3f3d9
4733544b-a657-435f-9b67-7c783497c0d2	915eab85-7100-462c-9961-90eb87c3f3d9	2c9fdccf-45f4-4d1d-b2e3-33e924cb3e5a
eaa216e5-0782-4a73-8c74-731e467d9f39	915eab85-7100-462c-9961-90eb87c3f3d9	915eab85-7100-462c-9961-90eb87c3f3d9
4ba7eeca-6cf1-460e-8167-c6802fe9bfc4	915eab85-7100-462c-9961-90eb87c3f3d9	2c9fdccf-45f4-4d1d-b2e3-33e924cb3e5a
a3fe6cef-5e00-4cd7-8c0e-89ef6ecddf02	915eab85-7100-462c-9961-90eb87c3f3d9	915eab85-7100-462c-9961-90eb87c3f3d9
7c4bf5f5-bf02-4899-a389-c66dde498bf9	915eab85-7100-462c-9961-90eb87c3f3d9	915eab85-7100-462c-9961-90eb87c3f3d9
78ba5bd0-caf1-4930-9c8f-7a85f96e2126	915eab85-7100-462c-9961-90eb87c3f3d9	915eab85-7100-462c-9961-90eb87c3f3d9
\.


--
-- TOC entry 3531 (class 0 OID 16449)
-- Dependencies: 216
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (id, first_name, last_name, email, password) FROM stdin;
2c9fdccf-45f4-4d1d-b2e3-33e924cb3e5a	Adrian	Ciobanu	adrian.ciobanu@mail.com	$2a$10$jmE4ocxUcDSnqopWS.AsnuozGdIor81xK0fI2YdQiy0w/tlB1pwjy
8b8bc550-a708-4024-8f05-d549c7a91c0a	Lucian	Radu	lucian.radu@mail.com	$2a$10$khl2N8aO4.jfrpXSSqryDOXc/lR8EnK8KmMjdpAJknNzovpEym8Mu
241f6a9e-d3fc-4ce7-9a79-b5869e9a0a60	Calin	Olaroiu	calin.olaroiu@mail.com	$2a$10$U.dNe5d.LEqDcNi0JQMOh.R8CVHNHTpYDaiTBVQUHx8c.EqoVXesS
acc406a7-d3ae-4296-a95e-3c83a06f1211	Radu	Iordanescu	radu.iordanescu@mail.com	$2a$10$zOqAVpOzCtoRbp2vijj2Q.KaquX/ARv9wbCZfb1VeesuqJEHpgFwa
5d37c7bf-1901-4a8b-8cec-660b3064e325	Matei	Rosu	matei.rosu@mail.com	$2a$10$zGgNzQB8cLU/VyDzjXkqIOeNPx2B8ZOWeBkVLLPN/DSSGm7UBt/i6
59ce44be-2276-4052-9d7a-b026b6fab51f	Calin	Iordanescu	calin.iordanescu@mail.com	$2a$10$gwgMpOCKpLaBrSuR1/videqyx3JT3ANdifjEpHotTV1ZtLDOi5xge
a8a74c6c-6b42-4f1f-9c22-474f05e1a380	Ion	Grigorescu	ion.grigorescu@mail.com	$2a$10$VU0rRtGzqV9Cq9.KOjXEP.EfxkNSQG9tJ3Hq.ybkuHgQmr.H.lGJq
e13dd821-dcc7-41f2-8eb7-61bdc7b505fa	Cosmin	Popovici	cosmin.popovici@mail.com	$2a$10$cT4dBMLjD/HxMBXEZDOpx.8zj7qPNwJB1Dm8dbSf.XUhIiQdb5mlO
4a3ee63c-802d-48c3-ac22-68d087928a80	Catalin	Albu	catalin.albu@mail.com	$2a$10$VDgcEaQow.vY3gX50a7jM.NfzVbIa9aHF4GxflCnCg9Fko7Zulfxa
4c4d5ea5-8d5f-46a6-a1db-5baa6f2ea586	Marius	Ciobanu	marius.ciobanu@mail.com	$2a$10$7zqiJOyWvmQ9Avb7hgiBi.1kJv6tdAeMrs/UYc.3C3sEctb7vZZU2
c88ae15e-0912-49ae-aa41-8ac8b6d898a0	Dan	Iordanescu	dan.iordanescu@mail.com	$2a$10$1XUL26j4x36NtC62yxMcKepg6U6vIACtqO5QV3yJCUvY6eTdhKY4e
3565cfef-b6e8-45d8-9aef-0c93ffc3c62e	Alin	Ciobanu	alin.ciobanu@mail.com	$2a$10$cSH/ZODfLluXupLTk5sLQuOoix85V/a0aOpy/F6AT19ddBV5i5LBa
da4f1981-5428-4a0a-8ee7-e11efe93929e	Catalin	Balan	catalin.balan@mail.com	$2a$10$Ttm8xcYNyGMFyEJ/Zp/nsuULEC/4qHFrizbVGu4jt7svCJZzuzqeu
fdd40c56-3a20-4e90-b50d-43ee20452097	Laurentiu	Grigorescu	laurentiu.grigorescu@mail.com	$2a$10$hKGEti4RLJk/0UPcVW7DzeRL5pOX7Cdj2qvS2XfgF0SgAhr2NmY8y
eec69f0e-d8d8-4e5c-8606-489f51f5166e	Iulian	Ambrosia	iulian.ambrosia@mail.com	$2a$10$Su9rrU0LjPozNRGkr.xi6ew0QT8nt4SiUKX4l7mze4KER74Y8a0mi
915eab85-7100-462c-9961-90eb87c3f3d9	Eusebiu	Ionescu	eusebiu.ionescu@mail.com	$2a$10$6jqyWi8WmlQZOaKplvOEkOVnJkD.VB89Pj2lrCltUqBD8.x7IpH1e
99280656-fe59-4748-969a-3d4c31fcc260	Marian	Popescu	marian.popescu@mail.com	$2a$10$8kTk4NAJwnrpaimwEGgumunkslVd2G7m/7CcWd16BWSID/BYHPE82
b68daa0d-e351-4312-9069-e15e8566cc91	Claudiu	Adrian	claudiu.adrian@mail.com	$2a$10$Au2YW8p1RJ/TThhDT2Ls5.HwLMZO8RXTkZdtvwFiMfj6FPfuh45gy
c5e372ee-20b9-4b56-8015-1c86f16030d2	Alex	Pop	alex.pop@mail.com	$2a$10$1kJbwP5cFsXSqnp4eOfcO.LNv2hIfj/GmLeH1KpdsYvyUR2hFgvha
\.


--
-- TOC entry 3374 (class 2606 OID 16511)
-- Name: friendships Friendships_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.friendships
    ADD CONSTRAINT "Friendships_pk" PRIMARY KEY (id_user1, id_user2);


--
-- TOC entry 3372 (class 2606 OID 16455)
-- Name: users Users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT "Users_pkey" PRIMARY KEY (id);


--
-- TOC entry 3376 (class 2606 OID 16624)
-- Name: friendrequests friendrequests_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.friendrequests
    ADD CONSTRAINT friendrequests_pk PRIMARY KEY (id_user1, id_user2, date);


--
-- TOC entry 3380 (class 2606 OID 16601)
-- Name: messagesUsers messagesUsers_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."messagesUsers"
    ADD CONSTRAINT "messagesUsers_pk" PRIMARY KEY (id_message, id_sender, id_receiver);


--
-- TOC entry 3378 (class 2606 OID 16586)
-- Name: messages messages_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.messages
    ADD CONSTRAINT messages_pk PRIMARY KEY (id);


--
-- TOC entry 3381 (class 2606 OID 16481)
-- Name: friendships fk_id_user1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.friendships
    ADD CONSTRAINT fk_id_user1 FOREIGN KEY (id_user1) REFERENCES public.users(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3382 (class 2606 OID 16486)
-- Name: friendships fk_id_user2; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.friendships
    ADD CONSTRAINT fk_id_user2 FOREIGN KEY (id_user2) REFERENCES public.users(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3383 (class 2606 OID 16558)
-- Name: friendrequests friendrequests___user1_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.friendrequests
    ADD CONSTRAINT friendrequests___user1_fk FOREIGN KEY (id_user1) REFERENCES public.users(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3384 (class 2606 OID 16563)
-- Name: friendrequests friendrequests___user2_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.friendrequests
    ADD CONSTRAINT friendrequests___user2_fk FOREIGN KEY (id_user2) REFERENCES public.users(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3385 (class 2606 OID 16602)
-- Name: messagesUsers messagesUsers_messages_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."messagesUsers"
    ADD CONSTRAINT "messagesUsers_messages_id_fk" FOREIGN KEY (id_message) REFERENCES public.messages(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3386 (class 2606 OID 16618)
-- Name: messagesUsers messagesUsers_receiver_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."messagesUsers"
    ADD CONSTRAINT "messagesUsers_receiver_id_fk" FOREIGN KEY (id_receiver) REFERENCES public.users(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3387 (class 2606 OID 16607)
-- Name: messagesUsers messagesUsers_sender_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."messagesUsers"
    ADD CONSTRAINT "messagesUsers_sender_id_fk" FOREIGN KEY (id_sender) REFERENCES public.users(id) ON UPDATE CASCADE ON DELETE CASCADE;


-- Completed on 2024-01-23 11:57:01 EET

--
-- PostgreSQL database dump complete
--

