create table data_sources(
    id varchar(36),
    name text not null,
    primary key(id)
);
create table categories(
    id varchar(36),
    name text not null,
    parent_id varchar(36),
    primary key(id),
    foreign key(parent_id) references categories(id)
);
create table parameters(
    id varchar(36),
    name text not null,
    category_id varchar(36) not null,
    primary key(id),
    foreign key(category_id) references categories(id)
);
create table data_source_parameters(
    data_source_id varchar(36),
    parameter_id varchar(36),
    primary key(data_source_id, parameter_id),
    foreign key(data_source_id) references data_sources(id),
    foreign key references(parent_id) parameters(id)
);

-- From example I've made an assumption that parameter can have relation to more than one data source.
-- In case if parameter can have relation to only one data source we shouldn't have data_source_parameters table.
-- parameters table in this case will be:
-- create table parameters(
--     id varchar(36) primary key,
--     name text not null,
--     category_id varchar(36) not null foreign key references categories(id),
--     data_source_id varchar(36) not null foreign key references data_sources(id)
--);