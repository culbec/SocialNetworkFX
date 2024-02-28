--
-- PostgreSQL database dump
--

-- Dumped from database version 16.1 (Ubuntu 16.1-1.pgdg22.04+1)
-- Dumped by pg_dump version 16.1 (Ubuntu 16.1-1.pgdg22.04+1)

-- Started on 2024-01-23 11:57:21 EET

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
\.


--
-- TOC entry 3532 (class 0 OID 16476)
-- Dependencies: 217
-- Data for Name: friendships; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.friendships (id_user1, id_user2, date) FROM stdin;
\.


--
-- TOC entry 3534 (class 0 OID 16580)
-- Dependencies: 219
-- Data for Name: messages; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.messages (id, message, date, reply_id) FROM stdin;
\.


--
-- TOC entry 3535 (class 0 OID 16597)
-- Dependencies: 220
-- Data for Name: messagesUsers; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public."messagesUsers" (id_message, id_sender, id_receiver) FROM stdin;
\.


--
-- TOC entry 3531 (class 0 OID 16449)
-- Dependencies: 216
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (id, first_name, last_name, email, password) FROM stdin;
dbd9155a-575d-420b-bc67-fc05a8e30643	Ion	Micu	ion.micu@mail.com	\N
c501da39-51c5-40fa-a746-1b94cc194ef4	Andrei	Micu	andrei.micu@mail.com	\N
9be65fba-523a-43c6-96ed-4723f1018ffb	Maria	Micu	maria.micu@mail.com	\N
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
-- TOC entry 3376 (class 2606 OID 16530)
-- Name: friendrequests friendrequests_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.friendrequests
    ADD CONSTRAINT friendrequests_pk PRIMARY KEY (id_user1, id_user2);


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
-- TOC entry 3386 (class 2606 OID 16612)
-- Name: messagesUsers messagesUsers_receiver_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."messagesUsers"
    ADD CONSTRAINT "messagesUsers_receiver_id_fk" FOREIGN KEY (id_receiver) REFERENCES public.users(id);


--
-- TOC entry 3387 (class 2606 OID 16607)
-- Name: messagesUsers messagesUsers_sender_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."messagesUsers"
    ADD CONSTRAINT "messagesUsers_sender_id_fk" FOREIGN KEY (id_sender) REFERENCES public.users(id) ON UPDATE CASCADE ON DELETE CASCADE;


-- Completed on 2024-01-23 11:57:21 EET

--
-- PostgreSQL database dump complete
--

