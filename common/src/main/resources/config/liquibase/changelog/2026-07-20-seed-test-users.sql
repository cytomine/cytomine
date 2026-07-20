--liquibase formatted sql
--changeset cytomine:2026-07-20-seed-test-users context:test

INSERT INTO sec_user (username, name, reference, enabled, account_expired, account_locked)
SELECT v.username, v.name, md5(v.username), true, false, false
FROM (VALUES
    ('admin', 'Just an Admin'),
    ('anotheruser', 'Just another User'),
    ('superadmin', 'Super Admin'),
    ('SUPER_ADMIN_ACL', 'firstname lastname'),
    ('ADMIN_ACL', 'firstname lastname'),
    ('ACL_USER_NO_ACL', 'firstname lastname'),
    ('USER_ACL_READ', 'firstname lastname'),
    ('USER_ACL_WRITE', 'firstname lastname'),
    ('USER_ACL_CREATE', 'firstname lastname'),
    ('USER_ACL_DELETE', 'firstname lastname'),
    ('USER_ACL_ADMIN', 'firstname lastname'),
    ('CREATOR', 'firstname lastname'),
    ('GUEST_ACL', 'firstname lastname')
) AS v(username, name)
WHERE NOT EXISTS (SELECT 1 FROM sec_user u WHERE u.username = v.username);

INSERT INTO sec_user_sec_role (sec_user_id, sec_role_id)
SELECT u.id, r.id
FROM (VALUES
    ('admin', 'ROLE_USER'), ('admin', 'ROLE_ADMIN'),
    ('anotheruser', 'ROLE_USER'), ('anotheruser', 'ROLE_ADMIN'), ('anotheruser', 'ROLE_SUPER_ADMIN'),
    ('superadmin', 'ROLE_USER'), ('superadmin', 'ROLE_ADMIN'), ('superadmin', 'ROLE_SUPER_ADMIN'),
    ('SUPER_ADMIN_ACL', 'ROLE_SUPER_ADMIN'),
    ('ADMIN_ACL', 'ROLE_ADMIN'),
    ('ACL_USER_NO_ACL', 'ROLE_USER'),
    ('USER_ACL_READ', 'ROLE_USER'),
    ('USER_ACL_WRITE', 'ROLE_USER'),
    ('USER_ACL_CREATE', 'ROLE_USER'),
    ('USER_ACL_DELETE', 'ROLE_USER'),
    ('USER_ACL_ADMIN', 'ROLE_USER'),
    ('CREATOR', 'ROLE_USER'),
    ('GUEST_ACL', 'ROLE_GUEST')
) AS v(username, authority)
JOIN sec_user u ON u.username = v.username
JOIN sec_role r ON r.authority = v.authority
WHERE NOT EXISTS (SELECT 1 FROM sec_user_sec_role sur WHERE sur.sec_user_id = u.id AND sur.sec_role_id = r.id);

INSERT INTO storage (name, user_id)
SELECT u.username || ' storage', u.id FROM sec_user u
WHERE u.username IN ('admin', 'anotheruser', 'superadmin')
AND NOT EXISTS (SELECT 1 FROM storage s WHERE s.name = u.username || ' storage');

INSERT INTO acl_sid (id, principal, sid)
SELECT nextval('hibernate_sequence'), true, u.username FROM sec_user u
WHERE u.username IN ('admin', 'anotheruser', 'superadmin')
AND NOT EXISTS (SELECT 1 FROM acl_sid sid WHERE sid.sid = u.username);

INSERT INTO acl_object_identity (id, object_id_class, entries_inheriting, object_id_identity, owner_sid)
SELECT nextval('hibernate_sequence'), c.id, true, s.id, sid.id
FROM storage s
JOIN sec_user u ON u.id = s.user_id
JOIN acl_sid sid ON sid.sid = u.username
JOIN acl_class c ON c.class = 'be.cytomine.domain.image.server.Storage'
WHERE u.username IN ('admin', 'anotheruser', 'superadmin')
AND NOT EXISTS (SELECT 1 FROM acl_object_identity aoi WHERE aoi.object_id_class = c.id AND aoi.object_id_identity = s.id);

INSERT INTO acl_entry (id, ace_order, acl_object_identity, audit_failure, audit_success, granting, mask, sid)
SELECT nextval('hibernate_sequence'), m.ace_order, aoi.id, false, false, true, m.mask, sid.id
FROM storage s
JOIN sec_user u ON u.id = s.user_id
JOIN acl_sid sid ON sid.sid = u.username
JOIN acl_object_identity aoi ON aoi.object_id_identity = s.id
CROSS JOIN (VALUES (0, 1), (1, 2), (2, 16)) AS m(ace_order, mask)
WHERE u.username IN ('admin', 'anotheruser', 'superadmin')
AND NOT EXISTS (SELECT 1 FROM acl_entry ae WHERE ae.acl_object_identity = aoi.id AND ae.sid = sid.id AND ae.mask = m.mask);
