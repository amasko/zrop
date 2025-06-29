
create table iva.invites
(
    id integer primary key generated always as identity,
    user_name text not null ,
    company_id bigint not null ,
    n_invites int not null ,
    active boolean not null default false
);

