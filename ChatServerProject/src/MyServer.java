import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class MyServer {
private ServerSocket server;
private JSONArray jsonArray;

	
	//사용자 객체들을 관리하는 ArrayList
	ArrayList<UserClass> user_list;
	public static void main(String[] args) {
		new MyServer();
	}	
	
    
	public MyServer() {
		try {
			user_list=new ArrayList<UserClass>();
			// 서버 가동
			server=new ServerSocket(40000);
			
			getJsonString();
			
			// 사용자 접속 대기 스레드 가동
			ConnectionThread thread= new ConnectionThread();
			thread.start();
		}catch(Exception e) {e.printStackTrace();}
	}
	
	public synchronized void sendToClient(String msg) {
		try {
			// 사용자의 수만큼 반복
			for (UserClass user : user_list) {
				// 메세지를 클라이언트들에게 전달한다.
				user.dos.writeUTF(msg);
				
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private String getJsonString()
    {
        String json = "";

        
        	JSONParser parser = new JSONParser(); 
        	
        	try { 
        		FileReader reader = new FileReader("chatList.json"); 
        		Object obj = parser.parse(reader); 
        		JSONObject jsonObject = (JSONObject) obj;
        		
        		jsonArray = (JSONArray) jsonObject.get("chatting");
        		
        		
            	

        		
        		reader.close(); 
        		
        	} 
        	catch (IOException | ParseException e) {
        		e.printStackTrace(); 
        	}

        return json;
    }
	

	
	
	


	
	//사용자 접속 대기를 처리하는 스레드 클래스
		class ConnectionThread extends Thread{
				
			@Override
			public void run() {
				// TODO Auto-generated method stub
				

				try {
					while(true) {
						System.out.println("사용자 접속 대기");
						Socket socket=server.accept();
						System.out.println("사용자가 접속하였습니다.");
						// 사용자 닉네임을 처리하는 스레드 가동
						NickNameThread thread=new NickNameThread(socket);
						thread.start();
						
					}
				}catch(Exception e) {e.printStackTrace();}
			}
		}


	//닉네임 입력처리 스레드
			class NickNameThread extends Thread{
				private Socket socket;
				
				public NickNameThread(Socket socket) {
					this.socket=socket;
				}
				public void run() {
					try {
						// 스트림 추출
						InputStream is = socket.getInputStream();
						OutputStream os= socket.getOutputStream();
						DataInputStream dis=new DataInputStream(is);
						DataOutputStream dos=new DataOutputStream(os);
						
						//닉네임 수신
						String nickName=dis.readUTF();
						// 환영 메세지를 전달한다.
						dos.writeUTF(nickName+" 님 환영합니다.");
						// 기 접속된 사용자들에게 접속 메세지를 전달한다.
						sendToClient("서버 : "+nickName+"님이 접속하였습니다.");
						// 사용자 정보를 관리하는 객체를 생성한다.
						UserClass user= new UserClass(nickName,socket);
						user.start();
						user_list.add(user);
					}catch(Exception e) {e.printStackTrace();}
				}
			}
			
			
			// 사용자 정보를 관리하는 클래스
			class UserClass extends Thread {
				String nickName;
				Socket socket;
				DataInputStream dis;
				DataOutputStream dos;
				
				public UserClass(String nickName,Socket socket) {
					try {
					this.nickName=nickName;
					this.socket=socket;
					InputStream is=socket.getInputStream();
					OutputStream os=socket.getOutputStream();
					dis = new DataInputStream(is);
					dos=new DataOutputStream(os);
					
					}catch(Exception e) {
						e.printStackTrace();
					}
				}
				// 사용자로부터 메세지를 수신받는 스레드
				public void run() {
					try {
						while(true) {
							//클라이언트에게 메세지를 수신받는다.
							String msg=dis.readUTF();
							
							//클라이언트 수신메세지 콘솔창 출
							System.out.println("["+nickName + "] : "+ msg);
							// 사용자들에게 메세지를 전달한다
							sendToClient(nickName+" : "+ msg); 
							
							
							
							ServerClass server= new ServerClass(msg,socket);
							server.start();
							
						}
					}catch(Exception e) {
						e.printStackTrace();
					}
				}
	
			}
			
			// 사용자에게 메시지를 답변하는 클래스 
			class ServerClass extends Thread {
				String msg;
				Socket socket;
				DataInputStream dis;
				DataOutputStream dos;
				Scanner scanner;
				String answer;
				
				
				public ServerClass(String msg,Socket socket) {
					try {
					this.msg=msg;
					this.socket=socket;
					InputStream is=socket.getInputStream();
					OutputStream os=socket.getOutputStream();
					dis = new DataInputStream(is);
					dos=new DataOutputStream(os);
					//scanner = new Scanner(System.in);
					
					
					}catch(Exception e) {
						e.printStackTrace();
					}
				}
				
				
				// 사용자에게 메세지를 보내는쓰레
				public void run() {
					
					  try{
						  
						  JSONObject element; 
						  String resultMsg = "무슨 말씀이신지 잘 모르겠네요.";
						  for (int i = 0; i < jsonArray.size(); i++) {
							  element = (JSONObject) jsonArray.get(i);
							  String serverMsg = (String) element.get("server");
						      String clientMsg = (String) element.get("client");
						      
						      if(msg.contains(clientMsg)) {
						    	  resultMsg = serverMsg;
						    	  break;
						      }
						  }


						  
						  sendToClient("SERVER : "+resultMsg);
						  
						  //직접 서버에서 메세지 송신하기(주석처리)
//			                while(scanner.hasNextLine()){
//			                    // 서버로부터 데이터를 수신받는다.
//			                	msg = scanner.nextLine();
//								
//								//클라이언트에게 메세지를 보낸다.
//								// 결과 출력
//								System.out.println("[SERVER] : " + msg);
//
//						
//							// 사용자들에게 메세지를 전달한다
//							sendToClient( "SERVER : "+ msg); 
//			                }
						}
		                catch(Exception e) {
							e.printStackTrace();
							sendToClient( "[SERVER] : 잠시 문제가 있습니다."); 
						}finally{
								
						}
				}
				
				
			}
}