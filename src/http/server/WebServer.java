///A Simple Web Server (WebServer.java)

package http.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * Example program from Chapter 1 Programming Spiders, Bots and Aggregators in
 * Java Copyright 2001 by Jeff Heaton
 * 
 * WebServer is a very simple web-server. Any request is responded with a very
 * simple web-page.
 * 
 * @author Jeff Heaton
 * @version 1.0
 */
public class WebServer {
  private String request;
  private String method;
  private String ressourceAsked;
  private String ressourceExtension;
  private final String PATH_TO_DOC="doc";
  private PrintWriter out;
  private BufferedOutputStream outByte;
  public WebServer(){

  }

  public void sendHTMLPage(File file){
    try {
      FileInputStream fl = new FileInputStream(file);
      byte[] arr = new byte[(int)file.length()];
      fl.read(arr);
      outByte.write(arr,0,(int)file.length());
      outByte.flush();
      fl.close();
      /*BufferedReader reader = new BufferedReader(new FileReader(file));
      String line;
      while((line=reader.readLine())!=null){
        out.println(line);
      }*/
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void treatRequest(){
    if(!request.isEmpty()){
      System.out.println("Request treated");
      StringTokenizer convertRequest=new StringTokenizer(request);
      method=convertRequest.nextToken();
      ressourceAsked=convertRequest.nextToken();
    }
    System.out.println("method: "+method);
    System.out.println("ressource: "+ressourceAsked);
    int i = ressourceAsked.lastIndexOf('.');
    if (i >= 0) {
      ressourceExtension = ressourceAsked.substring(i+1);
    }
    System.out.println("ressource extension: "+ressourceExtension);
  }

  public boolean ressourceExist(String ressource){
    try{
      File file=new File(PATH_TO_DOC+ressource);
      if(file.exists()){
        return true;
      }
    }catch (Exception e){
    }
    return false;
  }
  public void defaultMethod(){
    // Send the response
    // Send the headers
    out.println("HTTP/1.0 200 OK");
    out.println("Content-Type: text/html");
    out.println("Server: Bot");
    // this blank line signals the end of the headers
    out.println("");
    // Send the HTML page
    out.println("<H1>Welcome to the Ultra Mini-WebServer</H2>");
  }

  public void doGet(){
    if(ressourceExist(ressourceAsked)){
      File file=new File(PATH_TO_DOC+ressourceAsked);

      switch (ressourceExtension){
        case "html":
          // Send the response
          // Send the headers
          out.println("HTTP/1.0 200 OK");
          out.println("Content-Type: text/html");
          out.println("Content-Length: "+file.length());
          out.println("Server: Bot");
          // this blank line signals the end of the headers
          out.println("");
          // Send the HTML page
          sendHTMLPage(file);
      }
    }else{
      // Send the response
      // Send the headers
      out.println("HTTP/1.0 404 Not found");
      out.println("Content-Type: text/html");
      out.println("Server: Bot");
      // this blank line signals the end of the headers
      out.println("");
      // Send the HTML page
      out.println("Page not found.");
    }
    out.flush();
  }

  /**
   * WebServer constructor.
   */
  protected void start() {
    ServerSocket s;

    System.out.println("Webserver starting up on port 80");
    System.out.println("(press ctrl-c to exit)");
    try {
      // create the main server socket
      s = new ServerSocket(3000);
    } catch (Exception e) {
      System.out.println("Error: " + e);
      return;
    }

    System.out.println("Waiting for connection");
    for (;;) {
      try {
        // wait for a connection
        Socket remote = s.accept();
        request="";

        // remote is now the connected socket
        System.out.println("Connection, sending data.");
        BufferedReader in = new BufferedReader(new InputStreamReader(
            remote.getInputStream()));
        out = new PrintWriter(remote.getOutputStream());
        outByte=new BufferedOutputStream(remote.getOutputStream());

        // read the data sent. We basically ignore it,
        // stop reading once a blank line is hit. This
        // blank line signals the end of the client HTTP
        // headers.
        String str = ".";
        while (str != null && !str.equals("")) {
          str = in.readLine();
          System.out.println(str);
          request+=str+" ";
        }
        System.out.println("************");
        treatRequest();
        System.out.println("************");
        switch (method){
          case "GET":
            doGet();
          default:
            defaultMethod();
        }
        remote.close();
        System.out.println("end of request");
      } catch (Exception e) {
        System.out.println("Error: " + e);
      }
    }
  }

  /**
   * Start the application.
   * 
   * @param args
   *            Command line parameters are not used.
   */
  public static void main(String args[]) {
    WebServer ws = new WebServer();
    ws.start();
  }
}
