--liquibase formatted sql
--changeset cytomine:2026-08-26-seed-test-users-user-guest context:test

INSERT INTO sec_user (id, username, name, reference, public_key, private_key, enabled, account_expired, account_locked)
SELECT v.id, v.username, v.name, md5(v.username), v.public_key, v.private_key, true, false, false
FROM (VALUES
    (1104, 'user', 'firstname lastname', 'c9e42d0e-8e70-4d8c-9b0f-9f7b2f37f0a5', 'a37d6a24-13d5-4c0b-9f68-1b1ec5a6f2ab'),
    (1105, 'guest', 'firstname lastname', 'd1c2f5a8-3b47-4f6d-8a1e-6c9d0b4e7f31', 'e8b4c7d2-5a19-4e83-b6f0-2d7a1c9e4b56')
) AS v(id, username, name, public_key, private_key)
WHERE NOT EXISTS (SELECT 1 FROM sec_user u WHERE u.username = v.username);

SELECT setval(pg_get_serial_sequence('sec_user', 'id'), COALESCE((SELECT MAX(id) FROM sec_user), 0) + 1, false);

INSERT INTO sec_user_sec_role (sec_user_id, sec_role_id)
SELECT u.id, r.id
FROM (VALUES
    ('user', 'ROLE_USER'),
    ('guest', 'ROLE_GUEST')
) AS v(username, authority)
JOIN sec_user u ON u.username = v.username
JOIN sec_role r ON r.authority = v.authority
WHERE NOT EXISTS (SELECT 1 FROM sec_user_sec_role sur WHERE sur.sec_user_id = u.id AND sur.sec_role_id = r.id);
