CREATE UNIQUE INDEX imageFullpathIdx ON images (fullpath);
CREATE INDEX imageGeneralPurposeIdx ON images (photoTaken, presetId, hourTaken);