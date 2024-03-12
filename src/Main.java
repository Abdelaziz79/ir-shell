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

class Doc {
    public String ISBN;
    public String title;

    public Doc(String ISBN, String title) {
        this.ISBN = ISBN;
        this.title = title;
    }
}

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

    static LinkedList<String> getFunctionAndParam(String queryStr) {
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

    static class SectionTask {
        static void addDocuments(IndexWriter writer, Doc[] docs) throws IOException {

            for (Doc doc : docs) {
                addDoc(writer, doc.title, doc.ISBN);
            }
            writer.close();
            System.out.println("Indexing completed successfully.");
        }

        static void addDocument(IndexWriter writer, String ISBN, String title) throws IOException {
            addDoc(writer, title, ISBN);
            writer.close();

        }

        static void search(Analyzer analyzer, Directory indexLoc, String queryStr) throws IOException {
            Query query = null;
            try {
                query = new QueryParser(Version.LUCENE_42, "title", analyzer).parse(queryStr);

            } catch (Exception e) {
                e.printStackTrace();
            }

            IndexReader reader = DirectoryReader.open(indexLoc);
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs hits = searcher.search(query, 10);
            System.err.println("Found " + hits.totalHits + " document (s) " + " that matched query '" + queryStr + "':");
            System.out.println("Searching finished successfully \n");
            reader.close();
        }

        static void returnResults(Directory indexLoc, String queryStr) throws IOException {
            IndexReader reader = DirectoryReader.open(indexLoc);
            IndexSearcher searcher = new IndexSearcher(reader);
            Query query = null;
            try {
                query = new QueryParser(Version.LUCENE_42, "title", new StandardAnalyzer(Version.LUCENE_42)).parse(queryStr);

            } catch (Exception e) {
                e.printStackTrace();
            }
            TopDocs hits = searcher.search(query, 10);
            if (hits.totalHits == 0) {
                System.out.println("No results found");
                return;
            }
            System.out.println("ISBN\tTitle");
            for (ScoreDoc scoreDoc : hits.scoreDocs) {
                int docId = scoreDoc.doc;
                Document d = searcher.doc(docId);
                System.out.println(d.get("isbn") + "\t" + d.get("title"));

            }
        }

        static void matrix(Directory indexLoc, String queryStr) throws IOException {
            try (IndexReader reader = DirectoryReader.open(indexLoc)) {
                IndexSearcher searcher = new IndexSearcher(reader);
                Query query = new MatchAllDocsQuery();
                TopDocs hits = searcher.search(query, reader.maxDoc());
                System.out.print("\t\t\t");
                int i = 0;
                for (ScoreDoc scoreDoc : hits.scoreDocs) {
                    i++;
                    System.out.print("doc" + i + "\t");
                }
                System.out.println();
                StringBuilder queryStrBuilder = new StringBuilder(queryStr);
                while (queryStrBuilder.length() < 10) {
                    queryStrBuilder.append(" ");
                }

                System.out.print(queryStrBuilder.toString() + "\t");
                for (ScoreDoc scoreDoc : hits.scoreDocs) {
                    int docId = scoreDoc.doc;
                    Document d = searcher.doc(docId);
                    if (d.get("title").contains(queryStr)) System.out.print("1\t\t");
                    else System.out.print("0\t\t");
                }
                System.out.println();
                System.out.println("---------------------------------------------------------------------------------------------------------------------------------------");
            }
        }

        static void invertedIndex(Directory indexLoc, String queryStr) throws IOException {
            try (IndexReader reader = DirectoryReader.open(indexLoc)) {
                IndexSearcher searcher = new IndexSearcher(reader);
                Query query = new MatchAllDocsQuery();
                TopDocs hits = searcher.search(query, reader.maxDoc());

                StringBuilder queryStrBuilder = new StringBuilder(queryStr);
                while (queryStrBuilder.length() < 10) {
                    queryStrBuilder.append(" ");
                }
                int i = 0;
                System.out.print(queryStrBuilder.toString() + "\t");
                for (ScoreDoc scoreDoc : hits.scoreDocs) {
                    i++;
                    int docId = scoreDoc.doc;
                    Document d = searcher.doc(docId);
                    if (d.get("title").contains(queryStr)) System.out.print("doc" + i + ",\t");
                }
                System.out.println();
                System.out.println("---------------------------------------------------------------------------------------------------------------------------------------");
            }
        }

        static void returnAllResults(Directory indexLoc) throws IOException {
            try (IndexReader reader = DirectoryReader.open(indexLoc)) {
                IndexSearcher searcher = new IndexSearcher(reader);
                Query query = new MatchAllDocsQuery();
                TopDocs hits = searcher.search(query, reader.maxDoc());

                System.out.println("\t\tISBN\tTitle");
                int i = 0;
                for (ScoreDoc scoreDoc : hits.scoreDocs) {
                    i++;
                    int docId = scoreDoc.doc;
                    Document d = searcher.doc(docId);
                    System.out.println("doc" + i + "\t" + d.get("isbn") + "\t" + d.get("title"));
                }
            }
        }

        public static void addDoc(IndexWriter iw, String title, String isbn) throws IOException {
            Document doc = new Document();
            doc.add(new TextField("title", title, Field.Store.YES));
            doc.add(new StringField("isbn", isbn, Field.Store.YES));
            iw.addDocument(doc);

        }

        static void createStandardAnalyzer(String queryStr) throws IOException {
            StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_42);
            StringReader reader = new StringReader(queryStr);
            TokenStream tokenStream = analyzer.tokenStream("", reader);
            CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            System.out.print("Standard Analyzer => : ");
            while (tokenStream.incrementToken()) {
                System.out.print(charTermAttribute.toString() + ", ");
            }
            System.out.println();
        }

        static void createWhiteSpaceAnalyzer(String queryStr) throws IOException {
            WhitespaceAnalyzer analyzer = new WhitespaceAnalyzer(Version.LUCENE_42);
            StringReader reader = new StringReader(queryStr);
            TokenStream tokenStream = analyzer.tokenStream("", reader);
            CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            System.out.print("WhiteSpace Analyzer => : ");
            while (tokenStream.incrementToken()) {
                System.out.print(charTermAttribute.toString() + ", ");
            }
            System.out.println();
        }

        static void createStopAnalyzer(String queryStr) throws IOException {
            StopAnalyzer analyzer = new StopAnalyzer(Version.LUCENE_42);
            StringReader reader = new StringReader(queryStr);
            TokenStream tokenStream = analyzer.tokenStream("", reader);
            CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            System.out.print("Stop Analyzer => : ");
            while (tokenStream.incrementToken()) {
                System.out.print(charTermAttribute.toString() + ", ");
            }
            System.out.println();
        }

        static void createKeywordAnalyzer(String queryStr) throws IOException {
            KeywordAnalyzer analyzer = new KeywordAnalyzer();
            StringReader reader = new StringReader(queryStr);
            TokenStream tokenStream = analyzer.tokenStream("", reader);
            CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            System.out.print("Keyword Analyzer => : ");
            while (tokenStream.incrementToken()) {
                System.out.print(charTermAttribute.toString() + ", ");
            }
            System.out.println();
        }

        static void createSimpleAnalyzer(String queryStr) throws IOException {
            SimpleAnalyzer analyzer = new SimpleAnalyzer(Version.LUCENE_42);
            StringReader reader = new StringReader(queryStr);
            TokenStream tokenStream = analyzer.tokenStream("", reader);
            CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            System.out.print("Simple Analyzer => : ");
            while (tokenStream.incrementToken()) {
                System.out.print(charTermAttribute.toString() + ", ");
            }
            System.out.println();
        }

        static void deleteDocument(IndexWriter writer, String ISBN) throws IOException {

            writer.deleteDocuments(new Term("isbn", ISBN));
            writer.commit();
            writer.close();

        }

        static void deleteAllDocuments(IndexWriter writer) throws IOException {
            writer.deleteAll();
            writer.commit();
            writer.close();
        }

        static void updateDocument(IndexWriter writer, String ISBN, String title) throws IOException {
            Document document = new Document();
            document.add(new StringField("isbn", ISBN, Field.Store.YES));
            document.add(new TextField("title", title, Field.Store.YES));
            writer.updateDocument(new Term("isbn", ISBN), document);
            writer.commit();
            writer.close();
        }
    }

}