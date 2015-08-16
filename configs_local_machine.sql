insert into opco_configs  select 79582482, 'string', '2015-08-16 00:00:00', 'http_msisdn_param_name', 'msisdn', 79497164 from hibernate_sequence;
insert into opco_configs  select 79582484, 'string', '2015-08-16 00:00:00', 'http_sms_msg_param_name', 'message', 79497164 from hibernate_sequence;
insert into opco_configs  select 79582486, 'string', '2015-08-16 00:00:00', 'http_useheader', 'yes', 79497164 from hibernate_sequence;
insert into opco_configs  select 79582492, 'string', '2015-08-16 00:00:00', 'http_header_auth_has_username_and_password', 'yes', 79497164 from hibernate_sequence;
insert into opco_configs  select 79590924, 'string', '2015-08-16 00:00:00', 'http_haspayload', 'yes', 79497164 from hibernate_sequence;
insert into opco_configs  select 79604829, 'string', '2015-08-16 00:00:00', 'http_is_restful', 'yes', 79497164 from hibernate_sequence;
insert into opco_configs  select 79604830, 'string', '2015-08-16 00:00:00', 'http_header_auth_username_param_name', 'spId', 79497164 from hibernate_sequence;
insert into opco_configs  select 79604832, 'string', '2015-08-16 00:00:00', 'http_header_auth_password_param_name', 'spPassword', 79497164 from hibernate_sequence;
insert into opco_configs  select 79604836, 'string', '2015-08-16 00:00:00', 'http_header_auth_password_encryptionmode', 'basicmd5', 79497164 from hibernate_sequence;
insert into opco_configs  select 79604837, 'string', '2015-08-16 00:00:00', 'http_header_auth_method_param_name', 'Basic', 79497164 from hibernate_sequence;
insert into opco_configs  select 79604840, 'string', '2015-08-16 00:00:00', 'http_header_auth_hasmultiple_kv_pairs', 'yes', 79497164 from hibernate_sequence;
insert into opco_configs  select 79604841, 'string', '2015-08-16 00:00:00', 'http_header_auth_param_spId', '36', 79497164 from hibernate_sequence;
insert into opco_configs  select 79604842, 'string', '2015-08-16 00:00:00', 'http_header_auth_param_timeStamp', '20150105152020', 79497164 from hibernate_sequence;
insert into opco_configs  select 79604851, 'string', '2015-08-16 00:00:00', 'http_header_auth_param_serviceId', '34', 79497164 from hibernate_sequence;
insert into opco_configs  select 79604855, 'string', '2015-08-16 00:00:00', 'http_header_auth_param_name', 'Authorization', 79497164 from hibernate_sequence;
insert into opco_configs  select 79640684, 'string', '2015-08-16 00:00:00', 'http_payload_template_name', 'oneapi_json_template', 79497164 from hibernate_sequence;

insert into opco_templates  select 79655901,'2015-08-16 00:00:00', 'oneapi_json_template', 'PAYLOAD', '{\"validity_period\":\"991201230029000+\",\"address\":[\"tel:${http_payload_param_msisdn}\"],\"senderAddress\":\"${http_payload_param_senderaddress}\",\"message\":\"${http_payload_param_sms_msg}\",\"notifyURL\":\"http://test/test\",\"senderName\":\"${http_payload_param_senderaddress}\"}', 79497164 from hibernate_sequence;


insert into opco_configs  select 79761894, 'string', '2015-08-16 00:00:00', 'spId', '36', 79497164 from hibernate_sequence;
insert into opco_configs  select 79761896, 'string', '2015-08-16 00:00:00', 'spPassword', '111111', 79497164 from hibernate_sequence;


insert into opco_configs  select 79807029, 'string', '2015-08-16 00:00:00', 'http_rest_path_param_msisdn', 'msisdn', 79497164 from hibernate_sequence;

insert into sender_profiles  select 80452253, 1, '2015-08-16 00:00:00', 'parlay x' from hibernate_sequence;
insert into opco_senderprofiles select 80457410, 1, '2015-08-16 00:00:00', 0, 79497164, 80452253 from hibernate_sequence;

insert into `pixeland_content360`.`profile_configs` select id, data_type, effectiveDate, name, `value`, 80452253 as 'opco_id_fk' FROM `pixeland_content360`.`opco_configs`;
insert into `pixeland_content360`.`profile_templates` select id,effectiveDate,`name`,`type`,`value`,80452253 FROM `pixeland_content360`.`opco_templates`;


orange - 79497164
parlayx - 80452253
OpcoSenderProfile
