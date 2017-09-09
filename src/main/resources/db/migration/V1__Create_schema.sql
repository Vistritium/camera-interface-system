CREATE TABLE presets (
  id          INTEGER PRIMARY KEY AUTOINCREMENT,
  name        VARCHAR NOT NULL,
  displayName TEXT    NULL,
  CONSTRAINT name_is_unique UNIQUE (name)
);

CREATE TABLE images (
  id         INTEGER PRIMARY KEY AUTOINCREMENT,
  fullpath   VARCHAR  NOT NULL,
  filename   VARCHAR  NOT NULL,
  photoTaken DATETIME NOT NULL,
  presetId   INTEGER  NOT NULL,
  hourTaken  INTEGER  NOT NULL,
  FOREIGN KEY (presetId) REFERENCES presets (id),
  CONSTRAINT fullpath_is_unique UNIQUE (fullpath)
);