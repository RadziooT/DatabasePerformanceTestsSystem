-- Compact H2 seed focused on deterministic TPC-C transaction demos.

INSERT INTO WAREHOUSE (W_NAME, W_STREET_1, W_STREET_2, W_CITY, W_STATE, W_ZIP, W_TAX, W_YTD) VALUES
('W1', 'Main Street 1', 'Main Street 1', 'City1', 'ST', '100010000', 0.0800, 1000.00),
('W2', 'Main Street 2', 'Main Street 2', 'City2', 'ST', '200010000', 0.0700, 500.00);

INSERT INTO DISTRICT (D_W_ID, D_ID, D_NAME, D_STREET_1, D_STREET_2, D_CITY, D_STATE, D_ZIP, D_TAX, D_YTD, D_NEXT_O_ID) VALUES
(1, 1, 'D1', 'District 1 Street', 'District 1 Street', 'City1', 'ST', '110010000', 0.0500, 300.00, 3001),
(1, 2, 'D2', 'District 2 Street', 'District 2 Street', 'City1', 'ST', '110020000', 0.0400, 200.00, 3001),
(2, 1, 'D1', 'District 1 Street', 'District 1 Street', 'City2', 'ST', '210010000', 0.0600, 150.00, 3001);

INSERT INTO ITEM (I_ID, I_IM_ID, I_NAME, I_PRICE, I_DATA) VALUES
(1, 101, 'Item1', 10.00, 'general'),
(2, 102, 'Item2', 20.00, 'general'),
(3, 103, 'Item3', 15.00, 'low'),
(4, 104, 'Item4', 25.00, 'low'),
(5, 105, 'Item5', 30.00, 'remote'),
(6, 106, 'Item6', 5.00, 'small');

INSERT INTO CUSTOMER (C_W_ID, C_D_ID, C_ID, C_FIRST, C_MIDDLE, C_LAST, C_STREET_1, C_STREET_2, C_CITY, C_STATE, C_ZIP, C_PHONE, C_SINCE, C_CREDIT, C_CREDIT_LIM, C_DISCOUNT, C_BALANCE, C_YTD_PAYMENT, C_PAYMENT_CNT, C_DELIVERY_CNT, C_DATA) VALUES
(1, 1, 1, 'Alice', 'OE', 'Smith', 'Street 1', 'Street 1', 'City1', 'ST', '100000000', '1234567890123456', TIMESTAMP '2026-01-01 00:00:00', 'GC', 50000.00, 0.0500, 500.00, 0.00, 2, 0, 'data1'),
(1, 1, 2, 'Bob', 'OE', 'Smith', 'Street 2', 'Street 2', 'City1', 'ST', '100000000', '1234567890123456', TIMESTAMP '2026-01-01 00:00:00', 'GC', 50000.00, 0.0500, 350.00, 0.00, 1, 0, 'data2'),
(1, 1, 3, 'Carol', 'OE', 'Brown', 'Street 3', 'Street 3', 'City1', 'ST', '100000000', '1234567890123456', TIMESTAMP '2026-01-01 00:00:00', 'GC', 50000.00, 0.0500, 200.00, 0.00, 0, 0, 'data3'),
(1, 2, 4, 'David', 'OE', 'Lee', 'Street 4', 'Street 4', 'City1', 'ST', '100000000', '1234567890123456', TIMESTAMP '2026-01-01 00:00:00', 'GC', 50000.00, 0.0500, 120.00, 0.00, 0, 0, 'data4'),
(2, 1, 5, 'Eva', 'OE', 'Stone', 'Street 5', 'Street 5', 'City2', 'ST', '200000000', '1234567890123456', TIMESTAMP '2026-01-01 00:00:00', 'GC', 50000.00, 0.0500, 900.00, 0.00, 4, 0, 'data5');

INSERT INTO STOCK (S_W_ID, S_I_ID, S_QUANTITY, S_DIST_01, S_DIST_02, S_DIST_03, S_DIST_04, S_DIST_05, S_DIST_06, S_DIST_07, S_DIST_08, S_DIST_09, S_DIST_10, S_YTD, S_ORDER_CNT, S_REMOTE_CNT, S_DATA) VALUES
(1, 1, 30, 'DIST-01', 'DIST-02', 'DIST-03', 'DIST-04', 'DIST-05', 'DIST-06', 'DIST-07', 'DIST-08', 'DIST-09', 'DIST-10', 20, 2, 0, 'w1 i1'),
(1, 2, 25, 'DIST-01', 'DIST-02', 'DIST-03', 'DIST-04', 'DIST-05', 'DIST-06', 'DIST-07', 'DIST-08', 'DIST-09', 'DIST-10', 30, 2, 0, 'w1 i2'),
(1, 3, 8, 'DIST-01', 'DIST-02', 'DIST-03', 'DIST-04', 'DIST-05', 'DIST-06', 'DIST-07', 'DIST-08', 'DIST-09', 'DIST-10', 40, 3, 0, 'w1 i3'),
(1, 4, 6, 'DIST-01', 'DIST-02', 'DIST-03', 'DIST-04', 'DIST-05', 'DIST-06', 'DIST-07', 'DIST-08', 'DIST-09', 'DIST-10', 35, 2, 0, 'w1 i4'),
(1, 5, 2, 'DIST-01', 'DIST-02', 'DIST-03', 'DIST-04', 'DIST-05', 'DIST-06', 'DIST-07', 'DIST-08', 'DIST-09', 'DIST-10', 10, 1, 0, 'w1 i5'),
(1, 6, 40, 'DIST-01', 'DIST-02', 'DIST-03', 'DIST-04', 'DIST-05', 'DIST-06', 'DIST-07', 'DIST-08', 'DIST-09', 'DIST-10', 5, 1, 0, 'w1 i6'),
(2, 1, 35, 'DIST-01', 'DIST-02', 'DIST-03', 'DIST-04', 'DIST-05', 'DIST-06', 'DIST-07', 'DIST-08', 'DIST-09', 'DIST-10', 10, 1, 0, 'w2 i1'),
(2, 2, 40, 'DIST-01', 'DIST-02', 'DIST-03', 'DIST-04', 'DIST-05', 'DIST-06', 'DIST-07', 'DIST-08', 'DIST-09', 'DIST-10', 10, 1, 0, 'w2 i2'),
(2, 3, 22, 'DIST-01', 'DIST-02', 'DIST-03', 'DIST-04', 'DIST-05', 'DIST-06', 'DIST-07', 'DIST-08', 'DIST-09', 'DIST-10', 10, 1, 0, 'w2 i3'),
(2, 4, 18, 'DIST-01', 'DIST-02', 'DIST-03', 'DIST-04', 'DIST-05', 'DIST-06', 'DIST-07', 'DIST-08', 'DIST-09', 'DIST-10', 10, 1, 0, 'w2 i4'),
(2, 5, 50, 'DIST-01', 'DIST-02', 'DIST-03', 'DIST-04', 'DIST-05', 'DIST-06', 'DIST-07', 'DIST-08', 'DIST-09', 'DIST-10', 10, 1, 0, 'w2 i5'),
(2, 6, 60, 'DIST-01', 'DIST-02', 'DIST-03', 'DIST-04', 'DIST-05', 'DIST-06', 'DIST-07', 'DIST-08', 'DIST-09', 'DIST-10', 10, 1, 0, 'w2 i6');

INSERT INTO ORDERS (O_W_ID, O_D_ID, O_ID, O_C_ID, O_ENTRY_D, O_CARRIER_ID, O_OL_CNT, O_ALL_LOCAL) VALUES
(1, 1, 1, 1, TIMESTAMP '2026-04-10 09:00:00', 1, 2, 1),
(1, 1, 2, 2, TIMESTAMP '2026-04-10 09:30:00', NULL, 3, 1),
(1, 2, 3, 4, TIMESTAMP '2026-04-10 09:40:00', NULL, 2, 1),
(2, 1, 4, 5, TIMESTAMP '2026-04-10 09:50:00', NULL, 2, 1);

INSERT INTO NEW_ORDER (NO_W_ID, NO_D_ID, NO_O_ID) VALUES
(1, 1, 2),
(1, 2, 3),
(2, 1, 4);

INSERT INTO ORDER_LINE (OL_W_ID, OL_D_ID, OL_O_ID, OL_NUMBER, OL_I_ID, OL_SUPPLY_W_ID, OL_DELIVERY_D, OL_QUANTITY, OL_AMOUNT, OL_DIST_INFO) VALUES
(1, 1, 1, 1, 1, 1, TIMESTAMP '2026-04-10 09:05:00', 2, 20.00, 'delivered line 1    '),
(1, 1, 1, 2, 2, 1, TIMESTAMP '2026-04-10 09:05:00', 1, 20.00, 'delivered line 2    '),
(1, 1, 2, 1, 3, 1, NULL, 2, 30.00, 'open line 1         '),
(1, 1, 2, 2, 4, 1, NULL, 1, 25.00, 'open line 2         '),
(1, 1, 2, 3, 5, 2, NULL, 1, 30.00, 'open line 3 remote  '),
(1, 2, 3, 1, 3, 1, NULL, 1, 15.00, 'open line 1 d2      '),
(1, 2, 3, 2, 6, 1, NULL, 2, 10.00, 'open line 2 d2      '),
(2, 1, 4, 1, 1, 2, NULL, 3, 30.00, 'open line 1 w2      '),
(2, 1, 4, 2, 5, 2, NULL, 1, 30.00, 'open line 2 w2      ');

INSERT INTO HISTORY (H_C_W_ID, H_C_D_ID, H_C_ID, H_D_ID, H_W_ID, H_DATE, H_AMOUNT, H_DATA) VALUES
(1, 1, 1, 1, 1, TIMESTAMP '2026-04-09 11:00:00', 50.00, 'prev payment alice  '),
(1, 1, 2, 1, 1, TIMESTAMP '2026-04-09 12:00:00', 30.00, 'prev payment bob    '),
(2, 1, 5, 1, 2, TIMESTAMP '2026-04-09 13:00:00', 70.00, 'prev payment eva    ');
