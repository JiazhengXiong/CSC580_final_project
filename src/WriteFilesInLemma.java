import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Scanner;

import edu.stanford.nlp.simple.Sentence;

/**
 * Class: WriteFilesInLemma
 * Purpose: read all files and output lemma file with titles
 */

/**
 * @author Josh Xiong
 *
 */
public class WriteFilesInLemma {
	public static void main(String[] args) throws IOException {
		File file = new File("./src/source");
        File[] array = file.listFiles();//read all files in the director
        Scanner inputScanner = null;
        String currentWikiPage = "";//the string content for the current wiki page title
        int lemmaNum = 0;//different file name corresponding to the wiki pages file
        for(File wikis: array) {
        	if(wikis.getName().compareTo("questions.txt")==0)continue;//skip the question file
	        inputScanner = new Scanner(wikis,"UTF-8");//scan the file in UTF-8 format
	        while (inputScanner.hasNextLine()) {
	        	String entireLine = inputScanner.nextLine();
			    String[] current = entireLine.split(" ");
			    if(entireLine.indexOf("[[File:")!=-1)continue;//if we found the line starts with "[[File:" we skip over
			    if(current.length==0)continue;//if current line is a newline, skip
			    if(entireLine.indexOf("[[")==0 && entireLine.indexOf("]]")!=-1) {//it is a title, a new page
			    	if(currentWikiPage.length()!=0) {//we find a new title, fillin the wiki page content for previous title
			    		while(currentWikiPage.indexOf("[tpl]")!=-1) {//get rid of all reference lines
			    			currentWikiPage = currentWikiPage.substring(0, currentWikiPage.indexOf("[tpl]"))+currentWikiPage.substring(currentWikiPage.indexOf("[/tpl]")+6);
		        		}
			    		//start lemmatize
			    		Sentence sent = new Sentence(currentWikiPage);
		        		for(String word: sent.lemmas()) {
		        			currentWikiPage += word +" ";//concanate all lemmatization word into string with white space
		        		}
		        		//write into the file with UTF-8 format
				        OutputStream       outputStream       = new FileOutputStream("./src/lemmas/lemmaOutput"+lemmaNum+".txt",true);
				        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream,"UTF-8");

				        outputStreamWriter.write(currentWikiPage+"\n");
				        outputStreamWriter.flush();

				        outputStreamWriter.close();
				        currentWikiPage = "";
			    	}
			    	//write the read title into the file with UTF-8 format
			        OutputStream       outputStream       = new FileOutputStream("./src/lemmas/lemmaOutput"+lemmaNum+".txt",true);
			        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream,"UTF-8");

			        outputStreamWriter.write(entireLine+"\n");
			        outputStreamWriter.flush();

			        outputStreamWriter.close();
			        currentWikiPage = "";
			    }else {
			    	//for everything else, add to the content string
			    	if(entireLine.replaceAll( "[\\pP+~$`^=|<>¡«£à¡ç£Þ£«£½£ü£¼£¾£¤¡Á]" , "").length()==0)continue;
			    	currentWikiPage += (entireLine.replaceAll( "[\\pP+~$`^=|<>¡«£à¡ç£Þ£«£½£ü£¼£¾£¤¡Á]" , "")+" ");
			    }
	        }
	        //to the end of file, if there was no title, and some content still pending, add them to the file
	        if(currentWikiPage.length()!=0) {
	        	while(currentWikiPage.indexOf("[tpl]")!=-1) {
	    			currentWikiPage = currentWikiPage.substring(0, currentWikiPage.indexOf("[tpl]"))+currentWikiPage.substring(currentWikiPage.indexOf("[/tpl]")+6);
        		}
	    		Sentence sent = new Sentence(currentWikiPage);
        		for(String word: sent.lemmas()) {
        			currentWikiPage += word +" ";
        		}
		        OutputStream       outputStream       = new FileOutputStream("./src/lemmas/lemmaOutput"+lemmaNum+".txt",true);
		        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream,"UTF-8");

		        outputStreamWriter.write(currentWikiPage+"\n");
		        outputStreamWriter.flush();

		        outputStreamWriter.close();
	    	}
	        currentWikiPage  = "";
	        lemmaNum++;//increase the num to have a different file name
        }
	}//end main

}//end entile program
