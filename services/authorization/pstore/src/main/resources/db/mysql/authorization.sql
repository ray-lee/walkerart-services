alter table permissions_actions drop foreign key FK85F82042E2DC84FD;
drop table if exists accounts_roles;
drop table if exists permissions;
drop table if exists permissions_actions;
drop table if exists permissions_roles;
drop table if exists roles;
create table accounts_roles (HJID bigint not null auto_increment, account_id varchar(128) not null, created_at datetime not null, role_id varchar(128) not null, role_name varchar(255), screen_name varchar(255), user_id varchar(128) not null, primary key (HJID), unique (account_id, role_id));
create table permissions (csid varchar(128) not null, action_group varchar(128), attribute_name varchar(128), created_at datetime not null, description varchar(255), effect varchar(32) not null, resource_name varchar(128) not null, tenant_id varchar(128) not null, updated_at datetime, primary key (csid));
create table permissions_actions (HJID bigint not null auto_increment, name varchar(128) not null, ACTIONS_PERMISSION_CSID varchar(128), primary key (HJID));
create table permissions_roles (HJID bigint not null auto_increment, actionGroup varchar(255), created_at datetime not null, permission_id varchar(128) not null, permission_resource varchar(255), role_id varchar(128) not null, role_name varchar(255), primary key (HJID), unique (permission_id, role_id));
create table roles (csid varchar(128) not null, created_at datetime not null, description varchar(255), rolegroup varchar(255), rolename varchar(200) not null, tenant_id varchar(128) not null, updated_at datetime, primary key (csid), unique (rolename, tenant_id));
alter table permissions_actions add index FK85F82042E2DC84FD (ACTIONS_PERMISSION_CSID), add constraint FK85F82042E2DC84FD foreign key (ACTIONS_PERMISSION_CSID) references permissions (csid);

