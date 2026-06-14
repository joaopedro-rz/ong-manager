-- Convert existing DONOR users to VOLUNTEER, then remove DONOR role.
INSERT INTO user_roles (user_id, role_id)
SELECT ur.user_id, rv.id
FROM user_roles ur
JOIN roles rd ON rd.id = ur.role_id AND rd.name = 'DONOR'
JOIN roles rv ON rv.name = 'VOLUNTEER'
ON CONFLICT DO NOTHING;

DELETE FROM user_roles
WHERE role_id = (SELECT id FROM roles WHERE name = 'DONOR');

DELETE FROM roles WHERE name = 'DONOR';

