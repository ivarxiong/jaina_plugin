package jaina.intellij.plugin.provider;

import com.intellij.psi.PsiAnnotation;
import jaina.intellij.plugin.processor.Processor;

public class LombokProcessorData {
  private final Processor processor;
  private final PsiAnnotation psiAnnotation;

  public LombokProcessorData(Processor processor, PsiAnnotation psiAnnotation) {
    this.processor = processor;
    this.psiAnnotation = psiAnnotation;
  }

  public Processor getProcessor() {
    return processor;
  }

  public PsiAnnotation getPsiAnnotation() {
    return psiAnnotation;
  }
}
