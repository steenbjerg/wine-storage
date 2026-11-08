-- This file allows writing SQL commands that will be emitted in dev and test.
-- Leaving examples commented out to avoid schema mismatches with the JPA model.
-- insert into myentity (id, field) values(1, 'field-1');
-- insert into myentity (id, field) values(2, 'field-2');
-- insert into myentity (id, field) values(3, 'field-3');
-- alter sequence myentity_seq restart with 4;

-- The following example insert was disabled because the entity uses embedded columns
-- (Price) and the column list was incomplete. Use ORM seeding (WineInitializer) instead
-- or update this statement to match the generated schema.
-- insert into wine (id, type, name, year, country, region, grape, alcoholPercentage, vendor, vendorUrl, vendorDescription, actualCost) values(1, 'RED', 'Chardonnay', 2021, 'France');

-- alter sequence myentity_seq restart with 4;
