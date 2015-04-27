+-------+-------------+--------------------+---------------------+--------+--------------------------------+-----------+-----------+
| id    | accountCode | name               | dateactivated       | active | apiKey                         | pwd       | uname     |
+-------+-------------+--------------------+---------------------+--------+--------------------------------+-----------+-----------+
| 30778 | MPEDIGREE   | mPedigree bulk sms | 2015-04-25 01:11:50 |      1 | f0cp4tp180938b8vd0d632c57vs0ec | mpedigree | mpedigree |
+-------+-------------+--------------------+---------------------+--------+--------------------------------+-----------+-----------+


insert into bulksms_account select z.next_val,'MPEDIGREE','mPedigree bulk sms','2015-04-25 01:11:50',1,'f0cp4tp180938b8vd0d632c57vs0ec','mpedigree2015','mpedigree' from (select next_val from hibernate_sequence) z;
update hibernate_sequence set next_val=next_val+1;

f0cp4tp180938b8vd0d632c57vs0ec - mpedigree
5f94kkw9r9ksk34k3ld03kdo30fkoe - test accunt
i94ifkm49fmowls9emk9kls0ekdoed - unnassigned
insert into bulksms_account select z.next_val,'TESTACC','Setup test acc','2014-06-25 04:15:20',1,'5f94kkw9r9ksk34k3ld03kdo30fkoe','testacc2014','textacc' from (select next_val from hibernate_sequence) z;
update hibernate_sequence set next_val=next_val+1;

insert into bulksms_account select z.next_val,'TESTACC','Setup test acc','2014-06-25 04:15:20',1,'5f94kkw9r9ksk34k3ld03kdo30fkoe','testacc2014','textacc' from (select next_val from hibernate_sequence) z;
update hibernate_sequence set next_val=next_val+1;




mysql> select * from bulksms_plan;
+-------+--------+---------------------+-----------+----------+----------+----------+---------------+----------+-------------------+
| id    | active | date_purch          | no_of_sms | planid   | timeunit | validity | account_id_fk | telcoid  | max_outgoing_size |
+-------+--------+---------------------+-----------+----------+----------+----------+---------------+----------+-------------------+
| 30779 |      1 | 2015-04-25 01:16:01 |    100.00 | PLAN0001 | DAY      |      365 |         30778 | AIRTELKE |           5000.00 |
+-------+--------+---------------------+-----------+----------+----------+----------+---------------+----------+-------------------+
set foreign_key_checks=0;
insert into bulksms_plan select z.next_val,1,'2014-06-25 04:15:20',10000,'PLAN321245','DAY',365,38911,'AIRTELKE',10000 from (select next_val from hibernate_sequence) z;
update hibernate_sequence set next_val=next_val+1;
set foreign_key_checks=1;


set foreign_key_checks=0;
insert into bulksms_plan select z.next_val,1,'2014-06-25 04:15:20',100000,'TESTPLAN','DAY',365,38912,'AIRTELKE',100 from (select next_val from hibernate_sequence) z;
update hibernate_sequence set next_val=next_val+1;
set foreign_key_checks=1;




