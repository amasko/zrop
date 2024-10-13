create table if not exists companies (
    id integer primary key generated always as identity,
    slug text unique not null ,
    name text unique not null ,
    url text unique not null,
    location text,
    country text,
    industry text,
    image text,
    tags text[]
);

create table if not exists reviews (
                                           id integer primary key generated always as identity,
                                           company_id bigint not null ,
                                           user_id bigint not null ,
                                           management int not null,
                                           culture int not null ,
                                           salary int not null,
                                           benefits int not null,
                                           would_recommend int not null,
                                           review text not null,
                                           created timestamp not null default current_timestamp,
                                           updated timestamp not null default current_timestamp
);

create table if not exists users
(
    id integer primary key generated always as identity,
    email text unique not null,
    hashed_password text not null
)
