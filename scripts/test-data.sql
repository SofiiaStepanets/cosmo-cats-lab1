-- 1) Категорія для тестових товарів (виконати один раз у базі store_db)
INSERT INTO Category (CategoryName, Description, Active)
VALUES ('Cosmic Food', 'Food for cosmic cats', true);

-- 2) Перевірка після запуску send-test-order.sh
SELECT * FROM Category;
SELECT ProductId, ProductName, Price FROM Product;
SELECT * FROM Orders;
SELECT * FROM OrderItem;
SELECT * FROM ConsumedOrders;
