insert into operator select next_val+1,'Airtel','639-3' from hibernate_sequence;update hibernate_sequence set next_val=next_val+1;
insert into operator select next_val+1,'Orange','639-7' from hibernate_sequence;update hibernate_sequence set next_val=next_val+1;

insert into operator_country select next_val+1,79484794,79493566 from hibernate_sequence;update hibernate_sequence set next_val=next_val+1;
insert into operator_country select next_val+1,79484794,79494610 from hibernate_sequence;update hibernate_sequence set next_val=next_val+1;


ALTER TABLE `pixeland_content360`.`operator_country` ADD COLUMN `code` VARCHAR(255) AFTER `operator_id_fk`, ADD UNIQUE INDEX `coded_UNIQUE` (`code` ASC) ;


delimiter $$

CREATE TABLE `opco_configs` (
  `id` bigint(20) NOT NULL,
  `data_type` varchar(255) DEFAULT NULL,
  `effectiveDate` datetime NOT NULL,
  `opcconfidx` varchar(255) DEFAULT NULL,
  `value` varchar(255) DEFAULT NULL,
  `opco_id_fk` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `opcconfidx` (`effectiveDate`,`opco_id_fk`),
  KEY `name` (`opcconfidx`),
  KEY `FK_n0hncg1ud37ithhyihb9tup9y` (`opco_id_fk`),
  CONSTRAINT `FK_n0hncg1ud37ithhyihb9tup9y` FOREIGN KEY (`opco_id_fk`) REFERENCES `operator_country` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


insert into opco_configs  select next_val+1, 'string', '2017-12-20 00:00:00', 'protocol', 'http', 79497164 from hibernate_sequence;update hibernate_sequence set next_val=next_val+1;
insert into opco_configs  select next_val+1, 'string', '2017-12-20 00:00:00', 'senderimpl', 'com.pixelandtag.smssenders.PlainHttpSender', 79497164 from hibernate_sequence;update hibernate_sequence set next_val=next_val+1;
insert into opco_configs  select next_val+1, 'string', '2017-12-20 00:00:00', 'http_base_url', 'http://196.202.219.252:7061/1/smsmessaging/outbound/tel%3A%2B${msisdn}/requests', 79497164 from hibernate_sequence;update hibernate_sequence set next_val=next_val+1;
insert into opco_configs  select next_val+1, 'string', '2017-12-20 00:00:00', 'http_shortcode_param_name', 'senderName', 79497164 from hibernate_sequence;update hibernate_sequence set next_val=next_val+1;
insert into opco_configs  select next_val+1, 'string', '2017-12-20 00:00:00', 'http_msisdn_param_name', 'msisdn', 79497164 from hibernate_sequence;update hibernate_sequence set next_val=next_val+1;
insert into opco_configs  select next_val+1, 'string', '2017-12-20 00:00:00', 'http_sms_msg_param_name', 'message', 79497164 from hibernate_sequence;update hibernate_sequence set next_val=next_val+1;
insert into opco_configs  select next_val+1, 'string', '2017-12-20 00:00:00', 'http_useheader', 'yes', 79497164 from hibernate_sequence;update hibernate_sequence set next_val=next_val+1;
insert into opco_configs  select next_val+1, 'string', '2017-12-20 00:00:00', 'http_header_auth_has_username_and_password', 'yes', 79497164 from hibernate_sequence;update hibernate_sequence set next_val=next_val+1;
insert into opco_configs  select next_val+1, 'string', '2017-12-20 00:00:00', 'http_haspayload', 'yes', 79497164 from hibernate_sequence;update hibernate_sequence set next_val=next_val+1;
insert into opco_configs  select next_val+1, 'string', '2017-12-20 00:00:00', 'http_is_restful', 'yes', 79497164 from hibernate_sequence;update hibernate_sequence set next_val=next_val+1;
insert into opco_configs  select next_val+1, 'string', '2017-12-20 00:00:00', 'http_header_auth_username_param_name', 'spId', 79497164 from hibernate_sequence;update hibernate_sequence set next_val=next_val+1;
insert into opco_configs  select next_val+1, 'string', '2017-12-20 00:00:00', 'http_header_auth_password_param_name', 'spPassword', 79497164 from hibernate_sequence;update hibernate_sequence set next_val=next_val+1;
insert into opco_configs  select next_val+1, 'string', '2017-12-20 00:00:00', 'http_header_auth_password_encryptionmode', 'basicmd5', 79497164 from hibernate_sequence;update hibernate_sequence set next_val=next_val+1;
insert into opco_configs  select next_val+1, 'string', '2017-12-20 00:00:00', 'http_header_auth_method_param_name', 'Basic', 79497164 from hibernate_sequence;update hibernate_sequence set next_val=next_val+1;
insert into opco_configs  select next_val+1, 'string', '2017-12-20 00:00:00', 'http_header_auth_hasmultiple_kv_pairs', 'yes', 79497164 from hibernate_sequence;update hibernate_sequence set next_val=next_val+1;
insert into opco_configs  select next_val+1, 'string', '2017-12-20 00:00:00', 'http_header_auth_param_spId', '36', 79497164 from hibernate_sequence;update hibernate_sequence set next_val=next_val+1;
insert into opco_configs  select next_val+1, 'string', '2017-12-20 00:00:00', 'http_header_auth_param_timeStamp', '20150105152020', 79497164 from hibernate_sequence;update hibernate_sequence set next_val=next_val+1;
insert into opco_configs  select next_val+1, 'string', '2017-12-20 00:00:00', 'http_header_auth_param_serviceId', '34', 79497164 from hibernate_sequence;update hibernate_sequence set next_val=next_val+1;
insert into opco_configs  select next_val+1, 'string', '2017-12-20 00:00:00', 'http_header_auth_param_name', 'Authorization', 79497164 from hibernate_sequence;update hibernate_sequence set next_val=next_val+1;
insert into opco_configs  select next_val+1, 'string', '2017-12-20 00:00:00', 'http_payload_template_name', 'oneapi_json_template', 79497164 from hibernate_sequence;update hibernate_sequence set next_val=next_val+1;
insert into opco_templates  select next_val+1,'2017-12-20 00:00:00', 'oneapi_json_template', 'PAYLOAD', '{\"validity_period\":\"991201230029000+\",\"address\":[\"tel:${http_payload_param_msisdn}\"],\"senderAddress\":\"${http_payload_param_senderaddress}\",\"message\":\"${http_payload_param_sms_msg}\",\"notifyURL\":\"http://test/test\",\"senderName\":\"${http_payload_param_senderaddress}\"}', 79497164 from hibernate_sequence;update hibernate_sequence set next_val=next_val+1;
insert into opco_configs  select next_val+1, 'string', '2017-12-20 00:00:00', 'spId', '36', 79497164 from hibernate_sequence;update hibernate_sequence set next_val=next_val+1;
insert into opco_configs  select next_val+1, 'string', '2017-12-20 00:00:00', 'spPassword', '111111', 79497164 from hibernate_sequence;update hibernate_sequence set next_val=next_val+1;
insert into opco_configs  select next_val+1, 'string', '2017-12-20 00:00:00', 'http_payload_param_', '111111', 79497164 from hibernate_sequence;update hibernate_sequence set next_val=next_val+1;
UPDATE `pixeland_content360`.`opco_templates` SET `value`='{\"validity_period\":\"991201230029000+\",\"address\":[\"tel:${msisdn}\"],\"senderAddress\":\"${senderaddress}\",\"message\":\"${message}\",\"notifyURL\":\"http://test/test\",\"senderName\":\"${senderaddress}\"}' WHERE `id`='79655901';

insert into opco_configs  select next_val+1, 'string', '2017-12-20 00:00:00', 'http_rest_path_param_msisdn', 'msisdn', 79497164 from hibernate_sequence;update hibernate_sequence set next_val=next_val+1;

insert into sender_profiles  select next_val+1, 1, '2017-12-20 00:00:00', 'parlay x' from hibernate_sequence;update hibernate_sequence set next_val=next_val+1;

insert into opco_senderprofiles select next_val+1, 1, '2017-12-20 00:00:00', 0, 79497164, 80452253 from hibernate_sequence;update hibernate_sequence set next_val=next_val+1;





