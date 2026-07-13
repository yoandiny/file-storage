create table if not exists file
(
    id                uuid
        constraint file_pk primary key,
    name              varchar   not null,
    user_email        varchar   not null,
    creation_datetime timestamp not null default now()
);
