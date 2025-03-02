package dev.sid.webpage_ai_rag.parser;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.stereotype.Component;


@Component()
public class HtmlParser {
  private final String docUrl = "https://testcontainers.com/guides/introducing-testcontainers/";

  public List<Document> getHTMLDocuments() {
    var documentReader = new TikaDocumentReader(docUrl);
    return documentReader.read();
  }

  public String loadHTML() {
    org.jsoup.nodes.Document doc = null;
    try {
      String userAgent =
          "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36";
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
