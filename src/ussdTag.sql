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



delete from `pixeland_content360`.`dating_profileloc` where profile_id_fk = (select id from dating_profile where person_id_fk = (select id from dating_person where msisdn='254735594326'));
delete from dating_profile where person_id_fk = (select id from dating_person where msisdn='254735594326');
delete from dating_person where msisdn='254735594326';
delete from subscription  where msisdn='254735594326';


delete from dating_profile where person_id_fk = (select id from dating_person where msisdn='254733660220');
delete from dating_person where msisdn='254733660220';
delete from subscription  where msisdn='254733660220';

delete from dating_profile where person_id_fk = (select id from dating_person where msisdn='254735594326');
delete from dating_person where msisdn='254735594326';
delete from subscription  where msisdn='254735594326';


UPDATE `pixeland_content360`.`message` SET `message`='Sorry <USERNAME>. Please top up & renew your <SERVICE_NAME> subscription to continue chatting with <DEST_USERNAME>. Dial *329# to subscribe to chat bundles.' WHERE `id`='41';
UPDATE `pixeland_content360`.`message` SET `message`='Sorry <USERNAME>. Please top up & renew your <SERVICE_NAME> subscription to continue chatting with <DEST_USERNAME>. Dial *329# to subscribe to chat bundles.' WHERE `id`='42';
UPDATE `pixeland_content360`.`message` SET `message`='Sorry, Please renew your subscription to continue enjoying enjoying the service. Dial *329# to purchase chat bundle.' WHERE `id`='37';
UPDATE `pixeland_content360`.`message` SET `message`='Sorry, Please renew your subscription to continue enjoying enjoying the service. Dial *329# to purchase chat bundle.' WHERE `id`='38';


insert into dating_disallowedwords select next_val+1,'I' from hibernate_sequence;
update hibernate_sequence set next_val=next_val+1;

insert into dating_disallowedwords select next_val+1,'AM' from hibernate_sequence;
update hibernate_sequence set next_val=next_val+1;

insert into dating_disallowedwords select next_val+1,'HE' from hibernate_sequence;
update hibernate_sequence set next_val=next_val+1;

insert into dating_disallowedwords select next_val+1,'SHE' from hibernate_sequence;
update hibernate_sequence set next_val=next_val+1;

insert into dating_disallowedwords select next_val+1,'AND' from hibernate_sequence;
update hibernate_sequence set next_val=next_val+1;

insert into dating_disallowedwords select next_val+1,'STOP' from hibernate_sequence;
update hibernate_sequence set next_val=next_val+1;

insert into dating_disallowedwords select next_val+1,'WHERE' from hibernatde_sequence;
update hibernate_sequence set next_val=next_val+1;

insert into dating_disallowedwords select next_val+1,'WANT' from hibernate_sequence;
update hibernate_sequence set next_val=next_val+1;

insert into dating_disallowedwords select next_val+1,'UNSUBSCRIBED' from hibernate_sequence;
update hibernate_sequence set next_val=next_val+1;

insert into dating_disallowedwords select next_val+1,'ON' from hibernate_sequence;
update hibernate_sequence set next_val=next_val+1;

insert into dating_disallowedwords select next_val+1,'DATE' from hibernate_sequence;
update hibernate_sequence set next_val=next_val+1;

insert into dating_disallowedwords select next_val+1,'IUNSUBSCRIBED' from hibernate_sequence;
update hibernate_sequence set next_val=next_val+1;

insert into dating_disallowedwords select next_val+1,'HOW' from hibernate_sequence;
update hibernate_sequence set next_val=next_val+1;


insert into subscription_history select next_val+1,2,'254736338689','439','2015-05-02 08:49:27' from hibernate_sequence;update hibernate_sequence set next_val=next_val+1;



select count(*), date(timeStamp) ts, hour(now()) from messagelog WHERe hour(TimeStamp)<=hour(now()) group by ts order by ts asc;

select count(*), date(timeStamp) ts from messagelog  group by ts order by ts asc;
select count(*),profileComplete ,date(creationDate) dt from dating_profile group by profileComplete,dt;
select count(*),profileComplete from dating_profile group by profileComplete;
select count(*), date(timeStamp) ts from dating_chatlog where hour(timeStamp)<=hour(now()) group by ts order by ts asc;

select count(distinct SUB_Mobtel) from messagelog;



select count(*) from subscription where subscription_status='confirmed' and date(expiryDate)<=date(now()) and queue_status=2;

select cl.timeStamp, concat(sp.msisdn,' > ', dp.msisdn ,': ', cl.message) from dating_chatlog cl left join dating_person sp on sp.id=cl.source_person_id LEFT JOIN dating_person dp ON dp.id=cl.dest_person_id where date(timeStamp)>=date(now()) order by timeStamp asc limit 1000;

select count(*), date(timeStamp) ts from dating_chatlog group by ts order by ts asc; 


select success,processed,in_outgoing_queue,count(*) c, price, count(*)*price as 'revenue' from billable_queue where date(timeStamp)=curdate() group by success,in_outgoing_queue,processed;

/*add these when testing 14th April*/
INSERT INTO `pixeland_content360`.`sms_service` (`mo_processorFK`, `cmd`, `push_unique`, `service_name`, `service_description`, `price`, `price_point_keyword`, `CMP_Keyword`, `CMP_SKeyword`, `enabled`, `split_mt`) VALUES (14, 'BUNDLES', 1, 'Chat bundles', 'Chat bundles', 0, '32329_MAPENZI', 'IOD', 'IOD0000', 1, 0);
INSERT INTO `pixeland_content360`.`sms_service_metadata` (`sms_service_id_fk`, `meta_field`, `meta_value`) VALUES (444, 'db_name', 'pixeland_content360');
UPDATE `pixeland_content360`.`smsmenu_levels` SET `serviceid`=444 WHERE `id`='151';
UPDATE `pixeland_content360`.`smsmenu_levels` SET `serviceid`=-1 WHERE `id`='151';
UPDATE `pixeland_content360`.`smsmenu_levels` SET `serviceid`=444 WHERE `id`='151';




DELIMITER $
CREATE PROCEDURE unsubscribeAll(
   msisdn_ varchar(64)
 )
BEGIN
	DECLARE dp_id INT DEFAULT -1;
	DECLARE prof_id INT DEFAULT -1;
	SELECT dp_id as 'profile_id');
    SELECT dp_id =  id from dating_person where msisdn=msisdn_;
	SELECT dp_id as 'profile_id af');
    SELECT prof_id = id from dating_profile where person_id_fk = dp_id;
	update pixeland_content360.dating_person set active=0 where msisdn=msisdn_;
	update pixeland_content360.subscription set subscription_status='unsubscribed' where msisdn=msisdn_;
END;
$
DELIMITER ;


DELIMITER $
CREATE PROCEDURE showSubscribed(
   msisdn_ varchar(64)
 )
BEGIN
    select * from pixeland_content360.subscription s where s.subscription_status='confirmed' AND s.msisdn=msisdn_;
END;
$
DELIMITER ;


select count(*),profileComplete from dating_profile group by profileComplete;

insert into role select next_val+1, 'superuser' from hibernate_sequence;update hibernate_sequence set next_val=next_val+1;
insert into role select next_val+1, 'usermanagement' from hibernate_sequence;update hibernate_sequence set next_val=next_val+1;
insert into role select next_val+1, 'contentmanagement' from hibernate_sequence;update hibernate_sequence set next_val=next_val+1;
insert into role select next_val+1, 'customercare' from hibernate_sequence;update hibernate_sequence set next_val=next_val+1;
insert into role select next_val+1, 'subscriptionmanagement' from hibernate_sequence;update hibernate_sequence set next_val=next_val+1;
insert into role select next_val+1, 'statistics' from hibernate_sequence;update hibernate_sequence set next_val=next_val+1;

insert into user select next_val+1,'csd@airtel.co.ke',1,'csd@airtel.co.ke','csd@airtel.co.ke','Csd123' from hibernate_sequence;update hibernate_sequence set next_val=next_val+1;

insert into user select next_val+1,'francis@content360.co.ke',1,'francis@content360.co.ke','francis@content360.co.ke','Francis123' from hibernate_sequence;update hibernate_sequence set next_val=next_val+1;



DELIMITER $
CREATE PROCEDURE numbers()
BEGIN
    select count(distinct SUB_Mobtel) as 'distinct msisdn' from messagelog;
    select count(*), date(timeStamp) ts from messagelog  group by ts order by ts asc;
	select count(*),profileComplete ,date(creationDate) dt from dating_profile group by profileComplete,dt;
	select count(*),profileComplete from dating_profile group by profileComplete;
	select count(*), date(timeStamp) ts from dating_chatlog where hour(timeStamp)<=hour(now()) group by ts order by ts asc;
END;
$
DELIMITER ;
