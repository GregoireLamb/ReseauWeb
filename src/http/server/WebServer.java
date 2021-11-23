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
  private String bodyRequest;
  private String method;
  private String ressourceAsked;
  private String ressourceExtension;
  private String contentType;
  private int contentLength;

  private final String PATH_TO_DOC="doc";
  private PrintWriter out;
  private BufferedOutputStream outByte;

  public WebServer(){
  }

  /**
   * Give under a given format, the date "now"
   * @return now
   */
  public String getNow(){
    SimpleDateFormat sdf=new SimpleDateFormat("EEE dd MMM yyyy HH:mm:ss zzz", Locale.FRANCE);
    Date now= new Date();
    return sdf.format(now);
  }

  /**
   * Send a file in bytes on the output stream of the socket
   * @param file, the file to send
   */
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

  /**
   * Treat the request to catch different element of the request
   */
  public void treatRequest(){
    if(!request.isEmpty()){
      StringTokenizer convertRequest=new StringTokenizer(request);
      if(convertRequest.hasMoreTokens()){
        method=convertRequest.nextToken();
      }
      if(convertRequest.hasMoreTokens()){
        ressourceAsked=convertRequest.nextToken();
      }else{
        ressourceAsked="";
      }
      while(convertRequest.hasMoreTokens()){
        String str=convertRequest.nextToken();
        if(str.toLowerCase().startsWith("content-length")){
          contentLength=Integer.parseInt(convertRequest.nextToken());
        }else if(str.toLowerCase().startsWith("content-type")){
          contentType=convertRequest.nextToken().split(";")[0];
        }
      }
    }

    int i = ressourceAsked.lastIndexOf('.');
    if (i >= 0) {
      ressourceExtension = ressourceAsked.substring(i+1);
    }
    /*
    System.out.println("************");
    System.out.println("method: "+method);
    System.out.println("ressource: "+ressourceAsked);
    System.out.println("content-length: "+contentLength);
    System.out.println("content-type: "+contentType);
    System.out.println("ressource extension: "+ressourceExtension);
    System.out.println("************");
    */
  }

  /**
   * Check if a given ressource exists
   * @param ressource
   * @return true|false
   */
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

  /**
   * Send the index to the client
   */
  public void index(){
    // Send the response
    // Send the headers
    File file=new File(PATH_TO_DOC+"/index.html");
    out.println("HTTP/1.0 200 OK");
    out.println("Date:"+getNow());
    out.println("Content-Type: text/html");
    out.println("Content-Length: " + file.length());
    out.println("Server: Bot");
    // this blank line signals the end of the headers
    out.println("");
    out.flush();
    // Send the HTML page
    sendFileInBytes(file);
  }

  public void doPost(){

  }

  /**
   * METHOD HTTP DELETE
   * Delete the ressource specified in the URL
   * Returns :
   *  - 204 : file deleted, no content
   *  - 500 : file not deleted, internal server error
   *  - 404 : file not deleted, not found
   *  - 403 : forbidden
   */
  public void doDelete(){
    if(ressourceAsked.startsWith("/admin")){
        out.println("HTTP/1.0 403 Forbidden");
        // If the ressource exists
    }else if(ressourceExist(ressourceAsked)){
        File file=new File(PATH_TO_DOC+ressourceAsked);
        // Attempt to delete the file
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

  /**
   * METHOD HTTP PUT
   * Create or overwrite the ressource specified in the URL
   * with the content in the body
   * Returns :
   *  - 204 : file overwrite, no content
   *  - 201 : file created
   *  - 500 : internal server error
   *  - 403 : forbidden
   */
  public void doPut() {
    FileWriter fileWriter;
    if(ressourceAsked.startsWith("/admin")){
      out.println("HTTP/1.0 403 Forbidden");
    // If the ressource exists
    }else if(ressourceExist(ressourceAsked)){
      File file=new File(PATH_TO_DOC+ressourceAsked);
      try {
        // Overwrite the existing file with the body request
        fileWriter=new FileWriter(file);
        fileWriter.write(bodyRequest);
        fileWriter.flush();
        fileWriter.close();
        // Send the headers
        out.println("HTTP/1.0 204 No Content");
      } catch (IOException e) {
        // Send the headers
        out.println("HTTP/1.0 500 Internal Server Error");
      }
    }else{
      // The ressource doesn't exist
      int index =ressourceAsked.lastIndexOf("/");
      String newDirectories=ressourceAsked.substring(0,index);
      // Creation of possible new directories
      File file=new File(PATH_TO_DOC+newDirectories);
      file.mkdirs();
      // Creation of the new file
      file=new File(PATH_TO_DOC+ressourceAsked);
      try {
        // Write the body request into the new file
        file.createNewFile();
        fileWriter = new FileWriter(file);
        fileWriter.write(bodyRequest);
        fileWriter.flush();
        fileWriter.close();
        // Send the headers
        out.println("HTTP/1.0 201 Created");
      } catch (IOException e) {
        // Send the headers
        out.println("HTTP/1.0 500 Internal Server Error");
      }
    }
    out.println("Date:"+getNow());
    out.println("Server: Bot");
    // this blank line signals the end of the headers
    out.println();
    out.flush();
  }

  /**
   * METHOD HTTP HEAD
   * Send only the header of the ressource specified in the URL
   * The header is the same as the method GET
   * Returns :
   *  - 200 : file sent, ok
   *  - 404 : file not found
   *  - 403 : forbidden
   */
  public void doHead(){
    File file=new File(PATH_TO_DOC+"/index.html");
    if(ressourceAsked.equals("/")){
      out.println("HTTP/1.0 200 OK");
      out.println("Date:"+getNow());
      out.println("Content-Type: text/html");
      out.println("Content-Length: " + file.length());
    }else{
      if(ressourceExist(ressourceAsked)){
        if(ressourceAsked.startsWith("/admin")) {
          file = new File(PATH_TO_DOC + "/403.html");
          // Send the headers
          out.println("HTTP/1.0 403 Forbidden");
          out.println("Date:" + getNow());
          out.println("Content-Type: text/html");
        }else{
          file=new File(PATH_TO_DOC+ressourceAsked);
          String content = getContentType(ressourceAsked);
          // Send the headers
          out.println("HTTP/1.0 200 OK");
          out.println("Date:" + getNow());
          out.println("Content-Type: "+content);
        }
      }else{
        file = new File (PATH_TO_DOC+"/404.html");
        // Send the response
        // Send the headers
        out.println("HTTP/1.0 404 Not found");
        out.println("Date:"+getNow());
        out.println("Content-Type: text/html");
      }
    }
    out.println("Content-Length: " + file.length());
    out.println("Server: Bot");
    // this blank line signals the end of the headers
    out.println();
    out.flush();
  }

  /**
   * METHOD HTTP GET
   * Send the ressource specified in the URL
   * Returns :
   *  - 200 : file sent, ok
   *  - 404 : file not found
   *  - 403 : forbidden
   */
  public void doGet(){
    System.out.println("get called");
    System.out.println("ressource "+ressourceAsked);
    if(ressourceAsked.equals("/")){
      index();
    }else{
      File file;
      if(ressourceExist(ressourceAsked)){
        if(ressourceAsked.startsWith("/admin")) {
          file = new File(PATH_TO_DOC + "/403.html");
          // Send the headers
          out.println("HTTP/1.0 403 Forbidden");
          out.println("Date:" + getNow());
          out.println("Content-Type: text/html");
        }else{
          file=new File(PATH_TO_DOC+ressourceAsked);
          String content = getContentType(ressourceAsked);
          // Send the headers
          out.println("HTTP/1.0 200 OK");
          out.println("Date:" + getNow());
          out.println("Content-Type: "+content);
        }
        out.println("Content-Length: " + file.length());
        out.println("Server: Bot");
        out.println();

      }else{
        file = new File (PATH_TO_DOC+"/404.html");
        // Send the response
        // Send the headers
        out.println("HTTP/1.0 404 Not found");
        out.println("Date:"+getNow());
        out.println("Content-Type: text/html");
        out.println("Content-Length: " + file.length());
        out.println("Server: Bot");
        // this blank line signals the end of the headers
        out.println();
      }
      out.flush();
      // Send the HTML page
      sendFileInBytes(file);
    }
  }

  /**
   * METHOD HTTP NOT ALLOWED
   * When a method not implemented is called
   * Return:
   *  - 405 : method not allowed
   */
  public void doDefault(){
    // Send the response
    // Send the headers
    File file=new File(PATH_TO_DOC+"/405.html");
    out.println("HTTP/1.0 405 Method Not Allowed");
    out.println("Date:"+getNow());
    out.println("Content-Type: text/html");
    out.println("Content-Length: " + file.length());
    out.println("Server: Bot");
    // this blank line signals the end of the headers
    out.println("");
    out.flush();
    // Send the HTML page
    sendFileInBytes(file);
  }

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
        bodyRequest="";
        method="";
        ressourceAsked="";
        ressourceExtension="";
        contentType="";
        contentLength=0;

        // remote is now the connected socket
        System.out.println("Connection, sending data.");
        BufferedReader in = new BufferedReader(new InputStreamReader(
           remote.getInputStream()));

        out = new PrintWriter(remote.getOutputStream());
        outByte=new BufferedOutputStream(remote.getOutputStream());

        // read the data sent. Stop reading once a blank
        // line is hit. This blank line signals the end
        // of the client HTTP headers.
        String str = ".";
        int contentLength1=0;
        while (str != null && !str.equals("")) {
          str = in.readLine();
          request+=str+" ";
        }
        // Treat the request received
        treatRequest();

        // If the request has a body we read it
        if(contentLength>0){
          int read;
          while((read=in.read())!=-1){
            bodyRequest+=(char)read;
            if(bodyRequest.length()==contentLength){
              break;
            }
          }
          System.out.println(bodyRequest);
        }

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
            doHead();
            break;
          default:
            doDefault();
        }
        remote.close();
      } catch (Exception e) {
        System.out.println("Error: " + e);
        e.printStackTrace();
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
    if (fileRequested.endsWith(".png")){
      return "image/png";
    } else if (fileRequested.endsWith(".jpeg")) {
      return "image/jpeg";
    } else if (fileRequested.endsWith(".jpg")){
      return "image/jpg";
    }else if (fileRequested.endsWith(".mp3")){
      return "audio/mp3";
    } else if(fileRequested.endsWith(".wav") ) {
      return "audio/wav";
    }else if (fileRequested.endsWith(".mp4")) {
      return "video/mp4";
    }else if (fileRequested.endsWith(".html")) {
      return "text/html";
    }else if (fileRequested.endsWith(".css")) {
      return "text/css";
    }else if (fileRequested.endsWith(".txt")) {
      return "text/plain;charset=UTF-8";
    }else if (fileRequested.endsWith(".pdf")) {
      return "application/pdf";
    }else if (fileRequested.endsWith(".xml")) {
      return "application/xml";
    }else if (fileRequested.endsWith(".bmp") || fileRequested.endsWith(".webp") || fileRequested.endsWith(".gif")) {
      return "image";
    }else {
      return "application";
    }
  }

  /**
   * Start the application.
   * 
   * @param args
   *            Command line parameters are not used.
   */
  public static void main(String args[]) throws IOException {
    WebServer ws = new WebServer();
    ws.start();
  }
}
