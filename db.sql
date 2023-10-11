DROP DATABASE smart_fridge;

CREATE DATABASE smart_fridge;
\c smart_fridge;

CREATE TABLE sf_settings (
	settings_id varchar(64) UNIQUE NOT NULL,
	theme varchar(64) NOT NULL
);

CREATE TABLE sf_user (
	user_id varchar(64) UNIQUE NOT NULL,
	username varchar(64) NOT NULL,
	password varchar(64) NOT NULL,
	settings_id varchar(64) NOT NULL REFERENCES sf_settings(settings_id),
	PRIMARY KEY( user_id )
);

CREATE TABLE sf_session (
	session_id varchar(64) UNIQUE NOT NULL,
	user_id varchar(64) NOT NULL REFERENCES sf_user(user_id),
	expire_date date NOT NULL, PRIMARY KEY( session_id )
);

CREATE TABLE sf_ingredient (
	ingredient_id varchar(64) UNIQUE NOT NULL,
	user_id varchar(64) NOT NULL REFERENCES sf_user(user_id),
	name varchar(64),
	expire_date date,
	category varchar(64),
	quantity smallint,
	unit varchar(64),
	PRIMARY KEY( ingredient_id )
);
