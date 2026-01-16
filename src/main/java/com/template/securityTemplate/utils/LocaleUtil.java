package com.template.securityTemplate.utils;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class LocaleUtil {
    public static final ZoneId defaultZoneId = ZoneId.of("America/Mexico_City");
    public static final Locale defaultLocale = new Locale("es", "MX");
    public static final DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd", defaultLocale);
    public static final DateTimeFormatter formatterDateTimeToLog = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss", defaultLocale);
    public static final DateTimeFormatter formatterDateTime = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", defaultLocale);
    public static final DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("HH:mm:ss", defaultLocale);
}
