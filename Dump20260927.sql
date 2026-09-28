CREATE DATABASE  IF NOT EXISTS `timetable_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `timetable_db`;
-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: timetable_db
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `admin`
--

DROP TABLE IF EXISTS `admin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin` (
  `admin_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password_hash` varchar(64) NOT NULL,
  PRIMARY KEY (`admin_id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin`
--

LOCK TABLES `admin` WRITE;
/*!40000 ALTER TABLE `admin` DISABLE KEYS */;
/*!40000 ALTER TABLE `admin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `batch`
--

DROP TABLE IF EXISTS `batch`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `batch` (
  `batch_id` int NOT NULL AUTO_INCREMENT,
  `class_id` int NOT NULL,
  `batch_name` varchar(10) NOT NULL,
  `student_count` int NOT NULL,
  PRIMARY KEY (`batch_id`),
  UNIQUE KEY `uq_batch_per_class` (`class_id`,`batch_name`),
  CONSTRAINT `batch_ibfk_1` FOREIGN KEY (`class_id`) REFERENCES `class` (`class_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `batch`
--

LOCK TABLES `batch` WRITE;
/*!40000 ALTER TABLE `batch` DISABLE KEYS */;
INSERT INTO `batch` VALUES (3,3,'A',30),(4,3,'B',30);
/*!40000 ALTER TABLE `batch` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `batch_assignment`
--

DROP TABLE IF EXISTS `batch_assignment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `batch_assignment` (
  `batch_assignment_id` int NOT NULL AUTO_INCREMENT,
  `batch_id` int NOT NULL,
  `subject_id` int NOT NULL,
  `teacher_id` int NOT NULL,
  PRIMARY KEY (`batch_assignment_id`),
  KEY `batch_id` (`batch_id`),
  KEY `subject_id` (`subject_id`),
  KEY `teacher_id` (`teacher_id`),
  CONSTRAINT `batch_assignment_ibfk_1` FOREIGN KEY (`batch_id`) REFERENCES `batch` (`batch_id`),
  CONSTRAINT `batch_assignment_ibfk_2` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`subject_id`),
  CONSTRAINT `batch_assignment_ibfk_3` FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`teacher_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `batch_assignment`
--

LOCK TABLES `batch_assignment` WRITE;
/*!40000 ALTER TABLE `batch_assignment` DISABLE KEYS */;
INSERT INTO `batch_assignment` VALUES (3,3,9,8),(4,4,10,11);
/*!40000 ALTER TABLE `batch_assignment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `class`
--

DROP TABLE IF EXISTS `class`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `class` (
  `class_id` int NOT NULL AUTO_INCREMENT,
  `class_name` varchar(20) NOT NULL,
  `student_count` int NOT NULL,
  `home_room_id` int NOT NULL,
  PRIMARY KEY (`class_id`),
  UNIQUE KEY `class_name` (`class_name`),
  KEY `home_room_id` (`home_room_id`),
  CONSTRAINT `class_ibfk_1` FOREIGN KEY (`home_room_id`) REFERENCES `room` (`room_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `class`
--

LOCK TABLES `class` WRITE;
/*!40000 ALTER TABLE `class` DISABLE KEYS */;
INSERT INTO `class` VALUES (3,'S3AI',60,5),(4,'S3DS',30,6);
/*!40000 ALTER TABLE `class` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `room`
--

DROP TABLE IF EXISTS `room`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `room` (
  `room_id` int NOT NULL AUTO_INCREMENT,
  `room_name` varchar(50) NOT NULL,
  `room_type` enum('CLASSROOM','LAB') NOT NULL,
  `capacity` int NOT NULL,
  PRIMARY KEY (`room_id`),
  UNIQUE KEY `room_name` (`room_name`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `room`
--

LOCK TABLES `room` WRITE;
/*!40000 ALTER TABLE `room` DISABLE KEYS */;
INSERT INTO `room` VALUES (1,'Room 101','CLASSROOM',60),(2,'Lab 1','LAB',30),(3,'Lab 2','LAB',30),(5,'Room 102','CLASSROOM',60),(6,'Room 103','CLASSROOM',30);
/*!40000 ALTER TABLE `room` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `slot`
--

DROP TABLE IF EXISTS `slot`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `slot` (
  `slot_id` int NOT NULL AUTO_INCREMENT,
  `day` enum('MON','TUE','WED','THU','FRI') NOT NULL,
  `period` int NOT NULL,
  PRIMARY KEY (`slot_id`),
  UNIQUE KEY `uq_day_period` (`day`,`period`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `slot`
--

LOCK TABLES `slot` WRITE;
/*!40000 ALTER TABLE `slot` DISABLE KEYS */;
INSERT INTO `slot` VALUES (1,'MON',1),(2,'MON',2),(3,'MON',3),(4,'MON',4),(5,'MON',5),(6,'MON',6),(7,'MON',7),(8,'TUE',1),(9,'TUE',2),(10,'TUE',3),(11,'TUE',4),(12,'TUE',5),(13,'TUE',6),(14,'TUE',7),(15,'WED',1),(16,'WED',2),(17,'WED',3),(18,'WED',4),(19,'WED',5),(20,'WED',6),(21,'WED',7),(22,'THU',1),(23,'THU',2),(24,'THU',3),(25,'THU',4),(26,'THU',5),(27,'THU',6),(28,'THU',7),(29,'FRI',1),(30,'FRI',2),(31,'FRI',3),(32,'FRI',4),(33,'FRI',5),(34,'FRI',6),(35,'FRI',7);
/*!40000 ALTER TABLE `slot` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subject`
--

DROP TABLE IF EXISTS `subject`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subject` (
  `subject_id` int NOT NULL AUTO_INCREMENT,
  `subject_code` varchar(20) NOT NULL,
  `subject_name` varchar(100) NOT NULL,
  `subject_type` enum('THEORY','LAB') NOT NULL,
  `weekly_hours` int NOT NULL,
  `session_length` int NOT NULL DEFAULT '1',
  PRIMARY KEY (`subject_id`),
  UNIQUE KEY `subject_code` (`subject_code`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subject`
--

LOCK TABLES `subject` WRITE;
/*!40000 ALTER TABLE `subject` DISABLE KEYS */;
INSERT INTO `subject` VALUES (1,'CS301','DSA','THEORY',4,1),(2,'CS302','Python Lab','LAB',3,3),(3,'PCCST302','Theory of Computation','THEORY',5,1),(4,'GAMAT301','Mathematics for Computer and Information Science-3','THEORY',5,1),(5,'PBCST304','Object Oriented Programming','THEORY',5,1),(6,'PCCST303','Data Structures and Algorithms','THEORY',5,1),(7,'GAEST305','Digital Electronics and Logic Design','THEORY',5,1),(8,'UCHUT347','Engineering Ethics and Sustainable Development','THEORY',2,1),(9,'PCCSL307','Data Structures Lab','LAB',3,3),(10,'PCCAL308','Python Lab','LAB',3,3),(11,'PSMLXXX','Python and Statistical Modeling Lab','LAB',3,3);
/*!40000 ALTER TABLE `subject` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `teacher`
--

DROP TABLE IF EXISTS `teacher`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `teacher` (
  `teacher_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password_hash` varchar(64) NOT NULL,
  `max_weekly_hours` int NOT NULL DEFAULT '20',
  PRIMARY KEY (`teacher_id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `teacher`
--

LOCK TABLES `teacher` WRITE;
/*!40000 ALTER TABLE `teacher` DISABLE KEYS */;
INSERT INTO `teacher` VALUES (1,'Teacher A','teachera@test.com','placeholder',20),(2,'Teacher B','teacherb@test.com','placeholder',20),(3,'Teacher C','teacherc@test.com','placeholder',20),(4,'Teacher D','teacherd@test.com','placeholder',20),(5,'Sabitha M G','sabitha@asiet.ac.in','temp123',20),(6,'Dhanya R','dhanya@asiet.ac.in','temp123',20),(7,'Chaithanya C','chaithanya@asiet.ac.in','temp123',20),(8,'Reshmi V','reshmi@asiet.ac.in','temp123',20),(9,'Ganga Devi T R','gangadevi@asiet.ac.in','temp123',20),(10,'Divya V Chandran','divya@asiet.ac.in','temp123',20),(11,'Remya Ravindran','remya@asiet.ac.in','temp123',20),(12,'Amal Pavithran','amal.pavithran@placeholder.edu','placeholderhash1',20),(13,'Amrutha Muralidharan Nair','amrutha.nair@placeholder.edu','placeholderhash2',20),(14,'Hasna Hameed','hasna.hameed@placeholder.edu','placeholderhash3',20),(15,'Siji Jose Pulluparambil','siji.pulluparambil@placeholder.edu','placeholderhash4',20),(16,'TBD Lab1','tbd.lab1@placeholder.edu','placeholderhash5',20),(17,'TBD Lab2','tbd.lab2@placeholder.edu','placeholderhash6',20),(18,'Reshmi V','placeholder_1790482655518@example.com','changeme',20);
/*!40000 ALTER TABLE `teacher` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `teacher_subject_class`
--

DROP TABLE IF EXISTS `teacher_subject_class`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `teacher_subject_class` (
  `tsc_id` int NOT NULL AUTO_INCREMENT,
  `teacher_id` int NOT NULL,
  `subject_id` int NOT NULL,
  `class_id` int NOT NULL,
  PRIMARY KEY (`tsc_id`),
  UNIQUE KEY `uq_subject_per_class` (`subject_id`,`class_id`),
  KEY `teacher_id` (`teacher_id`),
  KEY `class_id` (`class_id`),
  CONSTRAINT `teacher_subject_class_ibfk_1` FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`teacher_id`),
  CONSTRAINT `teacher_subject_class_ibfk_2` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`subject_id`),
  CONSTRAINT `teacher_subject_class_ibfk_3` FOREIGN KEY (`class_id`) REFERENCES `class` (`class_id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `teacher_subject_class`
--

LOCK TABLES `teacher_subject_class` WRITE;
/*!40000 ALTER TABLE `teacher_subject_class` DISABLE KEYS */;
INSERT INTO `teacher_subject_class` VALUES (4,5,3,3),(5,6,4,3),(6,7,5,3),(7,8,6,3),(8,10,7,3),(9,9,8,3),(10,8,9,3),(11,11,10,3),(12,12,4,4),(13,5,3,4),(14,15,6,4),(15,13,5,4),(16,14,7,4),(17,9,8,4),(18,17,9,4),(19,16,11,4);
/*!40000 ALTER TABLE `teacher_subject_class` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `timetable_entry`
--

DROP TABLE IF EXISTS `timetable_entry`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `timetable_entry` (
  `entry_id` int NOT NULL AUTO_INCREMENT,
  `slot_id` int NOT NULL,
  `class_id` int NOT NULL,
  `subject_id` int NOT NULL,
  `teacher_id` int NOT NULL,
  `room_id` int NOT NULL,
  `batch_id` int DEFAULT NULL,
  `lab_session_group` int DEFAULT NULL,
  `status` enum('REGULAR','SUBSTITUTED') NOT NULL DEFAULT 'REGULAR',
  PRIMARY KEY (`entry_id`),
  KEY `slot_id` (`slot_id`),
  KEY `class_id` (`class_id`),
  KEY `subject_id` (`subject_id`),
  KEY `teacher_id` (`teacher_id`),
  KEY `room_id` (`room_id`),
  KEY `batch_id` (`batch_id`),
  CONSTRAINT `timetable_entry_ibfk_1` FOREIGN KEY (`slot_id`) REFERENCES `slot` (`slot_id`),
  CONSTRAINT `timetable_entry_ibfk_2` FOREIGN KEY (`class_id`) REFERENCES `class` (`class_id`),
  CONSTRAINT `timetable_entry_ibfk_3` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`subject_id`),
  CONSTRAINT `timetable_entry_ibfk_4` FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`teacher_id`),
  CONSTRAINT `timetable_entry_ibfk_5` FOREIGN KEY (`room_id`) REFERENCES `room` (`room_id`),
  CONSTRAINT `timetable_entry_ibfk_6` FOREIGN KEY (`batch_id`) REFERENCES `batch` (`batch_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1467 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `timetable_entry`
--

LOCK TABLES `timetable_entry` WRITE;
/*!40000 ALTER TABLE `timetable_entry` DISABLE KEYS */;
INSERT INTO `timetable_entry` VALUES (1401,1,4,9,17,3,NULL,0,'REGULAR'),(1402,2,4,9,17,3,NULL,0,'REGULAR'),(1403,3,4,9,17,3,NULL,0,'REGULAR'),(1404,8,4,11,16,3,NULL,1,'REGULAR'),(1405,9,4,11,16,3,NULL,1,'REGULAR'),(1406,10,4,11,16,3,NULL,1,'REGULAR'),(1407,22,4,4,12,6,NULL,NULL,'REGULAR'),(1408,17,4,4,12,6,NULL,NULL,'REGULAR'),(1409,35,4,4,12,6,NULL,NULL,'REGULAR'),(1410,34,4,4,12,6,NULL,NULL,'REGULAR'),(1411,16,4,4,12,6,NULL,NULL,'REGULAR'),(1412,24,4,3,5,6,NULL,NULL,'REGULAR'),(1413,33,4,3,5,6,NULL,NULL,'REGULAR'),(1414,15,4,3,5,6,NULL,NULL,'REGULAR'),(1415,23,4,3,5,6,NULL,NULL,'REGULAR'),(1416,13,4,3,5,6,NULL,NULL,'REGULAR'),(1417,21,4,6,15,6,NULL,NULL,'REGULAR'),(1418,30,4,6,15,6,NULL,NULL,'REGULAR'),(1419,25,4,6,15,6,NULL,NULL,'REGULAR'),(1420,5,4,6,15,6,NULL,NULL,'REGULAR'),(1421,14,4,6,15,6,NULL,NULL,'REGULAR'),(1422,26,4,5,13,6,NULL,NULL,'REGULAR'),(1423,31,4,5,13,6,NULL,NULL,'REGULAR'),(1424,20,4,5,13,6,NULL,NULL,'REGULAR'),(1425,6,4,5,13,6,NULL,NULL,'REGULAR'),(1426,12,4,5,13,6,NULL,NULL,'REGULAR'),(1427,27,4,7,14,6,NULL,NULL,'REGULAR'),(1428,7,4,7,14,6,NULL,NULL,'REGULAR'),(1429,18,4,7,14,6,NULL,NULL,'REGULAR'),(1430,32,4,7,14,6,NULL,NULL,'REGULAR'),(1431,11,4,7,14,6,NULL,NULL,'REGULAR'),(1432,29,4,8,9,6,NULL,NULL,'REGULAR'),(1433,19,4,8,9,6,NULL,NULL,'REGULAR'),(1434,1,3,9,8,2,3,2,'REGULAR'),(1435,2,3,9,8,2,3,2,'REGULAR'),(1436,3,3,9,8,2,3,2,'REGULAR'),(1437,8,3,10,11,2,4,3,'REGULAR'),(1438,9,3,10,11,2,4,3,'REGULAR'),(1439,10,3,10,11,2,4,3,'REGULAR'),(1440,25,3,3,5,5,NULL,NULL,'REGULAR'),(1441,31,3,3,5,5,NULL,NULL,'REGULAR'),(1442,19,3,3,5,5,NULL,NULL,'REGULAR'),(1443,35,3,3,5,5,NULL,NULL,'REGULAR'),(1444,21,3,3,5,5,NULL,NULL,'REGULAR'),(1445,22,3,4,6,5,NULL,NULL,'REGULAR'),(1446,23,3,4,6,5,NULL,NULL,'REGULAR'),(1447,17,3,4,6,5,NULL,NULL,'REGULAR'),(1448,30,3,4,6,5,NULL,NULL,'REGULAR'),(1449,5,3,4,6,5,NULL,NULL,'REGULAR'),(1450,12,3,5,7,5,NULL,NULL,'REGULAR'),(1451,34,3,5,7,5,NULL,NULL,'REGULAR'),(1452,20,3,5,7,5,NULL,NULL,'REGULAR'),(1453,26,3,5,7,5,NULL,NULL,'REGULAR'),(1454,4,3,5,7,5,NULL,NULL,'REGULAR'),(1455,14,3,6,8,5,NULL,NULL,'REGULAR'),(1456,29,3,6,8,5,NULL,NULL,'REGULAR'),(1457,24,3,6,8,5,NULL,NULL,'REGULAR'),(1458,16,3,6,8,5,NULL,NULL,'REGULAR'),(1459,6,3,6,8,5,NULL,NULL,'REGULAR'),(1460,32,3,7,10,5,NULL,NULL,'REGULAR'),(1461,15,3,7,10,5,NULL,NULL,'REGULAR'),(1462,27,3,7,10,5,NULL,NULL,'REGULAR'),(1463,13,3,7,10,5,NULL,NULL,'REGULAR'),(1464,7,3,7,10,5,NULL,NULL,'REGULAR'),(1465,18,3,8,9,5,NULL,NULL,'REGULAR'),(1466,28,3,8,9,5,NULL,NULL,'REGULAR');
/*!40000 ALTER TABLE `timetable_entry` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-27 20:12:13
