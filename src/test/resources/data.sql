-- Deterministic, realistic seed data for tests
-- 1) Images (stable UUIDs)
insert into image(id, name, src) values
  ('00000000-0000-0000-0000-000000000001','placeholder-1','/img/placeholder-1.jpg'),
  ('00000000-0000-0000-0000-000000000002','placeholder-2','/img/placeholder-2.jpg'),
  ('00000000-0000-0000-0000-000000000003','electronics','/img/categories/electronics.jpg'),
  ('00000000-0000-0000-0000-000000000004','fashion','/img/categories/fashion.jpg'),
  ('00000000-0000-0000-0000-000000000005','home','/img/categories/home.jpg');

-- 2) 100 real brand names (stable)
insert into brand(id, name) values
  (1,'Apple'),(2,'Samsung'),(3,'Sony'),(4,'LG'),(5,'Dell'),(6,'HP'),(7,'Lenovo'),(8,'Asus'),(9,'Acer'),(10,'Microsoft'),
  (11,'Google'),(12,'Huawei'),(13,'Xiaomi'),(14,'OnePlus'),(15,'Nokia'),(16,'Canon'),(17,'Nikon'),(18,'Panasonic'),(19,'Philips'),(20,'Bosch'),
  (21,'Siemens'),(22,'Whirlpool'),(23,'KitchenAid'),(24,'Tefal'),(25,'IKEA'),(26,'Adidas'),(27,'Nike'),(28,'Puma'),(29,'Reebok'),(30,'Under Armour'),
  (31,'New Balance'),(32,'H&M'),(33,'Zara'),(34,'Uniqlo'),(35,'Levi''s'),(36,'Gucci'),(37,'Prada'),(38,'Hermes'),(39,'Chanel'),(40,'L''Oréal'),
  (41,'Maybelline'),(42,'Estee Lauder'),(43,'Dior'),(44,'Clinique'),(45,'Gillette'),(46,'Colgate'),(47,'Oral-B'),(48,'Philips Hue'),(49,'Logitech'),(50,'Razer'),
  (51,'Corsair'),(52,'Kingston'),(53,'Sandisk'),(54,'Seagate'),(55,'Western Digital'),(56,'GoPro'),(57,'DJI'),(58,'Bose'),(59,'Sennheiser'),(60,'JBL'),
  (61,'Beats'),(62,'Marshall'),(63,'Yamaha'),(64,'Casio'),(65,'Fossil'),(66,'Rolex'),(67,'Timex'),(68,'Citizen'),(69,'Tissot'),(70,'Tag Heuer'),
  (71,'The North Face'),(72,'Patagonia'),(73,'Columbia'),(74,'Decathlon'),(75,'Salomon'),(76,'Fila'),(77,'Crocs'),(78,'Skechers'),(79,'Clarks'),(80,'Dr. Martens'),
  (81,'Toyota'),(82,'Honda'),(83,'Ford'),(84,'Chevrolet'),(85,'BMW'),(86,'Mercedes-Benz'),(87,'Audi'),(88,'Volkswagen'),(89,'Volvo'),(90,'Tesla'),
  (91,'Amazon Basics'),(92,'Anker'),(93,'Belkin'),(94,'TP-Link'),(95,'Netgear'),(96,'Synology'),(97,'Ring'),(98,'Arlo'),(99,'Nest'),(100,'Fitbit');

-- 3) Categories (parents and children)
insert into category(id, enabled, sort_order, created_at, updated_at, parent_id, image_id) values
  (1, TRUE, 1,  '2024-01-01T00:00:00Z','2024-06-01T00:00:00Z', null, '00000000-0000-0000-0000-000000000003'), -- Electronics
  (2, TRUE, 2,  '2024-01-01T00:00:00Z','2024-06-01T00:00:00Z', null, '00000000-0000-0000-0000-000000000004'), -- Fashion
  (3, TRUE, 3,  '2024-01-01T00:00:00Z','2024-06-01T00:00:00Z', null, '00000000-0000-0000-0000-000000000005'), -- Home & Kitchen
  (4, TRUE, 4,  '2024-01-01T00:00:00Z','2024-06-01T00:00:00Z', null, null), -- Sports & Outdoors
  (5, TRUE, 5,  '2024-01-01T00:00:00Z','2024-06-01T00:00:00Z', null, null), -- Beauty
  (6, TRUE, 6,  '2024-01-01T00:00:00Z','2024-06-01T00:00:00Z', null, null), -- Books
  (7, TRUE, 7,  '2024-01-01T00:00:00Z','2024-06-01T00:00:00Z', null, null), -- Toys & Games
  (8, TRUE, 8,  '2024-01-01T00:00:00Z','2024-06-01T00:00:00Z', null, null), -- Automotive
  (9, TRUE, 9,  '2024-01-01T00:00:00Z','2024-06-01T00:00:00Z', null, null), -- Pet Supplies
  (10, TRUE, 10,'2024-01-01T00:00:00Z','2024-06-01T00:00:00Z', null, null), -- Grocery
  -- children for Electronics
  (11, TRUE, 1, '2024-01-01T00:00:00Z','2024-06-01T00:00:00Z', 1, null), -- Smartphones
  (12, TRUE, 2, '2024-01-01T00:00:00Z','2024-06-01T00:00:00Z', 1, null), -- Laptops
  (13, TRUE, 3, '2024-01-01T00:00:00Z','2024-06-01T00:00:00Z', 1, null), -- Audio
  (14, TRUE, 4, '2024-01-01T00:00:00Z','2024-06-01T00:00:00Z', 1, null), -- Cameras
  -- children for Fashion
  (21, TRUE, 1, '2024-01-01T00:00:00Z','2024-06-01T00:00:00Z', 2, null), -- Men
  (22, TRUE, 2, '2024-01-01T00:00:00Z','2024-06-01T00:00:00Z', 2, null), -- Women
  (23, TRUE, 3, '2024-01-01T00:00:00Z','2024-06-01T00:00:00Z', 2, null), -- Kids
  (24, TRUE, 4, '2024-01-01T00:00:00Z','2024-06-01T00:00:00Z', 2, null), -- Shoes
  -- children for Home & Kitchen
  (31, TRUE, 1, '2024-01-01T00:00:00Z','2024-06-01T00:00:00Z', 3, null), -- Appliances
  (32, TRUE, 2, '2024-01-01T00:00:00Z','2024-06-01T00:00:00Z', 3, null), -- Furniture
  (33, TRUE, 3, '2024-01-01T00:00:00Z','2024-06-01T00:00:00Z', 3, null), -- Decor
  -- others as leafs without children for brevity
  (41, TRUE, 1, '2024-01-01T00:00:00Z','2024-06-01T00:00:00Z', 4, null), -- Fitness
  (51, TRUE, 1, '2024-01-01T00:00:00Z','2024-06-01T00:00:00Z', 5, null), -- Skincare
  (61, TRUE, 1, '2024-01-01T00:00:00Z','2024-06-01T00:00:00Z', 6, null), -- Fiction
  (71, TRUE, 1, '2024-01-01T00:00:00Z','2024-06-01T00:00:00Z', 7, null), -- Board Games
  (81, TRUE, 1, '2024-01-01T00:00:00Z','2024-06-01T00:00:00Z', 8, null), -- Car Accessories
  (91, TRUE, 1, '2024-01-01T00:00:00Z','2024-06-01T00:00:00Z', 9, null), -- Dog Supplies
  (101, TRUE,1, '2024-01-01T00:00:00Z','2024-06-01T00:00:00Z', 10, null); -- Snacks

-- Category descriptions (EN)
insert into category_description(category_id, locale, meta_description, meta_title, title, description) values
  (1,'en','Electronics and gadgets','Electronics','Electronics','Latest consumer electronics and gadgets'),
  (2,'en','Clothing and fashion items','Fashion','Fashion','Apparel, shoes and accessories'),
  (3,'en','Home and kitchen essentials','Home & Kitchen','Home & Kitchen','Appliances, furniture and decor'),
  (4,'en','Sporting goods and outdoor gear','Sports & Outdoors','Sports & Outdoors','Gear for fitness and adventures'),
  (5,'en','Beauty and personal care','Beauty','Beauty','Cosmetics, skincare, grooming'),
  (6,'en','Books and literature','Books','Books','Fiction and non-fiction'),
  (7,'en','Toys and games','Toys & Games','Toys & Games','For kids and adults'),
  (8,'en','Automotive accessories','Automotive','Automotive','Car and motorcycle accessories'),
  (9,'en','Pet supplies','Pet Supplies','Pet Supplies','Food, toys, and care for pets'),
  (10,'en','Grocery and pantry','Grocery','Grocery','Everyday food and drinks'),
  (11,'en','Smartphones','Smartphones','Smartphones','Android and iOS phones'),
  (12,'en','Laptops','Laptops','Laptops','Ultrabooks and gaming laptops'),
  (13,'en','Audio','Audio','Audio','Headphones and speakers'),
  (14,'en','Cameras','Cameras','Cameras','DSLRs and action cams'),
  (21,'en','Men''s fashion','Men','Men','Clothing for men'),
  (22,'en','Women''s fashion','Women','Women','Clothing for women'),
  (23,'en','Kids fashion','Kids','Kids','Clothing for kids'),
  (24,'en','Shoes','Shoes','Shoes','Casual and sports footwear'),
  (31,'en','Home appliances','Appliances','Appliances','Kitchen and home appliances'),
  (32,'en','Furniture','Furniture','Furniture','Chairs, tables, sofas'),
  (33,'en','Home decor','Decor','Decor','Lighting, art, decor'),
  (41,'en','Fitness gear','Fitness','Fitness','Training and fitness equipment'),
  (51,'en','Skincare','Skincare','Skincare','Creams, serums, masks'),
  (61,'en','Fiction books','Fiction','Fiction','Novels and stories'),
  (71,'en','Board games','Board Games','Board Games','Family and strategy games'),
  (81,'en','Car accessories','Car Accessories','Car Accessories','Interior and exterior'),
  (91,'en','Dog supplies','Dog Supplies','Dog Supplies','Food and accessories for dogs'),
  (101,'en','Snacks','Snacks','Snacks','Tasty packaged snacks');

-- 4) Attributes with varied input types
insert into attribute(id, input_type, enabled) values
  (1,'TEXT',TRUE),   -- Color
  (2,'TEXT',TRUE),   -- Size
  (3,'TEXT',TRUE),   -- Material
  (4,'NUMBER',TRUE), -- Rating (0-5)
  (5,'DATE',TRUE),   -- Release date
  (6,'DATETIME',TRUE), -- Last updated
  (7,'BOOLEAN',TRUE), -- In stock
  (8,'TEXT',TRUE);   -- Price band

insert into attribute_description(attribute_id, locale, name) values
  (1,'en','Color'),
  (2,'en','Size'),
  (3,'en','Material'),
  (4,'en','Rating'),
  (5,'en','Release Date'),
  (6,'en','Last Updated'),
  (7,'en','In Stock'),
  (8,'en','Price Band');

-- Map attributes to selected categories
insert into category_attribute(attribute_id, category_id, mandatory, use_in_filters, sort_order) values
  (1,11,TRUE,TRUE,1),(1,12,TRUE,TRUE,1),(1,13,TRUE,TRUE,1),(1,14,TRUE,TRUE,1), -- Color for electronics
  (2,24,FALSE,TRUE,2),(2,21,FALSE,TRUE,2),(2,22,FALSE,TRUE,2),(2,23,FALSE,TRUE,2), -- Size for fashion
  (3,31,FALSE,TRUE,3),(3,32,FALSE,TRUE,3),(3,33,FALSE,TRUE,3), -- Material for home
  (4,13,FALSE,TRUE,4),(4,41,FALSE,TRUE,4), -- Rating
  (5,11,FALSE,TRUE,5),(5,12,FALSE,TRUE,5),(5,13,FALSE,TRUE,5),(5,14,FALSE,TRUE,5), -- Release date
  (6,11,FALSE,FALSE,6),(6,12,FALSE,FALSE,6), -- Last updated
  (7,11,TRUE,TRUE,7),(7,31,TRUE,TRUE,7),(7,24,TRUE,TRUE,7), -- Availability
  (8,11,FALSE,TRUE,8),(8,12,FALSE,TRUE,8),(8,24,FALSE,TRUE,8); -- Price band

-- 5) Seed 360 products with deterministic variety
insert into product(
  id, currency, enabled, price, weight, brand_id, category_id, created_at, updated_at, ean, sku, weight_class
)
select 
  x as id,
  case mod(x,4) when 0 then 'USD' when 1 then 'EUR' when 2 then 'GBP' else 'JPY' end as currency,
  case when mod(x, 11) = 0 then FALSE else TRUE end as enabled,
  cast((case when mod(x,10)<3 then 49.990 when mod(x,10)<6 then 199.990 else 999.990 end) + (mod(x,37)) as decimal(15,3)) as price,
  cast( case when mod(x,100)<50 then (0.200 + (mod(x,25)*0.010)) else (1.500 + (mod(x,40)*0.050)) end as decimal(15,8)) as weight,
  mod(x, 100) + 1 as brand_id,
  -- Distribute among child categories with emphasis on electronics/fashion/home
  case 
    when mod(x,12)=0 then 14 -- Cameras
    when mod(x,12)=1 then 11 -- Smartphones
    when mod(x,12)=2 then 12 -- Laptops
    when mod(x,12)=3 then 13 -- Audio
    when mod(x,12)=4 then 24 -- Shoes
    when mod(x,12)=5 then 21 -- Men
    when mod(x,12)=6 then 22 -- Women
    when mod(x,12)=7 then 31 -- Appliances
    when mod(x,12)=8 then 32 -- Furniture
    when mod(x,12)=9 then 41 -- Fitness
    when mod(x,12)=10 then 61 -- Fiction
    else 71 -- Board Games
  end as category_id,
  '2024-01-01T00:00:00Z' as created_at,
  '2024-06-01T00:00:00Z' as updated_at,
  right('00000000000000' || cast(10000000 + x as varchar), 14) as ean,
  'SKU-' || case when mod(x,12)=0 then 'CAM' when mod(x,12)=1 then 'PHN' when mod(x,12)=2 then 'LAP' when mod(x,12)=3 then 'AUD' when mod(x,12)=4 then 'SHO' when mod(x,12)=5 then 'MEN' when mod(x,12)=6 then 'WMN' when mod(x,12)=7 then 'APP' when mod(x,12)=8 then 'FUR' when mod(x,12)=9 then 'FIT' when mod(x,12)=10 then 'BKF' else 'GME' end || '-' || right('0000' || cast(x as varchar), 4) as sku,
  case when mod(x,2)=0 then 'GRAM' else 'KILOGRAM' end as weight_class
from system_range(1, 360);

-- 6) Product descriptions with realistic titles using brand and category keywords
insert into product_description(product_id, locale, meta_description, meta_title, title, description)
select 
  p.id as product_id,
  'en' as locale,
  b.name || ' ' || 
    case when mod(p.id,12)=0 then 'Digital Camera' when mod(p.id,12)=1 then 'Smartphone' when mod(p.id,12)=2 then 'Laptop' when mod(p.id,12)=3 then 'Wireless Headphones' when mod(p.id,12)=4 then 'Running Shoes' when mod(p.id,12)=5 then 'Men''s Jacket' when mod(p.id,12)=6 then 'Women''s Dress' when mod(p.id,12)=7 then 'Kitchen Appliance' when mod(p.id,12)=8 then 'Sofa' when mod(p.id,12)=9 then 'Fitness Tracker' when mod(p.id,12)=10 then 'Fiction Book' else 'Board Game' end || ' - great features' as meta_description,
  b.name || ' ' || 
    case when mod(p.id,12)=0 then 'Camera' when mod(p.id,12)=1 then 'Phone' when mod(p.id,12)=2 then 'Laptop' when mod(p.id,12)=3 then 'Headphones' when mod(p.id,12)=4 then 'Shoes' when mod(p.id,12)=5 then 'Jacket' when mod(p.id,12)=6 then 'Dress' when mod(p.id,12)=7 then 'Appliance' when mod(p.id,12)=8 then 'Sofa' when mod(p.id,12)=9 then 'Tracker' when mod(p.id,12)=10 then 'Book' else 'Game' end as meta_title,
  b.name || ' ' || 
    case when mod(p.id,12)=0 then '4K Camera' when mod(p.id,12)=1 then '5G Smartphone' when mod(p.id,12)=2 then 'Ultrabook' when mod(p.id,12)=3 then 'Noise-Canceling Headphones' when mod(p.id,12)=4 then 'Running Shoes' when mod(p.id,12)=5 then 'Outdoor Jacket' when mod(p.id,12)=6 then 'Summer Dress' when mod(p.id,12)=7 then 'Blender' when mod(p.id,12)=8 then 'Sectional Sofa' when mod(p.id,12)=9 then 'Fitness Tracker' when mod(p.id,12)=10 then 'Hardcover Novel' else 'Strategy Board Game' end as title,
  'Explore the ' || lower(b.name) || ' ' || 
    case when mod(p.id,12)=0 then 'camera with 4K recording and optical zoom.' when mod(p.id,12)=1 then 'smartphone featuring 5G, OLED display, and long battery life.' when mod(p.id,12)=2 then 'ultrabook with SSD storage, backlit keyboard, and fast processors.' when mod(p.id,12)=3 then 'wireless headphones with active noise cancellation and deep bass.' when mod(p.id,12)=4 then 'running shoes designed for comfort and performance.' when mod(p.id,12)=5 then 'men''s jacket suitable for all seasons.' when mod(p.id,12)=6 then 'women''s dress perfect for casual wear.' when mod(p.id,12)=7 then 'kitchen appliance to speed up your cooking.' when mod(p.id,12)=8 then 'comfortable sectional sofa for your living room.' when mod(p.id,12)=9 then 'fitness tracker to monitor your activity and sleep.' when mod(p.id,12)=10 then 'fiction book by a renowned author.' else 'board game for fun evenings with friends.' end as description
from product p join brand b on b.id = mod(p.id,100)+1;

-- 7) Deterministic tags (multiple per product)
insert into product_tags(product_id, tags)
select id, 'new' from product where mod(id,5)=0;
insert into product_tags(product_id, tags)
select id, 'bestseller' from product where mod(id,7)=0;
insert into product_tags(product_id, tags)
select id, 'clearance' from product where mod(id,13)=0;
insert into product_tags(product_id, tags)
select id, 'eco' from product where mod(id,17)=0;
insert into product_tags(product_id, tags)
select id, 'premium' from product where mod(id,19)=0;
insert into product_tags(product_id, tags)
select id, 'budget' from product where mod(id,23)=0;
insert into product_tags(product_id, tags)
select id, 'wireless' from product where category_id in (11,12,13,14);
insert into product_tags(product_id, tags)
select id, 'smart' from product where category_id in (11,12,13);
insert into product_tags(product_id, tags)
select id, 'outdoor' from product where category_id in (41);

-- 8) Product images (link some products deterministically)
insert into product_image(product_id, image_id) select id, '00000000-0000-0000-0000-000000000001' from product where mod(id,3)=0;
insert into product_image(product_id, image_id) select id, '00000000-0000-0000-0000-000000000002' from product where mod(id,7)=0;

-- 9) Product single attribute (cycles over defined attributes with realistic values)
-- Build attribute value by case on attribute_id assigned from product id
insert into product_attribute(product_id, attribute_id, attribute_value)
select 
  p.id as product_id,
  case mod(p.id,8)+1
    when 1 then 1
    when 2 then 2
    when 3 then 3
    when 4 then 4
    when 5 then 5
    when 6 then 6
    when 7 then 7
    else 8
  end as attribute_id,
  case mod(p.id,8)+1
    when 1 then (case mod(p.id,7) when 0 then 'Black' when 1 then 'White' when 2 then 'Blue' when 3 then 'Red' when 4 then 'Green' when 5 then 'Silver' else 'Gold' end)
    when 2 then (case mod(p.id,5) when 0 then 'XS' when 1 then 'S' when 2 then 'M' when 3 then 'L' else 'XL' end)
    when 3 then (case mod(p.id,6) when 0 then 'Cotton' when 1 then 'Leather' when 2 then 'Metal' when 3 then 'Plastic' when 4 then 'Wood' else 'Glass' end)
    when 4 then cast(mod(p.id,6) as varchar)
    when 5 then ( '2023-' || right('00' || cast(1 + mod(p.id,12) as varchar), 2) || '-' || right('00' || cast(1 + mod(p.id,28) as varchar), 2) )
    when 6 then ( '2024-' || right('00' || cast(1 + mod(p.id,12) as varchar), 2) || '-' || right('00' || cast(1 + mod(p.id,28) as varchar), 2) || 'T' || right('00' || cast(mod(p.id,24) as varchar), 2) || ':00:00Z' )
    when 7 then (case when mod(p.id,2)=0 then 'true' else 'false' end)
    else (case mod(p.id,3) when 0 then 'Budget' when 1 then 'Mid-range' else 'Premium' end)
  end as attribute_value
from product p;
