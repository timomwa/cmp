insert into `pixeland_content360`.`biller_profile`(`id`,`active`,`effectiveDate`,`name`,`type`) values(101,1,now(),'Airtel - KE SDP biller','BILLER');
insert into `pixeland_content360`.`opco_biller_profile`(`id`,`active`, `effectiveDate`, `pickorder`, `workers`, `opco_id_fk`, `profile_id_fk`) values(1,1,now(),0,1,79497102,101);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(201,'string',now(),'billerimpl','com.pixelandtag.billing.HttpBiller',101);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(202,'string',now(),'biller_http_base_url','https://41.223.58.133:8443/ChargingServiceFlowWeb/sca/ChargingExport1',101);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(203,'string',now(),'biller_http_shortcode_param_name','SHORTCODE',101);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(204,'string',now(),'biller_http_msisdn_param_name','MSISDN',101);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(205,'string',now(),'biller_http_sms_msg_param_name','sms',101);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(206,'string',now(),'biller_http_useheader','yes',101);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(207,'string',now(),'biller_http_haspayload','yes',101);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(208,'string',now(),'biller_http_is_restful','no',101);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(209,'string',now(),'biller_http_header_auth_hasmultiple_kv_pairs','yes',101);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(210,'string',now(),'biller_http_protocol','https',101);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(211,'string',now(),'biller_http_payload_template_name','airtelbillertemplate1',101);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(212,'string',now(),'biller_http_request_method','POST',101);


INSERT INTO `pixeland_content360`.`biller_profile_templates` (`id`, `effectiveDate`, `name`, `type`, `value`, `profile_id_fk`) VALUES (1, '2016-01-19 20:24:00', 'airtelbillertemplate1', 'PAYLOAD', '<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"	xmlns:char=\"http://ChargingProcess/com/ibm/sdp/services/charging/abstraction/Charging\"><soapenv:Header /><soapenv:Body><char:charge><inputMsg><operation>${operation}</operation><userId>${msisdn}</userId><contentId>${contentid}</contentId><itemName>${itemname}</itemName><contentDescription>${description}</contentDescription><circleId></circleId><lineOfBusiness></lineOfBusiness><customerSegment></customerSegment><contentMediaType>${contentmediatype}</contentMediaType><serviceId>${serviceid}</serviceId><parentId></parentId><actualPrice>${price}</actualPrice><basePrice>${price}</basePrice><discountApplied>${discountapplied}</discountApplied><paymentMethod></paymentMethod><revenuePercent></revenuePercent><netShare>${netshare}</netShare><cpId>${cpid}</cpId><customerClass></customerClass><eventType>${eventType}</eventType><localTimeStamp></localTimeStamp><transactionId>${transactionid}</transactionId><subscriptionTypeCode>${subscriptiontypeCode}</subscriptionTypeCode><subscriptionName>${subscriptionname}</subscriptionName><parentType></parentType><deliveryChannel>${deliverychannel}</deliveryChannel><subscriptionExternalId>0</subscriptionExternalId><contentSize></contentSize><currency>${currency}</currency><copyrightId>${copyrightid}</copyrightId><cpTransactionId>${cp_tx_id}</cpTransactionId><copyrightDescription>${copyrightdes}</copyrightDescription><sMSkeyword>${smsKeyword}</sMSkeyword><srcCode>${shortcode}</srcCode><contentUrl>${contenturl}</contentUrl><subscriptiondays>${subscriptiondays}</subscriptiondays></inputMsg></char:charge></soapenv:Body></soapenv:Envelope>', 101);
UPDATE `pixeland_content360`.`biller_profile_templates` SET `value`='<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:char=\"http://ChargingProcess/com/ibm/sdp/services/charging/abstraction/Charging\"><soapenv:Header /><soapenv:Body><char:charge><inputMsg><operation>${OPERATION}</operation><userId>${MSISDN}</userId><contentId>${PRICE_POINT}</contentId><itemName>${PRICE_POINT}</itemName><contentDescription>${PRICE_POINT}</contentDescription><circleId></circleId><lineOfBusiness></lineOfBusiness><customerSegment></customerSegment><contentMediaType>${PRICE_POINT}</contentMediaType><serviceId>${SERVICE_ID}</serviceId><parentId></parentId><actualPrice>${PRICE}</actualPrice><basePrice>${PRICE}</basePrice><discountApplied>0</discountApplied><paymentMethod></paymentMethod><revenuePercent></revenuePercent><netShare>0</netShare><cpId>${CP_ID}</cpId><customerClass></customerClass><eventType>${EVENT_TYPE}</eventType><localTimeStamp></localTimeStamp><transactionId>${TX_ID}</transactionId><subscriptionTypeCode>abcd</subscriptionTypeCode><subscriptionName>0</subscriptionName><parentType></parentType><deliveryChannel>SMS</deliveryChannel><subscriptionExternalId>0</subscriptionExternalId><contentSize></contentSize><currency>Kshs</currency><copyrightId>mauj</copyrightId><cpTransactionId>${CP_TX_ID}</cpTransactionId><copyrightDescription>copyright</copyrightDescription><sMSkeyword>${KEYWORD}</sMSkeyword><srcCode>${SHORTCODE}</srcCode><contentUrl>www.content360.co.ke</contentUrl><subscriptiondays>1</subscriptiondays></inputMsg></char:charge></soapenv:Body></soapenv:Envelope>' WHERE `id`='1';

insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(213,'string',now(),'biller_http_transaction_id_param_name','CP_TX_ID',101);

insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(215,'string',now(),'biller_http_price_param_name','PRICE',101);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(217,'string',now(),'biller_http_keyword_param_name','KEYWORD',101);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(218,'string',now(),'biller_http_eventtype_param_name','EVENT_TYPE',101);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(219,'string',now(),'biller_http_cp_id_param_name','CP_ID',101);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(220,'string',now(),'biller_http_operation_param_name','OPERATION',101);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(221,'string',now(),'biller_http_header_auth_param_SOAPAction','',101);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(222,'string',now(),'biller_http_header_auth_has_username_and_password','yes',101);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(223,'string',now(),'biller_http_header_auth_username_param_name','username',101);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(224,'string',now(),'biller_http_header_auth_password_param_name','password',101);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(225,'string',now(),'username','CONTENT360_KE',101);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(226,'string',now(),'password','4ecf#hjsan7',101);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(227,'string',now(),'biller_http_header_auth_password_encryptionmode','basicbase64',101);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(228,'string',now(),'biller_http_header_auth_method_param_name','Basic',101);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(229,'string',now(),'biller_http_headerauth_param_name','Authorization',101);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(230,'string',now(),'biller_http_serviceid_param_name','SERVICE_ID',101);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(231,'string',now(),'biller_http_pricepoint_param_name','PRICE_POINT',101);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(232,'string',now(),'biller_http_resp_xml_ref_value_key','transactionId',101);
--Confirm if we really need this--
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(233,'string',now(),'biller_http_resp_xml_resp_msg_key','status',101);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(234,'string',now(),'biller_http_resp_xml_resp_msg_key_failure','errorMessage',101);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(235,'string',now(),'biller_http_resp_xml_resp_msg_key_success','status',101);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(236,'string',now(),'biller_http_resp_success_string','Success',101);
insert into `pixeland_content360`.`biller_profile_configs`(`id`,`data_type`,`effectiveDate`,`name`,`value`,`profile_id_fk`) VALUES(237,'string',now(),'biller_http_expectedcontenttype','xml',101);

















