-- Orange parlay x scripts --
-- delete FROM `pixeland_content360`.`sender_profiles` --
insert into sender_receiver_profile select 89016857,1,'2016-03-01 00:00:00','Orange''s ParlayX','SENDER';
insert into `pixeland_content360`.`opco_senderprofiles` select 89028424, 1, now(),1,79497164, 89016857, 1;
update  `pixeland_content360`.`opco_senderprofiles` set active = 0  where id = 80457410;
insert into `pixeland_content360`.`profile_configs` select 128080241,'string', now(),'senderimpl','com.pixelandtag.smssenders.PlainHttpSender',89016857;
insert into `pixeland_content360`.`profile_configs` select 128080242,'string', now(),'http_protocol','http',89016857;
insert into `pixeland_content360`.`profile_configs` select 128080243,'string', now(),'http_base_url','http://196.202.219.252:7062/SendSmsService',89016857;
insert into `pixeland_content360`.`profile_configs` select 128080244,'string', now(),'http_shortcode_param_name','shortcode',89016857;
insert into `pixeland_content360`.`profile_configs` select 128080245,'string', now(),'http_msisdn_param_name','msisdn',89016857;
insert into `pixeland_content360`.`profile_configs` select 128080246,'string', now(),'http_sms_msg_param_name','sms',89016857;
insert into `pixeland_content360`.`profile_configs` select 128080247,'string', now(),'http_useheader','no',89016857;
insert into `pixeland_content360`.`profile_configs` select 128080248,'string', now(),'http_haspayload','yes',89016857;
insert into `pixeland_content360`.`profile_configs` select 128080249,'string', now(),'http_is_restful','no',89016857;
insert into `pixeland_content360`.`profile_configs` select 128080250,'string', now(),'http_header_auth_hasmultiple_kv_pairs','no',89016857;
insert into `pixeland_content360`.`profile_configs` select 128080251,'string', now(),'http_allow_send_blank_text','no',89016857;
insert into `pixeland_content360`.`profile_configs` select 128080252,'string', now(),'http_payload_template_name','parlayx_template',89016857;
insert into `pixeland_content360`.`profile_configs` select 128080254,'string', now(),'http_payload_param_spId','36',89016857;
insert into `pixeland_content360`.`profile_configs` select 128080255,'string', now(),'http_payload_param_spPassword','96e79218965eb72c92a549dd5a330112',89016857;
insert into `pixeland_content360`.`profile_configs` select 128080256,'string', now(),'http_payload_param_interfaceName','interfaceName',89016857;
insert into `pixeland_content360`.`profile_configs` select 128080257,'string', now(),'http_transaction_id_param_name','txid',89016857;
insert into `pixeland_content360`.`profile_configs` select 128080258,'string', now(),'http_payload_param_serviceid','34',89016857;
INSERT INTO `pixeland_content360`.`profile_templates` (`id`, `effectiveDate`, `name`, `type`, `value`, `profile_id_fk`) VALUES (3, '2016-03-17 00:00:00', 'parlayx_template', 'PAYLOAD', '<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:v3=\"http://www.csapi.org/schema/parlayx/common/v3_1\" xmlns:loc=\"http://www.csapi.org/schema/parlayx/sms/send/v3_1/local\"><soapenv:Header><v3:RequestSOAPHeader><spId>${spId}</spId><spPassword>${spPassword}</spPassword><timeStamp></timeStamp><serviceId>${serviceid}</serviceId></v3:RequestSOAPHeader></soapenv:Header><soapenv:Body><loc:sendSms><loc:addresses>tel:${msisdn}</loc:addresses><loc:senderName>${shortcode}</loc:senderName><loc:message>${sms}</loc:message><loc:receiptRequest><endpoint></endpoint><interfaceName>${interfaceName}</interfaceName><correlator>${txid}</correlator></loc:receiptRequest></loc:sendSms></soapenv:Body></soapenv:Envelope>', 89016857);


