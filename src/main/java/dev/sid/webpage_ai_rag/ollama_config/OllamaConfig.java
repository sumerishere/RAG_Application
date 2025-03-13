//package dev.sid.webpage_ai_rag.ollama_config;
//
//import java.lang.reflect.Constructor;
//import java.util.Map;
//
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.MediaType;
//import org.springframework.web.reactive.function.client.WebClient;
//
//import reactor.core.publisher.Flux;
//
//
//@Configuration
//public class OllamaConfig {
//	
//	 @Bean
//	    public ChatClient chatClient() {
//	        return new Builder()
//	            .baseUrl("http://localhost:11434")
//	            .model("llama3.2:latest")
//	            .build();
//	    }
//	    
//	    public static class Builder {
//	        private String baseUrl;
//	        private String model;
//	        
//	        public Builder baseUrl(String baseUrl) {
//	            this.baseUrl = baseUrl;
//	            return this;
//	        }
//	        
//	        public Builder model(String model) {
//	            this.model = model;
//	            return this;
//	        }
//	        
//	        public ChatClient build() {
//	            // Create WebClient with the base URL
//	            WebClient webClient = WebClient.builder()
//	                .baseUrl(baseUrl)
//	                .build();
//	            
//	            // Here you would typically create the OllamaChatClient with the WebClient
//	            // Since we don't know the exact class structure, we'll use reflection
//	            try {
//	                Class<?> ollamaChatClientClass = Class.forName("org.springframework.ai.ollama.client.OllamaChatClient");
//	                Constructor<?> constructor = ollamaChatClientClass.getConstructor(WebClient.class, String.class);
//	                return (ChatClient) constructor.newInstance(webClient, model);
//	            } catch (Exception e) {
//	                throw new RuntimeException("Failed to create OllamaChatClient: " + e.getMessage(), e);
//	            }
//	        }
//	    }
    
//    @Bean
//    public WebClient ollamaWebClient() {
//        return WebClient.builder()
//            .baseUrl("http://localhost:11434")
//            .build();
//    }
//    
//    @Bean
//    public ChatService chatService(WebClient ollamaWebClient) {
//        return new OllamaChatService(ollamaWebClient);
//    }
//    
//    public static class OllamaChatService implements ChatService {
//        private final WebClient webClient;
//        
//        public OllamaChatService(WebClient webClient) {
//            this.webClient = webClient;
//        }
//        
//        public Flux<String> chat(String query) {
//            Map<String, Object> requestBody = Map.of(
//                "model", "llama3.2:latest",
//                "prompt", query,
//                "stream", true
//            );
//            
//            return webClient.post()
//                .uri("/api/generate")
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(requestBody)
//                .retrieve()
//                .bodyToFlux(String.class);
//        }
//    }
//    
//    public interface ChatService {
//        Flux<String> chat(String query);
//    }
//}