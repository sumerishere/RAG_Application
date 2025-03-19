package dev.sid.webpage_ai_rag.controller;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.sid.webpage_ai_rag.document.factory.HtmlDocumentFactory;
import dev.sid.webpage_ai_rag.document.factory.PdfDocumentFactory;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@CrossOrigin("*")
@RestController
@RequestMapping(value = "/web-ai-rag")
public class ChatController {

  @Value("classpath:prompts/prompt_template.st")
  private Resource promptTemplateResource;

  private final ChatClient chatClient;
  private final VectorStore vectorStore;
  private final RedisTemplate<String, String> redisTemplate;


  public ChatController(ChatClient.Builder builder, 
		  						HtmlDocumentFactory htmlDocumentFactory, 
		  						PdfDocumentFactory pdfDocumentFactory, 
		  						VectorStore vectorStore,
		  						RedisTemplate<String, String> redisTemplate
		  						) {
    this.chatClient = builder.build();
    this.vectorStore = vectorStore;
    this.redisTemplate = redisTemplate;
    
  }
 
  
  @GetMapping(value = "/chat")
  public Flux<String> testClient(@RequestParam(value = "query", defaultValue = "What is RAG?") final String query) {
      //----- start time of API --------//
      long startTime = System.currentTimeMillis();

      // Step 1: Check Redis cache
      String cachedResponse = redisTemplate.opsForValue().get(query);
      if (cachedResponse != null) {
          System.out.println("========> Response served from Redis cache for query: " + query);
          long endTime = System.currentTimeMillis();
          System.out.println("========> Total Response Time: " + (endTime - startTime) + " ms");
          System.out.println("--------------------------------------------------------");
          return Flux.just(cachedResponse);
      }

      // Log time for vector store query
      long vectorStoreStartTime = System.currentTimeMillis();
      List<String> documents = findSimilaritySearch(query);
      long vectorStoreEndTime = System.currentTimeMillis();
      System.out.println("--------------------------------------------------------");
      System.out.println("========> Calling Ollama service with query :--> " + query);
      System.out.println("========> Vector Store Query Time: " + (vectorStoreEndTime - vectorStoreStartTime) + " ms");

      // Log time for Ollama API call
      long ollamaStartTime = System.currentTimeMillis();

      PromptTemplate promptTemplate = new PromptTemplate(promptTemplateResource);
      Map<String, Object> promptParams = Map.of("input", query, "documents", String.join("\n", documents));

      Flux<String> response = chatClient.prompt(promptTemplate.create(promptParams))
          .stream()
          .content()
          .buffer(1024) // Stream in chunks of 1024 characters
          .flatMap(Flux::fromIterable)
          .subscribeOn(Schedulers.boundedElastic()) // Offload to a separate thread
          .collectList()
          .flatMapMany(responseList -> {
              // Step 2: Cache the response in Redis
              String responseText = String.join("\n", responseList);
              redisTemplate.opsForValue().set(query, responseText, 1, TimeUnit.HOURS); // Cache for 1 hour
              System.out.println("========> Response cached in Redis for query: " + query);
              System.out.println("--------------------------------------------------------");
              return Flux.fromIterable(responseList);
          })
          .onErrorResume(e -> {
              System.err.println("Error calling Ollama service: " + e.getMessage());
              e.printStackTrace();
              return Flux.just("The Ollama service is currently unavailable.");
          });

      long ollamaEndTime = System.currentTimeMillis();
      System.out.println("========> Ollama API Call Time: " + (ollamaEndTime - ollamaStartTime) + " ms");

      //----- end time of API --------//
      long endTime = System.currentTimeMillis();
      System.out.println("========> Total Response Time: " + (endTime - startTime) + " ms");

      return response;
  }

  private List<String> findSimilaritySearch(final String message) {
      return Stream.ofNullable(vectorStore.similaritySearch(SearchRequest.builder().query(message).topK(2).build()))
          .flatMap(Collection::stream)
          .map(Document::getText)
          .distinct()  //prevent duplication in response
          .toList();
  }
  
 
//  @GetMapping(value = "/chat")
//  public Flux<String> testClient(@RequestParam(value = "query", defaultValue = "What is RAG?") final String query) {
//	    
//	  //----- start time of API --------//
//      long startTime = System.currentTimeMillis();
//      
//      // Log time for vector store query
//      long vectorStoreStartTime = System.currentTimeMillis();
//      List<String> documents = findSimilaritySearch(query);
//      long vectorStoreEndTime = System.currentTimeMillis();
//      
//      System.out.println("========> Calling Ollama service with query :--> " + query);
//    
//      System.out.println("========> Vector Store Query Time: " + (vectorStoreEndTime - vectorStoreStartTime) + " ms");
//
//      // Log time for Ollama API call
//      long ollamaStartTime = System.currentTimeMillis();
//      
//      PromptTemplate promptTemplate = new PromptTemplate(promptTemplateResource);
//      
//      Map<String, Object> promptParams = Map.of("input", query, "documents", String.join("\n", documents));
//      
//      Flux<String> response = chatClient.prompt(promptTemplate.create(promptParams))
//          .stream()
//          .content()
//          .buffer(1024) // Stream in chunks of 1024 characters
//          .flatMap(Flux::fromIterable)
//          .subscribeOn(Schedulers.boundedElastic()) // Offload to a separate thread
//          .onErrorResume(e -> {
//              System.err.println("Error calling Ollama service: " + e.getMessage());
//              e.printStackTrace();
//              return Flux.just("The Ollama service is currently unavailable.");
//          });
//      
//      long ollamaEndTime = System.currentTimeMillis();
//      System.out.println("========> Ollama API Call Time: " + (ollamaEndTime - ollamaStartTime) + " ms");
//      
//      //----- end time of API --------//
//      long endTime = System.currentTimeMillis();
//      System.out.println("========> Total Response Time: " + (endTime - startTime) + " ms");
//      
//      //System.out.println("Prompt params: " + promptParams);
//
//      return response;
//    }
  
//    private List<String> findSimilaritySearch(final String message) {
//	   
//	   return Stream.ofNullable(vectorStore.similaritySearch(SearchRequest.builder().query(message).topK(2).build()))
//	        .flatMap(Collection::stream)
//	        .map(Document::getText)
//	        .distinct()  //prevent duplication in response
//	        .toList();
//    }
	  
    
  
	@GetMapping("/generative-ai")
	public ResponseEntity<Resource> getGenerativeAIPdf() {
		
	    Resource resource = new ClassPathResource("docs/Generative-AI-and-LLMs-for-Dummies.pdf");
	
	      if (resource.exists() || resource.isReadable()) {
	          return ResponseEntity.ok()
	              .contentType(MediaType.APPLICATION_PDF)
	              // Remove the Content-Disposition header or change it to "inline"
	              .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
	              .body(resource);
	      } 
	      else {
	          return ResponseEntity.notFound().build();
	      }
	 }
 
}
