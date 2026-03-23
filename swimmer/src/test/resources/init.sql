CREATE TABLE swimmers
(
    id             BIGSERIAL PRIMARY KEY,
    name           VARCHAR(255),
    coach_id       BIGINT,
    specialization VARCHAR(255)
);

INSERT INTO swimmers (name, coach_id, specialization)
VALUES ('Michael Phelps', 1, 'BUTTERFLY'),
       ('Katie Ledecky', 1, 'FREESTYLE'),
       ('Ryan Lochte', 2, 'BACKSTROKE'),
       ('Adam Peaty', 2, 'BREASTSTROKE'),
       ('Caeleb Dressel', 3, 'FREESTYLE'),
       ('Missy Franklin', 3, 'BACKSTROKE'),
       ('Chad le Clos', 1, 'BUTTERFLY'),
       ('Lilly King', 2, 'BREASTSTROKE'),
       ('Nathan Adrian', 3, 'FREESTYLE'),
       ('Regan Smith', 1, 'BACKSTROKE');
