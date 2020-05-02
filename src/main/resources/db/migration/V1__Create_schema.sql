CREATE TABLE presets
(
    id          SERIAL PRIMARY KEY,
    name        TEXT NOT NULL UNIQUE,
    display_name TEXT NULL
);

CREATE TABLE images
(
    id         SERIAL PRIMARY KEY,
    full_path   TEXT        NOT NULL,
    file_name   TEXT        NOT NULL,
    photo_taken TIMESTAMPTZ NOT NULL,
    preset_id   INTEGER     NOT NULL REFERENCES presets (id),
    hour_taken  INTEGER     NOT NULL
);

CREATE UNIQUE INDEX imagesFullPath ON images (full_path);
CREATE INDEX imageGeneralPurposeIdx ON images (photo_taken, preset_id, hour_taken);

INSERT INTO presets (id, name, display_name) VALUES (1, 'gorka', 'Górka');
INSERT INTO presets (id, name, display_name) VALUES (2, 'stol', 'Stół');
INSERT INTO presets (id, name, display_name) VALUES (3, 'beczka', 'Beczka');
INSERT INTO presets (id, name, display_name) VALUES (4, 'ogrod wisnia', 'Ogród - wiśnia');
INSERT INTO presets (id, name, display_name) VALUES (5, 'altana', 'Altana');
INSERT INTO presets (id, name, display_name) VALUES (6, 'doniczki', 'Doniczki');
INSERT INTO presets (id, name, display_name) VALUES (7, 'las', 'Las');
INSERT INTO presets (id, name, display_name) VALUES (8, 'ogrod swierki', 'Ogród świerki');
INSERT INTO presets (id, name, display_name) VALUES (9, 'hustawka', 'Huśtawka');
INSERT INTO presets (id, name, display_name) VALUES (10, 'kolo', 'Koło');
INSERT INTO presets (id, name, display_name) VALUES (11, 'azalia', 'Azalia');