INSERT INTO `pixeland_content360`.`smsmenu_levels` (`name`, `language_id`, `parent_level_id`, `menu_id`, `serviceid`, `visible`, `ussdTag`) VALUES ('1. 5/- per Day', '1', '151', '2', '439', '1', '*50');
INSERT INTO `pixeland_content360`.`smsmenu_levels` (`name`, `language_id`, `parent_level_id`, `menu_id`, `serviceid`, `visible`, `ussdTag`) VALUES ('2. 15/- per Day', '1', '151', '2', '440', '1', '*51');
INSERT INTO `pixeland_content360`.`smsmenu_levels` (`name`, `language_id`, `parent_level_id`, `menu_id`, `serviceid`, `visible`, `ussdTag`) VALUES ('3. 30/- per Month', '1', '151', '2', '441', '1', '*52');
INSERT INTO `pixeland_content360`.`smsmenu_levels` (`name`, `language_id`, `parent_level_id`, `menu_id`, `serviceid`, `visible`, `ussdTag`) VALUES ('Subscription Options', '1', '-1', '2', '-1', '1', '*100');


UPDATE `pixeland_content360`.`smsmenu_levels` SET `name`='Purchase Chat bundles' WHERE `id`='151';
INSERT INTO `pixeland_content360`.`smsmenu_levels` (`name`, `language_id`, `parent_level_id`, `menu_id`, `serviceid`, `visible`, `ussdTag`) VALUES ('Find friend near you', '1', '-1', '2', '-1', '1', '*200');
UPDATE `pixeland_content360`.`smsmenu_levels` SET `name`='5/- per Day' WHERE `id`='148';
UPDATE `pixeland_content360`.`smsmenu_levels` SET `name`='15/- per Day' WHERE `id`='149';
UPDATE `pixeland_content360`.`smsmenu_levels` SET `name`='30/- per Month' WHERE `id`='150';


INSERT INTO `pixeland_content360`.`sms_service` (`mo_processorFK`, `cmd`, `push_unique`, `price`, `price_point_keyword`, `CMP_Keyword`, `CMP_SKeyword`, `enabled`, `split_mt`, `subscription_length`, `subscription_length_time_unit`) VALUES ('14', 'FIND', '1', '0', 'MAPENZI', 'IOD', 'IOD0000', '1', '0', '1', 'DAY');
INSERT INTO `pixeland_content360`.`sms_service` (`mo_processorFK`, `cmd`, `push_unique`, `price`, `price_point_keyword`, `CMP_Keyword`, `CMP_SKeyword`, `subscription_length`, `subscription_length_time_unit`) VALUES ('14', 'TAFUTA', '1', '0', 'MAPENZI', 'IOD', 'IOD0000', '1', 'DAY');


UPDATE `pixeland_content360`.`smsmenu_levels` SET `serviceid`='442' WHERE `id`='152';



delete from dating_profile where person_id_fk = (select id from dating_person where msisdn='254735594326');
delete from dating_person where msisdn='254735594326';
delete from subscription  where msisdn='254735594326';


