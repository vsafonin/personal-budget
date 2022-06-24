-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------
-- -----------------------------------------------------
-- Schema personal_budget
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema personal_budget
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `personal_budget` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci ;
-- -----------------------------------------------------
-- Schema personal_budget_tg_bot
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema personal_budget_tg_bot
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `personal_budget_tg_bot` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci ;
USE `personal_budget` ;

-- -----------------------------------------------------
-- Table `personal_budget`.`activation_links`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `personal_budget`.`activation_links` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `expired_date` DATETIME(6) NULL DEFAULT NULL,
  `uuid` VARCHAR(255) NULL DEFAULT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 3
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `personal_budget`.`app_user`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `personal_budget`.`app_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `create_time` DATETIME(6) NULL DEFAULT NULL,
  `display_name` VARCHAR(255) NULL DEFAULT NULL,
  `email` VARCHAR(255) NULL DEFAULT NULL,
  `enabled` BIT(1) NULL DEFAULT NULL,
  `last_login` DATETIME(6) NULL DEFAULT NULL,
  `password` VARCHAR(255) NULL DEFAULT NULL,
  `username` VARCHAR(255) NULL DEFAULT NULL,
  `activation_link_id` BIGINT NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `UK_1j9d9a06i600gd43uu3km82jw` (`email` ASC) VISIBLE,
  UNIQUE INDEX `UK_3k4cplvh82srueuttfkwnylq0` (`username` ASC) VISIBLE,
  INDEX `FKaql5owwqellr1f4bd50nt71mv` (`activation_link_id` ASC) VISIBLE,
  CONSTRAINT `FKaql5owwqellr1f4bd50nt71mv`
    FOREIGN KEY (`activation_link_id`)
    REFERENCES `personal_budget`.`activation_links` (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 4
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `personal_budget`.`activation_email`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `personal_budget`.`activation_email` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(255) NULL DEFAULT NULL,
  `expired_date` DATETIME(6) NULL DEFAULT NULL,
  `uuid` VARCHAR(255) NULL DEFAULT NULL,
  `user_id` BIGINT NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  INDEX `FK71xrt8ps97xn2wcp3xh57111r` (`user_id` ASC) VISIBLE,
  CONSTRAINT `FK71xrt8ps97xn2wcp3xh57111r`
    FOREIGN KEY (`user_id`)
    REFERENCES `personal_budget`.`app_user` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `personal_budget`.`bank_account`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `personal_budget`.`bank_account` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `balance` DECIMAL(19,2) NULL DEFAULT NULL,
  `currency` VARCHAR(255) NULL DEFAULT NULL,
  `default_account` BIT(1) NULL DEFAULT NULL,
  `description` VARCHAR(255) NULL DEFAULT NULL,
  `name` VARCHAR(255) NULL DEFAULT NULL,
  `app_user_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `FK3epvri9c0xqtfg1rkpeevlf53` (`app_user_id` ASC) VISIBLE,
  CONSTRAINT `FK3epvri9c0xqtfg1rkpeevlf53`
    FOREIGN KEY (`app_user_id`)
    REFERENCES `personal_budget`.`app_user` (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 9
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `personal_budget`.`adjustment_transactions`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `personal_budget`.`adjustment_transactions` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `create_time` DATETIME(6) NOT NULL,
  `currency` VARCHAR(255) NULL DEFAULT NULL,
  `sum_transaction` DECIMAL(19,2) NULL DEFAULT NULL,
  `type_of_operations` INT NOT NULL,
  `app_user_id` BIGINT NOT NULL,
  `bank_account_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `FKbud1mmjjp8tjpsno9kpi43hb6` (`app_user_id` ASC) VISIBLE,
  INDEX `FK9jvk5ft5rwlw2k11ssuvxgyii` (`bank_account_id` ASC) VISIBLE,
  CONSTRAINT `FK9jvk5ft5rwlw2k11ssuvxgyii`
    FOREIGN KEY (`bank_account_id`)
    REFERENCES `personal_budget`.`bank_account` (`id`),
  CONSTRAINT `FKbud1mmjjp8tjpsno9kpi43hb6`
    FOREIGN KEY (`app_user_id`)
    REFERENCES `personal_budget`.`app_user` (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 9
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `personal_budget`.`app_user_jwt_token`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `personal_budget`.`app_user_jwt_token` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `expired_date` DATETIME(6) NULL DEFAULT NULL,
  `token` VARCHAR(255) NULL DEFAULT NULL,
  `app_user_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `FKbsg56ems2k5chhbh1powt93hn` (`app_user_id` ASC) VISIBLE,
  CONSTRAINT `FKbsg56ems2k5chhbh1powt93hn`
    FOREIGN KEY (`app_user_id`)
    REFERENCES `personal_budget`.`app_user` (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 2
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `personal_budget`.`app_user_recover_password`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `personal_budget`.`app_user_recover_password` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `expired_date` DATETIME(6) NULL DEFAULT NULL,
  `token` VARCHAR(255) NULL DEFAULT NULL,
  `app_use_id` BIGINT NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  INDEX `FKket529ajisalftmpxyaw520yo` (`app_use_id` ASC) VISIBLE,
  CONSTRAINT `FKket529ajisalftmpxyaw520yo`
    FOREIGN KEY (`app_use_id`)
    REFERENCES `personal_budget`.`app_user` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `personal_budget`.`authorities`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `personal_budget`.`authorities` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NULL DEFAULT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 2
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `personal_budget`.`category`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `personal_budget`.`category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NULL DEFAULT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 10
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `personal_budget`.`partner`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `personal_budget`.`partner` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NULL DEFAULT NULL,
  `app_user_id` BIGINT NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  INDEX `FKsvxxpptgh18lvon3l1s5ijqve` (`app_user_id` ASC) VISIBLE,
  CONSTRAINT `FKsvxxpptgh18lvon3l1s5ijqve`
    FOREIGN KEY (`app_user_id`)
    REFERENCES `personal_budget`.`app_user` (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 15
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `personal_budget`.`debt_transaction`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `personal_budget`.`debt_transaction` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `create_time` DATETIME(6) NOT NULL,
  `currency` VARCHAR(255) NULL DEFAULT NULL,
  `sum_transaction` DECIMAL(19,2) NULL DEFAULT NULL,
  `type_of_operations` INT NOT NULL,
  `active` BIT(1) NULL DEFAULT NULL,
  `description` VARCHAR(255) NULL DEFAULT NULL,
  `end_date` DATETIME(6) NULL DEFAULT NULL,
  `app_user_id` BIGINT NOT NULL,
  `bank_account_id` BIGINT NOT NULL,
  `partner_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `FKcjubqr59onamm3o3c41g02f9l` (`app_user_id` ASC) VISIBLE,
  INDEX `FKh1q2spaga2rt7ds9nxflr9nyt` (`bank_account_id` ASC) VISIBLE,
  INDEX `FKbjnin8ne423qw8echc7if8n49` (`partner_id` ASC) VISIBLE,
  CONSTRAINT `FKbjnin8ne423qw8echc7if8n49`
    FOREIGN KEY (`partner_id`)
    REFERENCES `personal_budget`.`partner` (`id`),
  CONSTRAINT `FKcjubqr59onamm3o3c41g02f9l`
    FOREIGN KEY (`app_user_id`)
    REFERENCES `personal_budget`.`app_user` (`id`),
  CONSTRAINT `FKh1q2spaga2rt7ds9nxflr9nyt`
    FOREIGN KEY (`bank_account_id`)
    REFERENCES `personal_budget`.`bank_account` (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 4
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `personal_budget`.`debt_payments`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `personal_budget`.`debt_payments` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `pay_date` DATETIME(6) NULL DEFAULT NULL,
  `pay_sum` DECIMAL(19,2) NULL DEFAULT NULL,
  `debt_transaction_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `FKoxdnonjm2y1nnk0cl95y4prvl` (`debt_transaction_id` ASC) VISIBLE,
  CONSTRAINT `FKoxdnonjm2y1nnk0cl95y4prvl`
    FOREIGN KEY (`debt_transaction_id`)
    REFERENCES `personal_budget`.`debt_transaction` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `personal_budget`.`hibernate_sequence`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `personal_budget`.`hibernate_sequence` (
  `next_val` BIGINT NULL DEFAULT NULL)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `personal_budget`.`product`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `personal_budget`.`product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NULL DEFAULT NULL,
  `category_id` BIGINT NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  INDEX `FK1mtsbur82frn64de7balymq9s` (`category_id` ASC) VISIBLE,
  CONSTRAINT `FK1mtsbur82frn64de7balymq9s`
    FOREIGN KEY (`category_id`)
    REFERENCES `personal_budget`.`category` (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 25
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `personal_budget`.`product_data`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `personal_budget`.`product_data` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `cost` DECIMAL(19,2) NULL DEFAULT NULL,
  `quantity` DECIMAL(19,2) NULL DEFAULT NULL,
  `product_id` BIGINT NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  INDEX `FKo5742tiaorwcentdtb1hqnam` (`product_id` ASC) VISIBLE,
  CONSTRAINT `FKo5742tiaorwcentdtb1hqnam`
    FOREIGN KEY (`product_id`)
    REFERENCES `personal_budget`.`product` (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 55
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `personal_budget`.`shop`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `personal_budget`.`shop` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NULL DEFAULT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 14
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `personal_budget`.`purchase_transaction`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `personal_budget`.`purchase_transaction` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `create_time` DATETIME(6) NOT NULL,
  `currency` VARCHAR(255) NULL DEFAULT NULL,
  `sum_transaction` DECIMAL(19,2) NULL DEFAULT NULL,
  `type_of_operations` INT NOT NULL,
  `fiscal_sign` BIGINT NULL DEFAULT NULL,
  `from_jsnon` BIT(1) NULL DEFAULT NULL,
  `app_user_id` BIGINT NOT NULL,
  `bank_account_id` BIGINT NOT NULL,
  `shop_id` BIGINT NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  INDEX `FKsnyw40d6vrsmxs3n2ggyeauxi` (`app_user_id` ASC) VISIBLE,
  INDEX `FKn7yqgdv6pd0p3468orf3vcogu` (`bank_account_id` ASC) VISIBLE,
  INDEX `FKofq907l3jpootgxlxgfwyfgax` (`shop_id` ASC) VISIBLE,
  CONSTRAINT `FKn7yqgdv6pd0p3468orf3vcogu`
    FOREIGN KEY (`bank_account_id`)
    REFERENCES `personal_budget`.`bank_account` (`id`),
  CONSTRAINT `FKofq907l3jpootgxlxgfwyfgax`
    FOREIGN KEY (`shop_id`)
    REFERENCES `personal_budget`.`shop` (`id`),
  CONSTRAINT `FKsnyw40d6vrsmxs3n2ggyeauxi`
    FOREIGN KEY (`app_user_id`)
    REFERENCES `personal_budget`.`app_user` (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 17
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `personal_budget`.`purchase_transaction_product_datas`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `personal_budget`.`purchase_transaction_product_datas` (
  `purchase_transaction_id` BIGINT NOT NULL,
  `product_datas_id` BIGINT NOT NULL,
  INDEX `FK7h3i5iw6j4fo8kwh7o8nubbix` (`product_datas_id` ASC) VISIBLE,
  INDEX `FK28c7w3kvpavt3bbf008x474y` (`purchase_transaction_id` ASC) VISIBLE,
  CONSTRAINT `FK28c7w3kvpavt3bbf008x474y`
    FOREIGN KEY (`purchase_transaction_id`)
    REFERENCES `personal_budget`.`purchase_transaction` (`id`),
  CONSTRAINT `FK7h3i5iw6j4fo8kwh7o8nubbix`
    FOREIGN KEY (`product_datas_id`)
    REFERENCES `personal_budget`.`product_data` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `personal_budget`.`salary_transaction`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `personal_budget`.`salary_transaction` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `create_time` DATETIME(6) NOT NULL,
  `currency` VARCHAR(255) NULL DEFAULT NULL,
  `sum_transaction` DECIMAL(19,2) NULL DEFAULT NULL,
  `type_of_operations` INT NOT NULL,
  `description` VARCHAR(255) NULL DEFAULT NULL,
  `app_user_id` BIGINT NOT NULL,
  `bank_account_id` BIGINT NOT NULL,
  `partner_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `FKr4u1x6cxq50ajwrn6fm5ye8ik` (`app_user_id` ASC) VISIBLE,
  INDEX `FKcwlulgjx8ftqk5773vqg6jal7` (`bank_account_id` ASC) VISIBLE,
  INDEX `FKm86gnyfncibhderqqu575aluk` (`partner_id` ASC) VISIBLE,
  CONSTRAINT `FKcwlulgjx8ftqk5773vqg6jal7`
    FOREIGN KEY (`bank_account_id`)
    REFERENCES `personal_budget`.`bank_account` (`id`),
  CONSTRAINT `FKm86gnyfncibhderqqu575aluk`
    FOREIGN KEY (`partner_id`)
    REFERENCES `personal_budget`.`partner` (`id`),
  CONSTRAINT `FKr4u1x6cxq50ajwrn6fm5ye8ik`
    FOREIGN KEY (`app_user_id`)
    REFERENCES `personal_budget`.`app_user` (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 8
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `personal_budget`.`transfer_transaction`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `personal_budget`.`transfer_transaction` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `create_time` DATETIME(6) NOT NULL,
  `sum_transaction` DECIMAL(19,2) NULL DEFAULT NULL,
  `app_user_id` BIGINT NOT NULL,
  `from_bank_account_id` BIGINT NOT NULL,
  `to_bank_account_id` BIGINT NOT NULL,
  `sum_transaction_from` DECIMAL(19,2) NULL DEFAULT NULL,
  `sum_transaction_to` DECIMAL(19,2) NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  INDEX `FKt95m4bmks7r9sy02sgrx3v4xl` (`app_user_id` ASC) VISIBLE,
  INDEX `FKbo5vne0ddk3avdasoug5hqf7g` (`from_bank_account_id` ASC) VISIBLE,
  INDEX `FK5oopryoj4jubq6p8e8ve9kk09` (`to_bank_account_id` ASC) VISIBLE,
  CONSTRAINT `FK5oopryoj4jubq6p8e8ve9kk09`
    FOREIGN KEY (`to_bank_account_id`)
    REFERENCES `personal_budget`.`bank_account` (`id`),
  CONSTRAINT `FKbo5vne0ddk3avdasoug5hqf7g`
    FOREIGN KEY (`from_bank_account_id`)
    REFERENCES `personal_budget`.`bank_account` (`id`),
  CONSTRAINT `FKt95m4bmks7r9sy02sgrx3v4xl`
    FOREIGN KEY (`app_user_id`)
    REFERENCES `personal_budget`.`app_user` (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 12
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `personal_budget`.`user_roles`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `personal_budget`.`user_roles` (
  `users_id` BIGINT NOT NULL,
  `roles_id` BIGINT NOT NULL,
  PRIMARY KEY (`users_id`, `roles_id`),
  INDEX `FKrusalkiavp6b1xibd2mb59uw0` (`roles_id` ASC) VISIBLE,
  CONSTRAINT `FK2oj5a9g4o3bo8ie8a19dj9csr`
    FOREIGN KEY (`users_id`)
    REFERENCES `personal_budget`.`app_user` (`id`),
  CONSTRAINT `FKrusalkiavp6b1xibd2mb59uw0`
    FOREIGN KEY (`roles_id`)
    REFERENCES `personal_budget`.`authorities` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;

USE `personal_budget_tg_bot` ;

-- -----------------------------------------------------
-- Table `personal_budget_tg_bot`.`tg_user`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `personal_budget_tg_bot`.`tg_user` (
  `id` BIGINT NOT NULL,
  `name` VARCHAR(255) NULL DEFAULT NULL,
  `token` VARCHAR(255) NULL DEFAULT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
