///A Simple Web Server (WebServer.java)

package http.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
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
  private final String PATH_TO_DOC="./doc";
  private PrintWriter out;
  private BufferedOutputStream outByte;
  public WebServer(){

  }

  public String getNow(){
    SimpleDateFormat sdf=new SimpleDateFormat("EEE dd MMM yyyy HH:mm:ss zzz", Locale.FRANCE);
    Date now= new Date();
    return sdf.format(now);
  }
  public void sendFileInBytes(File file){
    try {
      byte[] data = readFileData(file, (int) file.length());
      outByte.write(data,0,(int)file.length());
      outByte.flush();
      /*
      BufferedReader reader = new BufferedReader(new FileReader(file));
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
  //NOT USE
  public void defaultMethod(){
    // Send the response
    // Send the headers
    out.println("HTTP/1.0 200 OK");
    out.println("Date:"+getNow());
    out.println("Content-Type: text/html");
    out.println("Server: Bot");
    // this blank line signals the end of the headers
    out.println("");
    // Send the HTML page
    out.println("<h1>Welcome to the Ultra Mini-WebServer</h1>");
  }

  public void doPost(){

  }

  public void doDelete(){
    if(ressourceExist(ressourceAsked)){
        File file=new File(PATH_TO_DOC+ressourceAsked);
        if(file.delete()){
            // Send the headers
            out.println("HTTP/1.0 204 No Content");
        }else{
          // Send the headers
          out.println("HTTP/1.0 500 Internal Server Error");
        }
    }else{
      // Send the headers
      out.println("HTTP/1.0 404 Not Found");
    }
    out.println("Date:"+getNow());
    out.println("Server: Bot");
    // this blank line signals the end of the headers
    out.println();
    out.flush();
  }

  public void doPut(){

  }

  public void dohead(){

  }

  public void doGet(){
    File file;
    if(ressourceExist(ressourceAsked)){
      file=new File(PATH_TO_DOC+ressourceAsked);
      String content = getContentType(ressourceAsked);

      // Send the response
      // Send the headers
      out.println("HTTP/1.0 200 OK");
      out.println("Date:"+getNow());
      out.println("Content-Type: "+ content);
      out.println("Content-Length: "+file.length());
      out.println("Server: Bot");
      out.println();
      // this blank line signals the end of the headers
    }else{
      file = new File (PATH_TO_DOC+"/404.html");
      // Send the response
      // Send the headers
      out.println("HTTP/1.0 404 Not found");
      out.println("Content-Type: text/html");
      out.println("Server: Bot");
      // this blank line signals the end of the headers
      out.println();
    }
    out.flush();
    // Send the HTML page
    sendFileInBytes(file);
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
          request+=str+" ";
        }
        System.out.println("************");
        treatRequest();
        System.out.println("************");
        switch (method){
          case "GET":
            doGet();
            break;
          case "PUT":
            doPut();
            break;
          case "DELETE":
            doDelete();
            break;
          case "POST":
            doPost();
            break;
          case "HEAD":
            dohead();
            break;
          default:
            defaultMethod();
            break;
        }
        remote.close();
      } catch (Exception e) {
        System.out.println("Error: " + e);
      }
    }
  }

  private byte[] readFileData(File file, int fileLength) throws IOException {
    FileInputStream fileIn = null;
    byte[] fileData = new byte[fileLength];

    try {
      fileIn = new FileInputStream(file);
      fileIn.read(fileData);
    } finally {
      if (fileIn != null)
        fileIn.close();
    }

    return fileData;
  }

  // return supported MIME Types
  private String getContentType(String fileRequested) {
    if (fileRequested.endsWith(".png")  ||  fileRequested.endsWith(".jpeg") ||  fileRequested.endsWith(".jpg"))
      return "Image";
    else if (fileRequested.endsWith(".mp3"))
      return "audio";
    else
      return "text";
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
