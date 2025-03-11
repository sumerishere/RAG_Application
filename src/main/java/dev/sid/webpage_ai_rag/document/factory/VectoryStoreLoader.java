package dev.sid.webpage_ai_rag.document.factory;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import dev.sid.webpage_ai_rag.controller.ChatController;
import jakarta.annotation.PostConstruct;

@Component()
public class VectoryStoreLoader {

  private static final Logger LOGGER = LoggerFactory.getLogger(ChatController.class);

  private final HtmlDocumentFactory htmlDocumentFactory;
  private final PdfDocumentFactory pdfDocumentFactory;
  private final VectorStore vectorStore;

  public VectoryStoreLoader(HtmlDocumentFactory htmlDocumentFactory, PdfDocumentFactory pdfDocumentFactory,
      VectorStore vectorStore) {
    this.htmlDocumentFactory = htmlDocumentFactory;
    this.pdfDocumentFactory = pdfDocumentFactory;
    this.vectorStore = vectorStore;
  }

  @PostConstruct()
  public void postConstruct() {
	  
    var tokenTextSplitter = new TokenTextSplitter();
    LOGGER.info("Loading HTML documents.");
    
    var htmlDocuments = htmlDocumentFactory.getHTMLDocuments();
    LOGGER.info("Loading PDF documents.");
    
    var pdfDocuments = pdfDocumentFactory.getDocsFromPdf();
    
    final List<Document> documents = new ArrayList<>(htmlDocuments);
    documents.addAll(pdfDocuments);
    
    var splitText = tokenTextSplitter.apply(documents);
    
    vectorStore.accept(splitText);
    LOGGER.info("Finished loading documents. Size: {}.",documents.size());
    
  }
}
