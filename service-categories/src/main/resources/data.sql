-- Catégories par défaut avec slug
TRUNCATE TABLE categories RESTART IDENTITY CASCADE;

INSERT INTO categories (slug, label, icon_key, color, demand_count, percentage_share)
VALUES ('transport', 'Transport', 'car', 'blue', 10, 15.5);

INSERT INTO categories (slug, label, icon_key, color, demand_count, percentage_share)
VALUES ('logement', 'Logement', 'home', 'red', 25, 40.0);

INSERT INTO categories (slug, label, icon_key, color, demand_count, percentage_share)
VALUES ('sante', 'Santé', 'hospital', 'green', 5, 8.0);

INSERT INTO categories (slug, label, icon_key, color, demand_count, percentage_share)
VALUES ('education', 'Éducation', 'book', 'yellow', 12, 20.0);

