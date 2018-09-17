package jaina.intellij.plugin.util;

import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * @author Plushnikov Michail
 */
public class LombokProcessorUtil {

  @Nullable
  public static String getMethodModifier(@NotNull PsiAnnotation psiAnnotation) {
    return getLevelVisibility(psiAnnotation, "value");
  }

  @Nullable
  public static String getAccessVisibility(@NotNull PsiAnnotation psiAnnotation) {
    return getLevelVisibility(psiAnnotation, "access");
  }

  @Nullable
  public static String getLevelVisibility(@NotNull PsiAnnotation psiAnnotation) {
    return getLevelVisibility(psiAnnotation, "level");
  }

  @Nullable
  public static String getLevelVisibility(@NotNull PsiAnnotation psiAnnotation, @NotNull String parameter) {
    return convertAccessLevelToJavaModifier(PsiAnnotationUtil.getStringAnnotationValue(psiAnnotation, parameter));
  }

  public static boolean isLevelVisible(@NotNull PsiAnnotation psiAnnotation) {
    return null != getLevelVisibility(psiAnnotation);
  }

  public static Collection<String> getOnX(@NotNull PsiAnnotation psiAnnotation, @NotNull String parameterName) {
    PsiAnnotationMemberValue onXValue = psiAnnotation.findAttributeValue(parameterName);
    if (!(onXValue instanceof PsiAnnotation)) {
      return Collections.emptyList();
    }
    Collection<PsiAnnotation> annotations = PsiAnnotationUtil.getAnnotationValues((PsiAnnotation) onXValue, "value", PsiAnnotation.class);
    Collection<String> annotationStrings = new ArrayList<String>();
    for (PsiAnnotation annotation : annotations) {
      PsiAnnotationParameterList params = annotation.getParameterList();
      annotationStrings.add(PsiAnnotationSearchUtil.getSimpleNameOf(annotation) + params.getText());
    }
    return annotationStrings;
  }

  @Nullable
  private static String convertAccessLevelToJavaModifier(String value) {
    if (null == value || value.isEmpty()) {
      return PsiModifier.PUBLIC;
    }

    if ("PUBLIC".equals(value)) {
      return PsiModifier.PUBLIC;
    }
    if ("MODULE".equals(value)) {
      return PsiModifier.PACKAGE_LOCAL;
    }
    if ("PROTECTED".equals(value)) {
      return PsiModifier.PROTECTED;
    }
    if ("PACKAGE".equals(value)) {
      return PsiModifier.PACKAGE_LOCAL;
    }
    if ("PRIVATE".equals(value)) {
      return PsiModifier.PRIVATE;
    }
    if ("NONE".equals(value)) {
      return null;
    }
    return null;
  }

}
