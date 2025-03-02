package dev.sid.webpage_ai_rag.controller;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.sid.webpage_ai_rag.parser.HtmlParser;
import dev.sid.webpage_ai_rag.parser.PdfParser;
import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Flux;

@RestController()
@RequestMapping(value = "/web-ai-rag")
public class ChatController {

  private final Logger logger = Logger.getLogger("ChatController");

  @Value("classpath:prompts/jamf-guide-reference.st")
  private Resource jamfTemplateResource;

  private final ChatClient chatClient;
  private final HtmlParser htmlParser;
  private final PdfParser pdfParser;
  private final VectorStore vectorStore;

  public ChatController(ChatClient.Builder builder, HtmlParser htmlParser, PdfParser pdfParser, VectorStore vectorStore) {
    this.chatClient = builder.build();
    this.htmlParser = htmlParser;
    this.pdfParser = pdfParser;
    this.vectorStore = vectorStore;
  }

  @GetMapping(value = "/chat")
  public Flux<String> testClient(@RequestParam(value = "query", defaultValue = "Hi") final String query) {
    var promptTemplate = new PromptTemplate(jamfTemplateResource);
    Map<String, Object> promptParams = Map.of("input", query, "documents", String.join("\n", findSimilaritySearch(query)));
    return chatClient.prompt(promptTemplate.create(promptParams))
        .stream()
        .content();
  }

  @PostConstruct()
  public void postConstruct() {
    var tokenTextSplitter = new TokenTextSplitter();
    var documents = htmlParser.getHTMLDocuments();
    documents.addAll(pdfParser.getDocsFromPdf());
    logger.info(documents.toString());
    var splitText = tokenTextSplitter.apply(documents);
    vectorStore.accept(splitText);
  }



  private List<String> findSimilaritySearch(final String message) {
    return Stream.ofNullable(vectorStore.similaritySearch(SearchRequest.builder().query(message).topK(3).build()))
        .flatMap(Collection::stream)
        .map(Document::getText)
        .toList();
  }
}
