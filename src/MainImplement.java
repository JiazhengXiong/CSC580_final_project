import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.Set;

import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.util.Version;

import edu.stanford.nlp.simple.Sentence;

/**
 * Class: mainImplement
 * Purpose: this class performs Waston by Lucene
 */

/**
 * @author Josh Xiong
 *
 */
public class MainImplement {
    //main method
	public static void main(String[] args) throws IOException {
		BuildIndex invertIndex = null;//according to user input to decide the output invert index
		int doLemma = 0;//change if we need to lemmatize
		Similarity similarity = null;
		boolean pos = false;
		if(args.length==0) {//if no argument, it is a stop word analyzer
			invertIndex = new BuildIndex("stand",false);
			System.out.println("using only stop words");
		}else {
			if(args.length==2 && args[1].compareTo("changeSim")==0) {
				similarity = new ClassicSimilarity();//change similarity class if necessary
			}
			if(args.length==2 && args[1].compareTo("position")==0) {
				pos = true;//involke the position index
			}
			if(args[0].compareTo("lemma")==0) {//input to lemma changes to lemmatizer
				invertIndex = new BuildIndex("lemma", similarity!=null);
				System.out.println("using stop words and lemmatization");
				doLemma = 1;
			}else if(args[0].compareTo("stem")==0) {//stem analyzer
				invertIndex = new BuildIndex("stem", similarity!=null);
				System.out.println("using stop words and stemming");
			}else{
				System.out.println("please enter valid input. Program terminate");
				return;
			}
		}
		if(similarity != null) {
			System.out.println("change BM25 to tf-idf");
		}
		if(pos) {
			System.out.println("started positional index search");
		}
		File question = new File("./src/source/questions.txt");
		int score = 0;//the number we get correct in search
		int numOfQues = 0;//number of over all questions
		
		double mrrScore = 0.0;//the MRR score for the current implementation.
		
		try(Scanner inputScanner = new Scanner(question,"UTF-8")){
			while(inputScanner.hasNextLine()) {
				String catagory = inputScanner.nextLine();//the first line as catogory
				String clue = "";
				String answer = "";
				if(inputScanner.hasNextLine()) {
					clue = inputScanner.nextLine();//second as clue
				}else {
					break;
				}
				if(inputScanner.hasNextLine()) {
					answer = inputScanner.nextLine();//third line as answer
				}else {
					break;
				}
				if(inputScanner.hasNextLine()) {
					inputScanner.nextLine();//the newline, skip over
				}else {
					break;
				}
				//since we find using catagory will increase the performance, we join them together as query
				String resultQuery = catagory+" "+clue;
				if(doLemma == 1) {//if choose to do lemmatization, we use CoreNLP to lemmatize the query
        			String tempCur = resultQuery;
	        		Sentence sent = new Sentence(tempCur);
	        		for(String word: sent.lemmas()) {
	        			resultQuery += word +" ";
	        		}
        		}
				
				Query q = null;
				String queryString = QueryParser.escape(resultQuery).replaceAll( "[\\pP+~$`^=|<>¡«£à¡ç£Þ£«£½£ü£¼£¾£¤¡Á]" , "");
				int k=1;//set word within 1 to have the best performance
				if(pos) {
					//apply stop words analyzer first
					q = new QueryParser("content", invertIndex.analyzer).parse(queryString);
					String[] temp = q.toString("content").split(" ");//get the new query after eliminating stop words
					String newQuery = "";
					for(int i=0; i<temp.length-2;i++) {//get bigram and OR operation for each bigram
						newQuery += "\""+temp[i]+" "+temp[i+1]+"\"~"+k+" OR ";
					}
					if(temp.length == 1) {//special case without bigram
						newQuery = queryString;
					}
					//the last bigram words
					newQuery += "\""+temp[temp.length-2]+" "+temp[temp.length-1]+"\"~"+k;
					newQuery = QueryParser.escape(newQuery);
					q = new QueryParser("content", invertIndex.analyzer).parse(newQuery);
				}else{
					q = new QueryParser("content", invertIndex.analyzer).parse(queryString);
				}
				int hitsPerPage = 100;//consider as k which is the top k number of result
		        IndexReader reader = DirectoryReader.open(invertIndex.index);
		        IndexSearcher searcher = new IndexSearcher(reader);
		        if(similarity!=null) {//change similarity if needed
		        	searcher.setSimilarity(similarity);
		        }
		        TopDocs docs = searcher.search(q, hitsPerPage);
		        int rank = 1;//the rank in MRR score.
		        String[] differAns = answer.split("\\|");//for all answer separate by '|'
		        boolean find = false;
		        for (ScoreDoc hit : docs.scoreDocs) {
		        	find = false;
			        Document d = searcher.doc(hit.doc);
	
			        for(String answerOp: differAns) {
				        if(d.get("title").compareTo(answerOp)==0) {
				        	score++;//if finded, recorded and break out of the loop
				        	find = true;
				        	break;
				        }
			        }
			        if(find) {
			        	break;
			        }
			        rank++;//increase the rank
		        }
		        mrrScore += (double) 1/rank;
				numOfQues++;
			}
		} catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
			e.printStackTrace();
		}
		//the result
		mrrScore /= numOfQues;
		System.out.println("out of "+numOfQues+" questions, the program finds "+score+" correct answers");
		System.out.println("MRR score for current implement is: "+mrrScore);
	}//end main

}//end class
