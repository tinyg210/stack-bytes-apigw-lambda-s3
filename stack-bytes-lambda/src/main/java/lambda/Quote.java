package lambda;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Quote {

  private String author;
  private String text;

  public Quote(String jsonQuote) {
    Gson gson = new Gson();
    Quote input = gson.fromJson(jsonQuote, Quote.class);
    this.author = input.getAuthor();
    this.text = input.getText();
  }

  public String toString() {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    return gson.toJson(this);
  }


  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }
}