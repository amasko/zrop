
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

