package dev.sid.webpage_ai_rag.parser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.ToHTMLContentHandler;
import org.apache.tika.sax.ToXMLContentHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

@Component()
public class HtmlParser {
  private final String docUrl = "https://google.com";
  private final String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36";

  public String parseBodyToHTML() {
    ContentHandler handler = new ToHTMLContentHandler();
    AutoDetectParser parser = new AutoDetectParser();
    Metadata metadata = new Metadata();

    try (InputStream stream = new URI(docUrl).toURL().openStream()) {
      parser.parse(stream, handler, metadata);
      return handler.toString();
    } catch (IOException | SAXException | TikaException | URISyntaxException ex) {
      throw new RuntimeException(ex);
    }
  }

  public String loadHTML() {
    Document doc = null;
    try {
      doc = Jsoup.connect(docUrl)
          .userAgent(userAgent)
          .get();
      final Elements allElements = doc.getAllElements();
      return allElements.stream()
          .map(Element::textNodes)
          .flatMap(Collection::stream)
          .map(TextNode::text)
          .collect(Collectors.joining("\n"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
