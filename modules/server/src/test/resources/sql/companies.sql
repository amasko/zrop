
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