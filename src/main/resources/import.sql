-- Sample data loaded in dev mode only (see application.properties).
INSERT INTO tasks (title, description, status, priority, priority_weight, due_date, created_at, updated_at, completed_at)
VALUES ('Write onboarding guide', 'Short guide for new team members covering local setup.', 'IN_PROGRESS', 'HIGH', 3, DATEADD('DAY', 3, CURRENT_DATE), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL);
INSERT INTO tasks (title, description, status, priority, priority_weight, due_date, created_at, updated_at, completed_at)
VALUES ('Renew TLS certificate', 'Certificate for the staging domain expires soon.', 'TODO', 'URGENT', 4, DATEADD('DAY', -1, CURRENT_DATE), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL);
INSERT INTO tasks (title, description, status, priority, priority_weight, due_date, created_at, updated_at, completed_at)
VALUES ('Clean up old feature flags', NULL, 'TODO', 'LOW', 1, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL);
INSERT INTO tasks (title, description, status, priority, priority_weight, due_date, created_at, updated_at, completed_at)
VALUES ('Migrate CI to Java 21', 'Update build images and toolchains.', 'DONE', 'MEDIUM', 2, DATEADD('DAY', -5, CURRENT_DATE), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO tasks (title, description, status, priority, priority_weight, due_date, created_at, updated_at, completed_at)
VALUES ('Review vendor contract', 'Waiting on legal feedback.', 'BLOCKED', 'MEDIUM', 2, DATEADD('DAY', 10, CURRENT_DATE), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL);
