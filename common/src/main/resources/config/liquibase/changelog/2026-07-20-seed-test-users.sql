--liquibase formatted sql
--changeset cytomine:2026-07-20-seed-test-users context:test

INSERT INTO sec_user (username, name, reference, public_key, private_key, enabled, account_expired, account_locked)
SELECT v.username, v.name, md5(v.username), v.public_key, v.private_key, true, false, false
FROM (VALUES
    ('admin', 'Just an Admin', 'b241e8aa-1f3a-423b-ab09-f2a85a59e333', '43f53264-846a-4486-b705-e3b0beaf299f'),
    ('anotheruser', 'Just another User', '0abe8501-86f4-499b-8b70-3b5995186a7a', '7fa5f009-10b6-48f6-ad75-eeed235748b0'),
    ('superadmin', 'Super Admin', '481a7adb-dccb-47fa-ba0a-3d44111272ec', '3e01451e-39af-4a99-b4a2-6f1045e0b7c1'),
    ('SUPER_ADMIN_ACL', 'firstname lastname', '49463976-afa3-4d08-95d8-c676b9e671d5', 'ec347fbf-d093-44aa-8541-271a1332d967'),
    ('ADMIN_ACL', 'firstname lastname', '589f097c-5237-4913-84cb-c6c632afdd4e', '9521a551-8cf0-462b-99f0-2d7ba96a7ed6'),
    ('ACL_USER_NO_ACL', 'firstname lastname', '453f7887-85f3-403e-98ff-3c02426f787d', 'd38fffa3-f000-413a-b6b7-d81b6fa20984'),
    ('USER_ACL_READ', 'firstname lastname', '5c10f7ee-81dc-4379-a0d3-b10dbdafaed4', 'bbf3f35f-088d-41cf-addf-43b271f2e9e1'),
    ('USER_ACL_WRITE', 'firstname lastname', '54cf588b-cbb3-46c1-8bf4-62d804b588e1', '1305180e-3677-4b2a-a534-82e641b7f0ed'),
    ('USER_ACL_CREATE', 'firstname lastname', '501337d4-8c75-4a0d-a1e4-6fb9f56f3c30', 'e0565049-e32b-4436-bd70-6c9f40d2ea49'),
    ('USER_ACL_DELETE', 'firstname lastname', '9f885bf8-bc78-4eef-92bb-75c7ba3a25b1', '6bf408ca-cb8e-411e-971b-bfb06ac4bda2'),
    ('USER_ACL_ADMIN', 'firstname lastname', 'ff6028cb-1acb-412b-8b8c-d87af32108a3', '523d8f60-0792-437a-851e-d4270a29fba9'),
    ('CREATOR', 'firstname lastname', '8600f300-4c83-47da-b30a-511ac947b3ae', '6d9a1b84-ad52-4192-94d7-777aba9ebb52'),
    ('GUEST_ACL', 'firstname lastname', 'f0f11583-4d28-4f72-b89a-edeab395b6ec', 'aeb17143-909c-490f-b3a4-2852cf8eca4d')
) AS v(username, name, public_key, private_key)
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
