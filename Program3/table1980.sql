create table vabram.RailIncident80 (
    RailCode varchar2(4),
    ino varchar2(10),
    gcid varchar2(7),
    idate date,
    itime varchar2(8),
    StateName varchar2(20),
    hwyUser varchar2(13),
    temp number(3),
    visilbility varchar2(4),
    WeatherCond varchar2(6),
    NumLocUnits number(2),
    NumCars number(3),
    primary key (ino, idate)
);