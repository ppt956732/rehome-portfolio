-- Active: 1764346354646@@127.0.0.1@3306@rehome
-- MySQL dump 10.13  Distrib 8.0.19, for Win64 (x86_64)
--
-- Host: 43.203.134.113    Database: rehome
-- ------------------------------------------------------
-- Server version	8.0.44-0ubuntu0.24.04.2

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `adoption_member`
--

DROP TABLE IF EXISTS `adoption_member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `adoption_member` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '領養申請id',
  `case_id` int unsigned NOT NULL COMMENT '案號id (FK)',
  `member_id` int unsigned NOT NULL COMMENT '領養者id (FK)',
  `adoption_status_id` int unsigned NOT NULL COMMENT '領養狀態id (FK)',
  `marital_status` enum('single','married') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '婚姻狀態',
  `employment_status` enum('student','employed','unemployed') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '工作狀態',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '申請領養時間',
  `end_at` datetime DEFAULT NULL COMMENT '申請領養結束時間',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_member_case` (`case_id`,`member_id`) USING BTREE,
  KEY `member_id` (`member_id`),
  KEY `adoption_status_id` (`adoption_status_id`),
  KEY `adoption_member_case_id_IDX` (`case_id`) USING BTREE,
  CONSTRAINT `adoption_member_ibfk_1` FOREIGN KEY (`case_id`) REFERENCES `cases` (`id`),
  CONSTRAINT `adoption_member_ibfk_2` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`),
  CONSTRAINT `adoption_member_ibfk_3` FOREIGN KEY (`adoption_status_id`) REFERENCES `adoption_status` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `adoption_pet_area`
--

DROP TABLE IF EXISTS `adoption_pet_area`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `adoption_pet_area` (
  `case_id` int unsigned NOT NULL COMMENT '案號id (FK, PK)',
  `city_id` int unsigned NOT NULL COMMENT '縣市id (FK, PK)',
  PRIMARY KEY (`case_id`,`city_id`),
  KEY `city_id` (`city_id`),
  CONSTRAINT `adoption_pet_area_ibfk_1` FOREIGN KEY (`case_id`) REFERENCES `cases` (`id`),
  CONSTRAINT `adoption_pet_area_ibfk_2` FOREIGN KEY (`city_id`) REFERENCES `city` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `adoption_question`
--

DROP TABLE IF EXISTS `adoption_question`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `adoption_question` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '領養問卷回答id',
  `adoption_member_id` int unsigned NOT NULL COMMENT '案件與領養者id (FK)',
  `question_id` int NOT NULL COMMENT '問卷題目id (FK)',
  `answer` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '問題回答',
  PRIMARY KEY (`id`),
  KEY `adoption_member_id` (`adoption_member_id`),
  KEY `question_id` (`question_id`),
  CONSTRAINT `adoption_question_ibfk_1` FOREIGN KEY (`adoption_member_id`) REFERENCES `adoption_member` (`id`),
  CONSTRAINT `adoption_question_ibfk_2` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `adoption_status`
--

DROP TABLE IF EXISTS `adoption_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `adoption_status` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '領養狀態id',
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '領養狀態名稱',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `animal_species`
--

DROP TABLE IF EXISTS `animal_species`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `animal_species` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '寵物種類id',
  `name` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '動物種類名稱',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `banner`
--

DROP TABLE IF EXISTS `banner`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `banner` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '輪播圖id',
  `banner_lg` mediumblob COMMENT '輪播圖 大',
  `banner_sm` mediumblob COMMENT '輪播圖 小',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '標題',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '圖片路徑',
  `link_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '點擊跳轉連結',
  `sort_order` int DEFAULT '0' COMMENT '排序權重',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '是否顯示',
  `created_at` datetime DEFAULT NULL COMMENT '建立時間',
  `update_at` datetime DEFAULT NULL COMMENT '更新時間',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `case_status`
--

DROP TABLE IF EXISTS `case_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `case_status` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '案件狀態id',
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '案件狀態名稱',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `case_type`
--

DROP TABLE IF EXISTS `case_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `case_type` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '案件種類id',
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '案件種類名稱',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cases`
--

DROP TABLE IF EXISTS `cases`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cases` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '案號id',
  `case_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '案件編號',
  `case_type_id` int unsigned NOT NULL COMMENT '案件種類id (FK)',
  `case_status_id` int unsigned NOT NULL COMMENT '案件狀態id (FK)',
  `member_id` int unsigned NOT NULL COMMENT '建案會員id (FK)',
  `case_date_start` datetime DEFAULT NULL COMMENT '立案時間',
  `case_date_end` datetime DEFAULT NULL COMMENT '結案時間',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '案件描述',
  PRIMARY KEY (`id`),
  UNIQUE KEY `case_number` (`case_number`),
  KEY `case_status_id` (`case_status_id`),
  KEY `member_id` (`member_id`),
  KEY `case_type_id_IDX` (`case_type_id`,`case_status_id`,`case_date_start` DESC) USING BTREE,
  KEY `case_date_start` (`case_date_start`) USING BTREE,
  KEY `case_type_id` (`case_type_id`) USING BTREE,
  CONSTRAINT `cases_ibfk_1` FOREIGN KEY (`case_type_id`) REFERENCES `case_type` (`id`),
  CONSTRAINT `cases_ibfk_2` FOREIGN KEY (`case_status_id`) REFERENCES `case_status` (`id`),
  CONSTRAINT `cases_ibfk_3` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `chatroom`
--

DROP TABLE IF EXISTS `chatroom`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chatroom` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '聊天室id',
  `room_type` enum('USER_TO_USER','CS_TO_USER') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '聊天類型',
  `user_a_id` int unsigned NOT NULL COMMENT 'A方 (FK) - 送養人',
  `user_b_id` int unsigned NOT NULL COMMENT 'B方 (FK) - 領養人',
  `create_date` datetime DEFAULT NULL COMMENT '創建時間',
  `last_mesg_id` int DEFAULT NULL COMMENT '最後訊息id',
  `last_mesg_date` datetime DEFAULT NULL COMMENT '最後訊息時間',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '客服描述',
  PRIMARY KEY (`id`),
  KEY `user_a_id` (`user_a_id`),
  KEY `user_b_id` (`user_b_id`),
  CONSTRAINT `chatroom_ibfk_1` FOREIGN KEY (`user_a_id`) REFERENCES `member` (`id`),
  CONSTRAINT `chatroom_ibfk_2` FOREIGN KEY (`user_b_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `city`
--

DROP TABLE IF EXISTS `city`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `city` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '縣市id',
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '縣市名稱',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `contact`
--

DROP TABLE IF EXISTS `contact`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contact` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '聯絡人id',
  `case_id` int unsigned NOT NULL COMMENT '案號id (FK)',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '聯絡稱謂',
  `tel` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '連絡電話',
  `mail` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '聯絡mail',
  `other_contact` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '其他聯絡方式',
  `is_phone_display` tinyint(1) DEFAULT NULL COMMENT '電話顯示/不顯示',
  `is_email_display` tinyint(1) DEFAULT NULL COMMENT 'email顯示/不顯示',
  `shelter_id` int unsigned DEFAULT NULL COMMENT '收容所id (FK)',
  PRIMARY KEY (`id`),
  KEY `case_id` (`case_id`),
  KEY `shelter_id` (`shelter_id`),
  CONSTRAINT `contact_ibfk_1` FOREIGN KEY (`case_id`) REFERENCES `cases` (`id`),
  CONSTRAINT `contact_ibfk_2` FOREIGN KEY (`shelter_id`) REFERENCES `shelter` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cs_message`
--

DROP TABLE IF EXISTS `cs_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cs_message` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '訊息號',
  `sender_id` int NOT NULL COMMENT '寄件者ID',
  `receiver_id` int NOT NULL COMMENT '收件者ID',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '對話內容',
  `img` mediumblob COMMENT '圖片 (Medium Blob)',
  `sent_at` datetime DEFAULT NULL COMMENT '發送時間',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客服專用訊息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `customer_service_form`
--

DROP TABLE IF EXISTS `customer_service_form`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customer_service_form` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '客服表單id',
  `question_title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '問題標題',
  `question_info` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '問題描述',
  `cname` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '稱謂',
  `cmail` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '聯絡mail',
  `question_type_id` int unsigned DEFAULT NULL COMMENT '問題種類id (FK)',
  `form_date` datetime DEFAULT NULL COMMENT '表單時間',
  `reply` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '客服回覆',
  `reply_date` datetime DEFAULT NULL COMMENT '客服回覆時間',
  `status` enum('unprocessed','processed') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '表單狀態',
  PRIMARY KEY (`id`),
  KEY `question_type_id` (`question_type_id`),
  CONSTRAINT `customer_service_form_ibfk_1` FOREIGN KEY (`question_type_id`) REFERENCES `question_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `email_otp`
--

DROP TABLE IF EXISTS `email_otp`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `email_otp` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'OTP紀錄id',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '驗證的Email',
  `otp_code` varchar(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '6位數驗證碼',
  `created_at` datetime NOT NULL COMMENT '建立時間',
  `expires_at` datetime NOT NULL COMMENT '過期時間',
  `verified` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已驗證',
  PRIMARY KEY (`id`),
  KEY `idx_email` (`email`),
  KEY `idx_expires_at` (`expires_at`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Email OTP驗證表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `favorite`
--

DROP TABLE IF EXISTS `favorite`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `favorite` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '收藏id',
  `member_id` int unsigned NOT NULL COMMENT '使用者ID(FK)',
  `case_id` int unsigned NOT NULL COMMENT '案號id (FK)',
  `favorites_date` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '收藏時間',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_member_case` (`member_id`,`case_id`),
  KEY `case_id` (`case_id`),
  CONSTRAINT `favorite_ibfk_1` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`),
  CONSTRAINT `favorite_ibfk_2` FOREIGN KEY (`case_id`) REFERENCES `cases` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lost_notification`
--

DROP TABLE IF EXISTS `lost_notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lost_notification` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '站內信id',
  `case_id` int unsigned NOT NULL COMMENT '案號id (FK)',
  `message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '訊息內容',
  `send_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '訊息日期',
  PRIMARY KEY (`id`),
  KEY `case_id` (`case_id`),
  CONSTRAINT `lost_notification_ibfk_1` FOREIGN KEY (`case_id`) REFERENCES `cases` (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `member`
--

DROP TABLE IF EXISTS `member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `member` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '使用者唯一 ID',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '帳號 (Email)，登入用',
  `password_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '加密後的密碼',
  `icon` mediumblob COMMENT '圖片',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '姓名',
  `nick_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '暱稱',
  `gender` tinyint(1) DEFAULT NULL COMMENT '性別 (T:男/F:女)',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手機號碼',
  `birth_date` date DEFAULT NULL COMMENT '生日',
  `role` enum('visitor','member','admin') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'member' COMMENT '權限角色',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '註冊時間',
  `updated_at` datetime DEFAULT NULL COMMENT '更新時間',
  `status` enum('active','block') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '活躍Active /黑名單 Block',
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `member_refresh_token`
--

DROP TABLE IF EXISTS `member_refresh_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `member_refresh_token` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主鍵',
  `member_id` int unsigned NOT NULL COMMENT '會員 ID',
  `refresh_token_hash` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Refresh Token SHA-256 Hash',
  `expires_at` datetime NOT NULL COMMENT 'Refresh Token 過期時間',
  `revoked` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已撤銷',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
  `session_expires_at` datetime NOT NULL COMMENT '最終存活時間',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_refresh_token_hash` (`refresh_token_hash`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_refresh_token_hash` (`refresh_token_hash`),
  KEY `idx_expires_at` (`expires_at`),
  CONSTRAINT `member_refresh_token_member_FK` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='會員 Refresh Token';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `password_reset_token`
--

DROP TABLE IF EXISTS `password_reset_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `password_reset_token` (
  `id` int NOT NULL AUTO_INCREMENT,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `token` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime NOT NULL,
  `expires_at` datetime NOT NULL,
  `used` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `token` (`token`),
  KEY `idx_token` (`token`),
  KEY `email` (`email`),
  CONSTRAINT `password_reset_token_ibfk_1` FOREIGN KEY (`email`) REFERENCES `member` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pet_detail`
--

DROP TABLE IF EXISTS `pet_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pet_detail` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '寵物詳細id',
  `case_id` int unsigned NOT NULL COMMENT '案號id (FK)',
  `lost_date` date DEFAULT NULL COMMENT '遺失日期',
  `lost_region_id` int unsigned DEFAULT NULL COMMENT '鄉鎮區(FK)',
  `lost_addr` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '街道地址/地標描述',
  `lng` decimal(10,6) DEFAULT NULL COMMENT '經度',
  `lat` decimal(10,6) DEFAULT NULL COMMENT '緯度',
  `lost_process` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '遺失經過',
  `is_follow_ager` tinyint(1) DEFAULT NULL COMMENT '後須追蹤',
  `is_family_ager` tinyint(1) DEFAULT NULL COMMENT '家人同意',
  `is_age_limit` tinyint(1) DEFAULT NULL COMMENT '須滿20',
  `adoption_requ` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '領養條件',
  `medical_info` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '醫療狀態說明',
  `found_place` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '動物尋獲地',
  `entry_date` date DEFAULT NULL COMMENT '入所日期',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '領養說明',
  PRIMARY KEY (`id`),
  KEY `case_id` (`case_id`),
  KEY `lost_region_id` (`lost_region_id`),
  CONSTRAINT `pet_detail_ibfk_1` FOREIGN KEY (`case_id`) REFERENCES `cases` (`id`),
  CONSTRAINT `pet_detail_ibfk_2` FOREIGN KEY (`lost_region_id`) REFERENCES `region` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pet_image`
--

DROP TABLE IF EXISTS `pet_image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pet_image` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '圖片id',
  `case_id` int unsigned NOT NULL COMMENT '案號id (FK)',
  `photo` mediumblob COMMENT '寵物圖片',
  `photo_url` varchar(512) COMMENT '寵物圖片網址',
  `sort_order` int DEFAULT NULL COMMENT '排序權重',
  PRIMARY KEY (`id`),
  KEY `pet_image_case_id_IDX` (`case_id`,`sort_order`) USING BTREE,
  CONSTRAINT `pet_image_ibfk_1` FOREIGN KEY (`case_id`) REFERENCES `cases` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pet_info`
--

DROP TABLE IF EXISTS `pet_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pet_info` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '寵物id',
  `case_id` int unsigned NOT NULL COMMENT '案號id (FK)',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '寵物名稱',
  `animal_species_id` int unsigned NOT NULL COMMENT '寵物種類id(FK)',
  `animal_species_other` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '其他寵物種類',
  `gender` enum('male','female','unknown') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '寵物性別',
  `breed` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '寵物品種',
  `color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '寵物毛色',
  `size` enum('small','medium','big') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '寵物體型',
  `age` enum('child','adult','old') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '寵物年紀',
  `feature` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '寵物特徵',
  `is_ear_tipping` tinyint(1) DEFAULT NULL COMMENT '寵物有無剪耳/結紮',
  `is_chip` tinyint(1) DEFAULT NULL COMMENT '寵物有無晶片',
  `chip_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '寵物晶片號碼',
  `region_id` int unsigned DEFAULT NULL COMMENT '所在地區id (FK)',
  PRIMARY KEY (`id`),
  KEY `case_id` (`case_id`),
  KEY `animal_species_id` (`animal_species_id`),
  KEY `region_id` (`region_id`),
  KEY `size` (`size`) USING BTREE,
  KEY `age` (`age`) USING BTREE,
  KEY `gender` (`gender`) USING BTREE,
  KEY `is_ear_tipping` (`is_ear_tipping`) USING BTREE,
  KEY `is_chip` (`is_chip`) USING BTREE,
  CONSTRAINT `pet_info_ibfk_1` FOREIGN KEY (`case_id`) REFERENCES `cases` (`id`),
  CONSTRAINT `pet_info_ibfk_2` FOREIGN KEY (`animal_species_id`) REFERENCES `animal_species` (`id`),
  CONSTRAINT `pet_info_ibfk_3` FOREIGN KEY (`region_id`) REFERENCES `region` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `qna`
--

DROP TABLE IF EXISTS `qna`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qna` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '常見問題id',
  `question` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '問題敘述',
  `answer` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '問題解答',
  `question_type_id` int unsigned DEFAULT NULL COMMENT '問題種類id (FK)',
  PRIMARY KEY (`id`),
  KEY `question_type_id` (`question_type_id`),
  CONSTRAINT `qna_ibfk_1` FOREIGN KEY (`question_type_id`) REFERENCES `question_type` (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `question`
--

DROP TABLE IF EXISTS `question`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `question` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '問卷題目id',
  `question` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '題目',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '內容框',
  `sort_order` int DEFAULT NULL COMMENT '題目排序',
  `question_category` enum('adoption','default') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '問卷分類',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '最後更新時間',
  `is_active` tinyint NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `question_type`
--

DROP TABLE IF EXISTS `question_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `question_type` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '問題種類id',
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '問題種類名稱',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `region`
--

DROP TABLE IF EXISTS `region`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `region` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '地區id',
  `city_id` int unsigned NOT NULL COMMENT '縣市id (FK)',
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鄉鎮市區名稱',
  PRIMARY KEY (`id`),
  KEY `city_id` (`city_id`),
  CONSTRAINT `region_ibfk_1` FOREIGN KEY (`city_id`) REFERENCES `city` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `shelter`
--

DROP TABLE IF EXISTS `shelter`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shelter` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '收容所id',
  `region_id` int unsigned NOT NULL COMMENT '所屬地區id (FK)',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收容所名稱',
  `address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '收容所地址',
  `phone` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '收容所電話',
  `lng` decimal(10,6) DEFAULT NULL COMMENT '經度',
  `lat` decimal(10,6) DEFAULT NULL COMMENT '緯度',
  PRIMARY KEY (`id`),
  KEY `region_id` (`region_id`),
  CONSTRAINT `shelter_ibfk_1` FOREIGN KEY (`region_id`) REFERENCES `region` (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sticker`
--

DROP TABLE IF EXISTS `sticker`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sticker` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '圖庫id',
  `image` mediumblob COMMENT '圖',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `store_mesg`
--

DROP TABLE IF EXISTS `store_mesg`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `store_mesg` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '訊息id',
  `chatroom_id` int NOT NULL COMMENT '聊天室id (FK)',
  `sender_id` int unsigned NOT NULL COMMENT '發送者id (FK)',
  `text` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '對話類型(文字)',
  `img` mediumblob COMMENT '對話類型(圖片)',
  `time` datetime DEFAULT NULL COMMENT '發送時間',
  `is_read` tinyint(1) DEFAULT NULL COMMENT '是否已讀',
  PRIMARY KEY (`id`),
  KEY `chatroom_id` (`chatroom_id`),
  KEY `sender_id` (`sender_id`),
  CONSTRAINT `store_mesg_ibfk_1` FOREIGN KEY (`chatroom_id`) REFERENCES `chatroom` (`id`),
  CONSTRAINT `store_mesg_ibfk_2` FOREIGN KEY (`sender_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `template_message`
--

DROP TABLE IF EXISTS `template_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `template_message` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '訊息id',
  `mesg` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '訊息',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `opendata_import_log`
--

DROP TABLE IF EXISTS `opendata_import_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `opendata_import_log` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '流水號主鍵',
  `import_date` date NOT NULL COMMENT '導入日期 (YYYY-MM-DD)',
  `execution_time` datetime NOT NULL COMMENT '實際執行時間',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '執行狀態: SUCCESS, FAILED',
  `total_count` int DEFAULT '0' COMMENT 'API 總筆數',
  `success_count` int DEFAULT '0' COMMENT '成功寫入/更新筆數',
  `backup_file_path` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '本地備份路徑',
  `error_message` text COLLATE utf8mb4_unicode_ci COMMENT '錯誤訊息紀錄',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_import_date` (`import_date`) -- 核心：保證一天只有一筆成功或紀錄
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OpenData 排程執行紀錄表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping routines for database 'rehome'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-12-20 12:51:29
