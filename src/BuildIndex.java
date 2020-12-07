import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import edu.stanford.nlp.simple.Sentence;

/**
 * 
 */

/**
 * @author Administrator
 *
 */
public class BuildIndex {
	//all instance need for indexing
	Analyzer analyzer;
	Directory index;
	IndexWriterConfig config;
	IndexWriter w;
	Similarity similarity;
	
	//constructor
	BuildIndex(String input, boolean simi) throws IOException{
		if(simi) {//if required to change similiary fuction
			similarity = new ClassicSimilarity();
		}
		this.buildindex(input);
	}
    
	//method: build index with default similarity
    private void buildindex(String input) throws IOException {
        //Get file from resources folder
        File file = null;
        //check if the user want stop words only or with stem and lemmatization
        if(input.compareTo("stand")==0) {
        	analyzer = new StandardAnalyzer();
        	file = new File("./src/source");
        }else if(input.compareTo("stem")==0) {
        	analyzer = new EnglishAnalyzer();
        	file = new File("./src/source");
        }else {
        	analyzer = new StandardAnalyzer();
        	file = new File("./src/lemmas");
        }
        File[] array = file.listFiles();
        
		index = new RAMDirectory();

    	config = new IndexWriterConfig(analyzer);
    	if(similarity!=null) {//change similarity class to tf-idf if asked
    		config.setSimilarity(similarity);
    	}

    	w = new IndexWriter(index, config);//initialize the index writer

    	String currentContent = "";
    	Document doc = new Document();
    	Scanner inputScanner = null;
    	
        for(int i=0; i<array.length;i++) {//for all the files
        	if(array[i].getName().compareTo("questions.txt")==0)continue;//skip question text
	        inputScanner = new Scanner(array[i],"UTF-8");//read in UTF-8 format
		    while (inputScanner.hasNextLine()) {
		        String entireLine = inputScanner.nextLine();
		        String[] current = entireLine.split(" ");
		        if(entireLine.indexOf("[[File:")!=-1)continue;
		        if(current.length==0)continue;
		        if(entireLine.indexOf("[[")==0 && entireLine.indexOf("]]")!=-1) {//it is a title, a new page
		        	if(doc.get("title")==null) {//if current doc has no title, then copy the title
		        		currentContent = "";
		        		doc.add(new StringField("title", entireLine.replaceAll( "[\\pP+~$`^=|<>¡«£à¡ç£Þ£«£½£ü£¼£¾£¤¡Á]" , ""), Field.Store.YES));
		        	}else {//otherwise, this is the end of previous doc and start with a new doc. Copy the content into the previous doc
		        		while(currentContent.indexOf("[tpl]")!=-1) {
		        			currentContent = currentContent.substring(0, currentContent.indexOf("[tpl]"))+currentContent.substring(currentContent.indexOf("[/tpl]")+6);
		        		}
		        		doc.add(new TextField("content", currentContent, Field.Store.YES));
		        		w.addDocument(doc);
		        		currentContent = "";
			        	doc = new Document();
		        		doc.add(new StringField("title", entireLine.replaceAll( "[\\pP+~$`^=|<>¡«£à¡ç£Þ£«£½£ü£¼£¾£¤¡Á]" , ""), Field.Store.YES));
		        	}
		        }else {//if it is not the title line, copy into content
		        	if(entireLine.replaceAll( "[\\pP+~$`^=|<>¡«£à¡ç£Þ£«£½£ü£¼£¾£¤¡Á]" , "").length()==0)continue;
	            	currentContent += entireLine.replaceAll( "[\\pP+~$`^=|<>¡«£à¡ç£Þ£«£½£ü£¼£¾£¤¡Á]" , "")+" ";
		        }
		   }
		    //to the end of file, if there are some content left that has not add to the doc, add it into
		    if(doc.get("title")!=null) {
		    	while(currentContent.indexOf("[tpl]")!=-1) {
        			currentContent = currentContent.substring(0, currentContent.indexOf("[tpl]"))+currentContent.substring(currentContent.indexOf("[/tpl]")+6);
        		}
			   doc.add(new TextField("content", currentContent, Field.Store.YES));
			   w.addDocument(doc);
		    }
		   currentContent = "";
		   doc = new Document();
	       inputScanner.close();
	    }
        w.close();
        System.out.println("finished indexing");
    }//end buildIndex()
}//end the object
