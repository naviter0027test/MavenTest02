package idv.tst.MavenTest02;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.mysql.cj.xdevapi.PreparableStatement;
/**
 * Hello world!
 *
 */
public class App 
{
	private static String connStr = "jdbc:mysql://192.168.3.40:3306/WarmLove?"
			+ "user=skydep&password=d3c8b2ka"
			+ "&useUnicode=true&characterEncoding=UTF-8";
	private static Connection conn = null;
	private static Statement stmt = null;
	
    public static void main( String[] args )
    {
    	Scanner input = new Scanner(System.in);
        System.out.println( "請輸入帳號" );
        String inputAcc = input.nextLine();
        System.out.println( "請輸入密碼");
        String inputPass = input.nextLine();
        try {
        	connectMariaDB();
        	ArrayList<Map> admins = getAdmin();
        	boolean isLogin = false;
        	for(int i = 0;i < admins.size();++i) {
        		Map map = admins.get(i);
        		String account = (String) map.get("account");
        		String password = (String) map.get("password");
        		if(account.equals(inputAcc) && password.equals(inputPass))
        			isLogin = true;
        		//System.out.println("account:"+account+",pass:"+password);
        	}
        	if(isLogin == false) {
        		System.out.println("輸入帳密有誤");
        		closeMariaDB();
        		return;
        	}
        	System.out.println("登入成功");
        	
        	int choose = 0;
        	while(choose != 9) {
        		System.out.println("操作 1.建立愛心專案 2.新增捐贈紀錄");
            	System.out.println("操作 3.愛心專案列表 4.捐贈紀錄列表");
            	System.out.println("操作 5.愛心專案修改 6.捐贈紀錄修改");
            	System.out.println("操作 7.愛心專案刪除 8.捐贈紀錄刪除");
            	System.out.println("操作 9.Exit");
        		choose = input.nextInt();
	        	switch(choose) {
	        	case 1:
	        		addWarnLoveProject();
	        		break;
	        	case 2:
	        		addWarnLoveDonate();
	        		break;
	        	case 3:
	        		listWarnLoveProject();
	        		break;
	        	case 4:
	        		listWarnLoveDonate();
	        		break;
	        	case 5:
	        		modifyWarnLoveProject();
	        		break;
	        	case 6:
	        		modifyWarnLoveDonate();
	        		break;
	        	case 7:
	        		deleteWarnLoveProject();
	        		break;
	        	case 8:
	        		deleteWarnLoveDonate();
	        		break;
	        	}
        	}
        	closeMariaDB();
        	System.out.println("結束");
        } catch(Exception e) {
    		e.printStackTrace();
    	}
    }
    
    public static void connectMariaDB() throws Exception {
    	//System.out.println("start connect");
    	
    	Class.forName("com.mysql.cj.jdbc.Driver");
    	conn = DriverManager.getConnection(connStr);
    }
    
    public static void showTables() throws Exception {
    	stmt = conn.createStatement();
		String sql = "show tables";
		ResultSet result = stmt.executeQuery(sql);
		if(result != null) {
			while (result.next()) {
				System.out.println(result.getString(1));
			}
		}
		System.out.println("finish2");
    }
    
    public static ArrayList<Map> getAdmin() throws Exception {
    	ArrayList<Map> res = new ArrayList<Map>();
    	stmt = conn.createStatement();
		String sql = "select account, password from admin";
		ResultSet result = stmt.executeQuery(sql);
		if(result != null) {
			while (result.next()) {
				Map<String, String> data = new HashMap<>();
				data.put("account", result.getString(1));
				data.put("password", result.getString(2));
				res.add(data);
			}
		}
		return res;
    }
    
    public static void addWarnLoveProject() throws Exception {
    	String sql = "INSERT INTO Project (`title`, `desc`, `pay`) VALUES (?, ?, ?)";
    	PreparedStatement prepare = conn.prepareStatement(sql);
    	Scanner input = new Scanner(System.in);
    	
    	System.out.println("請輸入標題");
    	String title = input.nextLine();
    	System.out.println("請輸入描述");
    	String desc = input.nextLine();
    	System.out.println("請輸入金額");
    	int pay = input.nextInt();
    	
    	prepare.setString(1, title);
    	prepare.setString(2, desc);
    	prepare.setInt(3, pay);

    	prepare.executeUpdate();
    }
    
    public static void addWarnLoveDonate() throws Exception {
    	
    	stmt = conn.createStatement();

    	Scanner input = new Scanner(System.in);
    	
    	System.out.println("請輸入捐款的專案id");
    	int projectId = input.nextInt();
 
    	String proSql = "select `id`, `pay` from Project where id = "+Integer.toString(projectId);
    	ResultSet proResult = stmt.executeQuery(proSql);
    	int getProjectId = -1, pay = 0;
		if(proResult != null) {
			while (proResult.next()) {
				getProjectId = proResult.getInt(1);
				pay = proResult.getInt(2);
			}
		}
		if(getProjectId == -1) {
			System.out.println("無此捐款專案");
			return;
		}

		String sql = "INSERT INTO Donate (`projectId`, `pay`, `name`, `address`, `status`, `createdDate`, `updatedDate`) VALUES (?, ?, ?, ?, 0, ?, ?)";
    	PreparedStatement prepare = conn.prepareStatement(sql);
    	
    	input.nextLine();
    	System.out.println("請輸入名稱");
    	String name = input.nextLine();
    	System.out.println("請輸入地址");
    	String address = input.nextLine();
    	Timestamp createdDate = new Timestamp(System.currentTimeMillis());

    	prepare.setInt(1, projectId);
    	prepare.setInt(2, pay);
    	prepare.setString(3, name);
    	prepare.setString(4, address);
    	prepare.setTimestamp(5, createdDate);
    	prepare.setTimestamp(6, createdDate);
    	
    	prepare.executeUpdate();
    }
    
    public static void listWarnLoveProject() throws Exception {
    	String proSql = "select `id`, `title`, `desc`, `pay` from Project";
    	ResultSet proResult = stmt.executeQuery(proSql);
    	int projectId = 0, pay = 0;
    	String title = "", desc = "";
    	System.out.println("id\ttitle\tdesc\tpay");
		if(proResult != null) {
			while (proResult.next()) {
				projectId = proResult.getInt(1);
				title = proResult.getString(2);
				desc = proResult.getString(3);
				pay = proResult.getInt(4);
				String showLine = Integer.toString(projectId) + "\t" + title + "\t" + desc + "\t" + Integer.toString(pay);
				System.out.println(showLine);
			}
		}
    }
    
    public static void listWarnLoveDonate() throws Exception {
    	String sql = "select `id`, `pay`, `name`, `address`, `updatedDate` from Donate";
    	ResultSet res = stmt.executeQuery(sql);
    	int donateId = 0, pay = 0;
    	String name = "", address = "";
    	Timestamp updatedDate = null;
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	System.out.println("id\tpay\tname\taddress\t\tupdatedDate");
		if(res != null) {
			while (res.next()) {
				donateId = res.getInt(1);
				pay = res.getInt(2);
				name = res.getString(3);
				address = res.getString(4);
				updatedDate = res.getTimestamp(5);
				String showLine = Integer.toString(donateId) + "\t" + Integer.toString(pay) + "\t" + name + "\t" + address + "\t\t";
				if(updatedDate != null)
					showLine += sdf.format(updatedDate);
				System.out.println(showLine);
			}
		}
    }
    
    public static void modifyWarnLoveProject() throws Exception {
    	stmt = conn.createStatement();

    	Scanner input = new Scanner(System.in);
    	
    	System.out.println("請輸入要修改的捐款的專案id");
    	int projectId = input.nextInt();
    	
    	String proSql = "select `id`, `pay` from Project where id = "+Integer.toString(projectId);
    	ResultSet proResult = stmt.executeQuery(proSql);
    	int getProjectId = -1;
		if(proResult != null) {
			while (proResult.next()) {
				getProjectId = proResult.getInt(1);
			}
		}
		if(getProjectId == -1) {
			System.out.println("無此捐款專案");
			return;
		}
		input.nextLine();

    	String sql = "update Project set `title` = ?, `desc` = ?, `pay` = ? where id = ?";
    	PreparedStatement prepare = conn.prepareStatement(sql);
    	
    	System.out.println("請輸入標題");
    	String title = input.nextLine();
    	System.out.println("請輸入描述");
    	String desc = input.nextLine();
    	System.out.println("請輸入金額");
    	int pay = input.nextInt();
    	
    	prepare.setString(1, title);
    	prepare.setString(2, desc);
    	prepare.setInt(3, pay);
    	prepare.setInt(4, projectId);

    	prepare.executeUpdate();
    }
    
    public static void modifyWarnLoveDonate() throws Exception {
    	stmt = conn.createStatement();

    	Scanner input = new Scanner(System.in);
    	
    	System.out.println("請輸入要修改的捐款的紀錄id");
    	int projectId = input.nextInt();
    	
    	String proSql = "select `id`, `pay` from Donate where id = "+Integer.toString(projectId);
    	ResultSet proResult = stmt.executeQuery(proSql);
    	int getProjectId = -1;
		if(proResult != null) {
			while (proResult.next()) {
				getProjectId = proResult.getInt(1);
			}
		}
		if(getProjectId == -1) {
			System.out.println("無此捐款紀錄");
			return;
		}
		input.nextLine();

    	String sql = "update Donate set `pay` = ?, name = ?, address = ?, `status` = ? where id = ?";
    	PreparedStatement prepare = conn.prepareStatement(sql);

    	System.out.println("請輸入金額");
    	int pay = input.nextInt();
    	input.nextLine();
    	System.out.println("請輸入名稱");
    	String name = input.nextLine();
    	System.out.println("請輸入地址");
    	String address = input.nextLine();
    	System.out.println("請輸入狀態 (1:已付, 0:未付)");
    	int stat = input.nextInt();
    	
    	prepare.setInt(1, pay);
    	prepare.setString(2, name);
    	prepare.setString(3, address);
    	prepare.setInt(4, stat);
    	prepare.setInt(5, projectId);

    	prepare.executeUpdate();
    }
    
    public static void deleteWarnLoveProject() throws Exception {
    	stmt = conn.createStatement();

    	Scanner input = new Scanner(System.in);
    	
    	System.out.println("請輸入要刪除捐款的專案id");
    	int projectId = input.nextInt();

    	String sql = "delete from Project where id = ?";
    	PreparedStatement prepare = conn.prepareStatement(sql);

    	prepare.setInt(1, projectId);

    	prepare.executeUpdate();
    }
    
    public static void deleteWarnLoveDonate() throws Exception {
    	stmt = conn.createStatement();

    	Scanner input = new Scanner(System.in);
    	
    	System.out.println("請輸入要刪除捐款的紀錄id");
    	int projectId = input.nextInt();

    	String sql = "delete from Donate where id = ?";
    	PreparedStatement prepare = conn.prepareStatement(sql);

    	prepare.setInt(1, projectId);

    	prepare.executeUpdate();
    }
    
    public static void closeMariaDB() throws Exception {
    	conn.close();
//    	System.out.println("db close");
    }
}
