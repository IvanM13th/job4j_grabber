package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {
    private static final String SOURCE_LINK = "https://career.habr.com/";
    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);
    private static final int PAGES = 5;
    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    private String retrieveDescription(String link) {
        String text;
        try {
            text = Jsoup.connect(link).get()
                    .select(".style-ugc")
                    .text();
        } catch (IOException e) {
            throw new IllegalArgumentException();
        }
        return text;
    }

    @Override
    public List<Post> list(String link) {
        List<Post> postList = new ArrayList<>();
        for (int i = 1; i <= PAGES; i++) {
            try {
                Connection connection = Jsoup.connect(String.format("%s?page=%s", link, i));
                Document document = connection.get();
                Elements rows = document.select(".vacancy-card__inner");
                rows.forEach(row -> {
                    postList.add(postParse(row));
                });
            } catch (IOException e) {
                throw new IllegalArgumentException();
            }
        }
        return postList;
    }

    private Post postParse(Element e) {
        Post post = new Post();
        post.setTitle(e.select(".vacancy-card__title").first().text());
        post.setDescription(retrieveDescription(String.format("%s%s", SOURCE_LINK, e.select(".vacancy-card__title").first().child(0).attr("href"))));
        post.setLink(String.format("%s%s", SOURCE_LINK, e.select(".vacancy-card__title").first().child(0).attr("href")));
        post.setCreated(dateTimeParser.parse(e.select(".vacancy-card__date").first().child(0).attr("datetime")));
        return post;
    }
}