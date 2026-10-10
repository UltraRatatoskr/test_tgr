-- DocSlot — schéma MariaDB + données de test
-- Base : tgpr-2627-a01
-- Identifiants techniques (tables, colonnes, ENUM) : anglais
-- Contenu seed (noms, motifs, textes) : français
-- Attention ScriptRunner (MyBatis) : ne pas mettre de ';' dans les chaînes littérales.
--
-- Temps simulé de référence : 2026-07-21 11:58:00
--
-- Comptes de test (mot de passe en clair → Tools.hash) :
--   admin@docslot.be              / Password1,   → ADMIN
--   old.admin@docslot.be          / Password1,   → ADMIN (compte inactif)
--   sophie.dupont@docslot.be      / Password1,   → SECRETARY
--   julie.wauters@docslot.be      / Password1,   → SECRETARY (2e secrétaire)
--   antoine.leroy@docslot.be      / Password1,   → DOCTOR (actif, cardiologie)
--   claire.bernard@docslot.be     / Password1,   → DOCTOR (actif, médecine générale)
--   emma.kim@docslot.be           / Password1,   → DOCTOR (actif, dermatologie + médecine générale)
--   paul.moreau@docslot.be        / Password1,   → DOCTOR (inactif, pédiatrie)
--   nora.hassan@docslot.be        / Password1,   → DOCTOR (actif, orthopédie)
--   marc.peeters@docslot.be       / Password1,   → DOCTOR (actif, ophtalmologie ; aucune plage en ligne)
--   marie.dupont@mail.be          / Password1,   → PATIENT
--   sophie.martin@mail.be         / Password1,   → PATIENT
--   jean.dupont@mail.be           / Password1,   → PATIENT
--   luc.petit@mail.be             / Password1,   → PATIENT
--   anne.durand@mail.be           / Password1,   → PATIENT (compte inactif)
--   pierre.lambert@mail.be        / Password1,   → PATIENT
--   ines.renard@mail.be           / Password1,   → PATIENT
-- Patients sans compte : Claire Leroy, Thomas Nguyen, Emma Dubois, Hugo Fabry, Léa Simon
-- Spécialité sans médecin : ORL (recherche vide)
--
-- Tools.hash("Password1,") : 56ce92d1de4f05017cf03d6cd514d6d1

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

DROP DATABASE IF EXISTS `tgpr-2627-a01`;
CREATE DATABASE `tgpr-2627-a01` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `tgpr-2627-a01`;

-- --------------------------------------------------------
-- Structure
-- --------------------------------------------------------

DROP TABLE IF EXISTS `consultation`;
DROP TABLE IF EXISTS `appointment`;
DROP TABLE IF EXISTS `online_booking_slot`;
DROP TABLE IF EXISTS `doctor_specialty`;
DROP TABLE IF EXISTS `appointment_reason`;
DROP TABLE IF EXISTS `specialty`;
DROP TABLE IF EXISTS `doctor`;
DROP TABLE IF EXISTS `patient`;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS `system_time`;

CREATE TABLE `user` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(255) NOT NULL,
  `password_hash` VARCHAR(128) NOT NULL,
  `role` ENUM('PATIENT','DOCTOR','SECRETARY','ADMIN') NOT NULL,
  `active` TINYINT(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `patient` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `user_id` INT DEFAULT NULL,
  `last_name` VARCHAR(128) NOT NULL,
  `first_name` VARCHAR(128) NOT NULL,
  `birth_date` DATE DEFAULT NULL,
  `phone` VARCHAR(32) DEFAULT NULL,
  `email` VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_patient_user_id` (`user_id`),
  CONSTRAINT `fk_patient_user`
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `doctor` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `user_id` INT NOT NULL,
  `last_name` VARCHAR(128) NOT NULL,
  `first_name` VARCHAR(128) NOT NULL,
  `inami_number` CHAR(11) NOT NULL,
  `active` TINYINT(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_doctor_user_id` (`user_id`),
  UNIQUE KEY `uk_doctor_inami` (`inami_number`),
  CONSTRAINT `fk_doctor_user`
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `specialty` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(128) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_specialty_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `doctor_specialty` (
  `doctor_id` INT NOT NULL,
  `specialty_id` INT NOT NULL,
  PRIMARY KEY (`doctor_id`, `specialty_id`),
  KEY `idx_doctor_specialty_specialty` (`specialty_id`),
  CONSTRAINT `fk_doctor_specialty_doctor`
    FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_doctor_specialty_specialty`
    FOREIGN KEY (`specialty_id`) REFERENCES `specialty` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `appointment_reason` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(128) NOT NULL,
  `duration_minutes` INT NOT NULL,
  `bookable_online` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_appointment_reason_name` (`name`),
  CONSTRAINT `chk_appointment_reason_duration`
    CHECK (`duration_minutes` > 0 AND `duration_minutes` % 15 = 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `online_booking_slot` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `doctor_id` INT NOT NULL,
  `start_datetime` DATETIME NOT NULL,
  `end_datetime` DATETIME NOT NULL,
  `active` TINYINT(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  KEY `idx_online_booking_slot_doctor_start` (`doctor_id`, `start_datetime`),
  UNIQUE KEY `uk_online_booking_slot_doctor_range` (`doctor_id`, `start_datetime`, `end_datetime`),
  CONSTRAINT `fk_online_booking_slot_doctor`
    FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `chk_online_booking_slot_dates`
    CHECK (`end_datetime` > `start_datetime`),
  CONSTRAINT `chk_online_booking_slot_step`
    CHECK (
      MINUTE(`start_datetime`) % 15 = 0 AND SECOND(`start_datetime`) = 0
      AND MINUTE(`end_datetime`) % 15 = 0 AND SECOND(`end_datetime`) = 0
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `appointment` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `patient_id` INT NOT NULL,
  `doctor_id` INT NOT NULL,
  `reason_id` INT NOT NULL,
  `specialty_id` INT DEFAULT NULL,
  `start_datetime` DATETIME NOT NULL,
  `end_datetime` DATETIME NOT NULL,
  `status` ENUM('REQUESTED','CONFIRMED','CANCELLED','COMPLETED','NO_SHOW') NOT NULL,
  `origin` ENUM('INTERNAL','ONLINE') NOT NULL,
  `created_by_user_id` INT DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_appointment_doctor_start` (`doctor_id`, `start_datetime`),
  KEY `idx_appointment_patient_start` (`patient_id`, `start_datetime`),
  KEY `idx_appointment_reason` (`reason_id`),
  KEY `idx_appointment_specialty` (`specialty_id`),
  KEY `idx_appointment_created_by` (`created_by_user_id`),
  CONSTRAINT `fk_appointment_patient`
    FOREIGN KEY (`patient_id`) REFERENCES `patient` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_appointment_doctor`
    FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_appointment_reason`
    FOREIGN KEY (`reason_id`) REFERENCES `appointment_reason` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_appointment_specialty`
    FOREIGN KEY (`specialty_id`) REFERENCES `specialty` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_appointment_created_by`
    FOREIGN KEY (`created_by_user_id`) REFERENCES `user` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `chk_appointment_dates`
    CHECK (`end_datetime` > `start_datetime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `consultation` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `appointment_id` INT NOT NULL,
  `report` TEXT NOT NULL,
  `diagnosis` TEXT DEFAULT NULL,
  `treatment` TEXT DEFAULT NULL,
  `patient_instructions` TEXT DEFAULT NULL,
  `visible_to_patient` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_consultation_appointment` (`appointment_id`),
  CONSTRAINT `fk_consultation_appointment`
    FOREIGN KEY (`appointment_id`) REFERENCES `appointment` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `chk_consultation_report_required`
    CHECK (CHAR_LENGTH(TRIM(`report`)) > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `system_time` (
  `simulated_at` DATETIME DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Données de référence
-- --------------------------------------------------------

INSERT INTO `system_time` (`simulated_at`) VALUES ('2026-07-21 11:58:00');

-- password_hash = Tools.hash("Password1,")
INSERT INTO `user` (`id`, `email`, `password_hash`, `role`, `active`) VALUES
(1,  'admin@docslot.be',          '56ce92d1de4f05017cf03d6cd514d6d1', 'ADMIN',     1),
(2,  'sophie.dupont@docslot.be',  '56ce92d1de4f05017cf03d6cd514d6d1', 'SECRETARY', 1),
(3,  'antoine.leroy@docslot.be',  '56ce92d1de4f05017cf03d6cd514d6d1', 'DOCTOR',    1),
(4,  'claire.bernard@docslot.be', '56ce92d1de4f05017cf03d6cd514d6d1', 'DOCTOR',    1),
(5,  'emma.kim@docslot.be',       '56ce92d1de4f05017cf03d6cd514d6d1', 'DOCTOR',    1),
(6,  'paul.moreau@docslot.be',    '56ce92d1de4f05017cf03d6cd514d6d1', 'DOCTOR',    1),
(7,  'marie.dupont@mail.be',      '56ce92d1de4f05017cf03d6cd514d6d1', 'PATIENT',   1),
(8,  'sophie.martin@mail.be',     '56ce92d1de4f05017cf03d6cd514d6d1', 'PATIENT',   1),
(9,  'jean.dupont@mail.be',       '56ce92d1de4f05017cf03d6cd514d6d1', 'PATIENT',   1),
(10, 'luc.petit@mail.be',         '56ce92d1de4f05017cf03d6cd514d6d1', 'PATIENT',   1),
(11, 'anne.durand@mail.be',       '56ce92d1de4f05017cf03d6cd514d6d1', 'PATIENT',   0),
(12, 'julie.wauters@docslot.be',  '56ce92d1de4f05017cf03d6cd514d6d1', 'SECRETARY', 1),
(13, 'nora.hassan@docslot.be',    '56ce92d1de4f05017cf03d6cd514d6d1', 'DOCTOR',    1),
(14, 'marc.peeters@docslot.be',   '56ce92d1de4f05017cf03d6cd514d6d1', 'DOCTOR',    1),
(15, 'pierre.lambert@mail.be',    '56ce92d1de4f05017cf03d6cd514d6d1', 'PATIENT',   1),
(16, 'ines.renard@mail.be',       '56ce92d1de4f05017cf03d6cd514d6d1', 'PATIENT',   1),
(17, 'old.admin@docslot.be',      '56ce92d1de4f05017cf03d6cd514d6d1', 'ADMIN',     0);

INSERT INTO `specialty` (`id`, `name`) VALUES
(1, 'Cardiologie'),
(2, 'Médecine générale'),
(3, 'Dermatologie'),
(4, 'Pédiatrie'),
(5, 'Orthopédie'),
(6, 'Ophtalmologie'),
(7, 'ORL');

INSERT INTO `appointment_reason` (`id`, `name`, `duration_minutes`, `bookable_online`) VALUES
(1, 'Consultation',       30, 1),
(2, 'Contrôle',           30, 1),
(3, 'Suivi',              30, 0),
(4, 'Urgence légère',     15, 0),
(5, 'Bilan annuel',       45, 1),
(6, 'Première visite',    45, 1),
(7, 'Certificat médical', 15, 0);

INSERT INTO `doctor` (`id`, `user_id`, `last_name`, `first_name`, `inami_number`, `active`) VALUES
(1, 3,  'Leroy',   'Antoine', '12345678901', 1),
(2, 4,  'Bernard', 'Claire',  '23456789012', 1),
(3, 5,  'Kim',     'Emma',    '34567890123', 1),
(4, 6,  'Moreau',  'Paul',    '45678901234', 0),
(5, 13, 'Hassan',  'Nora',    '56789012345', 1),
(6, 14, 'Peeters', 'Marc',    '67890123456', 1);

INSERT INTO `doctor_specialty` (`doctor_id`, `specialty_id`) VALUES
(1, 1),
(2, 2),
(3, 3),
(3, 2),
(4, 4),
(5, 5),
(6, 6);
-- ORL (7) volontairement sans médecin

INSERT INTO `patient` (`id`, `user_id`, `last_name`, `first_name`, `birth_date`, `phone`, `email`) VALUES
(1,  7,    'Dupont',  'Marie',   '1990-03-15', '0476 12 34 56', 'marie.dupont@mail.be'),
(2,  8,    'Martin',  'Sophie',  '1988-03-12', '0470 12 34 56', 'sophie.martin@mail.be'),
(3,  9,    'Dupont',  'Jean',    '1979-09-05', '0499 98 76 54', 'jean.dupont@mail.be'),
(4,  10,   'Petit',   'Luc',     '2001-11-21', '0488 55 44 33', 'luc.petit@mail.be'),
(5,  11,   'Durand',  'Anne',    '1992-06-18', '0471 22 33 44', 'anne.durand@mail.be'),
(6,  NULL, 'Leroy',   'Claire',  '1995-01-30', '0477 00 11 22', 'claire.leroy@mail.be'),
(7,  NULL, 'Nguyen',  'Thomas',  '1985-08-04', '0465 77 88 99', 'thomas.nguyen@mail.be'),
(8,  NULL, 'Dubois',  'Emma',    '2012-04-22', '0472 11 22 33', NULL),
(9,  15,   'Lambert', 'Pierre',  '1955-12-01', '0473 44 55 66', 'pierre.lambert@mail.be'),
(10, 16,   'Renard',  'Inès',    '1998-07-09', '0474 66 77 88', 'ines.renard@mail.be'),
(11, NULL, 'Fabry',   'Hugo',    '2018-02-14', '0475 88 99 00', NULL),
(12, NULL, 'Simon',   'Léa',     NULL,         NULL,            'lea.simon@mail.be');

-- --------------------------------------------------------
-- Plages réservables en ligne (autour du 21/07/2026)
-- --------------------------------------------------------

INSERT INTO `online_booking_slot` (`id`, `doctor_id`, `start_datetime`, `end_datetime`, `active`) VALUES
-- Dr Leroy
(1, 1, '2026-07-21 09:00:00', '2026-07-21 12:00:00', 1),
(2, 1, '2026-07-21 14:00:00', '2026-07-21 17:00:00', 1),
(3, 1, '2026-07-22 09:00:00', '2026-07-22 12:00:00', 1),
(4, 1, '2026-07-28 09:00:00', '2026-07-28 12:00:00', 1),
(5, 1, '2026-07-23 09:00:00', '2026-07-23 12:00:00', 0),
(12, 1, '2026-07-15 09:00:00', '2026-07-15 12:00:00', 1),
(13, 1, '2026-07-29 14:00:00', '2026-07-29 17:00:00', 1),
-- Dr Bernard
(6, 2, '2026-07-21 08:30:00', '2026-07-21 12:00:00', 1),
(7, 2, '2026-07-22 14:00:00', '2026-07-22 18:00:00', 1),
(8, 2, '2026-07-24 09:00:00', '2026-07-24 12:00:00', 1),
(14, 2, '2026-07-27 09:00:00', '2026-07-27 11:00:00', 1),
-- Dr Kim
(9,  3, '2026-07-21 13:00:00', '2026-07-21 16:00:00', 1),
(10, 3, '2026-07-25 09:00:00', '2026-07-25 12:00:00', 1),
(15, 3, '2026-07-27 14:00:00', '2026-07-27 16:00:00', 1),
-- Dr Moreau (inactif)
(11, 4, '2026-07-10 09:00:00', '2026-07-10 12:00:00', 0),
(19, 4, '2026-07-29 09:00:00', '2026-07-29 12:00:00', 1),
-- Dr Hassan
(16, 5, '2026-07-23 09:00:00', '2026-07-23 11:00:00', 1),
(17, 5, '2026-07-30 09:00:00', '2026-07-30 12:00:00', 1),
(18, 5, '2026-07-20 14:00:00', '2026-07-20 17:00:00', 1),
-- Dr Peeters
(20, 6, '2026-07-31 08:00:00', '2026-07-31 09:00:00', 1),
(21, 6, '2026-07-31 09:00:00', '2026-07-31 10:00:00', 1),
(22, 6, '2026-07-31 10:00:00', '2026-07-31 11:00:00', 1),
(23, 6, '2026-07-31 11:00:00', '2026-07-31 12:00:00', 1);

-- --------------------------------------------------------
-- Rendez-vous (ancrage : maintenant = 2026-07-21 11:58)
-- --------------------------------------------------------

INSERT INTO `appointment` (`id`, `patient_id`, `doctor_id`, `reason_id`, `start_datetime`, `end_datetime`, `status`, `origin`, `created_by_user_id`) VALUES
-- Passé COMPLETED
(1, 1, 2, 3, '2026-06-10 10:00:00', '2026-06-10 10:30:00', 'COMPLETED', 'INTERNAL', 2),
(2, 2, 1, 1, '2026-06-15 09:00:00', '2026-06-15 09:30:00', 'COMPLETED', 'INTERNAL', 2),
(3, 2, 1, 2, '2026-07-01 10:00:00', '2026-07-01 10:30:00', 'COMPLETED', 'ONLINE', 8),
(4, 3, 3, 1, '2026-07-05 14:00:00', '2026-07-05 14:30:00', 'COMPLETED', 'INTERNAL', 2),
(5, 4, 2, 5, '2026-07-08 09:00:00', '2026-07-08 09:45:00', 'COMPLETED', 'INTERNAL', 2),
(6, 7, 2, 3, '2026-07-09 11:00:00', '2026-07-09 11:30:00', 'COMPLETED', 'INTERNAL', 2),
(7, 1, 1, 2, '2026-07-14 09:30:00', '2026-07-14 10:00:00', 'COMPLETED', 'ONLINE', 7),
(8, 6, 3, 1, '2026-07-16 10:00:00', '2026-07-16 10:30:00', 'COMPLETED', 'INTERNAL', 2),
(9, 5, 4, 1, '2026-05-20 10:00:00', '2026-05-20 10:30:00', 'COMPLETED', 'INTERNAL', 2),
(10, 8, 4, 2, '2026-06-02 09:00:00', '2026-06-02 09:30:00', 'COMPLETED', 'INTERNAL', 2),
(36, 2, 2, 1, '2026-03-12 10:00:00', '2026-03-12 10:30:00', 'COMPLETED', 'INTERNAL', 2),
(37, 2, 2, 3, '2026-04-20 11:00:00', '2026-04-20 11:30:00', 'COMPLETED', 'INTERNAL', 2),
(38, 2, 1, 1, '2026-05-05 09:00:00', '2026-05-05 09:30:00', 'COMPLETED', 'ONLINE', 8),
(39, 9, 1, 1, '2026-07-18 10:00:00', '2026-07-18 10:30:00', 'COMPLETED', 'INTERNAL', 2),
(40, 10, 5, 1, '2026-07-17 09:00:00', '2026-07-17 09:30:00', 'COMPLETED', 'INTERNAL', 12),
(41, 9, 6, 1, '2026-07-03 15:00:00', '2026-07-03 15:30:00', 'COMPLETED', 'INTERNAL', NULL),
(42, 10, 2, 6, '2026-06-28 09:00:00', '2026-06-28 09:45:00', 'COMPLETED', 'ONLINE', 16),
(43, 11, 2, 7, '2026-07-12 16:00:00', '2026-07-12 16:15:00', 'COMPLETED', 'INTERNAL', 2),

-- Passé NO_SHOW / CANCELLED / REQUESTED
(11, 3, 2, 1, '2026-07-07 15:00:00', '2026-07-07 15:30:00', 'NO_SHOW',   'INTERNAL', 2),
(12, 4, 1, 1, '2026-07-10 11:00:00', '2026-07-10 11:30:00', 'CANCELLED', 'ONLINE',   10),
(13, 1, 3, 2, '2026-07-11 13:00:00', '2026-07-11 13:30:00', 'CANCELLED', 'INTERNAL', 2),
(44, 10, 1, 2, '2026-07-15 09:00:00', '2026-07-15 09:30:00', 'NO_SHOW',   'ONLINE',   16),
(45, 7, 2, 1, '2026-07-13 10:00:00', '2026-07-13 10:30:00', 'CANCELLED', 'INTERNAL', 12),
(46, 9, 3, 1, '2026-07-19 14:00:00', '2026-07-19 14:30:00', 'REQUESTED', 'ONLINE', 15),

-- Jour J (21/07/2026)
(14, 4, 1, 2, '2026-07-21 10:00:00', '2026-07-21 10:30:00', 'REQUESTED', 'ONLINE', 10),
(15, 2, 1, 1, '2026-07-21 10:30:00', '2026-07-21 11:00:00', 'CONFIRMED', 'INTERNAL', 2),
(16, 3, 1, 2, '2026-07-21 11:00:00', '2026-07-21 11:30:00', 'CONFIRMED', 'ONLINE', 9),
(17, 1, 2, 1, '2026-07-21 09:00:00', '2026-07-21 09:30:00', 'CONFIRMED', 'INTERNAL', 2),
(18, 7, 2, 4, '2026-07-21 09:30:00', '2026-07-21 09:45:00', 'CONFIRMED', 'INTERNAL', 2),
(19, 6, 2, 3, '2026-07-21 11:00:00', '2026-07-21 11:30:00', 'REQUESTED', 'INTERNAL', 2),
(20, 4, 3, 1, '2026-07-21 13:30:00', '2026-07-21 14:00:00', 'CONFIRMED', 'ONLINE', 10),
(21, 2, 3, 2, '2026-07-21 14:00:00', '2026-07-21 14:30:00', 'REQUESTED', 'ONLINE', 8),
(22, 1, 1, 1, '2026-07-21 14:00:00', '2026-07-21 14:30:00', 'CONFIRMED', 'ONLINE', 7),
(23, 3, 1, 5, '2026-07-21 15:00:00', '2026-07-21 15:45:00', 'CONFIRMED', 'INTERNAL', 2),
(47, 1, 5, 1, '2026-07-21 16:00:00', '2026-07-21 16:30:00', 'CONFIRMED', 'INTERNAL', 2),
(48, 9, 6, 2, '2026-07-21 10:00:00', '2026-07-21 10:30:00', 'CONFIRMED', 'INTERNAL', 12),
(49, 12, 2, 4, '2026-07-21 10:00:00', '2026-07-21 10:15:00', 'CONFIRMED', 'INTERNAL', 2),
(50, 10, 3, 6, '2026-07-21 14:30:00', '2026-07-21 15:15:00', 'REQUESTED', 'ONLINE', 16),

-- Hassan 23/07
(51, 3, 5, 1, '2026-07-23 09:00:00', '2026-07-23 09:30:00', 'CONFIRMED', 'ONLINE', 9),
(52, 4, 5, 1, '2026-07-23 09:30:00', '2026-07-23 10:00:00', 'CONFIRMED', 'ONLINE', 10),
(53, 10, 5, 1, '2026-07-23 10:00:00', '2026-07-23 10:30:00', 'CONFIRMED', 'ONLINE', 16),
(54, 2, 5, 1, '2026-07-23 10:30:00', '2026-07-23 11:00:00', 'REQUESTED', 'ONLINE', 8),

-- Futur
(24, 1, 1, 1, '2026-07-22 09:00:00', '2026-07-22 09:30:00', 'CONFIRMED', 'ONLINE',   7),
(25, 2, 1, 2, '2026-07-22 09:30:00', '2026-07-22 10:00:00', 'REQUESTED', 'ONLINE',   8),
(26, 4, 2, 1, '2026-07-22 14:00:00', '2026-07-22 14:30:00', 'CONFIRMED', 'ONLINE',   10),
(27, 3, 2, 5, '2026-07-22 15:00:00', '2026-07-22 15:45:00', 'CONFIRMED', 'INTERNAL', 2),
(28, 7, 2, 3, '2026-07-24 09:00:00', '2026-07-24 09:30:00', 'CONFIRMED', 'INTERNAL', 2),
(29, 1, 3, 1, '2026-07-25 09:00:00', '2026-07-25 09:30:00', 'REQUESTED', 'ONLINE',   7),
(30, 2, 3, 2, '2026-07-25 09:30:00', '2026-07-25 10:00:00', 'CONFIRMED', 'ONLINE',   8),
(31, 6, 1, 1, '2026-07-28 09:00:00', '2026-07-28 09:30:00', 'CONFIRMED', 'INTERNAL', 2),
(32, 4, 1, 2, '2026-07-28 10:00:00', '2026-07-28 10:30:00', 'REQUESTED', 'ONLINE',   10),
(33, 3, 1, 1, '2026-07-28 10:00:00', '2026-07-28 10:30:00', 'CANCELLED', 'ONLINE', 9),
(34, 8, 2, 1, '2026-07-24 10:00:00', '2026-07-24 10:30:00', 'CONFIRMED', 'INTERNAL', 2),
(35, 5, 2, 2, '2026-07-22 16:00:00', '2026-07-22 16:30:00', 'CONFIRMED', 'INTERNAL', 2),
(55, 9, 5, 2, '2026-07-30 09:00:00', '2026-07-30 09:30:00', 'CONFIRMED', 'ONLINE', 15),
(56, 1, 5, 5, '2026-07-30 10:00:00', '2026-07-30 10:45:00', 'REQUESTED', 'ONLINE', 7),
(57, 10, 1, 1, '2026-07-29 14:00:00', '2026-07-29 14:30:00', 'CONFIRMED', 'ONLINE', 16),
(58, 9, 1, 2, '2026-07-29 14:30:00', '2026-07-29 15:00:00', 'CONFIRMED', 'ONLINE', 15),
(59, 3, 2, 1, '2026-07-27 09:00:00', '2026-07-27 09:30:00', 'CONFIRMED', 'ONLINE', 9),
(60, 7, 2, 6, '2026-07-27 09:30:00', '2026-07-27 10:15:00', 'REQUESTED', 'INTERNAL', 2),
(61, 4, 3, 1, '2026-07-27 14:00:00', '2026-07-27 14:30:00', 'CONFIRMED', 'ONLINE', 10),
(62, 2, 3, 2, '2026-07-27 14:30:00', '2026-07-27 15:00:00', 'CONFIRMED', 'ONLINE', 8),
(63, 6, 2, 1, '2026-07-24 11:00:00', '2026-07-24 11:30:00', 'CANCELLED', 'INTERNAL', 2),
(64, 9, 6, 1, '2026-07-28 14:00:00', '2026-07-28 14:30:00', 'CONFIRMED', 'INTERNAL', 12),
(65, 12, 6, 7, '2026-07-28 14:30:00', '2026-07-28 14:45:00', 'REQUESTED', 'INTERNAL', 2),
(66, 10, 2, 2, '2026-07-24 11:30:00', '2026-07-24 12:00:00', 'CONFIRMED', 'ONLINE', 16),
(67, 11, 2, 1, '2026-07-27 10:30:00', '2026-07-27 11:00:00', 'CONFIRMED', 'INTERNAL', 2),
(68, 9, 1, 1, '2026-07-28 11:00:00', '2026-07-28 11:30:00', 'REQUESTED', 'ONLINE', 15),
(69, 5, 4, 1, '2026-07-28 15:00:00', '2026-07-28 15:30:00', 'CONFIRMED', 'INTERNAL', 2);

-- Spécialité de visite
UPDATE `appointment` SET `specialty_id` = 1 WHERE `doctor_id` = 1;
UPDATE `appointment` SET `specialty_id` = 2 WHERE `doctor_id` = 2;
UPDATE `appointment` SET `specialty_id` = 4 WHERE `doctor_id` = 4;
UPDATE `appointment` SET `specialty_id` = 5 WHERE `doctor_id` = 5;
UPDATE `appointment` SET `specialty_id` = 6 WHERE `doctor_id` = 6;
UPDATE `appointment` SET `specialty_id` = 3 WHERE `doctor_id` = 3;
UPDATE `appointment` SET `specialty_id` = 2 WHERE `doctor_id` = 3 AND `id` IN (21, 30, 62);

ALTER TABLE `appointment`
  MODIFY COLUMN `specialty_id` INT NOT NULL;

-- --------------------------------------------------------
-- Consultations
-- --------------------------------------------------------

INSERT INTO `consultation` (`id`, `appointment_id`, `report`, `diagnosis`, `treatment`, `patient_instructions`, `visible_to_patient`) VALUES
(1, 1,
 'Patiente revue pour suivi tensionnel. TA 128/82. Bonne observance du traitement.',
 'Hypertension artérielle contrôlée',
 'Maintien amlodipine 5 mg 1x/j',
 'Continuer le traitement actuel. Contrôler la tension à domicile 2x/semaine. Revenir dans 3 mois.',
 1),
(2, 2,
 'Douleur thoracique atypique à l''effort. ECG de repos normal. Épreuve d''effort prévue.',
 'Douleur thoracique à investiguer',
 'Holter + épreuve d''effort à programmer',
 'Éviter les efforts intenses jusqu''aux résultats. En cas de douleur irradiant vers le bras gauche, contacter le 112.',
 1),
(3, 3,
 'Contrôle post-épreuve d''effort : négative. Pas de signe d''ischémie.',
 'Douleur thoracique non coronarienne',
 'Surveillance clinique, pas de traitement spécifique',
 NULL,
 0),
(4, 4,
 'Lésion érythémateuse avant-bras droit, évoluant depuis 3 semaines. Biopsie cutanée réalisée.',
 'Dermatite de contact suspectée',
 'Dermocorticoïde local 1x/j pendant 7 jours',
 'Appliquer la crème matin et soir pendant 7 jours. Éviter le contact avec produits ménagers sans gants.',
 1),
(5, 5,
 'Bilan annuel complet. Paramètres biologiques dans les normes. IMC 24.',
 'État de santé satisfaisant',
 'Pas de traitement, conseils hygiène de vie',
 'Maintenir une activité physique régulière (30 min, 3x/semaine). Prochain bilan dans un an.',
 1),
(6, 6,
 'Suivi lombalgie mécanique. Amélioration sous kiné. Examen neurologique normal.',
 'Lombalgie mécanique',
 'Poursuite kinésithérapie 2x/semaine pendant 3 semaines',
 'Continuer les exercices prescrits par le kiné. Éviter le port de charges lourdes.',
 1),
(7, 7,
 'Contrôle tensionnel et rythme. Holter reçu : extrasystoles rares, bénignes.',
 'Extrasystoles ventriculaires bénignes',
 'Pas de modification thérapeutique',
 'Réduire la caféine. Revoir dans 6 mois ou plus tôt si palpitations fréquentes.',
 1),
(8, 8,
 'Acné inflammatoire du visage, grade modéré. Pas d''échec thérapeutique antérieur.',
 'Acné inflammatoire modérée',
 'Peroxyde de benzoyle 5 % le soir + adaptation hygiène',
 'Appliquer le traitement le soir sur peau propre. Éviter l''exposition solaire prolongée.',
 1),
(9, 9,
 'Consultation pédiatrique de suivi. Courbe de croissance normale. Vaccinations à jour.',
 'Développement normal',
 'Aucun traitement',
 'Prochaine visite dans 12 mois. Surveiller l''apparition de fièvre prolongée.',
 1),
(10, 10,
 'Contrôle otite moyenne aiguë droite. Tympan normalisé. Audition cliniquement OK.',
 'Otite moyenne aiguë résolue',
 'Arrêt antibiotique terminé, surveillance',
 'Si récidive (otalgie, fièvre), reconsulter rapidement.',
 0),
(11, 36,
 'Consultation de printemps : fatigue, bilan TSH normal. Conseils sommeil.',
 'Asthénie réactionnelle',
 'Hygiène de sommeil, pas de traitement médicamenteux',
 'Coucher avant 23h. Limiter les écrans le soir.',
 1),
(12, 37,
 'Suivi tensionnel. TA 132/84. Légère prise de poids.',
 'Hypertension borderline',
 'Mesures hygiéno-diététiques, contrôle dans 2 mois',
 'Réduire le sel. Marcher 30 minutes par jour.',
 1),
(13, 38,
 'Première évaluation cardiaque. Souffle fonctionnel bénin.',
 'Souffle cardiaque fonctionnel',
 'Surveillance clinique annuelle',
 NULL,
 0),
(14, 41,
 'Examen ophtalmologique. Acuité 10/10 OD/OG. Fond d''œil normal.',
 'Examen ophtalmologique normal',
 'Aucune correction nécessaire',
 'Contrôle dans 2 ans, ou plus tôt en cas de baisse d''acuité.',
 1),
(15, 42,
 'Première visite : anamnèse complète, examen clinique sans particularité.',
 'Patiente en bonne santé apparente',
 'Mise à jour carnet de vaccination conseillée',
 'Apporter le carnet de vaccination à la prochaine visite.',
 1),
(16, 43,
 'Certificat pour sport scolaire. Examen cardio-respiratoire normal.',
 'Aptitude au sport scolaire',
 'Certificat délivré',
 'Valable pour l''année scolaire en cours.',
 1);

COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
