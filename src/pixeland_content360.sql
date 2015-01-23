-- MySQL dump 10.13  Distrib 5.5.8, for Win64 (x86)
--
-- Host: pixelandtag.com    Database: pixeland_content360
-- ------------------------------------------------------
-- Server version	5.5.40-cll

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
-- Table structure for table `BaseEntity`
--

DROP TABLE IF EXISTS `BaseEntity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `BaseEntity` (
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `BaseEntity`
--

LOCK TABLES `BaseEntity` WRITE;
/*!40000 ALTER TABLE `BaseEntity` DISABLE KEYS */;
/*!40000 ALTER TABLE `BaseEntity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SMSStatLog`
--

DROP TABLE IF EXISTS `SMSStatLog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SMSStatLog` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `SMSServiceID` int(11) NOT NULL,
  `timeStamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `msisdn` varchar(45) DEFAULT NULL,
  `charged` tinyint(4) NOT NULL DEFAULT '0',
  `transactionID` varchar(45) NOT NULL,
  `statusCode` enum('LimitSubscriberFailure','InvalidSubscriber','KeywordsNotFound','TariffCodeNotFound','Invalidshortcode','SetCMPConnectionFailure','ServicePriceNotFound','PSAInsufficientBalance','PSAInvalidNumber','PSANumberBarred','PSABusy','PSACreditExceeded','PSAChargeFailure','SCRNotInAllowList','SubscriberBlackListed','PCMSendFail','NegativeDN','Success','PCM200','PCM301','PCM302','PCM303','PCM304','PCM305','PCM306','PCM307','PCM400','PCM402','PCM403','PCM404','PCM405','PCM406','WaitingForDLR') NOT NULL DEFAULT 'WaitingForDLR',
  `subscription` tinyint(4) NOT NULL DEFAULT '0',
  `CMP_Keyword` varchar(45) NOT NULL,
  `CMP_SKeyword` varchar(45) NOT NULL,
  `price` double NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `transactionID` (`transactionID`),
  KEY `timeStamp` (`timeStamp`),
  KEY `msisdn` (`msisdn`),
  KEY `cmp_keywords` (`CMP_Keyword`,`CMP_SKeyword`),
  KEY `cmpKeyWord` (`CMP_Keyword`),
  KEY `cmpSKeyword` (`CMP_SKeyword`),
  KEY `statusCode_idx` (`statusCode`),
  KEY `price` (`price`),
  KEY `price_statusCod_idx` (`price`,`statusCode`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SMSStatLog`
--

LOCK TABLES `SMSStatLog` WRITE;
/*!40000 ALTER TABLE `SMSStatLog` DISABLE KEYS */;
/*!40000 ALTER TABLE `SMSStatLog` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ServiceSubscription`
--

DROP TABLE IF EXISTS `ServiceSubscription`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ServiceSubscription` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `serviceid` int(11) NOT NULL DEFAULT '0',
  `schedule` datetime DEFAULT NULL,
  `lastUpdated` datetime DEFAULT NULL,
  `ExpiryDate` datetime DEFAULT '2025-01-01 00:00:00',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ServiceSubscription`
--

LOCK TABLES `ServiceSubscription` WRITE;
/*!40000 ALTER TABLE `ServiceSubscription` DISABLE KEYS */;
/*!40000 ALTER TABLE `ServiceSubscription` ENABLE KEYS */;
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
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `cp_id` varchar(255) DEFAULT NULL,
  `cp_tx_id` bigint(20) DEFAULT NULL,
  `discount_applied` varchar(255) DEFAULT NULL,
  `event_type` varchar(255) DEFAULT NULL,
  `in_outgoing_queue` bigint(20) DEFAULT NULL,
  `keyword` varchar(255) DEFAULT NULL,
  `maxRetriesAllowed` bigint(20) DEFAULT NULL,
  `message_id` bigint(20) DEFAULT NULL,
  `msisdn` varchar(255) DEFAULT NULL,
  `operation` varchar(255) DEFAULT NULL,
  `price` decimal(19,2) DEFAULT NULL,
  `priority` bigint(20) DEFAULT NULL,
  `processed` bigint(20) DEFAULT NULL,
  `resp_status_code` varchar(255) DEFAULT NULL,
  `retry_count` bigint(20) DEFAULT NULL,
  `service_id` varchar(255) DEFAULT NULL,
  `shortcode` varchar(255) DEFAULT NULL,
  `success` tinyint(1) DEFAULT NULL,
  `timeStamp` datetime DEFAULT NULL,
  `tx_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_7m5nl58ehxkxy8lv7kr6i7f5x` (`cp_tx_id`),
  KEY `cp_idtxid_idx` (`cp_tx_id`),
  KEY `outq_idx` (`in_outgoing_queue`),
  KEY `priority_idx` (`priority`),
  KEY `processed_idx` (`processed`),
  KEY `timeStamp_idx` (`timeStamp`)
) ENGINE=InnoDB AUTO_INCREMENT=74 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `billable_queue`
--

LOCK TABLES `billable_queue` WRITE;
/*!40000 ALTER TABLE `billable_queue` DISABLE KEYS */;
INSERT INTO `billable_queue` VALUES (1,'CONTENT360_KE',1417741966122,'0','SUBSCRIPTION_PURCHASE',0,'JOBS',1,1,'254734606096','debit',5.00,0,1,'200',1,'JOBS','32329',0,'2014-12-04 20:12:47',1417741966122),(2,'CONTENT360_KE',1417742046410,'0','SUBSCRIPTION_PURCHASE',0,'JOBS',1,2,'254734606096','debit',5.00,0,1,'200',1,'JOBS','32329',0,'2014-12-04 20:14:07',1417742046410),(3,'CONTENT360_KE',1417742563424,'0','SUBSCRIPTION_PURCHASE',0,'JOBS',1,3,'254734606096','debit',5.00,0,1,'200',1,'JOBS','32329',0,'2014-12-04 20:22:44',1417742563424),(4,'CONTENT360_KE',1417742600730,'0','SUBSCRIPTION_PURCHASE',0,'JOBS',1,4,'254734606096','debit',5.00,0,1,'200',1,'JOBS','32329',0,'2014-12-04 20:23:20',1417742600730),(5,'CONTENT360_KE',1417742635425,'0','SUBSCRIPTION_PURCHASE',0,'JOBS',1,5,'254734606096','debit',5.00,0,1,'200',1,'JOBS','32329',0,'2014-12-04 20:23:55',1417742635425),(6,'CONTENT360_KE',1417743059169,'0','SUBSCRIPTION_PURCHASE',0,'JOBS',1,6,'254734606096','debit',5.00,0,1,'Success',1,'JOBS','32329',1,'2014-12-04 20:31:00',1417743059169),(7,'CONTENT360_KE',1417743080316,'0','SUBSCRIPTION_PURCHASE',0,'JOBS',1,8,'254734606096','debit',5.00,0,1,'Success',1,'JOBS','32329',1,'2014-12-04 20:31:20',1417743080316),(8,'CONTENT360_KE',1417743098213,'0','SUBSCRIPTION_PURCHASE',0,'JOBS',1,10,'254734606096','debit',5.00,0,1,'Success',1,'JOBS','32329',1,'2014-12-04 20:31:38',1417743098213),(9,'CONTENT360_KE',1417743282777,'0','SUBSCRIPTION_PURCHASE',1,'JOBS',1,12,'254734606096','debit',5.00,0,0,NULL,0,'JOBS','32329',NULL,'2014-12-04 20:34:44',1417743282777),(10,'CONTENT360_KE',1417743328241,'0','SUBSCRIPTION_PURCHASE',1,'JOBS',1,13,'254734606096','debit',5.00,0,0,NULL,0,'JOBS','32329',NULL,'2014-12-04 20:35:28',1417743328241),(11,'CONTENT360_KE',1417743686906,'0','SUBSCRIPTION_PURCHASE',0,'JOBS',1,14,'254734606096','debit',5.00,0,1,'Success',1,'JOBS','32329',1,'2014-12-04 20:41:28',1417743686906),(12,'CONTENT360_KE',1417743871804,'0','SUBSCRIPTION_PURCHASE',0,'JOBS',1,16,'254734606096','debit',5.00,0,1,'Success',1,'JOBS','32329',1,'2014-12-04 20:44:32',1417743871804),(13,'CONTENT360_KE',1417744053421,'0','SUBSCRIPTION_PURCHASE',1,'JOBS',1,18,'254734606096','debit',5.00,0,0,NULL,0,'JOBS','32329',NULL,'2014-12-04 20:47:34',1417744053421),(14,'CONTENT360_KE',1417744085057,'0','SUBSCRIPTION_PURCHASE',0,'JOBS',1,19,'254734606096','debit',5.00,0,1,'Success',1,'JOBS','32329',1,'2014-12-04 20:48:05',1417744085057),(15,'CONTENT360_KE',1417744284976,'0','SUBSCRIPTION_PURCHASE',1,'JOBS',1,21,'254734606096','debit',5.00,0,0,NULL,0,'JOBS','32329',NULL,'2014-12-04 20:51:26',1417744284976),(16,'CONTENT360_KE',1417744312370,'0','SUBSCRIPTION_PURCHASE',1,'JOBS',1,22,'254734606096','debit',5.00,0,0,NULL,0,'JOBS','32329',NULL,'2014-12-04 20:51:52',1417744312370),(17,'CONTENT360_KE',1417744994255,'0','SUBSCRIPTION_PURCHASE',0,'JOBS',1,23,'254734606096','debit',5.00,0,1,'Success',1,'JOBS','32329',1,'2014-12-04 21:03:14',1417744994255),(18,'CONTENT360_KE',1417745300639,'0','SUBSCRIPTION_PURCHASE',1,'JOBS',1,25,'254734606096','debit',5.00,0,0,NULL,0,'JOBS','32329',NULL,'2014-12-04 21:08:21',1417745300639),(19,'CONTENT360_KE',1417745358665,'0','SUBSCRIPTION_PURCHASE',0,'JOBS',1,26,'254734606096','debit',5.00,0,1,'Success',1,'JOBS','32329',1,'2014-12-04 21:09:18',1417745358665),(20,'CONTENT360_KE',1417746039448,'0','SUBSCRIPTION_PURCHASE',0,'JOBS',1,28,'254734606096','debit',5.00,0,1,'Success',1,'JOBS','32329',1,'2014-12-04 21:20:40',1417746039448),(21,'CONTENT360_KE',1417746063582,'0','SUBSCRIPTION_PURCHASE',0,'JOBS',1,30,'254734606096','debit',5.00,0,1,'Success',1,'JOBS','32329',1,'2014-12-04 21:21:03',1417746063582),(22,'CONTENT360_KE',1417746385889,'0','SUBSCRIPTION_PURCHASE',1,'JOBS',1,32,'254734606096','debit',5.00,0,0,NULL,0,'JOBS','32329',NULL,'2014-12-04 21:26:26',1417746385889),(23,'CONTENT360_KE',1417746552748,'0','SUBSCRIPTION_PURCHASE',0,'JOBS',1,33,'254734606096','debit',5.00,0,1,'Success',1,'JOBS','32329',1,'2014-12-04 21:29:13',1417746552748),(24,'CONTENT360_KE',1417746570726,'0','SUBSCRIPTION_PURCHASE',0,'JOBS',1,35,'254734606096','debit',5.00,0,1,'Success',1,'JOBS','32329',1,'2014-12-04 21:29:30',1417746570726),(25,'CONTENT360_KE',1417746685476,'0','SUBSCRIPTION_PURCHASE',1,'JOBS',1,37,'254734606096','debit',5.00,0,0,NULL,0,'JOBS','32329',NULL,'2014-12-04 21:31:26',1417746685476),(26,'CONTENT360_KE',1417746894246,'0','SUBSCRIPTION_PURCHASE',1,'JOBS',1,38,'254734606096','debit',5.00,0,0,NULL,0,'JOBS','32329',NULL,'2014-12-04 21:34:55',1417746894246),(27,'CONTENT360_KE',1417746904721,'0','SUBSCRIPTION_PURCHASE',1,'JOBS',1,39,'254734606096','debit',5.00,0,0,NULL,0,'JOBS','32329',NULL,'2014-12-04 21:35:04',1417746904721),(28,'CONTENT360_KE',1417748156923,'0','SUBSCRIPTION_PURCHASE',0,'JOBS',1,40,'254734606096','debit',5.00,0,1,'Success',1,'JOBS','32329',1,'2014-12-04 21:55:57',1417748156923),(29,'CONTENT360_KE',1417748180925,'0','SUBSCRIPTION_PURCHASE',0,'JOBS',1,42,'254734606096','debit',5.00,0,1,'Success',1,'JOBS','32329',1,'2014-12-04 21:56:21',1417748180925),(30,'CONTENT360_KE',1417748228458,'0','CONTENT_PURCHASE',0,'JOY',1,44,'254734606096','debit',5.00,0,1,'200',1,'JOY','32329',0,'2014-12-04 21:57:08',1417748228458),(31,'CONTENT360_KE',1417748262199,'0','CONTENT_PURCHASE',0,'JOB',1,45,'254734606096','debit',5.00,0,1,'200',1,'JOB','32329',0,'2014-12-04 21:57:42',1417748262199),(32,'CONTENT360_KE',1417748300070,'0','SUBSCRIPTION_PURCHASE',0,'JOBS',1,46,'254734606096','debit',5.00,0,1,'Success',1,'JOBS','32329',1,'2014-12-04 21:58:20',1417748300070),(33,'CONTENT360_KE',1417748321780,'0','SUBSCRIPTION_PURCHASE',0,'JOBS',1,48,'254734606096','debit',5.00,0,1,'Success',1,'JOBS','32329',1,'2014-12-04 21:58:41',1417748321780),(34,'CONTENT360_KE',1417748343456,'0','SUBSCRIPTION_PURCHASE',0,'JOBS',1,50,'254734606096','debit',5.00,0,1,'Success',1,'JOBS','32329',1,'2014-12-04 21:59:03',1417748343456),(35,'CONTENT360_KE',1417748375941,'0','SUBSCRIPTION_PURCHASE',0,'JOBS',1,52,'254734606096','debit',5.00,0,1,'Success',1,'JOBS','32329',1,'2014-12-04 21:59:35',1417748375941),(36,'CONTENT360_KE',1417748413845,'0','SUBSCRIPTION_PURCHASE',0,'JOBS',1,54,'254734606096','debit',5.00,0,1,'Success',1,'JOBS','32329',1,'2014-12-04 22:00:13',1417748413845),(37,'CONTENT360_KE',1417748450311,'0','SUBSCRIPTION_PURCHASE',0,'JOBS',1,56,'254734606096','debit',5.00,0,1,'Success',1,'JOBS','32329',1,'2014-12-04 22:00:49',1417748450311),(38,'CONTENT360_KE',1417748600509,'0','SUBSCRIPTION_PURCHASE',1,'JOBS',1,62,'254734606096','debit',5.00,0,0,NULL,0,'JOBS','32329',NULL,'2014-12-04 22:03:20',1417748600509),(39,'CONTENT360_KE',1417750363727,'0','SUBSCRIPTION_PURCHASE',0,'JOBS',1,63,'254734606096','debit',5.00,0,1,'Success',1,'JOBS','32329',1,'2014-12-04 22:32:44',1417750363727),(40,'CONTENT360_KE',1417750692612,'0','SUBSCRIPTION_PURCHASE',0,'JOBS',1,64,'254734606096','debit',5.00,0,1,'Success',1,'JOBS','32329',1,'2014-12-04 22:38:14',1417750692612),(41,'CONTENT360_KE',1417751542074,'0','SUBSCRIPTION_PURCHASE',0,'JOBS',1,67,'254734606096','debit',5.00,0,1,'Success',1,'JOBS','32329',1,'2014-12-04 22:52:22',1417751542074),(42,'CONTENT360_KE',1417751704735,'0','SUBSCRIPTION_PURCHASE',1,'JOBS',1,68,'254734606096','debit',5.00,0,0,NULL,0,'JOBS','32329',NULL,'2014-12-04 22:55:05',1417751704735),(43,'CONTENT360_KE',1417752150210,'0','SUBSCRIPTION_PURCHASE',1,'JOBS',1,69,'254734606096','debit',5.00,0,0,NULL,0,'JOBS','32329',NULL,'2014-12-04 23:02:31',1417752150210),(44,'CONTENT360_KE',1417752978702,'0','SUBSCRIPTION_PURCHASE',0,'JOBS',1,73,'254734606096','debit',5.00,0,1,'Success',1,'JOBS','32329',1,'2014-12-04 23:16:19',1417752978702),(45,'CONTENT360_KE',1417753074811,'0','SUBSCRIPTION_PURCHASE',1,'JOBS',1,77,'254734606096','debit',5.00,0,0,NULL,0,'JOBS','32329',NULL,'2014-12-04 23:17:54',1417753074811),(46,'CONTENT360_KE',1417753950292,'0','SUBSCRIPTION_PURCHASE',0,'JOBS',1,78,'254734606096','debit',5.00,0,1,'Success',1,'JOBS','32329',1,'2014-12-04 23:32:30',1417753950292),(47,'CONTENT360_KE',1417753967230,'0','SUBSCRIPTION_PURCHASE',0,'JOBS',1,80,'254734606096','debit',5.00,0,1,'Success',1,'JOBS','32329',1,'2014-12-04 23:32:47',1417753967230),(48,'CONTENT360_KE',1417754045571,'0','SUBSCRIPTION_PURCHASE',1,'JOBS',1,84,'254734606096','debit',5.00,0,0,NULL,0,'JOBS','32329',NULL,'2014-12-04 23:34:05',1417754045571),(49,'CONTENT360_KE',1417754094576,'0','SUBSCRIPTION_PURCHASE',1,'JOBS',1,85,'254734606096','debit',5.00,0,0,NULL,0,'JOBS','32329',NULL,'2014-12-04 23:34:54',1417754094576),(50,'CONTENT360_KE',1417754115328,'0','SUBSCRIPTION_PURCHASE',1,'JOBS',1,86,'254734606096','debit',5.00,0,0,NULL,0,'JOBS','32329',NULL,'2014-12-04 23:35:15',1417754115328),(51,'CONTENT360_KE',1417754633238,'0','SUBSCRIPTION_PURCHASE',1,'JOBS',1,87,'254734606096','debit',5.00,0,0,NULL,0,'JOBS','32329',NULL,'2014-12-04 23:43:54',1417754633238),(52,'CONTENT360_KE',1417754911191,'0','SUBSCRIPTION_PURCHASE',0,'JOBS',1,90,'254734606096','debit',5.00,0,1,'Success',1,'JOBS','32329',1,'2014-12-04 23:48:31',1417754911191),(53,'CONTENT360_KE',1417754930319,'0','SUBSCRIPTION_PURCHASE',1,'JOBS',1,92,'254734606096','debit',5.00,0,0,NULL,0,'JOBS','32329',NULL,'2014-12-04 23:48:49',1417754930319),(54,'CONTENT360_KE',1417755016146,'0','SUBSCRIPTION_PURCHASE',1,'JOBS',1,93,'254734606096','debit',5.00,0,0,NULL,0,'JOBS','32329',NULL,'2014-12-04 23:50:16',1417755016146),(55,'CONTENT360_KE',1417755040390,'0','SUBSCRIPTION_PURCHASE',1,'JOBS',1,94,'254734606096','debit',5.00,0,0,NULL,0,'JOBS','32329',NULL,'2014-12-04 23:50:40',1417755040390),(56,'CONTENT360_KE',1417755070666,'0','SUBSCRIPTION_PURCHASE',1,'JOBS',1,95,'254734606096','debit',5.00,0,0,NULL,0,'JOBS','32329',NULL,'2014-12-04 23:51:10',1417755070666),(57,'CONTENT360_KE',1417755419508,'0','SUBSCRIPTION_PURCHASE',0,'JOBS',1,96,'254734606096','debit',5.00,0,1,'Success',1,'JOBS','32329',1,'2014-12-04 23:57:00',1417755419508),(58,'CONTENT360_KE',1417755436495,'0','SUBSCRIPTION_PURCHASE',0,'JOBS',1,98,'254734606096','debit',5.00,0,1,'Success',1,'JOBS','32329',1,'2014-12-04 23:57:16',1417755436495),(59,'CONTENT360_KE',1417755510541,'0','SUBSCRIPTION_PURCHASE',1,'JOBS',1,100,'254734606096','debit',5.00,0,0,NULL,0,'JOBS','32329',NULL,'2014-12-04 23:58:31',1417755510541),(60,'CONTENT360_KE',1417755532748,'0','SUBSCRIPTION_PURCHASE',1,'JOBS',1,101,'254734606096','debit',5.00,0,0,NULL,0,'JOBS','32329',NULL,'2014-12-04 23:58:52',1417755532748),(61,'CONTENT360_KE',1417755729104,'0','SUBSCRIPTION_PURCHASE',0,'JOBS',1,102,'254734606096','debit',5.00,0,1,'Success',1,'JOBS','32329',1,'2014-12-05 00:02:11',1417755729104),(62,'CONTENT360_KE',1417755863178,'0','SUBSCRIPTION_PURCHASE',1,'JOBS',1,104,'254734606096','debit',5.00,0,0,NULL,0,'JOBS','32329',NULL,'2014-12-05 00:04:24',1417755863178),(63,'CONTENT360_KE',1417755887800,'0','SUBSCRIPTION_PURCHASE',1,'JOBS',1,105,'254734606096','debit',5.00,0,0,NULL,0,'JOBS','32329',NULL,'2014-12-05 00:04:48',1417755887800),(64,'CONTENT360_KE',1417756330669,'0','SUBSCRIPTION_PURCHASE',0,'JOBS',1,106,'254734606096','debit',5.00,0,1,'Success',1,'JOBS','32329',1,'2014-12-05 00:12:12',1417756330669),(65,'CONTENT360_KE',1417756346455,'0','SUBSCRIPTION_PURCHASE',0,'JOBS',1,108,'254734606096','debit',5.00,0,1,'Success',1,'JOBS','32329',1,'2014-12-05 00:12:26',1417756346455),(66,'CONTENT360_KE',1417756360017,'0','SUBSCRIPTION_PURCHASE',0,'JOBS',1,110,'254734606096','debit',5.00,0,1,'Success',1,'JOBS','32329',1,'2014-12-05 00:12:40',1417756360017),(67,'CONTENT360_KE',1417756376390,'0','SUBSCRIPTION_PURCHASE',0,'JOBS',1,112,'254734606096','debit',5.00,0,1,'Success',1,'JOBS','32329',1,'2014-12-05 00:12:57',1417756376390),(68,'CONTENT360_KE',1417756389431,'0','SUBSCRIPTION_PURCHASE',0,'JOBS',1,114,'254734606096','debit',5.00,0,1,'Success',1,'JOBS','32329',1,'2014-12-05 00:13:10',1417756389431),(69,'CONTENT360_KE',1417756407640,'0','SUBSCRIPTION_PURCHASE',0,'JOBS',1,116,'254734606096','debit',5.00,0,1,'Success',1,'JOBS','32329',1,'2014-12-05 00:13:28',1417756407640),(70,'CONTENT360_KE',1417756438089,'0','SUBSCRIPTION_PURCHASE',0,'JOBS',1,118,'254734606096','debit',5.00,0,1,'Success',1,'JOBS','32329',1,'2014-12-05 00:13:59',1417756438089),(71,'CONTENT360_KE',1417757621933,'0','SUBSCRIPTION_PURCHASE',1,'JOBS',1,120,'254734606096','debit',5.00,0,0,NULL,0,'JOBS','32329',NULL,'2014-12-05 00:33:44',1417757621933),(72,'CONTENT360_KE',1417757638003,'0','SUBSCRIPTION_PURCHASE',1,'JOBS',1,121,'254734606096','debit',5.00,0,0,NULL,0,'JOBS','32329',NULL,'2014-12-05 00:33:58',1417757638003),(73,'CONTENT360_KE',1417758896864,'0','SUBSCRIPTION_PURCHASE',1,'JOBS',1,122,'254734606096','debit',5.00,0,0,NULL,0,'JOBS','32329',NULL,'2014-12-05 00:54:58',1417758896864);
/*!40000 ALTER TABLE `billable_queue` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `broadcast_schedule`
--

DROP TABLE IF EXISTS `broadcast_schedule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `broadcast_schedule` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `keyword_id_fk` int(10) NOT NULL COMMENT 'The keyword id or the service id.',
  `push_hour` int(10) NOT NULL COMMENT 'The hour that this push should go',
  `last_push_done_timeStamp` timestamp NOT NULL DEFAULT '1979-01-01 02:00:00' COMMENT 'This is the timestamp for the',
  `active` int(1) NOT NULL DEFAULT '1' COMMENT 'Flag indicating whether the schedule is ',
  PRIMARY KEY (`id`),
  UNIQUE KEY `keyword_push_hour_idx` (`keyword_id_fk`,`push_hour`),
  KEY `active_idx` (`active`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `broadcast_schedule`
--

LOCK TABLES `broadcast_schedule` WRITE;
/*!40000 ALTER TABLE `broadcast_schedule` DISABLE KEYS */;
INSERT INTO `broadcast_schedule` VALUES (1,1,6,'1979-01-01 02:00:00',1),(2,2,6,'1979-01-01 02:00:00',1),(3,3,6,'1979-01-01 02:00:00',1);
/*!40000 ALTER TABLE `broadcast_schedule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `content`
--

DROP TABLE IF EXISTS `content`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `content` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `keyword_id_fk` int(11) NOT NULL DEFAULT '-1',
  `SMS_Content` text,
  `upload_timeStamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `keyword_id_fk_idx` (`keyword_id_fk`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `content`
--

LOCK TABLES `content` WRITE;
/*!40000 ALTER TABLE `content` DISABLE KEYS */;
/*!40000 ALTER TABLE `content` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `content_log`
--

DROP TABLE IF EXISTS `content_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `content_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `phone_number` varchar(45) NOT NULL COMMENT 'Phone number that got the subscription',
  `content_id_fk` int(11) NOT NULL DEFAULT '-1' COMMENT 'The conent piece id from the table called "content"',
  `subscription_id_fk` int(11) NOT NULL DEFAULT '-1' COMMENT 'This links directly to the subscription id. Is a foreign key from the table called "subscription"',
  `broadcast_schedule_id_fk` int(11) NOT NULL DEFAULT '-1' COMMENT 'The broadcast schedule id. Is a foreign key from the table called "broadcast_schedule"',
  `timeStamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp of record entry.',
  PRIMARY KEY (`id`),
  KEY `phone_number_id_content_id_fks_idx` (`phone_number`,`content_id_fk`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `content_log`
--

LOCK TABLES `content_log` WRITE;
/*!40000 ALTER TABLE `content_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `content_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dynamiccontent_content`
--

DROP TABLE IF EXISTS `dynamiccontent_content`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dynamiccontent_content` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `contentid` int(11) NOT NULL DEFAULT '0',
  `Category` varchar(100) NOT NULL DEFAULT '',
  `Text` mediumtext NOT NULL,
  `headline` varchar(1000) DEFAULT NULL,
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `dirty` tinyint(1) NOT NULL DEFAULT '0',
  `contentItemId` int(11) DEFAULT '0',
  `UserID` int(11) NOT NULL DEFAULT '0',
  `localeid` int(10) unsigned NOT NULL DEFAULT '0',
  `guid` varchar(300) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `dirty` (`dirty`),
  KEY `guid` (`guid`,`contentid`),
  KEY `contentidx` (`contentid`,`timestamp`),
  KEY `ttidx` (`contentid`,`headline`(767))
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dynamiccontent_content`
--

LOCK TABLES `dynamiccontent_content` WRITE;
/*!40000 ALTER TABLE `dynamiccontent_content` DISABLE KEYS */;
/*!40000 ALTER TABLE `dynamiccontent_content` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dynamiccontent_contentlog`
--

DROP TABLE IF EXISTS `dynamiccontent_contentlog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dynamiccontent_contentlog` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `TelcoId` int(11) NOT NULL DEFAULT '0',
  `ServiceId` int(11) NOT NULL DEFAULT '0',
  `MSISDN` varchar(50) NOT NULL DEFAULT '',
  `contentID` int(11) NOT NULL DEFAULT '0',
  `timestamp` datetime NOT NULL,
  KEY `id` (`id`),
  KEY `Index_2` (`TelcoId`,`ServiceId`,`MSISDN`,`contentID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dynamiccontent_contentlog`
--

LOCK TABLES `dynamiccontent_contentlog` WRITE;
/*!40000 ALTER TABLE `dynamiccontent_contentlog` DISABLE KEYS */;
/*!40000 ALTER TABLE `dynamiccontent_contentlog` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dynamiccontent_contenttype`
--

DROP TABLE IF EXISTS `dynamiccontent_contenttype`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dynamiccontent_contenttype` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `Content` varchar(100) NOT NULL DEFAULT '',
  `Category` varchar(100) NOT NULL DEFAULT '',
  `Servlet` varchar(100) NOT NULL DEFAULT 'update.jsp',
  `Priority` int(11) NOT NULL DEFAULT '0',
  `urlcreated` tinyint(1) NOT NULL DEFAULT '0',
  `telcoid` int(11) NOT NULL DEFAULT '0',
  `contenttypeid` int(11) NOT NULL DEFAULT '0',
  `keyword` varchar(100) DEFAULT NULL,
  `maxlength` int(11) NOT NULL DEFAULT '160',
  `poolId` int(11) DEFAULT '0',
  `localeId` int(11) DEFAULT '2',
  `LanguageID` int(11) NOT NULL DEFAULT '1',
  `goal` int(3) DEFAULT NULL,
  `oldid` int(10) unsigned DEFAULT NULL,
  `shortcode` varchar(20) DEFAULT NULL,
  `usexhtml` tinyint(1) unsigned DEFAULT '0' COMMENT 'Show html editor or plain text editor for wap news',
  `serviceid` varchar(255) DEFAULT '0' COMMENT 'Which serviceid to push to on this newsitem',
  `instantdelete` tinyint(1) DEFAULT '0' COMMENT 'Should the news item be deleted instantly, or await the ping-back from the external server?',
  `waponly` tinyint(1) DEFAULT '0' COMMENT 'Show the SMS Body field in the CMS?',
  `maxdowntime` int(11) DEFAULT '129600',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dynamiccontent_contenttype`
--

LOCK TABLES `dynamiccontent_contenttype` WRITE;
/*!40000 ALTER TABLE `dynamiccontent_contenttype` DISABLE KEYS */;
/*!40000 ALTER TABLE `dynamiccontent_contenttype` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dynamiccontent_dirtycontent`
--

DROP TABLE IF EXISTS `dynamiccontent_dirtycontent`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dynamiccontent_dirtycontent` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `telcoid` int(11) unsigned DEFAULT NULL,
  `contentid` int(11) unsigned DEFAULT NULL,
  `dirty` tinyint(1) unsigned DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `contentitemid` (`telcoid`,`contentid`),
  KEY `telcoid` (`telcoid`,`dirty`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dynamiccontent_dirtycontent`
--

LOCK TABLES `dynamiccontent_dirtycontent` WRITE;
/*!40000 ALTER TABLE `dynamiccontent_dirtycontent` DISABLE KEYS */;
/*!40000 ALTER TABLE `dynamiccontent_dirtycontent` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dynamiccontent_mirror`
--

DROP TABLE IF EXISTS `dynamiccontent_mirror`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dynamiccontent_mirror` (
  `contenttypeid` int(11) NOT NULL,
  `telcoid` int(11) unsigned DEFAULT NULL,
  `serviceid` int(11) NOT NULL DEFAULT '-1',
  UNIQUE KEY `telcoid` (`telcoid`,`contenttypeid`),
  KEY `contenttypeid` (`contenttypeid`,`telcoid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dynamiccontent_mirror`
--

LOCK TABLES `dynamiccontent_mirror` WRITE;
/*!40000 ALTER TABLE `dynamiccontent_mirror` DISABLE KEYS */;
/*!40000 ALTER TABLE `dynamiccontent_mirror` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `error_log`
--

DROP TABLE IF EXISTS `error_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `error_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `timeStamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ERROR_TEXT` text,
  `recepients` varchar(45) DEFAULT NULL,
  `linkId` varchar(45) DEFAULT NULL,
  `from` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `error_log`
--

LOCK TABLES `error_log` WRITE;
/*!40000 ALTER TABLE `error_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `error_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `financeoption`
--

DROP TABLE IF EXISTS `financeoption`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `financeoption` (
  `id` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `institution_id_fk` int(11) DEFAULT NULL,
  `interest_rate_id_fk` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_2y86yi0ftfmovksb6e1h6i9q4` (`institution_id_fk`),
  KEY `FK_fsxtjpnpy7khnqwocdb1aysjs` (`interest_rate_id_fk`),
  CONSTRAINT `FK_2y86yi0ftfmovksb6e1h6i9q4` FOREIGN KEY (`institution_id_fk`) REFERENCES `institution` (`id`),
  CONSTRAINT `FK_fsxtjpnpy7khnqwocdb1aysjs` FOREIGN KEY (`interest_rate_id_fk`) REFERENCES `interestrate` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `financeoption`
--

LOCK TABLES `financeoption` WRITE;
/*!40000 ALTER TABLE `financeoption` DISABLE KEYS */;
/*!40000 ALTER TABLE `financeoption` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hibernate_sequence`
--

DROP TABLE IF EXISTS `hibernate_sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `hibernate_sequence` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hibernate_sequence`
--

LOCK TABLES `hibernate_sequence` WRITE;
/*!40000 ALTER TABLE `hibernate_sequence` DISABLE KEYS */;
INSERT INTO `hibernate_sequence` VALUES (1);
/*!40000 ALTER TABLE `hibernate_sequence` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `httptosend`
--

DROP TABLE IF EXISTS `httptosend`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `httptosend` (
  `id` bigint(21) NOT NULL AUTO_INCREMENT,
  `in_outgoing_queue` tinyint(1) DEFAULT '0',
  `SMS` text,
  `MSISDN` varchar(45) DEFAULT NULL,
  `Type` enum('RT','OL','IC','TC','TM','VCARD','VCALENDAR','VBOOKMARK','IMELODY','SERVICEINDICATION') NOT NULL DEFAULT 'TM',
  `SendFrom` varchar(45) DEFAULT NULL,
  `price` double DEFAULT '0',
  `Priority` int(2) DEFAULT '0',
  `serviceid` int(11) DEFAULT NULL,
  `re_tries` int(11) DEFAULT '0' COMMENT 'The number of times we''ve attempted to send this message.',
  `ttl` int(11) NOT NULL DEFAULT '0' COMMENT 'The maximum number of times we should re-try. It doesn''t make sense to re-try 1000 times. TODO add this check when retrieving messages to be sent. Low Prio though',
  `TimeStamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `fromAddr` varchar(45) DEFAULT NULL,
  `charged` tinyint(1) DEFAULT NULL,
  `sent` tinyint(1) DEFAULT '0',
  `CMP_AKeyword` varchar(45) DEFAULT NULL COMMENT 'Action keyword',
  `CMP_SKeyword` varchar(45) DEFAULT NULL COMMENT 'Service Keyword',
  `sub_deviceType` varchar(45) NOT NULL DEFAULT 'NOKIA2100',
  `sub_r_mobtel` varchar(45) DEFAULT NULL COMMENT 'Msisdn to receive message\n',
  `sub_c_mobtel` varchar(45) DEFAULT NULL COMMENT 'Msisdn to be charged\n',
  `CMP_TxID` bigint(20) DEFAULT '0',
  `newCMP_Txid` varchar(25) NOT NULL DEFAULT '-1',
  `apiType` enum('1','2','3','4','5','6','7') DEFAULT '1',
  `ACTION` enum('IOD','IODM','REG','REGM','FriendFinder','ADD','FIND','SEND') DEFAULT NULL,
  `split` tinyint(1) DEFAULT '0',
  `mo_processorFK` int(11) NOT NULL DEFAULT '-1',
  `SMS_DataCodingId` int(11) NOT NULL DEFAULT '-1',
  `billing_status` enum('NO_BILLING_REQUIRED','WAITING_BILLING','BILLING_INPROGRESS','SUCCESSFULLY_BILLED','BILLING_FAILED','BILLING_FAILED_PERMANENTLY','INSUFFICIENT_FUNDS') NOT NULL DEFAULT 'NO_BILLING_REQUIRED',
  PRIMARY KEY (`id`),
  UNIQUE KEY `tx_id` (`CMP_TxID`),
  KEY `timeStamp` (`TimeStamp`)
) ENGINE=InnoDB AUTO_INCREMENT=83 DEFAULT CHARSET=latin1 COMMENT='Contains messages to be sent out';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `httptosend`
--

LOCK TABLES `httptosend` WRITE;
/*!40000 ALTER TABLE `httptosend` DISABLE KEYS */;
INSERT INTO `httptosend` VALUES (1,0,'Welcome to content360 content platform. This is an auto response message. 5.0/- @ sms','254734606096','TM','32329',5,0,363,0,0,'2014-12-05 01:12:47','32329',NULL,0,'IOD','IOD0000','NOKIA2100',NULL,NULL,1417741966122,'-1','1',NULL,0,11,0,'WAITING_BILLING'),(2,0,'Welcome to content360 content platform. This is an auto response message. 5.0/- @ sms','254734606096','TM','32329',5,0,363,0,0,'2014-12-05 01:14:08','32329',NULL,0,'IOD','IOD0000','NOKIA2100',NULL,NULL,1417742046410,'-1','1',NULL,0,11,0,'WAITING_BILLING'),(3,0,'Welcome to content360 content platform. This is an auto response message. 5.0/- @ sms','254734606096','TM','32329',5,0,363,0,0,'2014-12-05 01:22:44','32329',NULL,0,'IOD','IOD0000','NOKIA2100',NULL,NULL,1417742563424,'-1','1',NULL,0,11,0,'WAITING_BILLING'),(4,0,'Welcome to content360 content platform. This is an auto response message. 5.0/- @ sms','254734606096','TM','32329',5,0,363,0,0,'2014-12-05 01:23:21','32329',NULL,0,'IOD','IOD0000','NOKIA2100',NULL,NULL,1417742600730,'-1','1',NULL,0,11,0,'WAITING_BILLING'),(5,0,'Welcome to content360 content platform. This is an auto response message. 5.0/- @ sms','254734606096','TM','32329',5,0,363,0,0,'2014-12-05 01:23:56','32329',NULL,0,'IOD','IOD0000','NOKIA2100',NULL,NULL,1417742635425,'-1','1',NULL,0,11,0,'WAITING_BILLING'),(9,0,'Welcome to content360 content platform. This is an auto response message. 5.0/- @ sms','254734606096','TM','32329',5,0,363,0,0,'2014-12-05 01:34:44','32329',NULL,0,'IOD','IOD0000','NOKIA2100',NULL,NULL,1417743282777,'-1','1',NULL,0,11,0,'WAITING_BILLING'),(10,0,'Welcome to content360 content platform. This is an auto response message. 5.0/- @ sms','254734606096','TM','32329',5,0,363,0,0,'2014-12-05 01:35:29','32329',NULL,0,'IOD','IOD0000','NOKIA2100',NULL,NULL,1417743328241,'-1','1',NULL,0,11,0,'WAITING_BILLING'),(13,0,'Welcome to content360 content platform. This is an auto response message. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','254734606096','TM','32329',5,0,363,0,0,'2014-12-05 01:47:35','32329',NULL,0,'IOD','IOD0000','NOKIA2100',NULL,NULL,1417744053421,'-1','1',NULL,0,11,0,'WAITING_BILLING'),(15,0,'Welcome to content360 content platform. This is an auto response message. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','254734606096','TM','32329',5,0,363,0,0,'2014-12-05 01:51:26','32329',NULL,0,'IOD','IOD0000','NOKIA2100',NULL,NULL,1417744284976,'-1','1',NULL,0,11,0,'WAITING_BILLING'),(16,0,'Welcome to content360 content platform. This is an auto response message. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','254734606096','TM','32329',5,0,363,0,0,'2014-12-05 01:51:53','32329',NULL,0,'IOD','IOD0000','NOKIA2100',NULL,NULL,1417744312370,'-1','1',NULL,0,11,0,'WAITING_BILLING'),(18,0,'Welcome to content360 content platform. This is an auto response message. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','254734606096','TM','32329',5,0,363,0,0,'2014-12-05 02:08:22','32329',NULL,0,'IOD','IOD0000','NOKIA2100',NULL,NULL,1417745300639,'-1','1',NULL,0,11,0,'WAITING_BILLING'),(22,0,'Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','254734606096','TM','32329',5,0,363,0,0,'2014-12-05 02:26:27','32329',NULL,0,'IOD','IOD0000','NOKIA2100',NULL,NULL,1417746385889,'-1','1',NULL,0,11,0,'WAITING_BILLING'),(25,0,'Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','254734606096','TM','32329',5,0,363,0,0,'2014-12-05 02:31:27','32329',NULL,0,'IOD','IOD0000','NOKIA2100',NULL,NULL,1417746685476,'-1','1',NULL,0,11,0,'WAITING_BILLING'),(26,0,'Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','254734606096','TM','32329',5,0,363,0,0,'2014-12-05 02:34:55','32329',NULL,0,'IOD','IOD0000','NOKIA2100',NULL,NULL,1417746894246,'-1','1',NULL,0,11,0,'WAITING_BILLING'),(27,0,'Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','254734606096','TM','32329',5,0,363,0,0,'2014-12-05 02:35:05','32329',NULL,0,'IOD','IOD0000','NOKIA2100',NULL,NULL,1417746904721,'-1','1',NULL,0,11,0,'WAITING_BILLING'),(30,0,'Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOY.   Cost is 5.0/- @ sms','254734606096','TM','32329',5,0,362,0,0,'2014-12-05 02:57:09','32329',NULL,0,'IOD','IOD0000','NOKIA2100',NULL,NULL,1417748228458,'-1','1',NULL,0,11,0,'WAITING_BILLING'),(31,0,'Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOB.   Cost is 5.0/- @ sms','254734606096','TM','32329',5,0,362,0,0,'2014-12-05 02:57:43','32329',NULL,0,'IOD','IOD0000','NOKIA2100',NULL,NULL,1417748262199,'-1','1',NULL,0,11,0,'WAITING_BILLING'),(41,0,'Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','254734606096','TM','32329',5,0,363,0,0,'2014-12-05 03:03:21','32329',NULL,0,'IOD','IOD0000','NOKIA2100',NULL,NULL,1417748600509,'-1','1',NULL,0,11,0,'WAITING_BILLING'),(47,0,'Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','254734606096','TM','32329',5,0,363,0,0,'2014-12-05 03:55:06','32329',NULL,0,'IOD','IOD0000','NOKIA2100',NULL,NULL,1417751704735,'-1','1',NULL,0,11,0,'WAITING_BILLING'),(48,0,'Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','254734606096','TM','32329',5,0,363,0,0,'2014-12-05 04:02:31','32329',NULL,0,'IOD','IOD0000','NOKIA2100',NULL,NULL,1417752150210,'-1','1',NULL,0,11,0,'WAITING_BILLING'),(52,0,'Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','254734606096','TM','32329',5,0,363,0,0,'2014-12-05 04:17:55','32329',NULL,0,'IOD','IOD0000','NOKIA2100',NULL,NULL,1417753074811,'-1','1',NULL,0,11,0,'WAITING_BILLING'),(56,0,'Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','254734606096','TM','32329',5,0,363,0,0,'2014-12-05 04:34:06','32329',NULL,0,'IOD','IOD0000','NOKIA2100',NULL,NULL,1417754045571,'-1','1',NULL,0,11,0,'WAITING_BILLING'),(57,0,'Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','254734606096','TM','32329',5,0,363,0,0,'2014-12-05 04:34:54','32329',NULL,0,'IOD','IOD0000','NOKIA2100',NULL,NULL,1417754094576,'-1','1',NULL,0,11,0,'WAITING_BILLING'),(58,0,'Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','254734606096','TM','32329',5,0,363,0,0,'2014-12-05 04:35:15','32329',NULL,0,'IOD','IOD0000','NOKIA2100',NULL,NULL,1417754115328,'-1','1',NULL,0,11,0,'WAITING_BILLING'),(59,0,'Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','254734606096','TM','32329',5,0,363,0,0,'2014-12-05 04:43:54','32329',NULL,0,'IOD','IOD0000','NOKIA2100',NULL,NULL,1417754633238,'-1','1',NULL,0,11,0,'WAITING_BILLING'),(62,0,'Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','254734606096','TM','32329',5,0,363,0,0,'2014-12-05 04:48:50','32329',NULL,0,'IOD','IOD0000','NOKIA2100',NULL,NULL,1417754930319,'-1','1',NULL,0,11,0,'WAITING_BILLING'),(63,0,'Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','254734606096','TM','32329',5,0,363,0,0,'2014-12-05 04:50:17','32329',NULL,0,'IOD','IOD0000','NOKIA2100',NULL,NULL,1417755016146,'-1','1',NULL,0,11,0,'WAITING_BILLING'),(64,0,'Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','254734606096','TM','32329',5,0,363,0,0,'2014-12-05 04:50:40','32329',NULL,0,'IOD','IOD0000','NOKIA2100',NULL,NULL,1417755040390,'-1','1',NULL,0,11,0,'WAITING_BILLING'),(65,0,'Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','254734606096','TM','32329',5,0,363,0,0,'2014-12-05 04:51:11','32329',NULL,0,'IOD','IOD0000','NOKIA2100',NULL,NULL,1417755070666,'-1','1',NULL,0,11,0,'WAITING_BILLING'),(68,0,'Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','254734606096','TM','32329',5,0,363,0,0,'2014-12-05 04:58:32','32329',NULL,0,'IOD','IOD0000','NOKIA2100',NULL,NULL,1417755510541,'-1','1',NULL,0,11,0,'WAITING_BILLING'),(69,0,'Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','254734606096','TM','32329',5,0,363,0,0,'2014-12-05 04:58:53','32329',NULL,0,'IOD','IOD0000','NOKIA2100',NULL,NULL,1417755532748,'-1','1',NULL,0,11,0,'WAITING_BILLING'),(71,0,'Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','254734606096','TM','32329',5,0,363,0,0,'2014-12-05 05:04:25','32329',NULL,0,'IOD','IOD0000','NOKIA2100',NULL,NULL,1417755863178,'-1','1',NULL,0,11,0,'WAITING_BILLING'),(72,0,'Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','254734606096','TM','32329',5,0,363,0,0,'2014-12-05 05:04:49','32329',NULL,0,'IOD','IOD0000','NOKIA2100',NULL,NULL,1417755887800,'-1','1',NULL,0,11,0,'WAITING_BILLING'),(80,0,'Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','254734606096','TM','32329',5,0,363,0,0,'2014-12-05 05:33:44','32329',NULL,0,'IOD','IOD0000','NOKIA2100',NULL,NULL,1417757621933,'-1','1',NULL,0,11,0,'WAITING_BILLING'),(81,0,'Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','254734606096','TM','32329',5,0,363,0,0,'2014-12-05 05:33:59','32329',NULL,0,'IOD','IOD0000','NOKIA2100',NULL,NULL,1417757638003,'-1','1',NULL,0,11,0,'WAITING_BILLING'),(82,0,'Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','254734606096','TM','32329',5,0,363,0,0,'2014-12-05 05:54:59','32329',NULL,0,'IOD','IOD0000','NOKIA2100',NULL,NULL,1417758896864,'-1','1',NULL,0,11,0,'WAITING_BILLING');
/*!40000 ALTER TABLE `httptosend` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `institution`
--

DROP TABLE IF EXISTS `institution`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `institution` (
  `id` int(11) NOT NULL,
  `name` varchar(60) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_qhw15h5f7nc4g3ndva8sory1u` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `institution`
--

LOCK TABLES `institution` WRITE;
/*!40000 ALTER TABLE `institution` DISABLE KEYS */;
/*!40000 ALTER TABLE `institution` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `interestrate`
--

DROP TABLE IF EXISTS `interestrate`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `interestrate` (
  `id` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `rate` double DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_g4uxg53hvn7v0fpyhnxeqcq6f` (`rate`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `interestrate`
--

LOCK TABLES `interestrate` WRITE;
/*!40000 ALTER TABLE `interestrate` DISABLE KEYS */;
/*!40000 ALTER TABLE `interestrate` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `keywords`
--

DROP TABLE IF EXISTS `keywords`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `keywords` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `keyword` varchar(145) DEFAULT NULL,
  `description` varchar(545) DEFAULT NULL,
  `price` double NOT NULL DEFAULT '0',
  `subscription_push_tail_text` varchar(100) NOT NULL DEFAULT 'U''ve received daily subscription sms. Reply STOP <KWD> to unsubscribe. SMS cost KES<PRICE>' COMMENT 'Whn a daily subsription push is done, we send along this text that tells subscriber the cost and how to unsubscribe.',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `keywords`
--

LOCK TABLES `keywords` WRITE;
/*!40000 ALTER TABLE `keywords` DISABLE KEYS */;
INSERT INTO `keywords` VALUES (1,'DEFAULT','test',0,'Test'),(2,'ENEWS','Entertainment news',10,'You have been subscribed to entertainment news'),(3,'HGOSSIP','Hollywood gossip',10,'You\'ve been subscribed to hollywood gossip'),(4,'HABARI','Swahili Habari',10,'Umesajiliwa kwa Habari');
/*!40000 ALTER TABLE `keywords` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `members`
--

DROP TABLE IF EXISTS `members`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `members` (
  `id` bigint(20) NOT NULL,
  `u_pwd` varchar(255) DEFAULT NULL,
  `role` int(11) DEFAULT NULL,
  `u_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `members`
--

LOCK TABLES `members` WRITE;
/*!40000 ALTER TABLE `members` DISABLE KEYS */;
/*!40000 ALTER TABLE `members` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `message`
--

DROP TABLE IF EXISTS `message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `message` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `language_id` int(2) DEFAULT NULL,
  `key` varchar(45) DEFAULT NULL,
  `message` text NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `lang_key` (`language_id`,`key`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `message`
--

LOCK TABLES `message` WRITE;
/*!40000 ALTER TABLE `message` DISABLE KEYS */;
INSERT INTO `message` VALUES (1,2,'UNKNOWN_KEYWORD_ADVICE','Choose a job you love, and you will never have to work a day in your life.');
/*!40000 ALTER TABLE `message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `messagelog`
--

DROP TABLE IF EXISTS `messagelog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `messagelog` (
  `id` bigint(21) NOT NULL AUTO_INCREMENT,
  `mo_processor_id_fk` int(11) NOT NULL DEFAULT '-1',
  `processing` tinyint(1) DEFAULT '0' COMMENT 'Tells us if the record is currently being processed.\n',
  `CMP_Txid` bigint(20) NOT NULL,
  `MO_Received` varchar(160) DEFAULT NULL COMMENT 'This is the SMS sent by the subscriber. SMS_Message_String',
  `MT_Sent` text,
  `MT_SendTime` timestamp NULL DEFAULT NULL,
  `SMS_SourceAddr` varchar(20) DEFAULT NULL COMMENT 'This is a sort of msisdn prefix.',
  `SUB_Mobtel` varchar(20) DEFAULT NULL,
  `SMS_DataCodingId` varchar(45) DEFAULT NULL,
  `CMPResponse` varchar(45) DEFAULT NULL,
  `APIType` enum('1','2','3','4','5','6','7') DEFAULT '1',
  `CMP_Keyword` varchar(45) DEFAULT NULL,
  `CMP_SKeyword` varchar(45) DEFAULT NULL,
  `timeStamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `mo_ack` tinyint(1) NOT NULL DEFAULT '0',
  `mt_ack` tinyint(1) NOT NULL DEFAULT '0',
  `MT_STATUS` enum('LimitSubscriberFailure','InvalidSubscriber','KeywordsNotFound','TariffCodeNotFound','Invalidshortcode','SetCMPConnectionFailure','ServicePriceNotFound','PSAInsufficientBalance','PSAInvalidNumber','PSANumberBarred','PSABusy','PSACreditExceeded','PSAChargeFailure','SCRNotInAllowList','SubscriberBlackListed','PCMSendFail','NegativeDN','Success','PCM200','PCM301','PCM302','PCM303','PCM304','PCM305','PCM306','PCM307','PCM400','PCM402','PCM403','PCM404','PCM405','PCM406','WaitingForDLR','FailedToSend') NOT NULL DEFAULT 'Success',
  `delivery_report_arrive_time` timestamp NULL DEFAULT NULL,
  `price` double NOT NULL DEFAULT '0',
  `serviceid` int(11) NOT NULL DEFAULT '-1',
  `number_of_sms` int(11) NOT NULL DEFAULT '1',
  `msg_was_split` tinyint(1) NOT NULL DEFAULT '0',
  `re_try_count` int(2) NOT NULL DEFAULT '0',
  `re_try` tinyint(1) DEFAULT '0',
  `newCMP_Txid` varchar(22) NOT NULL DEFAULT '-1',
  `event_type` enum('Content Purchase','Subscription Purchase','ReSubscription') NOT NULL DEFAULT 'Content Purchase',
  PRIMARY KEY (`id`),
  UNIQUE KEY `transaction_id` (`CMP_Txid`),
  KEY `timeStamp` (`timeStamp`),
  KEY `timeStamp_msisdn` (`SUB_Mobtel`,`timeStamp`),
  KEY `timeStamp_msisdn_transaction_id` (`timeStamp`,`SUB_Mobtel`,`CMP_Txid`),
  KEY `msisdn_transaction_id` (`CMP_Txid`,`SUB_Mobtel`),
  KEY `service_id` (`serviceid`),
  KEY `originalCMPTxid` (`newCMP_Txid`)
) ENGINE=InnoDB AUTO_INCREMENT=123 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `messagelog`
--

LOCK TABLES `messagelog` WRITE;
/*!40000 ALTER TABLE `messagelog` DISABLE KEYS */;
INSERT INTO `messagelog` VALUES (1,11,0,1417741966122,'Jobs',NULL,NULL,'32329','254734606096','0',NULL,NULL,'IOD','IOD0000','2014-12-05 01:12:45',1,0,'Success',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(2,11,0,1417742046410,'Jobs',NULL,NULL,'32329','254734606096','0',NULL,NULL,'IOD','IOD0000','2014-12-05 01:14:05',1,0,'Success',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(3,11,0,1417742563424,'Jobs',NULL,NULL,'32329','254734606096','0',NULL,NULL,'IOD','IOD0000','2014-12-05 01:22:42',1,0,'Success',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(4,11,0,1417742600730,'Jobs',NULL,NULL,'32329','254734606096','0',NULL,NULL,'IOD','IOD0000','2014-12-05 01:23:20',1,0,'Success',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(5,11,0,1417742635425,'Jobs',NULL,NULL,'32329','254734606096','0',NULL,NULL,'IOD','IOD0000','2014-12-05 01:23:54',1,0,'Success',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(6,11,0,1417743059169,'Jobs','Welcome to content360 content platform. This is an auto response message. 5.0/- @ sms','2014-12-05 14:31:07','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 01:30:58',1,0,'WaitingForDLR',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(8,11,0,1417743080316,'Jobs','Welcome to content360 content platform. This is an auto response message. 5.0/- @ sms','2014-12-05 14:31:27','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 01:31:19',1,0,'WaitingForDLR',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(10,11,0,1417743098213,'Jobs','Welcome to content360 content platform. This is an auto response message. 5.0/- @ sms','2014-12-05 14:31:42','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 01:31:37',1,0,'WaitingForDLR',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(12,11,0,1417743282777,'Jobs',NULL,NULL,'32329','254734606096','0',NULL,NULL,'IOD','IOD0000','2014-12-05 01:34:42',1,0,'Success',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(13,11,0,1417743328241,'Jobs',NULL,NULL,'32329','254734606096','0',NULL,NULL,'IOD','IOD0000','2014-12-05 01:35:27',1,0,'Success',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(14,11,0,1417743686906,'Jobs','Welcome to content360 content platform. This is an auto response message. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 14:41:36','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 01:41:26',1,0,'WaitingForDLR',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(16,11,0,1417743871804,'Jobs','Welcome to content360 content platform. This is an auto response message. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 14:44:40','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 01:44:31',1,0,'WaitingForDLR',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(18,11,0,1417744053421,'Jobs',NULL,NULL,'32329','254734606096','0',NULL,NULL,'IOD','IOD0000','2014-12-05 01:47:32',1,0,'Success',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(19,11,0,1417744085057,'Jobs','Welcome to content360 content platform. This is an auto response message. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 14:48:14','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 01:48:04',1,0,'WaitingForDLR',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(21,11,0,1417744284976,'Jobs',NULL,NULL,'32329','254734606096','0',NULL,NULL,'IOD','IOD0000','2014-12-05 01:51:24',1,0,'Success',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(22,11,0,1417744312370,'Jobs',NULL,NULL,'32329','254734606096','0',NULL,NULL,'IOD','IOD0000','2014-12-05 01:51:51',1,0,'Success',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(23,11,0,1417744994255,'Jobs','Welcome to content360 content platform. This is an auto response message. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 15:03:23','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 02:03:13',1,0,'WaitingForDLR',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(25,11,0,1417745300639,'Jobs',NULL,NULL,'32329','254734606096','0',NULL,NULL,'IOD','IOD0000','2014-12-05 02:08:19',1,0,'Success',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(26,11,0,1417745358665,'Jobs','Welcome to content360 content platform. This is an auto response message. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 15:09:27','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 02:09:17',1,0,'WaitingForDLR',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(28,11,0,1417746039448,'Jobs','Welcome to content360 content platform. This is an auto response message. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 15:20:48','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 02:20:38',1,0,'WaitingForDLR',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(30,11,0,1417746063582,'Jobs','Welcome to content360 content platform. This is an auto response message. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 15:21:09','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 02:21:02',1,0,'WaitingForDLR',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(32,11,0,1417746385889,'Jobs',NULL,NULL,'32329','254734606096','0',NULL,NULL,'IOD','IOD0000','2014-12-05 02:26:25',1,0,'Success',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(33,11,0,1417746552748,'Jobs','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 15:29:22','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 02:29:12',1,0,'WaitingForDLR',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(35,11,0,1417746570726,'Jobs','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 15:29:36','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 02:29:30',1,0,'WaitingForDLR',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(37,11,0,1417746685476,'Jobs',NULL,NULL,'32329','254734606096','0',NULL,NULL,'IOD','IOD0000','2014-12-05 02:31:24',1,0,'Success',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(38,11,0,1417746894246,'Jobs',NULL,NULL,'32329','254734606096','0',NULL,NULL,'IOD','IOD0000','2014-12-05 02:34:53',1,0,'Success',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(39,11,0,1417746904721,'Jobs',NULL,NULL,'32329','254734606096','0',NULL,NULL,'IOD','IOD0000','2014-12-05 02:35:04',1,0,'Success',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(40,11,0,1417748156923,'Jobs','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 15:56:05','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 02:55:56',1,0,'WaitingForDLR',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(42,11,0,1417748180925,'Jobs','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 15:56:27','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 02:56:20',1,0,'WaitingForDLR',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(44,11,0,1417748228458,'Joy',NULL,NULL,'32329','254734606096','0',NULL,NULL,'IOD','IOD0000','2014-12-05 02:57:07',1,0,'Success',NULL,5,362,1,0,0,0,'-1','Content Purchase'),(45,11,0,1417748262199,'Job',NULL,NULL,'32329','254734606096','0',NULL,NULL,'IOD','IOD0000','2014-12-05 02:57:41',1,0,'Success',NULL,5,362,1,0,0,0,'-1','Content Purchase'),(46,11,0,1417748300070,'Jobs','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 15:58:29','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 02:58:19',1,0,'WaitingForDLR',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(48,11,0,1417748321780,'Jobs','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 15:58:48','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 02:58:41',1,0,'WaitingForDLR',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(50,11,0,1417748343456,'Jobs','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 15:59:23','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 02:59:02',1,0,'WaitingForDLR',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(52,11,0,1417748375941,'Jobs','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 15:59:41','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 02:59:35',1,0,'WaitingForDLR',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(54,11,0,1417748413845,'Jobs','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 16:00:17','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 03:00:13',1,0,'WaitingForDLR',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(56,11,0,1417748450311,'Jobs','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 16:00:56','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 03:00:49',1,0,'WaitingForDLR',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(58,11,0,1417748533749,'Any','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in ANY.   Cost is 0.0/- @ sms','2014-12-05 16:02:20','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 03:02:13',1,0,'WaitingForDLR',NULL,0,362,1,0,0,0,'-1','Content Purchase'),(60,11,0,1417748576280,'My test keyword','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in MY.   Cost is 0.0/- @ sms','2014-12-05 16:02:59','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 03:02:55',1,0,'WaitingForDLR',NULL,0,362,1,0,0,0,'-1','Content Purchase'),(62,11,0,1417748600509,'Jobs',NULL,NULL,'32329','254734606096','0',NULL,NULL,'IOD','IOD0000','2014-12-05 03:03:19',1,0,'Success',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(63,11,0,1417750363727,'Jobs','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 17:15:06','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 03:32:42',1,0,'WaitingForDLR',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(64,11,0,1417750692612,'Jobs','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 17:15:07','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 03:38:11',1,0,'WaitingForDLR',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(65,11,0,1417751526356,'Hjobs','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in HJOBS.   Cost is 0.0/- @ sms','2014-12-05 16:52:12','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 03:52:05',1,0,'WaitingForDLR',NULL,0,362,1,0,0,0,'-1','Content Purchase'),(67,11,0,1417751542074,'Jobs','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 17:15:07','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 03:52:21',1,0,'WaitingForDLR',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(68,11,0,1417751704735,'Jobs',NULL,NULL,'32329','254734606096','0',NULL,NULL,'IOD','IOD0000','2014-12-05 03:55:03',1,0,'Success',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(69,11,0,1417752150210,'Jobs',NULL,NULL,'32329','254734606096','0',NULL,NULL,'IOD','IOD0000','2014-12-05 04:02:29',1,0,'Success',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(73,11,0,1417752978702,'Jobs','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 17:16:28','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 04:16:17',1,0,'WaitingForDLR',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(75,11,0,1417753062990,'Joba','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBA.   Cost is 0.0/- @ sms','2014-12-05 17:17:48','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 04:17:42',1,0,'WaitingForDLR',NULL,0,362,1,0,0,0,'-1','Content Purchase'),(77,11,0,1417753074811,'Jobs',NULL,NULL,'32329','254734606096','0',NULL,NULL,'IOD','IOD0000','2014-12-05 04:17:54',1,0,'Success',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(78,11,0,1417753950292,'Jobs','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 17:32:39','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 04:32:29',1,0,'WaitingForDLR',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(80,11,0,1417753967230,'Jobs','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 17:33:02','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 04:32:46',1,0,'WaitingForDLR',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(82,11,0,1417753997692,'Qwerty','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in QWERTY.   Cost is 0.0/- @ sms','2014-12-05 17:33:19','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 04:33:16',1,0,'WaitingForDLR',NULL,0,362,1,0,0,0,'-1','Content Purchase'),(84,11,0,1417754045571,'Jobs',NULL,NULL,'32329','254734606096','0',NULL,NULL,'IOD','IOD0000','2014-12-05 04:34:04',1,0,'Success',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(85,11,0,1417754094576,'Jobs',NULL,NULL,'32329','254734606096','0',NULL,NULL,'IOD','IOD0000','2014-12-05 04:34:53',1,0,'Success',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(86,11,0,1417754115328,'Jobs',NULL,NULL,'32329','254734606096','0',NULL,NULL,'IOD','IOD0000','2014-12-05 04:35:14',1,0,'Success',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(87,11,0,1417754633238,'Jobs',NULL,NULL,'32329','254734606096','0',NULL,NULL,'IOD','IOD0000','2014-12-05 04:43:52',1,0,'Success',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(88,11,0,1417754651889,'He','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in HE.   Cost is 0.0/- @ sms','2014-12-05 17:44:16','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 04:44:11',1,0,'WaitingForDLR',NULL,0,362,1,0,0,0,'-1','Content Purchase'),(90,11,0,1417754911191,'Jobs','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 17:48:39','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 04:48:30',1,0,'WaitingForDLR',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(92,11,0,1417754930319,'Jobs',NULL,NULL,'32329','254734606096','0',NULL,NULL,'IOD','IOD0000','2014-12-05 04:48:49',1,0,'Success',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(93,11,0,1417755016146,'Jobs',NULL,NULL,'32329','254734606096','0',NULL,NULL,'IOD','IOD0000','2014-12-05 04:50:15',1,0,'Success',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(94,11,0,1417755040390,'Jobs',NULL,NULL,'32329','254734606096','0',NULL,NULL,'IOD','IOD0000','2014-12-05 04:50:39',1,0,'Success',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(95,11,0,1417755070666,'Jobs',NULL,NULL,'32329','254734606096','0',NULL,NULL,'IOD','IOD0000','2014-12-05 04:51:09',1,0,'Success',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(96,11,0,1417755419508,'Jobs','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 17:57:08','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 04:56:58',1,0,'WaitingForDLR',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(98,11,0,1417755436495,'Jobs','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 17:57:22','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 04:57:15',1,0,'WaitingForDLR',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(100,11,0,1417755510541,'Jobs',NULL,NULL,'32329','254734606096','0',NULL,NULL,'IOD','IOD0000','2014-12-05 04:58:29',1,0,'Success',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(101,11,0,1417755532748,'Jobs',NULL,NULL,'32329','254734606096','0',NULL,NULL,'IOD','IOD0000','2014-12-05 04:58:51',1,0,'Success',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(102,11,0,1417755729104,'Jobs','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 18:02:19','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 05:02:09',1,0,'WaitingForDLR',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(104,11,0,1417755863178,'Jobs',NULL,NULL,'32329','254734606096','0',NULL,NULL,'IOD','IOD0000','2014-12-05 05:04:23',1,0,'Success',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(105,11,0,1417755887800,'Jobs',NULL,NULL,'32329','254734606096','0',NULL,NULL,'IOD','IOD0000','2014-12-05 05:04:48',1,0,'Success',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(106,11,0,1417756330669,'Jobs','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 18:12:20','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 05:12:10',1,0,'WaitingForDLR',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(108,11,0,1417756346455,'Jobs','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 18:12:32','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 05:12:26',1,0,'WaitingForDLR',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(110,11,0,1417756360017,'Jobs','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 18:12:44','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 05:12:40',1,0,'WaitingForDLR',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(112,11,0,1417756376390,'Jobs','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 18:13:01','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 05:12:56',1,0,'WaitingForDLR',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(114,11,0,1417756389431,'Jobs','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 18:13:15','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 05:13:09',1,0,'WaitingForDLR',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(116,11,0,1417756407640,'Jobs','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 18:13:32','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 05:13:27',1,0,'WaitingForDLR',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(118,11,0,1417756438089,'Jobs','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 18:14:03','32329','254734606096','0','Success','1','IOD','IOD0000','2014-12-05 05:13:58',1,0,'WaitingForDLR',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(120,11,0,1417757621933,'Jobs',NULL,NULL,'32329','254734606096','0',NULL,NULL,'IOD','IOD0000','2014-12-05 05:33:42',1,0,'Success',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(121,11,0,1417757638003,'Jobs',NULL,NULL,'32329','254734606096','0',NULL,NULL,'IOD','IOD0000','2014-12-05 05:33:58',1,0,'Success',NULL,5,363,1,0,0,0,'-1','Subscription Purchase'),(122,11,0,1417758896864,'Jobs',NULL,NULL,'32329','254734606096','0',NULL,NULL,'IOD','IOD0000','2014-12-05 05:54:57',1,0,'Success',NULL,5,363,1,0,0,0,'-1','Subscription Purchase');
/*!40000 ALTER TABLE `messagelog` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mms_log`
--

DROP TABLE IF EXISTS `mms_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `mms_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `txID` varchar(45) NOT NULL DEFAULT '0000000000000000000',
  `msisdn` varchar(45) NOT NULL DEFAULT '00000000',
  `subject` varchar(45) NOT NULL DEFAULT 'MMS Subject',
  `mms_text` varchar(160) NOT NULL DEFAULT 'MMS Text',
  `media_path` varchar(260) NOT NULL DEFAULT '/home/inmobia/jobs/celcom/mms/sample.png' COMMENT 'The url to the content. ',
  `serviceid` int(10) NOT NULL DEFAULT '0',
  `shortcode` varchar(10) NOT NULL DEFAULT '23355',
  `servicecode` enum('MMSCMPMMS0000','MMSCMPMMS0100') NOT NULL DEFAULT 'MMSCMPMMS0000',
  `linked_id` varchar(45) NOT NULL DEFAULT '00000',
  `earliest_delivery_time` timestamp NOT NULL DEFAULT '1970-01-02 04:59:59',
  `expiry_date` timestamp NOT NULL DEFAULT '1970-01-02 04:59:59',
  `delivery_report_requested` tinyint(1) NOT NULL DEFAULT '1',
  `distributable` tinyint(1) NOT NULL DEFAULT '0',
  `timeStampOfInsertion` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `DLRArriveTimeStamp` timestamp NOT NULL DEFAULT '1970-01-02 04:59:59',
  `DLRReportStatus` varchar(35) NOT NULL DEFAULT 'SENT_WAITING_DLR',
  `sent` tinyint(1) NOT NULL DEFAULT '0',
  `mt_ack` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `linkedID` (`linked_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mms_log`
--

LOCK TABLES `mms_log` WRITE;
/*!40000 ALTER TABLE `mms_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `mms_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mms_to_send`
--

DROP TABLE IF EXISTS `mms_to_send`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `mms_to_send` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `msisdn` varchar(45) NOT NULL DEFAULT '00000000',
  `subject` varchar(45) NOT NULL DEFAULT 'MMS Subject',
  `mms_text` varchar(160) NOT NULL DEFAULT 'MMS Text',
  `media_path` varchar(260) NOT NULL DEFAULT '/home/inmobia/jobs/celcom/mms/sample.png' COMMENT 'The url to the content. ',
  `serviceid` int(10) NOT NULL DEFAULT '0',
  `shortcode` varchar(10) NOT NULL DEFAULT '23355',
  `servicecode` enum('MMSCMPMMS0000','MMSCMPMMS0100') NOT NULL DEFAULT 'MMSCMPMMS0000',
  `linked_id` varchar(45) NOT NULL DEFAULT '00000',
  `earliest_delivery_time` timestamp NOT NULL DEFAULT '1970-01-02 04:59:59',
  `expiry_date` timestamp NOT NULL DEFAULT '1970-01-02 04:59:59',
  `delivery_report_requested` tinyint(1) NOT NULL DEFAULT '1',
  `distributable` tinyint(1) NOT NULL DEFAULT '0',
  `timeStampOfInsertion` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `inProcessingQueue` tinyint(1) NOT NULL DEFAULT '0',
  `sent` tinyint(1) NOT NULL DEFAULT '0',
  `tx_id_waiting_to_succeed_before_sending` varchar(21) NOT NULL DEFAULT '-1',
  `paidFor` tinyint(1) NOT NULL DEFAULT '0',
  `billingStatus` enum('LimitSubscriberFailure','InvalidSubscriber','KeywordsNotFound','TariffCodeNotFound','Invalidshortcode','SetCMPConnectionFailure','ServicePriceNotFound','PSAInsufficientBalance','PSAInvalidNumber','PSANumberBarred','PSABusy','PSACreditExceeded','PSAChargeFailure','SCRNotInAllowList','SubscriberBlackListed','PCMSendFail','NegativeDN','Success','PCM200','PCM301','PCM302','PCM303','PCM304','PCM305','PCM306','PCM307','PCM400','PCM402','PCM403','PCM404','PCM405','PCM406','WaitingForDLR') NOT NULL DEFAULT 'WaitingForDLR',
  `dlrReceived` timestamp NOT NULL DEFAULT '1970-01-01 16:29:59',
  PRIMARY KEY (`id`),
  KEY `sent_idx` (`sent`),
  KEY `inProcessQueue_idx` (`inProcessingQueue`),
  KEY `sent_process_idx` (`inProcessingQueue`,`sent`),
  KEY `tx_id_idx` (`tx_id_waiting_to_succeed_before_sending`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mms_to_send`
--

LOCK TABLES `mms_to_send` WRITE;
/*!40000 ALTER TABLE `mms_to_send` DISABLE KEYS */;
/*!40000 ALTER TABLE `mms_to_send` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mo_processors`
--

DROP TABLE IF EXISTS `mo_processors`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `mo_processors` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ServiceName` varchar(45) DEFAULT NULL,
  `shortcode` varchar(45) DEFAULT NULL,
  `threads` int(2) DEFAULT '1' COMMENT 'Holds the number of processor threads required for the given service',
  `ProcessorClass` varchar(100) DEFAULT 'com.inmobia.celcom.GenericServiceProcessor',
  `enabled` tinyint(1) DEFAULT '1',
  `class_status` enum('CLASS_NOT_LOADED_YET','CLASS_NOT_FOUND','CLASS_THROWS_EXCEPTION','LOADED') DEFAULT 'CLASS_NOT_LOADED_YET',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=latin1 COMMENT='This table holds the processor classes for various services. Things like processor class, description and price can be set here...';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mo_processors`
--

LOCK TABLES `mo_processors` WRITE;
/*!40000 ALTER TABLE `mo_processors` DISABLE KEYS */;
INSERT INTO `mo_processors` VALUES (11,'Hello','32329',2,'com.pixelandtag.serviceprocessors.sms.Content360UnknownKeyword',1,'CLASS_NOT_LOADED_YET');
/*!40000 ALTER TABLE `mo_processors` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `raw_out_log`
--

DROP TABLE IF EXISTS `raw_out_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `raw_out_log` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `msisdn` varchar(160) NOT NULL,
  `response` text NOT NULL,
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `msisdn` (`msisdn`),
  KEY `timeStamp` (`timestamp`),
  KEY `msisdn_timestamp` (`msisdn`,`timestamp`)
) ENGINE=InnoDB AUTO_INCREMENT=2006 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `raw_out_log`
--

LOCK TABLES `raw_out_log` WRITE;
/*!40000 ALTER TABLE `raw_out_log` DISABLE KEYS */;
INSERT INTO `raw_out_log` VALUES (1952,'254734252504','From platform','2014-10-13 12:24:14'),(1953,'254734252504','Welcome to content360 content platform. This is an auto response message.','2014-12-04 15:56:36'),(1954,'254734252504','Welcome to content360 content platform. This is an auto response message.','2014-12-04 16:37:25'),(1955,'254734252504','Welcome to content360 content platform. This is an auto response message.','2014-12-04 16:38:27'),(1956,'254734252504','Welcome to content360 content platform. This is an auto response message.','2014-12-04 16:43:37'),(1957,'254734252504','Welcome to content360 content platform. This is an auto response message.','2014-12-04 17:25:29'),(1958,'254734606096','Welcome to content360 content platform. This is an auto response message.','2014-12-04 22:37:20'),(1959,'254734606096','Welcome to content360 content platform. This is an auto response message.Tail Text not subscribed','2014-12-05 00:35:31'),(1960,'254734606096','Welcome to content360 content platform. This is an auto response message.Tail Text not subscribed','2014-12-05 00:45:19'),(1961,'254734606096','Welcome to content360 content platform. This is an auto response message. 5.0/- @ sms','2014-12-05 00:55:56'),(1962,'254734606096','Welcome to content360 content platform. This is an auto response message. 5.0/- @ sms','2014-12-05 00:56:28'),(1963,'254734606096','Welcome to content360 content platform. This is an auto response message. 5.0/- @ sms','2014-12-05 01:31:07'),(1964,'254734606096','Welcome to content360 content platform. This is an auto response message. 5.0/- @ sms','2014-12-05 01:31:28'),(1965,'254734606096','Welcome to content360 content platform. This is an auto response message. 5.0/- @ sms','2014-12-05 01:31:42'),(1966,'254734606096','Welcome to content360 content platform. This is an auto response message. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 01:41:36'),(1967,'254734606096','Welcome to content360 content platform. This is an auto response message. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 01:44:40'),(1968,'254734606096','Welcome to content360 content platform. This is an auto response message. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 01:48:14'),(1969,'254734606096','Welcome to content360 content platform. This is an auto response message. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 02:03:23'),(1970,'254734606096','Welcome to content360 content platform. This is an auto response message. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 02:09:27'),(1971,'254734606096','Welcome to content360 content platform. This is an auto response message. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 02:20:48'),(1972,'254734606096','Welcome to content360 content platform. This is an auto response message. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 02:21:09'),(1973,'254734606096','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 02:29:22'),(1974,'254734606096','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 02:29:36'),(1975,'254734606096','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 02:56:06'),(1976,'254734606096','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 02:56:27'),(1977,'254734606096','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 02:58:29'),(1978,'254734606096','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 02:58:48'),(1979,'254734606096','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 02:59:23'),(1980,'254734606096','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 02:59:41'),(1981,'254734606096','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 03:00:18'),(1982,'254734606096','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 03:00:56'),(1983,'254734606096','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in ANY.   Cost is 0.0/- @ sms','2014-12-05 03:02:20'),(1984,'254734606096','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in MY.   Cost is 0.0/- @ sms','2014-12-05 03:02:59'),(1985,'254734606096','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in HJOBS.   Cost is 0.0/- @ sms','2014-12-05 03:52:12'),(1986,'254734606096','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 04:15:06'),(1987,'254734606096','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 04:15:07'),(1988,'254734606096','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 04:15:08'),(1989,'254734606096','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 04:16:29'),(1990,'254734606096','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBA.   Cost is 0.0/- @ sms','2014-12-05 04:17:48'),(1991,'254734606096','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 04:32:39'),(1992,'254734606096','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 04:33:02'),(1993,'254734606096','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in QWERTY.   Cost is 0.0/- @ sms','2014-12-05 04:33:19'),(1994,'254734606096','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in HE.   Cost is 0.0/- @ sms','2014-12-05 04:44:16'),(1995,'254734606096','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 04:48:39'),(1996,'254734606096','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 04:57:08'),(1997,'254734606096','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 04:57:22'),(1998,'254734606096','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 05:02:19'),(1999,'254734606096','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 05:12:20'),(2000,'254734606096','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 05:12:32'),(2001,'254734606096','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 05:12:45'),(2002,'254734606096','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 05:13:02'),(2003,'254734606096','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 05:13:15'),(2004,'254734606096','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 05:13:32'),(2005,'254734606096','Choose a job you love, and you will never have to work a day in your life. Thank you for your interest in JOBS.   Cost is 5.0/- @ sms','2014-12-05 05:14:03');
/*!40000 ALTER TABLE `raw_out_log` ENABLE KEYS */;
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
INSERT INTO `role` VALUES (1,'administrator'),(2,'user');
/*!40000 ALTER TABLE `role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `servicesubscription`
--

DROP TABLE IF EXISTS `servicesubscription`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `servicesubscription` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `serviceid` int(11) NOT NULL DEFAULT '0',
  `schedule` datetime DEFAULT NULL,
  `lastUpdated` datetime DEFAULT NULL,
  `ExpiryDate` datetime DEFAULT '2025-01-01 00:00:00',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `servicesubscription`
--

LOCK TABLES `servicesubscription` WRITE;
/*!40000 ALTER TABLE `servicesubscription` DISABLE KEYS */;
/*!40000 ALTER TABLE `servicesubscription` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sms_service`
--

DROP TABLE IF EXISTS `sms_service`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sms_service` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `mo_processorFK` int(11) DEFAULT NULL,
  `cmd` varchar(160) DEFAULT NULL,
  `push_unique` int(1) NOT NULL DEFAULT '0' COMMENT 'Tels the subscription whether we should pass each subscriber''s msisdn through the processor class to get unique response. if 0,  then a generic msisdn is created, and the MOsms dto retrieved will be used to push to all the subscribers for the given service.',
  `service_name` varchar(45) DEFAULT NULL,
  `service_description` varchar(45) DEFAULT NULL,
  `price` double NOT NULL DEFAULT '0',
  `CMP_Keyword` varchar(45) NOT NULL DEFAULT 'IOD',
  `CMP_SKeyword` varchar(45) NOT NULL DEFAULT 'IOD0000',
  `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'Flag to denote whether the keyword is enabled or not. If value is 1, then sms service is enabled, else it''s not enabled.',
  `split_mt` tinyint(1) NOT NULL DEFAULT '0',
  `subscriptionText` varchar(145) NOT NULL DEFAULT 'Congratulations! You are now subscribed to this service.' COMMENT 'Message added to the content text after a subscriber subscribes to the service for the first time.',
  `unsubscriptionText` varchar(145) NOT NULL DEFAULT 'Thank you for having subscribed to our service. Your subscription to this service has now been stopped' COMMENT 'A text sent to the subscriber when they unsubscribe from the service. This could be a message to encourage the subscriber to subscribe again, or to more services.',
  `tailText_subscribed` varchar(145) NOT NULL DEFAULT 'To unsubscribe, please reply with "STOP"' COMMENT 'A message that is always appended to the end of the content SMS if the receiver is already subscribed to the service. This could hold information on how to unsubscribe.',
  `tailText_notsubscribed` varchar(145) NOT NULL DEFAULT 'To confirm your subscription, reply with "BUY"' COMMENT 'A message that is always appended to the end of the content SMS if the receiver is not subscribed yet. This informs the subscriber what to do in order to confirm their subscription.',
  `event_type` enum('Content Purchase','Subscription Purchase','ReSubscription') NOT NULL DEFAULT 'Content Purchase',
  PRIMARY KEY (`id`),
  KEY `cmd_idx` (`cmd`),
  KEY `mo_processor_fk_idx` (`mo_processorFK`)
) ENGINE=InnoDB AUTO_INCREMENT=364 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sms_service`
--

LOCK TABLES `sms_service` WRITE;
/*!40000 ALTER TABLE `sms_service` DISABLE KEYS */;
INSERT INTO `sms_service` VALUES (362,11,'DEFAULT',0,'Default Processor','Default processor',0,'IOD','IOD0000',1,0,'You\'ve been subscribed','You\'ve been unsubscribed','Tail Text subscribed.','Thank you for your interest in <KEYWORD>.   Cost is <PRICE>/- @ sms','Content Purchase'),(363,11,'JOBS',0,'Jobs. by default','Jobs. by default',5,'IOD','IOD0000',1,0,'You\'ve successfully subscribed to <KEYWORD>. Reply STOP <KEYWORD> to unsubscribe. <PRICE>/- @ sms','You\'ve been unsubscribed from JOBS. To subscribe for more interesting content, reply with FUN. <PRICE>/- @ sms','To unsubscribe, reply STOP <KEYWORD>','Thank you for your interest in <KEYWORD>.   Cost is <PRICE>/- @ sms','Subscription Purchase');
/*!40000 ALTER TABLE `sms_service` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sms_service_metadata`
--

DROP TABLE IF EXISTS `sms_service_metadata`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sms_service_metadata` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sms_service_id_fk` int(11) NOT NULL DEFAULT '0',
  `meta_field` varchar(45) NOT NULL DEFAULT 'default',
  `meta_value` varchar(512) NOT NULL DEFAULT 'default',
  PRIMARY KEY (`id`),
  UNIQUE KEY `sms_service_meta_field_idx` (`sms_service_id_fk`,`meta_field`),
  KEY `sms_service_id_fk_idx` (`sms_service_id_fk`),
  KEY `meta_field` (`meta_field`)
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
-- Table structure for table `smsstatlog`
--

DROP TABLE IF EXISTS `smsstatlog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `smsstatlog` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `SMSServiceID` int(11) NOT NULL,
  `timeStamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `msisdn` varchar(45) DEFAULT NULL,
  `charged` tinyint(4) NOT NULL DEFAULT '0',
  `transactionID` varchar(45) NOT NULL,
  `statusCode` enum('LimitSubscriberFailure','InvalidSubscriber','KeywordsNotFound','TariffCodeNotFound','Invalidshortcode','SetCMPConnectionFailure','ServicePriceNotFound','PSAInsufficientBalance','PSAInvalidNumber','PSANumberBarred','PSABusy','PSACreditExceeded','PSAChargeFailure','SCRNotInAllowList','SubscriberBlackListed','PCMSendFail','NegativeDN','Success','PCM200','PCM301','PCM302','PCM303','PCM304','PCM305','PCM306','PCM307','PCM400','PCM402','PCM403','PCM404','PCM405','PCM406','WaitingForDLR') NOT NULL DEFAULT 'WaitingForDLR',
  `subscription` tinyint(4) NOT NULL DEFAULT '0',
  `CMP_Keyword` varchar(45) NOT NULL,
  `CMP_SKeyword` varchar(45) NOT NULL,
  `price` double NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `transactionID` (`transactionID`),
  KEY `timeStamp` (`timeStamp`),
  KEY `msisdn` (`msisdn`),
  KEY `cmp_keywords` (`CMP_Keyword`,`CMP_SKeyword`),
  KEY `cmpKeyWord` (`CMP_Keyword`),
  KEY `cmpSKeyword` (`CMP_SKeyword`),
  KEY `statusCode_idx` (`statusCode`),
  KEY `price` (`price`),
  KEY `price_statusCod_idx` (`price`,`statusCode`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `smsstatlog`
--

LOCK TABLES `smsstatlog` WRITE;
/*!40000 ALTER TABLE `smsstatlog` DISABLE KEYS */;
/*!40000 ALTER TABLE `smsstatlog` ENABLE KEYS */;
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
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `subscription_status` enum('waiting_confirmation','confirmed','unsubscribed') NOT NULL DEFAULT 'waiting_confirmation',
  `sms_service_id_fk` int(11) NOT NULL,
  `msisdn` varchar(45) NOT NULL DEFAULT '0000000000',
  `subscription_timeStamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'When subscriber subscribed',
  `smsmenu_levels_id_fk` int(11) NOT NULL DEFAULT '-1' COMMENT 'Links to celcom_static_content.smsmenu_levels. Easy to retrieve the subscription name.',
  `request_medium` enum('sms','wap') NOT NULL DEFAULT 'sms' COMMENT 'Denotes the source of this subscription entry. It can be WAP or SMS. By default we expect it to be SMS. ',
  PRIMARY KEY (`id`),
  UNIQUE KEY `status_serviceid_msisdn_uniq_idx` (`sms_service_id_fk`,`msisdn`),
  KEY `sms_service_idx` (`sms_service_id_fk`),
  KEY `subscription_status_idx` (`subscription_status`)
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
-- Table structure for table `surrogatemmslog`
--

DROP TABLE IF EXISTS `surrogatemmslog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `surrogatemmslog` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `txID` varchar(55) DEFAULT NULL,
  `msisdn` varchar(45) DEFAULT NULL,
  `Status` varchar(55) DEFAULT NULL,
  `timeStamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `surrogatemmslog`
--

LOCK TABLES `surrogatemmslog` WRITE;
/*!40000 ALTER TABLE `surrogatemmslog` DISABLE KEYS */;
/*!40000 ALTER TABLE `surrogatemmslog` ENABLE KEYS */;
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
  `role` int(11) DEFAULT NULL,
  `u_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'root',1,'root');
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
INSERT INTO `user_role` VALUES (1,1);
/*!40000 ALTER TABLE `user_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `zero_billing_list`
--

DROP TABLE IF EXISTS `zero_billing_list`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `zero_billing_list` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `msisdn` varchar(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `zero_billing_list`
--

LOCK TABLES `zero_billing_list` WRITE;
/*!40000 ALTER TABLE `zero_billing_list` DISABLE KEYS */;
INSERT INTO `zero_billing_list` VALUES (1,'254734252504');
/*!40000 ALTER TABLE `zero_billing_list` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2014-12-05 12:39:56
