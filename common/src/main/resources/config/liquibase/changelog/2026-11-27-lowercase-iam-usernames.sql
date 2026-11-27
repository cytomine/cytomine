--liquibase formatted sql
--changeset cytomine:2026-11-27-lowercase-iam-usernames

DROP TABLE IF EXISTS tmp_username_rename;

CREATE TEMPORARY TABLE tmp_username_rename AS
SELECT u.id,
       u.username AS old_username,
       lower(u.username) AS new_username
FROM sec_user u
WHERE u.username <> lower(u.username)
  AND (u.reference IS NULL
       OR u.reference ~* '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$')
  AND NOT EXISTS (
      SELECT 1
      FROM sec_user other
      WHERE other.id <> u.id
        AND other.username = lower(u.username)
  )
  AND NOT EXISTS (
      SELECT 1
      FROM acl_sid s
      WHERE s.principal = true
        AND s.sid = lower(u.username)
        AND s.sid <> u.username
  );

UPDATE sec_user u
SET username = r.new_username
FROM tmp_username_rename r
WHERE u.id = r.id;

UPDATE acl_sid s
SET sid = r.new_username
FROM tmp_username_rename r
WHERE s.principal = true
  AND s.sid = r.old_username;

DROP TABLE tmp_username_rename;

UPDATE acl_sid s
SET sid = lower(s.sid)
WHERE s.principal = true
  AND s.sid <> lower(s.sid)
  AND EXISTS (
      SELECT 1
      FROM sec_user u
      WHERE u.username = lower(s.sid)
        AND (u.reference IS NULL
             OR u.reference ~* '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$')
  )
  AND NOT EXISTS (
      SELECT 1
      FROM acl_sid other
      WHERE other.id <> s.id
        AND other.principal = true
        AND other.sid = lower(s.sid)
  );
