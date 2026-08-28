package com.disaster.service;

import com.disaster.config.EmailProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.disaster.service.EmailTemplateService.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the branded HTML email template builder: required sections,
 * every supported email type, HTML escaping and the plain-text stripping used
 * for the text part of multipart messages.
 */
class EmailTemplateServiceTest {

    private EmailTemplateService service;

    @BeforeEach
    void setUp() {
        EmailProperties properties = new EmailProperties();
        service = new EmailTemplateService(properties);
    }

    @Test
    void renderIncludesAllRequiredSections() {
        Map<String, String> fields = Map.of(
                FIELD_MESSAGE, "Severe flood reported",
                FIELD_DISASTER_TYPE, "Flood",
                FIELD_SEVERITY, "High",
                FIELD_LOCATION, "Assam, India",
                FIELD_TIME, "2026-08-06 10:30 IST",
                FIELD_SAFETY_INSTRUCTIONS, "Move to higher ground.\nDo not wade through floodwater.",
                FIELD_EMERGENCY_CONTACT, "112");

        String html = service.render(EmailType.DISASTER_ALERT, fields);

        assertTrue(html.contains("AI Disaster Management System"), "branding");
        assertTrue(html.contains("Disaster Alert"), "type label");
        assertTrue(html.contains("Flood"), "disaster type");
        assertTrue(html.contains("High"), "severity");
        assertTrue(html.contains("Assam, India"), "location");
        assertTrue(html.contains("2026-08-06 10:30 IST"), "time");
        assertTrue(html.contains("Move to higher ground."), "safety instructions");
        assertTrue(html.contains("112"), "emergency contact");
    }

    @Test
    void renderEscapesUserSuppliedHtml() {
        Map<String, String> fields = Map.of(
                FIELD_LOCATION, "<script>alert('xss')</script>",
                FIELD_DISASTER_TYPE, "Flood <b>");

        String html = service.render(EmailType.WEATHER_ALERT, fields);

        assertTrue(html.contains("&lt;script&gt;"));
        assertFalse(html.contains("<script>"));
    }

    @Test
    void allSupportedEmailTypesRender() {
        for (EmailType type : EmailType.values()) {
            String html = service.render(type, Map.of(FIELD_MESSAGE, "Test"));
            assertTrue(html.contains(type.getLabel()), "label for " + type);
        }
    }

    @Test
    void wrapProducesBrandedHtmlForPlainBody() {
        String html = service.wrap("Test subject", "Hello world");

        assertTrue(html.startsWith("<!DOCTYPE html>"));
        assertTrue(html.contains("AI Disaster Management System"));
        assertTrue(html.contains("Hello world"));
        assertTrue(html.contains("112"), "default emergency contact");
    }

    @Test
    void wrapEscapesPlainTextAndPreservesLineBreaks() {
        String html = service.wrap("Subject", "Line 1\n<script>alert(1)</script>");

        assertTrue(html.contains("Line 1<br>"));
        assertTrue(html.contains("&lt;script&gt;"));
        assertFalse(html.contains("<script>"));
    }

    @Test
    void stripHtmlRemovesTagsAndUnescapesEntities() {
        String text = service.stripHtml("<p>Hello <strong>World</strong> &amp; friends</p>");
        assertEquals("Hello World & friends", text);
    }

    @Test
    void stripHtmlHandlesNullAndPlainText() {
        assertEquals("", service.stripHtml(null));
        assertEquals("plain", service.stripHtml("plain"));
    }

    @Test
    void escapeHtmlNeutralizesAngleBracketsAndQuotes() {
        assertEquals("&lt;b&gt;&quot;x&quot;&lt;/b&gt;", EmailTemplateService.escapeHtml("<b>\"x\"</b>"));
    }
}
