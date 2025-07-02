INSERT INTO asset_list (asset_name, asset_full_name, current_price, is_deleted) VALUES
('TRY', 'Turk Lirasi', 1.00, FALSE),
('ASELS', 'Aselsan Elektronik Sanayi ve Ticaret A.S.', 45.75, FALSE),
('THYAO', 'Turk Hava Yollari A.O.', 120.50, FALSE),
('SISE', 'Sisecam', 35.20, FALSE),
('KRDMD', 'Kardemir Karabuk Demir Celik', 22.10, FALSE);


INSERT INTO customer (name, surname, email, username, password, role, is_deleted)
VALUES ('Admin', 'User', 'admin@mail.com', 'admin', '$2a$10$NYLEoVXc1IKdVvi34tJfBeA8YAqzI3fSQEbVS9lyIYGCeaCrB8pRq', 'ROLE_ADMIN', FALSE);

INSERT INTO customer (name, surname, email, username, password, role, is_deleted)
VALUES ('Baris', 'Incesu', 'barisincesu@mail.com', 'baris', '$2a$10$gzZRC6WVl2I5G2e6x5aVQez2F8QphPMunIolmFnBA6BY4WashiXtW', 'ROLE_USER', FALSE);

INSERT INTO customer (name, surname, email, username, password, role, is_deleted)
VALUES ('Murat', 'Aktan', 'murataktan@mail.com', 'murat', '$2a$10$gzZRC6WVl2I5G2e6x5aVQez2F8QphPMunIolmFnBA6BY4WashiXtW', 'ROLE_USER', FALSE);
