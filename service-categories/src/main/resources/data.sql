INSERT INTO service_categories (id, label, icon_key, description, color, budget_min, budget_max, demand_count, created_at, updated_at)
SELECT 'cat_plomberie', 'Plomberie', 'WRENCH', 'Réparation de fuites, installation sanitaire, dépannage plomberie', '#dbeafe', 5000, 50000, 0, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM service_categories WHERE id = 'cat_plomberie');

INSERT INTO service_categories (id, label, icon_key, description, color, budget_min, budget_max, demand_count, created_at, updated_at)
SELECT 'cat_electricite', 'Électricité', 'BOLT', 'Installation électrique, dépannage, mise aux normes', '#fef9c3', 5000, 60000, 0, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM service_categories WHERE id = 'cat_electricite');

INSERT INTO service_categories (id, label, icon_key, description, color, budget_min, budget_max, demand_count, created_at, updated_at)
SELECT 'cat_menage', 'Ménage', 'BROOM', 'Nettoyage de domicile, entretien courant, grand ménage', '#f3e8ff', 3000, 25000, 0, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM service_categories WHERE id = 'cat_menage');

INSERT INTO service_categories (id, label, icon_key, description, color, budget_min, budget_max, demand_count, created_at, updated_at)
SELECT 'cat_serrurerie', 'Serrurerie', 'KEY', 'Ouverture de porte, remplacement de serrures, dépannage urgence', '#e0e7ff', 5000, 40000, 0, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM service_categories WHERE id = 'cat_serrurerie');

INSERT INTO service_categories (id, label, icon_key, description, color, budget_min, budget_max, demand_count, created_at, updated_at)
SELECT 'cat_peinture', 'Peinture', 'BRUSH', 'Peinture intérieure et extérieure, rénovation de façades', '#ffe4e6', 10000, 100000, 0, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM service_categories WHERE id = 'cat_peinture');

INSERT INTO service_categories (id, label, icon_key, description, color, budget_min, budget_max, demand_count, created_at, updated_at)
SELECT 'cat_jardinage', 'Jardinage', 'LEAF', 'Entretien de jardins, tonte, taille de haies, plantations', '#d1fae5', 3000, 30000, 0, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM service_categories WHERE id = 'cat_jardinage');
