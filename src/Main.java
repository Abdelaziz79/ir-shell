import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Scanner;


public class Main {
    public static void main(String[] args) throws IOException, ParseException {

        StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_42);
        Directory indexLoc = FSDirectory.open(new File("index-dir-2"));
        Scanner sc = new Scanner(System.in);

        System.out.println("Welcome to our Shell 😊😊😊");

        while (true) {

            String query = sc.nextLine();
            LinkedList<String> queryStr = getFunctionAndParam(query);

            if (queryStr.isEmpty()) {
                continue;
            }

            switch (queryStr.get(0)) {
                case "add":
                    for (int i = 1; i < queryStr.size(); i += 2) {
                        SectionTask.addDocument(openNewIndexWriter("index-dir-2"), queryStr.get(i), queryStr.get(i + 1));
                    }
                    System.out.println("Document(s) added successfully \n");
                    break;
                case "search":
                    SectionTask.search(analyzer, indexLoc, queryStr.get(1));
                    break;
                case "returnresults":
                    SectionTask.returnResults(indexLoc, queryStr.get(1));
                    break;
                case "returnallresults":
                    SectionTask.returnAllResults(indexLoc);
                    break;
                case "delete":
                    SectionTask.deleteDocument(openNewIndexWriter("index-dir-2"), queryStr.get(1));
                    System.out.println("Document deleted successfully \n");
                    break;
                case "update":
                    SectionTask.updateDocument(openNewIndexWriter("index-dir-2"), queryStr.get(1), queryStr.get(2));
                    System.out.println("Document updated successfully \n");
                    break;
                case "deleteall":
                    SectionTask.deleteAllDocuments(openNewIndexWriter("index-dir-2"));
                    System.out.println("All documents deleted successfully \n");
                    break;
                case "exit":
                    System.exit(0);
                    break;
                case "standard":
                    SectionTask.createStandardAnalyzer(queryStr.get(1));
                    break;
                case "whitespace":
                    SectionTask.createWhiteSpaceAnalyzer(queryStr.get(1));
                    break;
                case "stop":
                    SectionTask.createStopAnalyzer(queryStr.get(1));
                    break;
                case "keyword":
                    SectionTask.createKeywordAnalyzer(queryStr.get(1));
                    break;
                case "simple":
                    SectionTask.createSimpleAnalyzer(queryStr.get(1));
                    break;
                case "matrix":
                    for (int i = 1; i < queryStr.size(); i++) {
                        SectionTask.matrix(indexLoc, queryStr.get(i));
                    }
                    break;
                case "invertedindex":
                    for (int i = 1; i < queryStr.size(); i++) {
                        SectionTask.invertedIndex(indexLoc, queryStr.get(i));
                    }
                    break;
                case "help":
                    System.out.println("add <ISBN> <title>");
                    System.out.println("search <title>");
                    System.out.println("returnResults <title>");
                    System.out.println("returnAllResults");
                    System.out.println("delete <ISBN>");
                    System.out.println("update <ISBN> <title>");
                    System.out.println("deleteAll");
                    System.out.println("exit");
                    System.out.println("standard <query>");
                    System.out.println("whitespace <query>");
                    System.out.println("stop <query>");
                    System.out.println("keyword <query>");
                    System.out.println("simple <query>");
                    System.out.println("matrix <query>");
                    System.out.println("invertedIndex <query>");
                    break;
                default:
                    if (!query.isEmpty())
                        System.out.println("Wrong command type help to see the list of commands");
                    System.out.println();
                    break;
            }

        }

    }

    private static IndexWriter openNewIndexWriter(String indexPath) {
        try {
            Directory directory = FSDirectory.open(new File(indexPath));
            IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_42, new StandardAnalyzer(Version.LUCENE_42));
            return new IndexWriter(directory, config);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static LinkedList<String> getFunctionAndParam(String queryStr) {
        String q;
        LinkedList<String> res = new LinkedList<>();


        for (int i = 0; i < queryStr.length(); i++) {
            if (queryStr.charAt(i) == '(') {
                q = queryStr.substring(0, i).toLowerCase();
                String[] params = queryStr.substring(i + 1, queryStr.length() - 1).split(",");
                for (int j = 0; j < params.length; j++) {
                    params[j] = params[j].trim().replace("\"", "").replace("[", "").replace("]", "");
                }
                res.add(q);
                res.addAll(Arrays.asList(params));
                return res;
            }
        }
        res.add(queryStr.toLowerCase());

        return res;
    }


}