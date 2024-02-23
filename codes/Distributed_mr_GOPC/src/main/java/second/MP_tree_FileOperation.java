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
		//用户目录
		String userDir = "/root/lichshe/";
		
		//PTN文件名
		String[] PTN_FileName =new String[]{"ptn0C0.2S0.5.txt","ptn1C0.3S0.01.txt",
				"ptn2C0.3S0.01.txt","ptn3C0.3S0.025.txt","ptn4C0.3S0.05.txt",
				"ptn5C0.2S0.25.txt","ptn6C0.3S0.05.txt","ptn7C0.2S0.2.txt",
				"ptn8C0.4S0.0025.txt","ptn9C0.3S0.5.txt"};
		
		//Trans文件名
		String[] Trans_FileName = new String[]{"Lug0.txt","Lug1.txt","Lug2.txt","Lug3.txt",
				"Lug4.txt","Lug5.txt","Lug6.txt","Lug7.txt","Lug8.txt","Lug9.txt"};
		
		//PTN处理结果目录
		String[] PTN_Result = new String[]{"/root/lichshe/ptns_one2/","/root/lichshe/ptns_two2/",
				"/root/lichshe/ptns_three2/","/root/lichshe/ptns_four2/","/root/lichshe/ptns_five2/",
				"/root/lichshe/ptns_six2/","/root/lichshe/ptns_seven2/","/root/lichshe/ptns_eight2/",
				"/root/lichshe/ptns_nine2/","/root/lichshe/ptns_ten2/"};
		
		//PTN的HDFS目录
		String[] HDFS_PTN_Dir = new String[]{"/lichshe/ptnses2/ptns_one/","/lichshe/ptnses2/ptns_two/",
				"/lichshe/ptnses2/ptns_three/","/lichshe/ptnses2/ptns_four/","/lichshe/ptnses2/ptns_five/",
				"/lichshe/ptnses2/ptns_six/","/lichshe/ptnses2/ptns_seven/","/lichshe/ptnses2/ptns_eight/",
				"/lichshe/ptnses2/ptns_nine/","/lichshe/ptnses2/ptns_ten/"};
		
		//标识符
		String[] Flags = new String[]{"one_","two_","three_","four_","five_",
				"six_","seven_","eight_","nine_","ten_"};
		//Trans的HDFS目录
		String[] HDFS_Trans_Dir = new String[]{"/lichshe/transes2/trans_one/","/lichshe/transes2/trans_two/",
				"/lichshe/transes2/trans_three/","/lichshe/transes2/trans_four/","/lichshe/transes2/trans_five/",
				"/lichshe/transes2/trans_six/","/lichshe/transes2/trans_seven/","/lichshe/transes2/trans_eight/",
				"/lichshe/transes2/trans_nine/","/lichshe/transes2/trans_ten/"};
		
		for(int i =0;i<10;i++)
		{
			splitfile(userDir,PTN_Result[i],PTN_FileName[i]);
			//ptn输出目录  hdfs目录
			uploadfile(PTN_Result[i],HDFS_PTN_Dir[i],configuration);
			//用户目录  文件 用户目录  hdfs目录
			appendtransfile(userDir,Flags[i],Trans_FileName[i],HDFS_Trans_Dir[i],configuration);
		}
		
	}
	/**
	 * 分割文件
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
			  String line = "";//当前读到的id
			  String tempids = "";
			  String ids = "";//全局id
			  int count = 0;//id的个数
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
			 line = "";//当前行
			 tempids = "";//当前id
			 ids = "";//全局id
			 int fileIdsize = count/filecount; //每个文件的id的个数
			 int filecountnow = 0;//第几个文件
			 
			 int fileIdsizenow = 0;//当前文件的id个数
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
		 * 修改trans文件并上传
		 * 文件名称为one_trans
		 * @param filepath
		 * @param name
		 * @param src
		 * @param dst
		 * @param conf
		 * @throws IOException
		 */
		public static void appendtransfile(String src,String Flag,String name, String dst, Configuration conf) throws IOException
		{
			String finName = src + name;//待操作文件名称
			String foutName = src + Flag + name;//操作后文件名称
			File fin = new File(finName);//待操作文件
			File fout = new File(foutName);//操作后的结果文件
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
		 * 将src下面生成的1.txt-12.txt上传文件到hdfs的dst目录
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
