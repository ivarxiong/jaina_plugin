package jaina.intellij.plugin.processor;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import jaina.intellij.plugin.model.LombokProblem;
import jaina.intellij.plugin.model.ProblemBuilder;
import jaina.intellij.plugin.model.ProblemNewBuilder;
import jaina.intellij.plugin.util.PsiAnnotationSearchUtil;
import jaina.intellij.plugin.util.PsiAnnotationUtil;
import jaina.intellij.plugin.util.PsiClassUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Base lombok processor class for field annotations
 *
 * @author Plushnikov Michail
 */
public abstract class AbstractFieldProcessor extends AbstractProcessor implements FieldProcessor {

    AbstractFieldProcessor(@NotNull Class<? extends PsiElement> supportedClass, @NotNull Class<? extends Annotation> supportedAnnotationClass) {
        super(supportedClass, supportedAnnotationClass);
    }

    AbstractFieldProcessor(@NotNull Class<? extends PsiElement> supportedClass, @NotNull Class<? extends Annotation> supportedAnnotationClass, Class<? extends Annotation>... equivalentAnnotationClasses) {
        super(supportedClass, supportedAnnotationClass, equivalentAnnotationClasses);
    }

    @NotNull
    @Override
    public List<? super PsiElement> process(@NotNull PsiClass psiClass) {
        List<? super PsiElement> result = new ArrayList<PsiElement>();
        for (PsiField psiField : PsiClassUtil.collectClassFieldsIntern(psiClass)) {
            PsiAnnotation psiAnnotation = PsiAnnotationSearchUtil.findAnnotation(psiField, getSupportedAnnotationClasses());
            if (null != psiAnnotation) {
                if (true) {
                    generatePsiElements(psiField, psiAnnotation, result);
                }
            }
        }
        return result;
    }

    @NotNull
    public Collection<PsiAnnotation> collectProcessedAnnotations(@NotNull PsiClass psiClass) {
        List<PsiAnnotation> result = new ArrayList<PsiAnnotation>();
        for (PsiField psiField : PsiClassUtil.collectClassFieldsIntern(psiClass)) {
            PsiAnnotation psiAnnotation = PsiAnnotationSearchUtil.findAnnotation(psiField, getSupportedAnnotationClasses());
            if (null != psiAnnotation) {
                result.add(psiAnnotation);
            }
        }
        return result;
    }

    @NotNull
    @Override
    public Collection<LombokProblem> verifyAnnotation(@NotNull PsiAnnotation psiAnnotation) {
        Collection<LombokProblem> result = Collections.emptyList();

        PsiField psiField = PsiTreeUtil.getParentOfType(psiAnnotation, PsiField.class);
        if (null != psiField) {
            ProblemNewBuilder problemNewBuilder = new ProblemNewBuilder();
            validate(psiAnnotation, psiField, problemNewBuilder);
            result = problemNewBuilder.getProblems();
        }

        return result;
    }

    protected abstract boolean validate(@NotNull PsiAnnotation psiAnnotation, @NotNull PsiField psiField, @NotNull ProblemBuilder builder);

    protected abstract void generatePsiElements(@NotNull PsiField psiField, @NotNull PsiAnnotation psiAnnotation, @NotNull List<? super PsiElement> target);

    protected void copyAnnotations(final PsiField fromPsiElement, final PsiModifierList toModifierList, final Pattern... patterns) {
        final Collection<String> annotationsToCopy = PsiAnnotationUtil.collectAnnotationsToCopy(fromPsiElement, patterns);
        for (String annotationFQN : annotationsToCopy) {
            toModifierList.addAnnotation(annotationFQN);
        }
    }

}
