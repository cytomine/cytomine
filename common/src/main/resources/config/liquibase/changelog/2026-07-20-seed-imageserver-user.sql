--liquibase formatted sql
--changeset cytomine:2026-07-20-seed-imageserver-user

INSERT INTO sec_user (username, name, reference, enabled, account_expired, account_locked, public_key, private_key)
SELECT 'ImageServer1', 'Image Server', md5('ImageServer1'), true, false, false, gen_random_uuid()::text, gen_random_uuid()::text
WHERE NOT EXISTS (SELECT 1 FROM sec_user WHERE username = 'ImageServer1');

--changeset cytomine:2026-07-20-seed-imageserver-user-keys context:test
UPDATE sec_user
SET public_key = '9a8b8369-2446-44cb-b0af-604a74e1dcdb',
    private_key = 'd70607f5-c478-403c-be11-3dbc1716d1cf'
WHERE username = 'ImageServer1';

INSERT INTO sec_user_sec_role (sec_user_id, sec_role_id)
SELECT u.id, r.id FROM sec_user u, sec_role r
WHERE u.username = 'ImageServer1' AND r.authority IN ('ROLE_USER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')
AND NOT EXISTS (SELECT 1 FROM sec_user_sec_role sur WHERE sur.sec_user_id = u.id AND sur.sec_role_id = r.id);

INSERT INTO storage (name, user_id)
SELECT 'ImageServer1 storage', u.id FROM sec_user u
WHERE u.username = 'ImageServer1'
AND NOT EXISTS (SELECT 1 FROM storage WHERE name = 'ImageServer1 storage');

INSERT INTO acl_class (id, class)
SELECT nextval('hibernate_sequence'), 'be.cytomine.domain.image.server.Storage'
WHERE NOT EXISTS (SELECT 1 FROM acl_class WHERE class = 'be.cytomine.domain.image.server.Storage');

INSERT INTO acl_sid (id, principal, sid)
SELECT nextval('hibernate_sequence'), true, 'ImageServer1'
WHERE NOT EXISTS (SELECT 1 FROM acl_sid WHERE sid = 'ImageServer1');

INSERT INTO acl_object_identity (id, object_id_class, entries_inheriting, object_id_identity, owner_sid)
SELECT nextval('hibernate_sequence'), c.id, true, s.id, sid.id
FROM acl_class c, storage s, acl_sid sid
WHERE c.class = 'be.cytomine.domain.image.server.Storage'
AND s.name = 'ImageServer1 storage'
AND sid.sid = 'ImageServer1'
AND NOT EXISTS (SELECT 1 FROM acl_object_identity aoi WHERE aoi.object_id_class = c.id AND aoi.object_id_identity = s.id);

INSERT INTO acl_entry (id, ace_order, acl_object_identity, audit_failure, audit_success, granting, mask, sid)
SELECT nextval('hibernate_sequence'), 0, aoi.id, false, false, true, 1, sid.id
FROM acl_object_identity aoi, storage s, acl_sid sid
WHERE aoi.object_id_identity = s.id AND s.name = 'ImageServer1 storage' AND sid.sid = 'ImageServer1'
AND NOT EXISTS (SELECT 1 FROM acl_entry ae WHERE ae.acl_object_identity = aoi.id AND ae.sid = sid.id AND ae.mask = 1);

INSERT INTO acl_entry (id, ace_order, acl_object_identity, audit_failure, audit_success, granting, mask, sid)
SELECT nextval('hibernate_sequence'), 1, aoi.id, false, false, true, 2, sid.id
FROM acl_object_identity aoi, storage s, acl_sid sid
WHERE aoi.object_id_identity = s.id AND s.name = 'ImageServer1 storage' AND sid.sid = 'ImageServer1'
AND NOT EXISTS (SELECT 1 FROM acl_entry ae WHERE ae.acl_object_identity = aoi.id AND ae.sid = sid.id AND ae.mask = 2);

INSERT INTO acl_entry (id, ace_order, acl_object_identity, audit_failure, audit_success, granting, mask, sid)
SELECT nextval('hibernate_sequence'), 2, aoi.id, false, false, true, 16, sid.id
FROM acl_object_identity aoi, storage s, acl_sid sid
WHERE aoi.object_id_identity = s.id AND s.name = 'ImageServer1 storage' AND sid.sid = 'ImageServer1'
AND NOT EXISTS (SELECT 1 FROM acl_entry ae WHERE ae.acl_object_identity = aoi.id AND ae.sid = sid.id AND ae.mask = 16);
