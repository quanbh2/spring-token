package net.friend.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Aspect
@Component
@Slf4j
public class AOPLogging {

  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface NoLogging {}

  @Target(ElementType.PARAMETER)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface MaskedParam {
    String maskedSpell();
  }

  private SpelExpressionParser spelExpressionParser = new SpelExpressionParser();

  @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
  public void springBeanPointcut() {
    // Method is empty as this is just a Pointcut, the implementations are in the advices.
  }

  @Pointcut("within(net.friend.controller..*)")
  public void withinControllers() {
    // Method is empty as this is just a Pointcut, the implementations are in the advices.
  }

  @Pointcut("!@annotation(net.friend.aop.AOPLogging.NoLogging)")
  private void logEnabled() {
      // Method is empty as this is just a Pointcut, the implementations are in the advices.
  }

    /**
     *
     * @param joinPoint
     * Log info before executing controller
     */
  @Before("withinControllers() && springBeanPointcut() && logEnabled()")
  public void logBefore(JoinPoint joinPoint) {

    try {

      HttpServletRequest request =
          ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

      StringBuilder msgBuilder = new StringBuilder();

      String methodName = jointPointName(joinPoint);
      String requestMethod = request.getMethod();
      String name = request.getUserPrincipal().getName();
      String browser = getDeviceName(request.getHeader("User-Agent"));

      //get user's remote address
      String remoteAddr = "";
      if (request != null) {
        remoteAddr = request.getHeader("X-FORWARDED-FOR");
        if (remoteAddr == null || "".equals(remoteAddr)) {
          remoteAddr = request.getRemoteAddr();
        }
      }

      msgBuilder
          .append(methodName)
          .append(" START: ")
          .append(requestMethod)
          .append(" - ")
          .append(" username: ")
          .append(name)
          .append(" - device: ")
          .append(browser)
          .append(" - address: ")
          .append(remoteAddr);

      // get all parameters of method
      Object[] args = joinPoint.getArgs();
      MethodSignature codeSignature = (MethodSignature) joinPoint.getSignature();
      int count = args.length;
      for (int i = 0; i < count; i++) {
        // get name of parameter
        msgBuilder.append(", ").append(codeSignature.getParameterNames()[i]).append(": ");
        MaskedParam maskedParam =
            codeSignature.getMethod().getParameters()[i].getAnnotation(MaskedParam.class);

        // get value of parameter
        if (maskedParam != null) {
          try {
            msgBuilder
                .append(" Masked:")
                .append(spelExpressionParser.parseRaw(maskedParam.maskedSpell()).getValue(args[i]));
          } catch (Exception e) {
            // no-op
          }
        } else {
          msgBuilder.append(args[i]);
        }
      }

      log.info(msgBuilder.toString());
    } catch (HttpClientErrorException e) {
      System.out.println("There is a client error!");
    } catch (HttpServerErrorException e) {
      System.out.println("There is a server error!");
    } catch (NullPointerException e) {
      System.out.println("Not supported request!");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

    /**
     *
     * @param joinPoint
     * @return
     */
  private String jointPointName(JoinPoint joinPoint) {
    return joinPoint.getSignature().toShortString();
  }

    /**
     *
     * @param userAgent
     * Get device name and browser
     * @return
     */
  private String getDeviceName(String userAgent) {
    if (userAgent == null) return "Unidentified";

    StringBuilder device = new StringBuilder();

    if (userAgent.contains("Mobi")) device.append("Mobile ");
    else device.append("PC ");

    if (userAgent.contains("Chrome")
        && !userAgent.contains("Chromium")
        && !userAgent.contains("FB")
        && !userAgent.contains("Edge")) device.append("Chrome");
    else if (userAgent.contains("Safari")
        && !(userAgent.contains("Chrome") || userAgent.contains("Chromium")))
      device.append("Safari");
    else if (userAgent.contains("SamsungBrowser")) device.append("Samsung Browser");
    else if (userAgent.contains("Firefox") && !userAgent.contains("Seamonkey"))
      device.append("Firefox");
    else if (userAgent.contains("Edge")) device.append("Edge");
    else if (userAgent.contains("OPR") || userAgent.contains("Opera")) device.append("Opera");
    else if (userAgent.contains("UCBrowser")) device.append("UCBrowser");
    else if (userAgent.contains("MSIE")) device.append("Internet Explorer IE");
    else if (userAgent.contains("Seamonkey")) device.append("Seamonkey");
    else if (userAgent.contains("Chromium")) device.append("Chromium");
    else if (userAgent.contains("coc_coc_browser")) device.append("Coc Coc");
    else if (userAgent.contains("Brave")) device.append("Brave");
    else if (userAgent.contains("iPhone")
        || userAgent.contains("iPad")
        || userAgent.contains("iPod")) device.append("Default IOS");
    else if (userAgent.contains("Android") && !userAgent.contains("Windows Phone"))
      device.append("Default Android");
    else device.append("Default Browser Other OS");

    return device.toString();
  }
}
