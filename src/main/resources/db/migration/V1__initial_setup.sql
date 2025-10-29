-- MySQL 8.x schema (InnoDB + utf8mb4)

-- Users Table
CREATE TABLE `users` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `email` VARCHAR(100) NOT NULL,
    `status` VARCHAR(20) DEFAULT 'OFFLINE',
    `last_seen` DATETIME(3) NULL,
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_users_username` (`username`),
    UNIQUE KEY `uk_users_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Chat Rooms Table
CREATE TABLE `chat_rooms` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `description` TEXT NULL,
    `type` VARCHAR(20) DEFAULT 'PUBLIC',
    `created_by` BIGINT NULL,
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    KEY `idx_chat_rooms_created_by` (`created_by`),
    CONSTRAINT `fk_chat_rooms_created_by`
        FOREIGN KEY (`created_by`) REFERENCES `users` (`id`)
        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Messages Table
CREATE TABLE `messages` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `sender_id` BIGINT NOT NULL,
    `chat_room_id` BIGINT NOT NULL,
    `content` TEXT NOT NULL,
    `type` VARCHAR(20) DEFAULT 'TEXT',
    `timestamp` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `edited` BOOLEAN DEFAULT FALSE,
    `edited_at` DATETIME(3) NULL,
    PRIMARY KEY (`id`),
    KEY `idx_messages_chat_room_timestamp` (`chat_room_id`, `timestamp`),
    KEY `idx_messages_sender` (`sender_id`),
    CONSTRAINT `fk_messages_sender`
        FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT `fk_messages_chat_room`
        FOREIGN KEY (`chat_room_id`) REFERENCES `chat_rooms` (`id`)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- User Chat Rooms Junction Table
CREATE TABLE `user_chat_rooms` (
    `user_id` BIGINT NOT NULL,
    `chat_room_id` BIGINT NOT NULL,
    PRIMARY KEY (`user_id`, `chat_room_id`),
    KEY `idx_user_chat_rooms_chat_room` (`chat_room_id`),
    CONSTRAINT `fk_ucr_user`
        FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT `fk_ucr_chat_room`
        FOREIGN KEY (`chat_room_id`) REFERENCES `chat_rooms` (`id`)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Message Readers Table
CREATE TABLE `message_readers` (
    `message_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    PRIMARY KEY (`message_id`, `user_id`),
    KEY `idx_message_readers_user` (`user_id`),
    CONSTRAINT `fk_msg_readers_message`
        FOREIGN KEY (`message_id`) REFERENCES `messages` (`id`)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT `fk_msg_readers_user`
        FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;