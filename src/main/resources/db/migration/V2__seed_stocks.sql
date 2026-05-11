INSERT INTO stocks (id, ticker, name, sector, price, yield_pct, dividend_frequency) VALUES
('01J9TCRZ3B00000000000001', 'STRC', 'Sarcos Technology', 'Technology', 99.80, 11.50, 'MONTHLY'),
('01J9TCRZ3B00000000000002', 'ARCC', 'Ares Capital', 'Financial', 21.45, 10.30, 'QUARTERLY'),
('01J9TCRZ3B00000000000003', 'AGNC', 'AGNC Investment', 'Real Estate', 9.87, 14.00, 'MONTHLY'),
('01J9TCRZ3B00000000000004', 'JEPI', 'JPMorgan Equity Premium Income', 'ETF', 57.30, 7.20, 'MONTHLY'),
('01J9TCRZ3B00000000000005', 'JEPQ', 'JPMorgan Nasdaq Equity Premium Income', 'ETF', 55.10, 8.50, 'MONTHLY'),
('01J9TCRZ3B00000000000006', 'O', 'Realty Income', 'Real Estate', 58.40, 5.80, 'MONTHLY'),
('01J9TCRZ3B00000000000007', 'SCHD', 'Schwab US Dividend Equity', 'ETF', 78.90, 3.50, 'QUARTERLY'),
('01J9TCRZ3B00000000000008', 'VOO', 'Vanguard S&P 500', 'ETF', 490.20, 1.40, 'QUARTERLY'),
('01J9TCRZ3B00000000000009', 'MAIN', 'Main Street Capital', 'Financial', 52.30, 6.90, 'MONTHLY'),
('01J9TCRZ3B00000000000010', 'SPHD', 'Invesco S&P 500 High Dividend', 'ETF', 45.60, 4.20, 'MONTHLY')
ON CONFLICT (ticker) DO NOTHING;
