insert into roles (name)
values
    ('ROLE_USER'), ('ROLE_ADMIN');

-- Вставка данных в таблицу users
insert into users (username, password, email)
values
    ('user', '$2a$04$Fx/SX9.BAvtPlMyIIqqFx.hLY2Xp8nnhpzvEEVINvVpwIPbA3v/.i', 'user@gmail.com'),
    ('admin', '$2a$04$Fx/SX9.BAvtPlMyIIqqFx.hLY2Xp8nnhpzvEEVINvVpwIPbA3v/.i', 'admin@gmail.com');

-- Вставка данных в таблицу users_roles
insert into users_roles (user_id, role_id)
values
    (1, 1),
    (2, 2);