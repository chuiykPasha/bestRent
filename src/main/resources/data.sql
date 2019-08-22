CREATE FULLTEXT index if not EXISTS fts_apartment_location on apartment(location);

INSERT IGNORE INTO apartment_comfort (is_active, name) VALUES (true, 'TV'),
                                                (true, 'Wi-fi'),
                                                (true, 'Fridge');

INSERT IGNORE INTO available_to_guest (is_active, name) VALUES (true, 'Private room'),
                                                        (true, 'Shared room'),
                                                        (true, 'Entire apartment');

INSERT IGNORE INTO type_of_house (is_active, name) VALUES (true, 'Flet'),
                                                    (true, 'House');
