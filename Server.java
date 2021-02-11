import com.sun.net.httpserver.*;
import java.net.InetSocketAddress;
import java.io.*;
import java.util.concurrent.*;
import org.apache.commons.text.StringEscapeUtils;

import java.awt.Robot;
import java.awt.AWTException;

class Server{
	public static void main(String[] arg){
		try{
			ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor)Executors.newFixedThreadPool(10);
			HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", 8001), 0);
			server.createContext("/control", new  MyHttpHandler());
			server.setExecutor(threadPoolExecutor);
			server.start();
		}catch(IOException e){
			System.out.println("IOException:");
		}
	}
}

class MyHttpHandler implements HttpHandler{
	Robot robot;

	public MyHttpHandler(){
		try{
			robot = new Robot();
		}catch(AWTException e){
			System.out.println("Problem instantiating Robot.");
		}
	}

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor)Executors.newFixedThreadPool(10);
		String requestParamValue=null;
		if("GET".equals(httpExchange.getRequestMethod())) {
			requestParamValue = handleGetRequest(httpExchange);
		}else if("POST".equals(httpExchange)) {
			System.out.println(httpExchange);
		}
			handleResponse(httpExchange,requestParamValue);
	}

	private String handleGetRequest(HttpExchange httpExchange) {
		return httpExchange.getRequestURI().toString().split("\\?")[1].split("=")[1];
	}

	private void handleResponse(HttpExchange httpExchange, String requestParamValue)  throws  IOException{
		System.out.println("got request: " + requestParamValue);
		String htmlResponse = "<html><head></head><body><h1>POWERPOINT REMOTE CONTROL</h1><table><tr><td><form action=/control><input type=hidden name=command value=prev><input type=submit command=prev value=PREV></form></td><td><form action=/control><input type=hidden name=command value=next><input type=submit command=prev value=NEXT></form></td></tr></table><br>";
		try{
			switch(requestParamValue){
				case "next":
					robot.keyPress(39);
					robot.keyRelease(39);
					htmlResponse = htmlResponse + "OK";
					break;
				case "prev":
					robot.keyPress(37);
					robot.keyRelease(37);
					htmlResponse = htmlResponse + "OK";
					break;
				case "SHOW_CONTROLS":
					//nothing, really (it was late)
					break;
				default:
					try{
						int key = Integer.valueOf(requestParamValue);
						robot.keyPress(key);
						robot.keyRelease(key);
					}catch(NumberFormatException nfe){
						htmlResponse = htmlResponse + "Unknown command.";
					}
					break;
			}

		}catch(Exception e){
			htmlResponse = htmlResponse + "FAIL!";
			e.printStackTrace();
		}

		htmlResponse = htmlResponse + "</body></html>";

		OutputStream outputStream = httpExchange.getResponseBody();
		StringBuilder htmlBuilder = new StringBuilder();

		// this line is apparently a must
		httpExchange.sendResponseHeaders(200, htmlResponse.length());
		outputStream.write(htmlResponse.getBytes());
		outputStream.flush();
		outputStream.close();

	}
}
