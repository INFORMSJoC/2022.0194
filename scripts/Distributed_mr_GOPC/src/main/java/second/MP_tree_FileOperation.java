package second;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class MP_tree_FileOperation {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Configuration configuration = new Configuration();
		//�û�Ŀ¼
		String userDir = "/root/lichshe/";
		
		//PTN�ļ���
		String[] PTN_FileName =new String[]{"ptn0C0.2S0.5.txt","ptn1C0.3S0.01.txt",
				"ptn2C0.3S0.01.txt","ptn3C0.3S0.025.txt","ptn4C0.3S0.05.txt",
				"ptn5C0.2S0.25.txt","ptn6C0.3S0.05.txt","ptn7C0.2S0.2.txt",
				"ptn8C0.4S0.0025.txt","ptn9C0.3S0.5.txt"};
		
		//Trans�ļ���
		String[] Trans_FileName = new String[]{"Lug0.txt","Lug1.txt","Lug2.txt","Lug3.txt",
				"Lug4.txt","Lug5.txt","Lug6.txt","Lug7.txt","Lug8.txt","Lug9.txt"};
		
		//PTN������Ŀ¼
		String[] PTN_Result = new String[]{"/root/lichshe/ptns_one2/","/root/lichshe/ptns_two2/",
				"/root/lichshe/ptns_three2/","/root/lichshe/ptns_four2/","/root/lichshe/ptns_five2/",
				"/root/lichshe/ptns_six2/","/root/lichshe/ptns_seven2/","/root/lichshe/ptns_eight2/",
				"/root/lichshe/ptns_nine2/","/root/lichshe/ptns_ten2/"};
		
		//PTN��HDFSĿ¼
		String[] HDFS_PTN_Dir = new String[]{"/lichshe/ptnses2/ptns_one/","/lichshe/ptnses2/ptns_two/",
				"/lichshe/ptnses2/ptns_three/","/lichshe/ptnses2/ptns_four/","/lichshe/ptnses2/ptns_five/",
				"/lichshe/ptnses2/ptns_six/","/lichshe/ptnses2/ptns_seven/","/lichshe/ptnses2/ptns_eight/",
				"/lichshe/ptnses2/ptns_nine/","/lichshe/ptnses2/ptns_ten/"};
		
		//��ʶ��
		String[] Flags = new String[]{"one_","two_","three_","four_","five_",
				"six_","seven_","eight_","nine_","ten_"};
		//Trans��HDFSĿ¼
		String[] HDFS_Trans_Dir = new String[]{"/lichshe/transes2/trans_one/","/lichshe/transes2/trans_two/",
				"/lichshe/transes2/trans_three/","/lichshe/transes2/trans_four/","/lichshe/transes2/trans_five/",
				"/lichshe/transes2/trans_six/","/lichshe/transes2/trans_seven/","/lichshe/transes2/trans_eight/",
				"/lichshe/transes2/trans_nine/","/lichshe/transes2/trans_ten/"};
		
		for(int i =0;i<10;i++)
		{
			splitfile(userDir,PTN_Result[i],PTN_FileName[i]);
			//ptn���Ŀ¼  hdfsĿ¼
			uploadfile(PTN_Result[i],HDFS_PTN_Dir[i],configuration);
			//�û�Ŀ¼  �ļ� �û�Ŀ¼  hdfsĿ¼
			appendtransfile(userDir,Flags[i],Trans_FileName[i],HDFS_Trans_Dir[i],configuration);
		}
		
	}
	/**
	 * �ָ��ļ�
	 * @param filepath
	 * @param name
	 * @throws IOException
	 */
		public static void splitfile(String filepath,String tofilepath,String name) throws IOException
		  {
			File dir = new File(tofilepath);
		      if(!dir.exists())
		      {
		    	  dir.mkdirs();
		      }
			  File f = new File(filepath + name);
			  BufferedReader br = new BufferedReader(new FileReader(f));
			  BufferedReader br2 = new BufferedReader(new FileReader(f));
			  BufferedWriter bw = null;
			  String line = "";//��ǰ������id
			  String tempids = "";
			  String ids = "";//ȫ��id
			  int count = 0;//id�ĸ���
			  int filecount  = 12;
			  while((line = br.readLine())!=null)
			  {
				  tempids = line.trim().split(" ")[0];
				  if(!tempids.equals(ids))
				  {
					  ids = tempids;
					  count++;
				  }
			  }
			 br.close();		
			 line = "";//��ǰ��
			 tempids = "";//��ǰid
			 ids = "";//ȫ��id
			 int fileIdsize = count/filecount; //ÿ���ļ���id�ĸ���
			 int filecountnow = 0;//�ڼ����ļ�
			 
			 int fileIdsizenow = 0;//��ǰ�ļ���id����
			 while((line = br2.readLine())!=null)
			 {
				 if(filecountnow<filecount)
				 {
					 tempids = line.trim().split(" ")[0];
					 if((!tempids.equals(ids))&&(fileIdsizenow>=fileIdsize))
					 {
						 filecountnow++;
						 bw.close();
						 bw = new BufferedWriter(new FileWriter(new File(tofilepath + filecountnow +".txt")));
						 ids = tempids;
						 fileIdsizenow = 1;
					 }
					 else
					 {
						 if(fileIdsizenow ==0)
					  		{
							 filecountnow++;
							 bw = new BufferedWriter(new FileWriter(new File(tofilepath + filecountnow +".txt")));
					  		}
						 if(!tempids.equals(ids))
						 	{
							 fileIdsizenow++;
						 	}
							 ids = tempids;
					 }
				 }
				 bw.write(filecountnow +" "+ line);
				 bw.newLine();
			 }
			 bw.close();
			 br2.close();
		  }
		/**
		 * �޸�trans�ļ����ϴ�
		 * �ļ�����Ϊone_trans
		 * @param filepath
		 * @param name
		 * @param src
		 * @param dst
		 * @param conf
		 * @throws IOException
		 */
		public static void appendtransfile(String src,String Flag,String name, String dst, Configuration conf) throws IOException
		{
			String finName = src + name;//�������ļ�����
			String foutName = src + Flag + name;//�������ļ�����
			File fin = new File(finName);//�������ļ�
			File fout = new File(foutName);//������Ľ���ļ�
			if(!fout.exists())
			{
				fout.createNewFile();
			}
			BufferedReader br = new BufferedReader(new FileReader(fin));
			BufferedWriter bw = new BufferedWriter(new FileWriter(fout));
			String s = "";
			int count = 1;
			while((s = br.readLine())!=null)
			{
				bw.write(count + " " + s);
				bw.newLine();
				count++;
			}
			br.close();
			bw.flush();
			bw.close();
			FileSystem hdfs = FileSystem.get(conf);
			Path dstPath = new Path(dst);
			if(!hdfs.exists(dstPath))
			{
				hdfs.mkdirs(dstPath);
			}
			hdfs.copyFromLocalFile(false, new Path(foutName), dstPath);
			hdfs.copyFromLocalFile(false, new Path(finName), dstPath);
			hdfs.close();
		}
		
		/**
		 * ��src�������ɵ�1.txt-12.txt�ϴ��ļ���hdfs��dstĿ¼
		 * @param src
		 * @param dst
		 * @param conf
		 * @return
		 * @throws IOException
		 */
		  public static boolean uploadfile(String src , String dst , Configuration conf) throws IOException
		  {
			  Path dstPath = new Path(dst);
			  FileSystem hdfs = FileSystem.get(conf);
			  if(!hdfs.exists(dstPath))
				{
					hdfs.mkdirs(dstPath);
				}
		      try{  
		            
				  for(int i=1; i<=12; i++){			
				    hdfs.copyFromLocalFile(false, new Path(src + i + ".txt"), dstPath);
				  } 
				  hdfs.close();
		      }catch(IOException ie){  
		          ie.printStackTrace() ;  
		          return false ;  
		      }  
		      return true ;  
		  } 
}
