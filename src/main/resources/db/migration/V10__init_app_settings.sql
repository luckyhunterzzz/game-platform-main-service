create table if not exists public.app_settings
(
    setting_key      varchar(150) primary key,
    setting_value    varchar(255)             not null,
    updated_at       timestamp with time zone not null default now(),
    updated_by       varchar(255),
    updated_by_email varchar(255)
);

insert into public.app_settings (setting_key, setting_value)
values ('hero_public_visibility', 'READY_ONLY')
on conflict (setting_key) do nothing;
