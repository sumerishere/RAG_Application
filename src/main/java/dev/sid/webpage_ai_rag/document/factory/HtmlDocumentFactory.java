package dev.sid.webpage_ai_rag.document.factory;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.stereotype.Component;


@Component()
public class HtmlDocumentFactory {
  private final String docUrl = "https://testcontainers.com/guides/introducing-testcontainers/";

  public List<Document> getHTMLDocuments() {
    var documentReader = new TikaDocumentReader(docUrl);
    return documentReader.read();
  }

}
