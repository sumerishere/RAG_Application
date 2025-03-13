//package dev.sid.webpage_ai_rag.service;
//
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Flux;
//
//import java.util.Map;
//import java.util.HashMap;
//import java.util.List;
//
//@Service
//public class OllamaService {
//    private final WebClient webClient;
//    private final String model;
//
//    public OllamaService() {
//        this.webClient = WebClient.builder()
//            .baseUrl("http://localhost:11434")
//            .build();
//        
//        // Use TinyLlama model
//        this.model = "tinyllama";
//        
//        System.out.println("OllamaService initialized with model: " + model);
//    }
//
//    public Flux<String> generateResponse(String query, List<String> documents) {
//        // Simple prompt construction
//        String context = String.join("\n", documents);
//        String prompt = "Context: " + context + "\nQuestion: " + query + "\nAnswer:";
//        
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("model", model);
//        requestBody.put("prompt", prompt);
//        requestBody.put("stream", false);  // Don't use streaming
//        
//        System.out.println("Sending request to Ollama with model: " + model);
//        
//        return webClient.post()
//            .uri("/api/generate")
//            .bodyValue(requestBody)
//            .retrieve()
//            .bodyToMono(Map.class)
//            .map(response -> {
//                System.out.println("Response received from Ollama");
//                if (response.containsKey("response")) {
//                    return response.get("response").toString();
//                }
//                return "No response content found in Ollama output";
//            })
//            .onErrorReturn("Error connecting to Ollama service. Make sure Ollama is running and the model is available.")
//            .flux();
//    }
//}