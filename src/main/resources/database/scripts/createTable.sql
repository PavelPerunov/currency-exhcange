create table Currencies
(
    id       INTEGER
        primary key autoincrement,
    code     varchar(128) not null
        unique,
    fullName varchar(128) not null,
    sign     varchar(128) not null
);

create table ExchangeRates
(
    id               INTEGER
        primary key autoincrement,
    baseCurrencyId   INT        not null
        references Currencies,
    targetCurrencyId INT        not null
        references Currencies,
    rate             Decimal(6) not null,
    unique (baseCurrencyId, targetCurrencyId)
);


