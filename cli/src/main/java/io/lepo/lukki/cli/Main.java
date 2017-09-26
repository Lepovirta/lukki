package io.lepo.lukki.cli;

import io.lepo.lukki.core.Crawler;

public class Main {
    public static void main(String[] args) {
        Crawler crawler = new Crawler();
        crawler.run("https://lepo.io/");
    }
}
