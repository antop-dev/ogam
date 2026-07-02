CREATE TABLE IF NOT EXISTS question (
    id       INTEGER PRIMARY KEY AUTOINCREMENT,
    option_a TEXT NOT NULL,
    option_b TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS room (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    code        TEXT    NOT NULL UNIQUE,
    status      TEXT    NOT NULL DEFAULT 'WAITING',
    current_seq INTEGER NOT NULL DEFAULT 0,
    created_at  TEXT    NOT NULL
);

CREATE TABLE IF NOT EXISTS player (
    id         TEXT    PRIMARY KEY,
    room_id    INTEGER NOT NULL,
    is_ready   INTEGER NOT NULL DEFAULT 0,
    created_at TEXT    NOT NULL
);

CREATE TABLE IF NOT EXISTS room_question (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    room_id     INTEGER NOT NULL,
    seq         INTEGER NOT NULL,
    question_id INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS answer (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    room_id    INTEGER NOT NULL,
    player_id  TEXT    NOT NULL,
    seq        INTEGER NOT NULL,
    choice     TEXT    NOT NULL,
    created_at TEXT    NOT NULL
);
