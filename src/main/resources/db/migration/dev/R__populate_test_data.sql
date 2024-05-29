INSERT INTO users (id, email, password, role)

VALUES ('84991c79-f6a9-4b7b-b1b4-0d66c0b92c81', 'user1@example.com',
        '$2a$12$7Cp4On1DBNyCkz4TaZYc3O.A.CBKi4WXgXnlI4SD0yn7CgBX5Gd6O', 'USER'),
       ('f6ff4ee4-92c4-49f3-97eb-d6c69a715325', 'user2@example.com',
        '$2a$12$7Cp4On1DBNyCkz4TaZYc3O.A.CBKi4WXgXnlI4SD0yn7CgBX5Gd6O', 'USER'),
       ('b95cb1b3-1a7e-4e8b-a7ef-f3e20aef5f0a', 'user3@example.com',
        '$2a$12$7Cp4On1DBNyCkz4TaZYc3O.A.CBKi4WXgXnlI4SD0yn7CgBX5Gd6O', 'ADMIN');

INSERT INTO links (id, long_link, short_link, user_id, created_time, expiration_time, statistics, status)
VALUES ('3053e49b-6da3-4389-9d06-23b2d57b6f25', 'https://www.youtube.com', 'short-link-1',
        '84991c79-f6a9-4b7b-b1b4-0d66c0b92c81', '2024-04-13 10:00:00', '2025-05-16 8:00:00', 100, 'ACTIVE'),
       ('5c8d1659-2a63-4b5e-8a0f-af6aefbf0baf', 'https://chat.openai.com', 'short-link-2',
        'f6ff4ee4-92c4-49f3-97eb-d6c69a715325', '2024-04-14 10:00:00', '2025-04-17 10:00:00', 150, 'ACTIVE'),
       ('3e486107-cbd3-45c0-8142-2e42342a1694', 'https://github.com', 'short-link-3',
        'b95cb1b3-1a7e-4e8b-a7ef-f3e20aef5f0a', '2024-04-15 10:00:00', '2025-05-19 10:00:00', 200, 'ACTIVE'),
       ('d2a8b2fc-0d25-4b13-9b4c-8b9ad443a5e1', 'https://www.codewars.com', 'short-link-4',
        '84991c79-f6a9-4b7b-b1b4-0d66c0b92c81', '2024-04-13 10:00:00', '2025-05-13 9:00:00', 600, 'ACTIVE'),
       ('9ef99e19-3986-4cf4-aa57-d6af46b72d7f', 'https://leetcode.com', 'short-link-5',
        'b95cb1b3-1a7e-4e8b-a7ef-f3e20aef5f0a', '2024-04-16 10:00:00', '2025-05-20 7:00:00', 199, 'INACTIVE');
