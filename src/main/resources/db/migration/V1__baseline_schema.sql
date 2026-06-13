-- payment.payment definition

CREATE TABLE `payment` (
                           `id` binary(16) NOT NULL,
                           `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                           `order_id` binary(16) NOT NULL,
                           `amount` decimal(10,2) NOT NULL,
                           `payment_method` varchar(20) NOT NULL,
                           `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           `updated_at` timestamp NOT NULL,
                           `user_id` binary(16) NOT NULL,
                           `provider_ref` varchar(100) NOT NULL,
                           PRIMARY KEY (`id`),
                           UNIQUE KEY `payment_unique` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;