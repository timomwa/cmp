insert into `pixeland_content360`.`biller_profile`(`id`,`active`,`effectiveDate`,`name`,`type`) values(102,1,now(),'Orange''s One API biller','BILLER');
insert into `pixeland_content360`.`opco_biller_profile`(`id`,`active`, `effectiveDate`, `pickorder`, `workers`, `opco_id_fk`, `profile_id_fk`) values(2,1,now(),0,1,79497164,102);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(301,'string',now(),'billerimpl','com.pixelandtag.billing.HttpBiller',102);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(302,'string',now(),'biller_http_base_url','http://196.202.219.252:7061/1/payment/tel%3A%2B${MSISDN}/transactions/amount',102);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(303,'string',now(),'biller_http_shortcode_param_name','SHORTCODE',102);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(304,'string',now(),'biller_http_msisdn_param_name','MSISDN',102);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(305,'string',now(),'biller_http_sms_msg_param_name','SMS',102);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(306,'string',now(),'biller_http_useheader','yes',102);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(307,'string',now(),'biller_http_haspayload','yes',102);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(308,'string',now(),'biller_http_is_restful','yes',102);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(309,'string',now(),'biller_http_header_auth_hasmultiple_kv_pairs','yes',102);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(310,'string',now(),'biller_http_protocol','http',102);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(311,'string',now(),'biller_http_payload_template_name','orangeoneapi_json_template',102);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(312,'string',now(),'biller_http_request_method','POST',102);
INSERT INTO `pixeland_content360`.`biller_profile_templates` (`id`, `effectiveDate`, `name`, `type`, `value`, `profile_id_fk`) VALUES (2, '2016-01-19 20:24:00', 'orangeoneapi_json_template', 'PAYLOAD', '{"endUserId": "tel:${MSISDN}","transactionOperationStatus": "Charged","chargingInformation": {"description": "Dating service","code": "${SHORTCODE}","amount": ${PRICE},"currency": "KES"},"referenceCode": "${CP_TX_ID}","clientCorrelator": "${CP_TX_ID}"}', 102);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(313,'string',now(),'biller_http_transaction_id_param_name','CP_TX_ID',102);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(314,'string',now(),'biller_http_header_auth_password_encryptionmode','basicmd5',102);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(315,'string',now(),'biller_http_price_param_name','PRICE',102);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(316,'string',now(),'biller_http_header_auth_has_username_and_password','no',102);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(317,'string',now(),'biller_http_header_auth_username_param_name','spid',102);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(318,'string',now(),'biller_http_header_auth_password_param_name','spPassword',102);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(319,'string',now(),'spid','36',102);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(320,'string',now(),'spPassword','111111',102);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(321,'string',now(),'biller_http_header_auth_method_param_name','',102);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(322,'string',now(),'biller_http_headerauth_param_name','Authorization',102);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(323,'string',now(),'biller_http_header_auth_param_spId','36',102);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(324,'string',now(),'biller_http_header_auth_param_timeStamp','${TIMESTAMP}',102);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(325,'string',now(),'biller_http_header_auth_param_serviceId','49',102);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(326,'string',now(),'biller_http_header_timeStampFormat','yyyyMMdd',102);

insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(327,'string',now(),'biller_http_pre_encode_template','${PASSWORD}',102);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(328,'string',now(),'biller_http_expectedcontenttype','json',102);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(329,'string',now(),'biller_http_resp_json_ref_value_key','serverReferenceCode',102);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(330,'string',now(),'biller_http_resp_json_resp_msg_key','transactionOperationStatus',102);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(331,'string',now(),'biller_http_resp_json_respcode_key','referenceCode',102);











