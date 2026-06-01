INSERT INTO users (name, phone_number) VALUES
    ('홍길동', '010-1111-2222'),
    ('김철수', '010-2222-3333'),
    ('이영희', '010-3333-4444'),
    ('박민준', '010-4444-5555');

INSERT INTO rooms (room_number, room_name, creator_id, max_members)
VALUES ('ROOM-' || LPAD(CAST(NEXT VALUE FOR room_number_seq AS VARCHAR), 3, '0'), '강남 달리기 모임', 1, 5);
INSERT INTO rooms (room_number, room_name, creator_id, max_members)
VALUES ('ROOM-' || LPAD(CAST(NEXT VALUE FOR room_number_seq AS VARCHAR), 3, '0'), '한강 자전거 그룹', 2, 4);
INSERT INTO rooms (room_number, room_name, creator_id, max_members)
VALUES ('ROOM-' || LPAD(CAST(NEXT VALUE FOR room_number_seq AS VARCHAR), 3, '0'), '북한산 등산팀', 1, 2);


-- 홍길동(1), 김철수(2), 이영희(3) → 강남 달리기 모임(1)
-- 김철수(2), 이영희(3), 박민준(4) → 한강 자전거 그룹(2)
-- 홍길동(1), 박민준(4)            → 북한산 등산팀(3)
INSERT INTO user_rooms (user_id, room_id, joined_at) VALUES
    (1, 1, NOW()), (2, 1, NOW()), (3, 1, NOW()),
    (2, 2, NOW()), (3, 2, NOW()), (4, 2, NOW()),
    (1, 3, NOW()), (4, 3, NOW());

-- 홍길동(1): 강남구, 김철수(2): 마포구(한강변), 이영희(3): 여의도 한강공원, 박민준(4): 북한산 입구
INSERT INTO locations (user_id, latitude, longitude, recorded_at) VALUES
    (1, 37.4979, 127.0276, NOW()),
    (2, 37.5502, 126.9098, NOW()),
    (3, 37.5283, 126.9325, NOW()),
    (4, 37.6583, 126.9774, NOW());