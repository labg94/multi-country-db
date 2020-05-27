package com.example.demo.config;

public class CountryContext {
    private static ThreadLocal<Object> currentCountry = new ThreadLocal<>();

    public static void setCurrentCountry(Object country) {
        currentCountry.set(country);
    }

    public static Object getCurrentCountry() {
        return currentCountry.get();
    }
}
