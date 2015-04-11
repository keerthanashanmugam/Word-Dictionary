package Word_dictionary;

import java.sql.*;

public class dataBase {
	   // JDBC driver name and database URL
	   static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	   static final String DB_URL = "jdbc:mysql://localhost/Dict";

	   //  Database credentials
	   static final String USER = "root";
	   static final String PASS = "keeRthi555&";
	   static Connection conn = null;
	   static Statement stmt = null;
	  // static TernarySearchTree tree;
	   public static TernarySearchTree Make_Dictionary() {
	   
		
		TernarySearchTree tree = new TernarySearchTree();   
		   
		   
	   try{
	      //STEP 2: Register JDBC driver
	      Class.forName("com.mysql.jdbc.Driver");

	      //STEP 3: Open a connection
	   //   System.out.println("Connecting to database...");
	      conn = DriverManager.getConnection(DB_URL,USER,PASS);

	      //STEP 4: Execute a query
	    //  System.out.println("Creating statement...");
	      
	      String sql;
	      stmt = conn.createStatement();
	      sql = "SELECT * FROM wn_synset";
	      
	      ResultSet rs = stmt.executeQuery(sql);
	      String s = null ;
	      while(rs.next()){
	         //Retrieve by column name
	         String  synset_id  = rs.getString("synset_id");
	         String wnum = rs.getString("w_num");
	         s = rs.getString("word");
	         Node new_node = new Node();
	         get_antonyms(synset_id,wnum,new_node);
	         get_gloss(synset_id,wnum,new_node);
	         get_synonyms(synset_id,wnum,new_node);
	     //    System.out.println("Completed!!!!");
	         tree.calInsert(s,new_node);
	      } 
	      //STEP 6: Clean-up environment
	      rs.close();
	      
	      stmt.close();
	      conn.close();
	   }catch(SQLException se){
	      //Handle errors for JDBC
	      se.printStackTrace();
	   }catch(Exception e){
	      //Handle errors for Class.forName
	      e.printStackTrace();
	   }finally{
	      //finally block used to close resources
	      try{
	         if(stmt!=null)
	            stmt.close();
	      }catch(SQLException se2){
	      }// nothing we can do
	      try{
	         if(conn!=null)
	            conn.close();
	      }catch(SQLException se){
	         se.printStackTrace();
	      }//end finally try
	   }//end try
	   System.out.println("Goodbye!");
	return tree;

	   }
	   
	   private static void get_antonyms(String synset_id,String wnum,Node new_node) throws SQLException{
		   String sql_data1;
		   sql_data1 = "SELECT wn_antonym.synset_id_1, wn_antonym.wnum_1 , wn_synset.word FROM wn_antonym "
	         		+ " INNER JOIN wn_synset ON "
	         		+" wn_antonym.synset_id_1 = wn_synset.synset_id and wn_antonym.wnum_1 = wn_synset.w_num"
	        		+" WHERE wn_antonym.synset_id_2 = " +"'"+synset_id +"'"+" and wn_antonym.wnum_2 ="+ "'"+wnum+"'" ;
	         	         
	         stmt = conn.createStatement();
	         ResultSet rs1 = stmt.executeQuery(sql_data1);
	         
	         String d_word = null;//sql_ant_word for query and d_word is actual antonyms
	      
	         
	         while(rs1.next())
	         {

	        		 d_word = rs1.getString("word");
	        		 new_node.antonyms.add(d_word);		//adding every antonym

	         }
	         
	     //    if(new_node.antonyms.size()>=1)
	      //   {
	    //    	System.out.println("Antonyms:  "+new_node.antonyms);
	        //	 new_node.print();
	      //   }
	        

	         rs1.close();//i++;
	      	   
	   }
	   
	   //////Get_synonyms/////////////
	   private static void get_synonyms(String synset_id,String wnum,Node new_node) throws SQLException{
		   String sql_data1;
		   sql_data1 = "SELECT wn_similar.synset_id_1,  wn_synset.word FROM wn_similar "
	         		+ " INNER JOIN wn_synset ON "
	         		+" wn_similar.synset_id_1 = wn_synset.synset_id "
	        		+" WHERE wn_similar.synset_id_2 = " +"'"+synset_id +"'" ;
	         	         
	         stmt = conn.createStatement();
	         ResultSet rs1 = stmt.executeQuery(sql_data1);
	         
	         String d_word = null;//sql_ant_word for query and d_word is actual antonyms
	      
	         
	         while(rs1.next())
	         {

	        		 d_word = rs1.getString("word");
	        		 new_node.synonyms.add(d_word);		//adding every antonym

	         }
	         
	      //   if(new_node.synonyms.size()>=1)
	       //  {
	      //  	System.out.println("Synonyms: "+new_node.synonyms);
//	        	 new_node.print();
	       //  }
	        

	         rs1.close();//i++;
	      
	   
	   
	   }
	   ///////////Get_gloss////////////
	   
	   private static void get_gloss(String synset_id,String wnum,Node new_node) throws SQLException{
		   String sql_data1;
		   sql_data1 = "SELECT  wn_gloss.gloss FROM wn_gloss "
	         	         		
	        		+" WHERE wn_gloss.synset_id = " +"'"+synset_id +"'" ;
	         	         
	         stmt = conn.createStatement();
	         ResultSet rs1 = stmt.executeQuery(sql_data1);
	         
	         String d_word = null;//sql_ant_word for query and d_word is actual antonyms
	      
	         
	         while(rs1.next())
	         {

	        		 d_word = rs1.getString("gloss");
	        		 new_node.meaning=(d_word);		//adding every antonym

	         }
	      //   System.out.println("meaning: "+new_node.meaning);
	         /*if(new_node.meaning.size()>1)
	         {
	        	//System.out.println("string: "+s+" number :" + i);
	        	 new_node.print();
	         }*/
	        

	         rs1.close();//i++;
	      
	   
	   
	   }
	   
	   
	   
	   
}