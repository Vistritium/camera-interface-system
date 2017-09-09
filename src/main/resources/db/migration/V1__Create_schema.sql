CREATE TABLE presets (
  id          INTEGER PRIMARY KEY,
  name        VARCHAR NOT NULL,
  displayName TEXT    NOT NULL
);

CREATE TABLE images (
  id         INTEGER PRIMARY KEY AUTOINCREMENT,
  fullpath   VARCHAR  NOT NULL,
  filename   VARCHAR  NOT NULL,
  photoTaken DATETIME NOT NULL,
  presetId   INTEGER  NOT NULL,
  FOREIGN KEY (presetId) REFERENCES presets (id)
);