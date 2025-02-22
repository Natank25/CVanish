CREATE TABLE IF NOT EXISTS applications
(
    user_id text PRIMARY KEY,
    first_name text NOT NULL,
    last_name text NOT NULL,
    email text NOT NULL,
    country text NOT NULL,
    city text NOT NULL
);
