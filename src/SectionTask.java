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
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.StringReader;

public class SectionTask {
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
