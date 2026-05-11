INSERT INTO stocks (ticker, name, stock_type, current_yield, payout_frequency, risk_level) VALUES
('STRC', 'Strategy Stretch',        'preferred', 11.5, 'monthly',   'high'),
('STRF', 'Strategy Strife',         'preferred', 10.0, 'monthly',   'high'),
('STRK', 'Strategy Strike',         'preferred',  8.0, 'monthly',   'high'),
('ARCC', 'Ares Capital',            'bdc',        10.3, 'quarterly', 'medium'),
('JEPI', 'JPMorgan Equity Premium', 'etf',         9.0, 'monthly',   'medium'),
('JEPQ', 'Nasdaq Covered Call ETF', 'etf',        10.0, 'monthly',   'medium'),
('MAIN', 'Main Street Capital',     'bdc',         7.0, 'monthly',   'low'),
('O',    'Realty Income',           'reit',        5.0, 'monthly',   'low'),
('AGNC', 'AGNC Investment',         'reit',       14.0, 'monthly',   'high')
ON CONFLICT (ticker) DO NOTHING;
