INSERT INTO service_categories (id, label, icon_key, color, demand_count, created_at, updated_at)
SELECT 'cat_plomberie', 'Plomberie', 'WRENCH', '#dbeafe', 0, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM service_categories WHERE id = 'cat_plomberie');

INSERT INTO service_categories (id, label, icon_key, color, demand_count, created_at, updated_at)
SELECT 'cat_electricite', 'Électricité', 'BOLT', '#fef9c3', 0, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM service_categories WHERE id = 'cat_electricite');

INSERT INTO service_categories (id, label, icon_key, color, demand_count, created_at, updated_at)
SELECT 'cat_menage', 'Ménage', 'BROOM', '#f3e8ff', 0, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM service_categories WHERE id = 'cat_menage');

INSERT INTO service_categories (id, label, icon_key, color, demand_count, created_at, updated_at)
SELECT 'cat_serrurerie', 'Serrurerie', 'KEY', '#e0e7ff', 0, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM service_categories WHERE id = 'cat_serrurerie');

INSERT INTO service_categories (id, label, icon_key, color, demand_count, created_at, updated_at)
SELECT 'cat_peinture', 'Peinture', 'BRUSH', '#ffe4e6', 0, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM service_categories WHERE id = 'cat_peinture');

INSERT INTO service_categories (id, label, icon_key, color, demand_count, created_at, updated_at)
SELECT 'cat_jardinage', 'Jardinage', 'LEAF', '#d1fae5', 0, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM service_categories WHERE id = 'cat_jardinage');
