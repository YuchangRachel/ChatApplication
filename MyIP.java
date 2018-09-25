package com.chatapplication
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MyIP {
	public static void main(String[] args){
		System.out.print("port is" + args[0]);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(System.in));
			while (true){
				String input = br.readLine();

				if (input.equals("myip")){
					System.out.println("it is me");
				}
			}
		}catch (IOException e){
			e.printStackTrace();
		}
	}
}
