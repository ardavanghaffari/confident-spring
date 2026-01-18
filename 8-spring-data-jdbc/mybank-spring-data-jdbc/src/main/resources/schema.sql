CREATE TABLE IF NOT EXISTS "transactions"
(
    id             UUID            DEFAULT random_uuid() PRIMARY KEY,
    amount         DECIMAL(15, 4)  NOT NULL,
    `timestamp`    TIMESTAMP       NOT NULL,
    reference      varchar(255)    NOT NULL,
    bank_slogan    varchar(255)    NOT NULL,
    receiving_user varchar(255)
);