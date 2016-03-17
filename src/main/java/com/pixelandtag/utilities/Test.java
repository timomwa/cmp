package com.pixelandtag.utilities;

public class Test {

	public static void main(String[] args) {
		String contextpath = "test.php";
		String response ="<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"
				+" <!DOCTYPE pages SYSTEM \"cellflash-1.3.dtd\">"
				+" <pages descr=\"News\">"
				+" <page>"
				+" Headlines<br/>"
				+" <a href=\""+contextpath+"?item=1\">Interest rates cut</a><br/>"
				+" <a href=\""+contextpath+"?item=2\">Concorde resumes service</a><br/>"
				+" </page>"
				+" <page tag=\""+contextpath+"?item=3\">"
				+" WASHINGTON-In a much anticipated move, the Federal Reserve"
				+" announced new rate cuts amid growing economic concerns.<br/>"
				+" <a href=\""+contextpath+"?item=4\">Next article</a>"
				+" </page>"
				+" <page tag=\""+contextpath+"?item=5\">"
				+" PARIS-Air France resumed its Concorde service Monday."
				+" The plane had been grounded following a tragic accident."
				+" </page>"
				+" </pages>"; 
		System.out.println(response);
	}

}
