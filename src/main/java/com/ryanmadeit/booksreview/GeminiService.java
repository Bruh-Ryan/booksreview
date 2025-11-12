package com.ryanmadeit.booksreview;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

@Service
public class GeminiService {

  // Uses your existing property key
  @Value("${genai.modelName:gemini-2.0-flash}")
  private String modelName;

  /**
   * Author profile: concise and factual, 1–2 short paragraphs.
   */
  public String authorSummary(String authorName, String knownFacts) {
    String apiKey = System.getenv("GOOGLE_GENAI_API_KEY");
    if (apiKey == null || apiKey.isBlank()) {
      return "Unable to generate author summary: missing GOOGLE_GENAI_API_KEY";
    }

    String prompt = """
      Write a brief, factual profile of the author "%s" in 1–2 short paragraphs.
      Summarize major achievements, themes, and intellectual style; avoid speculation.
      If helpful, use these known facts (may be partial): %s
      Keep it concise, neutral, and readable.
    """.formatted(authorName, knownFacts == null ? "" : knownFacts);

    try (Client client = Client.builder().apiKey(apiKey).build()) {
      GenerateContentResponse resp = client.models.generateContent(modelName, prompt, null);
      String text = resp != null ? resp.text() : null;
      return (text == null || text.isBlank()) ? "Description is currently unavailable." : text.trim();
    } catch (Exception e) {
      return "Unable to generate author summary: " + e.getMessage();
    }
  }

  /**
   * Book brief: 3 short paragraphs (summary, reader crux/themes, public traction).
   */
  public String bookBrief(String title, String authors, String knownFacts) {
    String apiKey = System.getenv("GOOGLE_GENAI_API_KEY");
    if (apiKey == null || apiKey.isBlank()) {
      return "Unable to generate book brief: missing GOOGLE_GENAI_API_KEY";
    }

    String prompt = """
      You are a concise literary analyst.
      Task: Write exactly 3 short paragraphs about the book "%s" by %s.
      - Paragraph 1: A brief, spoiler-aware summary (2–3 sentences).
      - Paragraph 2: The common crux while reading (themes, tone, pacing, what readers feel).
      - Paragraph 3: Sales/traction and public sentiment (general perception; avoid unverifiable numbers).
      If facts are unknown, say so briefly instead of inventing details.
      Extra context (optional, may be partial): %s
    """.formatted(title, authors == null ? "Unknown" : authors, knownFacts == null ? "" : knownFacts);

    try (Client client = Client.builder().apiKey(apiKey).build()) {
      GenerateContentResponse resp = client.models.generateContent(modelName, prompt, null);
      String text = resp != null ? resp.text() : null;
      return (text == null || text.isBlank()) ? "Description is currently unavailable." : text.trim();
    } catch (Exception e) {
      return "Unable to generate book brief: " + e.getMessage();
    }
  }

  /**
   * Convenience for your earlier controller signature.
   */
  public String describeBook(Books book) {
    return bookBrief(book.getTitle(), book.getAuthors(), buildKnownFacts(book));
  }

  public String describeAuthor(String name) {
    return authorSummary(name, "");
  }

  private String buildKnownFacts(Books b) {
    String avg = (b.getAverageRating() > 0) ? String.format("%.2f", b.getAverageRating()) : "-";
    String cnt = (b.getRatingsCount() > 0) ? Integer.toString(b.getRatingsCount()) : "-";
    return "Publisher=" + nvl(b.getPublisher())
         + "; Published=" + nvl(b.getPublishedDate())
         + "; AvgRating=" + avg
         + "; RatingsCount=" + cnt;
  }

  private static String nvl(String s) { return s == null ? "-" : s; }
}
