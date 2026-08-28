package com.disaster.service;

import com.disaster.config.EmailProperties;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Builds professional, branded HTML email bodies for every supported email
 * type. Callers pass plain, unstructured text through {@link #wrap(String, String)}
 * and fully-structured alerts through {@link #render(EmailType, Map)}.
 *
 * <p>All dynamic values are HTML-escaped so user-supplied content (locations,
 * descriptions, report text) can never inject markup into outgoing email.</p>
 */
@Service
public class EmailTemplateService {

    public static final String FIELD_MESSAGE = "message";
    public static final String FIELD_DISASTER_TYPE = "disasterType";
    public static final String FIELD_SEVERITY = "severity";
    public static final String FIELD_LOCATION = "location";
    public static final String FIELD_TIME = "time";
    public static final String FIELD_SAFETY_INSTRUCTIONS = "safetyInstructions";
    public static final String FIELD_EMERGENCY_CONTACT = "emergencyContact";

    private static final String BRAND = "AI Disaster Management System";

    private final EmailProperties properties;

    public EmailTemplateService(EmailProperties properties) {
        this.properties = properties;
    }

    /**
     * Renders a complete branded HTML email for a specific message type. The
     * supplied fields are placed into labelled sections (disaster type,
     * severity, location, time, safety instructions, emergency contact).
     */
    public String render(EmailType type, Map<String, String> fields) {
        Map<String, String> safe = fields == null ? Map.of() : fields;

        StringBuilder message = new StringBuilder();
        message.append("<p style=\"margin:0 0 16px;color:#333333;font-size:15px;line-height:1.6;\">")
                .append(escapeHtml(value(safe, FIELD_MESSAGE, "")))
                .append("</p>");

        StringBuilder details = new StringBuilder();
        details.append(detailRow("Disaster Type", value(safe, FIELD_DISASTER_TYPE, "N/A")));
        details.append(detailRow("Severity", value(safe, FIELD_SEVERITY, "N/A")));
        details.append(detailRow("Location", value(safe, FIELD_LOCATION, "N/A")));
        details.append(detailRow("Time", value(safe, FIELD_TIME, "N/A")));
        details.append(detailRow("Safety Instructions", value(safe, FIELD_SAFETY_INSTRUCTIONS, "")));
        details.append(detailRow("Emergency Contact", value(safe, FIELD_EMERGENCY_CONTACT, "")));

        return layout(type.getLabel(), type.getLabel(), message.toString(), details.toString());
    }

    /**
     * Wraps an unstructured plain-text body into the same branded HTML layout,
     * so every email that flows through {@link ResendEmailProvider} has a
     * consistent look even when callers only pass plain text.
     */
    public String wrap(String subject, String plainBody) {
        String escaped = escapeHtml(plainBody == null ? "" : plainBody)
                .replace("\r\n", "\n").replace("\n", "<br>");
        return layout(subject, "Notification",
                "<p style=\"margin:0 0 16px;color:#333333;font-size:15px;line-height:1.6;\">" + escaped + "</p>",
                "");
    }

    /** Strips all HTML tags and unescapes common entities to build a text part. */
    public String stripHtml(String html) {
        if (html == null) {
            return "";
        }
        String text = html.replaceAll("(?s)<[^>]*>", " ");
        text = text.replaceAll("\\s+", " ").trim();
        text = text.replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'");
        return text;
    }

    /** HTML-escapes a value so it is safe to inline into email markup. */
    public static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String layout(String title, String typeLabel, String messageHtml, String detailsHtml) {
        String emergencyContact = escapeHtml(properties.getEmergencyContact());
        String contactRow = emergencyContact.isBlank() ? "" :
                "<br>Emergency Contact: <strong>" + emergencyContact + "</strong>";

        return "<!DOCTYPE html>"
                + "<html lang=\"en\"><head><meta charset=\"UTF-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                + "<title>" + escapeHtml(title) + "</title></head>"
                + "<body style=\"margin:0;padding:0;background:#f4f6f8;font-family:Arial,Helvetica,sans-serif;\">"
                + "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#f4f6f8;\">"
                + "<tr><td align=\"center\" style=\"padding:32px 16px;\">"
                + "<table role=\"presentation\" width=\"600\" cellpadding=\"0\" cellspacing=\"0\""
                + " style=\"max-width:600px;width:100%;background:#ffffff;border-radius:8px;overflow:hidden;border:1px solid #e3e8ee;\">"
                + "<tr><td style=\"background:#b91c1c;padding:24px 28px;\">"
                + "<div style=\"color:#ffffff;font-size:20px;font-weight:bold;\">" + BRAND + "</div>"
                + "<div style=\"color:#fcd34d;font-size:13px;margin-top:4px;\">" + escapeHtml(typeLabel) + "</div>"
                + "</td></tr>"
                + "<tr><td style=\"padding:28px;\">"
                + messageHtml
                + (detailsHtml.isBlank() ? "" : "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse:collapse;\">" + detailsHtml + "</table>")
                + "</td></tr>"
                + "<tr><td style=\"background:#f8fafc;padding:18px 28px;border-top:1px solid #e3e8ee;\">"
                + "<div style=\"color:#64748b;font-size:12px;line-height:1.6;\">"
                + "This is an automated message from " + BRAND + "."
                + contactRow
                + "<br>Please do not reply to this email."
                + "</div></td></tr>"
                + "</table></td></tr></table></body></html>";
    }

    private String detailRow(String label, String value) {
        String escaped = escapeHtml(value);
        String content = escaped.contains("\n")
                ? "<span style=\"white-space:pre-line;\">" + escaped + "</span>"
                : escaped;
        return "<tr>"
                + "<td style=\"padding:8px 0;color:#64748b;font-size:13px;font-weight:bold;width:160px;vertical-align:top;\">"
                + label + "</td>"
                + "<td style=\"padding:8px 0;color:#333333;font-size:14px;\">" + content + "</td>"
                + "</tr>";
    }

    private String value(Map<String, String> fields, String key, String fallback) {
        String v = fields.get(key);
        return v == null ? fallback : v;
    }
}
