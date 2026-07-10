INSERT IGNORE INTO users (name, email, role, timezone) VALUES
    ('Alice Teacher',  'alice@example.com',  'TEACHER', 'America/New_York'),
    ('Bob Teacher',    'bob@example.com',    'TEACHER', 'Europe/London'),
    ('Carol Parent',   'carol@example.com',  'PARENT',  'Asia/Kolkata'),
    ('Dave Parent',    'dave@example.com',   'PARENT',  'Australia/Sydney');

INSERT IGNORE INTO courses (title, description) VALUES
    ('Minecraft Coding',    'Learn game design using Minecraft'),
    ('Python Basics',       'Introduction to programming with Python'),
    ('Art Drawing Class',   'Fundamentals of sketching and digital art'),
    ('Music class', 'Learn music in simple way');
