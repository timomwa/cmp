#!/bin/bash
# @author Timothy Mwangi
# Date created : 1st October 2014
PID=/var/run/sms_platform.pid

#Just fancy colors...
txtrst=$(tput sgr0)
txtred=$(tput setaf 1) # Red
txtgrn=$(tput setaf 2) # Green
txtylw=$(tput setaf 3) # Yellow
txtblu=$(tput setaf 4) # Blue
txtpur=$(tput setaf 5) # Purple
txtcyn=$(tput setaf 6) # Cyan
txtwht=$(tput setaf 7) # White
if [ -f $PID ] ; then
        if [ "$(ps -p `cat $PID` | wc -l)" -gt 1 ]; then
                echo -e "\n\n${txtred}Instance already running!${txtrst}\n\n"
                exit 1
        else
                /bin/rm -f $PID
        fi
fi
echo $$> $PID

cd /Volumes/Data/IDE_WORKSPACES/MOBI4/cmp
/usr/bin/java -cp "commons-codec-1.4.jar:./lib/commons-logging.jar:./lib/ibx.jar:./lib/log4j-1.2.16.jar:./lib/mysql-connector-java-5.1.17-bin.jar:./lib/saaj-impl.jar:./lib/commons-email-1.1.jar:./lib/DBPool-5.0.jar:./lib/javax.servlet.jar:./lib/lucky_dip.jar:./lib/poi-3.7-20101029.jar:./lib/utillib.jar:./lib/commons-httpclient-3.1.jar:./lib/httpclient-4.1.3.jar:./lib/jdom-1.0.jar:./lib/mail.jar:./lib/poi-ooxml-3.7-20101029.jar:./lib/commons-lang.jar:./lib/httpcore-4.1.4.jar:./lib/json.jar:./lib/mysql-connector-java-5.0.4-bin.jar:./lib/poi-ooxml-schemas-3.8-20120326.jar:./lib/commons-logging-1.1.1.jar:./lib/httpmime-4.1.3.jar:./lib/log4j-1.2.14.jar:./lib/mysql-connector-java-5.1.14-bin.jar:./lib/saaj-api.jar:celcom.jar:./target/cmp-jar-with-dependencies.jar" -Xms64m -Xmx512m com.pixelandtag.sms.core.SenderPlatform >> logs/platform.log  & echo $! > /var/run/sms_platform.pid 
