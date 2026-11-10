--liquibase formatted sql
--changeset cytomine:2026-08-18-seed-test-users-role-hierarchy context:test

INSERT INTO sec_user_sec_role (sec_user_id, sec_role_id)
SELECT u.id, r.id
FROM (VALUES
    ('SUPER_ADMIN_ACL', 'ROLE_USER'), ('SUPER_ADMIN_ACL', 'ROLE_ADMIN'),
    ('ADMIN_ACL', 'ROLE_USER')
) AS v(username, authority)
JOIN sec_user u ON u.username = v.username
JOIN sec_role r ON r.authority = v.authority
WHERE NOT EXISTS (SELECT 1 FROM sec_user_sec_role sur WHERE sur.sec_user_id = u.id AND sur.sec_role_id = r.id);
