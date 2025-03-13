package dev.sid.webpage_ai_rag.document.factory;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class PdfDocumentFactory {

  @Value("classpath:docs/Generative-AI-and-LLMs-for-Dummies.pdf")
  private Resource pdfResource;

  public List<Document> getDocsFromPdf() {
	  
    PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(pdfResource,
        PdfDocumentReaderConfig.builder()
            .withPageTopMargin(0)
            .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                .withNumberOfTopTextLinesToDelete(0)
                .build())
            .withPagesPerDocument(1)
            .build());

    return pdfReader.read();
  }
}
