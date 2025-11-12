-- ddl data definition language meme store

drop table if exists video_tag;
drop table if exists tag;
drop table if exists video;
drop table if exists client;

create table client ( -- asdfasdf
    id bigint primary key,
    email varchar not null unique,
    password varchar not null
);

create table video (
    id bigint primary key,
    name varchar not null,
    link varchar,
    path varchar,
    duration_seconds int,
    file_size int,
    video_state varchar,
    date timestamp,
    client_id bigint not null,
    foreign key (client_id) references client(id)
);

create table tag (
    id bigint primary key,
    name varchar not null,
    client_id bigint not null,
    foreign key (client_id) references client(id)
);

-- video_tag table (many-to-many relationship)
create table video_tag (
    video_id bigint not null,
    tag_id bigint not null,
    primary key (video_id, tag_id),
    foreign key (video_id) references video(id),
    foreign key (tag_id) references tag(id)
);
