package org.example.shared.util;

import org.slf4j.Logger;

public class ExceptionLogUtil {

  public static void trace(Logger log, Exception e, String thread) {
    log.trace("[{}] {}: {}", thread, e.getClass().getSimpleName(), e.getMessage(), e);
  }

  public static void debug(Logger log, Exception e, String thread) {
    log.debug("[{}] {}: {}", thread, e.getClass(), e.getMessage(), e);
  }

  public static void info(Logger log, Exception e, String thread) {
    log.info("[{}] {}: {}", thread, e.getClass().getSimpleName(), e.getMessage());
  }

  public static void warn(Logger log, Exception e, String thread) {
    log.warn("[{}] {}: {}", thread, e.getClass().getSimpleName(), e.getMessage(), e);
  }

  public static void error(Logger log, Exception e, String thread) {
    log.error("[{}] {}: {}", thread, e.getClass().getSimpleName(), e.getMessage(), e);
  }
}
