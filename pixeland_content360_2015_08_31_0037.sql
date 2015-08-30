-- MySQL dump 10.13  Distrib 5.5.8, for Win64 (x86)
--
-- Host: localhost    Database: pixeland_content360
-- ------------------------------------------------------
-- Server version	5.5.8

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `audit_trail`
--

DROP TABLE IF EXISTS `audit_trail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `audit_trail` (
  `id` bigint(20) NOT NULL,
  `data` varchar(4096) DEFAULT NULL,
  `module` varchar(255) DEFAULT NULL,
  `objectAffected` varchar(255) DEFAULT NULL,
  `process` varchar(255) DEFAULT NULL,
  `remotehost` varchar(255) DEFAULT NULL,
  `timeStamp` datetime DEFAULT NULL,
  `timeZone` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  `user_id_fk` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `uactidx` (`timeStamp`,`user_id_fk`,`username`),
  KEY `FK_dmwa0vmv88pnghccsyduxfwo` (`user_id_fk`),
  CONSTRAINT `FK_dmwa0vmv88pnghccsyduxfwo` FOREIGN KEY (`user_id_fk`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audit_trail`
--

LOCK TABLES `audit_trail` WRITE;
/*!40000 ALTER TABLE `audit_trail` DISABLE KEYS */;
/*!40000 ALTER TABLE `audit_trail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `baseentity`
--

DROP TABLE IF EXISTS `baseentity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `baseentity` (
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `baseentity`
--

LOCK TABLES `baseentity` WRITE;
/*!40000 ALTER TABLE `baseentity` DISABLE KEYS */;
/*!40000 ALTER TABLE `baseentity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `billable_queue`
--

DROP TABLE IF EXISTS `billable_queue`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `billable_queue` (
  `id` int(11) NOT NULL,
  `cp_id` varchar(255) DEFAULT NULL,
  `cp_tx_id` decimal(19,2) DEFAULT NULL,
  `discount_applied` varchar(255) DEFAULT NULL,
  `event_type` varchar(255) DEFAULT NULL,
  `in_outgoing_queue` bigint(20) DEFAULT NULL,
  `keyword` varchar(255) DEFAULT NULL,
  `maxRetriesAllowed` bigint(20) DEFAULT NULL,
  `message_id` bigint(20) DEFAULT NULL,
  `msisdn` varchar(255) DEFAULT NULL,
  `operation` varchar(255) DEFAULT NULL,
  `price` decimal(19,2) DEFAULT NULL,
  `price_point_keyword` varchar(255) DEFAULT NULL,
  `priority` bigint(20) DEFAULT NULL,
  `processed` bigint(20) DEFAULT NULL,
  `resp_status_code` varchar(255) DEFAULT NULL,
  `retry_count` bigint(20) DEFAULT NULL,
  `service_id` varchar(255) DEFAULT NULL,
  `shortcode` varchar(255) DEFAULT NULL,
  `success` tinyint(1) DEFAULT NULL,
  `timeStamp` datetime NOT NULL,
  `transactionId` varchar(255) DEFAULT NULL,
  `tx_id` decimal(19,2) DEFAULT NULL,
  `valid` tinyint(1) DEFAULT NULL,
  `opco_id_fk` bigint(20) NOT NULL,
  `opco_tx_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_7m5nl58ehxkxy8lv7kr6i7f5x` (`cp_tx_id`),
  KEY `cp_idtxid_idx` (`cp_tx_id`),
  KEY `outq_idx` (`in_outgoing_queue`),
  KEY `bilblmsisdidx` (`keyword`),
  KEY `bilblidx` (`message_id`,`price`,`resp_status_code`,`retry_count`,`success`),
  KEY `msisdnIdx` (`msisdn`,`service_id`),
  KEY `blblopcidx` (`opco_id_fk`),
  KEY `priority_idx` (`priority`),
  KEY `processed_idx` (`processed`),
  KEY `timeStamp_idx` (`timeStamp`),
  KEY `optxididx` (`transactionId`),
  KEY `FK_rh6jhf4fptx5mmgxx932svsc4` (`opco_id_fk`),
  KEY `opcotxid_idx` (`opco_tx_id`),
  CONSTRAINT `FK_rh6jhf4fptx5mmgxx932svsc4` FOREIGN KEY (`opco_id_fk`) REFERENCES `operator_country` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `billable_queue`
--

LOCK TABLES `billable_queue` WRITE;
/*!40000 ALTER TABLE `billable_queue` DISABLE KEYS */;
/*!40000 ALTER TABLE `billable_queue` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bulksms_account`
--

DROP TABLE IF EXISTS `bulksms_account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bulksms_account` (
  `id` bigint(20) NOT NULL,
  `accountCode` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `dateactivated` datetime DEFAULT NULL,
  `active` tinyint(1) DEFAULT NULL,
  `apiKey` varchar(255) DEFAULT NULL,
  `pwd` varchar(255) DEFAULT NULL,
  `uname` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_7bo64cobnpc97setiiy29p3fe` (`accountCode`),
  UNIQUE KEY `UK_movgpk5hpstcwq4bv9jdlkn4` (`name`),
  UNIQUE KEY `UK_3q46in6hpvy4x20dx7rmqrb8g` (`uname`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bulksms_account`
--

LOCK TABLES `bulksms_account` WRITE;
/*!40000 ALTER TABLE `bulksms_account` DISABLE KEYS */;
/*!40000 ALTER TABLE `bulksms_account` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bulksms_ipwhl`
--

DROP TABLE IF EXISTS `bulksms_ipwhl`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bulksms_ipwhl` (
  `id` bigint(20) NOT NULL,
  `active` tinyint(1) DEFAULT NULL,
  `ipaddress` varchar(255) DEFAULT NULL,
  `timecreated` datetime DEFAULT NULL,
  `timeunit` varchar(255) DEFAULT NULL,
  `validity` int(11) DEFAULT NULL,
  `accnt_id_fk` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `wlaccidx` (`accnt_id_fk`),
  KEY `ipadwidx` (`ipaddress`),
  KEY `FK_8n0bo15wxvdxsefsntmhafce2` (`accnt_id_fk`),
  CONSTRAINT `FK_8n0bo15wxvdxsefsntmhafce2` FOREIGN KEY (`accnt_id_fk`) REFERENCES `bulksms_account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bulksms_ipwhl`
--

LOCK TABLES `bulksms_ipwhl` WRITE;
/*!40000 ALTER TABLE `bulksms_ipwhl` DISABLE KEYS */;
/*!40000 ALTER TABLE `bulksms_ipwhl` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bulksms_plan`
--

DROP TABLE IF EXISTS `bulksms_plan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bulksms_plan` (
  `id` bigint(20) NOT NULL,
  `active` tinyint(1) DEFAULT NULL,
  `date_purch` datetime DEFAULT NULL,
  `dn_url` varchar(255) DEFAULT NULL,
  `max_outgoing_size` decimal(19,2) DEFAULT NULL,
  `no_of_sms` decimal(19,2) DEFAULT NULL,
  `planid` varchar(255) DEFAULT NULL,
  `processor_id` bigint(20) DEFAULT NULL,
  `protocol` varchar(255) DEFAULT NULL,
  `telcoid` varchar(255) DEFAULT NULL,
  `timeunit` varchar(255) DEFAULT NULL,
  `validity` int(11) DEFAULT NULL,
  `account_id_fk` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `plnaccidx` (`account_id_fk`,`planid`,`telcoid`),
  KEY `FK_ljlsbrvgwfq94ebku2usbs1wb` (`account_id_fk`),
  CONSTRAINT `FK_ljlsbrvgwfq94ebku2usbs1wb` FOREIGN KEY (`account_id_fk`) REFERENCES `bulksms_account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bulksms_plan`
--

LOCK TABLES `bulksms_plan` WRITE;
/*!40000 ALTER TABLE `bulksms_plan` DISABLE KEYS */;
/*!40000 ALTER TABLE `bulksms_plan` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bulksms_queue`
--

DROP TABLE IF EXISTS `bulksms_queue`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bulksms_queue` (
  `id` bigint(20) NOT NULL,
  `bulktxId` varchar(255) DEFAULT NULL,
  `cptxId` varchar(255) DEFAULT NULL,
  `max_retries` int(11) DEFAULT NULL,
  `msisdn` varchar(255) DEFAULT NULL,
  `priority` int(11) DEFAULT NULL,
  `retrycount` int(11) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `timelogged` datetime DEFAULT NULL,
  `timeunit` varchar(255) DEFAULT NULL,
  `validity` int(11) DEFAULT NULL,
  `txt_id_fk` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `logblkidx` (`bulktxId`),
  KEY `logcpidx` (`cptxId`),
  KEY `logmsidnidx` (`msisdn`),
  KEY `logstsidx` (`priority`,`status`,`timelogged`),
  KEY `FK_df39jof9d4nmbc5hgo3sk8990` (`txt_id_fk`),
  CONSTRAINT `FK_df39jof9d4nmbc5hgo3sk8990` FOREIGN KEY (`txt_id_fk`) REFERENCES `bulksms_text` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bulksms_queue`
--

LOCK TABLES `bulksms_queue` WRITE;
/*!40000 ALTER TABLE `bulksms_queue` DISABLE KEYS */;
/*!40000 ALTER TABLE `bulksms_queue` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bulksms_text`
--

DROP TABLE IF EXISTS `bulksms_text`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bulksms_text` (
  `id` bigint(20) NOT NULL,
  `content` varchar(720) DEFAULT NULL,
  `price` decimal(19,19) DEFAULT NULL,
  `queuesize` decimal(19,2) DEFAULT NULL,
  `senderid` varchar(255) DEFAULT NULL,
  `sheduledate` datetime DEFAULT NULL,
  `timecreated` datetime DEFAULT NULL,
  `timezone` varchar(255) DEFAULT NULL,
  `plan_id_fk` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `txtplnidx` (`plan_id_fk`),
  KEY `txtpidx` (`sheduledate`,`timecreated`),
  KEY `FK_y58sq8hilhnr403ra1lx9w7m` (`plan_id_fk`),
  CONSTRAINT `FK_y58sq8hilhnr403ra1lx9w7m` FOREIGN KEY (`plan_id_fk`) REFERENCES `bulksms_plan` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bulksms_text`
--

LOCK TABLES `bulksms_text` WRITE;
/*!40000 ALTER TABLE `bulksms_text` DISABLE KEYS */;
/*!40000 ALTER TABLE `bulksms_text` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cache_reset_cue`
--

DROP TABLE IF EXISTS `cache_reset_cue`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cache_reset_cue` (
  `id` bigint(20) NOT NULL,
  `module` varchar(255) DEFAULT NULL,
  `reset` tinyint(1) DEFAULT NULL,
  `resetafter` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cache_reset_cue`
--

LOCK TABLES `cache_reset_cue` WRITE;
/*!40000 ALTER TABLE `cache_reset_cue` DISABLE KEYS */;
/*!40000 ALTER TABLE `cache_reset_cue` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cmp_sequence`
--

DROP TABLE IF EXISTS `cmp_sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cmp_sequence` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) NOT NULL,
  `nextval` decimal(19,2) DEFAULT NULL,
  `prefix` varchar(255) DEFAULT NULL,
  `suffix` varchar(255) DEFAULT NULL,
  `timeStamp` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_4onpmecni7xf3ah4afp8ixi51` (`name`),
  KEY `cmpsqnmidx` (`name`),
  KEY `cmpsts0idx` (`timeStamp`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cmp_sequence`
--

LOCK TABLES `cmp_sequence` WRITE;
/*!40000 ALTER TABLE `cmp_sequence` DISABLE KEYS */;
/*!40000 ALTER TABLE `cmp_sequence` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `country`
--

DROP TABLE IF EXISTS `country`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `country` (
  `id` bigint(20) NOT NULL,
  `isdcode` varchar(255) NOT NULL,
  `isocode` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `timeZone` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_68ea5ais2jluul4fgyp0khq8b` (`isdcode`),
  UNIQUE KEY `UK_llidyp77h6xkeokpbmoy710d4` (`name`),
  UNIQUE KEY `UK_2y2bpew41sjwo6gbs3bnmtml7` (`timeZone`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `country`
--

LOCK TABLES `country` WRITE;
/*!40000 ALTER TABLE `country` DISABLE KEYS */;
INSERT INTO `country` VALUES (79484794,'254','KEN','Kenya','Africa/Nairobi');
/*!40000 ALTER TABLE `country` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dating_cellidranges`
--

DROP TABLE IF EXISTS `dating_cellidranges`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dating_cellidranges` (
  `id` bigint(20) NOT NULL,
  `location_id` bigint(20) NOT NULL,
  `max_cell_id` bigint(20) NOT NULL,
  `min_cell_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_iib1ueqv4mx6e51pprutpembc` (`location_id`),
  KEY `rlocididx` (`location_id`),
  KEY `crmxclidx` (`max_cell_id`),
  KEY `crmnclidx` (`min_cell_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dating_cellidranges`
--

LOCK TABLES `dating_cellidranges` WRITE;
/*!40000 ALTER TABLE `dating_cellidranges` DISABLE KEYS */;
/*!40000 ALTER TABLE `dating_cellidranges` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dating_chatlog`
--

DROP TABLE IF EXISTS `dating_chatlog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dating_chatlog` (
  `id` bigint(20) NOT NULL,
  `dest_person_id` bigint(20) NOT NULL,
  `message` varchar(255) DEFAULT NULL,
  `offline_msg` tinyint(1) DEFAULT NULL,
  `source_person_id` bigint(20) NOT NULL,
  `timeStamp` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `destpIdx` (`dest_person_id`),
  KEY `sourcePidx` (`source_person_id`),
  KEY `chltsmpidx` (`timeStamp`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dating_chatlog`
--

LOCK TABLES `dating_chatlog` WRITE;
/*!40000 ALTER TABLE `dating_chatlog` DISABLE KEYS */;
/*!40000 ALTER TABLE `dating_chatlog` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dating_disallowedwords`
--

DROP TABLE IF EXISTS `dating_disallowedwords`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dating_disallowedwords` (
  `id` int(11) NOT NULL,
  `word` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `dsalwedidx` (`word`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dating_disallowedwords`
--

LOCK TABLES `dating_disallowedwords` WRITE;
/*!40000 ALTER TABLE `dating_disallowedwords` DISABLE KEYS */;
/*!40000 ALTER TABLE `dating_disallowedwords` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dating_location`
--

DROP TABLE IF EXISTS `dating_location`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dating_location` (
  `id` bigint(20) NOT NULL,
  `cellid` bigint(20) NOT NULL,
  `locationName` varchar(255) DEFAULT NULL,
  `location_id` bigint(20) NOT NULL,
  `timeStamp` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `cellIdx` (`cellid`),
  KEY `locNameIdx` (`locationName`),
  KEY `locIdx` (`location_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dating_location`
--

LOCK TABLES `dating_location` WRITE;
/*!40000 ALTER TABLE `dating_location` DISABLE KEYS */;
/*!40000 ALTER TABLE `dating_location` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dating_person`
--

DROP TABLE IF EXISTS `dating_person`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dating_person` (
  `id` bigint(20) NOT NULL,
  `active` tinyint(1) DEFAULT NULL,
  `agreed_to_tnc` tinyint(1) DEFAULT NULL,
  `loggedin` tinyint(1) DEFAULT NULL,
  `msisdn` varchar(255) DEFAULT NULL,
  `opco_id_fk` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `active` (`active`),
  KEY `agreed_to_tnc` (`agreed_to_tnc`),
  KEY `msisdnidx` (`msisdn`),
  KEY `popcoidx` (`opco_id_fk`),
  KEY `FK_mldyry7ob86ju8b4iglh5dgjt` (`opco_id_fk`),
  CONSTRAINT `FK_mldyry7ob86ju8b4iglh5dgjt` FOREIGN KEY (`opco_id_fk`) REFERENCES `operator_country` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dating_person`
--

LOCK TABLES `dating_person` WRITE;
/*!40000 ALTER TABLE `dating_person` DISABLE KEYS */;
INSERT INTO `dating_person` VALUES (359,0,0,1,'254735594326',79497102);
/*!40000 ALTER TABLE `dating_person` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dating_profile`
--

DROP TABLE IF EXISTS `dating_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dating_profile` (
  `id` bigint(20) NOT NULL,
  `creationDate` datetime NOT NULL,
  `dob` datetime DEFAULT NULL,
  `gender` varchar(255) DEFAULT NULL,
  `language_id` int(11) DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `prefd_age` decimal(19,2) DEFAULT NULL,
  `pref_gender` varchar(255) DEFAULT NULL,
  `profileComplete` tinyint(1) DEFAULT NULL,
  `username` varchar(255) NOT NULL,
  `person_id_fk` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username_UNIQUE` (`username`),
  KEY `psnidfkidx` (`dob`,`person_id_fk`),
  KEY `usrnameIdx` (`username`),
  KEY `FK_dpff0y2rpx20hi8t034q1bewa` (`person_id_fk`),
  CONSTRAINT `FK_dpff0y2rpx20hi8t034q1bewa` FOREIGN KEY (`person_id_fk`) REFERENCES `dating_person` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dating_profile`
--

LOCK TABLES `dating_profile` WRITE;
/*!40000 ALTER TABLE `dating_profile` DISABLE KEYS */;
INSERT INTO `dating_profile` VALUES (360,'2015-08-31 00:22:36',NULL,NULL,0,NULL,NULL,NULL,0,'254735594326',359);
/*!40000 ALTER TABLE `dating_profile` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dating_profileloc`
--

DROP TABLE IF EXISTS `dating_profileloc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dating_profileloc` (
  `id` bigint(20) NOT NULL,
  `timeStamp` datetime NOT NULL,
  `location_id_fk` bigint(20) DEFAULT NULL,
  `profile_id_fk` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `plocidx` (`location_id_fk`),
  KEY `plocpidx` (`profile_id_fk`),
  KEY `tsidx` (`timeStamp`),
  KEY `FK_m87ixxtvrqrye1nlbo3r1rk30` (`location_id_fk`),
  KEY `FK_7bl2twb86s6cafrrvlgd41hi9` (`profile_id_fk`),
  CONSTRAINT `FK_7bl2twb86s6cafrrvlgd41hi9` FOREIGN KEY (`profile_id_fk`) REFERENCES `dating_profile` (`id`),
  CONSTRAINT `FK_m87ixxtvrqrye1nlbo3r1rk30` FOREIGN KEY (`location_id_fk`) REFERENCES `dating_location` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dating_profileloc`
--

LOCK TABLES `dating_profileloc` WRITE;
/*!40000 ALTER TABLE `dating_profileloc` DISABLE KEYS */;
/*!40000 ALTER TABLE `dating_profileloc` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dating_profilequestions`
--

DROP TABLE IF EXISTS `dating_profilequestions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dating_profilequestions` (
  `id` bigint(20) NOT NULL,
  `active` tinyint(1) DEFAULT NULL,
  `attrib` varchar(255) DEFAULT NULL,
  `language_id` bigint(20) DEFAULT NULL,
  `question` varchar(255) DEFAULT NULL,
  `serial` bigint(20) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dating_profilequestions`
--

LOCK TABLES `dating_profilequestions` WRITE;
/*!40000 ALTER TABLE `dating_profilequestions` DISABLE KEYS */;
INSERT INTO `dating_profilequestions` VALUES (0,1,'DISCLAIMER',1,'Welcome to SMS chat and friend finder! You can find friends near your area. You will meet real people so be kind to everyone. Proceed? \n \n1. No\n2. Yes',0,1),(1,1,'CHAT_USERNAME',1,'Question 1 of 4: Reply with a unique username that other people will use to chat with you.',0,1),(2,1,'GENDER',1,'Question 2 of 4: What is your gender <USERNAME>?\n1. Female\n2. Male',1,1),(3,1,'AGE',1,'Question 3 of 4: How old are you currently <USERNAME>? You must be 18 yrs and above. Reply with number e.g 18,24',2,1),(4,1,'LOCATION',1,'Last question <USERNAME>. To find a match near your area, we need to know your location. Where do you live?',3,1),(5,0,'PREFERRED_AGE',1,'What minimum age would you like your match to be? Note, age must be 18 yrs & above',4,1),(6,0,'PREFERRED_GENDER',1,'Question 6 of 6: What gender would you like your match to be <USERNAME> ?  Reply M for Male, F for Female.',5,1);
/*!40000 ALTER TABLE `dating_profilequestions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dating_questionlog`
--

DROP TABLE IF EXISTS `dating_questionlog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dating_questionlog` (
  `id` bigint(20) NOT NULL,
  `profile_id_fk` bigint(20) DEFAULT NULL,
  `question_id_fk` bigint(20) DEFAULT NULL,
  `timeStamp` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `profIdFkIdx` (`profile_id_fk`),
  KEY `sblogtmstp_idx` (`timeStamp`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dating_questionlog`
--

LOCK TABLES `dating_questionlog` WRITE;
/*!40000 ALTER TABLE `dating_questionlog` DISABLE KEYS */;
INSERT INTO `dating_questionlog` VALUES (316,315,0,'2015-08-30 23:41:48'),(332,331,0,'2015-08-31 00:13:12'),(361,360,0,'2015-08-31 00:22:36');
/*!40000 ALTER TABLE `dating_questionlog` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dating_systemmatchlog`
--

DROP TABLE IF EXISTS `dating_systemmatchlog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dating_systemmatchlog` (
  `id` bigint(20) NOT NULL,
  `creationDate` datetime NOT NULL,
  `person_a_id` bigint(20) DEFAULT NULL,
  `person_a_notified` tinyint(1) DEFAULT NULL,
  `person_b_id` bigint(20) DEFAULT NULL,
  `person_b_notified` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `psnAidx` (`person_a_id`,`person_b_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dating_systemmatchlog`
--

LOCK TABLES `dating_systemmatchlog` WRITE;
/*!40000 ALTER TABLE `dating_systemmatchlog` DISABLE KEYS */;
/*!40000 ALTER TABLE `dating_systemmatchlog` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hello_world_anthony`
--

DROP TABLE IF EXISTS `hello_world_anthony`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `hello_world_anthony` (
  `id` int(11) NOT NULL,
  `mo` varchar(255) DEFAULT NULL,
  `msisdn` varchar(255) DEFAULT NULL,
  `timeStamp` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hello_world_anthony`
--

LOCK TABLES `hello_world_anthony` WRITE;
/*!40000 ALTER TABLE `hello_world_anthony` DISABLE KEYS */;
/*!40000 ALTER TABLE `hello_world_anthony` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hibernate_sequence`
--

DROP TABLE IF EXISTS `hibernate_sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `hibernate_sequence` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hibernate_sequence`
--

LOCK TABLES `hibernate_sequence` WRITE;
/*!40000 ALTER TABLE `hibernate_sequence` DISABLE KEYS */;
INSERT INTO `hibernate_sequence` VALUES (414);
/*!40000 ALTER TABLE `hibernate_sequence` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `httptosend`
--

DROP TABLE IF EXISTS `httptosend`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `httptosend` (
  `id` bigint(20) NOT NULL,
  `CMP_AKeyword` varchar(255) DEFAULT NULL,
  `CMP_SKeyword` varchar(255) DEFAULT NULL,
  `CMP_TxID` decimal(19,2) DEFAULT NULL,
  `SMS_DataCodingId` bigint(20) DEFAULT NULL,
  `ACTION` varchar(255) DEFAULT NULL,
  `apiType` varchar(255) DEFAULT NULL,
  `billing_status` varchar(255) DEFAULT NULL,
  `charged` tinyint(1) DEFAULT NULL,
  `fromAddr` varchar(255) DEFAULT NULL,
  `in_outgoing_queue` tinyint(1) DEFAULT NULL,
  `mo_processorFK` bigint(20) DEFAULT NULL,
  `MSISDN` varchar(255) DEFAULT NULL,
  `newCMP_Txid` varchar(255) DEFAULT NULL,
  `price` decimal(19,2) DEFAULT NULL,
  `price_point_keyword` varchar(255) DEFAULT NULL,
  `Priority` int(11) DEFAULT NULL,
  `re_tries` bigint(20) DEFAULT NULL,
  `SendFrom` varchar(255) DEFAULT NULL,
  `sent` tinyint(1) DEFAULT NULL,
  `serviceid` bigint(20) DEFAULT NULL,
  `SMS` varchar(1000) DEFAULT NULL,
  `split` tinyint(1) DEFAULT NULL,
  `sub_c_mobtel` varchar(255) DEFAULT NULL,
  `sub_deviceType` varchar(255) DEFAULT NULL,
  `sub_r_mobtel` varchar(255) DEFAULT NULL,
  `subscription` tinyint(1) DEFAULT NULL,
  `timestamp` datetime DEFAULT NULL,
  `ttl` bigint(20) DEFAULT NULL,
  `Type` varchar(255) DEFAULT NULL,
  `opco_id_fk` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `CMP_TxID` (`CMP_TxID`),
  KEY `httsopcoidx` (`opco_id_fk`),
  KEY `TimeStamp` (`timestamp`),
  KEY `FK_1hkmbvxvtrq7my9mmmrsp0hb` (`opco_id_fk`),
  CONSTRAINT `FK_1hkmbvxvtrq7my9mmmrsp0hb` FOREIGN KEY (`opco_id_fk`) REFERENCES `operator_country` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `httptosend`
--

LOCK TABLES `httptosend` WRITE;
/*!40000 ALTER TABLE `httptosend` DISABLE KEYS */;
/*!40000 ALTER TABLE `httptosend` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `incoming_sms`
--

DROP TABLE IF EXISTS `incoming_sms`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `incoming_sms` (
  `id` bigint(20) NOT NULL,
  `billing_status` varchar(255) DEFAULT NULL,
  `cmp_tx_id` varchar(255) DEFAULT NULL,
  `event_type` varchar(255) NOT NULL,
  `isSubscription` tinyint(1) NOT NULL,
  `msisdn` varchar(255) DEFAULT NULL,
  `opco_tx_id` varchar(255) DEFAULT NULL,
  `price` decimal(19,2) DEFAULT NULL,
  `price_point_keyword` varchar(255) DEFAULT NULL,
  `serviceid` bigint(20) DEFAULT NULL,
  `shortcode` varchar(255) DEFAULT NULL,
  `sms` varchar(1000) DEFAULT NULL,
  `split` tinyint(1) DEFAULT NULL,
  `timestamp` datetime DEFAULT NULL,
  `mo_ack` tinyint(1) NOT NULL,
  `processed` tinyint(1) NOT NULL,
  `processor_id` bigint(20) NOT NULL,
  `opco_id_fk` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_3xf28vu9re18eo04o3vpq3xaj` (`cmp_tx_id`),
  KEY `osmcmp_tx_id` (`cmp_tx_id`),
  KEY `osmopco_tx_id` (`opco_tx_id`),
  KEY `timestampidx` (`timestamp`),
  KEY `opcproflidx` (`opco_id_fk`),
  KEY `FK_mkafgk656op967f0ybshecsb5` (`processor_id`),
  KEY `FK_on0e84u6ebf8cx5vj1xlw16v` (`opco_id_fk`),
  CONSTRAINT `FK_on0e84u6ebf8cx5vj1xlw16v` FOREIGN KEY (`opco_id_fk`) REFERENCES `operator_country` (`id`),
  CONSTRAINT `FK_mkafgk656op967f0ybshecsb5` FOREIGN KEY (`processor_id`) REFERENCES `mo_processors` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `incoming_sms`
--

LOCK TABLES `incoming_sms` WRITE;
/*!40000 ALTER TABLE `incoming_sms` DISABLE KEYS */;
INSERT INTO `incoming_sms` VALUES (318,'NO_BILLING_REQUIRED','709591074453768','Content Purchase',0,'254735594326',NULL,0.00,'32329_MAPENZI',442,'32329','find',0,'2015-08-30 23:54:29',1,1,14,79497102),(334,'NO_BILLING_REQUIRED','710744827200832','Content Purchase',0,'254735594326',NULL,0.00,'32329_MAPENZI',442,'32329','find',0,'2015-08-31 00:13:42',1,1,14,79497102),(347,'NO_BILLING_REQUIRED','711277688660004','Content Purchase',0,'254735594326',NULL,0.00,'32329_MAPENZI',442,'32329','find',0,'2015-08-31 00:22:35',1,1,14,79497102),(363,'NO_BILLING_REQUIRED','711656470602338','Content Purchase',0,'254735594326',NULL,0.00,'32329_JOBS',369,'32329','2',0,'2015-08-31 00:28:54',1,1,11,79497102),(375,'NO_BILLING_REQUIRED','711823195393111','Content Purchase',0,'254735594326',NULL,0.00,'32329_JOBS',369,'32329','2',0,'2015-08-31 00:31:41',1,1,11,79497102),(388,'NO_BILLING_REQUIRED','711885348402667','Content Purchase',0,'254735594326',NULL,0.00,'32329_JOBS',369,'32329','2',0,'2015-08-31 00:32:43',1,1,11,79497102),(401,'NO_BILLING_REQUIRED','712059468000397','Content Purchase',0,'254735594326',NULL,0.00,'32329_JOBS',369,'32329','2',0,'2015-08-31 00:35:37',1,1,11,79497102);
/*!40000 ALTER TABLE `incoming_sms` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `keywords`
--

DROP TABLE IF EXISTS `keywords`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `keywords` (
  `id` bigint(20) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `keyword` varchar(255) DEFAULT NULL,
  `price` double DEFAULT NULL,
  `subscription_push_tail_text` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `keywords`
--

LOCK TABLES `keywords` WRITE;
/*!40000 ALTER TABLE `keywords` DISABLE KEYS */;
/*!40000 ALTER TABLE `keywords` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `latency_log`
--

DROP TABLE IF EXISTS `latency_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `latency_log` (
  `id` bigint(20) NOT NULL,
  `latency` bigint(20) DEFAULT NULL,
  `link` varchar(255) NOT NULL,
  `timeStamp` datetime NOT NULL,
  `opco_id_fk` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `lnkidx` (`link`),
  KEY `llocpidx` (`opco_id_fk`),
  KEY `tmeStmp_idx` (`timeStamp`),
  KEY `FK_fl45nn97vcw8w08scy71jd3ue` (`opco_id_fk`),
  CONSTRAINT `FK_fl45nn97vcw8w08scy71jd3ue` FOREIGN KEY (`opco_id_fk`) REFERENCES `operator_country` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `latency_log`
--

LOCK TABLES `latency_log` WRITE;
/*!40000 ALTER TABLE `latency_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `latency_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `message`
--

DROP TABLE IF EXISTS `message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `message` (
  `id` bigint(20) NOT NULL,
  `msg_key` varchar(50) DEFAULT NULL,
  `language_id` bigint(20) DEFAULT NULL,
  `message` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_mibry4298oykj3sswtqy4x39a` (`language_id`,`msg_key`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `message`
--

LOCK TABLES `message` WRITE;
/*!40000 ALTER TABLE `message` DISABLE KEYS */;
INSERT INTO `message` VALUES (1,'UNKNOWN_KEYWORD_ADVICE',1,'Unknown keyword. Reply with MENU to see available services. Looking for a date? Reply with the word \'date\' and we\'ll find a match for you!'),(2,'MAIN_MENU_ADVICE',1,'Reply with number to see sub-menu, 0 to go back. SMS cost 0.0/-'),(3,'CONFIRMED_SUBSCRIPTION_ADVICE',1,'You\'ve been subscribed to \"<SERVICE_NAME>\". Quality content sent daily. Cost <SMS_SUBSCRIPTION_PRICE> /- @sms. To unsubscribe, reply \"STOP <KEYWORD>\". Reply 0 to go back to menu.'),(5,'SUBSCRIPTION_ADVICE',1,'Reply with corresponding number to subscribe, 0 to go back.'),(6,'ALREADY_SUBSCRIBED_ADVICE',1,'You\'re already subscribed to <KEYWORD>. If you want to unsubscribe, reply \"STOP <KEYWORD>\"'),(7,'MAIN_MENU_ADVICE',2,'Reply with number, 0 to go back. SMS cost 0.0/-'),(8,'SUBSCRIPTION_ADVICE',2,'You\'ve been subscribed to <SERVICE>. SMS Cost <PRICE>/- daily.'),(9,'DOUBLE_CONFIRMATION_ADVICE',1,'Reply with \"ON <CHOSEN>\" to confirm content subscription to <SERVICE_NAME>,  0 To go back.'),(10,'CONFIRMED_SUBSCRIPTION_ADVICE',2,'You\'ve been subscribed to \"<SERVICE_NAME>\". Quality content sent daily. Cost <SMS_SUBSCRIPTION_PRICE> /- @sms. To unsubscribe, reply \"STOP <KEYWORD>\". Reply 0 to go back to menu.'),(11,'ALREADY_SUBSCRIBED_ADVICE',2,'You\'re already subscribed to <SERVICE_NAME>.  SMS Cost <PRICE>/- daily.'),(12,'NO_PENDING_SUBSCRIPTION_ADVICE',1,'You don\'t have any pending subscriptions'),(13,'UNSUBSCRIBED_SINGLE_SERVICE_ADVICE',1,'We are sad to see you go :( You\'ve been unsubscribed from <SERVICE_NAME> and you\'ll no longer receive content from the service. Are you lonesome? Reply with \'DATE\' and we\'ll find someone for you.'),(14,'UNSUBSCRIBED_ALL_ADVICE',1,'You\'ve been unsubscribed from all services. Reply with MORE for more interesting content.'),(15,'INDIVIDUAL_UNSUBSCRIBE_ADVICE',1,'Reply with \"STOP #\" where # is the number corresponding to the service you want to unsubscribe from.'),(16,'NOT_SUBSCRIBED_TO_ANY_SERVICE_ADVICE',1,'You\'re not subscribed to any service.'),(17,'DATING_SUCCESS_REGISTRATION',1,'Please answer some questions to complete your dating profile.'),(18,'DATING_SUCCESS_REGISTRATION',2,'Answer only 6 questions to complete your dating profile.'),(19,'USERNAME_NOT_UNIQUE_TRY_AGAIN',1,'The username  \"<USERNAME>\" you chose is not unique. Please reply with another username.'),(20,'USERNAME_NOT_UNIQUE_TRY_AGAIN',2,'The username  \"<USERNAME>\" you chose is not unique. Please reply with another username.'),(21,'PROFILE_COMPLETE',1,'Dial *329# to purchase chat bundles or find a friend near your area'),(22,'PROFILE_COMPLETE',2,'Dial *329# to purchase chat bundles or find a friend near your area'),(23,'AGE_NUMBER_INCORRECT',1,'Sorry <USERNAME>, we didn\'t understand age specified. Please reply with a number representing age in years. For example 23'),(24,'AGE_NUMBER_INCORRECT',2,'Sorry <USERNAME>, we didn\'t understand age specified. Please reply with a number representing age in years. For example 23'),(25,'SERVICE_FOR_18_AND_ABOVE',1,'Sorry <USERNAME>, this service is only for adults who are 18 years and above. The age you specified does not qualify for this service.'),(26,'SERVICE_FOR_18_AND_ABOVE',2,'Sorry <USERNAME>, this service is only for adults who are 18 years and above. The age you specified does not qualify for this service.'),(35,'GENDER_NOT_UNDERSTOOD',1,'Sorry, gender not understood. Please specify either MALE or FEMALE.'),(36,'GENDER_NOT_UNDERSTOOD',2,'Sorry, gender not understood. Please specify either MALE or FEMALE.'),(37,'RENEW_SUBSCRIPTION',1,'Sorry, Please renew your subscription to continue enjoying enjoying the service. Dial *329# to purchase chat bundle.'),(38,'RENEW_SUBSCRIPTION',2,'Sorry, Please renew your subscription to continue enjoying enjoying the service. Dial *329# to purchase chat bundle.'),(39,'SUBSCRIPTION_RENEWED',1,'Your subscription has been renewed up to <EXPIRY_DATE>. You can now continue to enjoy the <SERVICE_NAME>.'),(40,'SUBSCRIPTION_RENEWED',2,'Your subscription has been renewed up to <EXPIRY_DATE>. You can now continue to enjoy the <SERVICE_NAME>.'),(41,'RENEW_CHAT_SUBSCRIPTION',1,'Sorry <USERNAME>. Please top up & renew your <SERVICE_NAME> subscription to continue chatting with <DEST_USERNAME>. Dial *329# to subscribe to chat bundles.'),(42,'RENEW_CHAT_SUBSCRIPTION',2,'Sorry <USERNAME>. Please top up & renew your <SERVICE_NAME> subscription to continue chatting with <DEST_USERNAME>. Dial *329# to subscribe to chat bundles.'),(43,'INSUFFICIENT_FUNDS',1,'Sorry, you don\'t have sufficient funds. Top up, try again and find friends near your area to chat with. Reply with FIND to see friends in your area.'),(44,'INSUFFICIENT_FUNDS',2,'Sorry, you don\'t have sufficient funds. Top up, try again and find friends near your area to chat with. Reply with FIND to see friends in your area.'),(45,'BILLING_FAILED',1,'Sorry, we couldn\'t process your request at this time. Please try again after some time.'),(46,'BILLING_FAILED',2,'Sorry, we couldn\'t process your request at this time. Please try again after some time.'),(47,'COULD_NOT_FIND_MATCH_AT_THE_MOMENT',1,'Sorry <USERNAME>, we couldn\'t find a match near you but do keep trying. We will also update you on SMS if the system finds a match for you.'),(48,'COULD_NOT_FIND_MATCH_AT_THE_MOMENT',2,'Sorry <USERNAME>, we couldn\'t find a match near you but do keep trying. We will also update you on SMS if the system finds a match for you.'),(49,'GENDER_PRONOUN_M',1,'his'),(50,'GENDER_PRONOUN_M',2,'his'),(51,'GENDER_PRONOUN_F',1,'her'),(52,'GENDER_PRONOUN_F',2,'her'),(53,'GENDER_PRONOUN_N',1,'them'),(54,'GENDER_PRONOUN_N',2,'them'),(55,'MATCH_FOUND',1,'We have a match 4 u <USERNAME>! <PROFILE>. <GENDER_PRONOUN> chat username is <DEST_USERNAME>. To chat with <GENDER_PRONOUN2>, compose a message starting with the word \"<DEST_USERNAME>\"'),(56,'MATCH_FOUND',2,'We have a match 4 u <USERNAME>! <PROFILE>. <GENDER_PRONOUN> chat username is <DEST_USERNAME>. To chat with <GENDER_PRONOUN2>, compose a message starting with the word \"<DEST_USERNAME>\"'),(57,'GENDER_PRONOUN_INCHAT_F',1,'her'),(58,'GENDER_PRONOUN_INCHAT_F',2,'her'),(59,'GENDER_PRONOUN_INCHAT_M',1,'him'),(60,'GENDER_PRONOUN_INCHAT_M',2,'him'),(62,'MUST_AGREE_TO_TNC',1,'You must agree/disagree to T&C b4 proceeding.'),(63,'MUST_AGREE_TO_TNC',2,'You must agree/disagree to T&C b4 proceeding.'),(68,'LOCATION_INVALID',1,'Hey <USERNAME>. Reply with the name of the area where you live in to complete your profile. E.g Mombasa, Kisumu, Nairobi. This helps us find a match for you'),(69,'LOCATION_INVALID',2,'Hey <USERNAME>. Reply with the name of the area where you live in to complete your profile. E.g Mombasa, Kisumu, Nairobi. This helps us find a match for you'),(70,'REPLY_WITH_USERNAME',1,'Welcome back! we thought we lost you there :) Reply with a unique username that your potential mates will use to chat with you.'),(71,'REPLY_WITH_USERNAME',2,'Welcome back! we thought we lost you there :) Reply with a unique username that your potential mates will use to chat with you.'),(74,'UNREALISTIC_AGE',1,'Sorry <USERNAME>, <AGE> years don\'t seem realistic :). How old are you? Please reply with your age in years'),(75,'UNREALISTIC_AGE',2,'Sorry <USERNAME>, <AGE> years don\'t seem realistic :). How old are you? Please reply with your age in years');
/*!40000 ALTER TABLE `message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `message_extra_params`
--

DROP TABLE IF EXISTS `message_extra_params`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `message_extra_params` (
  `id` bigint(20) NOT NULL,
  `paramKey` varchar(1000) NOT NULL,
  `paramValue` varchar(1000) NOT NULL,
  `transactionid` varchar(200) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `trxnididx` (`paramKey`(767),`transactionid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `message_extra_params`
--

LOCK TABLES `message_extra_params` WRITE;
/*!40000 ALTER TABLE `message_extra_params` DISABLE KEYS */;
INSERT INTO `message_extra_params` VALUES (101,'http_header_accept-language','en-US,en;q=0.8','684995074061712'),(102,'http_header_user-agent','Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36','684995074061712'),(103,'http_header_connection','keep-alive','684995074061712'),(104,'http_header_cache-control','max-age=0','684995074061712'),(105,'http_header_cookie','rememberMe=Uk7yydnB7bnqw+Y8cCOBTNWx51lMeWKhd72ixP86ZtIG3zUQImzJZKmhMzn7t/F+7XU4rUsB0tDhUGwkEsi8HkG9pqcqU/secwl07cUwotxCSVcWE7Oyi1Rp3C7VaGHPlXnJiwXR6Y+UYjzP39W+zzQEuM2AnQlFFUETYNnHyn2Y8mkG7Ms7EXQN8vA6nGNMb3lU3JiMYvEV9W3Du6AnS31+t1y5fj+d9vvZ4tLHZkFCVVdwppoqKyCkJrOTFgOLJvAJnqdoRGwqxFVQXvtQYspuTVdzOZNFt887+cEwqt+E7DaQsVjLVnn3F2QjPnB2cikPnkuZNFD5LknN/QZ7tw08cn7H7ppSd1ii760/lSju0K7jSk7j1dqNtUX9EIUxOi3qD8UzZqoo9DPtU9QBUs8U+5AtnSdk4vExI7jRiUGkInTRvcq2fwxCgL2Z7JGP8Jo07wuc8iq4zpHkIjgvGjHrY7O1ea1e99M/aDsEGN3tmi5ZIBXSFQIDGEdLmoAP; JSESSIONID=41317d4a-d7ba-40e6-a933-ba95d831031e','684995074061712'),(106,'http_header_accept-encoding','gzip, deflate','684995074061712'),(107,'http_header_accept','text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8','684995074061712'),(108,'ip.address','127.0.0.1','684995074061712'),(109,'http_header_upgrade-insecure-requests','1','684995074061712'),(110,'http_header_host','localhost:8880','684995074061712'),(113,'http_header_accept-language','en-US,en;q=0.8','685264415222215'),(114,'http_header_user-agent','Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36','685264415222215'),(115,'http_header_connection','keep-alive','685264415222215'),(116,'http_header_cache-control','max-age=0','685264415222215'),(117,'http_header_cookie','rememberMe=Uk7yydnB7bnqw+Y8cCOBTNWx51lMeWKhd72ixP86ZtIG3zUQImzJZKmhMzn7t/F+7XU4rUsB0tDhUGwkEsi8HkG9pqcqU/secwl07cUwotxCSVcWE7Oyi1Rp3C7VaGHPlXnJiwXR6Y+UYjzP39W+zzQEuM2AnQlFFUETYNnHyn2Y8mkG7Ms7EXQN8vA6nGNMb3lU3JiMYvEV9W3Du6AnS31+t1y5fj+d9vvZ4tLHZkFCVVdwppoqKyCkJrOTFgOLJvAJnqdoRGwqxFVQXvtQYspuTVdzOZNFt887+cEwqt+E7DaQsVjLVnn3F2QjPnB2cikPnkuZNFD5LknN/QZ7tw08cn7H7ppSd1ii760/lSju0K7jSk7j1dqNtUX9EIUxOi3qD8UzZqoo9DPtU9QBUs8U+5AtnSdk4vExI7jRiUGkInTRvcq2fwxCgL2Z7JGP8Jo07wuc8iq4zpHkIjgvGjHrY7O1ea1e99M/aDsEGN3tmi5ZIBXSFQIDGEdLmoAP; JSESSIONID=1e36c8e6-b79d-459c-b7c3-d4245e910774','685264415222215'),(118,'http_header_accept-encoding','gzip, deflate','685264415222215'),(119,'http_header_accept','text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8','685264415222215'),(120,'ip.address','127.0.0.1','685264415222215'),(121,'http_header_upgrade-insecure-requests','1','685264415222215'),(122,'http_header_host','localhost:8880','685264415222215'),(125,'http_header_accept-language','en-US,en;q=0.8','685361950428881'),(126,'http_header_user-agent','Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36','685361950428881'),(127,'http_header_connection','keep-alive','685361950428881'),(128,'http_header_cache-control','max-age=0','685361950428881'),(129,'http_header_cookie','rememberMe=Uk7yydnB7bnqw+Y8cCOBTNWx51lMeWKhd72ixP86ZtIG3zUQImzJZKmhMzn7t/F+7XU4rUsB0tDhUGwkEsi8HkG9pqcqU/secwl07cUwotxCSVcWE7Oyi1Rp3C7VaGHPlXnJiwXR6Y+UYjzP39W+zzQEuM2AnQlFFUETYNnHyn2Y8mkG7Ms7EXQN8vA6nGNMb3lU3JiMYvEV9W3Du6AnS31+t1y5fj+d9vvZ4tLHZkFCVVdwppoqKyCkJrOTFgOLJvAJnqdoRGwqxFVQXvtQYspuTVdzOZNFt887+cEwqt+E7DaQsVjLVnn3F2QjPnB2cikPnkuZNFD5LknN/QZ7tw08cn7H7ppSd1ii760/lSju0K7jSk7j1dqNtUX9EIUxOi3qD8UzZqoo9DPtU9QBUs8U+5AtnSdk4vExI7jRiUGkInTRvcq2fwxCgL2Z7JGP8Jo07wuc8iq4zpHkIjgvGjHrY7O1ea1e99M/aDsEGN3tmi5ZIBXSFQIDGEdLmoAP; JSESSIONID=93824d86-0051-48db-8244-186ab0f7c954','685361950428881'),(130,'http_header_accept-encoding','gzip, deflate','685361950428881'),(131,'http_header_accept','text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8','685361950428881'),(132,'ip.address','127.0.0.1','685361950428881'),(133,'http_header_upgrade-insecure-requests','1','685361950428881'),(134,'http_header_host','localhost:8880','685361950428881'),(137,'http_header_accept-language','en-US,en;q=0.8','687954009094887'),(138,'http_header_user-agent','Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36','687954009094887'),(139,'http_header_connection','keep-alive','687954009094887'),(140,'http_header_cache-control','max-age=0','687954009094887'),(141,'http_header_cookie','rememberMe=Uk7yydnB7bnqw+Y8cCOBTNWx51lMeWKhd72ixP86ZtIG3zUQImzJZKmhMzn7t/F+7XU4rUsB0tDhUGwkEsi8HkG9pqcqU/secwl07cUwotxCSVcWE7Oyi1Rp3C7VaGHPlXnJiwXR6Y+UYjzP39W+zzQEuM2AnQlFFUETYNnHyn2Y8mkG7Ms7EXQN8vA6nGNMb3lU3JiMYvEV9W3Du6AnS31+t1y5fj+d9vvZ4tLHZkFCVVdwppoqKyCkJrOTFgOLJvAJnqdoRGwqxFVQXvtQYspuTVdzOZNFt887+cEwqt+E7DaQsVjLVnn3F2QjPnB2cikPnkuZNFD5LknN/QZ7tw08cn7H7ppSd1ii760/lSju0K7jSk7j1dqNtUX9EIUxOi3qD8UzZqoo9DPtU9QBUs8U+5AtnSdk4vExI7jRiUGkInTRvcq2fwxCgL2Z7JGP8Jo07wuc8iq4zpHkIjgvGjHrY7O1ea1e99M/aDsEGN3tmi5ZIBXSFQIDGEdLmoAP; JSESSIONID=93824d86-0051-48db-8244-186ab0f7c954','687954009094887'),(142,'http_header_accept-encoding','gzip, deflate','687954009094887'),(143,'http_header_accept','text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8','687954009094887'),(144,'ip.address','127.0.0.1','687954009094887'),(145,'http_header_upgrade-insecure-requests','1','687954009094887'),(146,'http_header_host','localhost:8880','687954009094887'),(154,'http_header_accept-language','en-US,en;q=0.8','697528606010819'),(155,'http_header_user-agent','Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36','697528606010819'),(156,'http_header_accept-encoding','gzip, deflate','697528606010819'),(157,'http_header_cookie','rememberMe=Uk7yydnB7bnqw+Y8cCOBTNWx51lMeWKhd72ixP86ZtIG3zUQImzJZKmhMzn7t/F+7XU4rUsB0tDhUGwkEsi8HkG9pqcqU/secwl07cUwotxCSVcWE7Oyi1Rp3C7VaGHPlXnJiwXR6Y+UYjzP39W+zzQEuM2AnQlFFUETYNnHyn2Y8mkG7Ms7EXQN8vA6nGNMb3lU3JiMYvEV9W3Du6AnS31+t1y5fj+d9vvZ4tLHZkFCVVdwppoqKyCkJrOTFgOLJvAJnqdoRGwqxFVQXvtQYspuTVdzOZNFt887+cEwqt+E7DaQsVjLVnn3F2QjPnB2cikPnkuZNFD5LknN/QZ7tw08cn7H7ppSd1ii760/lSju0K7jSk7j1dqNtUX9EIUxOi3qD8UzZqoo9DPtU9QBUs8U+5AtnSdk4vExI7jRiUGkInTRvcq2fwxCgL2Z7JGP8Jo07wuc8iq4zpHkIjgvGjHrY7O1ea1e99M/aDsEGN3tmi5ZIBXSFQIDGEdLmoAP; JSESSIONID=ff1c8e64-75a8-48c4-99a1-1529fd276eda','697528606010819'),(158,'http_header_accept','text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8','697528606010819'),(159,'http_header_upgrade-insecure-requests','1','697528606010819'),(160,'ip.address','127.0.0.1','697528606010819'),(161,'http_header_host','localhost:8880','697528606010819'),(162,'http_header_connection','keep-alive','697528606010819'),(168,'http_header_accept-language','en-US,en;q=0.8','698804469585044'),(169,'http_header_user-agent','Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36','698804469585044'),(170,'http_header_connection','keep-alive','698804469585044'),(171,'http_header_cache-control','max-age=0','698804469585044'),(172,'http_header_cookie','rememberMe=Uk7yydnB7bnqw+Y8cCOBTNWx51lMeWKhd72ixP86ZtIG3zUQImzJZKmhMzn7t/F+7XU4rUsB0tDhUGwkEsi8HkG9pqcqU/secwl07cUwotxCSVcWE7Oyi1Rp3C7VaGHPlXnJiwXR6Y+UYjzP39W+zzQEuM2AnQlFFUETYNnHyn2Y8mkG7Ms7EXQN8vA6nGNMb3lU3JiMYvEV9W3Du6AnS31+t1y5fj+d9vvZ4tLHZkFCVVdwppoqKyCkJrOTFgOLJvAJnqdoRGwqxFVQXvtQYspuTVdzOZNFt887+cEwqt+E7DaQsVjLVnn3F2QjPnB2cikPnkuZNFD5LknN/QZ7tw08cn7H7ppSd1ii760/lSju0K7jSk7j1dqNtUX9EIUxOi3qD8UzZqoo9DPtU9QBUs8U+5AtnSdk4vExI7jRiUGkInTRvcq2fwxCgL2Z7JGP8Jo07wuc8iq4zpHkIjgvGjHrY7O1ea1e99M/aDsEGN3tmi5ZIBXSFQIDGEdLmoAP; JSESSIONID=35225a27-1d0e-48ce-a272-145f14eaa52b','698804469585044'),(173,'http_header_accept-encoding','gzip, deflate','698804469585044'),(174,'http_header_accept','text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8','698804469585044'),(175,'ip.address','127.0.0.1','698804469585044'),(176,'http_header_upgrade-insecure-requests','1','698804469585044'),(177,'http_header_host','localhost:8880','698804469585044'),(183,'http_header_accept-language','en-US,en;q=0.8','698903128222970'),(184,'http_header_user-agent','Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36','698903128222970'),(185,'http_header_accept-encoding','gzip, deflate','698903128222970'),(186,'http_header_cookie','rememberMe=Uk7yydnB7bnqw+Y8cCOBTNWx51lMeWKhd72ixP86ZtIG3zUQImzJZKmhMzn7t/F+7XU4rUsB0tDhUGwkEsi8HkG9pqcqU/secwl07cUwotxCSVcWE7Oyi1Rp3C7VaGHPlXnJiwXR6Y+UYjzP39W+zzQEuM2AnQlFFUETYNnHyn2Y8mkG7Ms7EXQN8vA6nGNMb3lU3JiMYvEV9W3Du6AnS31+t1y5fj+d9vvZ4tLHZkFCVVdwppoqKyCkJrOTFgOLJvAJnqdoRGwqxFVQXvtQYspuTVdzOZNFt887+cEwqt+E7DaQsVjLVnn3F2QjPnB2cikPnkuZNFD5LknN/QZ7tw08cn7H7ppSd1ii760/lSju0K7jSk7j1dqNtUX9EIUxOi3qD8UzZqoo9DPtU9QBUs8U+5AtnSdk4vExI7jRiUGkInTRvcq2fwxCgL2Z7JGP8Jo07wuc8iq4zpHkIjgvGjHrY7O1ea1e99M/aDsEGN3tmi5ZIBXSFQIDGEdLmoAP; JSESSIONID=795b9451-b7cf-47d5-a76d-70d6fee0a847','698903128222970'),(187,'http_header_accept','text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8','698903128222970'),(188,'http_header_upgrade-insecure-requests','1','698903128222970'),(189,'ip.address','127.0.0.1','698903128222970'),(190,'http_header_host','localhost:8880','698903128222970'),(191,'http_header_connection','keep-alive','698903128222970'),(195,'http_header_accept-language','en-US,en;q=0.8','699060599540452'),(196,'http_header_user-agent','Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36','699060599540452'),(197,'http_header_connection','keep-alive','699060599540452'),(198,'http_header_cache-control','max-age=0','699060599540452'),(199,'http_header_cookie','rememberMe=Uk7yydnB7bnqw+Y8cCOBTNWx51lMeWKhd72ixP86ZtIG3zUQImzJZKmhMzn7t/F+7XU4rUsB0tDhUGwkEsi8HkG9pqcqU/secwl07cUwotxCSVcWE7Oyi1Rp3C7VaGHPlXnJiwXR6Y+UYjzP39W+zzQEuM2AnQlFFUETYNnHyn2Y8mkG7Ms7EXQN8vA6nGNMb3lU3JiMYvEV9W3Du6AnS31+t1y5fj+d9vvZ4tLHZkFCVVdwppoqKyCkJrOTFgOLJvAJnqdoRGwqxFVQXvtQYspuTVdzOZNFt887+cEwqt+E7DaQsVjLVnn3F2QjPnB2cikPnkuZNFD5LknN/QZ7tw08cn7H7ppSd1ii760/lSju0K7jSk7j1dqNtUX9EIUxOi3qD8UzZqoo9DPtU9QBUs8U+5AtnSdk4vExI7jRiUGkInTRvcq2fwxCgL2Z7JGP8Jo07wuc8iq4zpHkIjgvGjHrY7O1ea1e99M/aDsEGN3tmi5ZIBXSFQIDGEdLmoAP; JSESSIONID=795b9451-b7cf-47d5-a76d-70d6fee0a847','699060599540452'),(200,'http_header_accept-encoding','gzip, deflate','699060599540452'),(201,'http_header_accept','text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8','699060599540452'),(202,'ip.address','127.0.0.1','699060599540452'),(203,'http_header_upgrade-insecure-requests','1','699060599540452'),(204,'http_header_host','localhost:8880','699060599540452'),(210,'http_header_accept-language','en-US,en;q=0.8','699331390211070'),(211,'http_header_user-agent','Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36','699331390211070'),(212,'http_header_connection','keep-alive','699331390211070'),(213,'http_header_cache-control','max-age=0','699331390211070'),(214,'http_header_cookie','rememberMe=Uk7yydnB7bnqw+Y8cCOBTNWx51lMeWKhd72ixP86ZtIG3zUQImzJZKmhMzn7t/F+7XU4rUsB0tDhUGwkEsi8HkG9pqcqU/secwl07cUwotxCSVcWE7Oyi1Rp3C7VaGHPlXnJiwXR6Y+UYjzP39W+zzQEuM2AnQlFFUETYNnHyn2Y8mkG7Ms7EXQN8vA6nGNMb3lU3JiMYvEV9W3Du6AnS31+t1y5fj+d9vvZ4tLHZkFCVVdwppoqKyCkJrOTFgOLJvAJnqdoRGwqxFVQXvtQYspuTVdzOZNFt887+cEwqt+E7DaQsVjLVnn3F2QjPnB2cikPnkuZNFD5LknN/QZ7tw08cn7H7ppSd1ii760/lSju0K7jSk7j1dqNtUX9EIUxOi3qD8UzZqoo9DPtU9QBUs8U+5AtnSdk4vExI7jRiUGkInTRvcq2fwxCgL2Z7JGP8Jo07wuc8iq4zpHkIjgvGjHrY7O1ea1e99M/aDsEGN3tmi5ZIBXSFQIDGEdLmoAP; JSESSIONID=795b9451-b7cf-47d5-a76d-70d6fee0a847','699331390211070'),(215,'http_header_accept-encoding','gzip, deflate','699331390211070'),(216,'http_header_accept','text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8','699331390211070'),(217,'ip.address','127.0.0.1','699331390211070'),(218,'http_header_upgrade-insecure-requests','1','699331390211070'),(219,'http_header_host','localhost:8880','699331390211070'),(225,'http_header_accept-language','en-US,en;q=0.8','702269116671059'),(226,'http_header_user-agent','Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36','702269116671059'),(227,'http_header_connection','keep-alive','702269116671059'),(228,'http_header_cache-control','max-age=0','702269116671059'),(229,'http_header_cookie','rememberMe=Uk7yydnB7bnqw+Y8cCOBTNWx51lMeWKhd72ixP86ZtIG3zUQImzJZKmhMzn7t/F+7XU4rUsB0tDhUGwkEsi8HkG9pqcqU/secwl07cUwotxCSVcWE7Oyi1Rp3C7VaGHPlXnJiwXR6Y+UYjzP39W+zzQEuM2AnQlFFUETYNnHyn2Y8mkG7Ms7EXQN8vA6nGNMb3lU3JiMYvEV9W3Du6AnS31+t1y5fj+d9vvZ4tLHZkFCVVdwppoqKyCkJrOTFgOLJvAJnqdoRGwqxFVQXvtQYspuTVdzOZNFt887+cEwqt+E7DaQsVjLVnn3F2QjPnB2cikPnkuZNFD5LknN/QZ7tw08cn7H7ppSd1ii760/lSju0K7jSk7j1dqNtUX9EIUxOi3qD8UzZqoo9DPtU9QBUs8U+5AtnSdk4vExI7jRiUGkInTRvcq2fwxCgL2Z7JGP8Jo07wuc8iq4zpHkIjgvGjHrY7O1ea1e99M/aDsEGN3tmi5ZIBXSFQIDGEdLmoAP; JSESSIONID=795b9451-b7cf-47d5-a76d-70d6fee0a847','702269116671059'),(230,'http_header_accept-encoding','gzip, deflate','702269116671059'),(231,'http_header_accept','text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8','702269116671059'),(232,'ip.address','127.0.0.1','702269116671059'),(233,'http_header_upgrade-insecure-requests','1','702269116671059'),(234,'http_header_host','localhost:8880','702269116671059'),(239,'http_header_accept-language','en-US,en;q=0.8','702683491896543'),(240,'http_header_user-agent','Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36','702683491896543'),(241,'http_header_connection','keep-alive','702683491896543'),(242,'http_header_cache-control','max-age=0','702683491896543'),(243,'http_header_cookie','rememberMe=Uk7yydnB7bnqw+Y8cCOBTNWx51lMeWKhd72ixP86ZtIG3zUQImzJZKmhMzn7t/F+7XU4rUsB0tDhUGwkEsi8HkG9pqcqU/secwl07cUwotxCSVcWE7Oyi1Rp3C7VaGHPlXnJiwXR6Y+UYjzP39W+zzQEuM2AnQlFFUETYNnHyn2Y8mkG7Ms7EXQN8vA6nGNMb3lU3JiMYvEV9W3Du6AnS31+t1y5fj+d9vvZ4tLHZkFCVVdwppoqKyCkJrOTFgOLJvAJnqdoRGwqxFVQXvtQYspuTVdzOZNFt887+cEwqt+E7DaQsVjLVnn3F2QjPnB2cikPnkuZNFD5LknN/QZ7tw08cn7H7ppSd1ii760/lSju0K7jSk7j1dqNtUX9EIUxOi3qD8UzZqoo9DPtU9QBUs8U+5AtnSdk4vExI7jRiUGkInTRvcq2fwxCgL2Z7JGP8Jo07wuc8iq4zpHkIjgvGjHrY7O1ea1e99M/aDsEGN3tmi5ZIBXSFQIDGEdLmoAP; JSESSIONID=795b9451-b7cf-47d5-a76d-70d6fee0a847','702683491896543'),(244,'http_header_accept-encoding','gzip, deflate','702683491896543'),(245,'http_header_accept','text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8','702683491896543'),(246,'ip.address','127.0.0.1','702683491896543'),(247,'http_header_upgrade-insecure-requests','1','702683491896543'),(248,'http_header_host','localhost:8880','702683491896543'),(253,'http_header_accept-language','en-US,en;q=0.8','703019746208547'),(254,'http_header_user-agent','Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36','703019746208547'),(255,'http_header_connection','keep-alive','703019746208547'),(256,'http_header_cache-control','max-age=0','703019746208547'),(257,'http_header_cookie','rememberMe=Uk7yydnB7bnqw+Y8cCOBTNWx51lMeWKhd72ixP86ZtIG3zUQImzJZKmhMzn7t/F+7XU4rUsB0tDhUGwkEsi8HkG9pqcqU/secwl07cUwotxCSVcWE7Oyi1Rp3C7VaGHPlXnJiwXR6Y+UYjzP39W+zzQEuM2AnQlFFUETYNnHyn2Y8mkG7Ms7EXQN8vA6nGNMb3lU3JiMYvEV9W3Du6AnS31+t1y5fj+d9vvZ4tLHZkFCVVdwppoqKyCkJrOTFgOLJvAJnqdoRGwqxFVQXvtQYspuTVdzOZNFt887+cEwqt+E7DaQsVjLVnn3F2QjPnB2cikPnkuZNFD5LknN/QZ7tw08cn7H7ppSd1ii760/lSju0K7jSk7j1dqNtUX9EIUxOi3qD8UzZqoo9DPtU9QBUs8U+5AtnSdk4vExI7jRiUGkInTRvcq2fwxCgL2Z7JGP8Jo07wuc8iq4zpHkIjgvGjHrY7O1ea1e99M/aDsEGN3tmi5ZIBXSFQIDGEdLmoAP; JSESSIONID=18f97276-aa75-403f-a1bd-adee76cd2e3e','703019746208547'),(258,'http_header_accept-encoding','gzip, deflate','703019746208547'),(259,'http_header_accept','text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8','703019746208547'),(260,'ip.address','127.0.0.1','703019746208547'),(261,'http_header_upgrade-insecure-requests','1','703019746208547'),(262,'http_header_host','localhost:8880','703019746208547'),(267,'http_header_accept-language','en-US,en;q=0.8','704285541445083'),(268,'http_header_user-agent','Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36','704285541445083'),(269,'http_header_connection','keep-alive','704285541445083'),(270,'http_header_cache-control','max-age=0','704285541445083'),(271,'http_header_cookie','rememberMe=Uk7yydnB7bnqw+Y8cCOBTNWx51lMeWKhd72ixP86ZtIG3zUQImzJZKmhMzn7t/F+7XU4rUsB0tDhUGwkEsi8HkG9pqcqU/secwl07cUwotxCSVcWE7Oyi1Rp3C7VaGHPlXnJiwXR6Y+UYjzP39W+zzQEuM2AnQlFFUETYNnHyn2Y8mkG7Ms7EXQN8vA6nGNMb3lU3JiMYvEV9W3Du6AnS31+t1y5fj+d9vvZ4tLHZkFCVVdwppoqKyCkJrOTFgOLJvAJnqdoRGwqxFVQXvtQYspuTVdzOZNFt887+cEwqt+E7DaQsVjLVnn3F2QjPnB2cikPnkuZNFD5LknN/QZ7tw08cn7H7ppSd1ii760/lSju0K7jSk7j1dqNtUX9EIUxOi3qD8UzZqoo9DPtU9QBUs8U+5AtnSdk4vExI7jRiUGkInTRvcq2fwxCgL2Z7JGP8Jo07wuc8iq4zpHkIjgvGjHrY7O1ea1e99M/aDsEGN3tmi5ZIBXSFQIDGEdLmoAP; JSESSIONID=fc70c9b6-df69-4cb0-9aea-bf72c62f6409','704285541445083'),(272,'http_header_accept-encoding','gzip, deflate','704285541445083'),(273,'http_header_accept','text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8','704285541445083'),(274,'ip.address','127.0.0.1','704285541445083'),(275,'http_header_upgrade-insecure-requests','1','704285541445083'),(276,'http_header_host','localhost:8880','704285541445083'),(279,'http_header_accept-language','en-US,en;q=0.8','708310311138014'),(280,'http_header_user-agent','Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36','708310311138014'),(281,'http_header_connection','keep-alive','708310311138014'),(282,'http_header_cache-control','max-age=0','708310311138014'),(283,'http_header_cookie','rememberMe=Uk7yydnB7bnqw+Y8cCOBTNWx51lMeWKhd72ixP86ZtIG3zUQImzJZKmhMzn7t/F+7XU4rUsB0tDhUGwkEsi8HkG9pqcqU/secwl07cUwotxCSVcWE7Oyi1Rp3C7VaGHPlXnJiwXR6Y+UYjzP39W+zzQEuM2AnQlFFUETYNnHyn2Y8mkG7Ms7EXQN8vA6nGNMb3lU3JiMYvEV9W3Du6AnS31+t1y5fj+d9vvZ4tLHZkFCVVdwppoqKyCkJrOTFgOLJvAJnqdoRGwqxFVQXvtQYspuTVdzOZNFt887+cEwqt+E7DaQsVjLVnn3F2QjPnB2cikPnkuZNFD5LknN/QZ7tw08cn7H7ppSd1ii760/lSju0K7jSk7j1dqNtUX9EIUxOi3qD8UzZqoo9DPtU9QBUs8U+5AtnSdk4vExI7jRiUGkInTRvcq2fwxCgL2Z7JGP8Jo07wuc8iq4zpHkIjgvGjHrY7O1ea1e99M/aDsEGN3tmi5ZIBXSFQIDGEdLmoAP; JSESSIONID=43380d6e-d45d-4fb1-8c06-754388407691','708310311138014'),(284,'http_header_accept-encoding','gzip, deflate','708310311138014'),(285,'http_header_accept','text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8','708310311138014'),(286,'ip.address','127.0.0.1','708310311138014'),(287,'http_header_upgrade-insecure-requests','1','708310311138014'),(288,'http_header_host','localhost:8880','708310311138014'),(291,'http_header_accept-language','en-US,en;q=0.8','708619741240721'),(292,'http_header_user-agent','Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36','708619741240721'),(293,'http_header_connection','keep-alive','708619741240721'),(294,'http_header_cache-control','max-age=0','708619741240721'),(295,'http_header_cookie','rememberMe=Uk7yydnB7bnqw+Y8cCOBTNWx51lMeWKhd72ixP86ZtIG3zUQImzJZKmhMzn7t/F+7XU4rUsB0tDhUGwkEsi8HkG9pqcqU/secwl07cUwotxCSVcWE7Oyi1Rp3C7VaGHPlXnJiwXR6Y+UYjzP39W+zzQEuM2AnQlFFUETYNnHyn2Y8mkG7Ms7EXQN8vA6nGNMb3lU3JiMYvEV9W3Du6AnS31+t1y5fj+d9vvZ4tLHZkFCVVdwppoqKyCkJrOTFgOLJvAJnqdoRGwqxFVQXvtQYspuTVdzOZNFt887+cEwqt+E7DaQsVjLVnn3F2QjPnB2cikPnkuZNFD5LknN/QZ7tw08cn7H7ppSd1ii760/lSju0K7jSk7j1dqNtUX9EIUxOi3qD8UzZqoo9DPtU9QBUs8U+5AtnSdk4vExI7jRiUGkInTRvcq2fwxCgL2Z7JGP8Jo07wuc8iq4zpHkIjgvGjHrY7O1ea1e99M/aDsEGN3tmi5ZIBXSFQIDGEdLmoAP; JSESSIONID=24c4ee10-d6c3-4b22-ae5f-90947f669401','708619741240721'),(296,'http_header_accept-encoding','gzip, deflate','708619741240721'),(297,'http_header_accept','text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8','708619741240721'),(298,'ip.address','127.0.0.1','708619741240721'),(299,'http_header_upgrade-insecure-requests','1','708619741240721'),(300,'http_header_host','localhost:8880','708619741240721'),(305,'http_header_accept-language','en-US,en;q=0.8','708813255139223'),(306,'http_header_user-agent','Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36','708813255139223'),(307,'http_header_connection','keep-alive','708813255139223'),(308,'http_header_cache-control','max-age=0','708813255139223'),(309,'http_header_cookie','rememberMe=Uk7yydnB7bnqw+Y8cCOBTNWx51lMeWKhd72ixP86ZtIG3zUQImzJZKmhMzn7t/F+7XU4rUsB0tDhUGwkEsi8HkG9pqcqU/secwl07cUwotxCSVcWE7Oyi1Rp3C7VaGHPlXnJiwXR6Y+UYjzP39W+zzQEuM2AnQlFFUETYNnHyn2Y8mkG7Ms7EXQN8vA6nGNMb3lU3JiMYvEV9W3Du6AnS31+t1y5fj+d9vvZ4tLHZkFCVVdwppoqKyCkJrOTFgOLJvAJnqdoRGwqxFVQXvtQYspuTVdzOZNFt887+cEwqt+E7DaQsVjLVnn3F2QjPnB2cikPnkuZNFD5LknN/QZ7tw08cn7H7ppSd1ii760/lSju0K7jSk7j1dqNtUX9EIUxOi3qD8UzZqoo9DPtU9QBUs8U+5AtnSdk4vExI7jRiUGkInTRvcq2fwxCgL2Z7JGP8Jo07wuc8iq4zpHkIjgvGjHrY7O1ea1e99M/aDsEGN3tmi5ZIBXSFQIDGEdLmoAP; JSESSIONID=a10bf9f1-8d01-4310-bb85-e806563ffc55','708813255139223'),(310,'http_header_accept-encoding','gzip, deflate','708813255139223'),(311,'http_header_accept','text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8','708813255139223'),(312,'ip.address','127.0.0.1','708813255139223'),(313,'http_header_upgrade-insecure-requests','1','708813255139223'),(314,'http_header_host','localhost:8880','708813255139223'),(320,'http_header_accept-language','en-US,en;q=0.8','709591074453768'),(321,'http_header_user-agent','Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36','709591074453768'),(322,'http_header_connection','keep-alive','709591074453768'),(323,'http_header_cache-control','max-age=0','709591074453768'),(324,'http_header_cookie','rememberMe=Uk7yydnB7bnqw+Y8cCOBTNWx51lMeWKhd72ixP86ZtIG3zUQImzJZKmhMzn7t/F+7XU4rUsB0tDhUGwkEsi8HkG9pqcqU/secwl07cUwotxCSVcWE7Oyi1Rp3C7VaGHPlXnJiwXR6Y+UYjzP39W+zzQEuM2AnQlFFUETYNnHyn2Y8mkG7Ms7EXQN8vA6nGNMb3lU3JiMYvEV9W3Du6AnS31+t1y5fj+d9vvZ4tLHZkFCVVdwppoqKyCkJrOTFgOLJvAJnqdoRGwqxFVQXvtQYspuTVdzOZNFt887+cEwqt+E7DaQsVjLVnn3F2QjPnB2cikPnkuZNFD5LknN/QZ7tw08cn7H7ppSd1ii760/lSju0K7jSk7j1dqNtUX9EIUxOi3qD8UzZqoo9DPtU9QBUs8U+5AtnSdk4vExI7jRiUGkInTRvcq2fwxCgL2Z7JGP8Jo07wuc8iq4zpHkIjgvGjHrY7O1ea1e99M/aDsEGN3tmi5ZIBXSFQIDGEdLmoAP; JSESSIONID=a10bf9f1-8d01-4310-bb85-e806563ffc55','709591074453768'),(325,'http_header_accept-encoding','gzip, deflate','709591074453768'),(326,'http_header_accept','text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8','709591074453768'),(327,'ip.address','127.0.0.1','709591074453768'),(328,'http_header_upgrade-insecure-requests','1','709591074453768'),(329,'http_header_host','localhost:8880','709591074453768'),(336,'http_header_accept-language','en-US,en;q=0.8','710744827200832'),(337,'http_header_user-agent','Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36','710744827200832'),(338,'http_header_connection','keep-alive','710744827200832'),(339,'http_header_cache-control','max-age=0','710744827200832'),(340,'http_header_cookie','rememberMe=Uk7yydnB7bnqw+Y8cCOBTNWx51lMeWKhd72ixP86ZtIG3zUQImzJZKmhMzn7t/F+7XU4rUsB0tDhUGwkEsi8HkG9pqcqU/secwl07cUwotxCSVcWE7Oyi1Rp3C7VaGHPlXnJiwXR6Y+UYjzP39W+zzQEuM2AnQlFFUETYNnHyn2Y8mkG7Ms7EXQN8vA6nGNMb3lU3JiMYvEV9W3Du6AnS31+t1y5fj+d9vvZ4tLHZkFCVVdwppoqKyCkJrOTFgOLJvAJnqdoRGwqxFVQXvtQYspuTVdzOZNFt887+cEwqt+E7DaQsVjLVnn3F2QjPnB2cikPnkuZNFD5LknN/QZ7tw08cn7H7ppSd1ii760/lSju0K7jSk7j1dqNtUX9EIUxOi3qD8UzZqoo9DPtU9QBUs8U+5AtnSdk4vExI7jRiUGkInTRvcq2fwxCgL2Z7JGP8Jo07wuc8iq4zpHkIjgvGjHrY7O1ea1e99M/aDsEGN3tmi5ZIBXSFQIDGEdLmoAP; JSESSIONID=c3183e07-842e-4bb7-b907-3a2a63a1c44e','710744827200832'),(341,'http_header_accept-encoding','gzip, deflate','710744827200832'),(342,'http_header_accept','text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8','710744827200832'),(343,'ip.address','127.0.0.1','710744827200832'),(344,'http_header_upgrade-insecure-requests','1','710744827200832'),(345,'http_header_host','localhost:8880','710744827200832'),(349,'http_header_accept-language','en-US,en;q=0.8','711277688660004'),(350,'http_header_user-agent','Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36','711277688660004'),(351,'http_header_connection','keep-alive','711277688660004'),(352,'http_header_cache-control','max-age=0','711277688660004'),(353,'http_header_cookie','rememberMe=Uk7yydnB7bnqw+Y8cCOBTNWx51lMeWKhd72ixP86ZtIG3zUQImzJZKmhMzn7t/F+7XU4rUsB0tDhUGwkEsi8HkG9pqcqU/secwl07cUwotxCSVcWE7Oyi1Rp3C7VaGHPlXnJiwXR6Y+UYjzP39W+zzQEuM2AnQlFFUETYNnHyn2Y8mkG7Ms7EXQN8vA6nGNMb3lU3JiMYvEV9W3Du6AnS31+t1y5fj+d9vvZ4tLHZkFCVVdwppoqKyCkJrOTFgOLJvAJnqdoRGwqxFVQXvtQYspuTVdzOZNFt887+cEwqt+E7DaQsVjLVnn3F2QjPnB2cikPnkuZNFD5LknN/QZ7tw08cn7H7ppSd1ii760/lSju0K7jSk7j1dqNtUX9EIUxOi3qD8UzZqoo9DPtU9QBUs8U+5AtnSdk4vExI7jRiUGkInTRvcq2fwxCgL2Z7JGP8Jo07wuc8iq4zpHkIjgvGjHrY7O1ea1e99M/aDsEGN3tmi5ZIBXSFQIDGEdLmoAP; JSESSIONID=5a4200f7-7025-4ea9-855a-75ba35664aa8','711277688660004'),(354,'http_header_accept-encoding','gzip, deflate','711277688660004'),(355,'http_header_accept','text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8','711277688660004'),(356,'ip.address','127.0.0.1','711277688660004'),(357,'http_header_upgrade-insecure-requests','1','711277688660004'),(358,'http_header_host','localhost:8880','711277688660004'),(365,'http_header_accept-language','en-US,en;q=0.8','711656470602338'),(366,'http_header_user-agent','Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36','711656470602338'),(367,'http_header_accept-encoding','gzip, deflate','711656470602338'),(368,'http_header_cookie','rememberMe=Uk7yydnB7bnqw+Y8cCOBTNWx51lMeWKhd72ixP86ZtIG3zUQImzJZKmhMzn7t/F+7XU4rUsB0tDhUGwkEsi8HkG9pqcqU/secwl07cUwotxCSVcWE7Oyi1Rp3C7VaGHPlXnJiwXR6Y+UYjzP39W+zzQEuM2AnQlFFUETYNnHyn2Y8mkG7Ms7EXQN8vA6nGNMb3lU3JiMYvEV9W3Du6AnS31+t1y5fj+d9vvZ4tLHZkFCVVdwppoqKyCkJrOTFgOLJvAJnqdoRGwqxFVQXvtQYspuTVdzOZNFt887+cEwqt+E7DaQsVjLVnn3F2QjPnB2cikPnkuZNFD5LknN/QZ7tw08cn7H7ppSd1ii760/lSju0K7jSk7j1dqNtUX9EIUxOi3qD8UzZqoo9DPtU9QBUs8U+5AtnSdk4vExI7jRiUGkInTRvcq2fwxCgL2Z7JGP8Jo07wuc8iq4zpHkIjgvGjHrY7O1ea1e99M/aDsEGN3tmi5ZIBXSFQIDGEdLmoAP; JSESSIONID=00f14032-1807-4062-9461-a05bd6fd6672','711656470602338'),(369,'http_header_accept','text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8','711656470602338'),(370,'http_header_upgrade-insecure-requests','1','711656470602338'),(371,'ip.address','127.0.0.1','711656470602338'),(372,'http_header_host','localhost:8880','711656470602338'),(373,'http_header_connection','keep-alive','711656470602338'),(377,'http_header_accept-language','en-US,en;q=0.8','711823195393111'),(378,'http_header_user-agent','Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36','711823195393111'),(379,'http_header_connection','keep-alive','711823195393111'),(380,'http_header_cache-control','max-age=0','711823195393111'),(381,'http_header_cookie','rememberMe=Uk7yydnB7bnqw+Y8cCOBTNWx51lMeWKhd72ixP86ZtIG3zUQImzJZKmhMzn7t/F+7XU4rUsB0tDhUGwkEsi8HkG9pqcqU/secwl07cUwotxCSVcWE7Oyi1Rp3C7VaGHPlXnJiwXR6Y+UYjzP39W+zzQEuM2AnQlFFUETYNnHyn2Y8mkG7Ms7EXQN8vA6nGNMb3lU3JiMYvEV9W3Du6AnS31+t1y5fj+d9vvZ4tLHZkFCVVdwppoqKyCkJrOTFgOLJvAJnqdoRGwqxFVQXvtQYspuTVdzOZNFt887+cEwqt+E7DaQsVjLVnn3F2QjPnB2cikPnkuZNFD5LknN/QZ7tw08cn7H7ppSd1ii760/lSju0K7jSk7j1dqNtUX9EIUxOi3qD8UzZqoo9DPtU9QBUs8U+5AtnSdk4vExI7jRiUGkInTRvcq2fwxCgL2Z7JGP8Jo07wuc8iq4zpHkIjgvGjHrY7O1ea1e99M/aDsEGN3tmi5ZIBXSFQIDGEdLmoAP; JSESSIONID=00f14032-1807-4062-9461-a05bd6fd6672','711823195393111'),(382,'http_header_accept-encoding','gzip, deflate','711823195393111'),(383,'http_header_accept','text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8','711823195393111'),(384,'ip.address','127.0.0.1','711823195393111'),(385,'http_header_upgrade-insecure-requests','1','711823195393111'),(386,'http_header_host','localhost:8880','711823195393111'),(390,'http_header_accept-language','en-US,en;q=0.8','711885348402667'),(391,'http_header_user-agent','Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36','711885348402667'),(392,'http_header_connection','keep-alive','711885348402667'),(393,'http_header_cache-control','max-age=0','711885348402667'),(394,'http_header_cookie','rememberMe=Uk7yydnB7bnqw+Y8cCOBTNWx51lMeWKhd72ixP86ZtIG3zUQImzJZKmhMzn7t/F+7XU4rUsB0tDhUGwkEsi8HkG9pqcqU/secwl07cUwotxCSVcWE7Oyi1Rp3C7VaGHPlXnJiwXR6Y+UYjzP39W+zzQEuM2AnQlFFUETYNnHyn2Y8mkG7Ms7EXQN8vA6nGNMb3lU3JiMYvEV9W3Du6AnS31+t1y5fj+d9vvZ4tLHZkFCVVdwppoqKyCkJrOTFgOLJvAJnqdoRGwqxFVQXvtQYspuTVdzOZNFt887+cEwqt+E7DaQsVjLVnn3F2QjPnB2cikPnkuZNFD5LknN/QZ7tw08cn7H7ppSd1ii760/lSju0K7jSk7j1dqNtUX9EIUxOi3qD8UzZqoo9DPtU9QBUs8U+5AtnSdk4vExI7jRiUGkInTRvcq2fwxCgL2Z7JGP8Jo07wuc8iq4zpHkIjgvGjHrY7O1ea1e99M/aDsEGN3tmi5ZIBXSFQIDGEdLmoAP; JSESSIONID=00f14032-1807-4062-9461-a05bd6fd6672','711885348402667'),(395,'http_header_accept-encoding','gzip, deflate','711885348402667'),(396,'http_header_accept','text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8','711885348402667'),(397,'ip.address','127.0.0.1','711885348402667'),(398,'http_header_upgrade-insecure-requests','1','711885348402667'),(399,'http_header_host','localhost:8880','711885348402667'),(403,'http_header_accept-language','en-US,en;q=0.8','712059468000397'),(404,'http_header_user-agent','Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36','712059468000397'),(405,'http_header_connection','keep-alive','712059468000397'),(406,'http_header_cache-control','max-age=0','712059468000397'),(407,'http_header_cookie','rememberMe=Uk7yydnB7bnqw+Y8cCOBTNWx51lMeWKhd72ixP86ZtIG3zUQImzJZKmhMzn7t/F+7XU4rUsB0tDhUGwkEsi8HkG9pqcqU/secwl07cUwotxCSVcWE7Oyi1Rp3C7VaGHPlXnJiwXR6Y+UYjzP39W+zzQEuM2AnQlFFUETYNnHyn2Y8mkG7Ms7EXQN8vA6nGNMb3lU3JiMYvEV9W3Du6AnS31+t1y5fj+d9vvZ4tLHZkFCVVdwppoqKyCkJrOTFgOLJvAJnqdoRGwqxFVQXvtQYspuTVdzOZNFt887+cEwqt+E7DaQsVjLVnn3F2QjPnB2cikPnkuZNFD5LknN/QZ7tw08cn7H7ppSd1ii760/lSju0K7jSk7j1dqNtUX9EIUxOi3qD8UzZqoo9DPtU9QBUs8U+5AtnSdk4vExI7jRiUGkInTRvcq2fwxCgL2Z7JGP8Jo07wuc8iq4zpHkIjgvGjHrY7O1ea1e99M/aDsEGN3tmi5ZIBXSFQIDGEdLmoAP; JSESSIONID=00f14032-1807-4062-9461-a05bd6fd6672','712059468000397'),(408,'http_header_accept-encoding','gzip, deflate','712059468000397'),(409,'http_header_accept','text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8','712059468000397'),(410,'ip.address','127.0.0.1','712059468000397'),(411,'http_header_upgrade-insecure-requests','1','712059468000397'),(412,'http_header_host','localhost:8880','712059468000397');
/*!40000 ALTER TABLE `message_extra_params` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `message_log`
--

DROP TABLE IF EXISTS `message_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `message_log` (
  `id` bigint(20) NOT NULL,
  `cmp_tx_id` varchar(255) DEFAULT NULL,
  `mo_processor_id_fk` bigint(20) DEFAULT NULL,
  `mo_sms` varchar(255) DEFAULT NULL,
  `mo_timestamp` datetime NOT NULL,
  `msisdn` varchar(255) DEFAULT NULL,
  `mt_sms` varchar(255) DEFAULT NULL,
  `mt_timestamp` datetime DEFAULT NULL,
  `opco_tx_id` varchar(255) DEFAULT NULL,
  `retry_count` bigint(20) DEFAULT NULL,
  `shortcode` varchar(255) DEFAULT NULL,
  `source` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_rosl06kfpamnx59u6e1pu8ony` (`cmp_tx_id`),
  KEY `mlcmp_tx_id` (`cmp_tx_id`),
  KEY `motimestampidx` (`mo_timestamp`),
  KEY `msisdnidx` (`msisdn`),
  KEY `mttimestampidx` (`mt_timestamp`),
  KEY `mlopco_tx_id` (`opco_tx_id`),
  KEY `sourceidx` (`source`),
  KEY `statusidx` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `message_log`
--

LOCK TABLES `message_log` WRITE;
/*!40000 ALTER TABLE `message_log` DISABLE KEYS */;
INSERT INTO `message_log` VALUES (112,'685264415222215',14,'meru ae','2015-08-30 17:09:02',NULL,NULL,NULL,NULL,0,'32329','sms','RECEIVED'),(124,'685361950428881',14,'meru ae','2015-08-30 17:10:39',NULL,NULL,NULL,NULL,0,'32329','sms','RECEIVED'),(136,'687954009094887',14,'meru ae','2015-08-30 17:53:51',NULL,NULL,NULL,NULL,0,'32329','sms','RECEIVED'),(153,'697528606010819',14,'meru ae','2015-08-30 20:33:26',NULL,NULL,NULL,NULL,0,'32329','sms','RECEIVED'),(167,'698804469585044',14,'meru ae','2015-08-30 20:54:42',NULL,NULL,NULL,NULL,0,'32329','sms','RECEIVED'),(182,'698903128222970',14,'find','2015-08-30 20:56:21',NULL,NULL,NULL,NULL,0,'32329','sms','RECEIVED'),(194,'699060599540452',14,'find','2015-08-30 20:58:58',NULL,NULL,NULL,NULL,0,'32329','sms','RECEIVED'),(209,'699331390211070',14,'find','2015-08-30 21:03:29',NULL,NULL,NULL,NULL,0,'32329','sms','RECEIVED'),(224,'702269116671059',14,'find','2015-08-30 21:52:27',NULL,NULL,NULL,NULL,0,'32329','sms','RECEIVED'),(238,'702683491896543',14,'find','2015-08-30 21:59:21',NULL,NULL,NULL,NULL,0,'32329','sms','RECEIVED'),(252,'703019746208547',14,'find','2015-08-30 22:04:57',NULL,NULL,NULL,NULL,0,'32329','sms','RECEIVED'),(266,'704285541445083',14,'find','2015-08-30 22:26:03',NULL,NULL,NULL,NULL,0,'32329','sms','RECEIVED'),(278,'708310311138014',14,'find','2015-08-30 23:33:08',NULL,NULL,NULL,NULL,0,'32329','sms','RECEIVED'),(290,'708619741240721',14,'find','2015-08-30 23:38:17',NULL,NULL,NULL,NULL,0,'32329','sms','RECEIVED'),(304,'708813255139223',14,'find','2015-08-30 23:41:31',NULL,NULL,NULL,NULL,0,'32329','sms','SENT_SUCCESSFULLY'),(319,'709591074453768',14,'find','2015-08-30 23:54:29',NULL,NULL,NULL,NULL,0,'32329','sms','SENT_SUCCESSFULLY'),(335,'710744827200832',14,'find','2015-08-31 00:13:42',NULL,NULL,NULL,NULL,0,'32329','sms','RECEIVED'),(348,'711277688660004',14,'find','2015-08-31 00:22:35',NULL,NULL,NULL,NULL,0,'32329','sms','SENT_SUCCESSFULLY'),(364,'711656470602338',11,'2','2015-08-31 00:28:54',NULL,NULL,NULL,NULL,0,'32329','sms','RECEIVED'),(376,'711823195393111',11,'2','2015-08-31 00:31:41',NULL,NULL,NULL,NULL,0,'32329','sms','RECEIVED'),(389,'711885348402667',11,'2','2015-08-31 00:32:43',NULL,NULL,NULL,NULL,0,'32329','sms','RECEIVED'),(402,'712059468000397',11,'2','2015-08-31 00:35:37',NULL,NULL,NULL,NULL,0,'32329','sms','RECEIVED');
/*!40000 ALTER TABLE `message_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mo_processors`
--

DROP TABLE IF EXISTS `mo_processors`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `mo_processors` (
  `id` bigint(20) NOT NULL,
  `class_status` varchar(255) DEFAULT NULL,
  `enable` bigint(20) DEFAULT NULL,
  `forwarding_url` varchar(255) DEFAULT NULL,
  `ProcessorClass` varchar(255) DEFAULT NULL,
  `processor_type` varchar(255) DEFAULT NULL,
  `protocol` varchar(255) DEFAULT NULL,
  `ServiceName` varchar(255) DEFAULT NULL,
  `shortcode` varchar(255) DEFAULT NULL,
  `smppid` bigint(20) DEFAULT NULL,
  `threads` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mo_processors`
--

LOCK TABLES `mo_processors` WRITE;
/*!40000 ALTER TABLE `mo_processors` DISABLE KEYS */;
INSERT INTO `mo_processors` VALUES (11,'CLASS_NOT_LOADED_YET',1,'','com.pixelandtag.serviceprocessors.sms.MoreProcessor','LOCAL','http','Hello','32329',NULL,1),(12,'CLASS_NOT_LOADED_YET',1,'','com.pixelandtag.serviceprocessors.sms.StaticContentProcessor','LOCAL','http','Static_content','32329',NULL,1),(13,'CLASS_NOT_LOADED_YET',1,'','com.pixelandtag.serviceprocessors.sms.MoreProcessor','LOCAL','http','Menu_processor','32329',NULL,1),(14,'CLASS_NOT_LOADED_YET',1,'','com.pixelandtag.serviceprocessors.sms.DatingServiceProcessor','LOCAL','http','Dating Service Processor','32329',NULL,5),(15,'CLASS_NOT_LOADED_YET',1,'http://kunfoo.com:8080/Koonfoo/content360','com.pixelandtag.serviceprocessors.sms.ContentProxyProcessor','CONTENT_PROXY','smpp','Hello Anto','1393',1,1),(16,'CLASS_NOT_LOADED_YET',1,'','','PHANTOM','http','USSD Phantom proc','329',NULL,0),(17,'CLASS_NOT_LOADED_YET',1,'http://kunfoo.com:8080/Koonfoo/content360','com.pixelandtag.serviceprocessors.sms.ContentProxyProcessor','CONTENT_PROXY','smpp','Mpedigree 1323','1323',1,1);
/*!40000 ALTER TABLE `mo_processors` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `opco_configs`
--

DROP TABLE IF EXISTS `opco_configs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `opco_configs` (
  `id` bigint(20) NOT NULL,
  `data_type` varchar(255) DEFAULT NULL,
  `effectiveDate` datetime NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `value` varchar(255) DEFAULT NULL,
  `opco_id_fk` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_n0hncg1ud37ithhyihb9tup9y` (`opco_id_fk`),
  KEY `opcconfidx` (`effectiveDate`,`opco_id_fk`,`name`),
  CONSTRAINT `FK_n0hncg1ud37ithhyihb9tup9y` FOREIGN KEY (`opco_id_fk`) REFERENCES `operator_country` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `opco_configs`
--

LOCK TABLES `opco_configs` WRITE;
/*!40000 ALTER TABLE `opco_configs` DISABLE KEYS */;
INSERT INTO `opco_configs` VALUES (79582482,'string','2015-08-16 00:00:00','http_msisdn_param_name','msisdn',79497164),(79582484,'string','2015-08-16 00:00:00','http_sms_msg_param_name','message',79497164),(79582486,'string','2015-08-16 00:00:00','http_useheader','yes',79497164),(79582492,'string','2015-08-16 00:00:00','http_header_auth_has_username_and_password','yes',79497164),(79590924,'string','2015-08-16 00:00:00','http_haspayload','yes',79497164),(79604829,'string','2015-08-16 00:00:00','http_is_restful','yes',79497164),(79604830,'string','2015-08-16 00:00:00','http_header_auth_username_param_name','spId',79497164),(79604832,'string','2015-08-16 00:00:00','http_header_auth_password_param_name','spPassword',79497164),(79604836,'string','2015-08-16 00:00:00','http_header_auth_password_encryptionmode','basicmd5',79497164),(79604837,'string','2015-08-16 00:00:00','http_header_auth_method_param_name','Basic',79497164),(79604840,'string','2015-08-16 00:00:00','http_header_auth_hasmultiple_kv_pairs','yes',79497164),(79604841,'string','2015-08-16 00:00:00','http_header_auth_param_spId','36',79497164),(79604842,'string','2015-08-16 00:00:00','http_header_auth_param_timeStamp','20150105152020',79497164),(79604851,'string','2015-08-16 00:00:00','http_header_auth_param_serviceId','34',79497164),(79604855,'string','2015-08-16 00:00:00','http_header_auth_param_name','Authorization',79497164),(79640684,'string','2015-08-16 00:00:00','http_payload_template_name','oneapi_json_template',79497164),(79640685,'string','2015-08-16 00:00:00','http_payload_param_senderaddress','32329',79497164);
/*!40000 ALTER TABLE `opco_configs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `opco_ip_map`
--

DROP TABLE IF EXISTS `opco_ip_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `opco_ip_map` (
  `id` bigint(20) NOT NULL,
  `ipaddress` varchar(255) DEFAULT NULL,
  `opco_id_fk` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `opcipadmapidx` (`opco_id_fk`),
  KEY `FK_rnc4wtsqfhwfsghrtlcreylyy` (`opco_id_fk`),
  CONSTRAINT `FK_rnc4wtsqfhwfsghrtlcreylyy` FOREIGN KEY (`opco_id_fk`) REFERENCES `operator_country` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `opco_ip_map`
--

LOCK TABLES `opco_ip_map` WRITE;
/*!40000 ALTER TABLE `opco_ip_map` DISABLE KEYS */;
INSERT INTO `opco_ip_map` VALUES (1,'127.0.0.1',79497102);
/*!40000 ALTER TABLE `opco_ip_map` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `opco_senderprofiles`
--

DROP TABLE IF EXISTS `opco_senderprofiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `opco_senderprofiles` (
  `id` bigint(20) NOT NULL,
  `active` tinyint(1) NOT NULL,
  `effectiveDate` datetime NOT NULL,
  `pickorder` int(11) NOT NULL,
  `opco_id_fk` bigint(20) DEFAULT NULL,
  `profile_id_fk` bigint(20) NOT NULL,
  `workers` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `opcproflidx` (`opco_id_fk`,`pickorder`,`profile_id_fk`),
  KEY `FK_17wvvcr6er67evms4yyuj5lpo` (`opco_id_fk`),
  KEY `FK_l3occxagp4da2t2bpks6khsih` (`profile_id_fk`),
  CONSTRAINT `FK_17wvvcr6er67evms4yyuj5lpo` FOREIGN KEY (`opco_id_fk`) REFERENCES `operator_country` (`id`),
  CONSTRAINT `FK_l3occxagp4da2t2bpks6khsih` FOREIGN KEY (`profile_id_fk`) REFERENCES `sender_profiles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `opco_senderprofiles`
--

LOCK TABLES `opco_senderprofiles` WRITE;
/*!40000 ALTER TABLE `opco_senderprofiles` DISABLE KEYS */;
INSERT INTO `opco_senderprofiles` VALUES (80457410,1,'2015-08-16 00:00:00',0,79497164,80452253,1),(89028422,1,'2015-08-16 00:00:00',0,79497102,89016855,1);
/*!40000 ALTER TABLE `opco_senderprofiles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `opco_templates`
--

DROP TABLE IF EXISTS `opco_templates`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `opco_templates` (
  `id` bigint(20) NOT NULL,
  `effectiveDate` datetime NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `value` varchar(10000) DEFAULT NULL,
  `opco_id_fk` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `opctpltfidx` (`effectiveDate`,`name`,`opco_id_fk`),
  KEY `FK_8ncegfnycr38cxdmrmpy8yqkl` (`opco_id_fk`),
  CONSTRAINT `FK_8ncegfnycr38cxdmrmpy8yqkl` FOREIGN KEY (`opco_id_fk`) REFERENCES `operator_country` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `opco_templates`
--

LOCK TABLES `opco_templates` WRITE;
/*!40000 ALTER TABLE `opco_templates` DISABLE KEYS */;
INSERT INTO `opco_templates` VALUES (2,'2015-08-16 00:00:00','oneapi_json_template','PAYLOAD','{\"validity_period\":\"991201230029000+\",\"address\":[\"tel:${http_payload_param_msisdn}\"],\"senderAddress\":\"${http_payload_param_senderaddress}\",\"message\":\"${http_payload_param_sms_msg}\",\"notifyURL\":\"http://test/test\",\"senderName\":\"${http_payload_param_senderaddress}\"}',79497164);
/*!40000 ALTER TABLE `opco_templates` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `operator`
--

DROP TABLE IF EXISTS `operator`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `operator` (
  `id` bigint(20) NOT NULL,
  `code` varchar(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_w9k9elcbisrwohlhyxy3orwn` (`code`),
  KEY `ortorcodidx` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `operator`
--

LOCK TABLES `operator` WRITE;
/*!40000 ALTER TABLE `operator` DISABLE KEYS */;
INSERT INTO `operator` VALUES (79493566,'639-3','Airtel'),(79494610,'639-7','Orange');
/*!40000 ALTER TABLE `operator` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `operator_country`
--

DROP TABLE IF EXISTS `operator_country`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `operator_country` (
  `id` bigint(20) NOT NULL,
  `code` varchar(255) DEFAULT NULL,
  `coutry_id_fk` bigint(20) DEFAULT NULL,
  `operator_id_fk` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_2opff31qioui5bkrg473mgftu` (`code`),
  KEY `opcoidx` (`coutry_id_fk`,`operator_id_fk`),
  KEY `FK_r660dtbdvkvmy79m9dw8l5u8q` (`coutry_id_fk`),
  KEY `FK_ri3jbjewe8s9w5dspphwcktyj` (`operator_id_fk`),
  CONSTRAINT `FK_r660dtbdvkvmy79m9dw8l5u8q` FOREIGN KEY (`coutry_id_fk`) REFERENCES `country` (`id`),
  CONSTRAINT `FK_ri3jbjewe8s9w5dspphwcktyj` FOREIGN KEY (`operator_id_fk`) REFERENCES `operator` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `operator_country`
--

LOCK TABLES `operator_country` WRITE;
/*!40000 ALTER TABLE `operator_country` DISABLE KEYS */;
INSERT INTO `operator_country` VALUES (79497102,'KEN-639-3',79484794,79493566),(79497164,'KEN-639-7',79484794,79494610);
/*!40000 ALTER TABLE `operator_country` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `outgoing_sms`
--

DROP TABLE IF EXISTS `outgoing_sms`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `outgoing_sms` (
  `id` bigint(20) NOT NULL,
  `billing_status` varchar(255) DEFAULT NULL,
  `charged` tinyint(1) DEFAULT NULL,
  `cmp_tx_id` varchar(255) DEFAULT NULL,
  `in_outgoing_queue` tinyint(1) DEFAULT NULL,
  `msisdn` varchar(255) DEFAULT NULL,
  `opco_tx_id` varchar(255) DEFAULT NULL,
  `price` decimal(19,2) DEFAULT NULL,
  `priority` int(11) DEFAULT NULL,
  `re_tries` bigint(20) DEFAULT NULL,
  `sent` tinyint(1) DEFAULT NULL,
  `serviceid` bigint(20) DEFAULT NULL,
  `shortcode` varchar(255) DEFAULT NULL,
  `sms` varchar(1000) DEFAULT NULL,
  `split` tinyint(1) DEFAULT NULL,
  `timestamp` datetime DEFAULT NULL,
  `ttl` bigint(20) DEFAULT NULL,
  `processor_id` bigint(20) DEFAULT NULL,
  `opco_profile_id` bigint(20) DEFAULT NULL,
  `event_type` varchar(255) NOT NULL,
  `isSubscription` tinyint(1) NOT NULL,
  `price_point_keyword` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `osmcmp_tx_id` (`cmp_tx_id`),
  KEY `osmopco_tx_id` (`opco_tx_id`),
  KEY `timestamp` (`timestamp`),
  KEY `FK_rlo2prjp6dbkcvq0ad99oerll` (`processor_id`),
  KEY `FK_htvbvogr4418jmfgwnlfh8m42` (`opco_profile_id`),
  KEY `timestampidx` (`timestamp`),
  CONSTRAINT `FK_htvbvogr4418jmfgwnlfh8m42` FOREIGN KEY (`opco_profile_id`) REFERENCES `opco_senderprofiles` (`id`),
  CONSTRAINT `FK_rlo2prjp6dbkcvq0ad99oerll` FOREIGN KEY (`processor_id`) REFERENCES `mo_processors` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `outgoing_sms`
--

LOCK TABLES `outgoing_sms` WRITE;
/*!40000 ALTER TABLE `outgoing_sms` DISABLE KEYS */;
INSERT INTO `outgoing_sms` VALUES (150,'NO_BILLING_REQUIRED',1,'687954009094887',1,'254735594326',NULL,0.00,3,0,0,362,'32329',NULL,0,'2015-08-30 17:53:51',3,NULL,NULL,'Content Purchase',0,'32329_JOBS'),(151,'NO_BILLING_REQUIRED',1,'693365460057457',1,'254735594326',NULL,0.00,3,0,0,439,'32329',NULL,NULL,'2015-08-30 19:24:03',3,NULL,NULL,'Subscription Purchase',0,'32329LOVECHAT'),(165,'NO_BILLING_REQUIRED',1,'697529305639782',1,'254735594326',NULL,0.00,3,0,0,439,'32329',NULL,NULL,'2015-08-30 20:33:27',3,NULL,NULL,'Subscription Purchase',0,'32329LOVECHAT'),(179,'NO_BILLING_REQUIRED',1,'698804469585044',1,'254735594326',NULL,0.00,3,0,0,362,'32329',NULL,0,'2015-08-30 20:54:42',3,NULL,NULL,'Content Purchase',0,'32329_JOBS'),(192,'NO_BILLING_REQUIRED',1,'698903128222970',1,'254735594326',NULL,0.00,3,0,0,442,'32329',NULL,0,'2015-08-30 20:56:21',3,NULL,NULL,'Content Purchase',0,'32329_MAPENZI'),(207,'NO_BILLING_REQUIRED',1,'699060599540452',1,'254735594326',NULL,0.00,3,0,0,442,'32329',NULL,0,'2015-08-30 20:58:58',3,NULL,NULL,'Content Purchase',0,'32329_MAPENZI'),(222,'NO_BILLING_REQUIRED',1,'699331390211070',1,'254735594326',NULL,0.00,3,0,0,442,'32329',NULL,0,'2015-08-30 21:03:29',3,NULL,NULL,'Content Purchase',0,'32329_MAPENZI'),(236,'NO_BILLING_REQUIRED',1,'702269116671059',1,'254735594326',NULL,0.00,3,0,0,442,'32329',NULL,0,'2015-08-30 21:52:27',3,NULL,NULL,'Content Purchase',0,'32329_MAPENZI'),(250,'NO_BILLING_REQUIRED',1,'702683491896543',1,'254735594326',NULL,0.00,3,0,0,442,'32329',NULL,0,'2015-08-30 21:59:21',3,NULL,NULL,'Content Purchase',0,'32329_MAPENZI'),(264,'NO_BILLING_REQUIRED',1,'703019746208547',1,'254735594326',NULL,0.00,3,0,0,442,'32329',NULL,0,'2015-08-30 22:04:57',3,NULL,NULL,'Content Purchase',0,'32329_MAPENZI'),(302,'NO_BILLING_REQUIRED',1,'708619741240721',1,'254735594326',NULL,0.00,3,0,0,442,'32329',NULL,0,'2015-08-30 23:38:17',3,NULL,NULL,'Content Purchase',0,'32329_MAPENZI'),(346,'NO_BILLING_REQUIRED',1,'710744827200832',1,'254735594326',NULL,0.00,3,0,0,442,'32329',NULL,0,'2015-08-31 00:13:42',3,NULL,NULL,'Content Purchase',0,'32329_MAPENZI'),(374,'NO_BILLING_REQUIRED',1,'711656470602338',1,'254735594326',NULL,0.00,3,0,0,369,'32329',NULL,0,'2015-08-31 00:28:54',3,NULL,NULL,'Content Purchase',0,'32329_JOBS'),(387,'NO_BILLING_REQUIRED',1,'711823195393111',1,'254735594326',NULL,0.00,3,0,0,369,'32329',NULL,0,'2015-08-31 00:31:41',3,NULL,NULL,'Content Purchase',0,'32329_JOBS'),(400,'NO_BILLING_REQUIRED',1,'711885348402667',1,'254735594326',NULL,0.00,3,0,0,369,'32329',NULL,0,'2015-08-31 00:32:43',3,NULL,NULL,'Content Purchase',0,'32329_JOBS'),(413,'NO_BILLING_REQUIRED',1,'712059468000397',1,'254735594326',NULL,0.00,3,0,0,369,'32329',NULL,0,'2015-08-31 00:35:37',3,NULL,NULL,'Content Purchase',0,'32329_JOBS');
/*!40000 ALTER TABLE `outgoing_sms` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `profile_configs`
--

DROP TABLE IF EXISTS `profile_configs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `profile_configs` (
  `id` bigint(20) NOT NULL,
  `data_type` varchar(255) DEFAULT NULL,
  `effectiveDate` datetime NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `value` varchar(255) DEFAULT NULL,
  `profile_id_fk` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `proflconfidx` (`effectiveDate`,`name`,`profile_id_fk`),
  KEY `FK_43q60e2injvq051r096x484mb` (`profile_id_fk`),
  CONSTRAINT `FK_43q60e2injvq051r096x484mb` FOREIGN KEY (`profile_id_fk`) REFERENCES `sender_profiles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `profile_configs`
--

LOCK TABLES `profile_configs` WRITE;
/*!40000 ALTER TABLE `profile_configs` DISABLE KEYS */;
INSERT INTO `profile_configs` VALUES (79548830,'string','2015-08-16 00:00:00','http_protocol','http',80452253),(79554657,'string','2015-08-16 00:00:00','senderimpl','com.pixelandtag.smssenders.PlainHttpSender',80452253),(79561972,'string','2015-08-16 00:00:00','http_base_url','http://196.202.219.252:7061/1/smsmessaging/outbound/tel%3A%2B${msisdn}/requests',80452253),(79575990,'string','2015-08-16 00:00:00','http_shortcode_param_name','senderaddress',80452253),(79582482,'string','2015-08-16 00:00:00','http_msisdn_param_name','msisdn',80452253),(79582484,'string','2015-08-16 00:00:00','http_sms_msg_param_name','message',80452253),(79582486,'string','2015-08-16 00:00:00','http_useheader','yes',80452253),(79582492,'string','2015-08-16 00:00:00','http_header_auth_has_username_and_password','yes',80452253),(79590924,'string','2015-08-16 00:00:00','http_haspayload','yes',80452253),(79604829,'string','2015-08-16 00:00:00','http_is_restful','yes',80452253),(79604830,'string','2015-08-16 00:00:00','http_header_auth_username_param_name','spId',80452253),(79604832,'string','2015-08-16 00:00:00','http_header_auth_password_param_name','spPassword',80452253),(79604836,'string','2015-08-16 00:00:00','http_header_auth_password_encryptionmode','basicmd5',80452253),(79604837,'string','2015-08-16 00:00:00','http_header_auth_method_param_name','Basic',80452253),(79604840,'string','2015-08-16 00:00:00','http_header_auth_hasmultiple_kv_pairs','yes',80452253),(79604841,'string','2015-08-16 00:00:00','http_header_auth_param_spId','36',80452253),(79604842,'string','2015-08-16 00:00:00','http_header_auth_param_timeStamp','20150105152020',80452253),(79604851,'string','2015-08-16 00:00:00','http_header_auth_param_serviceId','34',80452253),(79604855,'string','2015-08-16 00:00:00','http_headerauth_param_name','Authorization',80452253),(79640684,'string','2015-08-16 00:00:00','http_payload_template_name','oneapi_json_template',80452253),(79761894,'string','2015-08-16 00:00:00','spId','36',80452253),(79761896,'string','2015-08-16 00:00:00','spPassword','111111',80452253),(79807029,'string','2015-08-16 00:00:00','http_rest_path_param_msisdn','msisdn',80452253),(89057514,'string','2015-08-16 00:00:00','http_protocol','http',89016855),(89057515,'string','2015-08-16 00:00:00','senderimpl','com.pixelandtag.smssenders.PlainHttpSender',89016855),(89057516,'string','2015-08-16 00:00:00','http_useheader','no',89016855),(89057517,'string','2015-08-16 00:00:00','http_base_url','http://41.223.58.157:56000/Bharti?sms=${sms}&login=${login}&pass=${pass}&msisdn=${msisdn}&type=text&src=${shortcode}',89016855),(89057518,'string','2015-08-16 00:00:00','http_haspayload','no',89016855),(89057519,'string','2015-08-16 00:00:00','http_is_restful','yes',89016855),(89057520,'string','2015-08-16 00:00:00','http_shortcode_param_name','shortcode',89016855),(89057521,'string','2015-08-16 00:00:00','http_msisdn_param_name','msisdn',89016855),(89057609,'string','2015-08-16 00:00:00','http_sms_msg_param_name','sms',89016855),(89057610,'string','2015-08-16 00:00:00','http_header_auth_hasmultiple_kv_pairs','no',89016855),(89057611,'string','2015-08-16 00:00:00','http_rest_path_param_sms','sms',89016855),(89057612,'string','2015-08-16 00:00:00','http_rest_path_param_msisdn','msisdn',89016855),(89057613,'string','2015-08-16 00:00:00','http_rest_path_param_shortcode','shortcode',89016855),(89057614,'string','2015-08-16 00:00:00','http_rest_path_param_login','content36',89016855),(89057616,'string','2015-08-16 00:00:00','shortcode','32329',89016855),(89057617,'string','2015-08-16 00:00:00','http_rest_path_param_pass','BvZqN8wc',89016855),(89057618,'string','2015-08-16 00:00:00','receiver_msisdn_param_name','msisdn',89016855),(89057619,'string','2015-08-16 00:00:00','receiver_shortcode_param_name','shortCode',89016855),(89057620,'string','2015-08-16 00:00:00','receiver_sms_param_name','text',89016855),(89057621,'string','2015-08-16 00:00:00','receiver_has_payload','no',89016855),(89057622,'string','2015-08-16 00:00:00','receiver_expected_contenttype','plaintext',89016855),(89057623,'string','2015-08-16 00:00:00','mo_medium_source','sms',89016855),(89057624,'string','2015-08-16 00:00:00','receiver_opco_txid_param_name','',89016855);
/*!40000 ALTER TABLE `profile_configs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `profile_templates`
--

DROP TABLE IF EXISTS `profile_templates`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `profile_templates` (
  `id` bigint(20) NOT NULL,
  `effectiveDate` datetime NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `value` varchar(10000) DEFAULT NULL,
  `profile_id_fk` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `opctpltfidx` (`effectiveDate`,`name`,`profile_id_fk`),
  KEY `FK_mygjucnwre7d8qbqx0gf52i4d` (`profile_id_fk`),
  CONSTRAINT `FK_mygjucnwre7d8qbqx0gf52i4d` FOREIGN KEY (`profile_id_fk`) REFERENCES `sender_profiles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `profile_templates`
--

LOCK TABLES `profile_templates` WRITE;
/*!40000 ALTER TABLE `profile_templates` DISABLE KEYS */;
INSERT INTO `profile_templates` VALUES (2,'2015-08-16 00:00:00','oneapi_json_template','PAYLOAD','{\"validity_period\":\"991201230029000+\",\"address\":[\"tel:${msisdn}\"],\"senderAddress\":\"${senderaddress}\",\"message\":\"${message}\",\"notifyURL\":\"http://test/test\",\"senderName\":\"${senderaddress}\"}',80452253);
/*!40000 ALTER TABLE `profile_templates` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `role` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role`
--

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
/*!40000 ALTER TABLE `role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sender_profiles`
--

DROP TABLE IF EXISTS `sender_profiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sender_profiles` (
  `id` bigint(20) NOT NULL,
  `active` tinyint(1) NOT NULL,
  `effectiveDate` datetime NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_585agj4ac3pdbjjaii9jto7jt` (`name`),
  KEY `opcconfidx` (`effectiveDate`),
  KEY `profidx` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sender_profiles`
--

LOCK TABLES `sender_profiles` WRITE;
/*!40000 ALTER TABLE `sender_profiles` DISABLE KEYS */;
INSERT INTO `sender_profiles` VALUES (80452253,1,'2015-08-16 00:00:00','Orange\'s One API'),(89016855,1,'2015-08-16 00:00:00','Airtel HTTP Sender');
/*!40000 ALTER TABLE `sender_profiles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sender_receiver_profile`
--

DROP TABLE IF EXISTS `sender_receiver_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sender_receiver_profile` (
  `id` bigint(20) NOT NULL,
  `active` tinyint(1) NOT NULL,
  `effectiveDate` datetime NOT NULL,
  `name` varchar(255) NOT NULL,
  `type` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_l7lxn56ibvt4bao8rwbnoat8m` (`name`),
  KEY `opccoefdidx` (`effectiveDate`),
  KEY `profidx` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sender_receiver_profile`
--

LOCK TABLES `sender_receiver_profile` WRITE;
/*!40000 ALTER TABLE `sender_receiver_profile` DISABLE KEYS */;
INSERT INTO `sender_receiver_profile` VALUES (80452253,1,'2015-08-16 00:00:00','Orange\'s One API','SENDER'),(89016855,1,'2015-08-16 00:00:00','Airtel HTTP Sender','SENDER');
/*!40000 ALTER TABLE `sender_receiver_profile` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sms_service`
--

DROP TABLE IF EXISTS `sms_service`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sms_service` (
  `id` bigint(20) NOT NULL,
  `cmd` varchar(255) DEFAULT NULL,
  `enabled` tinyint(1) DEFAULT NULL,
  `event_type` varchar(255) DEFAULT NULL,
  `forward_url` varchar(255) DEFAULT NULL,
  `isjustAPricePoint` tinyint(1) DEFAULT NULL,
  `mo_processorFK` bigint(20) DEFAULT NULL,
  `price` double DEFAULT NULL,
  `price_point_keyword` varchar(255) DEFAULT NULL,
  `push_unique` tinyint(1) DEFAULT NULL,
  `service_description` varchar(255) DEFAULT NULL,
  `service_name` varchar(255) DEFAULT NULL,
  `split_mt` tinyint(1) DEFAULT NULL,
  `subscriptionText` varchar(255) DEFAULT NULL,
  `subscription_length` bigint(20) DEFAULT NULL,
  `subscription_length_time_unit` varchar(255) DEFAULT NULL,
  `tailText_notsubscribed` varchar(255) DEFAULT NULL,
  `tailText_subscribed` varchar(255) DEFAULT NULL,
  `unsubscriptionText` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_e19t5qj2ye2h7fqi3a1hqq01d` (`mo_processorFK`),
  CONSTRAINT `FK_e19t5qj2ye2h7fqi3a1hqq01d` FOREIGN KEY (`mo_processorFK`) REFERENCES `mo_processors` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sms_service`
--

LOCK TABLES `sms_service` WRITE;
/*!40000 ALTER TABLE `sms_service` DISABLE KEYS */;
INSERT INTO `sms_service` VALUES (362,'DEFAULT',1,'Content Purchase','NULL',0,14,0,'32329_JOBS',0,'Chat bundle','Chat bundle',0,'You\'re now subscribed to \"<SERVICE_NAME>\". Daily SMS Cost \"<SMS_SUBSCRIPTION_PRICE> /- @sms. To unsubscribe  reply \"STOP <KEYWORD>\".\"',1,'WEEK','Thank you for your interest in \"<KEYWORD>\".   Cost is <PRICE>/- @ sms','To unsubscribe, reply \"STOP\"','You\'ve been unsubscribed'),(363,'JOBS',1,'Subscription Purchase','NULL',0,11,0,'32329_JOBS',0,'Jobs. by default','Jobs. by default',0,'You\'re now subscribed to \"<SERVICE_NAME>\". Daily SMS Cost \"<SMS_SUBSCRIPTION_PRICE> /- @sms. To unsubscribe  reply \"STOP <KEYWORD>\".\"',1,'WEEK','Thank you for your interest in \"<KEYWORD>\".   Cost is <PRICE>/- @ sms','To unsubscribe, reply \"STOP\"','You\'ve been unsubscribed from JOBS. To subscribe for more interesting content, reply with FUN. <PRICE>/- @ sms'),(364,'JOKE',1,'Content Purchase','NULL',0,12,5,'32329_JOBS',1,'NULL','Joke',0,'You\'re now subscribed to \"<SERVICE_NAME>\". Daily SMS Cost \"<SMS_SUBSCRIPTION_PRICE> /- @sms. To unsubscribe  reply \"STOP <KEYWORD>\".\"',1,'WEEK','To confirm your subscription, reply with \"BUY\"','Cost 5/- @SMS. To unsubscribe reply \"STOP JOKE\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(365,'MORE',1,'Content Purchase','NULL',0,13,0,'32329_JOBS',0,'NULL','Top menu',0,'Congratulations! You are now subscribed to this service.',1,'WEEK','To confirm your subscription, reply with \"BUY\"','To unsubscribe, reply \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(366,'job_biz_support',1,'Content Purchase','NULL',0,12,5,'32329_JOBS',1,'NULL','Business support content',0,'Congratulations! You are now subscribed to this service.',1,'WEEK','To confirm your subscription, reply with \"BUY\"','To unsubscribe, reply \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(367,'1',1,'Content Purchase','NULL',0,13,0,'32329_JOBS',0,'NULL','Menu response for 1',0,'Congratulations! You are now subscribed to this service.',1,'WEEK','To confirm your subscription, reply with \"BUY\"','To unsubscribe, reply \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(368,'1',1,'Content Purchase','NULL',0,11,0,'32329_JOBS',0,'NULL','NULL',0,'Congratulations! You are now subscribed to this service.',1,'WEEK','To confirm your subscription, reply with \"BUY\"','To unsubscribe, reply \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(369,'2',1,'Content Purchase','NULL',0,11,0,'32329_JOBS',0,'NULL','NULL',0,'Congratulations! You are now subscribed to this service.',1,'WEEK','To confirm your subscription, reply with \"BUY\"','To unsubscribe, reply \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(370,'3',1,'Content Purchase','NULL',0,11,0,'32329_JOBS',0,'NULL','NULL',0,'Congratulations! You are now subscribed to this service.',1,'WEEK','To confirm your subscription, reply with \"BUY\"','To unsubscribe, reply \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(371,'4',1,'Content Purchase','NULL',0,11,0,'32329_JOBS',0,'NULL','NULL',0,'Congratulations! You are now subscribed to this service.',1,'WEEK','To confirm your subscription, reply with \"BUY\"','To unsubscribe, reply \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(372,'5',1,'Content Purchase','NULL',0,11,0,'32329_JOBS',0,'NULL','NULL',0,'Congratulations! You are now subscribed to this service.',1,'WEEK','To confirm your subscription, reply with \"BUY\"','To unsubscribe, reply \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(373,'6',1,'Content Purchase','NULL',0,11,0,'32329_JOBS',0,'NULL','NULL',0,'Congratulations! You are now subscribed to this service.',1,'WEEK','To confirm your subscription, reply with \"BUY\"','To unsubscribe, reply \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(374,'7',1,'Content Purchase','NULL',0,11,0,'32329_JOBS',0,'NULL','NULL',0,'Congratulations! You are now subscribed to this service.',1,'WEEK','To confirm your subscription, reply with \"BUY\"','To unsubscribe, reply \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(375,'8',1,'Content Purchase','NULL',0,11,0,'32329_JOBS',0,'NULL','NULL',0,'Congratulations! You are now subscribed to this service.',1,'WEEK','To confirm your subscription, reply with \"BUY\"','To unsubscribe, reply \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(376,'9',1,'Content Purchase','NULL',0,11,0,'32329_JOBS',0,'NULL','NULL',0,'Congratulations! You are now subscribed to this service.',1,'WEEK','To confirm your subscription, reply with \"BUY\"','To unsubscribe, reply \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(377,'10',1,'Content Purchase','NULL',0,11,0,'32329_JOBS',0,'NULL','NULL',0,'Congratulations! You are now subscribed to this service.',1,'WEEK','To confirm your subscription, reply with \"BUY\"','To unsubscribe, reply \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(378,'11',1,'Content Purchase','NULL',0,11,0,'32329_JOBS',0,'NULL','NULL',0,'Congratulations! You are now subscribed to this service.',1,'WEEK','To confirm your subscription, reply with \"BUY\"','To unsubscribe, reply \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(379,'12',1,'Content Purchase','NULL',0,11,0,'32329_JOBS',0,'NULL','NULL',0,'Congratulations! You are now subscribed to this service.',1,'WEEK','To confirm your subscription, reply with \"BUY\"','To unsubscribe, reply \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(380,'13',1,'Content Purchase','NULL',0,11,0,'32329_JOBS',0,'NULL','NULL',0,'Congratulations! You are now subscribed to this service.',1,'WEEK','To confirm your subscription, reply with \"BUY\"','To unsubscribe, reply \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(381,'14',1,'Content Purchase','NULL',0,11,0,'32329_JOBS',0,'NULL','NULL',0,'Congratulations! You are now subscribed to this service.',1,'WEEK','To confirm your subscription, reply with \"BUY\"','To unsubscribe, reply \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(382,'15',1,'Content Purchase','NULL',0,11,0,'32329_JOBS',0,'NULL','NULL',0,'Congratulations! You are now subscribed to this service.',1,'WEEK','To confirm your subscription, reply with \"BUY\"','To unsubscribe, reply \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(383,'16',1,'Content Purchase','NULL',0,11,0,'32329_JOBS',0,'NULL','NULL',0,'Congratulations! You are now subscribed to this service.',1,'WEEK','To confirm your subscription, reply with \"BUY\"','To unsubscribe, reply \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(384,'DISABLED__',1,'Content Purchase','NULL',0,11,0,'32329_JOBS',0,'NULL','NULL',0,'Congratulations! You are now subscribed to this service.',1,'WEEK','To confirm your subscription, reply with \"BUY\"','To unsubscribe, reply \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(385,'DISABLED__',1,'Content Purchase','NULL',0,11,0,'32329_JOBS',0,'NULL','NULL',0,'Congratulations! You are now subscribed to this service.',1,'WEEK','To confirm your subscription, reply with \"BUY\"','To unsubscribe, reply \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(386,'MENU',1,'Content Purchase','NULL',0,11,0,'32329_JOBS',0,'NULL','The top menu. English',0,'Congratulations! You are now subscribed to this service.',1,'WEEK','To confirm your subscription, reply with \"BUY\"','To unsubscribe, reply \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(387,'ORODHA',1,'Content Purchase','NULL',0,11,0,'32329_JOBS',0,'NULL','The top menu. Swahili',0,'Congratulations! You are now subscribed to this service.',1,'WEEK','To confirm your subscription, reply with \"BUY\"','To unsubscribe, reply \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(388,'BUY',1,'Content Purchase','NULL',0,11,0,'32329_JOBS',0,'NULL','Service subscription confirmation keyword. En',0,'Congratulations! You are now subscribed to this service.',1,'WEEK','To confirm your subscription, reply with \"BUY\"','To unsubscribe, reply \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(389,'NUNUA',1,'Content Purchase','NULL',0,11,0,'32329_JOBS',0,'NULL','Service subscription confirmation. Swahili',0,'Congratulations! You are now subscribed to this service.',1,'WEEK','To confirm your subscription, reply with \"BUY\"','To unsubscribe, reply \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(390,'ON',1,'Content Purchase','NULL',0,11,0,'32329_JOBS',0,'NULL','Service subscription confirmation keyword. En',0,'Congratulations! You are now subscribed to this service.',1,'WEEK','To confirm your subscription, reply with \"BUY\"','To unsubscribe, reply \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(391,'WEZESHA',1,'Content Purchase','NULL',0,11,0,'32329_JOBS',0,'NULL','Service subscription confirmation. Swahili',0,'Congratulations! You are now subscribed to this service.',1,'WEEK','To confirm your subscription, reply with \"BUY\"','To unsubscribe, reply \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(392,'STOP',1,'Content Purchase','NULL',0,11,0,'32329_JOBS',0,'NULL','Unsubscription keyword',0,'Congratulations! You are now subscribed to this service.',1,'WEEK','To confirm your subscription, reply with \"BUY\"','To unsubscribe, reply \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(393,'ROMANCE',1,'Subscription Purchase','NULL',0,12,5,'32329_JOBS',0,'NULL','Romance',0,'You\'re now subscribed to \"<SERVICE_NAME>\". Daily SMS Cost \"<SMS_SUBSCRIPTION_PRICE> /- @sms. To unsubscribe reply \"STOP <KEYWORD>\".\"',1,'WEEK','To confirm your subscription, reply with \"BUY\"','Cost 5/- @SMS. To unsubscribe reply \"STOP ROMANCE\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(394,'MCHONG',1,'Subscription Purchase','NULL',0,12,5,'32329_JOBS',0,'NULL','Mchongwano',0,'You\'re now subscribed to \"<SERVICE_NAME>\". Daily SMS Cost \"<SMS_SUBSCRIPTION_PRICE> /- @sms. To unsubscribe reply \"STOP <KEYWORD>\".\"',1,'WEEK','To confirm your subscription, reply with \"BUY\"','Cost 5/- @SMS. To unsubscribe reply \"STOP MCHONG\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(395,'ISLAMQ',1,'Subscription Purchase','NULL',0,12,5,'32329_JOBS',0,'NULL','Islamic quotes',0,'You\'re now subscribed to \"<SERVICE_NAME>\". Daily SMS Cost \"<SMS_SUBSCRIPTION_PRICE> /- @sms. To unsubscribe reply \"STOP <KEYWORD>\".\"',1,'WEEK','To confirm your subscription, reply with \"BUY\"','Cost 5/- @SMS. To unsubscribe reply \"STOP ISLAMQ\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(396,'BIBLEQ',1,'Subscription Purchase','NULL',0,12,5,'32329_JOBS',0,'NULL','Bible quotes',0,'You\'re now subscribed to \"<SERVICE_NAME>\". Daily SMS Cost \"<SMS_SUBSCRIPTION_PRICE> /- @sms. To unsubscribe reply \"STOP <KEYWORD>\".\"',1,'WEEK','To confirm your subscription, reply with \"BUY\"','Cost 5/- @SMS. To unsubscribe reply \"STOP BIBLEQ\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(397,'CHRISTMASQ',1,'Subscription Purchase','NULL',0,12,5,'32329_JOBS',0,'NULL','Christmas quotes',0,'You\'re now subscribed to \"<SERVICE_NAME>\". Daily SMS Cost \"<SMS_SUBSCRIPTION_PRICE> /- @sms. To unsubscribe reply \"STOP <KEYWORD>\".\"',1,'WEEK','To confirm your subscription, reply with \"BUY\"','Cost 5/- @SMS. To unsubscribe reply \"STOP CHRISTMASQ\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(398,'LOVEQ',1,'Subscription Purchase','NULL',0,12,5,'32329_JOBS',0,'NULL','Love quotes',0,'You\'re now subscribed to \"<SERVICE_NAME>\". Daily SMS Cost \"<SMS_SUBSCRIPTION_PRICE> /- @sms. To unsubscribe reply \"STOP <KEYWORD>\".\"',1,'WEEK','To confirm your subscription, reply with \"BUY\"','Cost 5/- @SMS. To unsubscribe reply \"STOP LOVEQ\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(399,'FUNFACTS',1,'Subscription Purchase','NULL',0,12,5,'32329_JOBS',0,'NULL','Fun Facts',0,'You\'re now subscribed to \"<SERVICE_NAME>\". Daily SMS Cost \"<SMS_SUBSCRIPTION_PRICE> /- @sms. To unsubscribe reply \"STOP <KEYWORD>\".\"',1,'WEEK','To confirm your subscription, reply with \"BUY\"','Cost 5/- @SMS. To unsubscribe reply \"STOP FUNFACTS\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(400,'VITENDA',1,'Subscription Purchase','NULL',0,12,5,'32329_JOBS',0,'NULL','Vitendawili',0,'You\'re now subscribed to \"<SERVICE_NAME>\". Daily SMS Cost \"<SMS_SUBSCRIPTION_PRICE> /- @sms. To unsubscribe reply \"STOP <KEYWORD>\".\"',1,'WEEK','To confirm your subscription, reply with \"BUY\"','Cost 5/- @SMS. To unsubscribe reply \"STOP VITENDA\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(401,'JOB_ACCOUNTS',1,'Subscription Purchase','NULL',0,12,5,'32329_JOBS',0,'NULL','Jobs Accounts',0,'You\'re now subscribed to \"<SERVICE_NAME>\". Daily SMS Cost \"<SMS_SUBSCRIPTION_PRICE> /- @sms. To unsubscribe reply \"STOP <KEYWORD>\".\"',1,'WEEK','To confirm your subscription, reply with \"BUY\"','Cost 5/- @SMS. To unsubscribe reply \"STOP JOB_ACCOUNTS\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(402,'JOB_ADMIN',1,'Subscription Purchase','NULL',0,12,5,'32329_JOBS',0,'NULL','Jobs Admin',0,'You\'re now subscribed to \"<SERVICE_NAME>\". Daily SMS Cost \"<SMS_SUBSCRIPTION_PRICE> /- @sms. To unsubscribe reply \"STOP <KEYWORD>\".\"',1,'WEEK','To confirm your subscription, reply with \"BUY\"','Cost 5/- @SMS. To unsubscribe reply \"STOP JOB_ADMIN\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(403,'JOB_BIZ_SUPP',1,'Subscription Purchase','NULL',0,12,5,'32329_JOBS',0,'NULL','Jobs biz support',0,'You\'re now subscribed to \"<SERVICE_NAME>\". Daily SMS Cost \"<SMS_SUBSCRIPTION_PRICE> /- @sms. To unsubscribe reply \"STOP <KEYWORD>\".\"',1,'WEEK','To confirm your subscription, reply with \"BUY\"','Cost 5/- @SMS. To unsubscribe reply \"STOP JOB_BIZ_SUPP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(404,'JOB_FINANCE',1,'Subscription Purchase','NULL',0,12,5,'32329_JOBS',0,'NULL','Jobs Finance',0,'You\'re now subscribed to \"<SERVICE_NAME>\". Daily SMS Cost \"<SMS_SUBSCRIPTION_PRICE> /- @sms. To unsubscribe reply \"STOP <KEYWORD>\".\"',1,'WEEK','To confirm your subscription, reply with \"BUY\"','Cost 5/- @SMS. To unsubscribe reply \"STOP JOB_FINANCE\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(405,'JOB_SALEMARKET',1,'Subscription Purchase','NULL',0,12,5,'32329_JOBS',0,'NULL','Jobs Sales & Marketing',0,'You\'re now subscribed to \"<SERVICE_NAME>\". Daily SMS Cost \"<SMS_SUBSCRIPTION_PRICE> /- @sms. To unsubscribe reply \"STOP <KEYWORD>\".\"',1,'WEEK','To confirm your subscription, reply with \"BUY\"','Cost 5/- @SMS. To unsubscribe reply \"STOP JOB_SALEMARKET\" \"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(406,'PROPERTY_RENTAL',1,'Content Purchase','NULL',0,12,5,'32329_JOBS',0,'NULL','Property Rental',0,'You\'re now subscribed to \"<SERVICE_NAME>\". Daily SMS Cost \"<SMS_SUBSCRIPTION_PRICE> /- @sms. To unsubscribe reply \"STOP <KEYWORD>\".\"',1,'WEEK','To confirm your subscription, reply with \"BUY\"','Cost 5/- @SMS. To unsubscribe reply \"STOP PROPERTY_RENTAL\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(407,'PROPERTY_SALE',1,'Content Purchase','NULL',0,12,5,'32329_JOBS',0,'NULL','Property 4 Sale',0,'You\'re now subscribed to \"<SERVICE_NAME>\". Daily SMS Cost \"<SMS_SUBSCRIPTION_PRICE> /- @sms. To unsubscribe reply \"STOP <KEYWORD>\".\"',1,'WEEK','To confirm your subscription, reply with \"BUY\"','Cost 5/- @SMS. To unsubscribe reply \"STOP PROPERTY_SALE\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(408,'VEHICLES',1,'Content Purchase','NULL',0,12,5,'32329_JOBS',0,'NULL','Vehicles 4 Sale',0,'You\'re now subscribed to \"<SERVICE_NAME>\". Daily SMS Cost \"<SMS_SUBSCRIPTION_PRICE> /- @sms. To unsubscribe reply \"STOP <KEYWORD>\".\"',1,'WEEK','To confirm your subscription, reply with \"BUY\"','Cost 5/- @SMS. To unsubscribe reply \"STOP VEHICLES\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(409,'BIASHARA4SALE',1,'Content Purchase','NULL',0,12,5,'32329_JOBS',0,'NULL','Biashara 4 sale',0,'You\'re now subscribed to \"<SERVICE_NAME>\". Daily SMS Cost \"<SMS_SUBSCRIPTION_PRICE> /- @sms. To unsubscribe reply \"STOP <KEYWORD>\".\"',1,'WEEK','To confirm your subscription, reply with \"BUY\"','Cost 5/- @SMS. To unsubscribe reply \"STOP BIASHARA4SALE\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(410,'DATE',1,'Content Purchase','NULL',0,14,0,'32329_JOBS',0,'Dating Service','Dating service',0,'You\'re now subscribed to \"<SERVICE_NAME>\". Daily SMS Cost \"<SMS_SUBSCRIPTION_PRICE> /- @sms. To unsubscribe reply \"STOP <KEYWORD>\".\"',1,'WEEK','To confirm your subscription, reply with \"BUY\"','Cost 5/- @SMS. To unsubscribe reply \"STOP DATE\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(411,'JOB_GRADTRAINE',1,'Content Purchase','NULL',0,12,5,'32329_JOBS',0,'NULL','Graduate Trainee  Jobs',0,'You\'re now subscribed to \"<SERVICE_NAME>\". Daily SMS Cost \"<SMS_SUBSCRIPTION_PRICE> /- @sms. To unsubscribe reply \"STOP <KEYWORD>\".\"',1,'WEEK','To confirm your subscription, reply with \"BUY\"','Cost 5/- @SMS. To unsubscribe reply \"STOP JOB_GRADTRAINE\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(412,'JOB_HOSPITALITY',1,'Content Purchase','NULL',0,12,5,'32329_JOBS',0,'NULL','Hospitality Jobs',0,'You\'re now subscribed to \"<SERVICE_NAME>\". Daily SMS Cost \"<SMS_SUBSCRIPTION_PRICE> /- @sms. To unsubscribe reply \"STOP <KEYWORD>\".\"',1,'WEEK','To confirm your subscription, reply with \"BUY\"','Cost 5/- @SMS. To unsubscribe reply \"STOP JOB_HOSPITALITY\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(413,'JOB_IT',1,'Content Purchase','NULL',0,12,5,'32329_JOBS',0,'NULL','IT Jobs',0,'You\'re now subscribed to \"<SERVICE_NAME>\". Daily SMS Cost \"<SMS_SUBSCRIPTION_PRICE> /- @sms. To unsubscribe reply \"STOP <KEYWORD>\".\"',1,'WEEK','To confirm your subscription, reply with \"BUY\"','Cost 5/- @SMS. To unsubscribe reply \"STOP JOB_IT\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(414,'JOB_LEGAL',1,'Content Purchase','NULL',0,12,5,'32329_JOBS',0,'NULL','Legal Jobs',0,'You\'re now subscribed to \"<SERVICE_NAME>\". Daily SMS Cost \"<SMS_SUBSCRIPTION_PRICE> /- @sms. To unsubscribe reply \"STOP <KEYWORD>\".\"',1,'WEEK','To confirm your subscription, reply with \"BUY\"','Cost 5/- @SMS. To unsubscribe reply \"STOP JOB_LEGAL\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(415,'JOB_MEDICAL',1,'Content Purchase','NULL',0,12,5,'32329_JOBS',0,'NULL','Medical Jobs',0,'You\'re now subscribed to \"<SERVICE_NAME>\". Daily SMS Cost \"<SMS_SUBSCRIPTION_PRICE> /- @sms. To unsubscribe reply \"STOP <KEYWORD>\".\"',1,'WEEK','To confirm your subscription, reply with \"BUY\"','Cost 5/- @SMS. To unsubscribe reply \"STOP JOB_MEDICAL\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(416,'JOB_ENGINEER',1,'Content Purchase','NULL',0,12,5,'32329_JOBS',0,'NULL','Engineering Jobs',0,'You\'re now subscribed to \"<SERVICE_NAME>\". Daily SMS Cost \"<SMS_SUBSCRIPTION_PRICE> /- @sms. To unsubscribe reply \"STOP <KEYWORD>\".\"',1,'WEEK','To confirm your subscription, reply with \"BUY\"','Cost 5/- @SMS. To unsubscribe reply \"STOP JOB_ENGINEER\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(417,'HINDU_QUOTES',1,'Content Purchase','NULL',0,12,5,'32329_JOBS',0,'NULL','Hindu Quotes',0,'You\'re now subscribed to \"<SERVICE_NAME>\". Daily SMS Cost \"<SMS_SUBSCRIPTION_PRICE> /- @sms. To unsubscribe reply \"STOP <KEYWORD>\".\"',1,'WEEK','To confirm your subscription, reply with \"BUY\"','Cost 5/- @SMS. To unsubscribe reply \"STOP HINDU_QUOTES\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(426,'ON1',1,'Content Purchase','NULL',0,11,0,'32329_JOBS',0,'NULL','NULL',0,'Congratulations! You are now subscribed to this service.',1,'WEEK','To confirm your subscription, reply with \"BUY\"','To unsubscribe, please reply with \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(427,'ON2',1,'Content Purchase','NULL',0,11,0,'32329_JOBS',0,'NULL','NULL',0,'Congratulations! You are now subscribed to this service.',1,'WEEK','To confirm your subscription, reply with \"BUY\"','To unsubscribe, please reply with \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(428,'ON3',1,'Content Purchase','NULL',0,11,0,'32329_JOBS',0,'NULL','NULL',0,'Congratulations! You are now subscribed to this service.',1,'WEEK','To confirm your subscription, reply with \"BUY\"','To unsubscribe, please reply with \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(429,'ON4',1,'Content Purchase','NULL',0,11,0,'32329_JOBS',0,'NULL','NULL',0,'Congratulations! You are now subscribed to this service.',1,'WEEK','To confirm your subscription, reply with \"BUY\"','To unsubscribe, please reply with \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(430,'ON5',1,'Content Purchase','NULL',0,11,0,'32329_JOBS',0,'NULL','NULL',0,'Congratulations! You are now subscribed to this service.',1,'WEEK','To confirm your subscription, reply with \"BUY\"','To unsubscribe, please reply with \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(431,'ON6',1,'Content Purchase','NULL',0,11,0,'32329_JOBS',0,'NULL','NULL',0,'Congratulations! You are now subscribed to this service.',1,'WEEK','To confirm your subscription, reply with \"BUY\"','To unsubscribe, please reply with \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(432,'0',1,'Content Purchase','NULL',0,11,0,'32329_JOBS',0,'NULL','NULL',0,'Congratulations! You are now subscribed to this service.',1,'WEEK','To confirm your subscription, reply with \"BUY\"','To unsubscribe, please reply with \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(433,'ON7',1,'Content Purchase','NULL',0,11,0,'32329_JOBS',0,'NULL','Service subscription confirmation keyword. En',0,'Congratulations! You are now subscribed to this service.',1,'WEEK','To confirm your subscription, reply with \"BUY\"','To unsubscribe, please reply with \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(434,'ON8',1,'Content Purchase','NULL',0,11,0,'32329_JOBS',0,'NULL','Service subscription confirmation keyword. En',0,'Congratulations! You are now subscribed to this service.',1,'WEEK','To confirm your subscription, reply with \"BUY\"','To unsubscribe, please reply with \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(435,'ON9',1,'Content Purchase','NULL',0,11,0,'32329_JOBS',0,'NULL','Service subscription confirmation keyword. En',0,'Congratulations! You are now subscribed to this service.',1,'WEEK','To confirm your subscription, reply with \"BUY\"','To unsubscribe, please reply with \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(436,'ON10',1,'Content Purchase','NULL',0,11,0,'32329_JOBS',0,'NULL','Service subscription confirmation keyword. En',0,'Congratulations! You are now subscribed to this service.',1,'WEEK','To confirm your subscription, reply with \"BUY\"','To unsubscribe, please reply with \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(437,'DEFAULT',1,'Content Purchase','NULL',0,15,0,'32329_JOBS',1,'NULL','NULL',0,'Congratulations! You are now subscribed to this service.',1,'WEEK','To confirm your subscription, reply with \"BUY\"','To unsubscribe, please reply with \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(438,'RENEW',1,'Subscription Purchase','NULL',0,14,5,'32329_MAPENZI',0,'Dating Service','Dating service',0,'Congratulations! You are now subscribed to this service.',1,'DAY','To confirm your subscription, reply with \"BUY\"','To unsubscribe, please reply with \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(439,'BILLING_SERV5',1,'Subscription Purchase','NULL',0,14,5,'32329LOVECHAT',1,'Dating chat & Friend finder','Dating chat & Friend finder',0,'Congratulations! You are now subscribed to this service.',1,'DAY','To confirm your subscription, reply with \"BUY\"','To unsubscribe, please reply with \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(440,'BILLING_SERV15',1,'Subscription Purchase','NULL',0,14,15,'32329LOVECHAT',1,'Dating chat & Friend finder','Dating chat & Friend finder',0,'Congratulations! You are now subscribed to this service.',1,'WEEK','To confirm your subscription, reply with \"BUY\"','To unsubscribe, please reply with \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(441,'BILLING_SERV30',1,'Subscription Purchase','NULL',0,14,30,'32329LOVECHAT',1,'Dating chat & Friend finder','Dating chat & Friend finder',0,'Congratulations! You are now subscribed to this service.',1,'MONTH','To confirm your subscription, reply with \"BUY\"','To unsubscribe, please reply with \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(442,'FIND',1,'Content Purchase','NULL',0,14,0,'32329_MAPENZI',1,'NULL','NULL',0,'Congratulations! You are now subscribed to this service.',1,'DAY','To confirm your subscription, reply with \"BUY\"','To unsubscribe, please reply with \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(443,'TAFUTA',1,'Content Purchase','NULL',0,14,0,'32329_MAPENZI',1,'NULL','NULL',0,'Congratulations! You are now subscribed to this service.',1,'DAY','To confirm your subscription, reply with \"BUY\"','To unsubscribe, please reply with \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(444,'BUNDLES',1,'Content Purchase','NULL',0,14,0,'32329_MAPENZI',1,'Chat bundles','Chat bundles',0,'Congratulations! You are now subscribed to this service.',0,'NULL','To confirm your subscription, reply with \"BUY\"','To unsubscribe, please reply with \"STOP\"','Thank you for having subscribed to our service. Your subscription to this service has now been stopped'),(445,'DEFAULT',1,'Content Purchase','NULL',0,15,0,'32329_JOBS',0,'Mpedigree test','Mpedigree test',0,'',1,'WEEK','','',''),(446,'DEFAULT',1,'Content Purchase','NULL',0,17,0,'32329_JOBS',0,'Mpedigree 1323','Mpedigree 1323',0,'',1,'WEEK','','',''),(447,'LOGIN',1,'Content Purchase','NULL',0,14,0,'32329LOVECHAT',0,'Logout','Logout',0,'',1,'YEAR','','',''),(448,'LOGOUT',1,'Content Purchase','NULL',0,14,0,'32329LOVECHAT',0,'Logout','Logout',0,'',1,'YEAR','','','');
/*!40000 ALTER TABLE `sms_service` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sms_service_metadata`
--

DROP TABLE IF EXISTS `sms_service_metadata`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sms_service_metadata` (
  `id` bigint(20) NOT NULL,
  `meta_field` varchar(255) DEFAULT NULL,
  `meta_value` varchar(255) DEFAULT NULL,
  `sms_service_id_fk` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sms_service_metadata`
--

LOCK TABLES `sms_service_metadata` WRITE;
/*!40000 ALTER TABLE `sms_service_metadata` DISABLE KEYS */;
/*!40000 ALTER TABLE `sms_service_metadata` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `smsmenu_levels`
--

DROP TABLE IF EXISTS `smsmenu_levels`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `smsmenu_levels` (
  `id` bigint(20) NOT NULL,
  `language_id` bigint(20) DEFAULT NULL,
  `menu_id` bigint(20) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `parent_level_id` bigint(20) DEFAULT NULL,
  `serviceid` bigint(20) DEFAULT NULL,
  `ussdTag` varchar(255) DEFAULT NULL,
  `visible` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `ussdtgidx` (`ussdTag`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `smsmenu_levels`
--

LOCK TABLES `smsmenu_levels` WRITE;
/*!40000 ALTER TABLE `smsmenu_levels` DISABLE KEYS */;
INSERT INTO `smsmenu_levels` VALUES (98,0,1,'127',1,-1,'1',NULL),(99,0,1,'127',1,-1,'1',NULL),(100,0,1,'129',1,410,'1',NULL),(120,0,1,'131',1,394,'1',NULL),(121,0,1,'128',1,395,'1',NULL),(122,0,1,'128',1,396,'1',NULL),(123,0,1,'128',1,397,'0',NULL),(124,0,1,'129',1,398,'1',NULL),(125,0,1,'131',1,399,'1',NULL),(126,0,1,'131',1,400,'1',NULL),(127,0,1,'-1',1,-1,'1',NULL),(128,0,1,'-1',1,-1,'1',NULL),(129,0,1,'-1',1,-1,'1',NULL),(130,0,1,'127',1,409,'1',NULL),(131,0,1,'-1',1,-1,'1',NULL),(132,0,1,'98',1,401,'1',NULL),(133,0,1,'98',1,405,'1',NULL),(134,0,1,'98',1,416,'1',NULL),(135,0,1,'98',1,402,'1',NULL),(136,0,1,'98',1,415,'1',NULL),(137,0,1,'98',1,414,'1',NULL),(138,0,1,'98',1,413,'1',NULL),(139,0,1,'98',1,412,'1',NULL),(140,0,1,'98',1,411,'1',NULL),(141,0,1,'127',1,408,'1',NULL),(142,0,1,'99',1,407,'1',NULL),(143,0,1,'99',1,406,'1',NULL),(144,0,1,'98',1,404,'1',NULL),(145,0,1,'98',1,403,'1',NULL),(146,0,1,'131',1,364,'1',NULL),(147,0,1,'128',1,417,'1',NULL),(148,5,1,'151',2,439,'1',0),(149,15,1,'151',2,440,'1',0),(150,30,1,'151',2,441,'1',0),(151,0,1,'-1',2,444,'1',0),(152,0,1,'-1',2,442,'1',0),(153,0,1,'-1',2,447,'1',NULL),(154,0,1,'-1',2,448,'1',NULL);
/*!40000 ALTER TABLE `smsmenu_levels` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `smsmenu_session`
--

DROP TABLE IF EXISTS `smsmenu_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `smsmenu_session` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `msisdn` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
  `smsmenu_levels_id_fk` int(11) NOT NULL DEFAULT '-1',
  `language_id` int(2) NOT NULL DEFAULT '1',
  `timeStamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `menuid` int(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `msisidn_idx` (`msisdn`),
  KEY `timeStamp_idx` (`timeStamp`),
  KEY `msisdn_timeStamp_idx` (`msisdn`,`timeStamp`),
  KEY `msisdn_lang_sms_levels_id_fk_timeStamp` (`msisdn`,`smsmenu_levels_id_fk`,`language_id`,`timeStamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Stores the point/level which the subscriber is at during his nagivation on the sms menu. This is only valid for 24 hours';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `smsmenu_session`
--

LOCK TABLES `smsmenu_session` WRITE;
/*!40000 ALTER TABLE `smsmenu_session` DISABLE KEYS */;
/*!40000 ALTER TABLE `smsmenu_session` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subscriber_profile`
--

DROP TABLE IF EXISTS `subscriber_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `subscriber_profile` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `msisdn` varchar(45) DEFAULT NULL,
  `language_id` int(2) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subscriber_profile`
--

LOCK TABLES `subscriber_profile` WRITE;
/*!40000 ALTER TABLE `subscriber_profile` DISABLE KEYS */;
/*!40000 ALTER TABLE `subscriber_profile` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subscription`
--

DROP TABLE IF EXISTS `subscription`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `subscription` (
  `id` bigint(20) NOT NULL,
  `credibility_index` int(11) DEFAULT NULL,
  `expiryDate` datetime DEFAULT NULL,
  `msisdn` varchar(255) DEFAULT NULL,
  `queue_status` bigint(20) DEFAULT NULL,
  `renewal_count` bigint(20) DEFAULT NULL,
  `request_medium` varchar(255) DEFAULT NULL,
  `sms_service_id_fk` bigint(20) DEFAULT NULL,
  `smsmenu_levels_id_fk` int(11) DEFAULT NULL,
  `subActive` tinyint(1) DEFAULT NULL,
  `subscription_status` varchar(255) DEFAULT NULL,
  `subscription_timeStamp` datetime DEFAULT NULL,
  `opco_id_fk` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `expiryDateidx` (`expiryDate`),
  KEY `msisdnIdx` (`msisdn`),
  KEY `oprtfkidx` (`opco_id_fk`),
  KEY `sms_service_idx` (`sms_service_id_fk`),
  KEY `tstmpIDX` (`subscription_timeStamp`),
  KEY `FK_afx13f9ta6fwwx83bj7f70cej` (`opco_id_fk`),
  CONSTRAINT `FK_afx13f9ta6fwwx83bj7f70cej` FOREIGN KEY (`opco_id_fk`) REFERENCES `operator_country` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subscription`
--

LOCK TABLES `subscription` WRITE;
/*!40000 ALTER TABLE `subscription` DISABLE KEYS */;
/*!40000 ALTER TABLE `subscription` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subscription_history`
--

DROP TABLE IF EXISTS `subscription_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `subscription_history` (
  `id` bigint(20) NOT NULL,
  `alteration_method` varchar(255) DEFAULT NULL,
  `event` bigint(20) DEFAULT NULL,
  `msisdn` varchar(255) NOT NULL,
  `service_id` bigint(20) NOT NULL,
  `timeStamp` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `subhistidx` (`msisdn`,`service_id`,`timeStamp`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subscription_history`
--

LOCK TABLES `subscription_history` WRITE;
/*!40000 ALTER TABLE `subscription_history` DISABLE KEYS */;
/*!40000 ALTER TABLE `subscription_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subscriptionlog`
--

DROP TABLE IF EXISTS `subscriptionlog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `subscriptionlog` (
  `id` bigint(20) NOT NULL,
  `msisdn` varchar(255) DEFAULT NULL,
  `service_subscription_id` int(11) DEFAULT NULL,
  `subscription_id` bigint(20) DEFAULT NULL,
  `timeStamp` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `msisdn_idx` (`msisdn`),
  KEY `servsub_idx` (`service_subscription_id`),
  KEY `subLog_idx` (`subscription_id`),
  KEY `sblogtmstp_idx` (`timeStamp`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subscriptionlog`
--

LOCK TABLES `subscriptionlog` WRITE;
/*!40000 ALTER TABLE `subscriptionlog` DISABLE KEYS */;
/*!40000 ALTER TABLE `subscriptionlog` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `success_billing`
--

DROP TABLE IF EXISTS `success_billing`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `success_billing` (
  `id` int(11) NOT NULL,
  `cp_tx_id` decimal(19,2) DEFAULT NULL,
  `keyword` varchar(255) DEFAULT NULL,
  `msisdn` varchar(255) DEFAULT NULL,
  `operation` varchar(255) DEFAULT NULL,
  `price` decimal(19,2) DEFAULT NULL,
  `price_point_keyword` varchar(255) DEFAULT NULL,
  `resp_status_code` varchar(255) DEFAULT NULL,
  `shortcode` varchar(255) DEFAULT NULL,
  `success` tinyint(1) DEFAULT NULL,
  `timeStamp` datetime NOT NULL,
  `transactionId` varchar(255) DEFAULT NULL,
  `transferin` tinyint(1) DEFAULT NULL,
  `opco_id_fk` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_51jwl8qbp8hailmevt5q1fokw` (`cp_tx_id`),
  UNIQUE KEY `UK_a57qfds28g75rk39emnabrfvu` (`transactionId`),
  KEY `sblocpidx` (`opco_id_fk`),
  KEY `billi_idx` (`timeStamp`),
  KEY `FK_f3dq4pr6niq4w5rwgm94wylb9` (`opco_id_fk`),
  CONSTRAINT `FK_f3dq4pr6niq4w5rwgm94wylb9` FOREIGN KEY (`opco_id_fk`) REFERENCES `operator_country` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `success_billing`
--

LOCK TABLES `success_billing` WRITE;
/*!40000 ALTER TABLE `success_billing` DISABLE KEYS */;
/*!40000 ALTER TABLE `success_billing` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `topupnumber`
--

DROP TABLE IF EXISTS `topupnumber`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `topupnumber` (
  `id` int(11) NOT NULL,
  `depleted` tinyint(1) DEFAULT NULL,
  `number` varchar(255) DEFAULT NULL,
  `serial` decimal(19,2) DEFAULT NULL,
  `telco` int(11) DEFAULT NULL,
  `value` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_8k9ejwfs6let9xxmrhc0nrxv` (`number`),
  UNIQUE KEY `UK_knqim4x2jb6gbp5jhsggw6hee` (`serial`),
  KEY `numberIdx` (`number`),
  KEY `serialIdx` (`serial`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `topupnumber`
--

LOCK TABLES `topupnumber` WRITE;
/*!40000 ALTER TABLE `topupnumber` DISABLE KEYS */;
/*!40000 ALTER TABLE `topupnumber` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL,
  `u_pwd` varchar(255) DEFAULT NULL,
  `u_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `u_name_idx` (`u_name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_role`
--

DROP TABLE IF EXISTS `user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_role` (
  `user_id` bigint(20) NOT NULL,
  `roles_id` bigint(20) NOT NULL,
  KEY `FK_5k3dviices5fr7560hvc81x4r` (`roles_id`),
  KEY `FK_apcc8lxk2xnug8377fatvbn04` (`user_id`),
  CONSTRAINT `FK_5k3dviices5fr7560hvc81x4r` FOREIGN KEY (`roles_id`) REFERENCES `role` (`id`),
  CONSTRAINT `FK_apcc8lxk2xnug8377fatvbn04` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_role`
--

LOCK TABLES `user_role` WRITE;
/*!40000 ALTER TABLE `user_role` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ussd_session`
--

DROP TABLE IF EXISTS `ussd_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ussd_session` (
  `id` bigint(20) NOT NULL,
  `language_id` bigint(20) DEFAULT NULL,
  `menuid` bigint(20) DEFAULT NULL,
  `msisdn` varchar(255) DEFAULT NULL,
  `sessionId` decimal(19,2) DEFAULT NULL,
  `smsmenu_levels_id_fk` bigint(20) DEFAULT NULL,
  `timeStamp` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `smsidnidx` (`msisdn`),
  KEY `ssessidx` (`sessionId`),
  KEY `ststpidx` (`timeStamp`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ussd_session`
--

LOCK TABLES `ussd_session` WRITE;
/*!40000 ALTER TABLE `ussd_session` DISABLE KEYS */;
/*!40000 ALTER TABLE `ussd_session` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2015-08-31  0:37:46
