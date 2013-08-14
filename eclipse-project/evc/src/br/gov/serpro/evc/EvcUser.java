/**
 * 
 * 
 *  Author: Serpro
 */
package br.gov.serpro.evc;

public class EvcUser {
	
	private static final Object[] genuid = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
	
	public static String buildId() {
		int i = 0;
		String uid = "";
		while(i++ < 64) {
			uid += genuid[(int)Math.round(Math.random()*15)];
		}
		
		return uid;
	}
}
