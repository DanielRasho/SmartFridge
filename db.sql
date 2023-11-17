DROP DATABASE IF EXISTS smart_fridge;

CREATE DATABASE smart_fridge;
\c smart_fridge;

CREATE TABLE sf_user (
	user_id varchar(64) UNIQUE NOT NULL,
	username varchar(64) NOT NULL,
	password varchar(64) NOT NULL,
	PRIMARY KEY( user_id )
);

CREATE TABLE sf_settings (
	settings_id varchar(64) UNIQUE NOT NULL,
	user_id varchar(64) NOT NULL REFERENCES sf_user(user_id),
	theme varchar(64) NOT NULL
);

CREATE TABLE sf_session (
	session_id varchar(64) UNIQUE NOT NULL,
	user_id varchar(64) NOT NULL REFERENCES sf_user(user_id),
	expire_date TIMESTAMP WITH TIME ZONE NOT NULL,
	PRIMARY KEY( session_id )
);

CREATE TABLE sf_ingredient (
	ingredient_id varchar(64) UNIQUE NOT NULL,
	user_id varchar(64) NOT NULL REFERENCES sf_user(user_id),
	name varchar(64) NOT NULL,
	expire_date TIMESTAMP WITH TIME ZONE NOT NULL,
	category varchar(64) NOT NULL,
	quantity float(4) NOT NULL,
	unit varchar(64) NOT NULL,
	PRIMARY KEY( ingredient_id )
);

-- Enabling Fuzzy search with extension
CREATE EXTENSION pg_trgm;

-- Creating default user
-- Username: EL
-- Password: 1234
INSERT INTO sf_user VALUES ('81770b26-a1ce-47e6-a7a0-231c64aaeaef', 'EL', 'rE6Ex3ELU+pY763tIvyZJQOsZ0IW8+Fcdh7hpeJV8GeVNiPIs4i0RZ4T+XjXyEb0');
INSERT INTO sf_settings VALUES ('1f52e19d-7147-4393-9567-e7fbbd8e0d66', '81770b26-a1ce-47e6-a7a0-231c64aaeaef', 'Dark');
