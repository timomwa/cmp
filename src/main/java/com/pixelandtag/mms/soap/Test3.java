package com.pixelandtag.mms.soap;


public class Test3 {
	
	public static void main(String[] args) throws Exception {
		String x = "<v2:linkid>${linkid}</v2:linkid>";
		System.out.println(x.replace("<v2:linkid>${linkid}</v2:linkid>",""));
	}

}
